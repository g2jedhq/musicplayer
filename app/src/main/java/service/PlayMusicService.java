package service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

import util.GlobalConsts;

public class PlayMusicService extends Service {
    private MediaPlayer player;

    @Override
    public void onCreate() {
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
        /**
         * 播放音乐
         * // 1. 重置
         player.reset();
         // 2. 设置歌曲路径
         player.setDataSource(musics.get(currentMusicIndex).getPath());
         // 3. 缓冲
         player.prepare();
         // 4. 快进
         player.seekTo(pausePosition);
         // 5. 播放
         player.start();
         * @param url  音乐的路径
         */
        public void playMusic(String url) {
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
    }


}
