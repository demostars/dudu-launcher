package com.wow.carlauncher.activity;

import android.util.Log;
import android.widget.TextView;

import com.wow.carlauncher.R;
import com.wow.carlauncher.common.CommonData;
import com.wow.carlauncher.common.base.BaseActivity;
import com.wow.carlauncher.ex.manage.ble.BleManage;
import com.wow.carlauncher.ex.plugin.obd.ObdPlugin;
import com.wow.carlauncher.ex.plugin.obd.evnet.PObdEventCarInfo;
import com.wow.carlauncher.ex.plugin.obd.evnet.PObdEventCarTp;
import com.wow.carlauncher.ex.plugin.obd.evnet.PObdEventConnect;
import com.wow.frame.util.CommonUtil;
import com.wow.frame.util.SharedPreUtil;

import org.greenrobot.eventbus.Subscribe;
import org.xutils.view.annotation.ViewInject;

import java.util.Locale;

import static com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED;
import static com.wow.carlauncher.common.CommonData.TAG;

/**
 * Created by 10124 on 2018/4/25.
 */

public class CarInfoActivity extends BaseActivity {
    @ViewInject(R.id.tv_info_speed)
    private TextView tv_info_speed;

    @ViewInject(R.id.tv_info_rev)
    private TextView tv_info_rev;

    @ViewInject(R.id.tv_info_wtemp)
    private TextView tv_info_wtemp;

    @ViewInject(R.id.tv_info_oil)
    private TextView tv_info_oil;

    @ViewInject(R.id.tv_tp_lf)
    private TextView tv_tp_lf;

    @ViewInject(R.id.tv_tp_rf)
    private TextView tv_tp_rf;

    @ViewInject(R.id.tv_tp_lb)
    private TextView tv_tp_lb;

    @ViewInject(R.id.tv_tp_rb)
    private TextView tv_tp_rb;

    @ViewInject(R.id.tv_tp_title)
    private TextView tv_tp_title;

    @Override
    public void init() {
        setContent(R.layout.activity_car_info);
    }

    @Override
    public void initView() {
        setTitle("车辆信息");

        onEventMainThread(ObdPlugin.self().getCurrentPObdEventCarInfo());
        onEventMainThread(ObdPlugin.self().getCurrentPObdEventCarTp());
        refreshObdState();
    }

    private void refreshObdState() {
        String address = SharedPreUtil.getSharedPreString(CommonData.SDATA_OBD_ADDRESS);
        if (CommonUtil.isNotNull(address)) {
            Log.d(TAG, "refreshObdState: " + BleManage.self().client().getConnectStatus(address) + " " + STATUS_DEVICE_CONNECTED);
            if (BleManage.self().client().getConnectStatus(address) == STATUS_DEVICE_CONNECTED) {
                setTitle("OBD(已连接)");
                if (ObdPlugin.self().supportTp()) {
                    tv_tp_title.setText("胎压数据:");
                } else {
                    tv_tp_title.setText("胎压数据(不支持):");
                }
            } else {
                setTitle("OBD(未连接)");
            }
        } else {
            setTitle("OBD(未绑定)");
        }
    }

    @Subscribe
    public void onEventMainThread(PObdEventConnect event) {
        refreshObdState();
    }

    @Subscribe
    public void onEventMainThread(PObdEventCarInfo event) {
        if (event.getSpeed() != null) {
            String msg = "车速:" + event.getSpeed() + "KM/H";
            tv_info_speed.setText(msg);
        }
        if (event.getRev() != null) {
            String msg = "转速:" + event.getRev() + "R/S";
            tv_info_rev.setText(msg);
        }
        if (event.getWaterTemp() != null) {
            String msg = "水温:" + event.getWaterTemp() + "℃";
            tv_info_wtemp.setText(msg);
        }
        if (event.getOilConsumption() != null) {
            String msg = "油量:" + event.getOilConsumption() + "%";
            tv_info_oil.setText(msg);
        }
    }

    @Subscribe
    public void onEventMainThread(PObdEventCarTp event) {
        if (event.getlFTirePressure() != null && event.getlFTemp() != null) {
            String msg = "左前轮:" + String.format(Locale.CHINA, "%.1f", event.getlFTirePressure()) + "/" + event.getlFTemp() + "℃";
            tv_tp_lf.setText(msg);
        }
        if (event.getlBTemp() != null && event.getlBTirePressure() != null) {
            String msg = "左后轮:" + String.format(Locale.CHINA, "%.1f", event.getlBTirePressure()) + "/" + event.getlBTemp() + "℃";
            tv_tp_lb.setText(msg);
        }

        if (event.getrFTirePressure() != null && event.getrFTemp() != null) {
            String msg = "右前轮:" + String.format(Locale.CHINA, "%.1f", event.getrFTirePressure()) + "/" + event.getrFTemp() + "℃";
            tv_tp_rf.setText(msg);
        }


        if (event.getrBTirePressure() != null && event.getrBTemp() != null) {
            String msg = "右后轮:" + String.format(Locale.CHINA, "%.1f", event.getrBTirePressure()) + "/" + event.getrBTemp() + "℃";
            tv_tp_rb.setText(msg);
        }
    }
}
