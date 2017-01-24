package com.everlastxgb.qianghongbao.job;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.everlastxgb.qianghongbao.BuildConfig;
import com.everlastxgb.qianghongbao.Config;
import com.everlastxgb.qianghongbao.IStatusBarNotification;
import com.everlastxgb.qianghongbao.QHBApplication;
import com.everlastxgb.qianghongbao.QiangHongBaoService;
import com.everlastxgb.qianghongbao.util.AccessibilityHelper;
import com.everlastxgb.qianghongbao.util.CommonUtils;
import com.everlastxgb.qianghongbao.util.NotifyHelper;

import java.util.List;

/**
 * 钉钉 抢红包
 *
 * @author Kido
 * @email everlastxgb@gmail.com
 * @create_time 2017/1/14 17:15
 */

public class DingtalkAccessbilityJob extends BaseAccessbilityJob {

    private static final String TAG = "DingtalkAccessbilityJob";

    /**
     * 微信的包名
     */
    public static final String PACKAGENAME = "com.alibaba.android.rimet";

    /**
     * 红包消息的关键字
     */
    private static final String HONGBAO_TEXT_KEY = "[红包]";
    private static final String HONGBAO_TEXT_KEY_1 = "[新春红包]";
    private static final String SEEHONGBAO_TEXT_KEY = "查看红包";
    private static final String SEEDETAIL_TEXT_KEY = "查看详情";

    private static final String CLASS_NAME_BUTTON = "android.widget.Button";
    private static final String CLASS_NAME_IMAGEBUTTON = "android.widget.ImageButton";
    private static final String CLASS_NAME_LISTVIEW = "android.widget.ListView";

    private static final String PAGE_HOME = "com.alibaba.android.rimet.biz.home.activity.HomeActivity"; // APP主页面
    private static final String PAGE_CHAT = "com.alibaba.android.dingtalkim.activities.ChatMsgActivity"; // 聊天页面
    private static final String PAGE_PICKREDPACKETS = "com.alibaba.android.dingtalk.redpackets.activities.PickRedPacketsActivity"; // 拆红包页面
    private static final String PAGE_PICKREDPACKETS_1 = "com.alibaba.android.dingtalk.redpackets.activities.FestivalRedPacketsPickActivity"; // 拆红包页面(新春红包)
    private static final String PAGE_REDPACKETSDETAIL = "com.alibaba.android.dingtalk.redpackets.activities.RedPacketsDetailActivity"; // 红包详情页


    /**
     * 不能再使用文字匹配的最小版本号
     */

    private static final int WINDOW_NONE = 0;
    private static final int WINDOW_LUCKYMONEY_RECEIVEUI = 1;
    private static final int WINDOW_LUCKYMONEY_DETAIL = 2;
    private static final int WINDOW_LAUNCHER = 3;
    private static final int WINDOW_CHAT = 4;
    private static final int WINDOW_FESTIVAL = 5;
    private static final int WINDOW_OTHER = -1;

    private int mCurrentWindow = WINDOW_NONE;

