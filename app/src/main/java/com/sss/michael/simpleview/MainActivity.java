package com.sss.michael.simpleview;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sss.michael.simpleview.utils.Log;
import com.sss.michael.simpleview.view.MyViewPager;
import com.sss.michael.simpleview.view.SimpleDoubleSeekBar;
import com.sss.michael.simpleview.view.SimpleDoubleSeekBar2;
import com.sss.michael.simpleview.view.SimpleHalfPieChart;
import com.sss.michael.simpleview.view.SimpleHalfRingView;
import com.sss.michael.simpleview.view.SimpleLinearChart;
import com.sss.michael.simpleview.view.SimpleProgressBar;
import com.sss.michael.simpleview.view.SimpleRotatingView;
import com.sss.michael.simpleview.view.SimpleSlideBesselView;
import com.sss.michael.simpleview.view.SimpleSpiderView;
import com.sss.michael.simpleview.view.SimpleWrapOffsetWidthView;
import com.sss.michael.simpleview.view.TransitionImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

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


        final ImageView last = findViewById(R.id.last);
        final ImageView next = findViewById(R.id.next);
        final ImageView image = findViewById(R.id.image);
        final MyViewPager<String> myViewPager = findViewById(R.id.myViewPager);
        myViewPager.setOnMyViewPagerCallBack(new MyViewPager.OnMyViewPagerCallBack<String>() {

            @Override
            public void onDataChange(String s, int position) {

            }

            @Override
            public void setImage(MyViewPager.Direction direction, List<String> models, final TransitionImageView imageView, int lastPosition, int currentPosition, int nextPosition) {
                Log.log(models.get(currentPosition), models.get(nextPosition));

                if (direction == MyViewPager.Direction.LEFT_TO_RIGHT) {
                    Glide.with(MainActivity.this).asBitmap().load(models.get(lastPosition)).into(last);
                } else if (direction == MyViewPager.Direction.RIGHT_TO_LEFT) {
                    Glide.with(MainActivity.this).asBitmap().load(models.get(nextPosition)).addListener(new RequestListener<Bitmap>() {

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            next.setImageBitmap(resource);
                            next.setDrawingCacheEnabled(true);
                            Bitmap bmp = Bitmap.createBitmap(next.getDrawingCache());
                            next.setDrawingCacheEnabled(false);
//                        Log.log("SSSSS",myViewPager.getLeft(), myViewPager.getTop(), myViewPager.getWidth(), myViewPager.getHeight());
                            bmp = imageCrop(bmp, myViewPager.getLeft(), myViewPager.getTop(), myViewPager.getWidth(), myViewPager.getHeight());
                            if (bmp != null) {
                                imageView.setImageBitmap(resource);
                            }
                            return false;
                        }
                    }).into(next);

                } else {
                    Glide.with(MainActivity.this).asBitmap().load(models.get(currentPosition)).into(image);
                    Glide.with(MainActivity.this).asBitmap().load(models.get(nextPosition)).into(next);
                    Glide.with(MainActivity.this).asBitmap().load(models.get(currentPosition)).addListener(new RequestListener<Bitmap>() {

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            image.setImageBitmap(resource);
                            image.setDrawingCacheEnabled(true);
                            Bitmap bmp = Bitmap.createBitmap(image.getDrawingCache());
                            image.setDrawingCacheEnabled(false);
//                        Log.log("SSSSS",myViewPager.getLeft(), myViewPager.getTop(), myViewPager.getWidth(), myViewPager.getHeight());
                            bmp = imageCrop(bmp, myViewPager.getLeft(), myViewPager.getTop(), myViewPager.getWidth(), myViewPager.getHeight());
                            if (bmp != null) {
                                imageView.setImageBitmap(resource);
                            }
                            return false;
                        }
                    }).into(image);
                }


            }
        });
        List<String> list = new ArrayList<>();
        list.add("https://alifei05.cfp.cn/creative/vcg/veer/1600water/veer-161885124.jpg");
        list.add("https://alifei04.cfp.cn/creative/vcg/veer/1600water/veer-303764513.jpg");
        list.add("https://tenfei04.cfp.cn/creative/vcg/veer/1600water/veer-142190838.jpg");
        myViewPager.setData(list);
    }

    /**
     * 根据控件比例剪裁bitmap成一个固定大小的图片
     *
     * @param resource 需要裁剪的图片的bitmap值
     * @param x        从图片的x轴的x处开始裁剪
     * @param y        从图片的y轴的y处开始裁剪
     * @param width    裁剪生成新图皮的宽
     * @param height   裁剪生成新图皮的高
     * @return 裁剪之后的bitmap
     */
    public static Bitmap imageCrop(Bitmap resource, int x, int y, int width, int height) {
        if (resource == null) {
            return null;
        }
        if (resource.getWidth() == 0 || resource.getHeight() == 0) {
            return null;
        }
        if (x > resource.getWidth() || x < 0) {
            x = 0;
        }

        if (y > resource.getHeight() || y < 0) {
            y = 0;
        }
        if (x + width > resource.getWidth()) {
            return null;
        }
        if (y + height > resource.getHeight()) {
            return null;
        }
        width = Math.min(width, resource.getWidth());
        height = Math.min(height, resource.getHeight());
        return Bitmap.createBitmap(resource, x, y, width, height);
    }

}