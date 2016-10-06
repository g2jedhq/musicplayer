package fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bobo.musicplayer.R;

import java.util.List;

import adapter.MusicAdapter;
import app.MusicApplication;
import entity.Music;
import entity.SongInfo;
import entity.SongUrl;
import model.MusicModel;
import service.PlayMusicService;

/**
 * Created by Qubo on 2016/10/1.
 * 显示热歌榜榜单
 */
public class HotMusicFragment extends Fragment {
    private ListView listView;
    private MusicAdapter adapter;
    private MusicModel musicModel;
    private PlayMusicService.MusicBinder musicBinder;
    private List<Music> musics;

    public void setMusicBinder(PlayMusicService.MusicBinder musicBinder) {
        this.musicBinder = musicBinder;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, null);
        setViews(view);
        musicModel = new MusicModel();
        musicModel.findHotMusicList(new MusicModel.Callback() {
            @Override
            public void onMusicListLoaded(List<Music> musics) {
                HotMusicFragment.this.musics = musics;
                setAdapter(musics);
            }
        }, 0, 50);
        setListeners();
        return view;
    }

    private void setListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //把当前播放列表与position保存到MusicApplication中
                MusicApplication app = (MusicApplication) getActivity().getApplication();
                app.setMusicPlayList(musics);
                app.setPosition(position);
                final Music music = adapter.getItem(position);
                musicModel.getSongInfoBySongId(music.getSong_id(), new MusicModel.SongInfoCallback() {
                    @Override
                    public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
                        if (urls ==null || info == null) {
                            Toast.makeText(HotMusicFragment.this.getContext(), "音乐加载失败, 检查网络", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //开始准备播放音乐
                        music.setSongUrls(urls);
                        music.setSongInfo(info);
                        //获取当前需要播放的音乐的路径
                        String musicPath = urls.get(0).getShow_link();
                        // 开始播放
                        musicBinder.playMusic(musicPath);

                    }
                });
            }
        });
    }

    private void setAdapter(List<Music> musics) {
        adapter = new MusicAdapter(musics, getContext(), listView);
        listView.setAdapter(adapter);
    }

    private void setViews(View view) {
        listView = (ListView) view.findViewById(R.id.listView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.stopThread();//把adapter中的线程停掉
    }
}
