package com.example.sudokusolver.imagerec

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat

class PuzzleScanner(x: Mat, context: Context) {
    private val originalMat: Mat
    private val context: Context
    private var puzzleFinder: PuzzleFinder? = null
        private get() {
            if (field == null) {
                field = PuzzleFinder(originalMat)
            }
            return field
        }

    @get:Throws(PuzzleNotFoundException::class)
    private var puzzleExtractor: PuzzleExtractor? = null
        private get() {
            if (field == null) {
                val finder = puzzleFinder!!
                field = PuzzleExtractor(
                    finder.getThresholdMat()!!,
                    finder.getLargestBlobMat()!!,
                    finder.findOutLine()
                )
            }
            return field
        }

    private fun initOpencv() {
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCv did not init properly")
        }
    }

    @Throws(PuzzleNotFoundException::class)
    fun extractPuzzle(): Bitmap {
        val extractedPuzzleMat =
            puzzleExtractor!!.getExtractedPuzzleMat() // first it will call getPuzzleExtractor() which returns an object of puzzleextractor class
        // then getExtractedPuzzleMat() for the function is called;
        return convertMatToBitMap(extractedPuzzleMat)
    }

    fun convertMatToBitMap(matToConvert: Mat?): Bitmap {
        val bitmap =
            Bitmap.createBitmap(matToConvert!!.cols(), matToConvert.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matToConvert, bitmap)
        return bitmap
    }

    companion object {
        private const val TAG = "OCVSample::Activity"
    }

    init {
        initOpencv()
        this.context = context
        originalMat = x
    }
}