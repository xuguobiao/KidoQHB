package com.everlastxgb.qianghongbao;

import android.app.Notification;

/**
 * @author Kido
 * @email everlastxgb@gmail.com
 * @create_time 2017/1/14 17:15
 */

public interface IStatusBarNotification {

    String getPackageName();
    Notification getNotification();
}
