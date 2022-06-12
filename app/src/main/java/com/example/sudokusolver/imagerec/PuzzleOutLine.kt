package com.example.sudokusolver.imagerec

import org.opencv.core.Point

internal class PuzzleOutLine {
    @JvmField
    var bottomLeft: Point? = null
    @JvmField
    var bottomRight: Point? = null
    @JvmField
    var topLeft: Point? = null
    @JvmField
    var topRight: Point? = null
    var top: Line? = null
    var bottom: Line? = null
    var left: Line? = null
    var right: Line? = null
    val size: Double
        get() {
            val height = height
            val width = width
            return if (height > width) height else width
        }
    private val height: Double
        private get() {
            var smallestY = Double.MAX_VALUE
            var largestY = Double.MIN_VALUE
            val points = arrayOf(bottomLeft, bottomRight, topLeft, topRight)
            for (i in points.indices) {
                if (points[i]!!.y < smallestY) smallestY = points[i]!!.y
                if (points[i]!!.y > largestY) largestY = points[i]!!.y
            }
            return largestY - smallestY
        }
    private val width: Double
        private get() {
            var smallestX = Double.MAX_VALUE
            var largestX = Double.MIN_VALUE
            val points = arrayOf(bottomLeft, bottomRight, topLeft, topRight)
            for (i in points.indices) {
                if (points[i]!!.x < smallestX) smallestX = points[i]!!.x
                if (points[i]!!.x > largestX) largestX = points[i]!!.x
            }
            return largestX - smallestX
        }
}