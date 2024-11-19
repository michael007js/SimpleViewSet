package com.sss.michael.simpleview;

import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sss.michael.simpleview.utils.DensityUtil;
import com.sss.michael.simpleview.view.PrinterTextView;

public class StreamTextActivity extends AppCompatActivity {
    RecyclerView recycler_view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {  //修正安卓8.0手机透明垂直报错
        //修正安卓8.0手机透明垂直报错
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O && Build.VERSION.SDK_INT != Build.VERSION_CODES.O_MR1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 锁定竖屏
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_text);
        recycler_view = findViewById(R.id.recycler_view);

        AppCompatEditText editText = findViewById(R.id.edit);

        StreamTextAdapter adapter = new StreamTextAdapter();
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        recycler_view.addItemDecoration(new RecyclerView.ItemDecoration() {

            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.bottom = DensityUtil.dp2px(10);
            }
        });
        recycler_view.setAdapter(adapter);


        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText() != null) {
                    String content = editText.getText().toString();
                    Bean bean = new Bean(0, content);
                    adapter.addData(bean);
                    recycler_view.post(new Runnable() {
                        @Override
                        public void run() {
                            recycler_view.smoothScrollToPosition(adapter.getData().size() - 1);
                        }
                    });
                    recycler_view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Bean bean = new Bean(1, "这是第" + adapter.getData().size() + "条回复");
                            adapter.addData(bean);
                            recycler_view.post(new Runnable() {
                                @Override
                                public void run() {
                                    recycler_view.smoothScrollToPosition(adapter.getData().size() - 1);
                                }
                            });
                        }
                    }, 1000);
                }
            }
        });
    }

    static class StreamTextAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
        public StreamTextAdapter() {
            addItemType(0, R.layout.item_stream_text_self);
            addItemType(1, R.layout.item_stream_text_target);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, MultiItemEntity item) {
            Bean bean = (Bean) item;
            PrinterTextView printerTextView = (PrinterTextView) helper.getView(R.id.printerTextView);
            if (bean.type == 1) {
                if (bean.outputed) {
                    printerTextView.setText(bean.content);
                } else {
                    bean.outputed = true;
                    printerTextView.setPrintText(bean.content);
                    printerTextView.startPrint();
                }
            } else {
                printerTextView.setText(bean.content);
            }
        }
    }

    static class Bean implements MultiItemEntity {
        private int type;
        private String content;
        private boolean outputed;

        public Bean(int type, String content) {
            this.type = type;
            this.content = content;
        }

        @Override
        public int getItemType() {
            return type;
        }
    }

}
