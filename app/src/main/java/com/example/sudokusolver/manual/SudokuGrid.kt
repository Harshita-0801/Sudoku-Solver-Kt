package com.example.sudokusolver.manual

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Space
import com.example.sudokusolver.R
import com.example.sudokusolver.manual.SolveSudoku.solvePuzzle
import com.example.sudokusolver.manual.SudokuGrid
import com.example.sudokusolver.manual.SolveSudoku

object SudokuGrid {
    //contains reference to the sudoku cells
    private lateinit var gridCell: Array<Array<EditText?>>

    //contains value of integer in cells, if blank then 0
    private var cellValues = Array(9) { IntArray(9) }
    private val copygrid = Array(9) { IntArray(9) }
    private var cellDimensions = 0
    fun initGrid(context: Context, gridLayout: GridLayout, dimensions: Int) {
        cellDimensions = dimensions
        gridCell = initCell(context)
        var i = 0
        var j: Int
        for (a in 0..10) {
            j = 0
            for (b in 0..10) {
                val rowSpan = GridLayout.spec(GridLayout.UNDEFINED, 1)
                val colSpan = GridLayout.spec(GridLayout.UNDEFINED, 1)
                val layoutParams = GridLayout.LayoutParams(rowSpan, colSpan)
                val space = Space(context)
                if (((a == 3) || (a == 7)) && ((b == 3) || (b == 7))) {
                    space.minimumWidth = cellDimensions / 10
                    space.minimumHeight = cellDimensions / 10
                    gridLayout.addView(space, layoutParams)
                    continue
                }
                if ((a == 3) || (a == 7)) {
                    space.minimumWidth = cellDimensions
                    space.minimumHeight = cellDimensions / 10
                    gridLayout.addView(space, layoutParams)
                    continue
                }
                if ((b == 3) || (b == 7)) {
                    space.minimumWidth = cellDimensions / 10
                    space.minimumHeight = cellDimensions
                    gridLayout.addView(space, layoutParams)
                    continue
                }
                gridLayout.addView(gridCell[i][j], layoutParams)
                j++
            }
            if ((a == 3) || (a == 7)) continue
            i++
        }
    }

    //initializes each cell with appropriate settings
    private fun initCell(context: Context): Array<Array<EditText?>> {
        val sudokuCell = Array(9) { arrayOfNulls<EditText>(9) }
        for (i in 0..8) {
            for (j in 0..8) {
                sudokuCell[i][j] = EditText(context)
                sudokuCell.get(i).get(j)!!.isCursorVisible = false
                sudokuCell[i][j]!!.setBackgroundResource(R.drawable.exit_text_style)
                sudokuCell.get(i).get(j)!!.minimumHeight = cellDimensions
                sudokuCell.get(i).get(j)!!.minimumWidth = cellDimensions
                sudokuCell.get(i).get(j)!!.textSize = 15f
                sudokuCell[i][j]!!.setPadding(0, 0, 0, 0)
                sudokuCell.get(i).get(j)!!.gravity = Gravity.CENTER
                sudokuCell.get(i).get(j)!!.isClickable = true
                sudokuCell.get(i).get(j)!!.isFocusable = true
                sudokuCell.get(i).get(j)!!.isFocusableInTouchMode = true
                sudokuCell.get(i).get(j)!!.inputType = InputType.TYPE_CLASS_NUMBER
                sudokuCell[i][j]!!.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    //does not allow any integers other than 1-9
                    override fun afterTextChanged(s: Editable) {
                        if ((s.length == 1) && (s.toString().toInt() != 0)) return
                        if (s.length == 0) return
                        if (s.toString().toInt() == 0) {
                            s.clear()
                            return
                        }
                        //if two digit integer is entered, take the last entered digit
                        s.replace(0, s.length, s.toString()[s.length - 1].toString())
                    }
                })

                //change cursor position to end of text
                sudokuCell.get(i).get(j)!!.onFocusChangeListener =
                    object : View.OnFocusChangeListener {
                        override fun onFocusChange(v: View, hasFocus: Boolean) {
                            if (hasFocus) {
                                val et: EditText = v as EditText
                                et.post(object : Runnable {
                                    override fun run() {
                                        et.setSelection(et.length())
                                    }
                                })
                            }
                        }
                    }
                sudokuCell[i][j]!!.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        val et = v as EditText
                        et.post(Runnable { et.setSelection(et.length()) })
                    }
                })
            }
        }
        return sudokuCell
    }

    fun getCellValues() {
        for (i in 0..8) {
            for (j in 0..8) {
                if (!gridCell[i][j]!!.text.toString().isEmpty()) {
                    cellValues[i][j] = gridCell[i][j]!!.text.toString().toInt()
                } else cellValues[i][j] = 0
            }
        }
        for (i in 0..8) {
            for (j in 0..8) {
                copygrid[i][j] = cellValues[i][j]
            }
        }
    }

    fun updateGrid() {
        for (i in 0..8) {
            for (j in 0..8) {
                gridCell[i][j]!!.setText(copygrid[i][j].toString())
                gridCell[i][j]!!.setTextColor(Color.parseColor("#000000"))
            }
        }
    }

    val solution: Boolean
        get() {
            val solnExists = solvePuzzle(cellValues)
            if (solnExists) cellValues = SolveSudoku.sudokuGrid
            Log.d(ContentValues.TAG, solnExists.toString())
            return solnExists
        }

    fun updateSolution() {
        for (i in 0..8) {
            for (j in 0..8) {
                if (copygrid[i][j] == 0) {
                    gridCell[i][j]!!.setText(cellValues[i][j].toString())
                    gridCell[i][j]!!.setTextColor(Color.parseColor("#FF0000"))
                } else {
                    gridCell[i][j]!!.setText(copygrid[i][j].toString())
                    gridCell[i][j]!!.setTextColor(Color.parseColor("#000000"))
                }
            }
        }
    }

    fun clearGrid() {
        for (i in 0..8) {
            for (j in 0..8) {
                cellValues[i][j] = 0
                gridCell[i][j]!!.setText("")
                gridCell[i][j]!!.setTextColor(Color.parseColor("#000000"))
            }
        }
    }
}