package com.example.sudokusolver

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import com.example.sudokusolver.imagerec.PortraitCameraView
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import com.example.sudokusolver.ShowCameraActivity
import org.opencv.android.OpenCVLoader
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import com.example.sudokusolver.imagerec.PuzzleScanner
import org.opencv.imgproc.Imgproc
import com.example.sudokusolver.imagerec.SudokuChecker
import org.opencv.android.Utils
import org.opencv.core.*
import java.io.*
import java.lang.Exception
import java.util.*

class ShowCameraActivity : AppCompatActivity(), CvCameraViewListener2 {
    //Dialog
    var pd: ProgressDialog? = null

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    //private CameraBridgeViewBase mOpenCvCameraView;
    private var mOpenCvCameraView: PortraitCameraView? = null
    private val mIsJavaCamera = true
    private val mItemSwitchCamera: MenuItem? = null
    private var mRgba: Mat? = null
    private var mIntermediateMat: Mat? = null
    private var mGray: Mat? = null
    var hierarchy: Mat? = null
    var cropped: Mat? = null
    var output: Bitmap? = null
    var matBW: Mat? = null
    var tessBaseApi: TessBaseAPI? = null
    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully !!!!!!!!!!!!")
                    mOpenCvCameraView!!.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_show_camera)
        mOpenCvCameraView = findViewById(R.id.camera)
        mOpenCvCameraView!!.setVisibility(SurfaceView.VISIBLE)
        mOpenCvCameraView!!.setCvCameraViewListener(this)
        pd = ProgressDialog(this)
        pd!!.setTitle("Processing Image")
        pd!!.setMessage("Loading.....")
        pd!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
    }

    public override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRgba = Mat(height, width, CvType.CV_8UC4)
        mIntermediateMat = Mat(height, width, CvType.CV_8UC4)
        mGray = Mat(height, width, CvType.CV_8UC1)
        hierarchy = Mat()
    }

    override fun onCameraViewStopped() {
        mRgba!!.release()
        mGray!!.release()
        mIntermediateMat!!.release()
        hierarchy!!.release()
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        val displayMat = inputFrame.rgba()
        output = Bitmap.createBitmap(displayMat.cols(), displayMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(displayMat, output)
        return displayMat
    }
    // so when we clicked the picture it was stored in output BitMap ( here we converted the mat to bitmap)

    private fun copyTessDataFiles(path: String) {
        try {
            val fileList = assets.list(path)
            for (fileName in fileList!!) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                val pathToDataFile = DATA_PATH + path + "/" + fileName
                Log.d(TAG, fileName)
                if (!File(pathToDataFile).exists()) {
                    Log.d(TAG, "Writing!!!!!!!")
                    val `in` = assets.open("$path/$fileName")
                    val out: OutputStream = FileOutputStream(pathToDataFile)

                    // Transfer bytes from in to out
                    val buf = ByteArray(1024)
                    var len: Int
                    while (`in`.read(buf).also { len = it } > 0) {
                        out.write(buf, 0, len)
                    }
                    `in`.close()
                    out.close()
                    Log.d(TAG, "Copied $fileName to tessdata")
                } else {
                    Log.d(TAG, "already copied")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Unable to copy files to tessdata $e")
        }
    }

    fun capture(v: View?) {
        pd!!.show()
        mOpenCvCameraView!!.visibility = View.GONE
        val iv = findViewById<ImageView>(R.id.solve_img)
        iv.visibility = View.VISIBLE


        //initialize the TessBase
        tessBaseApi = TessBaseAPI()
        val lang = "eng"
        val DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tesseract/"
        val dir = File(DATA_PATH + "tessdata/")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        copyTessDataFiles("tessdata")
        tessBaseApi!!.init(DATA_PATH, lang)
        tessBaseApi!!.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK
        tessBaseApi!!.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "123456789")
        tessBaseApi!!.setVariable("classify_bin_numeric_mode", "1")
        val outputtemp = Mat()
        Utils.bitmapToMat(output, outputtemp)
        try {
            val xyz = PuzzleScanner(outputtemp, this)


            //output = xyz.getThreshold();
            //output = xyz.getLargestBlob();
            //output = xyz.getHoughLines();
            //output = xyz.getOutLine();
            output = xyz.extractPuzzle()
        } catch (ex: Exception) {
            Log.e(null, "Error extracting puzzle", ex)
        }


//        iv.setImageBitmap(output);
//
//        findViewById(R.id.captureButton).setVisibility(View.INVISIBLE);
//        findViewById(R.id.SaveImage).setVisibility(View.VISIBLE);
        val output2 = Mat()
        Utils.bitmapToMat(output, output2) // output2 mat that contains the extracted puzzle
        val SUDOKU_SIZE = 9
        val IMAGE_WIDTH = output2.width()
        val IMAGE_HEIGHT = output2.height()
        Log.d(TAG, IMAGE_WIDTH.toString())
        Log.d(TAG, IMAGE_HEIGHT.toString())
        //double PADDING = IMAGE_WIDTH/25;
        val HSIZE = IMAGE_HEIGHT / 9
        val WSIZE = IMAGE_WIDTH / 9
        Log.d(TAG, "Square height$HSIZE")
        Log.d(TAG, "Square width$WSIZE")
        val sudos = Array(SUDOKU_SIZE) { IntArray(SUDOKU_SIZE) }
        var count = 0
        // Divide the image to 81 small grid and do the digit recognition
        var y = 0
        var iy = 0
        while (y < IMAGE_HEIGHT - HSIZE) {
            var x = 0
            var ix = 0
            while (x < IMAGE_WIDTH - WSIZE) {
                count++
                sudos[iy][ix] = 0

                //for correct 9*9 matrix block use this code
                val cx = x
                val cy = y
                val p1 = Point(cx.toDouble(), cy.toDouble())
                val p2 = Point((cx + WSIZE).toDouble(), (cy + HSIZE).toDouble())
                // Log.d(TAG,"Point :"+p1+" -> "+p2);
                val R = Rect(cx, cy, WSIZE, HSIZE)
                val digit_cropped = Mat(output2, R)
                Imgproc.GaussianBlur(digit_cropped, digit_cropped, Size(5.0, 5.0), 0.0)
                Imgproc.rectangle(output2, p1, p2, Scalar(0.0, 0.0, 0.0))
                val digit_bitmap = Bitmap.createBitmap(
                    digit_cropped.cols(),
                    digit_cropped.rows(),
                    Bitmap.Config.ARGB_8888
                )
                Utils.matToBitmap(digit_cropped, digit_bitmap)
                tessBaseApi!!.setImage(digit_bitmap)
                val recognizedText = tessBaseApi!!.utF8Text
                if (recognizedText.length == 1) {
                    sudos[iy][ix] = Integer.valueOf(recognizedText)
                }
                // Imgproc.putText(output, recognizedText, new Point(cx, cy), 1, 3.0f, new Scalar(0));
                tessBaseApi!!.clear()
                x += WSIZE
                ix++
            }
            Log.i("testing", "" + Arrays.toString(sudos[iy]))
            y += HSIZE
            iy++
        }
        Log.d(TAG, count.toString())
        //Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2RGBA);
        tessBaseApi!!.end()
        val test_sudo = Arrays.copyOf(sudos, sudos.size)
        setContentView(R.layout.camera_sudoku)
        val gridLayout = findViewById<GridLayout>(R.id.sudokuGr)
        //get screen size in pixels to adjust size of sudoku cells
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val dimensions = size.x / 11
        SudokuChecker.initGrid(this, gridLayout, dimensions, test_sudo)
        val solveButton = findViewById<Button>(R.id.solve)
        solveButton.setOnClickListener(View.OnClickListener {
            val temp = SudokuChecker.getCellValues()
            if (!SudokuChecker.solution) {
                Toast.makeText(applicationContext, "Solution does not exist", Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }
            Toast.makeText(applicationContext, "Solution Found", Toast.LENGTH_SHORT).show()
            val result = SudokuChecker.updateSolution()
            setContentView(R.layout.activity_show_camera)
            val iv = findViewById<ImageView>(R.id.solve_img)
            iv.visibility = View.VISIBLE
            //Print the result to screen
            var y = 0
            var iy = 0
            while (y < IMAGE_HEIGHT - HSIZE) {
                var x = 0
                var ix = 0
                while (x < IMAGE_WIDTH - WSIZE) {
                    if (temp[iy][ix] == 0 || temp[iy][ix] != sudos[iy][ix]) {
                        val cx = x + WSIZE / 4
                        val cy = y + HSIZE
                        val p = Point(cx.toDouble(), cy.toDouble())
                        Imgproc.putText(
                            output2,
                            result[iy][ix].toString() + "",
                            p,
                            2,
                            2.0,
                            Scalar(0.0, 255.0, 238.0),
                            2
                        )
                    }
                    x += WSIZE
                    ix++
                }
                Log.i("Solved", "" + Arrays.toString(result[iy]))
                y += HSIZE
                iy++
            }


            // Display the image
            val b = Bitmap.createBitmap(output2.cols(), output2.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(output2, b)
            iv.setImageBitmap(b)
            findViewById<View>(R.id.captureButton).visibility = View.INVISIBLE
            findViewById<View>(R.id.SaveImage).visibility = View.VISIBLE
            output2.release()
            outputtemp.release()
        })
        pd!!.dismiss()
    }

    //code to save image
    fun save(v: View?) {
        val iv = findViewById<ImageView>(R.id.solve_img)
        //to get the image from the ImageView (say iv)
        val draw = iv.drawable as BitmapDrawable
        val bitmap = draw.bitmap
        Log.d(TAG, " SAVING")
        var outStream: FileOutputStream? = null
        val sdCard = Environment.getExternalStorageDirectory()
        val dir = File(sdCard.absolutePath)
        dir.mkdirs()
        val fileName = String.format("%d.jpg", System.currentTimeMillis())
        val outFile = File(dir, fileName)
        try {
            Log.d(TAG, "try1")
            outStream = FileOutputStream(outFile)
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "bedagarg1")
            e.printStackTrace()
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        try {
            Log.d(TAG, "try2")
            outStream!!.flush()
        } catch (e: IOException) {
            Log.d(TAG, "bedagarg2")
            e.printStackTrace()
        }
        try {
            Log.d(TAG, "try3")
            outStream!!.close()
        } catch (e: IOException) {
            Log.d(TAG, "bedagarg3")
            e.printStackTrace()
        }
        Toast.makeText(this, "Image Saved Sucessfully", Toast.LENGTH_SHORT).show()
    }

    companion object {
        // Used for logging success or failure messages
        private const val TAG = "OCVSample::Activity"
        private const val lang = "eng"
        private val DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tesseract/"
    }

    init {
        Log.i(TAG, "Instantiated new " + this.javaClass)
    }
}