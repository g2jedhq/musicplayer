package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bobo.musicplayer.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.Music;
import util.BitmapUtils;
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
     * 声明用于实现内存缓存的map，构造一个具有默认初始容量 (16) 和默认加载因子 (0.75) 的空 HashMap
     */
    private Map<String,SoftReference<Bitmap>> cache = new HashMap<>();
    /**
     * 声明handler  显示图片
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_IMAGE_LOAD_SUCCESS://图片下载成功
                    ImageLoadTask imageLoadTask = (ImageLoadTask) msg.obj;
                    Bitmap bitmap = imageLoadTask.bitmap;
                    //通过listView.findViewWithTag()方法获取相应的imageView
                    ImageView imageView = (ImageView) listView.findViewWithTag(imageLoadTask.path);
                    if (imageView != null) {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(R.mipmap.ic_launcher);
                        }
                    }
                    break;
            }
        }
    };

    public MusicAdapter(List<Music> musics, Context context, ListView listView) {
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
                        imageLoadTask.bitmap = loadBitmap(imageLoadTask.path);//下载图片
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
        String path = music.getPic_small();
        //设置图片  先从缓存中读取图片
        SoftReference<Bitmap> ref = cache.get(path);
        if (ref != null) {
            Log.i("TAG", "从内存中获取的图片...");
            Bitmap bitmap = ref.get();
            if (bitmap!=null) {//图片还没有被销毁
                holder.ivPic.setImageBitmap(bitmap);
                return convertView;
            }
        }
        // 内存缓存中没有图片  则去文件缓存中读取
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        File file = new File(context.getCacheDir(),fileName);
        Bitmap bitmap = BitmapUtils.loadBitmap(file.getAbsolutePath());
        if (bitmap!=null) {
            Log.i("TAG", "从文件中获取的图片....");
            holder.ivPic.setImageBitmap(bitmap);
            //向内存缓存中再存一次
            cache.put(path, new SoftReference<Bitmap>(bitmap));
            return convertView;
        }
        // 如果缓存和文件中都没有，设置图片 向任务集合中添加一个图片下载任务
        //给imageView设置一个tag值  用于handler中通过tag值找到imageView
        holder.ivPic.setTag(path);
        ImageLoadTask imageLoadTask = new ImageLoadTask();
        imageLoadTask.path = path;
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
     * 停止线程
     */
    public void stopThread() {
        isLoop = false;
        synchronized (workThread) {
            //唤醒工作线程
            workThread.notify();
        }
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
     *
     * @param path
     * @return
     */
    public Bitmap loadBitmap(String path) {
        try {
            InputStream is = HttpUtils.get(path);
            // BitmapFactory.decodeStream(is)
             Bitmap bitmap = BitmapUtils.loadBitmap(is, 50, 50);// 压缩图片
            //下载成功  把bitmap存入内存缓存中
            cache.put(path, new SoftReference<Bitmap>(bitmap));
            //存入文件缓存目录中
            // path = http://musicdata.baidu.com/data2/pic/5b13c6847088bda14e02a681f962061e/271831977/271831977.jpg
            // public String substring(int beginIndex),参数beginIndex - 起始索引（包括）
            // 返回一个新的字符串，它是此字符串的一个子字符串。该子字符串从指定索引处的字符开始，直到此字符串末尾。
            String fileName = path.substring(path.lastIndexOf("/")+1);
            // public int lastIndexOf(String str)返回指定子字符串str在此字符串中最右边出现处的索引
            // file --> /data/data/cn.tedu.music/cache/xxxxx.jpg
            File file = new File(context.getCacheDir(),fileName);
            BitmapUtils.saveBitmap(bitmap, file.getAbsolutePath());
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
