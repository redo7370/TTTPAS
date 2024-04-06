/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.GridLayout;
import java.awt.Container;
import java.awt.Color;
import java.awt.event.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

// Main Class TTTPA
/*
 * Concatates All Subcomponents:
 * - TicTacToe Field
 * - Menu To Modify Game
 * 
 * Affects Components Of Other Classes:
 * – Changes Color Of Components
 * – Return Instructions To Other Classes
 */
public class TTTPAS {
    private static char[][] board = { { ' ', ' ', ' ' }, { ' ', ' ', ' ' }, { ' ', ' ', ' ' } }; // Global Board
    private static int turn = 0; // Global Turn Indicator
    public static FunctionPanel funcPanel; // Global funcPanel
    public static WinnerBoard winBoard;

    // Adding Components To Main Frame
    public static void GUI() {

        // Creating Window Of Program
        JFrame frame = new JFrame("TicTacToe – Type PAS");
        frame.setSize(800, 500); // Setting Window-Dimensions [800x500]

        // Creating Main-Panel For The Frame
        JPanel mainFramePanel = new JPanel();
        mainFramePanel.setLayout(new GridLayout(1, 3)); // Setting Panel's LayOut To GridLayout [1 row, 3 columns]

        // Creating Function-Panel, Winnerboard-Panel and TicTacToe-Panel
        funcPanel = new FunctionPanel();
        winBoard = new WinnerBoard();
        TTTPanel ttt = new TTTPanel();

        // Adding Function-Panel, Winnerboard-Panel and TicTacToe-Panel To The
        // Main-Panel
        mainFramePanel.add(funcPanel);
        mainFramePanel.add(ttt);
        mainFramePanel.add(winBoard);

        // Assign Close Action To Window
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                winBoard.saveData();
                System.exit(0);
            }
        });

        // Add Main-Panel To The Frame
        frame.getContentPane().add(mainFramePanel);

        // Display Window
        frame.setVisible(true);
    }

    // Return Current Board To Calling Function
    public static char[][] getField() {
        return board;
    }

    // Update Board With Given Inputs
    public static void setField(int[] cords, char sign) {
        board[cords[0]][cords[1]] = sign;
    }

    // Reset Board
    public static void resetField() {
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                board[i][j] = ' ';
            }
        }
    }

    // Return Current Turn To Calling Function
    public static int getTurn() {
        return turn;
    }

    // Set New Turn
    public static void setTurn(int num) {
        turn = num;
    }

    // MAIN
    public static void main(String[] args) {

        // GUI Issue Handling For macOS
        try {
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Cannot set LookAndFeel");
        }

        // Reserve Thread To Run The GUI
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GUI(); // –––– START PROGRAM –––– //
            }
        });
    }
}

// Class TTTPanel
/*
 * Contains The General Game Logic
 * - Displays Board
 * - Defines Basic Game Cycle
 */
class TTTPanel extends JPanel {
    public static TPanelRow firstRow, secondRow, thirdRow; // Rows Of The TicTacToe Board From Subclass TPanelRow
    private int panelAmount = 5; // Amount Of Subpanels In TTTPanel
    private static JLabel outputLabel; // Initializes outputLabel
    private static JButton resetButton; // Initializes resetButton
    private static MinMax virtualOpponent = new MinMax(); // Initializes Class MinMax Containing The Minnax Algorithm
    private static boolean IS_MINMAX = false; // Boolean To Tell If Minimax Is Active
    private static boolean ALLOW_MINMAX_INTERACTION = true; // Boolean To Allow To Activate Mininmax To Be Activated
    private static boolean GAME_END = false; // Boolean To Store Whether A Game Has Concluded

