package app;

import android.app.Application;

import java.util.List;

import entity.LrcLine;
import entity.Music;

/**
 * Created by Qubo on 2016/10/5.
 * 当app启动时创建
 */
public class MusicApplication extends Application {
    private List<Music> musicPlayList;
    private int position;
    private List<LrcLine> lrcLines;

    public List<LrcLine> getLrcLines() {
        return lrcLines;
    }

    public void setLrcLines(List<LrcLine> lrcLines) {
        this.lrcLines = lrcLines;
    }

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

    /**
     * 获取当前音乐
     *
     * @return
     */
    public Music getCurrentMusic() {
        // java.lang.ArrayIndexOutOfBoundsException
        return musicPlayList.get(position);//length=60; index=-1
    }

    /**
     * position到上一首
     */
    public void previousMusic() {
        position = position == 0 ? 0 : position - 1;
    }

    /**
     * position到下一首
     */
    public void nextMusic() {
        position = position == musicPlayList.size() - 1 ? 0 : position + 1;
    }


    @Override
    public void onCreate() {
        super.onCreate();

    }
}
