package com.example.sudokusolver.imagerec

import org.opencv.core.Scalar

internal object Constants {
    val WHITE: Scalar = Scalar(255.0)
    val BLACK: Scalar = Scalar(0.0)
    val GREY: Scalar = Scalar(64.0)
    val DARK_GREY: Scalar = Scalar(127.0)
    const val THRESHOLD = 128
}