package com.sss.michael.simpleview.utils;

public class Log {
    public static void log(Object...objects){
        StringBuilder stringBuilder = new StringBuilder();
        for (Object object : objects) {
            stringBuilder.append(stringBuilder.length() == 0 ? "" : " \n").append(object);
        }
//        stringBuilder.append(" \n").append("-------------------------------------------------------------------------------------------------------------");
        android.util.Log.e("SSSSS",stringBuilder.toString());
    }
}
