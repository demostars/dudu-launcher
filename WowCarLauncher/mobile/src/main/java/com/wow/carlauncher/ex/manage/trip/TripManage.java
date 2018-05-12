package com.wow.carlauncher.ex.manage.trip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wow.carlauncher.common.CommonData;
import com.wow.carlauncher.ex.ContextEx;
import com.wow.carlauncher.ex.manage.location.event.MNewLocationEvent;
import com.wow.carlauncher.ex.plugin.obd.evnet.PObdEventCarInfo;
import com.wow.carlauncher.repertory.db.model.Trip;
import com.wow.carlauncher.view.activity.driving.DrivingActivity;
import com.wow.carlauncher.view.activity.launcher.LauncherActivity;
import com.wow.frame.util.SharedPreUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.x;

/**
 * 行程管理器
 * Created by 10124 on 2018/5/11.
 */

public class TripManage extends ContextEx {
    private final static String TAG = "WOW_CAR 行程管理器:";

    private static class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static TripManage instance = new TripManage();
    }

    public static TripManage self() {
        return TripManage.SingletonHolder.instance;
    }

    private boolean drivingShow = false;
    private Trip trip;

    public long getTropStartTime() {
        if (trip != null) {
            return trip.getStartTime();
        } else {
            return 0L;
        }
    }

    public boolean isDrivingShow() {
        return drivingShow;
    }

    public boolean isTripStart() {
        return trip != null;
    }

    public TripManage setDrivingShow(boolean drivingShow) {
        this.drivingShow = drivingShow;
        return this;
    }

    private TripManage() {
        super();
    }

    public void init(Context context) {
        setContext(context);
        EventBus.getDefault().register(this);
        if (SharedPreUtil.getSharedPreBoolean(CommonData.SDATA_TRIP_START_WITH_APP, true)) {
            startTrip();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PObdEventCarInfo event) {
        Log.d(TAG, "onEventMainThread: " + event);
        if (event.getRev() != null && event.getRev() > 400) {
            if (!drivingShow) {
                Intent intent2 = new Intent(getContext(), DrivingActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent2);
            }
            if (!SharedPreUtil.getSharedPreBoolean(CommonData.SDATA_TRIP_START_WITH_APP, true)) {
                startTrip();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MNewLocationEvent event) {

    }


    private void startTrip() {
        if (trip != null) {
            return;
        }
        trip = new Trip().setStartTime(System.currentTimeMillis());
        Log.d(TAG, "startTrip");
    }
}
