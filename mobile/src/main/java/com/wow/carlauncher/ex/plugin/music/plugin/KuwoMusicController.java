package com.wow.carlauncher.ex.plugin.music.plugin;

import android.content.Context;
import android.content.Intent;

import com.wow.carlauncher.common.CommonData;
import com.wow.carlauncher.common.LrcAnalyze;
import com.wow.carlauncher.common.TaskExecutor;
import com.wow.carlauncher.common.util.CommonUtil;
import com.wow.carlauncher.common.util.SharedPreUtil;
import com.wow.carlauncher.ex.manage.time.event.MTimeSecondEvent;
import com.wow.carlauncher.ex.plugin.music.MusciCoverUtil;
import com.wow.carlauncher.ex.plugin.music.MusicController;
import com.wow.carlauncher.ex.plugin.music.MusicPlugin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.kuwo.autosdk.api.KWAPI;
import cn.kuwo.autosdk.api.OnGetLyricsListener;
import cn.kuwo.autosdk.api.OnGetSongImgUrlListener;
import cn.kuwo.autosdk.api.PlayState;
import cn.kuwo.base.bean.Music;

import static cn.kuwo.autosdk.api.PlayerStatus.PLAYING;

/**
 * Created by 10124 on 2017/10/26.
 */

public class KuwoMusicController extends MusicController {

    private KWAPI mKwapi;
    private boolean run;
    private Music nowMusic;
    private List<LrcAnalyze.LrcData> lrcDatas;

    public void init(Context context, MusicPlugin musicView) {
        super.init(context, musicView);
        mKwapi = KWAPI.createKWAPI(context.getApplicationContext(), "auto");
        mKwapi.bindAutoSdkService();
        mKwapi.registerConnectedListener(b -> {
            if (b) {
                setRun(CommonUtil.equals(PLAYING, mKwapi.getPlayerStatus()));
                nowMusic = mKwapi.getNowPlayingMusic();
                if (nowMusic != null) {
                    lrcDatas = null;
                    musicPlugin.refreshInfo(nowMusic.name, nowMusic.artist, false);
                    if (!SharedPreUtil.getBoolean(CommonData.SDATA_MUSIC_INSIDE_COVER, true)) {
                        MusciCoverUtil.loadCover(nowMusic.name, nowMusic.artist, musicPlugin);
                    } else {
                        mKwapi.getSongPicUrl(nowMusic, onGetSongImgUrlListener);
                    }
                    mKwapi.getLyrics(nowMusic, onGetLyricsListener);
                }
            } else {
                setRun(false);

                if (!mKwapi.isKuwoRunning()) {
                    mKwapi.startAPP(context, true);
                    toHome();
                }
            }
        });
        mKwapi.registerPlayerStatusListener((playerStatus, music) -> {
            nowMusic = music;
            setRun(CommonUtil.equals(PLAYING, playerStatus));
            lrcDatas = null;
            musicPlugin.refreshInfo(nowMusic.name, nowMusic.artist, false);
            if (!SharedPreUtil.getBoolean(CommonData.SDATA_MUSIC_INSIDE_COVER, true)) {
                MusciCoverUtil.loadCover(nowMusic.name, nowMusic.artist, musicPlugin);
            } else {
                mKwapi.getSongPicUrl(nowMusic, onGetSongImgUrlListener);
            }
            mKwapi.getLyrics(nowMusic, onGetLyricsListener);
        });
        mKwapi.registerExitListener(() -> {
            setRun(false);
        });
        mKwapi.openOrCloseToast(false);

        EventBus.getDefault().register(this);
    }

    private void setRun(boolean run) {
        this.run = run;
        musicPlugin.refreshState(run, true);
    }

    @Override
    public String clazz() {
        return "cn.kuwo.kwmusiccar";
    }

    public void play() {
        if (!mKwapi.isKuwoRunning()) {
            mKwapi.startAPP(context, true);
            toHome();
        } else {
            mKwapi.setPlayState(PlayState.STATE_PLAY);
        }
    }

    public void pause() {
        if (!mKwapi.isKuwoRunning()) {
            mKwapi.startAPP(context, true);
            toHome();
        } else {
            mKwapi.setPlayState(PlayState.STATE_PAUSE);
        }
    }

    public void next() {
        if (!mKwapi.isKuwoRunning()) {
            mKwapi.startAPP(context, true);
            toHome();
        } else {
            mKwapi.setPlayState(PlayState.STATE_NEXT);
        }
    }

    public void pre() {
        if (!mKwapi.isKuwoRunning()) {
            mKwapi.startAPP(context, true);
            toHome();
        } else {
            mKwapi.setPlayState(PlayState.STATE_PRE);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(final MTimeSecondEvent event) {
        int totalTime = mKwapi.getCurrentMusicDuration();
        long overTime = System.currentTimeMillis() + totalTime - mKwapi.getCurrentPos();

        //每隔一秒钟上报一下进度信息
        int ccc = (int) (totalTime + System.currentTimeMillis() - overTime);
        if (ccc < totalTime && run) {
            musicPlugin.refreshProgress((int) (totalTime + System.currentTimeMillis() - overTime), totalTime);
        }

        if (lrcDatas != null) {
            long xxx = (int) (totalTime + System.currentTimeMillis() - overTime);
            try {
                LrcAnalyze.LrcData lll = null;
                List<LrcAnalyze.LrcData> remove = new ArrayList<>();
                List<LrcAnalyze.LrcData> tempLrc = new ArrayList<>(lrcDatas);
                for (LrcAnalyze.LrcData lrc : tempLrc) {
                    if (lrc.getTimeMs() < xxx) {
                        lll = lrc;
                        remove.add(lrc);
                    } else {
                        break;
                    }
                }

                lrcDatas.removeAll(remove);
                if (lll != null) {
                    musicPlugin.refreshLrc(lll.getLrcLine());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void toHome() {
        TaskExecutor.self().post(() -> {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(home);
        }, 2000);
    }

    @Override
    public void destroy() {
        mKwapi.unRegisterPlayerStatusListener();
        mKwapi.unRegisterExitListener();
        mKwapi.unBindKuWoApp();
        EventBus.getDefault().unregister(this);
    }

    private OnGetSongImgUrlListener onGetSongImgUrlListener = new OnGetSongImgUrlListener() {
        @Override
        public void onGetSongImgUrlSucessed(Music music, String s) {
            musicPlugin.refreshCover(s);
        }

        @Override
        public void onGetSongImgUrlFailed(Music music, int i) {
            musicPlugin.refreshCover(null);
        }
    };

    private OnGetLyricsListener onGetLyricsListener = new OnGetLyricsListener() {
        @Override
        public void sendSyncNotice_LyricsStart(Music music) {
        }

        @Override
        public void sendSyncNotice_LyricsFinished(Music music, String s) {
            if (CommonUtil.equals(nowMusic, music)) {
                LrcAnalyze lrcAnalyze = new LrcAnalyze(s);
                lrcDatas = lrcAnalyze.lrcList();
                musicPlugin.refreshInfo(nowMusic.name, nowMusic.artist, lrcDatas != null);
            }
        }

        @Override
        public void sendSyncNotice_LyricsFailed(Music music) {
        }

        @Override
        public void sendSyncNotice_LyricsNone(Music music) {
        }
    };
}