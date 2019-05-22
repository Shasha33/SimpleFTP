package com.task.ftp;

import java.io.*;
import java.net.*;
import java.nio.charset.*;


public class Client {

    private int port;
    private String hostName;

    void connect(String hostName, int portNumber) throws IOException {
        this.hostName = hostName;
        try (var socket = new Socket(hostName, portNumber);
             var in = new DataInputStream(socket.getInputStream());
             var out = new DataOutputStream(socket.getOutputStream())) {
            socket.setTcpNoDelay(true);
            out.writeChars("Hello");
            port = in.readInt();

        }
    }

    void disconnect() throws IOException {
        try (var socket = new Socket(hostName, port);
             var in = new DataInputStream(socket.getInputStream());
             var out = new DataOutputStream(socket.getOutputStream())) {
            socket.setTcpNoDelay(true);
            out.writeChars("Bye");
        }
    }

    /**
     * Returns null if didnt get result or get in wrong format
     *
     */
    String[] executeList(String path) throws IOException {
        String result = "";
        try (var socket = new Socket(hostName, port);
             var in = new BufferedReader(new InputStreamReader(
                     new DataInputStream(socket.getInputStream()), StandardCharsets.UTF_8));
             var out = new DataOutputStream(socket.getOutputStream())) {
            socket.setTcpNoDelay(true);
            var request = "1 " + path;
            out.writeChars(request);
            String input;
            while ((input = in.readLine()) != null) {
                result += input;
            }
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
     *
     */
    byte[] executeGet(String path) {
        try (var socket = new Socket(hostName, port);
             var in = new DataInputStream(socket.getInputStream());
             var out = new DataOutputStream(socket.getOutputStream())) {
            socket.setTcpNoDelay(true);
            var request = "2 " + path;
            out.writeChars(request);
            int size = in.readInt();
            if (size == -1) {
                return null;
            }
            var buffer = new byte[size];
            in.read(buffer);
            if (in.read() != 0) {
                return null;
            }
            return buffer;
        } catch (IOException e) {
            return null;
        }

    }
}
