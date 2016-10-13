package com.bobo.musicplayer.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bobo.musicplayer.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import adapter.SearchResultAdapter;
import app.MusicApplication;
import entity.LrcLine;
import entity.Music;
import entity.SongInfo;
import entity.SongUrl;
import fragment.HotMusicFragment;
import fragment.NewMusicFragment;
import model.MusicModel;
import service.DownloadService;
import service.PlayMusicService;
import util.BitmapUtils;
import util.GlobalConsts;
import util.LogUtil;

public class MainActivity extends FragmentActivity {
    private RadioGroup radioGroup;
    private RadioButton rbNew;
    private RadioButton rbHot;
    /**
     * 当前播放音乐的图片
     */
    private ImageView ivCMPic;
    /**
     * 当前播放音乐的标题
     */
    private TextView tvCMTitle;
    private ViewPager viewPager;
    private List<Fragment> fragments;
    private PagerAdapter pagerAdapter;
    private ServiceConnection connection;
    private PlayMusicService.MusicBinder musicBinder;
    private UpdateMusicInfoReceiver receiver;
    RelativeLayout rlPlayMusic;
    private TextView tvPMTitle;
    private TextView tvPMSinger;
    private ImageView ivPMAlbum;
    private TextView tvPMLrc1;
    private TextView tvPMLrc0;
    private TextView tvPMCurrentTime;
    private TextView tvPMTotalTime;
    private SeekBar seekBar;
    private ImageView ivPlayModel;
    private ImageView ivPMPrevious;
    private ImageView ivPMPlayOrPause;
    private ImageView ivPMNext;
    private ImageView ivPlayList;
    private ImageView ivPMBackground;
    private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private MusicModel musicModel;
    private MusicApplication app;
    private ObjectAnimator animator;
    private TextView[] textViews = new TextView[2];
    protected List<Music> searchMusicList;
    protected SearchResultAdapter searchMusicAdapter;
    private RelativeLayout rlSearchMusic;
    private EditText etKeyword;
    private ListView lvSearchResult;
    private Button btnSearch;
    int i = 1;
    int j = 0;
    // 播放模式：顺序、随机、单曲循环
    int[] modeRes = {R.mipmap.playmode_normal, R.mipmap.playmode_shuffle, R.mipmap.playmode_repeat_current};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        setContentView(R.layout.activity_main);
        musicModel = new MusicModel();
        app = (MusicApplication) getApplication();
        //控件初始化
        setViews();
        //给viewPager设置适配器
        setViewPagerAdapter();
        //实现tab标签与viewpager的联动
        setListeners();
        //绑定Service
        bindPlayMusicService();
        //注册组件
        registComponent();
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.ivCMPic:// 显示播放界面
                showRLPlayMusic();
                break;
            case R.id.ivBack:// 隐藏播放界面
                hideRLPlayMusic();
                break;
            case R.id.ivPMPrevious:// 播放上一首
                previous();
                break;
            case R.id.ivPMPlayOrPause:// 播放或暂停
                musicBinder.playOrPause();
                if (musicBinder.isPlaying) {
                    // 开始播放，显示暂停
                    ivPMPlayOrPause.setImageResource(R.mipmap.appwidget_icon_pause_normal);
                    animator.resume();//恢复动画
                } else {
                    // 已经暂停，显示播放
                    ivPMPlayOrPause.setImageResource(R.mipmap.appwidget_icon_play_normal);
                    animator.pause();//暂停动画
                }
                break;
            case R.id.ivPMNext:// 播放下一首
                next();
                break;
            case R.id.btnSearch:  //显示搜索界面
                btnSearch.setEnabled(false);
                rlSearchMusic.setVisibility(View.VISIBLE);
                TranslateAnimation animation = new TranslateAnimation(0, 0, -rlSearchMusic.getHeight(), 0);
                animation.setDuration(250);
                rlSearchMusic.startAnimation(animation);
                break;
            case R.id.btnSearchMusic: //搜索音乐
                searchMusic();
                break;
            case R.id.btnCancel: //收起搜索界面
                btnSearch.setEnabled(true);
                rlSearchMusic.setVisibility(View.INVISIBLE);
                animation = new TranslateAnimation(0, 0, 0, -rlSearchMusic.getHeight());
                animation.setDuration(250);
                rlSearchMusic.startAnimation(animation);
                break;
            case R.id.ivDownload:// 下载歌曲
                downloadMusic();
                break;
            case R.id.ivPlayModel:
                j = i % 3;
                ivPlayModel.setImageResource(modeRes[j]);
                switch (j) {
                    case 0:// 顺序
                        app.setPlayMode(GlobalConsts.PLAYMODE_NORMAL);
                        Toast.makeText(MainActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:// 随机
                        app.setPlayMode(GlobalConsts.PLAYMODE_SHUFFLE);
                        Toast.makeText(MainActivity.this, "随机播放", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:// 单曲循环
                        app.setPlayMode(GlobalConsts.PLAYMODE_REPEAT_CURRENT);
                        Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                        break;
                }
                i++;
                break;
        }
    }

