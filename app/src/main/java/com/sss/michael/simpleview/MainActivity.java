package com.sss.michael.simpleview;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.sss.michael.simpleview.bean.BottomBarModel;
import com.sss.michael.simpleview.bean.GraffitiActivity;
import com.sss.michael.simpleview.bean.SimpleCandleViewTxtBean;
import com.sss.michael.simpleview.bean.SimpleLinearChart2TxtBean;
import com.sss.michael.simpleview.bean.SimpleMultipleColumnTxtBean;
import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.view.BannerViewPager;
import com.sss.michael.simpleview.view.BottomNavigationBar;
import com.sss.michael.simpleview.view.SimpleCandleView;
import com.sss.michael.simpleview.view.SimpleDoubleSeekBar;
import com.sss.michael.simpleview.view.SimpleDoubleSeekBar2;
import com.sss.michael.simpleview.view.SimpleHalfPieChart;
import com.sss.michael.simpleview.view.SimpleHalfRingView;
import com.sss.michael.simpleview.view.SimpleLinearChart;
import com.sss.michael.simpleview.view.SimpleLinearChart2;
import com.sss.michael.simpleview.view.SimpleMultipleColumnView;
import com.sss.michael.simpleview.view.SimpleProgressBar;
import com.sss.michael.simpleview.view.SimpleRotatingView;
import com.sss.michael.simpleview.view.SimpleRoundTabView;
import com.sss.michael.simpleview.view.SimpleSlideBesselView;
import com.sss.michael.simpleview.view.SimpleSpiderView;
import com.sss.michael.simpleview.view.SimpleWrapOffsetWidthView;
import com.sss.michael.simpleview.view.TransitionImageView;
import com.sss.michael.simpleview.view.toutiaoanimation.ArticleRl;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