    // Basic Structure Of The TicTacToe Field And Directly Associated Components
    public TTTPanel() {

        // Sets Layout To Gridlayout [5 rows, 1 column]
        this.setLayout(new GridLayout(panelAmount, 1));

        // Create SubPanels for different use cases
        JPanel[][] subPanels = new JPanel[panelAmount][1]; // Creating 2D-Array For Location Of Panels
        JPanel outputPanel = subPanels[0][0] = new JPanel();
        JPanel resetPanel = subPanels[4][0] = new JPanel();
        outputPanel.setLayout(new GridLayout(1, 1));
        resetPanel.setLayout(new GridLayout(1, 1));

        // Add Rows Of The TicTacToe Field Using TPanelRow Class
        TPanelRow[][] tRows = new TPanelRow[panelAmount][1];
        firstRow = tRows[1][0] = new TPanelRow();
        secondRow = tRows[2][0] = new TPanelRow();
        thirdRow = tRows[3][0] = new TPanelRow();

        // Creating Button To Reset The Board
        resetButton = new JButton("Reset");
        resetButton.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Adding Black Border To Button
        resetPanel.add(resetButton); // Adding Button To Designated Panel

        // Assign ActionListener To The Reset-Button
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                Reset(); // Calling Reset Function When Button Is Clicked
            }
        });

        this.add(outputPanel);
        this.add(firstRow);
        this.add(secondRow);
        this.add(thirdRow);
        this.add(resetPanel);

        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        outputLabel = new JLabel("", SwingConstants.CENTER);
        outputLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        outputPanel.add(outputLabel);

        Reset();
    }

    // Resets GUI And The Game
    public static void Reset() {

        // Color All Fields White
        firstRow.left.setBackground(Color.WHITE);
        firstRow.mid.setBackground(Color.WHITE);
        firstRow.right.setBackground(Color.WHITE);
        secondRow.left.setBackground(Color.WHITE);
        secondRow.mid.setBackground(Color.WHITE);
        secondRow.right.setBackground(Color.WHITE);
        thirdRow.left.setBackground(Color.WHITE);
        thirdRow.mid.setBackground(Color.WHITE);
        thirdRow.right.setBackground(Color.WHITE);
        resetButton.setBackground(Color.WHITE);

        // Erase Text Of All Fields
        firstRow.left.setText("");
        firstRow.mid.setText("");
        firstRow.right.setText("");
        secondRow.left.setText("");
        secondRow.mid.setText("");
        secondRow.right.setText("");
        thirdRow.left.setText("");
        thirdRow.mid.setText("");
        thirdRow.right.setText("");

        // Reset OutputLabel Instruction To Default
        outputLabel.setText(String.format("Click A Field To Begin"));

        // Reset Turn To 2
        TTTPAS.setTurn(2);

        // Reset Stored Board
        TTTPAS.resetField();

        // Color Button To Activate Minimax White
        TTTPAS.funcPanel.changeButtonColor(Color.WHITE);

        ALLOW_MINMAX_INTERACTION = true; // Allow To Activate Minimax
        GAME_END = false; // Reset Game Status
    }

    // Main Function To Handle Game Cycle
    public static void gameHandler(Container buttonParent, String position) {

        // Check If A Slot On The Field Is Free –– If All Slots Taken...
        if (!isSlotFree(TTTPAS.getField())) {
            return; // End gameHandler
        }

        // Check If Game Has Concluded –– If Concluded...
        if (GAME_END) {
            return; // End gameHandler
        }

        TTTPAS.funcPanel.changeButtonColor(Color.GRAY); // Set Virtual Opponent Button Color To Gray

        ALLOW_MINMAX_INTERACTION = false; // Disallow Activation Of Minimax Mid-Game
        char step = newStep(); // Choose New Symbol ['X' OR 'O']
        int[] fieldCoordinates = buttonOrientation(buttonParent, position); // Convert and Store Field-Coordinates

        // Check If Move Is Legal –– If Move is Illegal
        if (illegalMove(fieldCoordinates)) {
            return; // End gameHandler
        }

        TTTPAS.setField(fieldCoordinates, step); // Update 2D-Array In Superclass
        guiSetMove(fieldCoordinates, step); // Call guiSetMove To Display Move

        // Check If Move Was A Winning Move –– If Game Is Won...
        if (checkWin(TTTPAS.getField())) {
            GAME_END = true; // Set Game Status To Concluded
            declareWin(step); // Process Win Internally Using declareWin Function
            showWin(TTTPAS.getField()); // Update GUI To Show Winning Line
            return; // End gameHandler
        }

        // Check If A Slot On The Field Is Free –– If All Slots Taken...
        if (!isSlotFree(TTTPAS.getField())) {
            outputLabel.setText("Draw"); // Display Draw
            return; // End gameHandler
        }

        // Display Name Of Next Player
        outputLabel.setText(String.format("%s's Turn", TTTPAS.funcPanel.getPlayerName(step)));

        // ----- !!! END OF PLAYER MOVES !!! ----- //

        // Check If Minimax Is Active –– If Minimax Is Inactive...
        if (!IS_MINMAX) {
            return; // End gameHandler
        }

        // Check If A Slot On The Field Is Free –– If All Slots Taken...
        if (!isSlotFree(TTTPAS.getField())) {
            return; // End gameHandler
        }

        // Check If Game Has Concluded –– If Concluded...
        if (GAME_END) {
            return; // End gameHandler
        }

        step = newStep(); // Choose New Symbol [Always 'O']
        fieldCoordinates = virtualOpponent.findBestMove(TTTPAS.getField()); // Store Minimax Move Coordinates

        // Check If Move Is Legal –– If Move is Illegal
        if (illegalMove(fieldCoordinates)) {
            char[][] escapeBoard = TTTPAS.getField(); // Get Current Board

            // Choose First Valid Move
            for (int n = 0; n < 3; n++) {
                for (int m = 0; m < 3; m++) {
                    if (escapeBoard[n][m] == ' ') {
                        fieldCoordinates[0] = n;
                        fieldCoordinates[1] = m;
                    }
                }
            }
        }

        TTTPAS.setField(fieldCoordinates, step); // Update 2D-Array In Superclass
        guiSetMove(fieldCoordinates, step); // Call guiSetMove To Display Move

        // Check If Move Was A Winning Move –– If Game Is Won...
        if (checkWin(TTTPAS.getField())) {
            GAME_END = true; // Set Game Status To Concluded
            declareWin('!'); // Process Win Internally Using declareWin Function and '!' To Indicate Minimax
                             // As Winner
            showWin(TTTPAS.getField()); // Update GUI To Show Winning Line
            return; // End gameHandler
        }

        // Check If A Slot On The Field Is Free –– If All Slots Taken...
        if (!isSlotFree(TTTPAS.getField())) {
            outputLabel.setText("Draw"); // Display Draw
            return; // End gameHandler
        }
    }

    // Color Lines That Lead To Win Green
    private static void showWin(char[][] board) {
        for (int row = 0; row < 3; row++) { // Iterate Through All Rows –– If All Symbols In nth-Row Are Equal...
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2] && board[row][0] != ' ') {
                switch (row) {
                    case 0: // Color First Row Green
                        firstRow.left.setBackground(Color.GREEN);
                        firstRow.mid.setBackground(Color.GREEN);
                        firstRow.right.setBackground(Color.GREEN);
                        break;
                    case 1: // Color Second Row Green
                        secondRow.left.setBackground(Color.GREEN);
                        secondRow.mid.setBackground(Color.GREEN);
                        secondRow.right.setBackground(Color.GREEN);
                        break;
                    case 2: // Color Third Row Green
                        thirdRow.left.setBackground(Color.GREEN);
                        thirdRow.mid.setBackground(Color.GREEN);
                        thirdRow.right.setBackground(Color.GREEN);
                        break;
                }
            }
        }

        for (int col = 0; col < 3; col++) { // Iterate Through All Columns –– If All Symbols In nth-Column Are Equal...
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col] && board[0][col] != ' ') {
                switch (col) {
                    case 0: // Color First Column Green
                        firstRow.left.setBackground(Color.GREEN);
                        secondRow.left.setBackground(Color.GREEN);
                        thirdRow.left.setBackground(Color.GREEN);
                        break;
                    case 1: // Color Second Column Green
                        firstRow.mid.setBackground(Color.GREEN);
                        secondRow.mid.setBackground(Color.GREEN);
                        thirdRow.mid.setBackground(Color.GREEN);
                        break;
                    case 2: // Color Third Column Green
                        firstRow.right.setBackground(Color.GREEN);
                        secondRow.right.setBackground(Color.GREEN);
                        thirdRow.right.setBackground(Color.GREEN);
                        break;
                }
            }
        }

        // Check For Diagonal [0, 0] --> [2, 2]
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[1][1] != ' ') {
            firstRow.left.setBackground(Color.GREEN);
            secondRow.mid.setBackground(Color.GREEN);
            thirdRow.right.setBackground(Color.GREEN);
        }

        // Check For Diagonal [0, 2] --> [2, 0]
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[1][1] != ' ') {
            firstRow.right.setBackground(Color.GREEN);
            secondRow.mid.setBackground(Color.GREEN);
            thirdRow.left.setBackground(Color.GREEN);
        }
    }

    // Update Label To Show Winner
    private static void declareWin(char symbol) {
        if (IS_MINMAX && symbol == '!') { // If Minimax Is Active AND Symbol Is '!'
            outputLabel.setText("Winner: MINIMAX"); // Winner = Minimax
            return;
        } else if (IS_MINMAX && symbol != '!') { // If Minimax Is Active AND Symbol Is Not '!'
            String winner = TTTPAS.funcPanel.getPlayerName(symbol); // Get Playername From Function-Panel
            outputLabel.setText("Winner: " + winner); // Winner = Player
            return;
        }

        // If Minimax Is Not Active
        String winner = TTTPAS.funcPanel.getPlayerName(symbol); // Get Playername For Dedicated Symbol
        outputLabel.setText("Winner: " + winner); // Winner = Player
        if (winner != "X" && winner != "O") { // If Playername Isn't Default ['X' OR 'O']
            TTTPAS.winBoard.updateDatabase(winner); // Update Leaderboard And Database
        }
    }

    // Check If One Player Has One
    private static boolean checkWin(char[][] board) {
        // Return True If Whole nth-Row Is Same Symbol
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2] && board[row][0] != ' ') {
                return true;
            }
        }

        // Return True If Whole nth-Column Is Same Symbol
        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col] && board[0][col] != ' ') {
                return true;
            }
        }

        // Return True If Whole Diagonal [0, 0] --> [2, 2] Is Same Symbol
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[1][1] != ' ') {
            return true;
        }

        // Return True If Whole Diagonal [0, 2] --> [2, 0] Is Same Symbol
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[1][1] != ' ') {
            return true;
        }

        // Return False If All Lines Are Not Equal
        return false;
    }

    // Check If At Least One Space Is Not Occupied
    private static boolean isSlotFree(char[][] board) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == ' ') {
                    return true; // Return True If One Space Was Found
                }
            }
        }
        return false; // Return False If No Space Was Found
    }

    // Check If Move Is Possible
    private static boolean illegalMove(int[] cords) {
        char[][] board = TTTPAS.getField(); // Get Current Board
        if (board[cords[0]][cords[1]] != ' ') {
            return true; // Return True If Space On Board Is Occupied
        }
        return false; // Return False If Space Is Not Occupied
    }

    // Show Move In GUI
    private static void guiSetMove(int[] cords, char move) {
        String text = String.valueOf(move); // Convert Character To String

        switch (cords[0]) {
            case 0:
                switch (cords[1]) {
                    case 0:
                        firstRow.left.setText(text); // First Row – First Column [0, 0]
                        break;
                    case 1:
                        firstRow.mid.setText(text); // First Row – Second Column [0, 1]
                        break;
                    case 2:
                        firstRow.right.setText(text); // First Row – Third Column [0, 2]
                        break;
                    default:
                        System.out.println("ILLEGAL INPUT –– guiSetMove FAILED");
                        System.exit(0); // Exit Program If Illegal Input
                        break;
                }
                break;
            case 1:
                switch (cords[1]) {
                    case 0:
                        secondRow.left.setText(text); // Second Row – First Column [1, 0]
                        break;
                    case 1:
                        secondRow.mid.setText(text); // Second Row – Second Column [1, 1]
                        break;
                    case 2:
                        secondRow.right.setText(text); // Second Row – Third Column [1, 2]
                        break;
                    default:
                        System.out.println("ILLEGAL INPUT –– guiSetMove FAILED");
                        System.exit(0); // Exit Program If Illegal Input
                        break;
                }
                break;

            case 2:
                switch (cords[1]) {
                    case 0:
                        thirdRow.left.setText(text); // Third Row – First Column [2, 0]
                        break;
                    case 1:
                        thirdRow.mid.setText(text); // Third Row – Second Column [2, 1]
                        break;
                    case 2:
                        thirdRow.right.setText(text); // Third Row – Third Column [2, 2]
                        break;
                    default:
                        System.out.println("ILLEGAL INPUT –– guiSetMove FAILED");
                        System.exit(0); // Exit Program If Illegal Input
                        break;
                }
                break;

            default:
                System.out.println("ILLEGAL INPUT –– guiSetMove FAILED");
                System.exit(0); // Exit Program If Illegal Input
                break;
        }
    }

    // Choose New Symbol For Player Based On Current Turn In TTTPAS
    private static char newStep() {
        int turnNum = TTTPAS.getTurn(); // Get Turn From TTTPAS

        // If Turn Is Even
        if (turnNum % 2 == 0) {
            TTTPAS.setTurn(1); // Set Turn In TTTPAS To 1
            return 'X';
        } else {
            TTTPAS.setTurn(2); // Set Turn In TTTPAS To 1
            return 'O';
        }
    }

    // Figure Out Position Of Clicked Button Through Provided Container And Position
    // In Row
    public static int[] buttonOrientation(Container parent, String buttonPosition) {
        // Set Containers For Each Row
        List<Container> first = firstRow.selfParent();
        List<Container> second = secondRow.selfParent();
        List<Container> third = thirdRow.selfParent();

        // Initialize New Array To Store Coordinates Of The Clicked Button
        int[] cords = new int[2];

        // Check Which Row Is Parent Of Clicked Button
        if (first.contains(parent)) {
            cords[0] = 0; // First Coordinate [Row] = 0
        } else if (second.contains(parent)) {
            cords[0] = 1; // First Coordinate [Row] = 1
        } else if (third.contains(parent)) {
            cords[0] = 2; // First Coordinate [Row] = 2
        }

        // Check Which Position The Button Provided
        switch (buttonPosition) {
            case "left":
                cords[1] = 0; // Second Coordinate [Column] = 0
                break;

            case "mid":
                cords[1] = 1; // Second Coordinate [Column] = 1
                break;

            case "right":
                cords[1] = 2; // Second Coordinate [Column] = 2
                break;

            default:
                break;
        }
        return cords; // Return Coordinations
    }

    // Activate Minimax
    public static void setMinimaxActive() {
        // If Minimax Is Inactive
        if (!IS_MINMAX) {
            IS_MINMAX = true; // Activate Minimax
        } else {
            IS_MINMAX = false; // Deactivate Minimax
        }
    }

    // Return If Minimax Is Active
    public static boolean isMinimaxActive() {
        return IS_MINMAX;
    }

    // Return If It Is Allowed To Activate Minimax
    public static boolean getPermission() {
        return ALLOW_MINMAX_INTERACTION;
    }

}

