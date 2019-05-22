package com.task.ftp;

import java.io.*;
import java.util.*;

public class Server {

    private ArrayList<Byte> getAnswer(String path) {
        var file = new File(path);
        var buffer = new ArrayList<Byte>();
        return buffer;
    }

    private String listAnswer(String path) {
        var dir = new File(path);
        var result = "";
        if (!dir.isDirectory()) {
            return "-1";
        }
        var files = dir.listFiles();
        Arrays.sort(files);
        result += files.length;
        for (var file : files) {
            if (file.isDirectory()) {
                result += "1 ";
            } else {
                result += "0 ";
            }
            result += file.getName() + " " + file.isDirectory();
        }
    }
}
