#include "com_example_panorama_NativePanorama.h"
#include "opencv2/core.hpp"
#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core/base.hpp>
#include "opencv2/imgproc.hpp"
#include <string>
#include <iostream>

#include "opencv2/stitching.hpp"
#include "opencv2/imgcodecs.hpp"

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

template <typename T>
std::string NumberToString ( T Number )
{
    std::ostringstream ss;
    ss << Number;
    return ss.str();
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
        float scale = 1000.0f / curimage.rows;
        resize(curimage, curimage, Size(scale * curimage.rows, scale * curimage.cols));

        // Save as PNG and load
        vector<int> compression_params;
        compression_params.push_back(CV_IMWRITE_PNG_COMPRESSION);
        compression_params.push_back(9);

        /*std::string filepath = "/storage/sdcard0/saved_images_panorama/";
        std::string fileName = "outputStitchingv";
        fileName += NumberToString(k);
        fileName += ".png";

        filepath += fileName;
        imwrite("test.png", curimage, compression_params);*/

        imgVec.push_back(curimage);

      }
      Mat & result  = *(Mat*) outputAddress;

      Stitcher stitcher = Stitcher::createDefault(true);

      detail::BestOf2NearestMatcher *matcher = new detail::BestOf2NearestMatcher(false, 0.5f);
      stitcher.setFeaturesMatcher(matcher);
      stitcher.setBundleAdjuster(new detail::BundleAdjusterRay());
      stitcher.setSeamFinder(new detail::NoSeamFinder);
      //stitcher.setExposureCompensator(new detail::NoExposureCompensator());//exposure compensation
      stitcher.setExposureCompensator(new detail::GainCompensator());
      stitcher.setBlender(new detail::FeatherBlender());
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
      // Release the jlong array
      env->ReleaseLongArrayElements(imageAddressArray, imgAddressArr ,0);
  }
