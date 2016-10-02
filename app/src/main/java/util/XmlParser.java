package util;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import entity.Music;

/**
 * Created by Qubo on 2016/10/2.
 *  解析xml的工具类
 */
public class XmlParser {
    /**
     * 解析输入流  获取List<Music>
     * @param is
     * @return
     */
    public static List<Music> parseMusicList(InputStream is) {
        List<Music> musics = new ArrayList<>();
        try {
            //1.  创建XmlPullParser解析器
            XmlPullParser parser = Xml.newPullParser();
            //2.  setInput()
            parser.setInput(is, "utf-8");
            //3.  驱动事件  获取初始事件类型
            int eventType = parser.getEventType();
            Music music = new Music();
            //  不断的驱动事件
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        if ("song".equals(name)) {
                            music = new Music();
                        } else if ("artist_id".equals(name)) {
                            music.setArtist_id(parser.nextText());
                        } else if ("language".equals(name)) {
                            music.setLanguage(parser.nextText());
                        } else if ("pic_big".equals(name)) {
                            music.setPic_big(parser.nextText());
                        } else if ("pic_small".equals(name)) {
                            music.setPic_small(parser.nextText());
                        } else if ("publishtime".equals(name)) {
                            music.setPublishtime(parser.nextText());
                        } else if ("lrclink".equals(name)) {
                            music.setLrclink(parser.nextText());
                        } else if ("all_artist_ting_uid".equals(name)) {
                            music.setAll_artist_ting_uid(parser.nextText());
                        } else if ("all_artist_id".equals(name)) {
                            music.setAll_artist_id(parser.nextText());
                        } else if ("style".equals(name)) {
                            music.setStyle(parser.nextText());
                        } else if ("song_id".equals(name)) {
                            music.setSong_id(parser.nextText());
                        } else if ("title".equals(name)) {
                            music.setTitle(parser.nextText());
                        } else if ("author".equals(name)) {
                            music.setAuthor(parser.nextText());
                        } else if ("album_id".equals(name)) {
                            music.setAlbum_id(parser.nextText());
                        } else if ("album_title".equals(name)) {
                            music.setAlbum_title(parser.nextText());
                        } else if ("artist_name".equals(name)) {
                            music.setArtist_name(parser.nextText());
                            musics.add(music);
                        }
                        break;
                }
                //向后驱动事件
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return musics;
    }

}
