package com.wow.carlauncher.ex.plugin.fk;

import android.content.Context;
import android.util.Log;

import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.wow.carlauncher.common.CommonData;
import com.wow.carlauncher.ex.ContextEx;
import com.wow.carlauncher.ex.manage.ble.BleManage;
import com.wow.carlauncher.ex.manage.time.event.MTimeSecondEvent;
import com.wow.carlauncher.ex.manage.toast.ToastManage;
import com.wow.carlauncher.ex.plugin.fk.event.PFkEventConnect;
import com.wow.carlauncher.ex.plugin.fk.event.PFkEventModel;
import com.wow.carlauncher.ex.plugin.fk.protocol.YiLianProtocol;
import com.wow.frame.util.CommonUtil;
import com.wow.frame.util.SharedPreUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED;
import static com.wow.carlauncher.common.CommonData.SDATA_FANGKONG_CONTROLLER;
import static com.wow.carlauncher.common.CommonData.TAG;

/**
 * Created by 10124 on 2017/11/4.
 */

public class FangkongPlugin extends ContextEx {

    private static FangkongPlugin self;

    public static FangkongPlugin self() {
        if (self == null) {
            self = new FangkongPlugin();
        }
        return self;
    }

    private BleConnectOptions options;

    private FangkongPlugin() {

    }

    public void init(Context context) {
        setContext(context);

        options = new BleConnectOptions.Builder()
                .setConnectRetry(Integer.MAX_VALUE)
                .setConnectTimeout(5000)   // 连接超时5s
                .setServiceDiscoverRetry(Integer.MAX_VALUE)
                .setServiceDiscoverTimeout(5000)  // 发现服务超时5s
                .build();

        connect();

        EventBus.getDefault().register(this);
    }

    private FangkongProtocol fangkongProtocol;

    private FangkongProtocolListener changeModelCallBack = new FangkongProtocolListener() {
        @Override
        public void changeModel(final String name) {
            ToastManage.self().show("方控切换至:" + name);
            EventBus.getDefault().post(new PFkEventModel().setModelName(name));
        }

        @Override
        public void batteryLevel(Integer level, Integer total) {

        }
    };

    private boolean connecting = false;

    private synchronized void connect() {
        final String fkaddress = SharedPreUtil.getSharedPreString(CommonData.SDATA_FANGKONG_ADDRESS);
        Log.d(TAG, "connect: " + Constants.getStatusText(BleManage.self().client().getConnectStatus(fkaddress)) + "  " + CommonUtil.isNull(fkaddress) + "  " + Constants.getStatusText(BleManage.self().client().getConnectStatus(fkaddress)));
        if (connecting || CommonUtil.isNull(fkaddress) || BleManage.self().client().getConnectStatus(fkaddress) == STATUS_DEVICE_CONNECTED) {
            return;
        }
        connecting = true;
        FangkongProtocolEnum p1 = FangkongProtocolEnum.getById(SharedPreUtil.getSharedPreInteger(SDATA_FANGKONG_CONTROLLER, FangkongProtocolEnum.YLFK.getId()));
        switch (p1) {
            case YLFK: {
                fangkongProtocol = new YiLianProtocol(fkaddress, getContext(), changeModelCallBack);
                break;
            }
            default:
                fangkongProtocol = new YiLianProtocol(fkaddress, getContext(), changeModelCallBack);
                break;
        }
        Log.d(TAG, "开始连接");
        BleManage.self().client().clearRequest(fangkongProtocol.getAddress(), 0);
        BleManage.self().client().refreshCache(fangkongProtocol.getAddress());
        BleManage.self().client().connect(fangkongProtocol.getAddress(), options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                if (code == REQUEST_SUCCESS) {
                    BleManage.self().client().notify(fangkongProtocol.getAddress(),
                            fangkongProtocol.getService(),
                            fangkongProtocol.getCharacter(),
                            new BleNotifyResponse() {
                                @Override
                                public void onNotify(UUID service, UUID character, byte[] msg) {
                                    if (fangkongProtocol != null) {
                                        fangkongProtocol.receiveMessage(msg);
                                    }
                                }

                                @Override
                                public void onResponse(int code) {
                                    connecting = false;
                                    if (code == REQUEST_SUCCESS) {
                                        ToastManage.self().show("方控连接成功");
                                    } else {
                                        BleManage.self().client().disconnect(fangkongProtocol.getAddress());
                                    }
                                }
                            });
                } else {
                    connecting = false;
                    Log.d(TAG, "onResponse: 方控连接失败!!!");
                }
            }
        });
    }

    public synchronized void disconnect() {
        if (fangkongProtocol != null) {
            BleManage.self().client().disconnect(fangkongProtocol.getAddress());
        }
    }


    public String getModelName() {
        return fangkongProtocol.getModelName();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final MTimeSecondEvent event) {
        String fkaddress = SharedPreUtil.getSharedPreString(CommonData.SDATA_FANGKONG_ADDRESS);
        if (CommonUtil.isNotNull(fkaddress) && BleManage.self().client().getConnectStatus(fkaddress) == STATUS_DEVICE_CONNECTED) {
            postEvent(new PFkEventConnect().setConnected(true));
        } else {
            postEvent(new PFkEventConnect().setConnected(false));
        }
        if (CommonUtil.isNotNull(fkaddress) && BleManage.self().client().getConnectStatus(fkaddress) != STATUS_DEVICE_CONNECTED) {
            connect();
        }
    }
}
