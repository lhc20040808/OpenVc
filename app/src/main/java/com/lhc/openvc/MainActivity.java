package com.lhc.openvc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.lhc.openvc.identify.FaceIdentifier;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private Album album;
    private SurfaceView mSurface;
    private FaceIdentifier identifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurface = (SurfaceView) findViewById(R.id.surface_view);
        mSurface.getHolder().addCallback(this);
        album = new Album();
        identifier = new FaceIdentifier();
        identifier.loadFile(this, "haarcascade_frontalface_alt.xml");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Album.ALBUM_REQUEST_CODE:
                    album.getPicWithScale(this, data, mSurface.getWidth(), mSurface.getHeight());
                    Bitmap bitmap = album.getBitmap();
                    identifier.loadBitmap(bitmap);
                    break;
            }
        }
    }

    public void openAlbum(View view) {
        album.openAlbum(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (album != null)
            album.recycle();
        if (identifier != null)
            identifier.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Bitmap bitmap = album.getBitmap();
        identifier.loadSurface(surfaceHolder.getSurface(), 640, 960);//TODO 宽高动态获取
        identifier.loadBitmap(bitmap);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
