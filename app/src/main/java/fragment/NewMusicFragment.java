package fragment;

import android.os.Bundle;
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
 * 显示新歌榜的数据列表
 */
public class NewMusicFragment extends Fragment {
    private ListView listView;
    private MusicAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, null);
        setViews(view);//初始化控件
        MusicModel musicModel = new MusicModel();
        musicModel.findNewMusicList(new MusicModel.Callback() {
            @Override//将会在列表加载完毕后执行
            public void onMusicListLoaded(List<Music> musics) {
                setAdapter(musics);
            }
        }, 0, 50);//查询新歌榜榜单数据 List<Music>
        return view;
    }

    private void setAdapter(List<Music> musics) {
        adapter = new MusicAdapter(musics,getActivity(),listView);
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