    private void downloadMusic() {
        final Music music = app.getCurrentMusic();
        final List<SongUrl> songUrls = music.getSongUrls();
        //把集合中的file_size数据 解析为字符串数组
        String[] data = new String[songUrls.size()];
        for (int i = 0; i < songUrls.size(); i++) {
            SongUrl songUrl = songUrls.get(i);
            double fileSize = 100.0 * Integer.parseInt(songUrl.getFile_size()) / 1024 / 1024;
            //fileSize :  123.234234234
            data[i] = Math.floor(fileSize) / 100 + "M";
        }
        //弹出AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择版本")
                .setItems(data, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SongUrl songUrl = songUrls.get(which);
                        //启动Service执行下载操作
                        Intent intent = new Intent(MainActivity.this, DownloadService.class);
                        intent.putExtra("url", songUrl.getShow_link());
                        intent.putExtra("title", music.getTitle());
                        intent.putExtra("bit", songUrl.getFile_bitrate());
                        MainActivity.this.startService(intent);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void searchMusic() {
        //1.  获取关键字
        String keyWord = etKeyword.getText().toString();
        if ("".equals(keyWord)) {
            Toast.makeText(MainActivity.this, "请输入关键字", Toast.LENGTH_SHORT).show();
            return;
        }
        //2.  根据关键字  查询相关结果  http  Model
        musicModel.searchMusic(keyWord, new MusicModel.Callback() {
            @Override
            public void onMusicListLoaded(List<Music> musics) {
                //3.  List<Music>
                searchMusicList = musics;
                //4.  更新Adapter
                searchMusicAdapter = new SearchResultAdapter(MainActivity.this, searchMusicList);
                lvSearchResult.setAdapter(searchMusicAdapter);
            }
        });
    }

    private void previous() {
        textViews[0].setText("");
        textViews[1].setText("");
        animator.end();// 结束动画
        switch (app.getPlayMode()) {
            case GlobalConsts.PLAYMODE_NORMAL:
                app.previousMusic();
                break;
            case GlobalConsts.PLAYMODE_SHUFFLE:
                app.shuffle();
                break;
            case GlobalConsts.PLAYMODE_REPEAT_CURRENT:
                break;
        }

        final Music music = app.getCurrentMusic();
        musicModel.getSongInfoBySongId(music.getSong_id(), new MusicModel.SongInfoCallback() {
            @Override
            public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
                if (urls == null || info == null) {
                    Toast.makeText(MainActivity.this, "切歌失败！！！", Toast.LENGTH_SHORT).show();
                    return;
                }
                music.setSongInfo(info);
                music.setSongUrls(urls);
                musicBinder.playMusic(urls.get(0).getShow_link());
                // 显示暂停按钮
                //                ivPMPlayOrPause.setImageResource(R.mipmap.appwidget_icon_pause_normal);
            }
        });
    }

    private void next() {
        textViews[0].setText("");
        textViews[1].setText("");
        animator.end();// 结束动画
        switch (app.getPlayMode()) {
            case GlobalConsts.PLAYMODE_NORMAL:
                app.nextMusic();
                break;
            case GlobalConsts.PLAYMODE_SHUFFLE:
                app.shuffle();
                break;
            case GlobalConsts.PLAYMODE_REPEAT_CURRENT:
                break;
        }

        final Music music1 = app.getCurrentMusic();
        musicModel.getSongInfoBySongId(music1.getSong_id(), new MusicModel.SongInfoCallback() {
            @Override
            public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
                if (urls == null || info == null) {
                    Toast.makeText(MainActivity.this, "切歌失败！！！", Toast.LENGTH_SHORT).show();
                    return;
                }
                music1.setSongInfo(info);
                music1.setSongUrls(urls);
                musicBinder.playMusic(urls.get(0).getShow_link());
                // 显示暂停按钮
                //                ivPMPlayOrPause.setImageResource(R.mipmap.appwidget_icon_pause_normal);
            }
        });
    }

