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

// Main Class TTTPAS
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
    public static WinnerBoard winBoard; // Global winBoard

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
                frame.dispose();
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

        // Add Hook To Runtime For Data-Safe Closing
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("Safe Shutdown Initiated...");
                winBoard.saveData();
                System.out.println("Safe Shutdown Done");
            }
        }, "Shutdown-thread"));

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
    private final int panelAmount = 5; // Amount Of Subpanels In TTTPanel
    private static JLabel outputLabel; // Initializes outputLabel
    private static JButton resetButton; // Initializes resetButton
    private static MinMax virtualOpponent = new MinMax(); // Initializes Class MinMax Containing The Minmax Algorithm
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

        // Create And Add Output-Label
        outputLabel = new JLabel("", SwingConstants.CENTER);
        outputLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        outputPanel.add(outputLabel);

        // Add Components To Panel
        this.add(outputPanel);
        this.add(firstRow);
        this.add(secondRow);
        this.add(thirdRow);
        this.add(resetPanel);

        // Add Border To Panel
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

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
        // Iterate Through All Rows –– If All Symbols In nth-Row Are Equal...
        for (int row = 0; row < 3; row++) {
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

        // Iterate Through All Columns –– If All Symbols In nth-Column Are Equal...
        for (int col = 0; col < 3; col++) {
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
        // If Minimax Is Active AND Symbol Is '!'
        if (IS_MINMAX && symbol == '!') {
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

    // Check If One Player Has Won
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

        // If Space On Board Is Occupied
        if (board[cords[0]][cords[1]] != ' ') {
            return true; // Return True
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
            TTTPAS.setTurn(2); // Set Turn In TTTPAS To 2
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

// Class TPanelRow
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

        // Create Buttons
        JButton quitButton = new JButton("Quit"); // Quit-Button
        activateOpponent = new JButton("Activate Virtual Opponent"); // Minimax-Button
        JButton clearMemory = new JButton("Erase Leaderbaord"); // Database-Button

        // Set Color Of Buttons To White
        activateOpponent.setBackground(Color.WHITE);
        quitButton.setBackground(Color.WHITE);
        clearMemory.setBackground(Color.WHITE);

        // Cretae Labels
        JLabel playerOneLabel = new JLabel("Player X Name:", SwingConstants.CENTER); // Label Indicating Player #1 Name
        JLabel playerTwoLabel = new JLabel("Player O Name:", SwingConstants.CENTER); // Label Indicating Player #1 Name
        JLabel noticeLabel = new JLabel("Menu", SwingConstants.CENTER); // Label Indicating Purpose Of Area
        JLabel dummyLabel = new JLabel(); // Placeholder

        // Set Color Of Labels To White
        playerOnePanel.setBackground(Color.WHITE);
        playerTwoPanel.setBackground(Color.WHITE);
        noticeLabel.setBackground(Color.WHITE);
        quitButton.setBackground(Color.WHITE);

        // Create Textfields For Playernames
        playerOneText = new JTextField();
        playerTwoText = new JTextField();
        playerOneText.setHorizontalAlignment(SwingConstants.CENTER);
        playerTwoText.setHorizontalAlignment(SwingConstants.CENTER);

        // Add Actionlisteners To Buttons
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
                System.exit(0);
            }
        });

        // Adding Components To Designated B-Subpanels
        memoryPanel.add(clearMemory);
        plusPanel.add(activateOpponent);
        playerOnePanel.add(playerOneLabel);
        playerOnePanel.add(playerOneText);
        playerTwoPanel.add(playerTwoLabel);
        playerTwoPanel.add(playerTwoText);

        // Adding B-Subpanels And Components To A-Subpanels
        instructionPanel.add(noticeLabel);
        instructionPanel.add(dummyLabel);
        instructionPanel.add(quitButton);

        mediaPanel.add(memoryPanel);
        mediaPanel.add(plusPanel);
        mediaPanel.add(playerOnePanel);
        mediaPanel.add(playerTwoPanel);

        // Adding A-Subpanels To Main-Panel
        this.add(instructionPanel);
        this.add(mediaPanel);

        // Assigning Borders To All Components
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        playerOneLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        playerTwoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        playerOneText.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        playerTwoText.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        activateOpponent.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        quitButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    // Return Dedicated Playername
    public String getPlayerName(char symbol) {
        String name;

        // If symbol Is 'X'...
        if (symbol == 'X') {
            name = playerOneText.getText(); // Get Name Of Player #1
            // If Playername Is Default...
            if (name.isEmpty()) {
                return "X"; // Return Default
            }
        } else {
            name = playerTwoText.getText(); // Get Name Of Player #1
            // If Playername Is Default...
            if (name.isEmpty()) {
                return "O"; // Return Default
            }
        }
        return name; // Return Playername
    }

    // Channge Color Of Minimax-Button
    public void changeButtonColor(Color color) {
        activateOpponent.setBackground(color);
    }
}