// Class TPanel Row
/*
 * Blueprint For Each Row Of TicTacToe Board
 */
class TPanelRow extends JPanel {
    public JButton left, mid, right; // Initialize JButtons

    // Basic Layout Of A Row
    public TPanelRow() {
        this.setLayout(new GridLayout(1, 3)); // Set Layout To Gridlayout

        JButton[][] buttons = new JButton[1][3]; // 2D-Array Of All JButtons
        left = buttons[0][0] = new JButton("1");
        mid = buttons[0][1] = new JButton("2");
        right = buttons[0][2] = new JButton("3");

        // Add Buttons To Panel
        this.add(left);
        this.add(mid);
        this.add(right);

        // Assign Black Border To All Buttons
        left.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mid.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        right.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Assign Actionlisteners To All Buttons
        left.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // If Own Text Is Empty...
                if (left.getText() != "") {
                    return;
                }
                Container parent = left.getParent();
                callGameHandler(parent, "left"); // Call gameHandler
            }
        });
        mid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // If Own Text Is Empty...
                if (mid.getText() != "") {
                    return;
                }
                Container parent = mid.getParent();
                callGameHandler(parent, "mid"); // Call gameHandler
            }
        });
        right.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // If Own Text Is Empty...
                if (right.getText() != "") {
                    return;
                }
                Container parent = right.getParent();
                callGameHandler(parent, "right"); // Call gameHandler
            }
        });
    }

    // Return List Of Containers Of Parent From Each Button (Usually All Equal)
    public List<Container> selfParent() {
        List<Container> parentList = Arrays.asList(left.getParent(), mid.getParent(), right.getParent());
        return parentList;
    }

    // Execute Call To gameHandler
    protected static void callGameHandler(Container parent, String position) {
        TTTPanel.gameHandler(parent, position);
    }

}