    private void showRLPlayMusic() {
        btnSearch.setVisibility(View.GONE);
        RotateAnimation animation = new RotateAnimation(90, 0, rlPlayMusic.getWidth(), rlPlayMusic.getHeight());
        animation.setDuration(200);
        //        animation.setInterpolator(new LinearInterpolator());
        //        animation.setRepeatCount(1);
        rlPlayMusic.setAnimation(animation);
        rlPlayMusic.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (rlPlayMusic.getVisibility() == View.VISIBLE) {
            // 如果可见，则隐藏
            hideRLPlayMusic();
        } else {
            super.onBackPressed();
        }

    }

    private void hideRLPlayMusic() {
        btnSearch.setVisibility(View.VISIBLE);
        RotateAnimation animation = new RotateAnimation(0, 90, rlPlayMusic.getWidth(), rlPlayMusic.getHeight());
        animation.setDuration(200);
        rlPlayMusic.setAnimation(animation);
        rlPlayMusic.setVisibility(View.INVISIBLE);
    }

    /**
     * 注册各种组件
     */
    private void registComponent() {
        //注册广播接收器
        receiver = new UpdateMusicInfoReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConsts.ACTION_START_PLAY);
        intentFilter.addAction(GlobalConsts.ACTION_UPDATE_PROGRESS);
        intentFilter.addAction(GlobalConsts.ACTION_NEXT);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        //解除与Service的绑定
        unbindService(connection);
        // 解除注册广播接收器
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    /**
     * 绑定Service
     */
    private void bindPlayMusicService() {
        Intent intent = new Intent(this, PlayMusicService.class);
        connection = new ServiceConnection() {
            @Override//当与service绑定成功后 执行
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicBinder = (PlayMusicService.MusicBinder) service;// 将把binder对象保存到activity的成员变量
                //绑定成功后  把musicBinder 给Fragment
                NewMusicFragment newMusicFragment = (NewMusicFragment) fragments.get(0);
                HotMusicFragment hotMusicFragment = (HotMusicFragment) fragments.get(1);
                newMusicFragment.setMusicBinder(musicBinder);
                hotMusicFragment.setMusicBinder(musicBinder);
            }

            @Override//异常断开时 执行
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    /**
     * 控件初始化
     */
    private void setViews() {
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        rbNew = (RadioButton) findViewById(R.id.radioNew);
        rbHot = (RadioButton) findViewById(R.id.radioHot);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        ivCMPic = (ImageView) findViewById(R.id.ivCMPic);
        tvCMTitle = (TextView) findViewById(R.id.tvCMTitle);
        rlPlayMusic = (RelativeLayout) findViewById(R.id.rlPlayMusic);
        tvPMTitle = (TextView) findViewById(R.id.tvPMTitle);
        tvPMSinger = (TextView) findViewById(R.id.tvPMSinger);
        ivPMAlbum = (ImageView) findViewById(R.id.ivPMAlbum);
        tvPMLrc0 = (TextView) findViewById(R.id.tvPMLrc0);
        tvPMLrc1 = (TextView) findViewById(R.id.tvPMLrc1);
        tvPMCurrentTime = (TextView) findViewById(R.id.tvPMCurrentTime);
        tvPMTotalTime = (TextView) findViewById(R.id.tvPMTotalTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        ivPlayModel = (ImageView) findViewById(R.id.ivPlayModel);
        ivPMPrevious = (ImageView) findViewById(R.id.ivPMPrevious);
        ivPMPlayOrPause = (ImageView) findViewById(R.id.ivPMPlayOrPause);
        ivPMNext = (ImageView) findViewById(R.id.ivPMNext);
        ivPlayList = (ImageView) findViewById(R.id.ivPlayList);
        ivPMBackground = (ImageView) findViewById(R.id.ivPMBackground);
        textViews[0] = tvPMLrc0;
        textViews[1] = tvPMLrc1;
        rlSearchMusic = (RelativeLayout) findViewById(R.id.rlSearchMusic);
        lvSearchResult = (ListView) findViewById(R.id.lvSearchResult);
        etKeyword = (EditText) findViewById(R.id.etKeyword);
        btnSearch = (Button) findViewById(R.id.btnSearch);
    }

    /**
     * 给viewPager设置适配器
     */
    private void setViewPagerAdapter() {
        //构建Fragment数据源
        fragments = new ArrayList<>();
        //向fragments集合中添加Fragment
        fragments.add(new NewMusicFragment());
        fragments.add(new HotMusicFragment());
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
    }

    private void setListeners() {
        // 给搜索列表设置监听
        lvSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 将当前歌曲集合存入app
                app.setMusicPlayList(searchMusicList);
                app.setPosition(position);
                final Music music = searchMusicList.get(position);
                musicModel.getSongInfoBySongId(music.getSong_id(), new MusicModel.SongInfoCallback() {
                    @Override
                    public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
                        if (urls == null || info == null) {
                            Toast.makeText(MainActivity.this, "无法加载歌曲，请检查网络！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        music.setSongUrls(urls);
                        music.setSongInfo(info);
                        musicBinder.playMusic(urls.get(0).getShow_link());
                    }
                });
            }
        });
        // 给rlPlayMusic设置监听
        rlPlayMusic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // onTouch方法返回值是true（事件被消费）时，则onTouchEvent方法将不会被执行；
                // 假如onTouch方法返回false，会接着触发onTouchEvent，反之onTouchEvent方法不会被调用。
                return true;
            }

        });
        //滑动viewpager 控制 导航栏
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        rbNew.setChecked(true);
                        //                        radioGroup.check(R.id.radioNew);
                        break;
                    case 1:
                        rbHot.setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        //点击导航 控制viewpager
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioNew:
                        viewPager.setCurrentItem(0);// Alt+Shift+上下
                        break;
                    case R.id.radioHot:
                        viewPager.setCurrentItem(1);
                        break;
                }

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicBinder.seekTo(seekBar.getProgress());
            }
        });


    }

    /**
     * 编写viewPager的Adapter
     */
    class MyPagerAdapter extends FragmentPagerAdapter {


        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

    }

    class UpdateMusicInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GlobalConsts.ACTION_NEXT)) {
                // 播放下一首
                next();
            }
            // 更新进度
            if (action.equals(GlobalConsts.ACTION_UPDATE_PROGRESS)) {
                setUpdateProgress(intent);
            }
            //音乐已经开始播放
            if (action.equals(GlobalConsts.ACTION_START_PLAY)) {
                musicBinder.isPlaying = true;
                setStartPlay(context);
            }
        }
    }

    /**
     * 设置开始播放的界面
     *
     * @param context
     */
    private void setStartPlay(Context context) {
        if (animator!=null) {
            animator.end();//TODO 测试动画
        }
        // 显示暂停按钮
        ivPMPlayOrPause.setImageResource(R.mipmap.appwidget_icon_pause_normal);
        //获取到当前正在播放的music对象
        app = (MusicApplication) getApplication();
        List<Music> musicPlayList = app.getMusicPlayList();
        int position = app.getPosition();
        Music music = musicPlayList.get(position);
        //更新tvPMTitle
        tvPMTitle.setText(music.getTitle());
        //更新tvPMSinger
        if (music.getSongInfo() == null) {
            return;
        }
        //        tvPMSinger.setText(music.getArtist_name());
        tvPMSinger.setText(music.getSongInfo().getAuthor());
        //更新专辑图片ivPMAlbum
//        String albumPath = music.getSongInfo().getAlbum_500_500();
        String albumPath = music.getSongInfo().getPic_radio();
        LogUtil.i("TAG","getAlbum_500_500()="+albumPath);
        // 通过输入流直接加载
        BitmapUtils.loadBitmap(context, albumPath, 0, 0, new BitmapUtils.BitmapCallback() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap) {
                if (bitmap != null) {
                    ivPMAlbum.setImageBitmap(bitmap);
                } else {
                    ivPMAlbum.setImageResource(R.mipmap.default_music_pic);
                }
            }
        });
        //更新背景图片ivPMBackground
