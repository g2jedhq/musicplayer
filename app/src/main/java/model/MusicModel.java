package model;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import entity.LrcLine;
import entity.Music;
import entity.SongInfo;
import entity.SongUrl;
import util.HttpUtils;
import util.JsonParser;
import util.LrcUtils;
import util.UrlFactory;
import util.XmlParser;

/**
 * Created by Qubo on 2016/10/2.
 * 音乐相关的业务类
 */
public class MusicModel {
    /**
     * 下载歌词
     *
     * @param lrcUrl
     * @param callback
     */
    public void downloadLrc(final Context context, final String lrcUrl, final LrcCallback callback) {
        //异步发送http请求
        new AsyncTask<String, String, List<LrcLine>>() {
            @Override
            protected List<LrcLine> doInBackground(String... params) {
                //下载歌词

                //                try {
                //                    InputStream is = HttpUtils.get(lrcUrl);
                //                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                //                    String line;
                //                    while ((line = reader.readLine()) != null) {// 按行读取输入流
                //                        //[00:02.21]独角戏
                //                        //[00:04.20]演唱：许茹芸
                //                        if ("".equals(line)) {
                //                            continue;// 结束本次循环，开始下一次
                //                        }
                //                        String time = line.substring(1,line.indexOf("]"));// 左闭右开
                //                        String content = line.substring(line.indexOf("]") + 1);
                //                        LrcLine lrcLine = new LrcLine(time,content);
                //                        lrcLines.add(lrcLine);
                //                    }
                //                    return lrcLines;
                //                } catch (IOException e) {
                //                    e.printStackTrace();
                //                }
                try {
                    // 先从文件中加载
                    String fileName = lrcUrl.substring(lrcUrl.lastIndexOf("/") + 1);
                    File targetFile = new File(context.getCacheDir(), fileName);
                    List<LrcLine> lrcLines = LrcUtils.parseLrc(targetFile);
                    if (lrcLines != null) {//已经读取到缓存的歌词
                        return lrcLines;//不需要重新下载
                    }
                    // 没有缓存的歌词，从输入流中加载
                    InputStream is = HttpUtils.get(lrcUrl);
                    lrcLines = LrcUtils.parseLrc(is, targetFile);
                    return lrcLines;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override//主线程中执行   调用回调方法  返回list
            protected void onPostExecute(List<LrcLine> lrcLines) {
                callback.onLrcLoaded(lrcLines);
            }
        }.execute();
    }


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
     *
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
                    callback.onSongInfoLoaded(null, null);
                }
            }
        };
        task.execute();
    }

    /**
     * 根据关键字查询音乐结果列表
     *
     * @param key
     * @param callback
     */
    public void searchMusic(final String key, final Callback callback) {
        new AsyncTask<String, String, List<Music>>() {
            @Override
            protected List<Music> doInBackground(String[] params) {
                String url = UrlFactory.getSearchMusicUrl(key, 1, 30);
                InputStream is;
                try {
                    is = HttpUtils.get(url);
                    String json = HttpUtils.isToString(is);
                    //解析json
                    List<Music> musics = JsonParser.parseSearchResult(json);
                    if (musics != null) {
                        for (Music music : musics) {
                            String title = music.getTitle();// "title": "最炫<em>小苹果</em>",// "title": "<em>小苹果</em>c调伴奏",
                            if (title.contains("<em>")) {
                                String[] array = title.split("/");
                                String t1 = "";
                                for (String arr : array) {
                                    t1 += arr;
                                }
                                String[] array1 = t1.split("<em>");
                                String t2 = "";
                                for (String arr1 : array1) {
                                    t2 += arr1;
                                }
                                music.setTitle(t2);
                            }
                        }
                    }
                    return musics;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            protected void onPostExecute(List<Music> result) {
                callback.onMusicListLoaded(result);
            }
        }.execute();
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

    /**
     * 歌词相关的回调接口
     */
    public interface LrcCallback {
        /**
         * 歌词下载完成后 回调该方法
         *
         * @param lrcLines
         */
        void onLrcLoaded(List<LrcLine> lrcLines);
    }

}