@SuppressWarnings("all")
public class MainActivity extends AppCompatActivity {
    private SimpleHalfPieChart simpleHalfPieChart;
    private SimpleLinearChart simpleLinearChart;
    private SimpleSpiderView simpleSpiderView;
    private SimpleProgressBar simpleProgressBar;
    private SimpleRotatingView simpleRotatingView;
    private SimpleDoubleSeekBar simpleDoubleSeekBar;
    private SimpleDoubleSeekBar2 simpleDoubleSeekBar2;
    private SimpleHalfRingView simpleHalfRingView;
    private SimpleWrapOffsetWidthView simpleWrapOffsetWidthView;
    private SimpleSlideBesselView simpleSlideBesselView;
    private NestedScrollView nestedScrollView;
    private SeekBar simpleHalfRingViewSeekBar;
    private BottomNavigationBar bottomNavigationBar;
    private SimpleCandleView simpleCandleView;
    private SimpleLinearChart2 simpleLinearChart2;
    private SimpleMultipleColumnView simpleMultipleColumnView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        simpleSlideBesselView = findViewById(R.id.simpleSlideBesselView);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nestedScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    simpleSlideBesselView.setOffset(scrollY);
                }
            });
        }
        simpleHalfPieChart = findViewById(R.id.simpleHalfPieChart);
        simpleHalfPieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SimpleHalfPieChart.SimpleHalfPieChartBean> list = new ArrayList<>();
                list.add(new SimpleHalfPieChart.SimpleHalfPieChartBean("医药", "19%", 0.19f, Color.parseColor("#7A95FF")));
                list.add(new SimpleHalfPieChart.SimpleHalfPieChartBean("艺术", "11%", 0.11f, Color.parseColor("#3A95FF")));
                list.add(new SimpleHalfPieChart.SimpleHalfPieChartBean("语言", "22%", 0.22f, Color.parseColor("#0A65FF")));
                list.add(new SimpleHalfPieChart.SimpleHalfPieChartBean("财经", "5%", 0.05f, Color.parseColor("#ff95FF")));
                list.add(new SimpleHalfPieChart.SimpleHalfPieChartBean("财经2", "5%", 0.05f, Color.parseColor("#ff95FF")));
                list.add(new SimpleHalfPieChart.SimpleHalfPieChartBean("理工", "13%", 0.13f, Color.parseColor("#09ff99")));
                list.add(new SimpleHalfPieChart.SimpleHalfPieChartBean("农林", "15%", 0.15f, Color.parseColor("#76582F")));
                list.add(new SimpleHalfPieChart.SimpleHalfPieChartBean("军事", "5%", 0.05f, Color.parseColor("#8541FF")));
                list.add(new SimpleHalfPieChart.SimpleHalfPieChartBean("军事2", "5%", 0.05f, Color.parseColor("#8541FF")));
                simpleHalfPieChart.setData("倾向", list, true);
            }
        });
        simpleLinearChart = findViewById(R.id.simpleLinearChart);
        simpleLinearChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SimpleLinearChart.SimpleLinearChartBean> list = new ArrayList<>();
                list.add(new SimpleLinearChart.SimpleLinearChartBean(524, "1/1", "524"));
                list.add(new SimpleLinearChart.SimpleLinearChartBean(532, "1/2", "532"));
                list.add(new SimpleLinearChart.SimpleLinearChartBean(504, "1/3", "504"));
                list.add(new SimpleLinearChart.SimpleLinearChartBean(517, "1/4", "517"));
                list.add(new SimpleLinearChart.SimpleLinearChartBean(535, "1/5", "535"));
                list.add(new SimpleLinearChart.SimpleLinearChartBean(549, "1/6", "549"));
                simpleLinearChart.setData(list, 504, 549, true);
            }
        });
        simpleSpiderView = findViewById(R.id.simpleSpiderView);
        simpleSpiderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SimpleSpiderView.SimpleSpiderViewBean> data = new ArrayList<>();
                data.add(new SimpleSpiderView.SimpleSpiderViewBean("B", "目标规划", 0.6f));
                data.add(new SimpleSpiderView.SimpleSpiderViewBean("A+", "自我认知", 0.9f));
                data.add(new SimpleSpiderView.SimpleSpiderViewBean("A", "学习状态", 0.67f));
                data.add(new SimpleSpiderView.SimpleSpiderViewBean("A+", "心理健康", 1.0f));
                data.add(new SimpleSpiderView.SimpleSpiderViewBean("A", "生涯学习", 0.8f));
                simpleSpiderView.setData(data, true);
            }
        });
        simpleProgressBar = findViewById(R.id.simpleProgressBar);
        simpleProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        simpleProgressBar.setText((int) animation.getAnimatedValue() + "%");
                        simpleProgressBar.setCurrentProgress((int) animation.getAnimatedValue());
                    }
                });
                valueAnimator.setRepeatMode(ValueAnimator.RESTART);
                valueAnimator.setDuration(1000);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.start();
            }
        });
        simpleRotatingView = findViewById(R.id.simpleRotatingView);
        simpleRotatingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleRotatingView.start();
            }
        });
        simpleDoubleSeekBar = findViewById(R.id.simpleDoubleSeekBar);
        simpleDoubleSeekBar.setData(false, 100, 300, 50, 300);
        simpleDoubleSeekBar.setOnSimpleDoubleSeekBarCallBack(new SimpleDoubleSeekBar.OnSimpleDoubleSeekBarCallBack() {
            @Override
            public void onValueChanged(int currentMinValue, int currentMaxValue, float currentMinPosition, float currentMaxPosition) {
//                Log.e("SSS", "CMinV:" + currentMinValue + ",CMaxV:" + currentMaxValue + ",MinP:" + currentMinPosition + ",MaxP:" + currentMaxPosition);
            }
        });

        simpleDoubleSeekBar2 = findViewById(R.id.simpleDoubleSeekBar2);
        simpleDoubleSeekBar2.setData(true, 50, 30, 50, 300);
        simpleDoubleSeekBar2.setOnSimpleDoubleSeekBarCallBack(new SimpleDoubleSeekBar2.OnSimpleDoubleSeekBar2CallBack() {
            @Override
            public void onValueChanged(int currentMinValue, int currentMaxValue, float currentMinPosition, float currentMaxPosition) {
//                Log.e("SSS", "CMinV:" + currentMinValue + ",CMaxV:" + currentMaxValue + ",MinP:" + currentMinPosition + ",MaxP:" + currentMaxPosition);
            }
        });

        simpleHalfRingView = findViewById(R.id.simple_half_ring_view);
        simpleHalfRingViewSeekBar = findViewById(R.id.simple_half_ring_view_seek_bar);
        simpleHalfRingViewSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    simpleHalfRingView.setData(888, progress * 1.0f / 100, false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        simpleHalfRingView.setOnSimpleHalfRingViewCallBack(new SimpleHalfRingView.OnSimpleHalfRingViewCallBack() {
            @Override
            public void onProgressChanged(float percent, int total, boolean fromAnimation) {
                simpleHalfRingViewSeekBar.setProgress((int) (percent * 100));
            }
        });
        simpleHalfRingView.setData(888, simpleHalfRingViewSeekBar.getProgress() * 1.0f / 100, true);


        simpleWrapOffsetWidthView = findViewById(R.id.simpleWrapOffsetWidthView);
        findViewById(R.id.simpleDeformationBackgroundView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() == null) {
                    simpleWrapOffsetWidthView.start(SimpleWrapOffsetWidthView.STATE_COLLAPSED);
                    v.setTag("");
                } else {
                    simpleWrapOffsetWidthView.start(SimpleWrapOffsetWidthView.STATE_EXPANDED);
                    v.setTag(null);
                }
            }
        });


        final TransitionImageView last = findViewById(R.id.lastBuffer);
        final TransitionImageView next = findViewById(R.id.nextBuffer);
        final TransitionImageView current = findViewById(R.id.currentBuffer);
        final TransitionImageView bg = findViewById(R.id.bg);
        final BannerViewPager<String> myViewPager = findViewById(R.id.myViewPager);
        final List<String> list = new ArrayList<>();
        list.add("https://alifei05.cfp.cn/creative/vcg/veer/1600water/veer-161885124.jpg");
        list.add("https://alifei04.cfp.cn/creative/vcg/veer/1600water/veer-303764513.jpg");
        list.add("https://tenfei04.cfp.cn/creative/vcg/veer/1600water/veer-142190838.jpg");
        list.add("https://alifei05.cfp.cn/creative/vcg/800/version23/VCG41175510742.jpg");

        myViewPager.post(new Runnable() {
            @Override
            public void run() {
                myViewPager.setOnMyViewPagerCallBack(new BannerViewPager.OnMyViewPagerCallBack<String>() {


                    @Override
                    public void onImageChange(boolean byUser, BannerViewPager.Direction direction, List<String> models, ImageView left, ImageView middle, ImageView right, int[] position) {
                        int lastPosition = position[0];
                        int currentPosition = position[1];
                        int nextPosition = position[2];

                        if (direction == BannerViewPager.Direction.RIGHT_TO_LEFT) {
                            bg.setTransitionAlpha(models.get(currentPosition), models.get(nextPosition));
                        } else if (direction == BannerViewPager.Direction.LEFT_TO_RIGHT) {
                            bg.setTransitionAlpha(models.get(currentPosition), models.get(lastPosition));
                        }
                    }

                    @Override
                    public void onTouchScroll(BannerViewPager.Direction direction, float offsetPercent, List<String> models, ImageView left, ImageView middle, ImageView right, int[] position) {
                    }

                    @Override
                    public void previewBannerImage(BannerViewPager.Direction direction, List<String> models, ImageView leftBanner, final ImageView middleBanner, ImageView rightBanner, int[] position) {
                        int lastPosition = position[0];
                        int currentPosition = position[1];
                        int nextPosition = position[2];
                        last.setImgUrl(models.get(currentPosition), models.get(lastPosition), leftBanner, myViewPager.getLeft(), myViewPager.getTop(), myViewPager.getWidth(), myViewPager.getHeight());
                        current.setImgUrl(models.get(currentPosition), models.get(currentPosition), middleBanner, myViewPager.getLeft(), myViewPager.getTop(), myViewPager.getWidth(), myViewPager.getHeight());
                        next.setImgUrl(models.get(currentPosition), models.get(nextPosition), rightBanner, myViewPager.getLeft(), myViewPager.getTop(), myViewPager.getWidth(), myViewPager.getHeight());

                        if (direction == BannerViewPager.Direction.NORMAL) {
                            bg.setImgUrl(models.get(currentPosition), models.get(currentPosition), null, myViewPager.getLeft(), myViewPager.getTop(), myViewPager.getWidth(), myViewPager.getHeight());
                        }


                    }
                });
                myViewPager.setData(list);
            }
        });

        final Button button = findViewById(R.id.jinritoutiao_animation);
        final ArticleRl articleRl = findViewById(R.id.article_view);

        button.setOnTouchListener(new View.OnTouchListener() {
            int x;
            int y;
            long lastDownTime;
            Runnable mLongPressed = new Runnable() {
                @Override
                public void run() {
                    articleRl.setVisibility(View.VISIBLE);
                    articleRl.setThumb(x, y);
                    articleRl.postDelayed(this, 50);
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lastDownTime = System.currentTimeMillis();
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    articleRl.postDelayed(mLongPressed, 100);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (System.currentTimeMillis() - lastDownTime < 100) {//判断为单击事件
                        articleRl.setVisibility(View.VISIBLE);
                        articleRl.setThumb(x, y);
                        articleRl.removeCallbacks(mLongPressed);
                    } else {//判断为长按事件后松开
                        articleRl.removeCallbacks(mLongPressed);
                    }
                }
                return true;
            }
        });


        bottomNavigationBar = findViewById(R.id.navigation_bar);
        bottomNavigationBar.setOnBottomNavigationBarCallBack(new BottomNavigationBar.OnBottomNavigationBarCallBack() {
            @Override
            public void onBottomNavigationBarItemClick(int position, boolean bigImage, BottomNavigationBar.Extra extra) {
                Toast.makeText(MainActivity.this, position + "---" + bigImage, Toast.LENGTH_SHORT).show();
            }
        });


        BottomBarModel bottomBarModel = null;
        bottomBarModel = bottomNavigationBar.initBottomBarConfig(bottomBarModel);
        ConstraintLayout.LayoutParams layoutParamsNavigation = (ConstraintLayout.LayoutParams) bottomNavigationBar.getLayoutParams();
        if (layoutParamsNavigation != null && bottomBarModel.getHeight() > 0) {
            layoutParamsNavigation.height = DensityUtil.dp2px(bottomBarModel.getHeight());
            bottomNavigationBar.setLayoutParams(layoutParamsNavigation);
        }
        View navigationHolderView = findViewById(R.id.navigation_holder_view);
        ConstraintLayout.LayoutParams layoutParamsHolder = (ConstraintLayout.LayoutParams) navigationHolderView.getLayoutParams();
        if (layoutParamsHolder != null) {
            layoutParamsHolder.topMargin = DensityUtil.dp2px(bottomBarModel.getReserveAreaHeight());
            navigationHolderView.setLayoutParams(layoutParamsHolder);
        }
        List<BottomNavigationBar.BottomNavigationBarItem> items = new ArrayList<>();
        for (int i = 0; i < bottomBarModel.getTabs().size(); i++) {
            BottomNavigationBar.Builder builder = new BottomNavigationBar.Builder(bottomNavigationBar);
            builder.setBetweenImageAndText(bottomBarModel.getTabs().get(i).getBetweenImageAndText());
            builder.setBigImage(bottomBarModel.getTabs().get(i).isBigImage());
            builder.setChecked(bottomBarModel.getTabs().get(i).isIsChecked());
            builder.setCheckedUrl(bottomBarModel.getTabs().get(i).getCheckedUrl());
            builder.setUnCheckedUrl(bottomBarModel.getTabs().get(i).getUnCheckedUrl());
            builder.setFragmentIndex(bottomBarModel.getTabs().get(i).getFragmentIndex());
            builder.setImageHeight(bottomBarModel.getTabs().get(i).getImageHeight());
            builder.setImageWidth(bottomBarModel.getTabs().get(i).getImageWidth());
            builder.setLabel(bottomBarModel.getTabs().get(i).getLabel());
            builder.setPageUrl(bottomBarModel.getTabs().get(i).getPageUrl());
            builder.setTextOffsetY(bottomBarModel.getTabs().get(i).getTextOffsetY());
            builder.setTextSize(bottomBarModel.getTabs().get(i).getTextSize());
            builder.setWeight(bottomBarModel.getTabs().get(i).getWeight());
            if (bottomBarModel.isCornerMarkByConfig()){
                builder.setCornerMark(bottomBarModel.getTabs().get(i).getCornerMark());
            }
            builder.setCornerMarkTextSize(bottomBarModel.getTabs().get(i).getCornerMarkTextSize());
            builder.setCornerMarkPaddingVertical(bottomBarModel.getTabs().get(i).getCornerMarkPaddingVertical());
            builder.setCornerMarkPaddingHorizontal(bottomBarModel.getTabs().get(i).getCornerMarkPaddingHorizontal());
            try {
                builder.setCheckTextColor(Color.parseColor(bottomBarModel.getTabs().get(i).getCheckTextColor()));
                builder.setUnCheckTextColor(Color.parseColor(bottomBarModel.getTabs().get(i).getUnCheckTextColor()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            BottomNavigationBar.BottomNavigationBarItem navigationBarItem = builder.build(MainActivity.this);
            navigationBarItem.load(MainActivity.this, builder);
            items.add(navigationBarItem);
        }
        bottomNavigationBar.setItems(items);
        bottomNavigationBar.setCornerMarkByLabel("模块3","666");

        SimpleRoundTabView simpleRoundTabView = findViewById(R.id.simpleRoundTabView);
        List<SimpleRoundTabView.SimpleRoundTabBean> simpleRoundTabViewList = new ArrayList();
        for (int i = 0; i < 3; i++) {
            SimpleRoundTabView.SimpleRoundTabBean simpleRoundTabBean = new SimpleRoundTabView.SimpleRoundTabBean();
            simpleRoundTabBean.text = "标签No." + (i + 1);
            simpleRoundTabBean.checked = i == 0;
            simpleRoundTabViewList.add(simpleRoundTabBean);
        }
        simpleRoundTabView.setList(simpleRoundTabViewList);
        simpleRoundTabView.setOnSimpleRoundTabViewCallBack(new SimpleRoundTabView.OnSimpleRoundTabViewCallBack() {
            @Override
            public void onTabChecked(int fromPosition, int toPosition) {
                Toast.makeText(MainActivity.this, fromPosition + "***" + toPosition, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_graffiti).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GraffitiActivity.class));
            }
        });

        simpleCandleView = findViewById(R.id.simple_candle_view);
        //Y轴
        List<SimpleCandleView.OnSimpleCandleViewXyAxisTextRealization<SimpleCandleViewTxtBean>> yAxisData = new ArrayList<>();
        yAxisData.add(new SimpleCandleViewTxtBean("25k", 25000));
        yAxisData.add(new SimpleCandleViewTxtBean("20k", 20000));
        yAxisData.add(new SimpleCandleViewTxtBean("15k", 15000));
        yAxisData.add(new SimpleCandleViewTxtBean("10k", 10000));
        yAxisData.add(new SimpleCandleViewTxtBean("5k", 5000));
        yAxisData.add(new SimpleCandleViewTxtBean("1k", 1000));
        SimpleCandleView.SimpleCandleViewCoordinateAxisBean yAxisBean = new SimpleCandleView.SimpleCandleViewCoordinateAxisBean();
        yAxisBean.setCoordinateAxisTextData(yAxisData, true);
        //X轴
        List<SimpleCandleView.OnSimpleCandleViewXyAxisTextRealization<SimpleCandleViewTxtBean>> xAxisData = new ArrayList<>();
        xAxisData.add(new SimpleCandleViewTxtBean("4月", 25000));
        xAxisData.add(new SimpleCandleViewTxtBean("5月", 20000));
        xAxisData.add(new SimpleCandleViewTxtBean("6月", 15000));
        xAxisData.add(new SimpleCandleViewTxtBean("7月", 10000));
        xAxisData.add(new SimpleCandleViewTxtBean("8月", 5000));
        xAxisData.add(new SimpleCandleViewTxtBean("9月", 1000));
        xAxisData.add(new SimpleCandleViewTxtBean("10月", 1000));
        xAxisData.add(new SimpleCandleViewTxtBean("11月", 1000));
        xAxisData.add(new SimpleCandleViewTxtBean("12月", 1000));
        xAxisData.add(new SimpleCandleViewTxtBean("1月", 1000));
        xAxisData.add(new SimpleCandleViewTxtBean("2月", 1000));
        xAxisData.add(new SimpleCandleViewTxtBean("3月", 1000));
        SimpleCandleView.SimpleCandleViewCoordinateAxisBean xAxisBean = new SimpleCandleView.SimpleCandleViewCoordinateAxisBean();
        xAxisBean.setCoordinateAxisTextData(xAxisData, false);
        //内容轴
        List<SimpleCandleView.OnSimpleCandleViewXyAxisTextRealization<SimpleCandleViewTxtBean>> contentAxisData = new ArrayList<>();
        contentAxisData.add(new SimpleCandleViewTxtBean("4月", 20000, 15000, 4000, 2000));
        contentAxisData.add(new SimpleCandleViewTxtBean("5月", 19000, 14000, 6000, 4500));
        contentAxisData.add(new SimpleCandleViewTxtBean("6月", 22500, 18000, 6000, 4000));
        contentAxisData.add(new SimpleCandleViewTxtBean("7月", 20500, 16000, 10000, 5000));
        contentAxisData.add(new SimpleCandleViewTxtBean("8月", 22000, 18000, 12000, 10500));
        contentAxisData.add(new SimpleCandleViewTxtBean("9月", 22000, 18500, 7000, 6000));
        contentAxisData.add(new SimpleCandleViewTxtBean("10月", 19000, 16000, 13000, 11000));
        contentAxisData.add(new SimpleCandleViewTxtBean("11月", 18500, 15500, 7000, 6000));
        contentAxisData.add(new SimpleCandleViewTxtBean("12月", 20500, 18000, 8100, 7800));
        contentAxisData.add(new SimpleCandleViewTxtBean("1月", 21000, 16500, 4000, 4000));
        contentAxisData.add(new SimpleCandleViewTxtBean("2月", 23000, 18000, 13500, 13500));
        contentAxisData.add(new SimpleCandleViewTxtBean("3月", 17000, 13000, 4900, 1200));
        SimpleCandleView.SimpleCandleViewCoordinateAxisBean contentAxisBean = new SimpleCandleView.SimpleCandleViewCoordinateAxisBean();
        contentAxisBean.setCoordinateAxisTextData(contentAxisData, false);


        simpleCandleView.setData(yAxisBean, xAxisBean, contentAxisBean);


        simpleLinearChart2 = findViewById(R.id.simpleLinearChart2);
        //Y轴
        List<SimpleLinearChart2.OnSimpleLinearChart2XyAxisTextRealization<SimpleLinearChart2TxtBean>> simpleLinearChart2YAxisData = new ArrayList<>();
        simpleLinearChart2YAxisData.add(new SimpleLinearChart2TxtBean("5w", 50000));
        simpleLinearChart2YAxisData.add(new SimpleLinearChart2TxtBean("4w", 40000));
        simpleLinearChart2YAxisData.add(new SimpleLinearChart2TxtBean("3w", 30000));
        simpleLinearChart2YAxisData.add(new SimpleLinearChart2TxtBean("2w", 20000));
        simpleLinearChart2YAxisData.add(new SimpleLinearChart2TxtBean("1w", 10000));
        simpleLinearChart2YAxisData.add(new SimpleLinearChart2TxtBean("0", 0));
        SimpleLinearChart2.SimpleLinearChart2CoordinateAxisBean simpleLinearChart2YAxisBean = new SimpleLinearChart2.SimpleLinearChart2CoordinateAxisBean();
        simpleLinearChart2YAxisBean.setCoordinateAxisTextData(simpleLinearChart2YAxisData, true);
        //X轴
        List<SimpleLinearChart2.OnSimpleLinearChart2XyAxisTextRealization<SimpleLinearChart2TxtBean>> simpleLinearChart2XAxisData = new ArrayList<>();
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("4月", 0));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("5月", 0));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("6月", 0));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("7月", 0));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("8月", 0));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("9月", 0));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("10月", 0));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("11月", 0));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("12月", 0));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("1月", 35000));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("2月", 20000));
        simpleLinearChart2XAxisData.add(new SimpleLinearChart2TxtBean("3月", 30000));
        SimpleLinearChart2.SimpleLinearChart2CoordinateAxisBean simpleLinearChart2XAxisBean = new SimpleLinearChart2.SimpleLinearChart2CoordinateAxisBean();
        simpleLinearChart2XAxisBean.setCoordinateAxisTextData(simpleLinearChart2XAxisData, false);
        //内容轴
        List<SimpleLinearChart2.SimpleLinearChart2CoordinateAxisBean> contentAxisDataList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<SimpleLinearChart2.OnSimpleLinearChart2XyAxisTextRealization<SimpleLinearChart2TxtBean>> simpleLinearChart2ContentAxisData = new ArrayList<>();
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("4月", (i + 1) * 10000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("5月", (i + 1) * 10000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("6月", (i + 1) * 9000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("7月", (i + 1) * 12000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("8月", (i + 1) * 9000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("9月", (i + 1) * 8000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("10月", (i + 1) * 7000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("11月", (i + 1) * 8000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("12月", (i + 1) * 11000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("1月", (i + 1) * 10000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("2月", (i + 1) * 9000));
            simpleLinearChart2ContentAxisData.add(new SimpleLinearChart2TxtBean("3月", (i + 1) * 10000));
            SimpleLinearChart2.SimpleLinearChart2CoordinateAxisBean simpleLinearChart2ContentAxisBean = new SimpleLinearChart2.SimpleLinearChart2CoordinateAxisBean();
            simpleLinearChart2ContentAxisBean.setRemark("202" + i + "年");
            simpleLinearChart2ContentAxisBean.setChecked(i == 1);
            simpleLinearChart2ContentAxisBean.setCoordinateAxisTextData(simpleLinearChart2ContentAxisData, false);
            contentAxisDataList.add(simpleLinearChart2ContentAxisBean);
        }


        simpleLinearChart2.setData(simpleLinearChart2YAxisBean, simpleLinearChart2XAxisBean, contentAxisDataList);


        simpleMultipleColumnView = findViewById(R.id.simpleMultipleColumnView);
        //Y轴
        List<SimpleMultipleColumnView.OnSimpleMultipleColumnViewXyAxisTextRealization<SimpleMultipleColumnTxtBean>> simpleMultipleColumnViewYAxisData = new ArrayList<>();
        simpleMultipleColumnViewYAxisData.add(new SimpleMultipleColumnTxtBean("25k", 25000));
        simpleMultipleColumnViewYAxisData.add(new SimpleMultipleColumnTxtBean("20k", 20000));
        simpleMultipleColumnViewYAxisData.add(new SimpleMultipleColumnTxtBean("15k", 15000));
        simpleMultipleColumnViewYAxisData.add(new SimpleMultipleColumnTxtBean("10k", 10000));
        simpleMultipleColumnViewYAxisData.add(new SimpleMultipleColumnTxtBean("5k", 5000));
        simpleMultipleColumnViewYAxisData.add(new SimpleMultipleColumnTxtBean("1k", 1000));
        SimpleMultipleColumnView.SimpleMultipleColumnViewCoordinateAxisBean simpleMultipleColumnViewYAxisBean = new SimpleMultipleColumnView.SimpleMultipleColumnViewCoordinateAxisBean();
        simpleMultipleColumnViewYAxisBean.setCoordinateAxisTextData(simpleMultipleColumnViewYAxisData, true);
        //X轴
        List<SimpleMultipleColumnView.OnSimpleMultipleColumnViewXyAxisTextRealization<SimpleMultipleColumnTxtBean>> simpleMultipleColumnViewXAxisData = new ArrayList<>();
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("1月", 25000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("2月", 20000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("3月", 15000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("4月", 10000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("5月", 5000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("6月", 1000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("7月", 1000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("8月", 1000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("9月", 1000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("10月", 1000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("11月", 1000));
        simpleMultipleColumnViewXAxisData.add(new SimpleMultipleColumnTxtBean("12月", 1000));
        SimpleMultipleColumnView.SimpleMultipleColumnViewCoordinateAxisBean simpleMultipleColumnViewXAxisBean = new SimpleMultipleColumnView.SimpleMultipleColumnViewCoordinateAxisBean();
        simpleMultipleColumnViewXAxisBean.setCoordinateAxisTextData(simpleMultipleColumnViewXAxisData, false);
        //内容轴
        List<SimpleMultipleColumnView.OnSimpleMultipleColumnViewXyAxisTextRealization<SimpleMultipleColumnTxtBean>> simpleMultipleColumnViewContentAxisData = new ArrayList<>();
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("1月", 0f, 0f, 0f, 0f, 0f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("2月", 0.1f, 0.2f, 0.3f, 0.3f, 0.1f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("3月", 0.3f, 0.2f, 0.1f, 0.1f, 0.3f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("4月", 0.1f, 0.2f, 0.3f, 0.3f, 0.1f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("5月", 0.3f, 0.2f, 0.1f, 0.1f, 0.3f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("6月", 0.1f, 0.2f, 0.3f, 0.3f, 0.1f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("7月", 0.3f, 0.2f, 0.1f, 0.1f, 0.3f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("8月", 0.1f, 0.2f, 0.3f, 0.3f, 0.1f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("9月", 0.3f, 0.2f, 0.1f, 0.1f, 0.3f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("10月", 0.1f, 0.2f, 0.3f, 0.3f, 0.1f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("11月", 0.3f, 0.2f, 0.1f, 0.1f, 0.3f));
        simpleMultipleColumnViewContentAxisData.add(new SimpleMultipleColumnTxtBean("12月", 0.1f, 0.2f, 0.3f, 0.3f, 0.1f));
        SimpleMultipleColumnView.SimpleMultipleColumnViewCoordinateAxisBean simpleMultipleColumnViewContentAxisBean = new SimpleMultipleColumnView.SimpleMultipleColumnViewCoordinateAxisBean();
        simpleMultipleColumnViewContentAxisBean.setCoordinateAxisTextData(simpleMultipleColumnViewContentAxisData, false);


        simpleMultipleColumnView.setData(simpleMultipleColumnViewYAxisBean, simpleMultipleColumnViewXAxisBean, simpleMultipleColumnViewContentAxisBean);

    }

}