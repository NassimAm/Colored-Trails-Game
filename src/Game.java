public class Game {
    private int[][] board;
    private int nbRows;
    private int nbColumns;
    private int nbColors;
    private int nbPlayers;
    private int[][] goals;
    private boolean over = false;

    public Game(int nbRows, int nbColumns, int nbColors, int nbPlayers) {
        this.nbRows = nbRows;
        this.nbColumns = nbColumns;
        this.nbColors = nbColors;
        this.nbPlayers = nbPlayers;
        this.goals = new int[nbPlayers][2];
        this.board = new int[nbRows][nbColumns];

        for (int i = 0; i < nbRows; i++) {
            for (int j = 0; j < nbColumns; j++) {
                this.board[i][j] = (int) (Math.random() * nbColors);
            }
        }

        for (int i = 0; i < nbPlayers; i++) {
            this.goals[i][0] = (int) (Math.random() * nbRows);
            this.goals[i][1] = (int) (Math.random() * nbColumns);
        }
    }

    public int getNbRows() {
        return nbRows;
    }

    public int getNbColumns() {
        return nbColumns;
    }

    public int getNbColors() {
        return nbColors;
    }

    public int getNbPlayers() {
        return nbPlayers;
    }

    public int getColor(int row, int column) {
        return board[row][column];
    }

    public int[] getGoal(int playerId) {
        return goals[playerId];
    }

    public void setOver() {
        over = true;
    }

    public boolean isOver() {
        return over;
    }

    public void print() {
        for (int i = 0; i < nbRows; i++) {
            for (int j = 0; j < nbColumns; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
}
