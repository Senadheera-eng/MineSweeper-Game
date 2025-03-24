import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class MineSweeper extends JFrame {
    //Game board components
    private static final int CELL_SIZE = 30;
    private JButton[][] buttons;
    private int[][] mines;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private int rows;
    private int cols;
    private int numMines;
    private int timeElapsed;
    private Timer timer;
    private JLabel timerLabel;
    private JLabel mineCountLabel;
    private boolean firstClick;
    private JPanel gamePanel;
    private boolean gameOver;
    private int remainingMines;
    private ImageIcon mineIcon;
    private ImageIcon flagIcon;
    private ImageIcon wrongMineIcon;
    
    
// Constructor: Initialize the game with specified size and number of mines
    public MineSweeper(int size, int mines) {
        this.rows = size;
        this.cols = size;
        this.numMines = mines;
        this.remainingMines = mines;
        
        setTitle("MineSweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        createIcons();        // Create game icons
        initializeGame();     // Initialize game state
        createGUI();         // Set up the game interface
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    // Create icons for mines and flags
    private void createIcons() {
        int iconSize = CELL_SIZE - 2;
        mineIcon = createMineIcon(iconSize, Color.BLACK);
        wrongMineIcon = createMineIcon(iconSize, Color.RED);
        flagIcon = createFlagIcon(iconSize);
    }

    // Create mine icon with specified size and color
    private ImageIcon createMineIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw mine circle
        g2d.setColor(color);
        g2d.fillOval(size/4, size/4, size/2, size/2);
        
        // Draw mine spikes
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int x1 = size/2 + (int)((size/4) * Math.cos(angle));
            int y1 = size/2 + (int)((size/4) * Math.sin(angle));
            int x2 = size/2 + (int)((size/2) * Math.cos(angle));
            int y2 = size/2 + (int)((size/2) * Math.sin(angle));
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // Draw highlight
        g2d.setColor(Color.WHITE);
        g2d.fillOval(size/3, size/3, size/6, size/6);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    // Create flag icon
    private ImageIcon createFlagIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw flag pole
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(size/3, size/4, size/3, size*3/4);
        
        // Draw flag triangle
        int[] xPoints = {size/3, size*2/3, size/3};
        int[] yPoints = {size/4, size*3/8, size/2};
        g2d.setColor(Color.RED);
        g2d.fillPolygon(xPoints, yPoints, 3);
        
         // Draw base
        g2d.setColor(Color.BLACK);
        g2d.fillRect(size/4, size*2/3, size/2, size/8);
        
        g2d.dispose();
        return new ImageIcon(image);
    }

    // Initialize game state variables
    private void initializeGame() {
        buttons = new JButton[rows][cols];
        mines = new int[rows][cols];
        revealed = new boolean[rows][cols];
        flagged = new boolean[rows][cols];
        firstClick = true;
        gameOver = false;
        timeElapsed = 0;
        
        // Create timer for tracking game duration
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeElapsed++;
                updateTimerLabel();
            }
        });
    }
    
    // Create the game's graphical interface
    private void createGUI() {
        // Create top panel with timer, new game button, and mine count
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new GridLayout(1, 3));
        timerLabel = new JLabel("Time: 0", SwingConstants.CENTER);
        mineCountLabel = new JLabel("Mines: " + remainingMines, SwingConstants.CENTER);
        
        // Style the labels
        timerLabel.setFont(new Font("Digital-7 Mono", Font.BOLD, 20));
        mineCountLabel.setFont(new Font("Digital-7 Mono", Font.BOLD, 20));
        timerLabel.setForeground(Color.RED);
        mineCountLabel.setForeground(Color.RED);
        
        // Create and configure New Game button
        JButton newGameButton = new JButton("New Game");
        newGameButton.setFocusPainted(false);
        newGameButton.setBackground(new Color(60, 63, 65));
        newGameButton.setForeground(Color.WHITE);
        newGameButton.addActionListener(e -> startNewGame());
        
        headerPanel.add(timerLabel);
        headerPanel.add(newGameButton);
        headerPanel.add(mineCountLabel);
        
        topPanel.add(headerPanel, BorderLayout.CENTER);
        
        // Create game grid
        gamePanel = new JPanel(new GridLayout(rows, cols));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create and configure all cell buttons
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                button.setFont(new Font("Arial", Font.BOLD, 16));
                button.setFocusPainted(false);
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setBackground(new Color(189, 189, 189));
                
                final int row = i;
                final int col = j;
                
                // Add mouse listeners for left and right clicks
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!gameOver) {
                            if (e.getButton() == MouseEvent.BUTTON1) {
                                handleLeftClick(row, col);
                            } else if (e.getButton() == MouseEvent.BUTTON3) {
                                handleRightClick(row, col);
                            }
                        }
                    }
                });
                
                buttons[i][j] = button;
                gamePanel.add(button);
            }
        }
        
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
    }

    // Handle starting a new game
    private void startNewGame() {
        Object[] options = {"10x10 (10 mines)", "15x15 (20 mines)"};
        int choice = JOptionPane.showOptionDialog(this,
            "Select game size:",
            "New Game",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        timer.stop();
        dispose();
        
        MineSweeper game;
        if (choice == 0) {
            game = new MineSweeper(10, 10);
        } else {
            game = new MineSweeper(15, 20);
        }
        game.setVisible(true);
    }

    // Place mines randomly on the board (avoiding first click location)
    private void placeMines(int firstRow, int firstCol) {
        Random random = new Random();
        int minesPlaced = 0;
        
        while (minesPlaced < numMines) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            
            if (mines[row][col] != -1 && !(row == firstRow && col == firstCol)) {
                mines[row][col] = -1;
                minesPlaced++;
            }
        }
        
        // Calculate numbers for non-mine cells
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mines[i][j] != -1) {
                    mines[i][j] = countAdjacentMines(i, j);
                }
            }
        }
    }

    // Count mines adjacent to a cell
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    if (mines[newRow][newCol] == -1) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    // Handle left click on a cell
    private void handleLeftClick(int row, int col) {
        if (firstClick) {
            firstClick = false;
            placeMines(row, col);
            timer.start();
        }
        
        // Check for chording (clicking on revealed number)
        if (revealed[row][col] && mines[row][col] > 0) {
            int adjacentFlags = 0;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int newRow = row + i;
                    int newCol = col + j;
                    if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                        if (flagged[newRow][newCol]) {
                            adjacentFlags++;
                        }
                    }
                }
            }
            // If flags match number, reveal adjacent cells
            if (adjacentFlags == mines[row][col]) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int newRow = row + i;
                        int newCol = col + j;
                        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                            if (!flagged[newRow][newCol] && !revealed[newRow][newCol]) {
                                if (mines[newRow][newCol] == -1) {
                                    gameOver();
                                    return;
                                }
                                revealCell(newRow, newCol);
                            }
                        }
                    }
                }
                checkWin();
                return;
            }
        }
        
        // Normal left-click behavior
        if (flagged[row][col] || revealed[row][col]) {
            return;
        }
        
        if (mines[row][col] == -1) {
            gameOver();
            return;
        }
        
        revealCell(row, col);
        checkWin();
    }

    // Handle right click (flagging)
    private void handleRightClick(int row, int col) {
        if (!revealed[row][col]) {
            flagged[row][col] = !flagged[row][col];
            buttons[row][col].setIcon(flagged[row][col] ? flagIcon : null);
            remainingMines += flagged[row][col] ? -1 : 1;
            mineCountLabel.setText("Mines: " + remainingMines);
        }
    }

    // Reveal a cell and its surrounding cells if empty
    private void revealCell(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols || revealed[row][col]) {
            return;
        }
        
        revealed[row][col] = true;
        buttons[row][col].setBackground(Color.WHITE);
        
        if (mines[row][col] > 0) {
            buttons[row][col].setText(String.valueOf(mines[row][col]));
            setNumberColor(buttons[row][col], mines[row][col]);
        } else if (mines[row][col] == 0) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    revealCell(row + i, col + j);
                }
            }
        }
    }

    // Set color for number based on value
    private void setNumberColor(JButton button, int number) {
        Color[] colors = {
            null,
            new Color(25, 118, 210),  // 1 - Blue
            new Color(56, 142, 60),   // 2 - Green
            new Color(211, 47, 47),   // 3 - Red
            new Color(123, 31, 162),  // 4 - Purple
            new Color(255, 143, 0),   // 5 - Orange
            new Color(0, 151, 167),   // 6 - Cyan
            new Color(66, 66, 66),    // 7 - Dark Gray
            new Color(158, 158, 158)  // 8 - Gray
        };
        button.setForeground(colors[number]);
    }

    private void gameOver() {
        gameOver = true;
        timer.stop();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mines[i][j] == -1) {
                    buttons[i][j].setIcon(mineIcon);
                    buttons[i][j].setBackground(Color.RED);
                }
            }
        }
        
        JOptionPane.showMessageDialog(this, "Game Over!");
        askForNewGame();
    }

    // Check if player has won
    private void checkWin() {
        boolean won = true;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mines[i][j] != -1 && !revealed[i][j]) {
                    won = false;
                    break;
                }
            }
        }
        
        if (won) {
            gameOver = true;
            timer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! You won!");
            askForNewGame();
        }
    }

    // Ask player if they want to start a new game
    private void askForNewGame() {
        int choice = JOptionPane.showConfirmDialog(this, 
            "Would you like to start a new game?", 
            "New Game", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            dispose();
        }
    }

    // Update timer display
    private void updateTimerLabel() {
        timerLabel.setText("Time: " + timeElapsed);
    }

     // Main method to start the game
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Object[] options = {"10x10 (10 mines)", "15x15 (20 mines)"};
            int choice = JOptionPane.showOptionDialog(null,
                "Select game size:",
                "MineSweeper",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
            
            MineSweeper game;
            if (choice == 0) {
                game = new MineSweeper(10, 10);
            } else {
                game = new MineSweeper(15, 20);
            }
            game.setVisible(true);
        });
    }
}