// Class WinnerBoard
/*
 * Displays Winnerboard
 * Accespoint To Database
 */
class WinnerBoard extends JPanel {
    private DataBase database; // Initialize Database
    private JLabel[][] playerLabels, winLabels; // Initialize Labeltypes

    public WinnerBoard() {
        this.setLayout(new GridLayout(3, 1)); // Set Gridlayout

        // Create Subpanels
        JPanel[][] subPanels = new JPanel[3][1];
        JPanel upPanel = subPanels[0][0] = new JPanel();
        JPanel midPanel = subPanels[1][0] = new JPanel();
        JPanel downPanel = subPanels[2][0] = new JPanel();

        // Set Gridlayout And Background
        upPanel.setLayout(new GridLayout(2, 1));
        midPanel.setLayout(new GridLayout(3, 2));
        downPanel.setLayout(new GridLayout(3, 2));
        midPanel.setBackground(Color.WHITE);
        downPanel.setBackground(Color.WHITE);

        // Create DescriptionPanel And Set Gridlayout
        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setLayout(new GridLayout(3, 1));

        // Create Labels
        JLabel upperDummyLabel = new JLabel(" ");
        JLabel descriptionLabel = new JLabel("Winnerboard", SwingConstants.CENTER);
        JLabel lowerDummyLabel = new JLabel(" ");
        JLabel lowDummyLabel = new JLabel(" ");

        // Add Labels To Designated Panels
        descriptionPanel.add(upperDummyLabel);
        descriptionPanel.add(descriptionLabel);
        descriptionPanel.add(lowerDummyLabel);
        upPanel.add(descriptionPanel);
        upPanel.add(lowDummyLabel);

        // Create Labels For Playernames
        playerLabels = new JLabel[7][2];
        playerLabels[0][0] = new JLabel("Playername", SwingConstants.CENTER);
        playerLabels[1][0] = new JLabel("", SwingConstants.CENTER);
        playerLabels[2][0] = new JLabel("", SwingConstants.CENTER);
        playerLabels[3][0] = new JLabel("", SwingConstants.CENTER);
        playerLabels[4][0] = new JLabel("", SwingConstants.CENTER);
        playerLabels[5][0] = new JLabel("", SwingConstants.CENTER);

        // Create Labels For Wincounts
        winLabels = new JLabel[7][2];
        winLabels[0][1] = new JLabel("Wins", SwingConstants.CENTER);
        winLabels[1][1] = new JLabel("", SwingConstants.CENTER);
        winLabels[2][1] = new JLabel("", SwingConstants.CENTER);
        winLabels[3][1] = new JLabel("", SwingConstants.CENTER);
        winLabels[4][1] = new JLabel("", SwingConstants.CENTER);
        winLabels[5][1] = new JLabel("", SwingConstants.CENTER);

        // Add Labels To First Panel
        for (int i = 0; i <= 2; i++) {
            midPanel.add(playerLabels[i][0]);
            midPanel.add(winLabels[i][1]);
        }

        // Add Labels To Second Panel
        for (int i = 3; i <= 5; i++) {
            downPanel.add(playerLabels[i][0]);
            downPanel.add(winLabels[i][1]);
        }

        // Set Borders And Backgroudns Of Labels
        for (int i = 0; i <= 5; i++) {
            playerLabels[i][0].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            winLabels[i][1].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            playerLabels[i][0].setBackground(Color.WHITE);
            winLabels[i][1].setBackground(Color.WHITE);
        }

        // Add Subpanels To Main-Panel
        this.add(upPanel);
        this.add(midPanel);
        this.add(downPanel);

        // Set Border Of Main-Panel
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Try To Create New Database
        try {
            database = new DataBase();
        } catch (Exception e) {
            throw new IllegalStateException("Database Failed", e);
        }

        // Show Received Data
        setWinnerBoard();
    }

