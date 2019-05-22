package com.task.ftp;

import java.io.*;
import java.net.*;

public class Client {

    int connect(String hostName, int portNumber) throws IOException {
        try (var socket = new Socket(hostName, portNumber);
             var in = new DataInputStream(socket.getInputStream());
             var out = new DataOutputStream(socket.getOutputStream())) {

        }
    }

    int disconnect() {

    }

    int
}
