package com.wow.carlauncher.view.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wow.carlauncher.CarLauncherApplication;
import com.wow.carlauncher.R;
import com.wow.carlauncher.common.AppContext;
import com.wow.carlauncher.common.CommonData;
import com.wow.carlauncher.common.LogEx;
import com.wow.carlauncher.common.TaskExecutor;
import com.wow.carlauncher.common.util.AppUtil;
import com.wow.carlauncher.common.util.CommonUtil;
import com.wow.carlauncher.common.util.DateUtil;
import com.wow.carlauncher.common.util.SharedPreUtil;
import com.wow.carlauncher.ex.plugin.amapcar.AMapCarPlugin;
import com.wow.carlauncher.ex.plugin.amapcar.event.PAmapEventNavInfo;
import com.wow.carlauncher.ex.plugin.amapcar.event.PAmapEventState;
import com.wow.carlauncher.ex.plugin.music.MusicPlugin;
import com.wow.carlauncher.ex.plugin.music.event.PMusicEventInfo;
import com.wow.carlauncher.ex.plugin.music.event.PMusicEventProgress;
import com.wow.carlauncher.ex.plugin.music.event.PMusicEventState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.wow.carlauncher.common.CommonData.PACKAGE_NAME;
import static com.wow.carlauncher.common.CommonData.SDATA_POPUP_ALLOW_SHOW;
import static com.wow.carlauncher.common.CommonData.SDATA_POPUP_CURRENT_PLUGIN;
import static com.wow.carlauncher.common.CommonData.TAG;
import static com.wow.carlauncher.ex.plugin.amapcar.AMapCarConstant.AMAP_PACKAGE;
import static com.wow.carlauncher.ex.plugin.amapcar.AMapCarConstant.ICONS;

