package entity;

/**
 * Created by Qubo on 2016/10/4.
 * 描述一首歌曲的地址信息
 */
public class SongUrl {
    /**
     * 歌曲的表演播放链接
     */
    private String show_link;
    /**
     * 歌曲文件id
     */
    private String song_file_id;
    /**
     * 歌曲的大小
     */
    private String file_size;
    /**
     * 歌曲的格式
     */
    private String file_extension;
    /**
     * 歌曲的时长单位：s
     */
    private String file_duration;
    /**
     * 歌曲的比特率
     */
    private String file_bitrate;
    /**
     * 歌曲的下载链接
     */
    private String file_link;

    public SongUrl() {
    }

    public SongUrl(String show_link, String song_file_id, String file_size, String file_extension, String file_duration, String file_bitrate, String file_link) {
        this.show_link = show_link;
        this.song_file_id = song_file_id;
        this.file_size = file_size;
        this.file_extension = file_extension;
        this.file_duration = file_duration;
        this.file_bitrate = file_bitrate;
        this.file_link = file_link;
    }

    public String getShow_link() {
        return show_link;
    }

    public void setShow_link(String show_link) {
        this.show_link = show_link;
    }

    public String getSong_file_id() {
        return song_file_id;
    }

    public void setSong_file_id(String song_file_id) {
        this.song_file_id = song_file_id;
    }

    public String getFile_size() {
        return file_size;
    }

    public void setFile_size(String file_size) {
        this.file_size = file_size;
    }

    public String getFile_extension() {
        return file_extension;
    }

    public void setFile_extension(String file_extension) {
        this.file_extension = file_extension;
    }

    public String getFile_duration() {
        return file_duration;
    }

    public void setFile_duration(String file_duration) {
        this.file_duration = file_duration;
    }

    public String getFile_bitrate() {
        return file_bitrate;
    }

    public void setFile_bitrate(String file_bitrate) {
        this.file_bitrate = file_bitrate;
    }

    public String getFile_link() {
        return file_link;
    }

    public void setFile_link(String file_link) {
        this.file_link = file_link;
    }

    @Override
    public String toString() {
        return "SongUrl{" +
                "show_link='" + show_link + '\'' +
                ", song_file_id='" + song_file_id + '\'' +
                ", file_size='" + file_size + '\'' +
                ", file_extension='" + file_extension + '\'' +
                ", file_duration='" + file_duration + '\'' +
                ", file_bitrate='" + file_bitrate + '\'' +
                ", file_link='" + file_link + '\'' +
                '}';
    }
}
