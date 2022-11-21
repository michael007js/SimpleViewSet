package com.sss.michael.simpleview.utils;

/**
 * @author Michael by Administrator
 * @date 2021/7/2 13:31
 * @Description 判空工具类
 */
public final class EmptyUtils {

    /**
     * 判断字符串是否为null或长度为0
     *
     * @param s 待校验字符串
     * @return {@code true}: 空<br> {@code false}: 不为空
     */
    public static boolean isEmpty(CharSequence s) {
        if (s == null) {
            return true;
        } else {
            if (s.length() == 0) {
                return true;
            } else {
                if ("".equals(s) || "null".equals(s) || "NULL".equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 判断字符串是否不为-
     *
     * @param str
     * @return
     */
    public static boolean isStrNull(String str) {
        if ("-".equals(str) || EmptyUtils.isEmpty(str)) {
            return false;
        }
        return true;
    }
}