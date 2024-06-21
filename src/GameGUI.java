import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GameGUI extends JFrame {
    private Game game;
    private float fps = 60.0f;
    private JPanel boardPanel;
    private JPanel pinsPanel;
    private JPanel[] playersPinsPanels;
    private ArrayList<GamePosNotification> posNotifications = new ArrayList<GamePosNotification>();
    private ArrayList<GamePinsNotification> pinsNotifications = new ArrayList<GamePinsNotification>();

    public GameGUI(Game game, float fps) {
        this.game = game;
        this.fps = fps;
        setTitle("Colored Trails Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Pins panel
        this.pinsPanel = new JPanel(new GridLayout(game.getNbPlayers(), 1, 5, 5));
        this.playersPinsPanels = new JPanel[game.getNbPlayers()];
        mainPanel.add(pinsPanel);

        // Board panel
        this.boardPanel = new JPanel(new GridLayout(game.getNbRows(), game.getNbColumns(), 5, 5));
        this.boardPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        initBoard();
        mainPanel.add(boardPanel);

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
                    int color = game.getColor(i, j);
                    goalLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    goalLabel.setForeground(Color.BLACK);
                    cellPanel.setBackground(getColorFromIndex(color)); // Set goal cell color
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
        posNotifications.add(new GamePosNotification(playerId, row, col));
    }

    public void notifyPins(int playerId, ArrayList<Integer> pins) {
        pinsNotifications.add(new GamePinsNotification(playerId, pins));
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
                for(int k = 0; k<posNotifications.size(); k++)
                {
                    if(i == posNotifications.get(k).getRow() && j == posNotifications.get(k).getCol())
                    {
                        playerId = posNotifications.get(k).getPlayerId();
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
                    posNotifications.remove(notifId);
                }
                else
                {
                    Integer goalIndex = getGoalIndex(i, j);
                    if (goalIndex != -1) {
                        int color = game.getColor(i, j);
                        JLabel goalLabel = new JLabel("G" + (goalIndex+1));
                        goalLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        goalLabel.setForeground(Color.BLACK);
                        cellPanel.setBackground(getColorFromIndex(color)); // Set goal cell color
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

    public void updatePinsPanel() {
        // Clear pins panel
        pinsPanel.removeAll();

        for(int i=0; i<pinsNotifications.size(); i++)
        {
            int playerId = pinsNotifications.get(i).getPlayerId();
            ArrayList<Integer> pins = pinsNotifications.get(i).getPins();

            JPanel playerPinsPanel = new JPanel(new GridLayout(2, 1, 0, 0));
            JLabel pinsLabel = new JLabel("Player" + (playerId + 1) + " Pins");
            pinsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            playerPinsPanel.add(pinsLabel, BorderLayout.NORTH);

            JPanel pinsContentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            pinsContentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Display pins as colored circles
            for (int pin : pins) {
                JPanel pinPanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(getColorFromIndex(pin));
                        int diameter = Math.min(getWidth(), getHeight());
                        g.fillOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2, diameter, diameter);
                    }
                };
                pinPanel.setPreferredSize(new Dimension(20, 20)); // Adjust pin size as needed
                pinsContentPanel.add(pinPanel);
            }

            playerPinsPanel.add(pinsContentPanel, BorderLayout.CENTER);

            // Update the main pins panel
            playersPinsPanels[playerId] = playerPinsPanel;
        }

        for(int i=0; i<playersPinsPanels.length; i++)
        {
            if(playersPinsPanels[i] != null)
            {
                pinsPanel.add(playersPinsPanels[i]);
            }
        
        }

        revalidate();
        repaint();
    }

    public Game getGame() {
        return game;
    }
}