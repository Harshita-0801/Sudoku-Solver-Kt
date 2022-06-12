package com.example.sudokusolver.imagerec

import org.opencv.core.Point

internal enum class Orientation {
    horizontal, vertical, fortyFiveDegree
}

internal class Line {
    @JvmField
    var origin: Point
    @JvmField
    var destination: Point

    constructor(origin: Point, destination: Point) {
        this.origin = origin
        this.destination = destination
    }

    constructor(vector: Vector, height: Int, width: Int) {
        val origin = Point()
        val destination = Point()
        val a = Math.cos(vector.theta)
        val b = Math.sin(vector.theta)
        val x0 = a * vector.rho
        val y0 = b * vector.rho
        origin.x = x0 + width * -b
        origin.y = y0 + height * a
        destination.x = x0 - width * -b
        destination.y = y0 - height * a
        this.origin = origin
        this.destination = destination
    }

    val orientation: Orientation
        get() {
            if (height == getwidth()) return Orientation.fortyFiveDegree
            return if (height > getwidth()) Orientation.vertical else Orientation.horizontal
        }
    private val height: Double
        private get() = maxY - minY

    private fun getwidth(): Double {
        return maxX - minX
    }

    val minX: Double
        get() = if (origin.x < destination.x) origin.x else destination.x
    val maxX: Double
        get() = if (origin.x > destination.x) origin.x else destination.x
    val maxY: Double
        get() = if (origin.y > destination.y) origin.y else destination.y
    val minY: Double
        get() = if (origin.y < destination.y) origin.y else destination.y
    val angleFromXAxis: Double
        get() {
            val radAngle = Math.atan(height / getwidth())
            return radAngle * 180 / Math.PI
        }

    fun findIntersection(line2: Line): Point? {
        //See http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
        val line1DeltaX = destination.x - origin.x
        val line1DeltaY = destination.y - origin.y
        val line2DeltaX = line2.destination.x - line2.origin.x
        val line2DeltaY = line2.destination.y - line2.origin.y
        val linesDeltaOriginX = origin.x - line2.origin.x
        val linesDeltaOriginY = origin.y - line2.origin.y
        val denominator = line1DeltaX * line2DeltaY - line2DeltaX * line1DeltaY
        val numeratorT = line2DeltaX * linesDeltaOriginY - line2DeltaY * linesDeltaOriginX
        val t = numeratorT / denominator
        return if (linesAreColinear(denominator)) null else calculateIntersection(
            line1DeltaX,
            line1DeltaY,
            t
        )
    }

    private fun linesAreColinear(denominator: Double): Boolean {
        return denominator == 0.0
    }

    fun calculateIntersection(line1DeltaX: Double, line1DeltaY: Double, t: Double): Point {
        val intersection = Point()
        intersection.x = origin.x + t * line1DeltaX
        intersection.y = origin.y + t * line1DeltaY
        return intersection
    }
}