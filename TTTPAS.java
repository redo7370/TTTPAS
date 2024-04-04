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
    // Global Board + Global Turn Indicator + Global Access To playPanel
    private static char[][] board = { { ' ', ' ', ' ' }, { ' ', ' ', ' ' }, { ' ', ' ', ' ' } };
    private static int turn = 0;
    public static PlayPanel playPanel;
    public static WinnerBoard winBoard;

    // Adding Components To Main Frame
    public static void GUI() {

        JFrame frame = new JFrame("TicTacToe – Type PAS");
        frame.setSize(800, 500);

        JPanel mainFramePanel = new JPanel();
        mainFramePanel.setLayout(new GridLayout(1, 3));

        playPanel = new PlayPanel();
        TTTPanel ttt = new TTTPanel();

        winBoard = new WinnerBoard();

        mainFramePanel.add(playPanel);
        mainFramePanel.add(ttt);
        mainFramePanel.add(winBoard);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                winBoard.saveData();
                System.exit(0);
            }
        });

        frame.getContentPane().add(mainFramePanel);
        frame.setVisible(true);
    }

    // Return Current Board To Calling Function
    public static char[][] getField() {
        return board;
    }

    // Update Board
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

    // Set The Turn
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
                GUI();
            }
        });
    }
}

// Class TTTPanel
/*
 * Contains The General Game
 * - Concatates The Board
 * - Defines The Basic Game Routine
 */
class TTTPanel extends JPanel {
    public static TPanelRow firstRow, secondRow, thirdRow; // Rows Of The TicTacToe Board From Subclass TPanelRow
    private int panelAmount = 5; // Amount Of Subpanels In TTTPanel
    private static JLabel outputLabel; // Initializes outputLabel
    private static JButton resetButton; // Initializes resetButton
    private static MinMax virtualOpponent = new MinMax(); // Initializes Class MinMax Containing The Minnax Algorithm
    private static boolean IS_MINMAX = false;
    private static boolean ALLOW_MINMAX_INTERACTION = true;
    private static boolean GAME_END = false;

    // Basic Structure Of The TicTacToe Field And Directly Associated Components
    public TTTPanel() {
        this.setLayout(new GridLayout(panelAmount, 1));

        // Create SubPanels for different use cases
        JPanel[][] subPanels = new JPanel[panelAmount][1];
        JPanel outputPanel = subPanels[0][0] = new JPanel();
        JPanel resetPanel = subPanels[4][0] = new JPanel();
        outputPanel.setLayout(new GridLayout(1, 1));
        resetPanel.setLayout(new GridLayout(1, 1));

        TPanelRow[][] tRows = new TPanelRow[panelAmount][1];
        firstRow = tRows[1][0] = new TPanelRow();
        secondRow = tRows[2][0] = new TPanelRow();
        thirdRow = tRows[3][0] = new TPanelRow();

        resetButton = new JButton("Reset");
        resetButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        resetPanel.add(resetButton);

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                Reset();
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

        firstRow.left.setText("");
        firstRow.mid.setText("");
        firstRow.right.setText("");
        secondRow.left.setText("");
        secondRow.mid.setText("");
        secondRow.right.setText("");
        thirdRow.left.setText("");
        thirdRow.mid.setText("");
        thirdRow.right.setText("");
        outputLabel.setText(String.format("Click A Field To Begin"));

        TTTPAS.setTurn(2);
        TTTPAS.resetField();

        TTTPAS.playPanel.changeButtonColor(Color.WHITE);

        ALLOW_MINMAX_INTERACTION = true;
        GAME_END = false;
    }

    public static void gameHandler(Container buttonParent, String position) {
        if (!isSlotFree(TTTPAS.getField())) {
            return;
        }

        if (GAME_END) {
            return;
        }

        ALLOW_MINMAX_INTERACTION = false;

        char step = newStep();
        int[] fieldCoordinates = buttonOrientation(buttonParent, position);

        if (illegalMove(fieldCoordinates)) {
            return;
        }

        TTTPAS.setField(fieldCoordinates, step);
        guiSetMove(fieldCoordinates, step);

        if (checkWin(TTTPAS.getField())) {
            GAME_END = true;
            declareWin(step);
            showWin(TTTPAS.getField());
            return;
        }

        if (!isSlotFree(TTTPAS.getField())) {
            outputLabel.setText("Draw");
            return;
        }

        outputLabel.setText(String.format("%s's Turn", TTTPAS.playPanel.getPlayerName(step)));
        if (!IS_MINMAX) {
            return;
        }

        // ----- !!! END OF PLAYER MOVES !!! -----

        if (!isSlotFree(TTTPAS.getField())) {
            return;
        }

        if (GAME_END) {
            return;
        }

        step = newStep();
        fieldCoordinates = virtualOpponent.findBestMove(TTTPAS.getField());

        if (illegalMove(fieldCoordinates)) {
            return;
        }

        TTTPAS.setField(fieldCoordinates, step);
        guiSetMove(fieldCoordinates, step);

        if (checkWin(TTTPAS.getField())) {
            GAME_END = true;
            declareWin('!');
            showWin(TTTPAS.getField());
            return;
        }

        if (!isSlotFree(TTTPAS.getField())) {
            outputLabel.setText("Draw");
            return;
        }
    }