    private boolean isReceivingHongbao;
    private PackageInfo mWechatPackageInfo = null;
    private Handler mHandler = null;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //更新安装包信息
            updatePackageInfo();
        }
    };

    @Override
    public void onCreateJob(QiangHongBaoService service) {
        super.onCreateJob(service);

        updatePackageInfo();

        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");

        getContext().registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onStopJob() {
        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationPosted(IStatusBarNotification sbn) {
        Notification nf = sbn.getNotification();
        String text = String.valueOf(sbn.getNotification().tickerText);
        notificationEvent(text, nf);
    }

    @Override
    public boolean isEnable() {
        return getConfig().isEnableDingtalk();
    }

    @Override
    public String getTargetPackageName() {
        return PACKAGENAME;
    }

    @Override
    public void onReceiveJob(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        //通知栏事件
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Parcelable data = event.getParcelableData();
            if (data == null || !(data instanceof Notification)) {
                return;
            }
            if (QiangHongBaoService.isNotificationServiceRunning() && getConfig().isEnableNotificationService()) { //开启快速模式，不处理
                return;
            }
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                String text = String.valueOf(texts.get(0));
                notificationEvent(text, (Notification) data);
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            openHongBao(event);
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            if (mCurrentWindow != WINDOW_LAUNCHER && mCurrentWindow != WINDOW_CHAT &&  mCurrentWindow != WINDOW_FESTIVAL) { //不在聊天界面或聊天列表，不是新春红包，不处理
                return;
            }
            if (isReceivingHongbao) {
                handleChatListHongBao();
            }
        }
    }

//    /**
//     * 是否为群聊天
//     */
//    private boolean isMemberChatUi(AccessibilityNodeInfo nodeInfo) {
//        return false;
//    }

    /**
     * 通知栏事件
     */
    private void notificationEvent(String ticker, Notification nf) {
        String text = ticker;
        if (text.startsWith(HONGBAO_TEXT_KEY) || text.startsWith(HONGBAO_TEXT_KEY_1)) { //红包消息
            newHongBaoNotification(nf);
        }
    }

    /**
     * 打开通知栏消息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void newHongBaoNotification(Notification notification) {
        isReceivingHongbao = true;
        //以下是精华，将微信的通知栏消息打开
        PendingIntent pendingIntent = notification.contentIntent;
        boolean lock = NotifyHelper.isLockScreen(getContext());

        if (!lock) {
            NotifyHelper.send(pendingIntent);
        } else {
            NotifyHelper.showNotify(getContext(), String.valueOf(notification.tickerText), pendingIntent);
        }

        if (lock || getConfig().getWechatMode() != Config.WX_MODE_0) {
            NotifyHelper.playEffect(getContext(), getConfig());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        if (PAGE_PICKREDPACKETS.equals(event.getClassName()) || PAGE_PICKREDPACKETS_1.equals(event.getClassName())) {
            mCurrentWindow = WINDOW_LUCKYMONEY_RECEIVEUI;
            //点中了红包，下一步就是去拆红包
            handleLuckyMoneyReceive();
        } else if (PAGE_REDPACKETSDETAIL.equals(event.getClassName())) {
            mCurrentWindow = WINDOW_LUCKYMONEY_DETAIL;
            //拆完红包后看详细的纪录界面
            if (getConfig().getWechatAfterGetHongBaoEvent() == Config.WX_AFTER_GET_GOHOME) { //返回主界面，以便收到下一次的红包通知
                AccessibilityHelper.performHome(getService());
            }
        } else if (PAGE_HOME.equals(event.getClassName())) {
            mCurrentWindow = WINDOW_LAUNCHER;
            //在聊天列表,去点中红包
            handleChatListHongBao();
        } else if (PAGE_CHAT.equals(event.getClassName())) {
            mCurrentWindow = WINDOW_CHAT;
            handleChatListHongBao();
        } else {
            mCurrentWindow = WINDOW_OTHER;
        }
    }

    /**
     * 点击聊天里的红包后，显示的界面
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void handleLuckyMoneyReceive() {
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        AccessibilityNodeInfo targetNode = null;
        int event = getConfig().getWechatAfterOpenHongBaoEvent();
        if (event == Config.WX_AFTER_OPEN_HONGBAO) { //拆红包
            targetNode = AccessibilityHelper.findNodeInfosByClassName(nodeInfo, CLASS_NAME_IMAGEBUTTON); // 普通红包
            if (targetNode == null) {
                targetNode = AccessibilityHelper.findNodeInfosByClassNameRecursion(nodeInfo, CLASS_NAME_LISTVIEW); // 新春红包
                if (targetNode != null) {
                    mCurrentWindow = WINDOW_FESTIVAL;
                }
            }
        }

        if (targetNode != null) {
//            final AccessibilityNodeInfo node = targetNode;
//            performClickDelay(node); // 钉钉这里的imageButton不是真正的中间拆红包那个点，所以这里点击node会没反应。

            Rect rect = new Rect();
            targetNode.getBoundsInScreen(rect);// 由于拿到node不是中间的“拆红包”，这里获取其中心点方便下面模拟点击
            int screenX = rect.centerX();
            int screenY = rect.centerY();

            CommonUtils.execTap(screenX, screenY); // 这里通过命令模拟点击，需要root权限。
            QHBApplication.eventStatistics(getContext(), "open_hongbao_dingtalk", targetNode.toString());
        }
    }

    /**
     * 收到聊天里的红包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleChatListHongBao() {
        int mode = getConfig().getWechatMode();
        if (mode == Config.WX_MODE_3) { //只通知模式
            return;
        }

        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
//
//        if (mode != Config.WX_MODE_0) {
//            boolean isMember = isMemberChatUi(nodeInfo);
//            if (mode == Config.WX_MODE_1 && isMember) {//过滤群聊
//                return;
//            } else if (mode == Config.WX_MODE_2 && !isMember) { //过滤单聊
//                return;
//            }
//        }

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(SEEHONGBAO_TEXT_KEY);

        if (list != null && list.isEmpty()) {
            // 从消息列表查找红包
            final AccessibilityNodeInfo node = AccessibilityHelper.findNodeInfosByTexts(nodeInfo, HONGBAO_TEXT_KEY, HONGBAO_TEXT_KEY_1);
            if (node != null) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "-->钉钉红包:" + node);
                }
                isReceivingHongbao = true;
                performClickDelay(node);
            }
        } else if (list != null) {
            if (isReceivingHongbao) {
                //最新的红包领起
                AccessibilityNodeInfo node = list.get(list.size() - 1);
                performClickDelay(node);
                isReceivingHongbao = false;
            }
        }
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    /**
     * 获取钉钉的版本
     */
    private int getDingtalkVersion() {
        if (mWechatPackageInfo == null) {
            return 0;
        }
        return mWechatPackageInfo.versionCode;
    }

    /**
     * 更新包信息
     */
    private void updatePackageInfo() {
        try {
            mWechatPackageInfo = getContext().getPackageManager().getPackageInfo(PACKAGENAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void performClickDelay(final AccessibilityNodeInfo node) {
        long sDelayTime = getConfig().getWechatOpenDelayTime();
        if (sDelayTime != 0) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AccessibilityHelper.performClick(node);
                }
            }, sDelayTime);
        } else {
            AccessibilityHelper.performClick(node);
        }
    }
}