// Class FunctionPanel
/*
 * Adds Additional Options For User To Change Game Mechanic
 * - Activate Virtual Opponent
 * - Designated Button To Quit Program
 * - Clearing All Stored Data
 * - Setting Own Playernames
 */
class FunctionPanel extends JPanel {
    private static JTextField playerOneText, playerTwoText; // Initialize TextFields Of Player One And Player Two
    private static JButton activateOpponent; // Initialize JButton To Activate Opponent

    // Layout Of Function-Panel
    public FunctionPanel() {
        this.setLayout(new GridLayout(2, 1)); // Set Layout To Gridlayout

        JPanel instructionPanel = new JPanel(); // Top Panel
        JPanel mediaPanel = new JPanel(); // Bottom Panel

        // Setting Gridlayouts To Subpanels
        mediaPanel.setLayout(new GridLayout(4, 1));
        instructionPanel.setLayout(new GridLayout(3, 1));

        // Creating Subpanels Of mediaPanel
        JPanel[][] subPanels = new JPanel[4][1];
        JPanel memoryPanel = subPanels[0][0] = new JPanel(); // For Button To Clear Leaderboard
        JPanel plusPanel = subPanels[1][0] = new JPanel(); // For Button To Activate Minimax
        JPanel playerOnePanel = subPanels[2][0] = new JPanel(); // For Name Of Player #1
        JPanel playerTwoPanel = subPanels[3][0] = new JPanel(); // For Name Of Player #2

        // Assigning Gridlayouts To Subpanels
        memoryPanel.setLayout(new GridLayout(1, 1));
        plusPanel.setLayout(new GridLayout(1, 1));
        playerOnePanel.setLayout(new GridLayout(1, 2));
        playerTwoPanel.setLayout(new GridLayout(1, 2));

        JButton quitButton = new JButton("Quit");
        activateOpponent = new JButton("Activate Virtual Opponent");
        JButton clearMemory = new JButton("Erase Leaderbaord");

        activateOpponent.setBackground(Color.WHITE);
        quitButton.setBackground(Color.WHITE);
        clearMemory.setBackground(Color.WHITE);

        JLabel playerOneLabel = new JLabel("Player X Name:", SwingConstants.CENTER);
        JLabel playerTwoLabel = new JLabel("Player O Name:", SwingConstants.CENTER);
        JLabel instructionLabel = new JLabel("Menu", SwingConstants.CENTER);
        JLabel dummyLabel = new JLabel();

        playerOnePanel.setBackground(Color.WHITE);
        playerTwoPanel.setBackground(Color.WHITE);
        instructionLabel.setBackground(Color.WHITE);
        quitButton.setBackground(Color.WHITE);

        playerOneText = new JTextField();
        playerTwoText = new JTextField();
        playerOneText.setHorizontalAlignment(SwingConstants.CENTER);
        playerTwoText.setHorizontalAlignment(SwingConstants.CENTER);

        activateOpponent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (TTTPanel.getPermission()) {
                    TTTPanel.setMinimaxActive();
                    if (TTTPanel.isMinimaxActive()) {
                        playerTwoPanel.setBackground(Color.GRAY);
                        activateOpponent.setText("Deactivate Virtual Opponent");
                    } else {
                        playerTwoPanel.setBackground(Color.WHITE);
                        activateOpponent.setText("Activate Virtual Opponent");
                    }
                }
            }
        });

        clearMemory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                TTTPAS.winBoard.clearLeaderboard();
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                TTTPAS.winBoard.saveData();
                System.exit(0);
            }
        });

        memoryPanel.add(clearMemory);
        plusPanel.add(activateOpponent);
        playerOnePanel.add(playerOneLabel);
        playerOnePanel.add(playerOneText);
        playerTwoPanel.add(playerTwoLabel);
        playerTwoPanel.add(playerTwoText);
        instructionPanel.add(instructionLabel);
        instructionPanel.add(dummyLabel);
        instructionPanel.add(quitButton);

        mediaPanel.add(memoryPanel);
        mediaPanel.add(plusPanel);
        mediaPanel.add(playerOnePanel);
        mediaPanel.add(playerTwoPanel);

        this.add(instructionPanel);
        this.add(mediaPanel);

        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        playerOneLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        playerTwoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        playerOneText.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        playerTwoText.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        activateOpponent.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        quitButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    public String getPlayerName(char symbol) {
        String content;
        if (symbol == 'X') {
            content = playerOneText.getText();
            if (content.isEmpty()) {
                return "X";
            }
        } else {
            content = playerTwoText.getText();
            if (content.isEmpty()) {
                return "O";
            }
        }
        return content;
    }

    public void changeButtonColor(Color color) {
        activateOpponent.setBackground(color);
    }
}

