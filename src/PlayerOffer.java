import java.io.Serializable;

public class PlayerOffer implements Serializable {
    private int playerId;
    private int pinOffered;
    private int pinRequested;
    private int nbRoundsBlocked;
    private boolean proposerDeceived = false;
    private boolean responderDeceived = false;

    public PlayerOffer(int playerId, int pinOffered, int pinRequested, int nbRoundsBlocked) {
        this.playerId = playerId;
        this.pinOffered = pinOffered;
        this.pinRequested = pinRequested;
        this.nbRoundsBlocked = nbRoundsBlocked;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getPinOffered() {
        return pinOffered;
    }

    public int getPinRequested() {
        return pinRequested;
    }

    public int getNbRoundsBlocked() {
        return nbRoundsBlocked;
    }

    public boolean hasProposerDeceived() {
        return proposerDeceived;
    }

    public void setProposerDeceived(boolean proposerDeceived) {
        this.proposerDeceived = proposerDeceived;
    }

    public boolean hasResponderDeceived() {
        return responderDeceived;
    }

    public void setResponderDeceived(boolean responderDeceived) {
        this.responderDeceived = responderDeceived;
    }
}
