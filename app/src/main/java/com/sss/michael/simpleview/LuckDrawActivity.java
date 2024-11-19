package com.sss.michael.simpleview;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.sss.michael.simpleview.view.SimpleLuckDrawView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LuckDrawActivity extends AppCompatActivity {
    SimpleLuckDrawView luckDrawView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {  //修正安卓8.0手机透明垂直报错
        //修正安卓8.0手机透明垂直报错
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O && Build.VERSION.SDK_INT != Build.VERSION_CODES.O_MR1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 锁定竖屏
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luck_draw);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        ImageView ivShadow = findViewById(R.id.iv_shadow);
        ivShadow.setImageResource(R.mipmap.bg_light);

        luckDrawView = findViewById(R.id.luck_draw_view);

        List<SimpleLuckDrawView.LuckDrawBean> list = new ArrayList<>();
        list.add(new SimpleLuckDrawView.LuckDrawBean(this, "幸运符x20", "https://img.duoziwang.com/2019/01/02132028910281.jpg"));
        list.add(new SimpleLuckDrawView.LuckDrawBean(this, "50史诗自选罐x2", "https://img.duoziwang.com/2019/01/02132028910280.jpg"));
        list.add(new SimpleLuckDrawView.LuckDrawBean(this, "史诗跨界石x1", "https://img.duoziwang.com/2019/01/02132028910288.jpg"));
        list.add(new SimpleLuckDrawView.LuckDrawBean(this, "炉岩碳x2000", "https://img.duoziwang.com/2019/01/02132028910287.jpg"));
        list.add(new SimpleLuckDrawView.LuckDrawBean(this, "金币x100W", "https://img.duoziwang.com/2019/01/02132028910285.jpg"));
        list.add(new SimpleLuckDrawView.LuckDrawBean(this, "Lv50升级券x1", "https://img.duoziwang.com/2019/01/02132028910293.jpg"));

        luckDrawView.setOnLuckDrawCallBack(new SimpleLuckDrawView.OnLuckDrawCallBack() {
            @Override
            public void onPrepareComplete(int count, boolean success) {

            }

            @Override
            public void onLoadComplete(boolean ready) {
                if (ready) {
                    luckDrawView.invalidate();
                    luckDrawView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            luckDrawView.start(3);
                        }
                    });
                } else {
                    Toast.makeText(LuckDrawActivity.this, "图片加载出错", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLuckDrawResult(int position) {
                Toast.makeText(LuckDrawActivity.this, "恭喜获得" + list.get(position).getLabel(), Toast.LENGTH_SHORT).show();
            }
        });
        luckDrawView.preview(list);

    }
}
