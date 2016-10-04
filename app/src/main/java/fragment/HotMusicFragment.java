package fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bobo.musicplayer.R;

import java.util.List;

import adapter.MusicAdapter;
import entity.Music;
import model.MusicModel;

/**
 * Created by Qubo on 2016/10/1.
 * 显示热歌榜榜单
 */
public class HotMusicFragment extends Fragment {
    private ListView listView;
    private MusicAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, null);
        setViews(view);
        MusicModel musicModel = new MusicModel();
        musicModel.findHotMusicList(new MusicModel.Callback() {
            @Override
            public void onMusicListLoaded(List<Music> musics) {
                setAdapter(musics);
            }
        }, 0, 50);
        return view;
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
