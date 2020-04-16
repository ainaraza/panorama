#include "com_example_panorama_NativePanorama.h"
#include "opencv2/core.hpp"
#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core/base.hpp>
#include "opencv2/imgproc.hpp"
#include <string>
#include <iostream>

#include "opencv2/stitching.hpp"
#include <opencv2/imgcodecs.hpp>

#define BORDER_GRAY_LEVEL 0

#include <android/log.h>
#include <android/bitmap.h>
#include <sstream>

#define LOG_TAG "GESTION_STITCHING"

using namespace std;
using namespace cv;


#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

bool checkInteriorExterior(const cv::Mat &mask, const cv::Rect &croppingMask,
                                 int &top, int &bottom, int &left, int &right);

bool compareX(cv::Point a, cv::Point b);
bool compareY(cv::Point a, cv::Point b);
void crop(cv::Mat &source);

template <typename T>
std::string NumberToString ( T Number )
{
    std::ostringstream ss;
    ss << Number;
    return ss.str();
}
JNIEXPORT void JNICALL Java_com_example_panorama_NativePanorama_crop
  (JNIEnv * env, jclass clazz, jlong imageAddressArray){
    Mat & image = * (Mat*) imageAddressArray;
    crop(image);
  }

JNIEXPORT void JNICALL Java_com_example_panorama_NativePanorama_processPanorama
  (JNIEnv * env, jclass clazz, jlongArray imageAddressArray, jlong outputAddress)
  {
    // Get the length of the long array
      jsize a_len = env->GetArrayLength(imageAddressArray);
      // Convert the jlongArray to an array of jlong
      jlong *imgAddressArr = env->GetLongArrayElements(imageAddressArray,0);
      // Create a vector to store all the image
      vector< Mat > imgVec;

      for(int k=0;k<a_len;k++)
      {
        // Get the image
        Mat & curimage=*(Mat*)imgAddressArr[k];
        //Mat newimage;
        // Convert to a 3 channel Mat to use with Stitcher module
        //cvtColor(curimage, newimage, CV_BGRA2RGB);
        // Reduce the resolution for fast computation

        //float scale = 1000.0f / curimage.rows;
        //resize(curimage, curimage, Size(scale * curimage.rows, scale * curimage.cols));

        // Save as PNG and load

        /*std::string filepath = "/storage/sdcard0/saved_images_panorama/";
        std::string fileName = "outputStitchingv";
        fileName += NumberToString(k);
        fileName += ".png";

        filepath += fileName;
        imwrite("test.png", curimage, compression_params);*/

        imgVec.push_back(curimage);
        //vector<int> params;
        //params.push_back(CV_IMWRITE_JPEG_QUALITY);
        //params.push_back(60);
        //string filepath = "/storage/sdcard0/azela_captures/file.jpg";
        //int img = imwrite(filepath, curimage, params);
      }
      Mat & result  = *(Mat*) outputAddress;

      Stitcher stitcher = Stitcher::createDefault(true);

      //detail::BestOf2NearestMatcher *matcher = new detail::BestOf2NearestMatcher(false, 0.5f);
      //stitcher.setFeaturesMatcher(matcher);
      //stitcher.setBundleAdjuster(new detail::BundleAdjusterRay());
      //stitcher.setSeamFinder(new detail::NoSeamFinder);
      //stitcher.setExposureCompensator(new detail::NoExposureCompensator());//exposure compensation

      //stitcher.setBlender(new detail::MultiBandBlender());

      //stitcher.setRegistrationResol(-1); /// 0.6
      //stitcher.setSeamEstimationResol(-1);   /// 0.1
      //stitcher.setCompositingResol(-1);   //1
      //stitcher.setPanoConfidenceThresh(-1);   //1
      //stitcher.setWaveCorrection(true);
      //stitcher.setWaveCorrectKind(detail::WAVE_CORRECT_HORIZ);


       LOGD("Begin stitching ...");
       Stitcher::Status status = stitcher.stitch(imgVec, result);
       if (status != Stitcher::OK)
       {
           LOGD("Can't stitch images, error code");
           cout << "Can't stitch images, error code = " << int(status) << endl;
       } else {
           LOGD("Stitching OK");
       }
       //crop(result);
      // Release the jlong array
      env->ReleaseLongArrayElements(imageAddressArray, imgAddressArr ,0);
  }

