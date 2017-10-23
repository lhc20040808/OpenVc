package com.lhc.openvc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * 作者：lhc
 * 时间：2017/10/21.
 */

public class Album {
    private Bitmap bitmap;

    public static final String PIC_SCHEME_FILE = "file";
    public static final String PIC_SCHEME_CONTENT = "content";
    public static final int ALBUM_REQUEST_CODE = 100;

    public void openAlbum(Activity context) {
        Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        intent.setType("image/*");
        context.startActivityForResult(Intent.createChooser(intent, "选择待识别图片"), ALBUM_REQUEST_CODE);
    }

    public void getPicWithoutScale(Context context, Intent intent) {
        String imgPath = getPicPath(context, intent);

        if (!TextUtils.isEmpty(imgPath)) {
            bitmap = BitmapFactory.decodeFile(imgPath);
        }
    }

    public void getPicWithScale(Context context, Intent intent, int reqWidth, int reqHeight) {
        String imgPath = getPicPath(context, intent);
        if (!TextUtils.isEmpty(imgPath)) {
            bitmap = scaleBitmap(imgPath, reqWidth, reqHeight);
        }
    }

    @Nullable
    private String getPicPath(Context context, Intent intent) {
        recycle();
        Uri uri = intent.getData();
        String imgPath = null;

        if (PIC_SCHEME_FILE.equals(uri.getScheme())) {
            Log.d("test", "path uri 获得图片");
            imgPath = uri.getPath();
        } else if (PIC_SCHEME_CONTENT.equals(uri.getScheme())) {
            Log.d("test", "content uri 获得图片");
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = context.getContentResolver().query(uri, filePathColumns, null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(filePathColumns[0]);
                    imgPath = c.getString(columnIndex);
                }
                c.close();
            }
        }
        return imgPath;
    }

    public static Bitmap scaleBitmap(String path, int reqWidth, int reqHeight) {
        if (TextUtils.isEmpty(path))
            return null;

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opt);
        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;
        Log.d("test", "图片原始宽度:" + picWidth + " 原始高度:" + picHeight);
        int scale = 1;
        while (true) {
            if (picWidth <= reqWidth && picHeight <= reqHeight)
                break;
            picHeight /= 2;
            picWidth /= 2;
            scale *= 2;
        }
        BitmapFactory.Options finalOpt = new BitmapFactory.Options();
        finalOpt.inSampleSize = scale;
        finalOpt.outHeight = picHeight;
        finalOpt.outWidth = picWidth;
        return BitmapFactory.decodeFile(path, finalOpt);

    }

    public void recycle() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        bitmap = null;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
