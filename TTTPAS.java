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
    public static void setField(int row, int column, char sign) {
        board[row][column] = sign;
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
    private static char[][] currentBoard;
    public static boolean gameStatus = true;
    private static boolean BOT_ACTIVE = false;
    private static boolean gameStart = true;
    private static boolean countWinner = true;
    public static TPanelRow firstRow, secondRow, thirdRow;
    private int panelAmount = 5;
    private static List<Container> first, second, third;
    private static int row, column;
    private static List<String> instructionList;
    private static JLabel outputLabel;
    private static JButton resetButton;
    private static MinMax virtualOpponent = new MinMax();

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
                guiReset();
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

        guiReset();
    }

    public static void guiReset() {

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

        gameStart = true;
        gameStatus = true;
        countWinner = true;

        TTTPAS.setTurn(2);
        TTTPAS.resetField();

        TTTPAS.playPanel.changeButtonColor(Color.WHITE);
    }

    public static char setStep() {
        if (BOT_ACTIVE) {
            return 'X';
        }
        if (TTTPAS.getTurn() % 2 == 0) {
            TTTPAS.setTurn(1);
            return 'X';
        } else if (TTTPAS.getTurn() % 2 == 1) {
            TTTPAS.setTurn(2);
            return 'O';
        }
        return ' ';
    }

    public static String gameHandler(List<String> position) {
        gameStart = false;
        TTTPAS.playPanel.changeButtonColor(Color.GRAY);

        if (!gameStatus) {
            return " ";
        }
        currentBoard = TTTPAS.getField();

        if (invalidTurn(currentBoard, row, column)) {
            return " ";
        }

        // Setting the next varible to be shown
        char step = setStep();

        TTTPAS.setField(row, column, step);

        instructionList = checkWin(TTTPAS.getField(), step);

        switch (instructionList.get(0)) {
            case "row":
                switch (instructionList.get(1)) {
                    case "0":
                        firstRow.left.setBackground(Color.GREEN);
                        firstRow.mid.setBackground(Color.GREEN);
                        firstRow.right.setBackground(Color.GREEN);
                        break;

                    case "1":
                        secondRow.left.setBackground(Color.GREEN);
                        secondRow.mid.setBackground(Color.GREEN);
                        secondRow.right.setBackground(Color.GREEN);
                        break;

                    case "2":
                        thirdRow.left.setBackground(Color.GREEN);
                        thirdRow.mid.setBackground(Color.GREEN);
                        thirdRow.right.setBackground(Color.GREEN);
                        break;

                    default:
                        break;
                }
                break;

            case "column":
                switch (instructionList.get(1)) {
                    case "0":
                        firstRow.left.setBackground(Color.GREEN);
                        secondRow.left.setBackground(Color.GREEN);
                        thirdRow.left.setBackground(Color.GREEN);
                        break;

                    case "1":
                        firstRow.mid.setBackground(Color.GREEN);
                        secondRow.mid.setBackground(Color.GREEN);
                        thirdRow.mid.setBackground(Color.GREEN);
                        break;

                    case "2":
                        firstRow.right.setBackground(Color.GREEN);
                        secondRow.right.setBackground(Color.GREEN);
                        thirdRow.right.setBackground(Color.GREEN);
                        break;

                    default:
                        break;
                }
                break;

            case "TB":
                firstRow.left.setBackground(Color.GREEN);
                secondRow.mid.setBackground(Color.GREEN);
                thirdRow.right.setBackground(Color.GREEN);
                break;

            case "BT":
                firstRow.right.setBackground(Color.GREEN);
                secondRow.mid.setBackground(Color.GREEN);
                thirdRow.left.setBackground(Color.GREEN);
                break;

            default:
                if (isSlotFree()) {
                    outputLabel.setText(String.format("%s's Turn", TTTPAS.playPanel.getPlayerNames().get(0)));
                } else {
                    outputLabel.setText("Draw");
                }
                break;

        }

        if (BOT_ACTIVE && gameStatus) {
            List<Integer> oppMove = virtualOpponent.findBestMove(TTTPAS.getField());

            switch (oppMove.get(0)) {
                case 0:
                    TTTPAS.setField(oppMove.get(0), oppMove.get(1), 'O');
                    switch (oppMove.get(1)) {
                        case 0:
                            firstRow.left.setText(String.valueOf('O'));
                            break;

                        case 1:
                            firstRow.mid.setText(String.valueOf('O'));
                            break;

                        case 2:
                            firstRow.right.setText(String.valueOf('O'));
                            break;

                        default:
                            break;
                    }
                    break;

                case 1:
                    TTTPAS.setField(oppMove.get(0), oppMove.get(1), 'O');
                    switch (oppMove.get(1)) {
                        case 0:
                            secondRow.left.setText(String.valueOf('O'));
                            break;

                        case 1:
                            secondRow.mid.setText(String.valueOf('O'));
                            break;

                        case 2:
                            secondRow.right.setText(String.valueOf('O'));
                            break;

                        default:
                            break;
                    }
                    break;

                case 2:
                    TTTPAS.setField(oppMove.get(0), oppMove.get(1), 'O');
                    switch (oppMove.get(1)) {
                        case 0:
                            thirdRow.left.setText(String.valueOf('O'));
                            break;

                        case 1:
                            thirdRow.mid.setText(String.valueOf('O'));
                            break;

                        case 2:
                            thirdRow.right.setText(String.valueOf('O'));
                            break;

                        default:
                            break;
                    }
                    break;

                default:
                    char[][] drawBoard = TTTPAS.getField();
                    boolean space = false;
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {

                            if (drawBoard[i][j] == ' ' && !space) {

                                switch (i) {
                                    case 0:
                                        switch (j) {
                                            case 0:
                                                TTTPAS.setField(i, j, 'O');
                                                firstRow.left.setText(String.valueOf('O'));
                                                break;

                                            case 1:
                                                TTTPAS.setField(i, j, 'O');
                                                firstRow.mid.setText(String.valueOf('O'));
                                                break;

                                            case 2:
                                                TTTPAS.setField(i, j, 'O');
                                                firstRow.right.setText(String.valueOf('O'));
                                                break;

                                            default:
                                                break;
                                        }
                                        break;

                                    case 1:
                                        switch (j) {
                                            case 0:
                                                TTTPAS.setField(i, j, 'O');
                                                secondRow.left.setText(String.valueOf('O'));
                                                break;

                                            case 1:
                                                TTTPAS.setField(i, j, 'O');
                                                secondRow.mid.setText(String.valueOf('O'));
                                                break;

                                            case 2:
                                                TTTPAS.setField(i, j, 'O');
                                                secondRow.right.setText(String.valueOf('O'));
                                                break;

                                            default:
                                                break;
                                        }
                                        break;

                                    case 2:
                                        switch (j) {
                                            case 0:
                                                TTTPAS.setField(i, j, 'O');
                                                thirdRow.left.setText(String.valueOf('O'));
                                                break;

                                            case 1:
                                                TTTPAS.setField(i, j, 'O');
                                                thirdRow.mid.setText(String.valueOf('O'));
                                                break;

                                            case 2:
                                                TTTPAS.setField(i, j, 'O');
                                                thirdRow.right.setText(String.valueOf('O'));
                                                break;

                                            default:
                                                break;
                                        }
                                        break;
                                }
                                space = true;
                            }

                        }

                    }

                    break;
            }
        }

        if (BOT_ACTIVE) {
            instructionList = checkWin(TTTPAS.getField(), step);

            switch (instructionList.get(0)) {
                case "row":
                    switch (instructionList.get(1)) {
                        case "0":
                            firstRow.left.setBackground(Color.GREEN);
                            firstRow.mid.setBackground(Color.GREEN);
                            firstRow.right.setBackground(Color.GREEN);
                            break;

                        case "1":
                            secondRow.left.setBackground(Color.GREEN);
                            secondRow.mid.setBackground(Color.GREEN);
                            secondRow.right.setBackground(Color.GREEN);
                            break;

                        case "2":
                            thirdRow.left.setBackground(Color.GREEN);
                            thirdRow.mid.setBackground(Color.GREEN);
                            thirdRow.right.setBackground(Color.GREEN);
                            break;

                        default:
                            break;
                    }
                    break;

                case "column":
                    switch (instructionList.get(1)) {
                        case "0":
                            firstRow.left.setBackground(Color.GREEN);
                            secondRow.left.setBackground(Color.GREEN);
                            thirdRow.left.setBackground(Color.GREEN);
                            break;

                        case "1":
                            firstRow.mid.setBackground(Color.GREEN);
                            secondRow.mid.setBackground(Color.GREEN);
                            thirdRow.mid.setBackground(Color.GREEN);
                            break;

                        case "2":
                            firstRow.right.setBackground(Color.GREEN);
                            secondRow.right.setBackground(Color.GREEN);
                            thirdRow.right.setBackground(Color.GREEN);
                            break;

                        default:
                            break;
                    }
                    break;

                case "TB":
                    firstRow.left.setBackground(Color.GREEN);
                    secondRow.mid.setBackground(Color.GREEN);
                    thirdRow.right.setBackground(Color.GREEN);
                    break;

                case "BT":
                    firstRow.right.setBackground(Color.GREEN);
                    secondRow.mid.setBackground(Color.GREEN);
                    thirdRow.left.setBackground(Color.GREEN);
                    break;

                default:
                    if (isSlotFree()) {
                        outputLabel.setText(String.format("%s's Turn", TTTPAS.playPanel.getPlayerNames().get(0)));
                    } else {
                        outputLabel.setText("Draw");
                    }
                    break;
            }
        }

        return String.valueOf(step);

    }

    // Checks is space is empty
    private static boolean invalidTurn(char[][] board, int r, int c) {
        if (board[r][c] == ' ') {
            return false;
        }
        return true;
    }

    private static boolean isSlotFree() {
        char[][] field = TTTPAS.getField();

        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (field[i][j] == ' ') {
                    return true;
                }
            }
        }

        return false;
    }

    // Checks if TicTacToe is fulfilled
    private static List<String> checkWin(char[][] cBoard, char next) {
        if (next == 'X') {
            next = 'O';
        } else if (next == 'O') {
            next = 'X';
        }

        for (int i = 0; i <= 2; i++) {
            if ((cBoard[i][0] != ' ') && (cBoard[i][0] == cBoard[i][1])
                    && (cBoard[i][0] == cBoard[i][2])) {

                outputLabel.setText(displayWinner(String.valueOf(cBoard[i][0])));
                return Arrays.asList("row", String.valueOf(i), String.valueOf(cBoard[i][0]));

            }
            if ((cBoard[0][i] != ' ') && (cBoard[0][i] == cBoard[1][i])
                    && (cBoard[0][i] == cBoard[2][i])) {

                outputLabel.setText(displayWinner(String.valueOf(cBoard[0][i])));
                return Arrays.asList("column", String.valueOf(i), String.valueOf(cBoard[0][i]));

            }

        }

        if ((cBoard[0][0] != ' ') && (cBoard[0][0] == cBoard[1][1])
                && (cBoard[0][0] == cBoard[2][2])) {

            outputLabel.setText(displayWinner(String.valueOf(cBoard[1][1])));
            return Arrays.asList("TB", "N/A", String.valueOf(cBoard[1][1]));

        }
        if ((cBoard[2][0] != ' ') && (cBoard[2][0] == cBoard[1][1])
                && (cBoard[2][0] == cBoard[0][2])) {

            outputLabel.setText(displayWinner(String.valueOf(cBoard[1][1])));
            return Arrays.asList("BT", "N/A", String.valueOf(cBoard[1][1]));

        }

        return Arrays.asList("N/A", "N/A", String.valueOf(next));
    }

    private static String displayWinner(String winner) {
        gameStatus = false;

        switch (winner) {
            case "X":
                winner = TTTPAS.playPanel.getPlayerNames().get(0);

                if (countWinner && winner != "X") {
                    countWinner = false;
                    TTTPAS.winBoard.updateDatabase(winner);
                }

                return String.format("Winner: %s", winner);

            case "O":
                if (BOT_ACTIVE) {
                    return "Winner: AI";
                }

                winner = TTTPAS.playPanel.getPlayerNames().get(1);

                if (countWinner && winner != "O") {
                    countWinner = false;
                    TTTPAS.winBoard.updateDatabase(winner);
                }

                return String.format("Winner: %s", winner);

            default:
                return " ";
        }
    }

    public static List<String> orientationButton(Container parent, String position) {
        first = firstRow.selfParent();
        second = secondRow.selfParent();
        third = thirdRow.selfParent();

        if (first.contains(parent)) {
            row = 0;
        } else if (second.contains(parent)) {
            row = 1;
        } else if (third.contains(parent)) {
            row = 2;
        }

        switch (position) {
            case "left":
                column = 0;
                break;

            case "mid":
                column = 1;
                break;

            case "right":
                column = 2;
                break;

            default:
                break;
        }

        return Arrays.asList(String.valueOf(row), String.valueOf(column));

    }

    public static void setBotActive() {
        if (!BOT_ACTIVE) {
            BOT_ACTIVE = true;
        } else {
            BOT_ACTIVE = false;
        }
    }

    public static boolean isBotActive() {
        return BOT_ACTIVE;
    }

    public static boolean getPermission() {
        return gameStart;
    }

}