bool checkInteriorExterior(const cv::Mat &mask, const cv::Rect &croppingMask,
                                 int &top, int &bottom, int &left, int &right)
{
    // Return true if the rectangle is fine as it is
    bool result = true;

    cv::Mat sub = mask(croppingMask);
    int x = 0;
    int y = 0;

    // Count how many exterior pixels are, and choose that side for
    // reduction where mose exterior pixels occurred (that's the heuristic)

    int top_row = 0;
    int bottom_row = 0;
    int left_column = 0;
    int right_column = 0;

    for (y = 0, x = 0; x < sub.cols; ++x)
    {
        // If there is an exterior part in the interior we have
        // to move the top side of the rect a bit to the bottom
        if (sub.at<char>(y, x) == 0)
        {
            result = false;
            ++top_row;
        }
    }

    for (y = (sub.rows - 1), x = 0; x < sub.cols; ++x)
    {
        // If there is an exterior part in the interior we have
        // to move the bottom side of the rect a bit to the top
        if (sub.at<char>(y, x) == 0)
        {
            result = false;
            ++bottom_row;
        }
    }

    for (y = 0, x = 0; y < sub.rows; ++y)
    {
        // If there is an exterior part in the interior
        if (sub.at<char>(y, x) == 0)
        {
            result = false;
            ++left_column;
        }
    }

    for (x = (sub.cols - 1), y = 0; y < sub.rows; ++y)
    {
        // If there is an exterior part in the interior
        if (sub.at<char>(y, x) == 0)
        {
            result = false;
            ++right_column;
        }
    }

    // The idea is to set `top = 1` if it's better to reduce
    // the rect at the top than anywhere else.
    if (top_row > bottom_row)
    {
        if (top_row > left_column)
        {
            if (top_row > right_column)
            {
                top = 1;
            }
        }
    }
    else if (bottom_row > left_column)
    {
        if (bottom_row > right_column)
        {
            bottom = 1;
        }
    }

    if (left_column >= right_column)
    {
        if (left_column >= bottom_row)
        {
            if (left_column >= top_row)
            {
                left = 1;
            }
        }
    }
    else if (right_column >= top_row)
    {
        if (right_column >= bottom_row)
        {
            right = 1;
        }
    }

    return result;
}

bool compareX(cv::Point a, cv::Point b)
{
    return a.x < b.x;
}

bool compareY(cv::Point a, cv::Point b)
{
    return a.y < b.y;
}

void crop(cv::Mat &source)
{
    cv::Mat gray;
    source.convertTo(source, CV_8U);
    cvtColor(source, gray, cv::COLOR_RGB2GRAY);

    // Extract all the black background (and some interior parts maybe)

    cv::Mat mask = gray > 0;

    // now extract the outer contour
    std::vector<std::vector<cv::Point> > contours;
    std::vector<cv::Vec4i> hierarchy;

    cv::findContours(mask, contours, hierarchy, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_NONE, cv::Point(0, 0));
    cv::Mat contourImage = cv::Mat::zeros(source.size(), CV_8UC3);;

    // Find contour with max elements

    int maxSize = 0;
    int id = 0;

    for (int i = 0; i < contours.size(); ++i)
    {
        if (contours.at((unsigned long)i).size() > maxSize)
        {
            maxSize = (int)contours.at((unsigned long)i).size();
            id = i;
        }
    }

    // Draw filled contour to obtain a mask with interior parts

    cv::Mat contourMask = cv::Mat::zeros(source.size(), CV_8UC1);
    drawContours(contourMask, contours, id, cv::Scalar(255), -1, 8, hierarchy, 0, cv::Point());

    // Sort contour in x/y directions to easily find min/max and next

    std::vector<cv::Point> cSortedX = contours.at((unsigned long)id);
    std::sort(cSortedX.begin(), cSortedX.end(), compareX);
    std::vector<cv::Point> cSortedY = contours.at((unsigned long)id);
    std::sort(cSortedY.begin(), cSortedY.end(), compareY);

    int minXId = 0;
    int maxXId = (int)(cSortedX.size() - 1);
    int minYId = 0;
    int maxYId = (int)(cSortedY.size() - 1);

    cv::Rect croppingMask;

    while ((minXId < maxXId) && (minYId < maxYId))
    {
        cv::Point min(cSortedX[minXId].x, cSortedY[minYId].y);
        cv::Point max(cSortedX[maxXId].x, cSortedY[maxYId].y);
        croppingMask = cv::Rect(min.x, min.y, max.x - min.x, max.y - min.y);

        // Out-codes: if one of them is set, the rectangle size has to be reduced at that border

        int ocTop = 0;
        int ocBottom = 0;
        int ocLeft = 0;
        int ocRight = 0;

        bool finished = checkInteriorExterior(contourMask, croppingMask, ocTop, ocBottom, ocLeft, ocRight);

        if (finished == true)
        {
            break;
        }

        // Reduce rectangle at border if necessary

        if (ocLeft)
        { ++minXId; }
        if (ocRight)
        { --maxXId; }
        if (ocTop)
        { ++minYId; }
        if (ocBottom)
        { --maxYId; }
    }
    source = source(croppingMask);
}
