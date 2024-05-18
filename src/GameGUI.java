import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GameGUI extends JFrame {
    private Game game;
    private float fps = 60.0f;
    private JPanel boardPanel;
    private ArrayList<GamePosNotification> notifications = new ArrayList<GamePosNotification>();

    public GameGUI(Game game, float fps) {
        this.game = game;
        this.fps = fps;
        setTitle("Colored Trails Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        this.boardPanel = new JPanel(new GridLayout(game.getNbRows(), game.getNbColumns(), 5, 5));
        this.boardPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        initBoard();

        mainPanel.add(boardPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initBoard() {
        boardPanel.removeAll();
        for (int i = 0; i < game.getNbRows(); i++) {
            for (int j = 0; j < game.getNbColumns(); j++) {
                JPanel cellPanel = new JPanel();
                cellPanel.setPreferredSize(new Dimension(50, 50)); // Adjust cell size as needed
                Integer goalIndex = getGoalIndex(i, j);
                if (goalIndex != -1) {
                    JLabel goalLabel = new JLabel("G" + (goalIndex+1));
                    goalLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    goalLabel.setForeground(Color.WHITE);
                    cellPanel.setBackground(Color.DARK_GRAY); // Set goal cell color
                    cellPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Add border around goal cells
                    cellPanel.setLayout(new BorderLayout());
                    cellPanel.add(goalLabel, BorderLayout.CENTER);
                } else {
                    int color = game.getColor(i, j);
                    cellPanel.setBackground(getColorFromIndex(color));
                    cellPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Add border around cells
                }
                boardPanel.add(cellPanel);
            }
        }
        revalidate();
        repaint();
    }

    public void notifyPos(int playerId, int row, int col) {
        notifications.add(new GamePosNotification(playerId, row, col));
    }

    public void updateBoard() {
        boardPanel.removeAll();
        for (int i = 0; i < game.getNbRows(); i++) {
            for (int j = 0; j < game.getNbColumns(); j++) {
                JPanel cellPanel = new JPanel();
                cellPanel.setPreferredSize(new Dimension(50, 50)); // Adjust cell size as needed

                // Search in notifications
                int playerId = -1;
                int notifId = -1;
                for(int k = 0; k<notifications.size(); k++)
                {
                    if(i == notifications.get(k).getRow() && j == notifications.get(k).getCol())
                    {
                        playerId = notifications.get(k).getPlayerId();
                        notifId = k;
                        break;
                    }
                }

                if(playerId != -1)
                {
                    JLabel playerLabel = new JLabel("P" + (playerId+1));
                    playerLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    playerLabel.setForeground(Color.BLACK);
                    cellPanel.setBackground(Color.WHITE); // Set player cell color
                    cellPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Add border around player cells
                    cellPanel.setLayout(new BorderLayout());
                    cellPanel.add(playerLabel, BorderLayout.CENTER);
                    notifications.remove(notifId);
                }
                else
                {
                    Integer goalIndex = getGoalIndex(i, j);
                    if (goalIndex != -1) {
                        JLabel goalLabel = new JLabel("G" + (goalIndex+1));
                        goalLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        goalLabel.setForeground(Color.WHITE);
                        cellPanel.setBackground(Color.DARK_GRAY); // Set goal cell color
                        cellPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Add border around goal cells
                        cellPanel.setLayout(new BorderLayout());
                        cellPanel.add(goalLabel, BorderLayout.CENTER);
                    } else {
                        int color = game.getColor(i, j);
                        cellPanel.setBackground(getColorFromIndex(color));
                        cellPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Add border around cells
                    }   
                }
                boardPanel.add(cellPanel);
            }
        }
        revalidate();
        repaint();

        try {
            Thread.sleep((long) (1000.0 / fps));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Integer getGoalIndex(int row, int col) {
        for (int i = 0; i < game.getNbPlayers(); i++) {
            int[] goal = game.getGoal(i);
            if (goal[0] == row && goal[1] == col) {
                return i;
            }
        }
        return -1;
    }

    private Color getColorFromIndex(int colorIndex) {
        // Define color mapping logic
        switch (colorIndex) {
            case 0:
                return Color.BLUE;
            case 1:
                return Color.GREEN;
            case 2:
                return Color.ORANGE;
            case 3:
                return Color.YELLOW;
            case 4:
                return Color.CYAN;
            case 5:
                return Color.MAGENTA;
            case 6:
                return Color.PINK;
            case 7:
                return Color.RED;
            default:
                return Color.GRAY;
        }
    }

    public Game getGame() {
        return game;
    }
}