class WinnerBoard extends JPanel {
    private DataBase database;
    private JLabel[][] playerLabels, winLabels;

    public WinnerBoard() {
        this.setLayout(new GridLayout(3, 1));

        JPanel[][] subPanels = new JPanel[3][1];
        JPanel upPanel = subPanels[0][0] = new JPanel();
        JPanel midPanel = subPanels[1][0] = new JPanel();
        JPanel downPanel = subPanels[2][0] = new JPanel();
        upPanel.setLayout(new GridLayout(2, 1));
        midPanel.setLayout(new GridLayout(3, 2));
        downPanel.setLayout(new GridLayout(3, 2));
        midPanel.setBackground(Color.WHITE);
        downPanel.setBackground(Color.WHITE);

        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setLayout(new GridLayout(3, 1));

        JLabel upperDummyLabel = new JLabel(" ");
        JLabel descriptionLabel = new JLabel("Winnerboard", SwingConstants.CENTER);
        JLabel lowerDummyLabel = new JLabel(" ");
        JLabel lowDummyLabel = new JLabel(" ");
        descriptionPanel.add(upperDummyLabel);
        descriptionPanel.add(descriptionLabel);
        descriptionPanel.add(lowerDummyLabel);
        upPanel.add(descriptionPanel);
        upPanel.add(lowDummyLabel);

        playerLabels = new JLabel[7][2];
        playerLabels[0][0] = new JLabel("Playername", SwingConstants.CENTER);
        playerLabels[1][0] = new JLabel("", SwingConstants.CENTER);
        playerLabels[2][0] = new JLabel("", SwingConstants.CENTER);
        playerLabels[3][0] = new JLabel("", SwingConstants.CENTER);
        playerLabels[4][0] = new JLabel("", SwingConstants.CENTER);
        playerLabels[5][0] = new JLabel("", SwingConstants.CENTER);

        winLabels = new JLabel[7][2];
        winLabels[0][1] = new JLabel("Wins", SwingConstants.CENTER);
        winLabels[1][1] = new JLabel("", SwingConstants.CENTER);
        winLabels[2][1] = new JLabel("", SwingConstants.CENTER);
        winLabels[3][1] = new JLabel("", SwingConstants.CENTER);
        winLabels[4][1] = new JLabel("", SwingConstants.CENTER);
        winLabels[5][1] = new JLabel("", SwingConstants.CENTER);

        for (int i = 0; i <= 2; i++) {
            midPanel.add(playerLabels[i][0]);
            midPanel.add(winLabels[i][1]);
        }

        for (int i = 3; i <= 5; i++) {
            downPanel.add(playerLabels[i][0]);
            downPanel.add(winLabels[i][1]);
        }

        for (int i = 0; i <= 5; i++) {
            playerLabels[i][0].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            winLabels[i][1].setBorder(BorderFactory.createLineBorder(Color.BLACK));

            playerLabels[i][0].setBackground(Color.WHITE);
            winLabels[i][1].setBackground(Color.WHITE);
        }

        this.add(upPanel);
        this.add(midPanel);
        this.add(downPanel);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        try {
            database = new DataBase();
        } catch (Exception e) {
            throw new IllegalStateException("Database Failed", e);
        }

        setWinnerBoard();
    }