class TPanelRow extends JPanel {
    private Container parent;
    public JButton left, mid, right;
    private List<Container> parentList;
    private static String proceed;

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
                parent = left.getParent();
                proceed = buttonFieldEvent(parent, "left");
                if (proceed != " ") {
                    left.setText(proceed);
                }
            }
        });
        mid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                parent = mid.getParent();
                proceed = buttonFieldEvent(parent, "mid");
                if (proceed != " ") {
                    mid.setText(proceed);
                }
            }
        });
        right.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                parent = right.getParent();
                proceed = buttonFieldEvent(parent, "right");
                if (proceed != " ") {
                    right.setText(proceed);
                }
            }
        });
    }

    public List<Container> selfParent() {

        parentList = Arrays.asList(left.getParent(), mid.getParent(), right.getParent());
        return parentList;

    }

    private static String buttonFieldEvent(Container parent, String position) {

        proceed = TTTPanel.gameHandler(TTTPanel.orientationButton(parent, position));

        return proceed;
    }

}

class PlayPanel extends JPanel {

    private static JTextField playerOneText, playerTwoText;
    private static JButton activateOpponent;

    public PlayPanel() {
        this.setLayout(new GridLayout(2, 1));

        JPanel mediaPanel = new JPanel();
        JPanel instructionPanel = new JPanel();
        mediaPanel.setLayout(new GridLayout(3, 1));
        instructionPanel.setLayout(new GridLayout(3, 1));

        JPanel[][] subPanels = new JPanel[3][1];
        JPanel botPanel = subPanels[0][0] = new JPanel();
        JPanel playerOnePanel = subPanels[1][0] = new JPanel();
        JPanel playerTwoPanel = subPanels[2][0] = new JPanel();
        botPanel.setLayout(new GridLayout(1, 1));
        playerOnePanel.setLayout(new GridLayout(1, 2));
        playerTwoPanel.setLayout(new GridLayout(1, 2));

        JButton quitButton = new JButton("Quit");
        activateOpponent = new JButton("Activate Virtual Opponent");

        activateOpponent.setBackground(Color.WHITE);
        quitButton.setBackground(Color.WHITE);

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
                    TTTPanel.setBotActive();
                    if (TTTPanel.isBotActive()) {
                        playerTwoPanel.setBackground(Color.GRAY);
                        activateOpponent.setText("Deactivate Virtual Opponent");
                    } else {
                        playerTwoPanel.setBackground(Color.WHITE);
                        activateOpponent.setText("Activate Virtual Opponent");
                    }
                }
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                TTTPAS.winBoard.saveData();
                System.exit(0);
            }
        });

        botPanel.add(activateOpponent);
        playerOnePanel.add(playerOneLabel);
        playerOnePanel.add(playerOneText);
        playerTwoPanel.add(playerTwoLabel);
        playerTwoPanel.add(playerTwoText);
        instructionPanel.add(instructionLabel);
        instructionPanel.add(dummyLabel);
        instructionPanel.add(quitButton);

        mediaPanel.add(botPanel);
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

    public List<String> getPlayerNames() {

        String p1 = playerOneText.getText(), p2 = playerTwoText.getText();

        if (p1.isEmpty()) {
            p1 = "X";
        }

        if (p2.isEmpty()) {
            p2 = "O";
        }

        return Arrays.asList(p1, p2);
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
        Map<String, Integer> hashMap = database.players;
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
}

class DataBase {
    public Map<String, Integer> players;
    private File file;

    public DataBase() {
        players = fetchContent();
    }

    public Map<String, Integer> fetchContent() {
        try {
            file = new File("data.txt");
            file.createNewFile();

            Scanner scanFile = new Scanner(new FileReader(file));
            players = new HashMap<String, Integer>();

            while (scanFile.hasNext()) {
                String player = scanFile.next();
                int wins = Integer.parseInt(scanFile.next());
                players.put(player, wins);
            }

            scanFile.close();

            return players;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void updateTable(String winner) {
        if (players.containsKey(winner)) {
            players.put(winner, players.get(winner) + 1);
        } else {
            players.put(winner, 1);
        }
    }

    public void saveRecords() {
        Iterator<Map.Entry<String, Integer>> iterator = players.entrySet().iterator();

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

    public List<Integer> findBestMove(char board[][]) {

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

        return Arrays.asList(row, col);
    }
}