package com.lhc.openvc.identify;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentValues.TAG;

/**
 * 作者：lhc
 * 时间：2017/10/23.
 */

public class TessManager {

    static {
        System.loadLibrary("native-lib");
    }

    public static final String DIR_NAME = "tess";
    public static final String SUB_DIR_NAME = "tessdata";
    public static final String SUFFIX = ".traineddata";
    private TessBaseAPI baseAPI;

    public TessManager() {
        baseAPI = new TessBaseAPI();
    }

    public void loadFile(final Context context, final String trainData) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                boolean ret = false;
                File dir = context.getExternalFilesDir(DIR_NAME);
                Log.d(TAG, "存储路径:" + dir.getAbsolutePath());
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File subDir = new File(dir, SUB_DIR_NAME);
                if (!subDir.exists()) {
                    subDir.mkdirs();
                }

                InputStream is = null;
                FileOutputStream fos = null;
                File file = null;
                try {
                    file = new File(subDir, trainData);
                    if (!file.exists()) {
                        is = context.getAssets().open(trainData);
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
                            ret = baseAPI.init(dir.getAbsolutePath(), getLanguageFromData(trainData));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Log.d("test", "加载结果:" + ret);
                return null;
            }


        }.execute();

    }

    private String getLanguageFromData(String trainData) {
        if (trainData.contains(SUFFIX)) {
            return trainData.substring(0, trainData.indexOf(SUFFIX));
        }
        return trainData;
    }

    public String recognizeBitmap(Bitmap bitmap) {
        try {
            baseAPI.setImage(bitmap);
            return baseAPI.getUTF8Text();
        } finally {
            baseAPI.clear();
        }
    }


    public static native Bitmap findIdCardNumber(Bitmap template, Bitmap src, Bitmap.Config config);

}
