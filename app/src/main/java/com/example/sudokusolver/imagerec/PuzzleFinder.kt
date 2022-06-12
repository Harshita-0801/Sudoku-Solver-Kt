package com.example.sudokusolver.imagerec

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*

internal class PuzzleFinder(private val originalMat: Mat) {
    private var greyMat: Mat? = null
    private var thresholdMat: Mat? = null
    private var largestBlobMat: Mat? = null
    private var houghLinesMat: Mat? = null
    private var outLineMat: Mat? = null
    fun getGreyMat(): Mat? {
        if (greyMat == null) {
            generateGreyMat()
        }
        return greyMat
    }

    private fun generateGreyMat() {
        greyMat = originalMat.clone()
        Imgproc.cvtColor(originalMat, greyMat, Imgproc.COLOR_RGB2GRAY)
    }

    fun getThresholdMat(): Mat? {
        if (thresholdMat == null) {
            generateThresholdMat()
        }
        return thresholdMat
    }

    private fun generateThresholdMat() {
        thresholdMat = getGreyMat()!!.clone()
        Imgproc.adaptiveThreshold(
            thresholdMat,
            thresholdMat,
            255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY,
            7,
            5.0
        )
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, Size(2.0, 2.0))
        Imgproc.erode(thresholdMat, thresholdMat, kernel)
        val kernelDil = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, Size(2.0, 2.0))
        Imgproc.dilate(thresholdMat, thresholdMat, kernelDil)
        Core.bitwise_not(thresholdMat, thresholdMat)
    }

    fun getLargestBlobMat(): Mat? {
        if (largestBlobMat == null) {
            generateLargestBlobMat()
        }
        return largestBlobMat
    }

    private fun generateLargestBlobMat() {
        largestBlobMat = getThresholdMat()!!.clone()
        val height = largestBlobMat!!.height()
        val width = largestBlobMat!!.width()
        var maxBlobOrigin: Point? = Point(0.0, 0.0)
        var maxBlobSize = 0
        val greyMask = Mat(height + 2, width + 2, CvType.CV_8U, Scalar(0.0, 0.0, 0.0))
        val blackMask = Mat(height + 2, width + 2, CvType.CV_8U, Scalar(0.0, 0.0, 0.0))
        for (y in 0 until height) {
            val row = largestBlobMat!!.row(y)
            for (x in 0 until width) {
                val value = row[0, x]
                val currentPoint = Point(x.toDouble(), y.toDouble())
                if (value[0] > Constants.THRESHOLD) {
                    val blobSize =
                        Imgproc.floodFill(largestBlobMat, greyMask, currentPoint, Constants.GREY)
                    if (blobSize > maxBlobSize) {
                        Imgproc.floodFill(largestBlobMat, blackMask, maxBlobOrigin, Constants.BLACK)
                        maxBlobOrigin = currentPoint
                        maxBlobSize = blobSize
                    } else {
                        Imgproc.floodFill(largestBlobMat, blackMask, currentPoint, Constants.BLACK)
                    }
                }
            }
        }
        val largeBlobMask = Mat(height + 2, width + 2, CvType.CV_8U, Constants.BLACK)
        Imgproc.floodFill(largestBlobMat, largeBlobMask, maxBlobOrigin, Constants.WHITE)
    }

    fun getHoughLinesMat(): Mat? {
        if (houghLinesMat == null) generateHoughLinesMat()
        return houghLinesMat
    }

    private fun generateHoughLinesMat() {
        houghLinesMat = getLargestBlobMat()!!.clone()
        val houghLines = houghLines
        for (line in houghLines) {
            Imgproc.line(houghLinesMat, line.origin, line.destination, Constants.GREY)
        }
    }
    //Need to think about the threshold as getting this correct is very important!

    //The Hough transform returns a series of lines in Polar format this is returned in the
    //form of a Mat where each row is a vector where row[0] is rho and row[1] is theta
    //See http://docs.opencv.org/2.4/doc/tutorials/imgproc/imgtrans/hough_lines/hough_lines.html
    //and http://stackoverflow.com/questions/7925698/android-opencv-drawing-hough-lines/7975315#7975315
    private val houghLines: List<Line>
        private get() {
            val linesMat = getLargestBlobMat()!!.clone()
            val largestBlobMat = getLargestBlobMat()
            val width = largestBlobMat!!.width()
            val height = largestBlobMat.height()

            //Need to think about the threshold as getting this correct is very important!
            Imgproc.HoughLines(largestBlobMat, linesMat, 1.toDouble(), Math.PI / 180, 400)

            //The Hough transform returns a series of lines in Polar format this is returned in the
            //form of a Mat where each row is a vector where row[0] is rho and row[1] is theta
            //See http://docs.opencv.org/2.4/doc/tutorials/imgproc/imgtrans/hough_lines/hough_lines.html
            //and http://stackoverflow.com/questions/7925698/android-opencv-drawing-hough-lines/7975315#7975315
            val houghLines: MutableList<Line> = ArrayList()
            val lines = linesMat.rows()
            for (x in 0 until lines) {
                val vec = linesMat[x, 0]
                val vector = Vector(vec[0], vec[1])
                val line = Line(vector, height, width)
                houghLines.add(line)
            }
            return houghLines
        }

    @Throws(PuzzleNotFoundException::class)
    fun findOutLine(): PuzzleOutLine {
        val location = PuzzleOutLine()
        val height = getLargestBlobMat()!!.height()
        val width = getLargestBlobMat()!!.width()
        var countHorizontalLines = 0
        var countVerticalLines = 0
        val houghLines = houghLines
        for (line in houghLines) {
            if (line.orientation === Orientation.horizontal) {
                countHorizontalLines++
                if (location.top == null) {
                    location.top = line
                    location.bottom = line
                    continue
                }
                if (line.angleFromXAxis > 6) continue
                if (line.angleFromXAxis < 1 && (line.minY < 5 || line.maxY > height - 5)) continue
                if (line.minY < location.bottom!!.minY) location.bottom = line
                if (line.maxY > location.top!!.maxY) location.top = line
            } else if (line.orientation === Orientation.vertical) {
                countVerticalLines++
                if (location.left == null) {
                    location.left = line
                    location.right = line
                    continue
                }
                if (line.angleFromXAxis < 84) continue
                if (line.angleFromXAxis > 89 && (line.minX < 5 || line.maxX > width - 5)) continue
                if (line.minX < location.left!!.minX) location.left = line
                if (line.maxX > location.right!!.maxX) location.right = line
            }
        }
        if (houghLines.size < 4) throw PuzzleNotFoundException("not enough possible edges found. Need at least 4 for a rectangle.")
        if (countHorizontalLines < 2) throw PuzzleNotFoundException("not enough horizontal edges found. Need at least 2 for a rectangle.")
        if (countVerticalLines < 2) throw PuzzleNotFoundException("not enough vertical edges found. Need at least 2 for a rectangle.")
        location.topLeft = location.top?.findIntersection(location.left!!)
        if (location.topLeft == null) throw PuzzleNotFoundException("Cannot find top left corner")
        location.topRight = location.top?.findIntersection(location.right!!)
        if (location.topRight == null) throw PuzzleNotFoundException("Cannot find top right corner")
        location.bottomLeft = location.bottom?.findIntersection(location.left!!)
        if (location.topLeft == null) throw PuzzleNotFoundException("Cannot find bottom left corner")
        location.bottomRight = location.bottom?.findIntersection(location.right!!)
        if (location.topLeft == null) throw PuzzleNotFoundException("Cannot find bottom right corner")
        return location
    }

    @Throws(PuzzleNotFoundException::class)
    fun getOutLineMat(): Mat? {
        if (outLineMat == null) generateOutlineMat()
        return outLineMat
    }

    @Throws(PuzzleNotFoundException::class)
    private fun generateOutlineMat() {
        outLineMat = getGreyMat()!!.clone()
        val location = findOutLine()
        Imgproc.drawMarker(
            outLineMat,
            location.topLeft,
            Constants.GREY,
            Imgproc.MARKER_TILTED_CROSS,
            30,
            10,
            8
        )
        Imgproc.drawMarker(
            outLineMat,
            location.topRight,
            Constants.GREY,
            Imgproc.MARKER_TILTED_CROSS,
            30,
            10,
            8
        )
        Imgproc.drawMarker(
            outLineMat,
            location.bottomLeft,
            Constants.GREY,
            Imgproc.MARKER_TILTED_CROSS,
            30,
            10,
            8
        )
        Imgproc.drawMarker(
            outLineMat,
            location.bottomRight,
            Constants.GREY,
            Imgproc.MARKER_TILTED_CROSS,
            30,
            10,
            8
        )
        Imgproc.line(outLineMat, location.top?.origin, location.top?.destination, Constants.GREY)
        Imgproc.line(
            outLineMat,
            location.bottom?.origin,
            location.bottom?.destination,
            Constants.DARK_GREY
        )
        Imgproc.line(outLineMat, location.left?.origin, location.left?.destination, Constants.GREY)
        Imgproc.line(
            outLineMat,
            location.right?.origin,
            location.right?.destination,
            Constants.DARK_GREY
        )
    }
}