package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bobo.musicplayer.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import entity.Music;
import util.HttpUtils;

/**
 * Created by Qubo on 2016/10/2.
 * 歌曲列表的Adapter
 */
public class MusicAdapter extends BaseAdapter {
    public static final int HANDLE_IMAGE_LOAD_SUCCESS = 1;
    private List<Music> musics;
    private Context context;
    private ListView listView;
    /**
     * 声明图片下载任务集合
     */
    private List<ImageLoadTask> imageLoadTasks = new ArrayList<>();
    /**
     * 声明用于轮循任务队列的工作线程
     */
    private Thread workThread;
    private boolean isLoop = true;
    /**
     * 声明handler  显示图片
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_IMAGE_LOAD_SUCCESS://图片下载成功
                    ImageLoadTask imageLoadTask = (ImageLoadTask) msg.obj;
                    Bitmap bitmap = imageLoadTask.bitmap;
                    //通过listView.findViewWithTag()方法获取相应的imageView
                    ImageView imageView = (ImageView) listView.findViewWithTag(bitmap);
                    if (imageView !=null) {
                        if (bitmap != null) {
                            imageView.setImageBitmap(imageLoadTask.bitmap);
                        } else {
                            imageView.setImageResource(R.mipmap.ic_launcher);
                        }
                    }
                    break;
            }
        }
    };
    public MusicAdapter(List<Music> musics, Context context,ListView listView) {
        this.musics = musics;
        this.context = context;
        this.listView = listView;
        //初始化并且启动workThread
        workThread = new Thread() {
            @Override
            public void run() {
                //不断轮循任务集合  一旦有任务  则获取然后执行下载操作
                while (isLoop) {
                    if (!imageLoadTasks.isEmpty()) {
                        ImageLoadTask imageLoadTask = imageLoadTasks.remove(0);
                        imageLoadTask.bitmap  = loadBitmap(imageLoadTask.path);//下载图片
                        Message message = Message.obtain();
                        message.what = HANDLE_IMAGE_LOAD_SUCCESS;
                        message.obj = imageLoadTask;
                        handler.sendMessage(message);
                    } else {//没有任务  工作线程等待
                        synchronized (workThread) {
                            try {
                                workThread.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        workThread.start();

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
        //设置图片 向任务集合中添加一个图片下载任务
        //给imageView设置一个tag值  用于handler中通过tag值找到imageView
        holder.ivPic.setTag(music.getPic_small());
        ImageLoadTask imageLoadTask = new ImageLoadTask();
        imageLoadTask.path = music.getPic_small();
        imageLoadTasks.add(imageLoadTask);
        //唤醒工作线程  赶紧起来干活
        synchronized (workThread) {
            workThread.notify();
        }
        return convertView;
    }

    class ViewHolder {
        ImageView ivPic;
        TextView tvName;
        TextView tvSinger;
    }

    /**
     * 封装一个图片下载任务
     */
    class ImageLoadTask {
        /**
         * 图片路径
         */
        String path;
        /**
         * 通过路径下载成功的bitmap
         */
        Bitmap bitmap;
    }

    /**
     * 下载图片
     * @param path
     * @return
     */
    public Bitmap loadBitmap(String path) {
        Bitmap bitmap = null;
        try {
            InputStream is = HttpUtils.get(path);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
