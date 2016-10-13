package util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import entity.Music;
import entity.SongInfo;
import entity.SongUrl;

/**
 * Created by Qubo on 2016/10/2.
 * 解析json的工具类
 */
public class JsonParser {
    /**
     * 解析json数据，获取歌曲集合
     * @param jsonStr
     * @return
     */
    public static List<Music> parseMusicList(String jsonStr) {
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(jsonStr);
        JSONArray song_list = jsonObject.getJSONArray("song_list");
        // Music实体类并不包含json中所有的名值对
        List<Music> musics = JSON.parseArray(song_list.toString(), Music.class);
        return musics;
    }

    /**
     *解析json字符串 获取音乐的基本信息
     * @param json
     * @return
     */
    public static Music parseSongInfo(String json) {
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(json);
        JSONObject songurl = jsonObject.getJSONObject("songurl");
        JSONObject songinfo = jsonObject.getJSONObject("songinfo");
        JSONArray url = songurl.getJSONArray("url");
        List<SongUrl> songUrls = JSON.parseArray(url.toString(), SongUrl.class);
        SongInfo songInfo = JSON.parseObject(songinfo.toString(), SongInfo.class);
        Music music = new Music();
        music.setSongUrls(songUrls);
        music.setSongInfo(songInfo);
        return music;
    }
    /**
     * 解析搜索结果列表
     * @param json  {  song_list  : [{},{},{},{}]  }
     * @return 搜到的歌曲的集合
     */
    public static List<Music> parseSearchResult(String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray song_list = jsonObject.getJSONArray("song_list");
        List<Music> musics = JSON.parseArray(song_list.toString(),Music.class);
        return musics;
    }


}
