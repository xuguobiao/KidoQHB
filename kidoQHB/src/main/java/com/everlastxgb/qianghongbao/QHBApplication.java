package com.everlastxgb.qianghongbao;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.pgyersdk.crash.PgyCrashManager;
import com.tendcloud.tenddata.TCAgent;
import com.umeng.analytics.MobclickAgent;

/**
 * @author Kido
 * @email everlastxgb@gmail.com
 * @create_time 2017/1/14 17:15
 */

public class QHBApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void showShare(final Activity activity) {

    }

    /** 显示分享*/
    public static void showShare(final Activity activity, final String shareUrl) {
    }

    /** 检查更新*/
    public static void checkUpdate(Activity activity) {

    }

    /** 首个activity启动调用*/
    public static void activityStartMain(Activity activity) {

    }

    /** 每个activity生命周期里的onCreate*/
    public static void activityCreateStatistics(Activity activity) {

    }

    /** 每个activity生命周期里的onResume*/
    public static void activityResumeStatistics(Activity activity) {
        MobclickAgent.onResume(activity);
        TCAgent.onResume(activity);

    }

    /** 每个activity生命周期里的onPause*/
    public static void activityPauseStatistics(Activity activity) {
        MobclickAgent.onPause(activity);
        TCAgent.onPause(activity);
    }

    /** 事件统计*/
    public static void eventStatistics(Context context, String event) {
        MobclickAgent.onEvent(context, event);
        TCAgent.onEvent(context, event);
    }

    /** 事件统计*/
    public static void eventStatistics(Context context, String event, String tag) {
        MobclickAgent.onEvent(context, event, tag);
        TCAgent.onEvent(context, event, tag);
    }
}
