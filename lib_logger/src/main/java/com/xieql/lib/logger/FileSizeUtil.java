package com.xieql.lib.logger;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;

public class FileSizeUtil {

    private static final String TAG="FileSizeUtil";

    private static final double BIT = 1024.0;

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小
     */
    public static double getFileOrFilesSize(String filePath, int sizeType) {

        double size = getFileLength(new File(filePath));
        Log.i(TAG,"日志文件大小："+size);
        switch (sizeType){
            case 1:
                 break;
            case 2:
                size = size/BIT;
                break;
            case 3:
                size =  size/BIT/BIT;
                break;
            case 4:
                size =  size/BIT/BIT/BIT;
                break;
            default:
                break;
        }
        return size;

    }


    // 递归遍历每个文件的大小
    public static long getFileLength(File file) {
        if (file == null)
            return -1;
        long size = 0;
        if (!file.isDirectory()) {
            size = file.length();
        } else {
            for (File f : file.listFiles()) {
                size += getFileLength(f);
            }
        }
        return size;
    }


}
