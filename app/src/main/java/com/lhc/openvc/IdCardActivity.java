package com.lhc.openvc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lhc.openvc.identify.TessManager;

public class IdCardActivity extends AppCompatActivity {
    private Album album;
    private TessManager manager;
    private Bitmap template;
    private TextView tv_idcard;
    private ImageView img_idcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcard);
        album = new Album();
        manager = new TessManager();
        template = BitmapFactory.decodeResource(getResources(), R.mipmap.te);
        manager.loadFile(IdCardActivity.this, "card.traineddata");
        tv_idcard = (TextView) findViewById(R.id.tv_idcard);
        img_idcard = (ImageView) findViewById(R.id.img_idcard);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Album.ALBUM_REQUEST_CODE:
                    album.getPicWithoutScale(this, data);
                    Bitmap bitmap = album.getBitmap();
                    Bitmap idCardBitmap = TessManager.findIdCardNumber(template, bitmap, Bitmap.Config.ARGB_8888);
                    img_idcard.setImageBitmap(idCardBitmap);
                    bitmap.recycle();
                    String idCardNum = manager.recognizeBitmap(idCardBitmap);
                    tv_idcard.setText("身份证号：" + idCardNum);
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

    }
}
