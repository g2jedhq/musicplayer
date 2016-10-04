package util;

/**
 * Created by Qubo on 2016/10/2.
 * URL工厂  整理所有的url地址
 */
public class UrlFactory {
    /**
     * 获取新歌榜的请求地址
     * @param offset  起始条目的下标
     * @param size   返回音乐的个数
     * @return 返回Xml格式数据的地址
     */
    public static String getNewMusicListXmlUrl( int offset, int size) {//type=1
        String path ="http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.billboard.billList&format=xml&type=1&offset="+offset+"&size="+size;
        return path;
    }

    /**
     * 获取热歌榜的请求地址
     * @param offset
     * @param size
     * @return 返回Json格式数据的地址
     */
    public static String getHotMusicListJsonUrl( int offset, int size) {//type=2
        String path ="http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.billboard.billList&format=json&type=2&offset="+offset+"&size="+size;
        return path;
    }
    /**
     * 查询歌曲详细信息的url地址
     * @param songId
     * @return
     */
    public static String getSongInfoUrl(String songId) {
        String path = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.song.getInfos&format=json&songid="+songId+"&ts=1408284347323&e=JoN56kTXnnbEpd9MVczkYJCSx%2FE1mkLx%2BPMIkTcOEu4%3D&nw=2&ucf=1&res=1";
        return path;
    }
}
