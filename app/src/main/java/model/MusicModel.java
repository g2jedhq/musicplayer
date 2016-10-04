package model;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import entity.Music;
import entity.SongInfo;
import entity.SongUrl;
import util.HttpUtils;
import util.JsonParser;
import util.UrlFactory;
import util.XmlParser;

/**
 * Created by Qubo on 2016/10/2.
 * 音乐相关的业务类
 */
public class MusicModel {
    /**
     * 查询新歌榜榜单
     *
     * @param callback
     * @param offset
     * @param size
     */
    public void findNewMusicList(final Callback callback, final int offset, final int size) {
        AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>() {
            @Override// 执行在子线程中，处理耗时操作   发送 http请求 解析List
            protected List<Music> doInBackground(String... params) {
                String path = UrlFactory.getNewMusicListXmlUrl(offset, size);
                List<Music> musics = new ArrayList<>();
                try {
                    InputStream is = HttpUtils.get(path);
                    //                    String xml = HttpUtils.isToString(is);
                    //                    Log.i("TAG", "doInBackground: "+xml);
                    musics = XmlParser.parseMusicList(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return musics;
            }

            @Override//主线程中执行  调用callback的方法 执行后续操作
            protected void onPostExecute(List<Music> musics) {
                if (callback != null) {//更新UI界面
                    callback.onMusicListLoaded(musics);
                }
            }
        };

        task.execute();//执行异步任务


    }

    /**
     * 查询热歌榜榜单
     *
     * @param callback
     * @param offset
     * @param size
     */
    public void findHotMusicList(final Callback callback, final int offset, final int size) {
        AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>() {
            @Override// 执行在子线程中，处理耗时操作   发送 http请求 解析List
            protected List<Music> doInBackground(String... params) {
                String path = UrlFactory.getHotMusicListJsonUrl(offset, size);
                List<Music> musics = new ArrayList<>();
                try {
                    InputStream is = HttpUtils.get(path);
                    String jsonStr = HttpUtils.isToString(is);
                    musics = JsonParser.parseMusicList(jsonStr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return musics;
            }

            @Override//主线程中执行  调用callback的方法 执行后续操作
            protected void onPostExecute(List<Music> musics) {
                if (callback != null) {//更新UI界面
                    callback.onMusicListLoaded(musics);
                }
            }
        };
        task.execute();//执行异步任务
    }

    /**
     * 异步发送请求   解析json获取：  List<SongUrl>  SongInfo
     * 在主线程中调用callback.onSongInfoLoaded()
     * @param songId
     * @param callback
     */
    public void getSongInfoBySongId(final String songId, final SongInfoCallback callback) {
        AsyncTask<String, String, Music> task = new AsyncTask<String, String, Music>() {
            @Override//在工作线程中发送请求  解析json
            protected Music doInBackground(String... params) {
                String path = UrlFactory.getSongInfoUrl(songId);
                try {
                    InputStream is = HttpUtils.get(path);
                    String json = HttpUtils.isToString(is);
                    // 解析json
                    Music music = JsonParser.parseSongInfo(json);
                    return music;

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override//主线程中调用callback回调方法
            protected void onPostExecute(Music music) {
                if (music != null) {
                    callback.onSongInfoLoaded(music.getSongUrls(), music.getSongInfo());
                } else {
                    callback.onSongInfoLoaded(null,null);
                }
            }
        };
    }

    public interface Callback {
        /**
         * 当列表加载完毕后 将会调用该方法
         * 在该方法的实现中需要执行列表加载完毕后的业务逻辑
         *
         * @param musics
         */
        void onMusicListLoaded(List<Music> musics);
    }

    /**
     * 访问songInfo所需要的回调接口
     */
    public interface SongInfoCallback {
        /**
         * 当音乐的基本信息加载完毕后
         * 将会在主线程中自动执行
         *
         * @param urls
         * @param info
         */
        void onSongInfoLoaded(List<SongUrl> urls, SongInfo info);
    }

}
