# SudokuSolver
<ul>
<li>Created an android-application to solve a sudoku puzzle.</li>
<li>Input can be provided either in 9X9 grid manually or by capturing the problem image through the device's camera.</li>
<li>The solution can be stored in the device's internal storage for future reference.</li>
<li>Implemented using Tesseract OCR, Open CV, and android development.</li>
 </ul>
<hr>

## _MANUAL INPUT_
It provides a sudoku grid where user needs to enter numbers in each cell in the grid as in the puzzle. 
<br>
### SUDOKU GRID
The user can enter the values as per his desire in this 9X9 grid. The value can be entered to the particular cell by tapping on the respective cell and entering the suitable number. 
<br>In the case if the user tries to enter a multi-digit number then the last entered digit is considered as the value of respective cell.
<br>The digit 0 is not a suitable value for the sudoku problem so if the user tries to enter the digit 0 the cell does not get initialised and remains empty.
<br>
<ul type="circle">
After this the user has got three options:
<li><b>SOLVE </b> :-
  <br>After the user has entered the values in the grid, he can click this button to get the solution of the sudoku puzzle. 
<br>If the solution to the problem exists then the toast indicating “Solution found” message appears on the screen and the missing values in the grid get filled with Red colour. 
 <br>As there are certain cases in which no solution exists for a particular problem. So to handle these cases, a toast gets executed indicating “Solution does not exist” message. 
<li><b>RESET</b> :- <br>This button helps the user to clear the values of the grid. After this, the user can re-enter the suitable values. 							
<li><b>CLEAR</b> :- <br>This Button enables the user to reset the grid to the original puzzle entered after executing the solve or clear button. From here the user can edit the original entered puzzle as per his own choice.
  </ul>
 <hr>
 
 ## _CAPTURE_
 
In this section basically the user needs to capture the sudoku puzzle using Capture button and then he is directed to Manual Edit Section. Further on clicking Solve button, the solution gets printed on the image which can be saved. 
On first clicking the capture button, the image of the sudoku is captured.

### EXTRACTING SUDOKU PUZZLE :-
For extracting the sudoku puzzle we basically follow 3 steps:-
<ul type ="circle">
 <li>Cleaning the image:- <br>
Removing the color and thresholding the image to make it binary using an adaptive threshold to deal with shadows and variations in light

<li>Finding the grid:- <br>
-> Isolating the grid from the rest of the image (I used a simple floodfill algorithm)
<br>-> Finding edges using a Hough Transform 
<br>-> Calculating the corners of the images by : -
<ul> 
  <li>Finding outer edges (i.e. those nearest to the borders of the image)
  <li>Calculating their intersections to find the corners </ul>
<li>
  Extracting the puzzle 
<br>-> A bit more image clean up (I.e. removing the grid that we found so that if some of it gets into the extracted image it doesn't confuse the OCR algorithm).
<br>-> Stretching the image straight using the OpenCV "Warp Perspective" transform.
  </ul>
### EXTRACTING THE DIGITS :-
The height and width of the image is calculated through mat obtained from the capture() containing the cropped image of sudoku. This is then further divided into 9X9 grid (81 cells) in order to work on the individual cell. 
<br>The rect is created around a cell and the cropped digit mat is created. It is then converted to a bitmap which is processed by the OCR to recognise the digit.
The extracted digits are saved in a matrix which are displayed in the edit section.

### EDIT SECTION :-
In this view the matrix of the digits recognised by the digit recogniser are displayed on the 9X9 grid. 
<br>This view gives user the option to edit the digits by simply tapping on the appropriate square cells. 
<br>This view is added keeping in mind the scenario in case some of digits are wrongly recognized or the missed as skipping even a single digit can change the complete answer. 
<br>After editing the puzzle, user presses the ‘Solve button’ to proceed further.

### PRINTING THE SOLUTION AND SAVING IT :- 
The result is printed on the problem image and the solution is saved to the memory using save button. This will create an album named sdCard and add the image to the folder in jpg format. 

### DEMONSTRATION :- 
 Click on the image below to see the demonstration:-
 
 <a href ="https://drive.google.com/file/d/1BaBd8zU7Bbtrx1ecYOY15FeWdcRHxa4I/view?usp=sharing"> <img src="https://media.istockphoto.com/photos/demo-sign-colorful-tags-picture-id472909414?k=20&m=472909414&s=612x612&w=0&h=lfJ4C6qJEAfNUkOeZgqIsJ6RtZMENS35KXavRMIXKe8=" width="300" height="150"> </a>



  


 
  
