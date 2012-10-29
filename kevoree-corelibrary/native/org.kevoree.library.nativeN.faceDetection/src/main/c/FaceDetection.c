#include "FaceDetection.h"

extern void faceDetected(void *input);

#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>
#include <stdio.h>

using namespace std;
using namespace cv;


CvCapture* capture = 0;
Mat frame, frameCopy, image;
const String scaleOpt = "--scale=";
size_t scaleOptLen = scaleOpt.length();
const String cascadeOpt = "--cascade=";
size_t cascadeOptLen = cascadeOpt.length();
const String nestedCascadeOpt = "--nested-cascade";
size_t nestedCascadeOptLen = nestedCascadeOpt.length();
String inputName;
string cascadeName = "";
string nestedCascadeName ="";




void detectAndDraw( Mat& img, CascadeClassifier& cascade, CascadeClassifier& nestedCascade, double scale);

/*@Start*/
int start()
{
	fprintf(stderr,"Component starting \n");


 // get Ressources
  const char *cascadeName_c =  getRessource("haarcascades/haarcascade_frontalface_alt.xml");
  const char * nestedCascadeName_c = getRessource("haarcascades/haarcascade_eye_tree_eyeglasses.xml");

   cascadeName  = string(cascadeName_c);
   nestedCascadeName  = string(nestedCascadeName_c);

      cout <<  cascadeName << endl;
      cout <<  nestedCascadeName << endl;

	 CascadeClassifier cascade, nestedCascade;
        double scale = 1;

        if( !cascade.load( cascadeName ) )
        {
            cerr << "ERROR: Could not load classifier cascade" << endl;
            cerr << "Usage: facedetect [--cascade=<cascade_path>]\n"
                "   [--nested-cascade[=nested_cascade_path]]\n"
                "   [--scale[=<image scale>\n"
                "   [filename|camera_index]\n" << endl ;
            return -1;
        }

        if( inputName.empty() || (isdigit(inputName.c_str()[0]) && inputName.c_str()[1] == '\0') )
        {
            capture = cvCaptureFromCAM( inputName.empty() ? 0 : inputName.c_str()[0] - '0' );
            int c = inputName.empty() ? 0 : inputName.c_str()[0] - '0' ;
            if(!capture) cout << "Capture from CAM " <<  c << " didn't work" << endl;
        }
        else if( inputName.size() )
        {
            image = imread( inputName, 1 );
            if( image.empty() )
            {
                capture = cvCaptureFromAVI( inputName.c_str() );
                if(!capture) cout << "Capture from AVI didn't work" << endl;
            }
        }
        else
        {
            image = imread( "lena.jpg", 1 );
            if(image.empty()) cout << "Couldn't read lena.jpg" << endl;
        }

        cvNamedWindow( "result", 1 );

        if( capture )
        {
            cout << "In capture ..." << endl;
            for(;;)
            {
                IplImage* iplImg = cvQueryFrame( capture );
                frame = iplImg;
                if( frame.empty() )
                    break;
                if( iplImg->origin == IPL_ORIGIN_TL )
                    frame.copyTo( frameCopy );
                else
                    flip( frame, frameCopy, 0 );

                detectAndDraw( frameCopy, cascade, nestedCascade, scale );

                if( waitKey( 10 ) >= 0 )
                    goto _cleanup_;
            }

            waitKey(0);

    _cleanup_:
            cvReleaseCapture( &capture );
        }
        else
        {
            cout << "In image read" << endl;
            if( !image.empty() )
            {
                detectAndDraw( image, cascade, nestedCascade, scale );
                waitKey(0);
            }
            else if( !inputName.empty() )
            {
                /* assume it is a text file containing the
                list of the image filenames to be processed - one per line */
                FILE* f = fopen( inputName.c_str(), "rt" );
                if( f )
                {
                    char buf[1000+1];
                    while( fgets( buf, 1000, f ) )
                    {
                        int len = (int)strlen(buf), c;
                        while( len > 0 && isspace(buf[len-1]) )
                            len--;
                        buf[len] = '\0';
                        cout << "file " << buf << endl;
                        image = imread( buf, 1 );
                        if( !image.empty() )
                        {
                            detectAndDraw( image, cascade, nestedCascade, scale );
                            c = waitKey(0);
                            if( c == 27 || c == 'q' || c == 'Q' )
                                break;
                        }
                        else
                        {
                        	cerr << "Aw snap, couldn't read image " << buf << endl;
                        }
                    }
                    fclose(f);
                }
            }
        }


return 0;
}

