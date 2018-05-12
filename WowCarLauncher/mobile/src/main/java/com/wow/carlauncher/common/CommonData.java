package com.wow.carlauncher.common;

public class CommonData {
    public static final String TAG = "WOW_CAR";

    public static final String[] POPUP_SIZE = {"小", "中", "大"};

    public static final String PACKAGE_NAME = "com.wow.carlauncher";

    //
    public static final int REQUEST_SELECT_APP_TO_DOCK1 = 10000;
    public static final int REQUEST_SELECT_APP_TO_DOCK2 = 10001;
    public static final int REQUEST_SELECT_APP_TO_DOCK3 = 10002;
    public static final int REQUEST_SELECT_APP_TO_DOCK4 = 10003;
    public static final int REQUEST_SELECT_APP_TO_DOCK5 = 10004;

    //-------------------------------Intent传输数据时使用的mark-start-----------------------------------
    //应用包名
    public static final String IDATA_APP_PACKAGE_NAME = "IDATA_APP_PACKAGE_NAME";
    //应用标记
    public static final String IDATA_APP_MARK = "IDATA_APP_MARK";
    //-------------------------------Intent传输数据时使用的mark-end-----------------------------------


    //-------------------------------本地缓存使用的mark-start-----------------------------------
    //音乐播放器的控制器
    public static final String SDATA_MUSIC_CONTROLLER = "SDATA_MUSIC_CONTROLLER";

    //-------------------------------方控设置相关-start-----------------------------------
    //方控的控制器
    public static final String SDATA_FANGKONG_CONTROLLER = "SDATA_FANGKONG_CONTROLLER";
    //方控的名称
    public static final String SDATA_FANGKONG_NAME = "SDATA_FANGKONG_NAME";
    //方控的地址
    public static final String SDATA_FANGKONG_ADDRESS = "SDATA_FANGKONG_ADDRESS";
    //-------------------------------方控设置相关-end-----------------------------------

    //-------------------------------OBD设置相关-start-----------------------------------
    //OBD的控制器
    public static final String SDATA_OBD_CONTROLLER = "SDATA_OBD_CONTROLLER";
    //OBD的名称
    public static final String SDATA_OBD_NAME = "SDATA_OBD_NAME";
    //OBD的地址
    public static final String SDATA_OBD_ADDRESS = "SDATA_OBD_ADDRESS";
    //-------------------------------OBD设置相关-end-----------------------------------
    //dock的包名start
    public static final String SDATA_DOCK1_CLASS = "DOCK1_BEAN";
    public static final String SDATA_DOCK2_CLASS = "DOCK2_BEAN";
    public static final String SDATA_DOCK3_CLASS = "DOCK3_BEAN";
    public static final String SDATA_DOCK4_CLASS = "DOCK4_BEAN";
    public static final String SDATA_DOCK5_CLASS = "DOCK5_BEAN";
    //dock的包名end
    //悬浮框的X坐标
    public static final String SDATA_POPUP_WIN_X = "SDATA_POPUP_WIN_X";
    //悬浮框的Y坐标
    public static final String SDATA_POPUP_WIN_Y = "SDATA_POPUP_WIN_Y";
    //悬浮框展示的APP
    public static final String SDATA_POPUP_SHOW_APPS = "SDATA_POPUP_SHOW_APPS";
    //悬浮框是否全局展示
    public static final String SDATA_POPUP_SHOW_TYPE = "SDATA_POPUP_SHOW_TYPE";
    //悬浮框当前的插件
    public static final String SDATA_POPUP_CURRENT_PLUGIN = "SDATA_POPUP_CURRENT_PLUGIN_";
    //是否允许悬浮框展示
    public static final String SDATA_LAUNCHER_DOCK_LABEL_SHOW = "SDATA_LAUNCHER_DOCK_LABEL_SHOW";
    //是否允许悬浮框展示
    public static final String SDATA_POPUP_ALLOW_SHOW = "SDATA_POPUP_ALLOW_SHOW";
    //悬浮框是否覆盖状态栏
    public static final String SDATA_POPUP_FULL_SCREEN = "SDATA_POPUP_FULL_SCREEN";
    //悬浮框尺寸
    public static final String SDATA_POPUP_SIZE = "SDATA_POPUP_SIZE";
    //隐藏的APP的包名，格式：[包名],[包名],[包名],[包名]
    public static final String SDATA_HIDE_APPS = "SDATA_HIDE_APPS";
    //时间插件打开的APP
    public static final String SDATA_TIME_PLUGIN_OPEN_APP = "SDATA_TIME_PLUGIN_OPEN_APP";
    //时间插件打开的APP
    public static final String SDATA_CONSOLE_MARK = "SDATA_CONSOLE_MARK";
    //天气所属的城市
    public static final String SDATA_WEATHER_CITY = "SDATA_WEATHER_CITY";
    //是否接受开发中的APP包
    public static final String SDATA_ALLOW_DEBUG_APP = "SDATA_ALLOW_DEBUG_APP";
    //应用包名标记使用的间隔
    public static final String CONSTANT_APP_PACKAGE_SEPARATE = ":";

    //行程以app启动为准
    public static final String SDATA_TRIP_START_WITH_APP = "SDATA_TRIP_START_WITH_APP";

    //-------------------------------本地缓存使用的mark-end-----------------------------------
}
