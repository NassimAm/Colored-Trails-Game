public class GamePosNotification {
    private int playerId;
    private int row;
    private int col;

    public GamePosNotification(int playerId, int row, int col) {
        this.playerId = playerId;
        this.row = row;
        this.col = col;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
