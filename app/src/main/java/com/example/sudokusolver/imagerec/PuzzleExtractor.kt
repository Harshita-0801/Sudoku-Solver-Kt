package com.example.sudokusolver.imagerec

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters
import java.util.*

internal class PuzzleExtractor(
    private val thresholdMat: Mat,
    private val largestBlobMat: Mat,
    private val puzzleOutline: PuzzleOutLine
) {
    private var extractedPuzzleMat: Mat? = null
    fun getExtractedPuzzleMat(): Mat? {
        if (extractedPuzzleMat == null) generateExtractedPuzzleMat()
        return extractedPuzzleMat
    }

    private fun generateExtractedPuzzleMat() {
        extractedPuzzleMat = thresholdMat.clone()
        RemovePuzzleOutline()
        CorrectPerspective()
    }

    private fun CorrectPerspective() {
        val size = puzzleOutline.size
        val outputMat = Mat(size.toInt(), size.toInt(), CvType.CV_8U)
        val source: MutableList<Point?> = ArrayList()
        source.add(puzzleOutline.bottomLeft)
        source.add(puzzleOutline.topLeft)
        source.add(puzzleOutline.topRight)
        source.add(puzzleOutline.bottomRight)
        val startM = Converters.vector_Point2f_to_Mat(source)
        val bottomLeft = Point(0.0, 0.0)
        val topLeft = Point(0.0, size)
        val topRight = Point(size, size)
        val bottomRight = Point(size, 0.0)
        val dest: MutableList<Point> = ArrayList()
        dest.add(bottomLeft)
        dest.add(topLeft)
        dest.add(topRight)
        dest.add(bottomRight)
        val endM = Converters.vector_Point2f_to_Mat(dest)
        val perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM)
        Imgproc.warpPerspective(
            extractedPuzzleMat,
            outputMat,
            perspectiveTransform,
            Size(size, size),
            Imgproc.INTER_CUBIC
        )
        extractedPuzzleMat = outputMat
    }

    private fun RemovePuzzleOutline() {
        val height = thresholdMat.height()
        val width = thresholdMat.width()
        for (y in 0 until height) {
            val row = largestBlobMat.row(y)
            for (x in 0 until width) {
                val value = row[0, x]
                val currentPoint = Point(x.toDouble(), y.toDouble())
                if (value[0] > Constants.THRESHOLD) {
                    val blackMask = Mat(height + 2, width + 2, CvType.CV_8U, Scalar(0.0, 0.0, 0.0))
                    Imgproc.floodFill(extractedPuzzleMat, blackMask, currentPoint, Constants.BLACK)
                    return
                }
            }
        }
    }
}