package com.bobo.musicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bobo.musicplayer.R;

import java.util.ArrayList;
import java.util.List;

import app.MusicApplication;
import entity.Music;
import fragment.HotMusicFragment;
import fragment.NewMusicFragment;
import service.PlayMusicService;
import util.BitmapUtils;
import util.GlobalConsts;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
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

    /**
     * 注册各种组件
     */
    private void registComponent() {
        //注册广播接收器
        receiver = new UpdateMusicInfoReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConsts.ACTION_START_PLAY);
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
            //音乐已经开始播放
            String action = intent.getAction();
            if (action.equals(GlobalConsts.ACTION_START_PLAY)) {
                //获取到当前正在播放的music对象
                MusicApplication app = (MusicApplication) getApplication();
                List<Music> musicPlayList = app.getMusicPlayList();
                int position = app.getPosition();
                Music music = musicPlayList.get(position);
                //更新CircleImageView   TextView
                tvCMTitle.setText(music.getTitle());
                BitmapUtils.loadBitmap(context, music.getPic_small(), new BitmapUtils.BitmapCallback() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap) {
                        if (bitmap != null) {
                            ivCMPic.setImageBitmap(bitmap);
                            //启动旋转动画
                            RotateAnimation animation = new RotateAnimation(0, 360, ivCMPic.getWidth() / 2, ivCMPic.getHeight() / 2);
                            animation.setDuration(20000);
                            //设置插值器 匀速旋转
                            animation.setInterpolator(new LinearInterpolator());
                            // 设置重复计数//一直转
                            animation.setRepeatCount(Animation.INFINITE);
                            ivCMPic.setAnimation(animation);
                            Toast.makeText(MainActivity.this, "setAnimation", Toast.LENGTH_SHORT).show();
                        } else {
                            ivCMPic.setImageResource(R.mipmap.ic_launcher);
                        }
                    }
                });
            }
        }
    }
}