    public void setWinnerBoard() {
        Map<String, Integer> hashMap = database.memory;
        Iterator<Map.Entry<String, Integer>> iterator = hashMap.entrySet().iterator();

        String[] winnerNames = { "N/A", "N/A", "N/A", "N/A", "N/A" };
        int[] mostWins = { 0, 0, 0, 0, 0 };

        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String key = entry.getKey();
            int value = entry.getValue();

            boolean guard = true;

            for (int i = 0; i < 5; i++) {
                if (value > mostWins[i] && guard) {
                    for (int j = 4; j > i; j--) {
                        mostWins[j] = mostWins[j - 1];
                        winnerNames[j] = winnerNames[j - 1];
                    }
                    mostWins[i] = value;
                    winnerNames[i] = key;
                    guard = false;
                }
            }
        }

        for (int i = 1; i <= 5; i++) {
            playerLabels[i][0].setText(winnerNames[i - 1]);
            winLabels[i][1].setText(String.valueOf(mostWins[i - 1]));
        }
    }

    public void updateDatabase(String name) {
        database.updateTable(name);
        setWinnerBoard();
    }

    public void saveData() {
        database.saveRecords();
    }

    public void clearLeaderboard() {
        database.resetData();
        setWinnerBoard();
    }
}

class DataBase {
    public Map<String, Integer> memory;
    private File file;

