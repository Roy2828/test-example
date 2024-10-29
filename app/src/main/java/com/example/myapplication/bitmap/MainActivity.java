package com.example.myapplication.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bitmap);

        initView();
        decodeBitmap();
        ImageCache.getInstance().init(this, Environment.getExternalStorageDirectory() + "/bitmap");
    }

    /**
     * 初始化 recyclerView
     */
    private void initView() {
        RecyclerView rv = findViewById(R.id.rv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(linearLayoutManager);

        BitmapAdapter bitmapAdapter = new BitmapAdapter(this);
        rv.setAdapter(bitmapAdapter);
    }

    /**
     * 解析图片
     */
    private void decodeBitmap() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back_white_24dp);
        Log.e("leo", "decodeBitmap: " + bitmap.getWidth() + "X" + bitmap.getHeight() + "x"
                + bitmap.getConfig() + ",内存总大小" + bitmap.getByteCount());
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back_white_24dp);
        Log.e("leo", "decodeBitmap: " + bitmap1.getWidth() + "X" + bitmap1.getHeight() + "x"
                + bitmap1.getConfig() + ",内存总大小" + bitmap1.getByteCount());
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back_white_24dp);
        Log.e("leo", "decodeBitmap: " + bitmap2.getWidth() + "X" + bitmap2.getHeight() + "x"
                + bitmap2.getConfig() + ",内存总大小" + bitmap2.getByteCount());
    }
}


