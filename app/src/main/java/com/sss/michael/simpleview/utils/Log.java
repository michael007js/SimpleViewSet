package com.sss.michael.simpleview.utils;

public class Log {
    public static void log(Object...objects){
        log("SSSSS",objects);
    }
    public static void log(String tag,Object...objects){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < objects.length; i++) {
            stringBuilder.append(stringBuilder.length() == 0?"":" \n").append(objects[i]);
        }
//        stringBuilder.append(" \n").append("-------------------------------------------------------------------------------------------------------------");
        android.util.Log.e(tag,stringBuilder.toString());
    }
}
