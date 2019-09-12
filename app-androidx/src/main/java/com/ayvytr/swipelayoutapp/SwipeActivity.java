package com.ayvytr.swipelayoutapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ayvytr.swipelayout.SwipeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SwipeActivity extends AppCompatActivity {

    private TextView tv;
    private SwipeLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);
        initView();
    }

    private void initView() {
        tv = findViewById(R.id.tv);
        swipeLayout = findViewById(R.id.swipe_layout);
        swipeLayout.setOnStateChangedListener(new SwipeLayout.OnStateChangedListener() {
            @Override
            public void onChanged(boolean isOpen, SwipeLayout swipeLayout) {
                Toast.makeText(SwipeActivity.this, isOpen + "", Toast.LENGTH_SHORT).show();
            }
        });
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SwipeActivity.this, swipeLayout.isOpen() + "", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