    private static void showWin(char[][] board) {
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2] && board[row][0] != ' ') {
                switch (row) {
                    case 0:
                        firstRow.left.setBackground(Color.GREEN);
                        firstRow.mid.setBackground(Color.GREEN);
                        firstRow.right.setBackground(Color.GREEN);
                        break;
                    case 1:
                        secondRow.left.setBackground(Color.GREEN);
                        secondRow.mid.setBackground(Color.GREEN);
                        secondRow.right.setBackground(Color.GREEN);
                        break;
                    case 2:
                        thirdRow.left.setBackground(Color.GREEN);
                        thirdRow.mid.setBackground(Color.GREEN);
                        thirdRow.right.setBackground(Color.GREEN);
                        break;
                }
            }
        }

        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col] && board[0][col] != ' ') {
                switch (col) {
                    case 0:
                        firstRow.left.setBackground(Color.GREEN);
                        secondRow.left.setBackground(Color.GREEN);
                        thirdRow.left.setBackground(Color.GREEN);
                        break;
                    case 1:
                        firstRow.mid.setBackground(Color.GREEN);
                        secondRow.mid.setBackground(Color.GREEN);
                        thirdRow.mid.setBackground(Color.GREEN);
                        break;
                    case 2:
                        firstRow.right.setBackground(Color.GREEN);
                        secondRow.right.setBackground(Color.GREEN);
                        thirdRow.right.setBackground(Color.GREEN);
                        break;
                }
            }
        }

        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[1][1] != ' ') {
            firstRow.left.setBackground(Color.GREEN);
            secondRow.mid.setBackground(Color.GREEN);
            thirdRow.right.setBackground(Color.GREEN);
        }

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[1][1] != ' ') {
            firstRow.right.setBackground(Color.GREEN);
            secondRow.mid.setBackground(Color.GREEN);
            thirdRow.left.setBackground(Color.GREEN);
        }
    }

    private static void declareWin(char symbol) {
        if (IS_MINMAX && symbol == '!') {
            outputLabel.setText("Winner: MINIMAX");
            return;
        } else if (IS_MINMAX && symbol != '!') {
            String winner = TTTPAS.playPanel.getPlayerName(symbol);
            outputLabel.setText("Winner: " + winner);
            return;
        }

        String winner = TTTPAS.playPanel.getPlayerName(symbol);
        outputLabel.setText("Winner: " + winner);
        if (winner != "X" && winner != "O") {
            TTTPAS.winBoard.updateDatabase(winner);
        }
    }

    private static boolean checkWin(char[][] board) {
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2] && board[row][0] != ' ') {
                return true;
            }
        }

        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col] && board[0][col] != ' ') {
                return true;
            }
        }

        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[1][1] != ' ') {
            return true;
        }

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[1][1] != ' ') {
            return true;
        }

        return false;
    }

    private static boolean isSlotFree(char[][] board) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == ' ') {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean illegalMove(int[] cords) {
        char[][] board = TTTPAS.getField();
        if (board[cords[0]][cords[1]] != ' ') {
            return true;
        }
        return false;
    }

    private static void guiSetMove(int[] cords, char move) {
        String text = String.valueOf(move);

        switch (cords[0]) {
            case 0:
                switch (cords[1]) {
                    case 0:
                        firstRow.left.setText(text);
                        break;
                    case 1:
                        firstRow.mid.setText(text);
                        break;
                    case 2:
                        firstRow.right.setText(text);
                        break;
                    default:
                        System.exit(0);
                        break;
                }
                break;
            case 1:
                switch (cords[1]) {
                    case 0:
                        secondRow.left.setText(text);
                        break;
                    case 1:
                        secondRow.mid.setText(text);
                        break;
                    case 2:
                        secondRow.right.setText(text);
                        break;
                    default:
                        System.exit(0);
                        break;
                }
                break;

            case 2:
                switch (cords[1]) {
                    case 0:
                        thirdRow.left.setText(text);
                        break;
                    case 1:
                        thirdRow.mid.setText(text);
                        break;
                    case 2:
                        thirdRow.right.setText(text);
                        break;
                    default:
                        System.exit(0);
                        break;
                }
                break;

            default:
                System.exit(0);
                break;
        }
    }

    private static char newStep() {
        int turnNum = TTTPAS.getTurn();
        if (turnNum % 2 == 0) {
            TTTPAS.setTurn(1);
            return 'X';
        } else {
            TTTPAS.setTurn(2);
            return 'O';
        }
    }

    public static int[] buttonOrientation(Container parent, String buttonPosition) {
        List<Container> first = firstRow.selfParent();
        List<Container> second = secondRow.selfParent();
        List<Container> third = thirdRow.selfParent();

        int[] cords = new int[2];

        if (first.contains(parent)) {
            cords[0] = 0;
        } else if (second.contains(parent)) {
            cords[0] = 1;
        } else if (third.contains(parent)) {
            cords[0] = 2;
        }

        switch (buttonPosition) {
            case "left":
                cords[1] = 0;
                break;

            case "mid":
                cords[1] = 1;
                break;

            case "right":
                cords[1] = 2;
                break;

            default:
                break;
        }
        return cords;
    }

    public static void setMinimaxActive() {
        if (!IS_MINMAX) {
            IS_MINMAX = true;
        } else {
            IS_MINMAX = false;
        }
    }

    public static boolean isMinimaxActive() {
        return IS_MINMAX;
    }

    public static boolean getPermission() {
        return ALLOW_MINMAX_INTERACTION;
    }

}

