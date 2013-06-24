package server;


import com.googlecode.javacpp.BytePointer; 
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.awt.image.BufferedImage; 
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;

import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;
import static com.googlecode.javacv.cpp.opencv_contrib.*;

public class FaceDetection {
	 
	// ------ Zmienne globalne 
final static opencv_objdetect.CvHaarClassifierCascade FaceCascade = 
	        new opencv_objdetect.CvHaarClassifierCascade(
	            cvLoad("resources/haarcascade_frontalface_default.xml"));

final static opencv_objdetect.CvHaarClassifierCascade EyesCascade = 
			new opencv_objdetect.CvHaarClassifierCascade(
				cvLoad("resources/haarcascade_eye.xml"));

final static String trainingDir = "/Users/Szu/Desktop/Praca Magisterska/twarze2";
final static String outPath = "/Users/Szu/Desktop/";

static FaceRecognizer faceRecognizer;
final static CanvasFrame canvas = new CanvasFrame("My Image", 1);
final static CanvasFrame canvas2 = new CanvasFrame("My Image2", 1);
static ArrayList<IplImage> snapshots = new  ArrayList<IplImage>();
static ArrayList<Integer> predictions = new ArrayList<Integer>();
int Frame=0;
static IplImage Background;

public String compare(byte[] imageData) throws IOException {
		
		File root = new File(trainingDir);
		
        FilenameFilter pngFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        };

        File[] imageFiles = root.listFiles(pngFilter);

        MatVector images = new MatVector(imageFiles.length);

        int[] labels = new int[imageFiles.length];

        int counter = 0;
        int label;

       

        IplImage testImage = cvDecodeImage(cvMat(1, imageData.length,CV_8UC1, new BytePointer(imageData)));
        
        IplImage greyTestImage = IplImage.create(testImage.width(), testImage.height(), IPL_DEPTH_8U, 1);
         cvCvtColor(testImage, greyTestImage, CV_BGR2GRAY);
         
         
        cvNormalize(greyTestImage, greyTestImage, 0, 255, CV_MINMAX,null);
        cvEqualizeHist(greyTestImage,greyTestImage);
        IplImage img;
        IplImage grayImg; 
        for (File image : imageFiles) {
            img = cvLoadImage(image.getAbsolutePath());
            
            label = Integer.parseInt(image.getName().split("\\-")[0]);

            grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);
   
            cvNormalize(grayImg, grayImg, 0, 255, CV_MINMAX,null);
            cvEqualizeHist(grayImg,grayImg);
            
            images.put(counter, grayImg);

            labels[counter] = label;

            counter++;
        }
        
//       FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
//       FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
         FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();
         
         faceRecognizer.train(images, labels);
 
	    int predictedLabel = faceRecognizer.predict(greyTestImage);
		
	    return Integer.toString(predictedLabel);
	}
 
private static void training( ) throws IOException {
		
		File root = new File(trainingDir);
		
        FilenameFilter pngFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        };

        File[] imageFiles = root.listFiles(pngFilter);

        MatVector images = new MatVector(imageFiles.length);

        int[] labels = new int[imageFiles.length];

        int counter = 0;
        int label;
 
        IplImage img;
        IplImage grayImg;  
        for (File image : imageFiles) {
            img = cvLoadImage(image.getAbsolutePath());
            
            label = Integer.parseInt(image.getName().split("\\-")[0]);
            grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);
           //
            cvCvtColor(img, grayImg, CV_BGR2GRAY);
          
            cvNormalize(grayImg, grayImg, 0, 255, CV_MINMAX,null);
            cvEqualizeHist(grayImg,grayImg);
//            canvas2.showImage(img);
            images.put(counter, grayImg); 
            labels[counter] = label;
            counter++;
        }
        
         faceRecognizer = createFisherFaceRecognizer();
//         faceRecognizer = createEigenFaceRecognizer();
//         faceRecognizer = createLBPHFaceRecognizer();
         
         faceRecognizer.train(images, labels);
       
	   
	}

