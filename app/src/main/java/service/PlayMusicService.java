package service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;

import java.io.IOException;

import app.MusicApplication;
import util.GlobalConsts;

public class PlayMusicService extends Service {
    private MediaPlayer player;
    private boolean isLoop = true;
    private String currentUrl;
    private MusicApplication app ;

    @Override
    public void onCreate() {
        app = (MusicApplication) getApplication();
        // 初始化播放器
        player = new MediaPlayer();
        // 设置当准备完成监听器
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override//prepare完成后  执行该方法
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                //发送自定义广播  告诉Activity 音乐已经开始播放
                Intent intent = new Intent(GlobalConsts.ACTION_START_PLAY);
                sendBroadcast(intent);
            }
        });
        // 设置当播放结束监听器
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent intent = new Intent(GlobalConsts.ACTION_NEXT);
                sendBroadcast(intent);
            }
        });
        //启动工作线程  每1秒给Activity发一次广播
        new WorkThread().start();
    }

    /**
     * 当有客户端绑定该service时  执行
     * context.bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }


    /**
     * 返回给客户端的binder对象
     * 声明开放给客户端调用的接口方法
     */
    public class MusicBinder extends Binder {
        public boolean isPlaying;
        /**
         * 播放音乐
         * // 1. 重置
         * player.reset();
         * // 2. 设置歌曲路径
         * player.setDataSource(musics.get(currentMusicIndex).getPath());
         * // 3. 缓冲
         * player.prepare();
         * // 4. 快进
         * player.seekTo(pausePosition);
         * // 5. 播放
         * player.start();
         *
         * @param url 音乐的路径
         */
        public void playMusic(String url) {
            currentUrl = url;
            try {
                player.reset();
                player.setDataSource(url);
                //异步加载音乐信息
                player.prepareAsync();
                //在player准备完成后  执行start播放
                //给player设置监听
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 暂停或播放
         * @return false:表示暂停;   true:表示播放
         */
        public void playOrPause() {
            if (player.isPlaying()) {
                player.pause();
                isPlaying = false;
            } else {
                player.start();
                isPlaying = true;
            }
        }
        /**
         * 跳转到某个播放位置
         * @param progress
         */
        public void seekTo(int progress) {
            player.seekTo(progress);
        }

    }

    class WorkThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isLoop) {
                SystemClock.sleep(1000);
                if (player.isPlaying()) {
                    Intent intent = new Intent(GlobalConsts.ACTION_UPDATE_PROGRESS);
                    intent.putExtra(GlobalConsts.MUSIC_CURRENT_POSITION, player.getCurrentPosition());
                    intent.putExtra(GlobalConsts.MUSIC_DURATION, player.getDuration());
                    sendBroadcast(intent);
                }
            }
        }
    }


}
