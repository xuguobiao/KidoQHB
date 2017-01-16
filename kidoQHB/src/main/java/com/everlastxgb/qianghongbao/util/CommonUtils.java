package com.everlastxgb.qianghongbao.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * @author Kido
 * @email everlastxgb@gmail.com
 * @create_time 2017/1/14 17:15
 */

public class CommonUtils {

    private final static String FORMAT_CMD_TAP = "input tap %1$s %2$s";

    /**
     * 执行shell命令
     *
     * @param cmd
     */
    public static void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 执行点击
     *
     * @param posX 相对屏幕的x轴
     * @param posY 相对屏幕的y轴
     */
    public static void execTap(int posX, int posY) {
        String cmd = String.format(FORMAT_CMD_TAP, posX, posY);
        execShellCmd(cmd);
    }

    public static void openWeb(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
