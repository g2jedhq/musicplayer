package model;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import entity.Music;
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

    public interface Callback {
        /**
         * 当列表加载完毕后 将会调用该方法
         * 在该方法的实现中需要执行列表加载完毕后的业务逻辑
         *
         * @param musics
         */
        void onMusicListLoaded(List<Music> musics);
    }


}
