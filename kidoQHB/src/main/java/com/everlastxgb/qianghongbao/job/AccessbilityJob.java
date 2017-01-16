package com.everlastxgb.qianghongbao.job;

import android.view.accessibility.AccessibilityEvent;

import com.everlastxgb.qianghongbao.IStatusBarNotification;
import com.everlastxgb.qianghongbao.QiangHongBaoService;

/**
 * @author Kido
 * @email everlastxgb@gmail.com
 * @create_time 2017/1/14 17:15
 */

public interface AccessbilityJob {
    String getTargetPackageName();
    void onCreateJob(QiangHongBaoService service);
    void onReceiveJob(AccessibilityEvent event);
    void onStopJob();
    void onNotificationPosted(IStatusBarNotification service);
    boolean isEnable();
}
