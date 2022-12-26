package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;

//import org.opencv.features2d.DescriptorExtractor;
//import org.opencv.features2d.FeatureDetector;

public class AsyncCompareTask extends AsyncTask<Void, Void, Void> {
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java4");
    }

    public interface AsyncResponse {
        void processFinish(Bitmap result, String fileID);
    }

    public AsyncResponse delegate = null;//Call back interface
    public String fileID;

    //Constants
    private static final String TAG = "EndaReco " + AsyncCompareTask.class.toString();

    //Attributes
    private Context context;

    private Bitmap bmpObjToRecognize, bmpScene, bmpMatchedScene;
    private double minDistance, maxDistance;
    private Scalar RED = new Scalar(255, 0, 0);
    private Scalar GREEN = new Scalar(0, 255, 0);

    private int matchesFound;

    private SIFT sift = SIFT.create();

    //FeatureDetector detector;
    //DescriptorExtractor descriptor;
    DescriptorMatcher matcher;
    Mat descriptors2, descriptors1;
    Mat img1, img2;
    MatOfKeyPoint keypoints1, keypoints2;

    int goodMatches = 0;

    //Constructors
    public AsyncCompareTask(Context context, AsyncResponse asyncResponse) {
        this.delegate = asyncResponse;
        this.context = context;
        this.minDistance = 100;
        this.maxDistance = 0;
        this.matchesFound = 0;
    }

    //Properties

    public Bitmap getObjToRecognize() {
        return bmpObjToRecognize;
    }

    public void setObjToRecognize(Bitmap bmpObjToRecognize) {
        this.bmpObjToRecognize = bmpObjToRecognize;//this.rotateBitmap(bmpObjToRecognize, 90);
    }

    public Bitmap getScene() {
        return bmpScene;
    }

    public void setScene(Bitmap bmpScene) {
        this.bmpScene = bmpScene;//this.rotateBitmap(bmpScene, 90);
        //this.inputImage = bmpScene;//this.rotateBitmap(bmpScene, 90);
    }

    public double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Void doInBackground(Void... arg0) {
        // TODO Auto-generated method stub
        //sift();
        runSimCheck();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        try {
            //            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
            //                    context);
            //            alertDialog.setTitle("Result");
            //            alertDialog.setCancelable(true);
            //            LayoutInflater factory = LayoutInflater.from(context);
            //            final View view = factory.inflate(R.layout.view_image_result, null);
            //            ImageView matchedImages = (ImageView) view
            //                    .findViewById(R.id.finalImage);
            //            //matchedImages.setImageBitmap(bmpMatchedScene);
            //            matchedImages.setImageBitmap(bmpMatchedScene);
            //            matchedImages.invalidate();
            //            TextView message = (TextView) view.findViewById(R.id.message);
            //            message.setText("!!!----");
            //            alertDialog.setView(view);
            //
            //            alertDialog.show();
            this.delegate.processFinish(bmpMatchedScene, "matches : " + this.goodMatches);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void runSimCheck() {
        sift();
    }

    public void sift() {
        Mat rgba = new Mat();
        Utils.bitmapToMat(bmpObjToRecognize, rgba);

        Mat rgba2 = new Mat();
        Utils.bitmapToMat(bmpScene, rgba2);

        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);

        // image 1
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        Mat descriptor = new Mat();
        Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_RGBA2GRAY);
        sift.detect(rgba, keyPoints);
        sift.compute(rgba, keyPoints, descriptor);

        // image 2
        MatOfKeyPoint keyPoints_2 = new MatOfKeyPoint();
        Mat descriptor_2 = new Mat();
        Imgproc.cvtColor(rgba2, rgba2, Imgproc.COLOR_RGBA2GRAY);
        sift.detect(rgba2, keyPoints_2);
        sift.compute(rgba2, keyPoints_2, descriptor_2);

        MatOfDMatch matches = new MatOfDMatch();
        descriptorMatcher.match(descriptor, descriptor_2, matches);

        Log.d("Matches - ", ""+matches.toArray().length);

        this.goodMatches = matches.toArray().length;

        // more
        Mat output = new Mat();
        Features2d.drawMatches(rgba, keyPoints, rgba2, keyPoints_2, matches, output);
        bmpMatchedScene = Bitmap.createBitmap(output.cols(), output.rows(), Bitmap.Config.RGB_565);//need to save bitmap

        Utils.matToBitmap(output, bmpMatchedScene);
    }

    /*
    public void orb() {
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        img1 = new Mat();
        Utils.bitmapToMat(bmpScene, img1);

        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGB2GRAY);
        img1.convertTo(img1, 0); //converting the image to match with the type of the cameras image
        descriptors1 = new Mat();
        keypoints1 = new MatOfKeyPoint();
        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);

        img2 = new Mat();
        Utils.bitmapToMat(bmpObjToRecognize, img2);

        Mat img3 = recognize(img2);
        bmpMatchedScene = Bitmap.createBitmap(img3.cols(), img3.rows(), Bitmap.Config.RGB_565);//need to save bitmap
        Utils.matToBitmap(img3, bmpMatchedScene);

    }

    public Mat recognize(Mat aInputFrame) {

        Imgproc.cvtColor(aInputFrame, aInputFrame, Imgproc.COLOR_RGB2GRAY);
        descriptors2 = new Mat();
        keypoints2 = new MatOfKeyPoint();
        detector.detect(aInputFrame, keypoints2);
        descriptor.compute(aInputFrame, keypoints2, descriptors2);

        // Matching
        MatOfDMatch matches = new MatOfDMatch();
        if (img1.type() == aInputFrame.type()) {
            matcher.match(descriptors1, descriptors2, matches);
        } else {
            return aInputFrame;
        }
        List<DMatch> matchesList = matches.toList();

        Double max_dist = 0.0;
        Double min_dist = 100.0;

        for (int i = 0; i < matchesList.size(); i++) {
            Double dist = (double) matchesList.get(i).distance;
            if (dist < min_dist)
                min_dist = dist;
            if (dist > max_dist)
                max_dist = dist;
        }

        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
        for (int i = 0; i < matchesList.size(); i++) {
            if (matchesList.get(i).distance <= (1.5 * min_dist))
                good_matches.addLast(matchesList.get(i));
        }

        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromList(good_matches);
        Mat outputImg = new Mat();
        MatOfByte drawnMatches = new MatOfByte();
        if (aInputFrame.empty() || aInputFrame.cols() < 1 || aInputFrame.rows() < 1) {
            return aInputFrame;
        }

        Features2d.drawMatches(img1, keypoints1, aInputFrame, keypoints2, goodMatches, outputImg, GREEN, RED, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
        Imgproc.resize(outputImg, outputImg, aInputFrame.size());

        this.goodMatches = good_matches.size();

        return outputImg;
    }

     */
}
