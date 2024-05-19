import java.util.ArrayList;

public class GamePinsNotification {
    private int playerId;
    private ArrayList<Integer> pins;

    public GamePinsNotification(int playerId, ArrayList<Integer> pins) {
        this.playerId = playerId;
        this.pins = pins;
    }

    public int getPlayerId() {
        return playerId;
    }

    public ArrayList<Integer> getPins() {
        return pins;
    }
}
