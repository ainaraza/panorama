#include "com_example_panorama_NativePanorama.h"
#include "opencv2/opencv.hpp"
#include "opencv2/stitching.hpp"
#include <android/log.h>
using namespace std;
using namespace cv;

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
        imgVec.push_back(curimage);
      }
      Mat & result  = *(Mat*) outputAddress;
      Stitcher stitcher = Stitcher::createDefault(0);
      stitcher.stitch(imgVec, result);
      // Release the jlong array
      env->ReleaseLongArrayElements(imageAddressArray, imgAddressArr ,0);

    /*  vector<Mat> imgVecPartial;
      vector<vector<Mat>> allImages(4, vector<Mat>());

    __android_log_print(ANDROID_LOG_INFO, "NATIVELOG", "Helloworld");
      int n_groups = 4;
      int n_in_group = 2;
      for(int i=0; i<n_groups; i++){
        Mat stitchedGroup;
        for(int j=0; j<n_in_group; j++){
            Mat& curimage = *(Mat*) imgAddressArr[2*i + j];
            float scale = 1000.0f / curimage.rows;
            resize(curimage, curimage, Size(scale * curimage.rows, scale * curimage.cols));
            allImages[i].push_back(curimage);
        }
        Stitcher s = Stitcher::createDefault(1);
        //s.stitch(allImages[i], stitchedGroup);
      }*/
  }
