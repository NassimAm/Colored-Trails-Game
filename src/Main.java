import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.GUI, "false");
        ContainerController cc = rt.createMainContainer(p);
        
        // Create players
        int numPlayers = 2;
        float fps = 1f;

        // Create board
        Game game = new Game(5, 7, numPlayers + 1, numPlayers);
        GameGUI gui = new GameGUI(game, fps);

        // Create a game master agent
        try {
            AgentController acGM = cc.createNewAgent("GameMaster", "GameMaster", new Object[]{gui});
            acGM.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create agents (players)
        try {
            for(int i=0; i<numPlayers; i++)
            {
                AgentController ac = cc.createNewAgent("Player"+(i+1), "Player", new Object[]{gui, i});
                ac.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
