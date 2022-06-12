package com.example.sudokusolver.manual

import com.example.sudokusolver.manual.SolveSudoku

//class to solve the puzzle
object SolveSudoku {

    lateinit var sudokuGrid: Array<IntArray>
    @JvmStatic
    fun solvePuzzle(grid: Array<IntArray>): Boolean {
        sudokuGrid = grid
        var solnExists = checkValidity()
        if (!solnExists) return solnExists
        solnExists = solve(0, 0)
        return solnExists
    }

    //check whether no two same numbers exist in each row, column or smaller square
    fun checkValidity(): Boolean {
        val poss = BooleanArray(9)
        for (i in 0..8) {
            for (j in 0..8) poss[j] = true
            if (!horizontalCheck(poss, i)) return false
        }
        for (i in 0..8) {
            for (j in 0..8) poss[j] = true
            if (!verticalCheck(poss, i)) return false
        }
        var i = 0
        while (i < 9) {
            var j = 0
            while (j < 9) {
                for (k in 0..8) poss[k] = true
                if (!squareCheck(poss, i, j)) return false
                j = j + 3
            }
            i = i + 3
        }
        return true
    }

    private fun horizontalCheck(poss: BooleanArray, x: Int): Boolean {
        var output = true
        for (i in 0..8) {
            if (sudokuGrid[x][i] != 0) {
                if (poss[sudokuGrid[x][i] - 1]) {
                    poss[sudokuGrid[x][i] - 1] = false
                } else {
                    output = false
                }
            }
        }
        return output
    }

    private fun verticalCheck(poss: BooleanArray, y: Int): Boolean {
        var output = true
        for (i in 0..8) {
            if (sudokuGrid[i][y] != 0) {
                if (poss[sudokuGrid[i][y] - 1]) {
                    poss[sudokuGrid[i][y] - 1] = false
                } else output = false
            }
        }
        return output
    }

    private fun squareCheck(poss: BooleanArray, x: Int, y: Int): Boolean {
        var output = true
        val startx: Int
        val starty: Int
        startx = x / 3 * 3
        starty = y / 3 * 3
        for (i in startx until startx + 3) {
            for (j in starty until starty + 3) {
                if (sudokuGrid[i][j] != 0) {
                    if (poss[sudokuGrid[i][j] - 1]) {
                        poss[sudokuGrid[i][j] - 1] = false
                    } else output = false
                }
            }
        }
        return output
    }

    fun solve(x: Int, y: Int): Boolean {
        if (x == 9) return true
        val ny: Int
        val nx: Int
        if (y < 8) {
            ny = y + 1
            nx = x
        } else {
            ny = 0
            nx = x + 1
        }
        if (sudokuGrid[x][y] != 0) return solve(nx, ny)
        //possible entries for current cell
        val poss = BooleanArray(9)
        for (i in 0..8) poss[i] = true
        horizontalCheck(poss, x)
        verticalCheck(poss, y)
        squareCheck(poss, x, y)
        for (i in 0..8) {
            if (poss[i]) {
                sudokuGrid[x][y] = i + 1
                if (solve(nx, ny)) return true
            }
        }
        sudokuGrid[x][y] = 0
        return false
    }
}