    public DataBase() {
        memory = fetchContent();
    }

    public void resetData() {
        memory.clear();
    }

    public Map<String, Integer> fetchContent() {
        try {
            file = new File("data.txt");
            file.createNewFile();

            Scanner scanFile = new Scanner(new FileReader(file));
            memory = new HashMap<String, Integer>();

            while (scanFile.hasNext()) {
                String player = scanFile.next();
                int wins = Integer.parseInt(scanFile.next());
                memory.put(player, wins);
            }

            scanFile.close();

            return memory;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void updateTable(String winner) {
        if (memory.containsKey(winner)) {
            memory.put(winner, memory.get(winner) + 1);
        } else {
            memory.put(winner, 1);
        }
    }

    public void saveRecords() {
        Iterator<Map.Entry<String, Integer>> iterator = memory.entrySet().iterator();

        String saveString = "";

        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String key = entry.getKey();
            int value = entry.getValue();
            saveString = saveString + key + " " + String.valueOf(value) + "\n";
        }

        try {
            file = new File("data.txt");
            file.createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("DATA FILE DOES NOT EXIST");
        }

        try {
            PrintWriter writer = new PrintWriter(file);
            writer.print(saveString);
            writer.close();
        } catch (FileNotFoundException f) {
            throw new IllegalStateException("DATA SAVE FAILED" + f);
        }
    }
}

class MinMax {
    private static char player = 'X', algo = 'O';