//        String backgroundPath = music.getSongInfo().getArtist_480_800();
        String backgroundPath = music.getSongInfo().getArtist_500_500();
        LogUtil.i("TAG","getArtist_480_800()="+backgroundPath);
        if ("".equals(backgroundPath)) {
            backgroundPath = music.getSongInfo().getArtist_640_1136();
        }
        if ("".equals(backgroundPath)) {
            backgroundPath = music.getSongInfo().getArtist_480_800();
        }
        // 显示背景图片
        BitmapUtils.loadBitmap(context, backgroundPath, 100, 100, new BitmapUtils.BitmapCallback() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap) {
                if (bitmap != null) {
                    setImageViewColor(bitmap);//设置播放控制按钮的颜色
                    //图片已经加载成功   需要模糊化处理
                    BitmapUtils.loadBluredBitmap(bitmap, 10, new BitmapUtils.BitmapCallback() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap) {
                            //图片已经模糊化处理完成
                            ivPMBackground.setImageBitmap(bitmap);
                        }
                    });
                } else {
                    ivPMBackground.setImageResource(R.mipmap.default_music_background);
                }
            }
        });
        //更新CircleImageView   TextView
        tvCMTitle.setText(music.getTitle());
        BitmapUtils.loadBitmap(context, music.getSongInfo().getPic_small(), 50, 50, new BitmapUtils.BitmapCallback() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap) {
                if (bitmap != null) {
                    ivCMPic.setImageBitmap(bitmap);
                    //启动旋转动画,补间动画
                    //                    animation = new RotateAnimation(0, 360, ivCMPic.getWidth() / 2, ivCMPic.getHeight() / 2);
                    //                    animation.setDuration(20000);
                    //                    //设置插值器 匀速旋转
                    //                    animation.setInterpolator(new LinearInterpolator());
                    //                    // 设置重复计数//一直转
                    //                    animation.setRepeatCount(Animation.INFINITE);
                    //                    ivCMPic.setAnimation(animation);
                    //                            Toast.makeText(MainActivity.this, "setAnimation", Toast.LENGTH_SHORT).show();
                    // ObjectAnimator属性动画
                    animator = null;
                    animator = ObjectAnimator.ofFloat(ivCMPic, "rotation", 0f, 360.0f);
                    animator.setDuration(20000);
                    animator.setInterpolator(new LinearInterpolator());//不停顿
                    animator.setRepeatCount(ValueAnimator.INFINITE);//设置动画重复次数
                    animator.setRepeatMode(ValueAnimator.RESTART);//动画重复模式
                    animator.start();// 开始动画

                } else {
                    ivCMPic.setImageResource(R.mipmap.ic_launcher);
                }
            }
        });
        // 下载歌词
        musicModel.downloadLrc(MainActivity.this, music.getSongInfo().getLrclink(), new MusicModel.LrcCallback() {
            @Override
            public void onLrcLoaded(List<LrcLine> lrcLines) {
                app.setLrcLines(lrcLines);// 将歌词存入app
            }
        });
    }

    private void setUpdateProgress(Intent intent) {
        //更新音乐的播放进度  seekBar textView
        int currentPosition = intent.getIntExtra(GlobalConsts.MUSIC_CURRENT_POSITION, 0);
        int duration = intent.getIntExtra(GlobalConsts.MUSIC_DURATION, 0);
        seekBar.setMax(duration);
        seekBar.setProgress(currentPosition);
        //更新两个textView
        tvPMCurrentTime.setText(sdf.format(new Date(currentPosition)));
        tvPMTotalTime.setText(sdf.format(new Date(duration)));
        // 显示歌词
        List<LrcLine> lrcLines = app.getLrcLines();
        if (lrcLines == null) {
            return;//歌词还没下载，跳出
        }

        for (int i = 0; i < lrcLines.size(); i++) {
            LrcLine lrcLine = lrcLines.get(i);
            // 与当前时间进行匹配
            if (lrcLine.equalsTime(currentPosition + 200)) {
                // 如果第一行的颜色是黄色
                if (textViews[0].getCurrentTextColor() == Color.YELLOW) {
                    textViews[0].setTextColor(Color.WHITE);// 一白
                    textViews[1].setTextColor(Color.YELLOW);// 二黄
                    if (i + 1 < lrcLines.size()) {
                        // 第一行提前显示下一句歌词
                        textViews[0].setText(lrcLines.get(i + 1).getContent());
                    }
                } else if (textViews[0].getCurrentTextColor() == Color.WHITE) {// 如果第一行的颜色是白色
                    textViews[0].setTextColor(Color.YELLOW);// 一黄
                    textViews[1].setTextColor(Color.WHITE);// 二白
                    if (i + 1 < lrcLines.size()) {
                        // 第二行提前显示下一句歌词
                        textViews[1].setText(lrcLines.get(i + 1).getContent());
                    }
                }
            }
        }

    }

    /**
     * 设置播放控制按钮的颜色
     *
     * @param bitmap
     */
    private void setImageViewColor(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //                Palette.Swatch swatch = palette.getMutedSwatch();
                Palette.Swatch swatch = palette.getLightVibrantSwatch();
                if (swatch != null) {
                    ivPMPlayOrPause.setColorFilter(swatch.getRgb());
                    ivPMNext.setColorFilter(swatch.getRgb());
                    ivPMPrevious.setColorFilter(swatch.getRgb());
                }
                Palette.Swatch swatch1 = palette.getDarkVibrantSwatch();
                if (swatch1 != null) {
                    tvPMLrc1.setTextColor(swatch1.getRgb());
                }
            }

        });

    }
}