private static Integer compareFromImage(IplImage imageData) throws IOException {
    IplImage testImage = imageData; 
    IplImage greyTestImage = IplImage.create(testImage.width(), testImage.height(), IPL_DEPTH_8U, 1);
     cvCvtColor(testImage, greyTestImage, CV_BGR2GRAY); 
    cvNormalize(greyTestImage, greyTestImage, 0, 255, CV_MINMAX,null);
    cvEqualizeHist(greyTestImage,greyTestImage);
    int predictedLabel = faceRecognizer.predict(greyTestImage);
	System.out.println(predictedLabel); 
    return predictedLabel;
}

public void detect(byte[] imageData) throws IOException { 
		 IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length,CV_8UC1, new BytePointer(imageData)));
		 
//			IplImage hsvImg = cvCreateImage(cvGetSize(originalImage), 8, 3);; 
//			cvCvtColor(originalImage, hsvImg, CV_BGR2HSV);
//			cvInRangeS(hsvImg, cvScalar(0, 58, 89,0),cvScalar(25, 173, 229,0),originalImage);
			
			canvas.showImage(originalImage);

		 opencv_core.IplImage grayImage = opencv_core.IplImage.create(
			        originalImage.width(),
			        originalImage.height(), 
			        opencv_core.IPL_DEPTH_8U, 1);
		 
		 cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
		 
		 
		 opencv_core.CvMemStorage storage = opencv_core.CvMemStorage.create();
		 

		
			  
// Faza pierwsza - wykrycie oczu
if(detectEyes(grayImage))
{
	System.out.println("Wykrylem oczy");
//Faza druga - usuniecie tla i wykrycie twarzy


		
		opencv_core.CvSeq faces = cvHaarDetectObjects(
		        grayImage, FaceCascade, storage, 1.1, 1, 0);
		if(!faces.isNull())
		 {
			 for(int i=0;i<faces.sizeof();i++)
		     {
		      opencv_core.CvRect r = new opencv_core.CvRect(cvGetSeqElem(faces, i));
		      if(!r.isNull())
		      {
		    	  if(r.width()>50 && r.height()>50)
		    	  {
		    	  CvRect face = new CvRect(r.x(),r.y(),r.width(),r.height());
		    	  cvSetImageROI(originalImage, face);
		    	  IplImage cropped = cvCreateImage(cvGetSize(originalImage), originalImage.depth(), originalImage.nChannels());
		    	  cvCopy(originalImage, cropped); 
		    	  
		    	  //resize
		    	  IplImage resized = IplImage.create(320, 240, cropped.depth(),cropped.nChannels());
		    	  cvResize(cropped,resized,  CV_INTER_AREA); 
		    	  
		    	  cvRectangle(originalImage, cvPoint(r.x(), r.y()),
			      cvPoint(r.x() + r.width(), r.y() + r.height()), 
			      opencv_core.CvScalar.YELLOW, 1, CV_AA, 0);
		    	  //canvas 
//		    	  canvas.showImage(resized);
		    	  
		    	  // CurrentFrame 
		    	  snapshots.add(resized);
		    	  Frame++;
//		    	  cvSaveImage(outPath + "6-jon_doe_"+Frame+".png", resized);
		    	  }
		    	  
		       }
		    } 
		 }
	}
}

	public int mostFrequently(ArrayList<Integer> lista)
	{
		ArrayList<Integer> czestotliwosc = new ArrayList<Integer>() ;
		for(int i=0;i<7;i++)
			czestotliwosc.add( Collections.frequency(lista, i));  
		 
		return czestotliwosc.indexOf(Collections.max(czestotliwosc));
	}
	
	public Boolean detectEyes(IplImage grayImage)
{
		opencv_core.CvMemStorage storage = opencv_core.CvMemStorage.create();
		 
		CvSeq eyes = cvHaarDetectObjects(
				grayImage,
				EyesCascade,
				storage,
				1.5,
				3,
				0);
		
		if(eyes.sizeof()>0)
    	 return true;
		else
		 return false;
}
	
	public int prediction() throws IOException{
		training();
		 System.out.println("Liczba znalezionych twarzy:" + Frame);
		 for (IplImage image : snapshots) { 
		predictions.add(compareFromImage(image));
		 }
		 int wynik = mostFrequently(predictions);
		 System.out.println("uzytkownik numer:" +mostFrequently(predictions));
		 snapshots.clear();
		 predictions.clear();
		 Frame=0;
		 return wynik;
	}
}