    public MinMax() {
    }

    private static Boolean isMovesLeft(char board[][]) {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    return true;
                }
            }
        }
        return false;
    }

    private static int evaluate(char[][] board) {

        for (int row = 0; row < 3; row++) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2]) {
                if (board[row][0] == player) {
                    return +10;
                } else if (board[row][0] == algo) {
                    return -10;
                }
            }
        }

        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col]) {
                if (board[0][col] == player) {
                    return +10;
                } else if (board[0][col] == algo) {
                    return -10;
                }
            }
        }

        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (board[0][0] == player) {
                return +10;
            } else if (board[0][0] == algo) {
                return -10;
            }
        }

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[0][2] == player) {
                return +10;
            } else if (board[0][2] == algo) {
                return -10;
            }
        }
        return 0;
    }

    private static int minimax(char board[][], int depth, Boolean isMax) {

        int score = evaluate(board);

        if (score == 10) {
            return score - depth;
        }

        if (score == -10) {
            return score + depth;
        }

        if (!isMovesLeft(board)) {
            return 0;
        }

        if (isMax) {

            int best = -1000;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {

                    if (board[i][j] == ' ') {

                        board[i][j] = player;

                        best = Math.max(best, minimax(board, depth + 1, !isMax));

                        board[i][j] = ' ';
                    }
                }
            }
            return best;

        } else {

            int best = 1000;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {

                    if (board[i][j] == ' ') {

                        board[i][j] = algo;

                        best = Math.min(best, minimax(board, depth + 1, !isMax));

                        board[i][j] = ' ';
                    }
                }
            }
            return best;
        }
    }

    public int[] findBestMove(char board[][]) {
        int bestVal = 1000;
        int row = -1, col = -1;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                if (board[i][j] == ' ') {

                    board[i][j] = algo;

                    int moveVal = minimax(board, 0, true);

                    board[i][j] = ' ';

                    if (moveVal < bestVal) {
                        row = i;
                        col = j;
                        bestVal = moveVal;
                    }
                }
            }
        }
        int[] result = { row, col };
        return result;
    }
}