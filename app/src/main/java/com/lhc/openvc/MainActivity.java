package com.lhc.openvc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.lhc.openvc.identify.FaceIdentifier;
import com.lhc.openvc.identify.Identifier;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private Album album;
    private SurfaceView mSurface;
    private Identifier identifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurface = (SurfaceView) findViewById(R.id.surface_view);
        mSurface.getHolder().addCallback(this);
        album = new Album();
        identifier = new Identifier();
        identifier.setListener(new FaceIdentifier());
        identifier.loadFile(this, "haarcascade_frontalface_alt.xml");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Album.ALBUM_REQUEST_CODE:
                    Log.d("test", "Surface:" + (mSurface.getHolder().getSurface() == null));
                    album.getPicWithScale(this, data, 480, 640);
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
        Log.d("test", "surfaceChanged" + " width:" + mSurface.getWidth() + " height:" + mSurface.getHeight());
        identifier.loadSurface(surfaceHolder.getSurface(), 480, 640);//TODO 宽高动态获取
        Bitmap bitmap = album.getBitmap();
        identifier.loadBitmap(bitmap);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