public class PopupWin {
    private static class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static PopupWin instance = new PopupWin();
    }

    public static PopupWin self() {
        return PopupWin.SingletonHolder.instance;
    }

    private PopupWin() {

    }

    //窗口管理器
    private WindowManager wm = null;
    //窗口的布局参数
    private WindowManager.LayoutParams winparams;
    //是否展示了
    private Boolean isShow = false;
    private CarLauncherApplication context;
    //窗口视图
    private View popupWindow;
    //插件试图
    private LinearLayout pluginHome;
    private TextView tv_time;

    private int rank = 2;

    private int owidth = 0;
    private int oheight = 0;

    public void setRank(int rank) {
        winparams.width = owidth * rank;
        winparams.height = oheight * rank;
        this.rank = rank;
    }

    public void init(CarLauncherApplication context) {
        long t1 = System.currentTimeMillis();
        this.context = context;
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        EventBus.getDefault().register(this);

        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;


        oheight = (int) (screenWidth * 0.15 / 3);
        owidth = oheight / 2 * 3;

        winparams = new WindowManager.LayoutParams();
        // 类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//6.0
            winparams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            winparams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        if (SharedPreUtil.getBoolean(CommonData.SDATA_POPUP_FULL_SCREEN, true)) {
            winparams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            winparams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        winparams.format = PixelFormat.TRANSLUCENT;

        rank = SharedPreUtil.getInteger(CommonData.SDATA_POPUP_SIZE, 1) + 1;
        winparams.width = owidth * rank;
        winparams.height = oheight * rank;

        winparams.gravity = Gravity.TOP | Gravity.START;
        winparams.x = SharedPreUtil.getInteger(CommonData.SDATA_POPUP_WIN_X, 0);
        winparams.y = SharedPreUtil.getInteger(CommonData.SDATA_POPUP_WIN_Y, 0);

        popupWindow = View.inflate(context, R.layout.popup_quick, null);
        pluginHome = (LinearLayout) popupWindow.findViewById(R.id.ll_plugin);
        tv_time = (TextView) popupWindow.findViewById(R.id.tv_time);

        popupWindow.findViewById(R.id.ll_yidong).setOnTouchListener(moveTouchListener);
        popupWindow.findViewById(R.id.ll_xunhuan).setOnClickListener(onClickListener);

        LogEx.d(this, "init time:" + (System.currentTimeMillis() - t1));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PEventFSRefresh event) {
        LogEx.d(this, "onEventMainThread: " + event);
        if (SharedPreUtil.getBoolean(CommonData.SDATA_POPUP_FULL_SCREEN, true)) {
            winparams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            winparams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        if (isShow) {
            wm.updateViewLayout(popupWindow, winparams);
        }
    }

    private String nowApp = "";

    private void checkShowApp(final String app) {
        //如果APP是空的,则说明用户没有打开权限,则直接不显示了
        if (!SharedPreUtil.getBoolean(CommonData.SDATA_POPUP_SHOW_TYPE, true)) {
            if (CommonUtil.isNull(app)) {
                TaskExecutor.self().autoPost(() -> popupWindow.setVisibility(View.GONE));
            }
        }

        //如果不显示了,或者传进来的app参数是空的
        if (!isShow) {
            return;
        }
        //如果APP没切换,也不用处理了
        if (nowApp != null && nowApp.equals(app)) {
            return;
        }
        this.nowApp = app;
        if (this.nowApp == null) {
            this.nowApp = PACKAGE_NAME;
        }
        TaskExecutor.self().autoPost(new Runnable() {
            @Override
            public void run() {
                if (!SharedPreUtil.getBoolean(CommonData.SDATA_POPUP_SHOW_TYPE, true)) {
                    final String selectapp = SharedPreUtil.getString(CommonData.SDATA_POPUP_SHOW_APPS);
                    if (selectapp.contains("[" + app + "]")) {
                        popupWindow.setVisibility(View.VISIBLE);
                    } else {
                        popupWindow.setVisibility(View.GONE);
                    }
                } else {
                    popupWindow.setVisibility(View.VISIBLE);
                }
                if (popupWindow.getVisibility() == View.GONE) {
                    return;
                }
                showPlugin(false);
            }
        });
    }

    private void showPlugin(boolean goNext) {
        int pluginId = SharedPreUtil.getInteger(SDATA_POPUP_CURRENT_PLUGIN, 1);
        if (goNext) {
            if (pluginId >= 3) {
                pluginId = 1;
            } else {
                pluginId = pluginId + 1;
            }
        }
        SharedPreUtil.saveInteger(SDATA_POPUP_CURRENT_PLUGIN, pluginId);
        if (pluginId == 1) {
            pluginHome.setVisibility(View.GONE);
            winparams.width = owidth * rank;
            winparams.height = oheight * rank;
            try {
                wm.updateViewLayout(popupWindow, winparams);
            } catch (Throwable ignored) {
            }
        } else if (pluginId == 2) {
            pluginHome.setVisibility(View.VISIBLE);
            pluginHome.removeAllViews();
            pluginHome.addView(musicView(), LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            winparams.width = owidth * 3 * rank;
            winparams.height = oheight * rank;
            wm.updateViewLayout(popupWindow, winparams);

        } else if (pluginId == 3) {
            pluginHome.setVisibility(View.VISIBLE);
            pluginHome.removeAllViews();
            pluginHome.addView(amapView(), LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            winparams.width = owidth * 3 * rank;
            winparams.height = oheight * rank;
            wm.updateViewLayout(popupWindow, winparams);

        }
    }

    private FrameLayout amapView;

    private View amapView() {
        if (amapView == null) {
            amapView = (FrameLayout) View.inflate(context, R.layout.popup_quick_amap, null);
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.btn_search:
                            Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(AMAP_PACKAGE);
                            if (appIntent == null) {
                                Toast.makeText(context, "没有安装高德地图", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(appIntent);
                            break;
                        case R.id.btn_go_home:
                            AMapCarPlugin.self().naviToHome();
                            break;
                        case R.id.btn_go_company:
                            AMapCarPlugin.self().naviToComp();
                            break;
                    }
                }
            };
            amapView.findViewById(R.id.btn_search).setOnClickListener(clickListener);
            amapView.findViewById(R.id.btn_go_home).setOnClickListener(clickListener);
            amapView.findViewById(R.id.btn_go_company).setOnClickListener(clickListener);
        }
        return amapView;
    }

    private LinearLayout musicView;

    private View musicView() {
        if (musicView == null) {
            musicView = (LinearLayout) View.inflate(context, R.layout.popup_quick_music, null);
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.ll_play:
                            MusicPlugin.self().playOrPause();
                            break;
                        case R.id.ll_next:
                            MusicPlugin.self().next();
                            break;
                        case R.id.ll_prew:
                            MusicPlugin.self().pre();
                            break;
                    }
                }
            };
            musicView.findViewById(R.id.ll_prew).setOnClickListener(clickListener);
            musicView.findViewById(R.id.ll_next).setOnClickListener(clickListener);
            musicView.findViewById(R.id.ll_play).setOnClickListener(clickListener);
        }
        return musicView;
    }

    public synchronized void checkShow(int count) {
        if (AppContext.self().getApplication().checkActivity(count) > 0) {
            hide();
        } else {
            Log.d(TAG, "checkShow: 请求打开悬浮窗");
            show();
        }
    }

    //隐藏方法
    private void hide() {
        if (isShow) {
            wm.removeView(popupWindow);
            isShow = false;

            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }

    private Timer timer;
    private static final byte[] lock = new byte[0];

    //显示方法
    private void show() {
        if (!SharedPreUtil.getBoolean(SDATA_POPUP_ALLOW_SHOW, true)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(AppContext.self().getApplication())) {
            return;
        }
        synchronized (lock) {
            if (popupWindow == null) {
                init(AppContext.self().getApplication());
            }
        }
        if (!isShow) {
            wm.addView(popupWindow, winparams);
            isShow = true;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkShowApp(AppUtil.getForegroundApp(context));
                setTime();
            }
        }, 500 - System.currentTimeMillis() % 500, 500);
    }

    private View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {
            case R.id.ll_xunhuan: {
                showPlugin(true);
                break;
            }
        }
    };

    private View.OnTouchListener moveTouchListener = new View.OnTouchListener() {
        private int tx, ty;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                tx = (int) e.getX();
                ty = (int) e.getY();
                return true;
            } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                winparams.x = (int) (e.getRawX() - tx);
                winparams.y = (int) (e.getRawY() - ty);
                wm.updateViewLayout(popupWindow, winparams);
                return true;
            } else if (e.getAction() == MotionEvent.ACTION_UP) {
                SharedPreUtil.saveInteger(CommonData.SDATA_POPUP_WIN_X, winparams.x);
                SharedPreUtil.saveInteger(CommonData.SDATA_POPUP_WIN_Y, winparams.y);
                return true;
            }
            return false;
        }
    };

    private void setTime() {
        TaskExecutor.self().autoPost(() -> {
            Date d = new Date();
            String datetime = DateUtil.dateToString(d, "MM月dd日 HH:mm");
            tv_time.setText(datetime);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final PMusicEventInfo event) {
        if (CommonUtil.isNotNull(event.getTitle())) {
            ((TextView) musicView().findViewById(R.id.tv_title)).setText(event.getTitle());
        } else {
            ((TextView) musicView().findViewById(R.id.tv_title)).setText("标题");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final PMusicEventProgress event) {
        if (event.getTotalTime() > 0) {
            ProgressBar progressBar = ((ProgressBar) musicView().findViewById(R.id.pb_music));
            progressBar.setProgress(event.getCurrTime());
            progressBar.setMax(event.getTotalTime());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final PMusicEventState event) {
        if (!event.isRun()) {
            ((ImageView) musicView().findViewById(R.id.iv_play)).setImageResource(R.mipmap.ic_play2);
        } else {
            ((ImageView) musicView().findViewById(R.id.iv_play)).setImageResource(R.mipmap.ic_pause2);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final PAmapEventState event) {
        if (amapView != null) {
            LinearLayout popupcontroller = (LinearLayout) amapView.findViewById(R.id.ll_controller);
            RelativeLayout popupnavi = (RelativeLayout) amapView.findViewById(R.id.ll_navi);
            if (popupcontroller != null && popupnavi != null) {
                if (event.isRunning()) {
                    popupcontroller.setVisibility(View.GONE);
                    popupnavi.setVisibility(View.VISIBLE);
                } else {
                    popupcontroller.setVisibility(View.VISIBLE);
                    popupnavi.setVisibility(View.GONE);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final PAmapEventNavInfo event) {
        if (amapView != null) {
            ImageView popupIcon = (ImageView) amapView.findViewById(R.id.iv_icon);
            TextView popupdis = (TextView) amapView.findViewById(R.id.tv_dis);
            TextView popupmsg = (TextView) amapView.findViewById(R.id.tv_msg);
            if (popupIcon != null && event.getIcon() - 1 >= 0 && event.getIcon() - 1 < ICONS.length) {
                popupIcon.setImageResource(ICONS[event.getIcon() - 1]);
            }
            if (popupdis != null && event.getSegRemainDis() > -1) {
                String msg = "";
                if (event.getSegRemainDis() < 10) {
                    msg = "现在";
                } else {
                    if (event.getSegRemainDis() > 1000) {
                        msg = event.getSegRemainDis() / 1000 + "公里后";
                    } else {
                        msg = event.getSegRemainDis() + "米后";
                    }
                }
                msg = msg + event.getNextRoadName();
                popupdis.setText(msg);
            }

            if (popupmsg != null && event.getRouteRemainTime() > -1 && event.getRouteRemainDis() > -1) {
                if (event.getRouteRemainTime() == 0 || event.getRouteRemainDis() == 0) {
                    popupmsg.setText("到达");
                } else {
                    String msg = "剩余" + new BigDecimal(event.getRouteRemainDis() / 1000f).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() + "公里  " +
                            event.getRouteRemainTime() / 60 + "分钟";
                    popupmsg.setText(msg);
                }
            }
        }
    }


    public static class PEventFSRefresh {
    }

}
