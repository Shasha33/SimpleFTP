package com.task.ftp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {

    private Integer helloPort;
    private Executor pool;
    private Thread helloThread;
    private Set<Socket> allSockets;

    public Server() {
        pool = Executors.newFixedThreadPool(100);
        allSockets = new HashSet<>();
    }

    public void start(Integer port) throws IOException {
        helloPort = port;
        allSockets.clear();

        helloThread = new Thread(new Runnable() {
            public void run() {
                try {
                    var serverSocket = new ServerSocket(helloPort);

                    while (true) {
                        var socket = serverSocket.accept();
                        synchronized (allSockets) {
                            allSockets.add(socket);
                        }

                        pool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    var in = new BufferedReader(new InputStreamReader(
                                            new DataInputStream(socket.getInputStream()), StandardCharsets.UTF_8));
                                    var out = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(socket.getOutputStream())));

                                    while (socket.isConnected()) {
                                        var query = in.readLine();
                                        /*your code here*/

                                    }

                                    synchronized (allSockets) {
                                        allSockets.remove(socket);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        if (Thread.interrupted()) {
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
