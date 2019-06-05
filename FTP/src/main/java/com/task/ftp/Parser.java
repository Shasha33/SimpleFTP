package com.task.ftp;

import java.io.*;
import java.math.*;
import java.util.*;

public abstract class Parser {

//    public static int getType(String c) {
//        var list = c.split(" ");
//        try {
//            return Integer.parseInt(list[0]);
//        } catch (Exception e) {
//            return -1;
//        }
//    }
//
//    public static int getType(byte[] array) {
//
//    }

    public static byte[] concat(byte[] a, byte[] b) {
        var result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

//    public static byte[] convertToByte(char[] array){
//    }
//
//    public static char[] convertToChar(byte[] array) {
//
//    }
//
//    public static Byte[] getAnswer(String path) {
//        var file = new File(path);
//        var result = new byte[1024];
//        if (file.isFile()) {
//            var size = file.getTotalSpace();
//            try (var reader = new FileInputStream(file)) {
//                var fileBuffer = new byte[1024];
//                while (reader.read(fileBuffer) != 0){
//                    for (var b : fileBuffer) {
//                        buffer.add(b);
//                    }
////                    buffer.add(Arrays.asList(fileBuffer));
//                }
//            } catch (Exception e) {
//                buffer.add((byte) -1);;
//            }
//        } else {
//            buffer.add((byte) -1);
//        }
//        var b = new Byte[buffer.size()];
//        return buffer.toArray(b);
//    }
//
//    public static String listAnswer(String path) {
//        var dir = new File(path);
//        var result = "";
//        if (!dir.isDirectory()) {
//            return "-1";
//        }
//        var files = dir.listFiles();
//        Arrays.sort(files);
//        result += files.length;
//        for (var file : files) {
//            if (file.isDirectory()) {
//                result += "1 ";
//            } else {
//                result += "0 ";
//            }
//            result += file.getName() + " " + file.isDirectory();
//        }
//        return result;
//    }


}
