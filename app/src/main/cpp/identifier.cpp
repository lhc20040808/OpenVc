#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#include <android/log.h>

#define LOG_TAG "test"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#define DEFAULT_CARD_WIDTH 640
#define DEFAULT_CARD_HEIGHT 400
#define  FIX_IDCARD_SIZE Size(DEFAULT_CARD_WIDTH,DEFAULT_CARD_HEIGHT)
#define FIX_TEMPLATE_SIZE  Size(153, 28)

extern "C" {

using namespace cv;
using namespace std;

extern JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nBitmapToMat2
        (JNIEnv *env, jclass, jobject bitmap, jlong m_addr, jboolean needUnPremultiplyAlpha);
extern JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nMatToBitmap
        (JNIEnv *env, jclass, jlong m_addr, jobject bitmap);

jobject createBitmap(JNIEnv *env, Mat srcDate, jobject config) {
    int imgWidth = srcDate.cols;
    int imgHeight = srcDate.rows;

    jclass bmpClz = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMid = env->GetStaticMethodID(bmpClz, "createBitmap",
                                                       "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jobject jBmpObj = env->CallStaticObjectMethod(bmpClz, createBitmapMid, imgWidth, imgHeight,
                                                  config);
    Java_org_opencv_android_Utils_nMatToBitmap(env, 0, (jlong) &srcDate, jBmpObj);
    return jBmpObj;
}

JNIEXPORT jobject JNICALL
Java_com_lhc_openvc_identify_TessManager_findIdCardNumber(JNIEnv *env, jclass type,
                                                          jobject tpl, jobject src,
                                                          jobject config) {

    Mat img_src;
    Mat img_gray;
    Mat img_threshold;
    Mat img_gaussian;
    Mat img_tpl;
    Java_org_opencv_android_Utils_nBitmapToMat2(env, type, src, (jlong) &img_src, 0);
    Java_org_opencv_android_Utils_nBitmapToMat2(env, type, tpl, (jlong) &img_tpl, 0);
    cvtColor(img_src, img_gray, COLOR_BGR2GRAY);//转换成灰度图
//    imwrite("sdcard/gray.png", img_gray);
    threshold(img_gray, img_threshold, 195, 255, THRESH_TRUNC);//将图片二值化
//    imwrite("sdcard/threshold.png", img_gray);
    GaussianBlur(img_threshold, img_gaussian, Size(3, 3), 0);//图片进行高斯滤波
//    imwrite("sdcard/gaussian.png", img_gray);
    Canny(img_gaussian, img_gaussian, 180, 255);//边缘检测
//    imwrite("sdcard/canny.png", img_gray);
    vector<vector<Point> > contours;
    vector<Vec4i> hierachy;
    findContours(img_gaussian, contours, hierachy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);//轮廓检测（外轮廓）
    int half_width = img_src.cols >> 1;
    int half_height = img_src.rows >> 1;
    vector<Rect> areas;
    //寻找宽高符合要求的轮廓
    for (int i = 0; i < contours.size(); i++) {
        vector<Point> contour = contours.at(i);
        Rect rect = boundingRect(contour);
        rectangle(img_threshold, rect, Scalar(255, 255, 255));
        if (rect.width >= half_width && rect.height >= half_height) {
            areas.push_back(rect);
        }
    }
    Rect min_area;
    if (areas.size() > 0) {
        min_area = areas.at(0);
        for (int i = 1; i < areas.size(); i++) {
            Rect tmp_area = areas.at(i);
            if (tmp_area.area() < min_area.area()) {
                min_area = tmp_area;
            }
        }
    } else {
        min_area = Rect(0, 0, img_src.cols, img_src.rows);
    }
//    imwrite("sdcard/contours.png", img_threshold);

    Mat img_id_card = img_gray(min_area);
//    imwrite("sdcard/idCard.png", img_id_card);
    resize(img_id_card, img_id_card, FIX_IDCARD_SIZE);
    resize(img_tpl, img_tpl, FIX_TEMPLATE_SIZE);
    cvtColor(img_tpl, img_tpl, COLOR_BGR2GRAY);
//    imwrite("sdcard/tpl.png", img_tpl);
    Mat match;
    matchTemplate(img_id_card, img_tpl, match, TM_CCORR_NORMED);//找到匹配的模板
    normalize(match, match, 0, 1, NORM_MINMAX, -1);//归一化
    Point loc;
    minMaxLoc(match, 0, 0, 0, &loc);//找到模板左上角坐标
    LOGI("y:%d", loc.y);
    Rect num_rect(loc.x + img_tpl.cols + 10, loc.y - 5,
                  img_id_card.cols - (loc.x + img_tpl.cols) - 50,
                  img_tpl.rows + 10);

    Mat img_id_card_num = img_id_card(num_rect);
    //    imwrite("sdcard/img_id_card_num.png", img_id_card_num);
    jobject result_bmp = createBitmap(env, img_id_card_num, config);

    img_src.release();
    img_gray.release();
    img_id_card.release();
    img_id_card_num.release();
    img_tpl.release();
    img_threshold.release();
    img_gaussian.release();
    match.release();

    return result_bmp;
}

}