    // Evaluate Data And Display IT
    public void setWinnerBoard() {
        Map<String, Integer> hashMap = database.getMemory(); // Get Data From Datatbase
        Iterator<Map.Entry<String, Integer>> iterator = hashMap.entrySet().iterator(); // Initialize Iterator For Data

        // Set Default Values For Leaderboard
        String[] winnerNames = { "N/A", "N/A", "N/A", "N/A", "N/A" };
        int[] mostWins = { 0, 0, 0, 0, 0 }; //

        // Iterate Through Data
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next(); // Get Group
            String key = entry.getKey(); // Srore Key
            int value = entry.getValue(); // Store Value

            boolean guard = true; // Add Guard To Stop Iteration Once Item Is Set

            // Iterate Through Stored Leaderboard
            for (int i = 0; i < 5; i++) {
                // If Guard Is Active AND Value Is Greater Than Value In nth-Place
                if (value > mostWins[i] && guard) {
                    // Iterate Through List from Back To nth-Place
                    for (int j = 4; j > i; j--) {
                        // Moving Wins And Names Towards End Of Arrays
                        mostWins[j] = mostWins[j - 1];
                        winnerNames[j] = winnerNames[j - 1];
                    }
                    // Insert Values At nth-Place
                    mostWins[i] = value;
                    winnerNames[i] = key;
                    guard = false; // Deactivate Updates
                }
            }
        }

        // Display Values On Labels
        for (int i = 0; i < 5; i++) {
            playerLabels[i + 1][0].setText(winnerNames[i]);
            winLabels[i + 1][1].setText(String.valueOf(mostWins[i]));
        }
    }

    // Send New Winner To Database And Update Leaderboard
    public void updateDatabase(String name) {
        database.updateTable(name);
        setWinnerBoard();
    }

    // Save Last Leaderboard
    public void saveData() {
        database.saveRecords();
    }

    // Reset Database and Leaderboard
    public void clearLeaderboard() {
        database.resetData();
        setWinnerBoard();
    }
}

// Class Database
/*
 * .txt-File Interaction
 */
class DataBase {
    protected Map<String, Integer> memory; // Initializing Runtime-Memory

    // Store Data In Runtime-Memory
    public DataBase() {
        memory = fetchContent();
    }

    // Delete Runtime-Memory
    public void resetData() {
        memory.clear();
    }

    // Fetch Contents From .txt-File ["data.txt"]
    public Map<String, Integer> fetchContent() {
        // Try To Connect To .txt-File
        try {
            // Open New File Object And Check If It Exists/Create New File
            File file = new File("data.txt");
            file.createNewFile();

            Scanner scanFile = new Scanner(new FileReader(file)); // Create New Reader For File
            Map<String, Integer> fetch = new HashMap<String, Integer>(); // Assign New Hashmap For Runtime-Memory

            // Iterate Through Contents Of .txt-File
            while (scanFile.hasNext()) {
                // Get Playernames And Wincounts And Update Runtime-Memory
                String player = scanFile.next();
                int wins = Integer.parseInt(scanFile.next());
                fetch.put(player, wins);
            }

            scanFile.close(); // Close Reader
            return fetch; // Return Fetched-Data

        } catch (IOException e) {
            throw new IllegalStateException(e); // Throw Exception If Fetch Fails
        }
    }

    // Update Runtime-Memory
    public void updateTable(String winner) {
        // If Player Already In Rumtime Memory...
        if (memory.containsKey(winner)) {
            memory.put(winner, memory.get(winner) + 1); // Increment Wincount
        } else {
            memory.put(winner, 1); // Else Initialize New Notice
        }
    }

    // Save Runtime-Memory To .txt-File
    public void saveRecords() {
        Iterator<Map.Entry<String, Integer>> iterator = memory.entrySet().iterator(); // Initialize Iterator
        String saveString = ""; // Initialize String For Save

        // Iterate Through All Groups In Runtime-Memory
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String key = entry.getKey(); // Get Playername
            int value = entry.getValue(); // Get Wincount
            saveString = saveString + key + " " + String.valueOf(value) + "\n"; // Concat String
        }

