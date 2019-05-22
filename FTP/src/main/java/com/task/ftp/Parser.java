package com.task.ftp;

import java.io.*;
import java.math.*;
import java.util.*;

public class Parser {

    public static int getType(String c) {
        var list = c.split(" ");
        try {
            return Integer.parseInt(list[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    public static byte[] getAnswer(String path) {
        var file = new File(path);
        var buffer = new ArrayList<Byte>();
        if (file.isFile()) {
            var size = BigInteger.valueOf(file.getTotalSpace());
            buffer.addAll(List.of(size.toByteArray()));
            try (var reader = new FileInputStream(file)) {
                var fileBuffer = new byte[1024];
                while (reader.read(fileBuffer) != 0){
                    buffer.add(Arrays.asList(fileBuffer));
                }
            } catch (Exception e) {
                buffer.add((byte) -1);;
            }
        } else {
            buffer.add((byte) -1);
        }
        return buffer.toArray(new byte[]);
    }

    public static String listAnswer(String path) {
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
        return result;
    }


}
