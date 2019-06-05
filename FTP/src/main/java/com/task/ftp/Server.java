package com.task.ftp;

import com.google.common.primitives.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {

    private class ClientStateHolder {

        private static final int HEAD_SIZE = 4;

        private final ByteBuffer inBuffer;
        private final ByteBuffer outBuffer;
        private SocketChannel socketChannel;

        private int toRead;
        private int toWrite;
        private int readed;
        private int writen;
        private int type;
        private String path;


        private ClientStateHolder(SocketChannel channel) {
            socketChannel = channel;
            inBuffer = ByteBuffer.allocate(BUFF_SIZE);
            outBuffer = ByteBuffer.allocate(BUFF_SIZE);
        }

        private int read() {
            try {
                readed += socketChannel.read(inBuffer);
                if (readed >= BUFF_SIZE) {
                    return 1;
                }
            } catch (IOException e) {
                return -1;
            }
            return 0;
        }

        private int write() {
            try {
                writen += socketChannel.write(outBuffer);
            } catch (IOException e) {
                return -1;
            }
            if (!outBuffer.hasRemaining()) {
                return 1;
            }
            return 0;
        }

        private int parseInput() {
            inBuffer.flip();
            type = inBuffer.getInt();
            int size = inBuffer.getInt();
            path = "";
            for (int i = 0; i < size; i++) {
                path += inBuffer.getChar();
            }
            if (type != 1 && type != 2) {
                return -1;
            }

            return 0;
        }

        private SocketChannel getSocketChannel() {
            return socketChannel;
        }

        private List<File> getFileList(File file) {
            if (file.isDirectory()) {
                var list = new ArrayList<File>();
                for (var inner : file.listFiles()) {
                    list.addAll(getFileList(inner));
                }
                return list;
            }
            if (file.isFile()) {
                return List.of(file);
            }
            return List.of();
        }

        private void listAnswer() {
            var files = getFileList(new File(path));
            files.sort(Comparator.comparing(File::getName));
            outBuffer.putInt(files.size());
            var str = "";
            for (var file : files) {
                if (file.isDirectory()) {
                    str += "1 ";
                } else {
                    str += "0 ";
                }
                str += file.getName();
            }
            for (var ch : str.toCharArray()) {
                outBuffer.putChar(ch);
            }
            outBuffer.flip();
        }

        private int getAnswer() {
            var file = new File(path);
            var result = new byte[1];
            outBuffer.put(Longs.toByteArray(file.getTotalSpace()));
            try (var fileInputStream = new FileInputStream(file)) {
                while (fileInputStream.read(result) >= 1) {
                    outBuffer.put(result);
                }
            } catch (IOException e) {
                return -1;
            }
            outBuffer.flip();
            return 0;
        }

        private int compute() {
            parseInput();
            if (type == 1) {
                listAnswer();
                return 0;
            } else {
                return getAnswer();
            }
        }
    }

    private static final int PORT = 8000;
    private static final int BUFF_SIZE = 4096;
    private static final String HOST = "localhost";

    private Integer helloPort;
    private final Executor pool;


    private Thread helloThread;
    private Thread readingThread;
    private Thread writingThread;

    private final @NotNull Selector writeSelector;
    private final @NotNull Selector readSelector;

    private final Set<Socket> allSockets;

    public static void main(String... args) {
        try {
            var server = new Server();
            server.start(PORT);
            var client = new Client();
            int res = client.connect(HOST, PORT);
            System.out.println("connection client " + res);
            var result = client.executeList("src");
            System.out.println(result);
        } catch (Exception e) {
            System.out.println(e + " main");
        }

    }

    public Server() throws IOException {
        pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        allSockets = new HashSet<>();
        writeSelector = Selector.open();
        readSelector = Selector.open();
    }

    public void start(Integer port) throws IOException {
        helloPort = port;
        allSockets.clear();

        helloThread = new Thread(() -> {
            try (var serverSocketChannel = ServerSocketChannel.open();
                var socket = serverSocketChannel.socket()) {

                socket.bind(new InetSocketAddress(helloPort));
                serverSocketChannel.configureBlocking(false);

                while (true) {
                    var socketChannel = serverSocketChannel.accept();
                    if (socketChannel != null) {
                        socketChannel.configureBlocking(false);
                        socketChannel.register(readSelector, SelectionKey.OP_READ, new ClientStateHolder(socketChannel));
                    }

                }

            } catch (IOException e) {
                //kek
                System.out.println(e + "lel");
            }
        });
        helloThread.start();

        readingThread = new Thread(() -> {
            while(true) {
                try {
                    if (readSelector.select(100) == 0) {
                        continue;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                var keys = readSelector.selectedKeys();
                var iterator = keys.iterator();
                while (iterator.hasNext()) {
                    var key = iterator.next();
                    if (key.isReadable()) {
                        var current = (ClientStateHolder) key.attachment();
                        int result = current.read();
                        if (result == 1) {
                            key.cancel();
                            pool.execute(() -> {
                                current.compute();
                                try {
                                    current.getSocketChannel().register(writeSelector, SelectionKey.OP_WRITE, current);
                                } catch (ClosedChannelException e) {
                                    //kek
                                    System.out.println("In read " + e);
                                }
                            });
                        }
                    }
                    iterator.remove();
                }
            }

        });
        readingThread.start();

        writingThread = new Thread(() -> {
            while (true) {
                try {
                    if (writeSelector.select(100) == 0) continue;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                var iterator = writeSelector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    var key = iterator.next();
                    if (key.isWritable()) {
                        var current = (ClientStateHolder) key.attachment();
                        int result = current.write();
                        if (result == 1) {
                            key.cancel();
                            pool.execute(() -> {
                                try {
                                    current.getSocketChannel().register(readSelector, SelectionKey.OP_READ,
                                            new ClientStateHolder(current.getSocketChannel()));
                                } catch (ClosedChannelException e) {
                                    //kek
                                    System.out.println("In write " + e);
                                }
                            });
                        }
                    }
                }
            }
        });
        writingThread.start();
    }

    public void stop() throws IOException {
        helloThread.interrupt();
        synchronized (allSockets) {
            for (var socket: allSockets) {
                socket.close();
            }
        }
    }

}