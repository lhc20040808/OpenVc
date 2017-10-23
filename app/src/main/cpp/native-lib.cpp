#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#include <android/log.h>

#define LOG_TAG "test"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

extern "C" {

using namespace cv;
using namespace std;

void bitmap2Mat(JNIEnv *env, jobject bitmap, Mat &dst);
void releaseANativeWindow();

CascadeClassifier *cascadeClassifier;
ANativeWindow *nativeWindow;

JNIEXPORT void JNICALL
Java_com_lhc_openvc_identify_Identifier_setClassifier(JNIEnv *env, jobject instance,
                                                      jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    LOGI("native--->加载分类器");
    cascadeClassifier = new CascadeClassifier(path);

    env->ReleaseStringUTFChars(path_, path);
}


JNIEXPORT int JNICALL
Java_com_lhc_openvc_identify_Identifier_setBitmap(JNIEnv *env, jobject instance, jobject bitmap) {

    int ret = 1;
    Mat src;
    bitmap2Mat(env, bitmap, src);

    if (cascadeClassifier) {
        vector<Rect> results;
        Mat dst;
        cvtColor(src, dst, CV_BGR2GRAY);//图像灰度化
//        imwrite("sdcard/huidu.png", dst);
        equalizeHist(dst, dst);//直方图均衡化
//        imwrite("sdcard/e.png", dst);
        cascadeClassifier->detectMultiScale(dst, results);
        dst.release();
        for (int i = 0; i < results.size(); i++) {
            Rect rect = results[i];
            rectangle(src, rect.tl(), rect.br(), Scalar(0, 255, 255));
        }
    }

    if (!nativeWindow) {
        LOGI("native window is null");
        ret = 0;
        goto end;
    }

    ANativeWindow_Buffer window_buffer;
    if (ANativeWindow_lock(nativeWindow, &window_buffer, 0)) {
        LOGI("native window lock loadFail");
        ret = 0;
        goto end;
    }
    LOGI("window buffer width:%d height:%d", window_buffer.width, window_buffer.height);
    cvtColor(src, src, CV_BGR2RGBA);
    resize(src, src, Size(window_buffer.width, window_buffer.height));
    memcpy(window_buffer.bits, src.data, window_buffer.width * window_buffer.height * 4);
    ANativeWindow_unlockAndPost(nativeWindow);

    end:
    src.release();

    return ret;
}

JNIEXPORT void JNICALL
Java_com_lhc_openvc_identify_Identifier_setSurface(JNIEnv *env, jobject instance, jobject surface,
                                                   jint width, jint height) {

    if (surface && width && height) {
        releaseANativeWindow();

        nativeWindow = ANativeWindow_fromSurface(env, surface);
        if (nativeWindow) {
            LOGI("创建native window width:%d height:%d", width, height);
            ANativeWindow_setBuffersGeometry(nativeWindow, width, height, WINDOW_FORMAT_RGBA_8888);
        }
    } else {
        releaseANativeWindow();
    }

}

JNIEXPORT void JNICALL
Java_com_lhc_openvc_identify_Identifier_destroy(JNIEnv *env, jobject instance) {

    if (cascadeClassifier) {
        delete cascadeClassifier;
        cascadeClassifier = 0;
    }

    releaseANativeWindow();
}

void bitmap2Mat(JNIEnv *env, jobject bitmap, Mat &dst) {
#if 0
    AndroidBitmapInfo info;
    void *pixels = 0;
    //获取bitmap信息
    CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
    //必须是 rgba8888 rgb565
    CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
    //lock 获得数据
    CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
    CV_Assert(pixels);

    dst.create(info.height, info.width, CV_8UC3);
    LOGI("bitmap2Mat: RGBA_8888 bitmap -> Mat");
    Mat tmp;
    tmp = Mat(info.height, info.width, CV_8UC3, pixels);
    cvtColor(tmp, dst, COLOR_RGBA2BGR);
    tmp.release();
    AndroidBitmap_unlockPixels(env, bitmap);
#else
    AndroidBitmapInfo info;
    void *pixels = 0;


    try {
        LOGI("nBitmapToMat");
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        dst.create(info.height, info.width, CV_8UC4);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            LOGI("nBitmapToMat: RGBA_8888 -> CV_8UC4");
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
//            if(needUnPremultiplyAlpha) cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
//            else tmp.copyTo(dst);
            tmp.copyTo(dst);
//            cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
            cvtColor(dst, dst, COLOR_RGBA2BGR);
            tmp.release();
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            LOGI("nBitmapToMat: RGB_565 -> CV_8UC4");
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
//            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
            cvtColor(tmp, dst, COLOR_BGR5652BGR);
            tmp.release();
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGI("nBitmapToMat catched cv::Exception: %s", e.what());
        jclass je = env->FindClass("org/opencv/core/CvException");
        if (!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGI("nBitmapToMat catched unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
#endif

}

void releaseANativeWindow() {
    if (nativeWindow) {
        LOGI("释放native window");
        ANativeWindow_release(nativeWindow);
        nativeWindow = 0;
    }
}

}

