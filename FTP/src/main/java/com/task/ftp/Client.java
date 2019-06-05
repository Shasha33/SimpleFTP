package com.task.ftp;

import javax.print.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

import static java.lang.Thread.sleep;


public class Client {

    private SocketChannel socketChannel;

    private ByteBuffer inBuffer;
    private ByteBuffer outBuffer;

    private final int BUFF_SIZE = 4096;

    int connect(String hostName, int portNumber) throws InterruptedException {
        try {
            inBuffer = ByteBuffer.allocate(BUFF_SIZE);
            outBuffer = ByteBuffer.allocate(BUFF_SIZE);
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(hostName, portNumber));
            while (!socketChannel.finishConnect()) {
                sleep(1);
            }
            System.out.println("OK");
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    int disconnect() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    /**
     * Returns null if didnt get result or get in wrong format
     *
     */
    String[] executeList(String path) throws IOException {
        var result = "";
        try {
            outBuffer.putInt(1);
            outBuffer.putInt(path.length());
            for (var c : path.toCharArray()) {
                outBuffer.putChar(c);
            }

            while (outBuffer.remaining() != 0) {
                outBuffer.put((byte) 0);
            }

            outBuffer.flip();

            while (outBuffer.hasRemaining()) {
                socketChannel.write(outBuffer);
            }

            int red = 0;
            while (red < 4) {
                red += socketChannel.read(inBuffer);
            }
            inBuffer.flip();
            int size = inBuffer.getInt();
            inBuffer.flip();
//            inBuffer.clear();

            int red1 = 0;

            while (red1 < size && red1 != 0) {
                System.out.println("kek " + red1 + " " + size);
                red1 += socketChannel.read(inBuffer);
            }
            inBuffer.flip();



            if (size < 1) {
                return null;
            }

            while (inBuffer.hasRemaining()) {
                System.out.println("ELELELEL");
                result += inBuffer.getChar();
            }
            System.out.println("got " + result);

        } catch (Exception e) {
            System.out.println(e + " execc");
        }

        String[] res = result.split(" ");

        if (res.length == 0) {
            return null;
        }

        try {
            if (Integer.parseInt(res[0]) == -1) {
                return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return res;
    }

    /**
     * Returns requested file content as byte array
     * If incorrect package received, returns null
     */
    byte[] executeGet(String path) {
        try {
            outBuffer.putInt(2);
            outBuffer.putInt(path.length());
            for (var c : path.toCharArray()) {
                outBuffer.putChar(c);
            }


            while (outBuffer.remaining() != 0) {
                outBuffer.put((byte) 0);
            }

            outBuffer.flip();

            while (outBuffer.remaining() != 0) {
                socketChannel.write(outBuffer);
            }

            int red = 0;
            while (red < 4) {
                red += socketChannel.read(inBuffer);
            }
            inBuffer.flip();
            int size = inBuffer.getInt();
            inBuffer.clear();

            int red1 = 0;

            while (red1 < size) {
                red1 += socketChannel.read(inBuffer);
            }
            inBuffer.flip();

            if (size < 1) {
                return null;
            }

            var buffer = new byte[size];
            inBuffer.get(buffer);
            return buffer;
        } catch (IOException e) {
            return null;
        }

    }
}
