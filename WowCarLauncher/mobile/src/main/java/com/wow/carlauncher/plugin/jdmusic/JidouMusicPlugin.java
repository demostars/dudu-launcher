package com.wow.carlauncher.plugin.jdmusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.wow.carlauncher.plugin.BasePlugin;
import com.wow.carlauncher.plugin.PluginManage;
import com.wow.carlauncher.plugin.common.music.MusicComPlugin;
import com.wow.carlauncher.plugin.common.music.MusicController;
import com.wow.carlauncher.plugin.common.music.MusicLauncherView;
import com.wow.carlauncher.plugin.common.music.MusicPopupView;
import com.wow.carlauncher.plugin.common.music.MusicView;
import com.wow.carlauncher.plugin.pevent.PEventMusicInfoChange;
import com.wow.carlauncher.plugin.pevent.PEventMusicStateChange;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wow.carlauncher.common.CommonData.TAG;

public class JidouMusicPlugin extends MusicComPlugin implements MusicController {
    public JidouMusicPlugin(Context context, PluginManage pluginManage) {
        super(context, pluginManage);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.ijidou.action.UPDATE_PROGRESS");
        intentFilter.addAction("com.ijidou.card.music");
        this.context.registerReceiver(mReceiver, intentFilter);

    }

    @Override
    public String getName() {
        return "极豆音乐";
    }

    public void play() {
        sendEvent(KeyEvent.KEYCODE_MEDIA_PLAY);
    }

    public void pause() {
        sendEvent(KeyEvent.KEYCODE_MEDIA_PAUSE);
    }

    public void next() {
        sendEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    public void pre() {
        sendEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    private void sendEvent(int event) {
        Intent localObject = new Intent(Intent.ACTION_MEDIA_BUTTON);
        localObject.setPackage("com.ijidou.music");
        localObject.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(System.currentTimeMillis(), System.currentTimeMillis() + 1, 0, event, 0));
        context.sendOrderedBroadcast(localObject, null);

        localObject = new Intent(Intent.ACTION_MEDIA_BUTTON);
        localObject.setPackage("com.ijidou.music");
        localObject.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(System.currentTimeMillis(), System.currentTimeMillis() + 1, 1, event, 0));
        context.sendOrderedBroadcast(localObject, null);
    }

    @Override
    public void destroy() {
        super.destroy();
        context.unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        String music_title;
        String music_artist;

        public void onReceive(Context paramAnonymousContext, Intent intent) {
            if (intent.getAction().equals("com.ijidou.card.music")) {
                boolean music_status = intent.getBooleanExtra("music_status", false);
                EventBus.getDefault().post(new PEventMusicStateChange());
                refreshState(music_status);
                music_artist = intent.getStringExtra("music_artist");
                music_title = intent.getStringExtra("music_title");
                if (music_title != null && music_artist != null) {
                    refreshInfo(music_title, music_artist);
                }
            }
            if (intent.getAction().equals("com.ijidou.action.UPDATE_PROGRESS")) {
                int elapse = intent.getIntExtra("elapse", 0);
                int duration = intent.getIntExtra("duration", 0);
                refreshProgress(elapse, duration);
            }
        }
    };
}