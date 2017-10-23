package com.lhc.openvc.identify;

import android.util.Log;

import java.io.File;

/**
 * 作者：lhc
 * 时间：2017/10/23.
 */

public class FaceIdentifier implements Identifier.LoadFileListener {

    @Override
    public void loadSuccess(Identifier identifier, File file) {
        identifier.loadClassifier(file.getAbsolutePath());
    }

    @Override
    public void loadFail() {
        Log.d("test", "分类器加载失败");
    }
}
