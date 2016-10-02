package util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.util.List;

import entity.Music;

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
        // Music实体类并不包含所有的名值对
        List<Music> musics = JSON.parseArray(song_list.toString(), Music.class);
        return musics;
    }


}