        // Print Data To .txt-File
        try {
            // Open New File Object And Check If It Exists/Create New File
            File file = new File("data.txt");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(file); // Create Wirter For File
            writer.print(saveString); // Print String To File
            writer.close(); // Close Writer

        } catch (IOException e) {
            // Throw Exception If Save Failed
            throw new IllegalStateException("DATA SAVE FAILED - saveRecords", e);
        }
    }

    // Return Runtime-Memory Contents
    public Map<String, Integer> getMemory() {
        return memory;
    }
}

// Class Minimax
/*
 * Virtual Opponent For Singleplayer
 */
class MinMax {
    private static char player = 'X', algo = 'O'; // Initialize Player And Minimax Symbols

    public MinMax() {
    }

    // Check For Empty Cells On Board
    private static Boolean isMovesLeft(char board[][]) {
        // Iterate Through Board
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    return true; // Return True If One Cell Is Empty
                }
            }
        }
        return false; // Return False If All Cells Are Filled
    }

    // Return Value Of Board State
    private static int evaluate(char[][] board) {
        // Check If Row Is Won
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2]) {
                // If Player Won...
                if (board[row][0] == player) {
                    return +10; // Return +10
                } else if (board[row][0] == algo) {
                    return -10; // Else Return -10
                }
            }
        }

        // Check If Column Is Won
        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col]) {
                // If Player Won...
                if (board[0][col] == player) {
                    return +10; // Return +10
                } else if (board[0][col] == algo) {
                    return -10; // Else Return -10
                }
            }
        }

        // Check If Diagonal Is Won
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            // If Player Won...
            if (board[0][0] == player) {
                return +10; // Return +10
            } else if (board[0][0] == algo) {
                return -10; // Else Return -10
            }
        }

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            // If Player Won...
            if (board[0][2] == player) {
                return +10; // Return +10
            } else if (board[0][2] == algo) {
                return -10; // Else Return -10
            }
        }
        return 0; // Return 0 If Nobody Won
    }

    // Find Move Values In Search-Tree
    private static int minimax(char board[][], int depth, Boolean isMax) {
        int score = evaluate(board);

        // If Score Of Current Position Is 10...
        if (score == 10) {
            return score - depth; // Return Score - Amount Of Moves Until This Position
        }

        // If Score Of Current Position Is -10...
        if (score == -10) {
            return score + depth; // Return Score + Amount Of Moves Until This Position
        }

        // If Board Is In Draw State...
        if (!isMovesLeft(board)) {
            return 0; // Return 0
        }

        // If Party Is Maximizing
        if (isMax) {
            int best = -1000; // Set Low Value

            // Iterate Through All Cells
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    // If Cell Is Empty...
                    if (board[i][j] == ' ') {
                        board[i][j] = player; // Set Temporary Player-Move
                        // Recursive Call --> Pass Up Highest Value
                        best = Math.max(best, minimax(board, depth + 1, !isMax));
                        board[i][j] = ' '; // Undo Player-Move
                    }
                }
            }
            return best; // Return Best Value For Maximizer [best --> Positive]

            // IF Party Is Minimizing
        } else {
            int best = 1000; // Set High Value

            // Iterate Through All Cells
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    // If Cell Is Empty...
                    if (board[i][j] == ' ') {
                        board[i][j] = algo; // Set Temporary Minimax-Move
                        // Recursive Call --> Pass Up Lowest Value
                        best = Math.min(best, minimax(board, depth + 1, !isMax));
                        board[i][j] = ' '; // Undo Minimax-Move
                    }
                }
            }
            return best; // Return Best Value For Minimizer [best --> Negative]
        }
    }

    // Begin Search For Best Minimax-Move For Passed Board
    public int[] findBestMove(char board[][]) {
        int bestVal = 1000; // Set High Value
        int row = -1, col = -1; // Initialize Row And Column Parameters

        // Iterate Through All Cells
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // If Cell Is Empty...
                if (board[i][j] == ' ') {
                    board[i][j] = algo; // Set Temporary Minimax-Move
                    // Recursive Call --> Pass Up Lowest Value
                    int moveVal = minimax(board, 0, true);
                    board[i][j] = ' '; // Undo Minimax-Move

                    // If Value Is Lower Than Last Smallest Value...
                    if (moveVal < bestVal) {
                        // Store Row And Column And Value
                        row = i;
                        col = j;
                        bestVal = moveVal;
                    }
                }
            }
        }
        int[] result = { row, col }; // Combine Row And Column In Array
        return result; // Return Array
    }
}