package com.task.ftp;

import java.io.*;
import java.net.*;
import java.nio.charset.*;


public class Client {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    int connect(String hostName, int portNumber){
        try {
            socket = new Socket(hostName, portNumber);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            socket.setTcpNoDelay(true);
            out.writeChars("Hello");
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    int disconnect() {
        try {
            out.writeChars("Bye");

            out.close();
            in.close();
            socket.close();
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
        String result = "";
        try (var inputReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            socket.setTcpNoDelay(true);
            var request = "1 " + path;
            out.writeChars(request);
            String input;
            while ((input = inputReader.readLine()) != null) {
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
        try {
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
