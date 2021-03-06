package com.wow.carlauncher.ex.plugin.fk;

import android.annotation.SuppressLint;
import android.content.Context;

import com.wow.carlauncher.common.CommonData;
import com.wow.carlauncher.common.LogEx;
import com.wow.carlauncher.common.TaskExecutor;
import com.wow.carlauncher.common.util.CommonUtil;
import com.wow.carlauncher.common.util.SharedPreUtil;
import com.wow.carlauncher.ex.ContextEx;
import com.wow.carlauncher.ex.manage.ble.BleListener;
import com.wow.carlauncher.ex.manage.ble.BleManage;
import com.wow.carlauncher.ex.manage.ble.MyBleConnectStatusListener;
import com.wow.carlauncher.ex.manage.time.event.TMEvent3Second;
import com.wow.carlauncher.ex.manage.toast.ToastManage;
import com.wow.carlauncher.ex.plugin.fk.event.PFkEventAction;
import com.wow.carlauncher.ex.plugin.fk.event.PFkEventBatterLevel;
import com.wow.carlauncher.ex.plugin.fk.event.PFkEventConnect;
import com.wow.carlauncher.ex.plugin.fk.protocol.YiLianProtocol;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTING;
import static com.wow.carlauncher.common.CommonData.SDATA_FANGKONG_CONTROLLER;

/**
 * Created by 10124 on 2017/11/4.
 */

public class FangkongPlugin extends ContextEx {
    private static final String BLE_MARK = "BLE_FANGKONG";


    private static class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static FangkongPlugin instance = new FangkongPlugin();
    }

    public static FangkongPlugin self() {
        return FangkongPlugin.SingletonHolder.instance;
    }

    private FangkongPlugin() {

    }

    public void init(Context context) {
        long t1 = System.currentTimeMillis();
        setContext(context);
        BleManage.self().addListener(bleListener);
        myBleConnectStatusListener = new MyBleConnectStatusListener(BLE_MARK);
        EventBus.getDefault().register(this);
        LogEx.d(this, "init time:" + (System.currentTimeMillis() - t1));
    }

    private MyBleConnectStatusListener myBleConnectStatusListener;
    private final BleListener bleListener = new BleListener() {
        @Override
        public String getMark() {
            return BLE_MARK;
        }

        @Override
        public void connect(boolean success) {
            postEvent(new PFkEventConnect().setConnected(success));
            if (fangkongProtocol != null) {
                if (success) {
                    ToastManage.self().show("方控连接成功!");
                } else {
                    disconnect();
                }
            }
            connecting = false;
        }

        @Override
        public void receiveMessage(byte[] msg) {
            if (fangkongProtocol != null) {
                fangkongProtocol.receiveMessage(msg);
            }
        }
    };

    public boolean isConnect() {
        String fkaddress = SharedPreUtil.getString(CommonData.SDATA_OBD_ADDRESS);
        return CommonUtil.isNotNull(fkaddress) && BleManage.self().getConnectStatus(fkaddress) == STATUS_DEVICE_CONNECTED;
    }

    private FangkongProtocol fangkongProtocol;

    private FangkongProtocolListener changeModelCallBack = new FangkongProtocolListener() {
        @Override
        public void batteryLevel(Integer level, Integer total) {
            postEvent(new PFkEventBatterLevel().setLevel(level).setTotal(total));
        }

        @Override
        public void onAction(final int action) {
            TaskExecutor.self().run(() -> postEvent(new PFkEventAction()
                    .setAction(action)
                    .setFangkongProtocol(FangkongProtocolEnum.getById(SharedPreUtil.getInteger(SDATA_FANGKONG_CONTROLLER, FangkongProtocolEnum.YLFK.getId())))
            ));
        }
    };

    private boolean connecting = false;

    private void connect() {
        final String fkaddress = SharedPreUtil.getString(CommonData.SDATA_FANGKONG_ADDRESS);
        if (connecting ||
                CommonUtil.isNull(fkaddress) ||
                BleManage.self().getConnectStatus(fkaddress) == STATUS_DEVICE_CONNECTED ||
                BleManage.self().getConnectStatus(fkaddress) == STATUS_DEVICE_CONNECTING) {
            return;
        }
        LogEx.d(this, "connecting");
        connecting = true;

        disconnect();

        FangkongProtocolEnum p1 = FangkongProtocolEnum.getById(SharedPreUtil.getInteger(SDATA_FANGKONG_CONTROLLER, FangkongProtocolEnum.YLFK.getId()));
        switch (p1) {
            case YLFK: {
                fangkongProtocol = new YiLianProtocol(fkaddress, getContext(), changeModelCallBack);
                break;
            }
            default:
                fangkongProtocol = new YiLianProtocol(fkaddress, getContext(), changeModelCallBack);
                break;
        }
        BleManage.self().connect(BLE_MARK, fangkongProtocol.getAddress(), fangkongProtocol.getService(), fangkongProtocol.getCharacter(), myBleConnectStatusListener);
        LogEx.d(this, "connect over:" + fangkongProtocol);
    }

    public void disconnect() {
        BleManage.self().disconnect(BLE_MARK);
        if (fangkongProtocol != null) {
            fangkongProtocol.destroy();
            fangkongProtocol = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(final TMEvent3Second event) {
        String fkaddress = SharedPreUtil.getString(CommonData.SDATA_FANGKONG_ADDRESS);
        if (CommonUtil.isNotNull(fkaddress)) {
            connect();
        }
    }
}