/*@Stop */
int stop()
{
    fprintf(stderr,"Component stoping \n");

    cvDestroyWindow("result");
return 0;
}

/*@Update */
int update()
{
    fprintf(stderr,"Component updating \n");
 return 0;
}
   int last=0;
    char test[512];
void detectAndDraw( Mat& img,CascadeClassifier& cascade, CascadeClassifier& nestedCascade,  double scale)
{
    int i = 0;
    double t = 0;
    vector<Rect> faces;
    const static Scalar colors[] =  { CV_RGB(0,0,255),
        CV_RGB(0,128,255),
        CV_RGB(0,255,255),
        CV_RGB(0,255,0),
        CV_RGB(255,128,0),
        CV_RGB(255,255,0),
        CV_RGB(255,0,0),
        CV_RGB(255,0,255)} ;
    Mat gray, smallImg( cvRound (img.rows/scale), cvRound(img.cols/scale), CV_8UC1 );

    cvtColor( img, gray, CV_BGR2GRAY );
    resize( gray, smallImg, smallImg.size(), 0, 0, INTER_LINEAR );
    equalizeHist( smallImg, smallImg );

    t = (double)cvGetTickCount();
    cascade.detectMultiScale( smallImg, faces,
        1.1, 2, 0
        //|CV_HAAR_FIND_BIGGEST_OBJECT
        //|CV_HAAR_DO_ROUGH_SEARCH
        |CV_HAAR_SCALE_IMAGE
        ,
        Size(30, 30) );
    t = (double)cvGetTickCount() - t;


   // sprintf(test,"detection time = %g ms\n",t/((double)cvGetTickFrequency()*1000.));

      sprintf(test," Faces %d \n",faces.size());

        if(last != faces.size())
        {
             faceDetected((char*)&test);
             last = faces.size();
        }



    for( vector<Rect>::const_iterator r = faces.begin(); r != faces.end(); r++, i++ )
    {
        Mat smallImgROI;
        vector<Rect> nestedObjects;
        Point center;
        Scalar color = colors[i%8];
        int radius;
        center.x = cvRound((r->x + r->width*0.5)*scale);
        center.y = cvRound((r->y + r->height*0.5)*scale);
        radius = cvRound((r->width + r->height)*0.25*scale);
        circle( img, center, radius, color, 3, 8, 0 );
        if( nestedCascade.empty() )
            continue;
        smallImgROI = smallImg(*r);
        nestedCascade.detectMultiScale( smallImgROI, nestedObjects,
            1.1, 2, 0
            //|CV_HAAR_FIND_BIGGEST_OBJECT
            //|CV_HAAR_DO_ROUGH_SEARCH
            //|CV_HAAR_DO_CANNY_PRUNING
            |CV_HAAR_SCALE_IMAGE
            ,
            Size(30, 30) );

        for( vector<Rect>::const_iterator nr = nestedObjects.begin(); nr != nestedObjects.end(); nr++ )
        {
            center.x = cvRound((r->x + nr->x + nr->width*0.5)*scale);
            center.y = cvRound((r->y + nr->y + nr->height*0.5)*scale);
            radius = cvRound((nr->width + nr->height)*0.25*scale);
            circle( img, center, radius, color, 3, 8, 0 );
        }


    }
    cv::imshow( "result", img );
}