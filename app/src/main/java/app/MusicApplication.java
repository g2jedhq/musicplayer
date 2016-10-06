package app;

import android.app.Application;

import java.util.List;

import entity.Music;

/**
 * Created by Qubo on 2016/10/5.
 * 当app启动时创建
 */
public class MusicApplication extends Application {
    private List<Music> musicPlayList;
    private int position;

    public List<Music> getMusicPlayList() {
        return musicPlayList;
    }

    public void setMusicPlayList(List<Music> musicPlayList) {
        this.musicPlayList = musicPlayList;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
}
