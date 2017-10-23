package com.lhc.openvc.identify;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 作者：lhc
 * 时间：2017/10/21.
 */

public class FaceIdentifier {

    static {
        System.loadLibrary("native-lib");
    }

    private final static String TAG = "test";
    private final static String DIR_NAME = "classifier";

    public void loadFile(final Context context, final String classifier) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                File dir = context.getExternalFilesDir(DIR_NAME);
                Log.d(TAG, "存储路径:" + dir.getAbsolutePath());
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                InputStream is = null;
                FileOutputStream fos = null;
                File file = null;
                try {
                    file = new File(dir, classifier);
                    if (!file.exists()) {
                        is = context.getAssets().open(classifier);
                        fos = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null)
                            fos.close();
                        if (is != null)
                            is.close();
                        if (file != null)
                            setClassifier(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }


        }.execute();

    }

    public void loadBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            Log.d("test", "加载bitmap");
            setBitmap(bitmap);
        }
    }

    public void loadSurface(Surface surface, int width, int height) {
        setSurface(surface, width, height);
    }

    public void onDestroy() {
        destroy();
    }


    public void loadClassifier(String path) {
        if (!TextUtils.isEmpty(path)) {
            setClassifier(path);
        }
    }

    private native void setClassifier(String path);

    private native int setBitmap(Bitmap bitmap);

    private native void setSurface(Surface surface, int width, int height);

    private native void destroy();

}
