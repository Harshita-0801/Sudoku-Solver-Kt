package com.example.sudokusolver;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sudokusolver.manual.SudokuGrid;


public class SudokuBoard extends AppCompatActivity {
   @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_board);
        GridLayout gridLayout = findViewById(R.id.sudokuGrid);
        //get screen size in pixels to adjust size of sudoku cells
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int dimensions = size.x / 11;

        SudokuGrid.initGrid(this, gridLayout, dimensions);

        final Button solveButton= findViewById(R.id.solveButton);


        solveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SudokuGrid.getCellValues();
                if(!SudokuGrid.getSolution()){
                    Toast.makeText(getApplicationContext(),"Solution does not exist",Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getApplicationContext(),"Solution Found",Toast.LENGTH_SHORT).show();
                SudokuGrid.updateSolution();
            }
        });

        Button Resetbutton= findViewById(R.id.resetButton);
        Resetbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SudokuGrid.updateGrid();
            }
        });

        Button clearButton= findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SudokuGrid.clearGrid();
            }
        });

    }
}
