package com.bobo.musicplayer.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bobo.musicplayer.R;

import java.util.ArrayList;
import java.util.List;

import fragment.HotMusicFragment;
import fragment.NewMusicFragment;

public class MainActivity extends FragmentActivity {
    private RadioGroup radioGroup;
    private RadioButton rbNew;
    private RadioButton rbHot;
    private ViewPager viewPager;
    private List<Fragment> fragments;
    private PagerAdapter pagerAdapter;

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
    }
    /**
     * 控件初始化
     */
    private void setViews() {
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        rbNew = (RadioButton) findViewById(R.id.radioNew);
        rbHot = (RadioButton) findViewById(R.id.radioHot);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

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
}
