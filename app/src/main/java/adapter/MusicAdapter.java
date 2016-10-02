package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bobo.musicplayer.R;

import java.util.List;

import entity.Music;

/**
 * Created by Qubo on 2016/10/2.
 * 歌曲列表的Adapter
 */
public class MusicAdapter extends BaseAdapter {
    private List<Music> musics;
    private Context context;

    public MusicAdapter(List<Music> musics, Context context) {
        this.musics = musics;
        this.context = context;
    }

    @Override
    public int getCount() {
        return musics.size();
    }

    @Override
    public Music getItem(int position) {
        return musics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Music music = getItem(position);
        ViewHolder holder;// 声明
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_lv_music, null);
            holder.ivPic = (ImageView) convertView.findViewById(R.id.ivPic);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.tvSinger = (TextView) convertView.findViewById(R.id.tvSinger);
            convertView.setTag(holder);
        }
            holder = (ViewHolder) convertView.getTag();
        // 组装数据和模板
        holder.tvName.setText(music.getTitle());
        holder.tvSinger.setText(music.getArtist_name());
        return convertView;
    }

    class ViewHolder {
        ImageView ivPic;
        TextView tvName;
        TextView tvSinger;
    }

}
