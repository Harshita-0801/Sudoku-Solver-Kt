package com.example.sudokusolver

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sudokusolver.manual.SudokuGrid

class SudokuBoard : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku_board)
        val gridLayout = findViewById<GridLayout>(R.id.sudokuGrid)
        //get screen size in pixels to adjust size of sudoku cells
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val dimensions = size.x / 11
        SudokuGrid.initGrid(this, gridLayout, dimensions)
        val solveButton = findViewById<Button>(R.id.solveButton)
        solveButton.setOnClickListener(View.OnClickListener {
            SudokuGrid.getCellValues()
            if (!SudokuGrid.solution) {
                Toast.makeText(applicationContext, "Solution does not exist", Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }
            Toast.makeText(applicationContext, "Solution Found", Toast.LENGTH_SHORT).show()
            SudokuGrid.updateSolution()
        })
        val Resetbutton = findViewById<Button>(R.id.resetButton)
        Resetbutton.setOnClickListener { SudokuGrid.updateGrid() }
        val clearButton = findViewById<Button>(R.id.clearButton)
        clearButton.setOnClickListener { SudokuGrid.clearGrid() }
    }
}