class TPanelRow extends JPanel {
    private Container parent;
    public JButton left, mid, right;
    private List<Container> parentList;

    public TPanelRow() {
        this.setLayout(new GridLayout(1, 3));
        JButton[][] topButtons = new JButton[1][3];
        left = topButtons[0][0] = new JButton("1");
        mid = topButtons[0][1] = new JButton("2");
        right = topButtons[0][2] = new JButton("3");
        this.add(left);
        this.add(mid);
        this.add(right);

        left.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mid.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        right.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        left.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (left.getText() != "") {
                    return;
                }
                parent = left.getParent();
                callGameHandler(parent, "left");
            }
        });
        mid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (mid.getText() != "") {
                    return;
                }
                parent = mid.getParent();
                callGameHandler(parent, "mid");
            }
        });
        right.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (right.getText() != "") {
                    return;
                }
                parent = right.getParent();
                callGameHandler(parent, "right");
            }
        });
    }

    public List<Container> selfParent() {
        parentList = Arrays.asList(left.getParent(), mid.getParent(), right.getParent());
        return parentList;
    }

    protected static void callGameHandler(Container parent, String position) {
        TTTPanel.gameHandler(parent, position);
    }

}

class PlayPanel extends JPanel {

    private static JTextField playerOneText, playerTwoText;
    private static JButton activateOpponent;

    public PlayPanel() {
        this.setLayout(new GridLayout(2, 1));

        JPanel mediaPanel = new JPanel();
        JPanel instructionPanel = new JPanel();
        mediaPanel.setLayout(new GridLayout(4, 1));
        instructionPanel.setLayout(new GridLayout(3, 1));

        JPanel[][] subPanels = new JPanel[4][1];
        JPanel memoryPanel = subPanels[0][0] = new JPanel();
        JPanel plusPanel = subPanels[1][0] = new JPanel();
        JPanel playerOnePanel = subPanels[2][0] = new JPanel();
        JPanel playerTwoPanel = subPanels[3][0] = new JPanel();
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
                if (board[row][0] == algo) {
                    return +10;
                } else if (board[row][0] == player) {
                    return -10;
                }
            }
        }

        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col]) {
                if (board[0][col] == algo) {
                    return +10;
                } else if (board[0][col] == player) {
                    return -10;
                }
            }
        }

        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (board[0][0] == algo) {
                return +10;
            } else if (board[0][0] == player) {
                return -10;
            }
        }

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[0][2] == algo) {
                return +10;
            } else if (board[0][2] == player) {
                return -10;
            }
        }
        return 0;
    }

    private static int minimax(char board[][], int depth, Boolean isMax, int alpha, int beta) {

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

            for (int i = 0; i <= 2; i++) {
                for (int j = 0; j <= 2; j++) {

                    if (board[i][j] == ' ') {

                        board[i][j] = algo;

                        score = Math.max(score, minimax(board, depth + 1, !isMax, alpha, beta));

                        board[i][j] = ' ';

                        if (score >= beta) {
                            return beta;
                        }

                        if (score > alpha) {
                            alpha = score;
                        }
                    }
                }
            }
            return alpha;

        } else {

            for (int i = 0; i <= 2; i++) {
                for (int j = 0; j <= 2; j++) {

                    if (board[i][j] == ' ') {

                        board[i][j] = player;

                        score = Math.min(score, minimax(board, depth + 1, isMax, alpha, beta));

                        board[i][j] = ' ';

                        if (score <= alpha) {
                            return alpha;
                        }

                        if (score < beta) {
                            beta = score;
                        }
                    }
                }
            }
            return beta;
        }
    }

    public int[] findBestMove(char board[][]) {
        int bestVal = -1000;
        int row = 9, col = 9;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                if (board[i][j] == ' ') {

                    board[i][j] = algo;

                    int moveVal = minimax(board, 0, false, -1000, 1000);

                    board[i][j] = ' ';

                    if (moveVal > bestVal) {
                        row = i;
                        col = j;
                        bestVal = moveVal;
                    }
                }
            }
        }

        System.out.println(" ");
        System.out.println(bestVal);
        System.out.println(row + " " + col);
        System.out.println(" ");

        int[] result = { row, col };

        return result;
    }
}