package com.sss.michael.simpleview.bean;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sss.michael.simpleview.R;
import com.sss.michael.simpleview.utils.Log;
import com.sss.michael.simpleview.view.SimpleGraffitiView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GraffitiActivity extends AppCompatActivity {

    private Button revoked, recovery;
    private SimpleGraffitiView graffitiView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graffiti);
        graffitiView = findViewById(R.id.graffiti_view);
        revoked = findViewById(R.id.revoked);
        recovery = findViewById(R.id.recovery);
        revoked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graffitiView.revoked();
            }
        });
        recovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graffitiView.recovery();
            }
        });
        graffitiView.setOnSimpleGraffitiViewCallBack(new SimpleGraffitiView.OnSimpleGraffitiViewCallBack() {
            @Override
            public void onTrajectoryCreated(List<SimpleGraffitiView.PointTrajectory> points) {
                int revokedCount = 0;
                int recoveryCount = 0;
                for (int i = 0; i < points.size(); i++) {
                    if (points.get(i).isRevoked()) {
                        revokedCount++;
                    } else {
                        recoveryCount++;
                    }
                }
                revoked.setEnabled(recoveryCount > 0);
                recovery.setEnabled(revokedCount > 0);
            }
        });
    }
}
