import java.util.ArrayList;
import java.util.Collections;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Player extends Agent {
    private int id;
    private GameGUI gui;
    private Game game;
    private int row;
    private int col;
    private int reward = 0;
    private int step = 0;
    private int nbRoundsBlocked = 0;
    private boolean reachedGoal = false;
    private ArrayList<Integer> pins = new ArrayList<Integer>();
    private ArrayList<PlayerOffer> offers = new ArrayList<PlayerOffer>();
    private int nbOffersSent = 0;
    private int gameNbOffers = 0;
    private int nbRespondersRepliesReceived = 0;
    private boolean gameOver = false;
    private String gameOverDescription = null;

    public static final int MAX_ROUNDS_BLOCKED = 3;

    public static final int CHOOSE_ACTION_STEP = 0;
    public static final int SYNCHRONIZE_CHOOSE_ACTION_STEP = 1;
    public static final int RECEIVE_OFFERS_STEP = 2;
    public static final int SYNCHRONIZE_RECEIVE_OFFERS_STEP = 3;
    public static final int OFFERS_REPLY_STEP = 4;
    public static final int SYNCHRONIZE_OFFERS_REPLY_STEP = 5;
    public static final int SYNCHRONIZE_GAME_OVER_STEP = 6;

    public static final String SYNCHRONIZE_CHOOSE_ACTION_ONTOLOGY = "Synchronize Choose Action";
    public static final String SYNCHRONIZE_RECEIVE_OFFERS_ONTOLOGY = "Synchronize Receive Offers";
    public static final String SYNCHRONIZE_OFFERS_REPLY_ONTOLOGY = "Synchronize Offers Reply";
    public static final String SYNCHRONIZE_GAME_OVER_ONTOLOGY = "Synchronize Game Over";
    public static final String SYNCHRONIZE_REWARD_ONTOLOGY = "Synchronize Reward";

    public static final String OFFER_PROPOSAL_ONTOLOGY = "Offer Proposal";
    public static final String RESPONDER_OFFER_REPLY_ONTOLOGY = "Responder Offer Reply";

    @Override
    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        this.gui = (GameGUI) args[0];
        this.game = gui.getGame();
        this.id = (int) args[1];
        this.row = (int) (Math.random() * this.game.getNbRows());
        this.col = (int) (Math.random() * this.game.getNbColumns());
        for (int i = 0; i < (int) ((this.game.getNbRows() + this.game.getNbColumns()) / 2); i++) {
            this.pins.add((int) (Math.random() * this.game.getNbColors()));
        }

        addBehaviour(new Behaviour() {
            @Override
            public void action() {
                switch (step) {
                    // ===================== Taking action Step =========================
                    case CHOOSE_ACTION_STEP:
                    {
                        int[] goal = game.getGoal(id);
                        ArrayList<Integer> colors = new ArrayList<Integer>();
                        ArrayList<Integer> distances = new ArrayList<Integer>();
                        
                        // Notify the gui with the new player position
                        gui.notifyPos(id, row, col);

                        // Check if player is on goal
                        if(row == goal[0] && col == goal[1])
                        {
                            // The player reached the goal
                            reachedGoal = true;
                            gameOverDescription = "Player" + (id+1) + " reached the goal!";
                            // Send a message to the game message to inform it that the game is over
                            ACLMessage msgGM = new ACLMessage(ACLMessage.INFORM);
                            msgGM.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                            msgGM.setOntology(SYNCHRONIZE_GAME_OVER_ONTOLOGY);
                            msgGM.setContent(gameOverDescription);
                            send(msgGM);

                            step = SYNCHRONIZE_CHOOSE_ACTION_STEP;
                            return;
                        }

                        // Check adjencent colors
                        if(col - 1 >= 0)
                        {
                            colors.add(game.getColor(row, col - 1));
                            distances.add(Math.abs(goal[0] - row) + Math.abs(goal[1] - (col - 1)));
                        }
                        else
                        {
                            colors.add(-1);
                            distances.add(Integer.MAX_VALUE);
                        }

                        if(row - 1 >= 0)
                        {
                            colors.add(game.getColor(row - 1, col));
                            distances.add(Math.abs(goal[0] - (row - 1)) + Math.abs(goal[1] - col));
                        }
                        else
                        {
                            colors.add(-1);
                            distances.add(Integer.MAX_VALUE);
                        }

                        if(col + 1 < game.getNbColumns())
                        {
                            colors.add(game.getColor(row, col + 1));
                            distances.add(Math.abs(goal[0] - row) + Math.abs(goal[1] - (col + 1)));
                        }
                        else
                        {
                            colors.add(-1);
                            distances.add(Integer.MAX_VALUE);
                        }

                        if(row + 1 < game.getNbRows())
                        {
                            colors.add(game.getColor(row + 1, col));
                            distances.add(Math.abs(goal[0] - (row + 1)) + Math.abs(goal[1] - col));
                        }
                        else
                        {
                            colors.add(-1);
                            distances.add(Integer.MAX_VALUE);
                        }

                        // Select the next action to take
                        int minDistance = Collections.min(distances);
                        int minIndex = -1;
                        int bestIndex = 0;
                        int bestDistance = Integer.MAX_VALUE;
                        for(int i=0; i<colors.size(); i++)
                        {
                            if(colors.get(i) != -1 && distances.get(i) == minDistance && pins.contains(colors.get(i)))
                            {
                                minDistance = distances.get(i);
                                minIndex = i;
                            }
                            if(colors.get(i) != -1 && distances.get(i) < bestDistance)
                            {
                                bestDistance = distances.get(i);
                                bestIndex = i;
                            }
                        }

                        // Move the player or generate an offer
                        PlayerOffer offer = null;
                        if(minIndex != -1)
                        {
                            if(minIndex == 0)
                            {
                                col--;
                            }
                            else if(minIndex == 1)
                            {
                                row--;
                            }
                            else if(minIndex == 2)
                            {
                                col++;
                            }
                            else if(minIndex == 3)
                            {
                                row++;
                            }
                            pins.remove((Object)(colors.get(minIndex)));
                            nbRoundsBlocked = 0;
                        }
                        else
                        {
                            nbRoundsBlocked++;
                            
                            // Send offer as a message to other players
                            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                            for(int i=0; i<game.getNbPlayers(); i++)
                            {
                                if(i != id)
                                {
                                    msg.addReceiver(new AID("Player"+(i+1), AID.ISLOCALNAME));
                                }
                            }

                            int maxNbPins = -1;
                            int pinRequested = colors.get(bestIndex);
                            int pinOffered = pinRequested;
                            for(int i=0; i<game.getNbColors(); i++)
                            {
                                if(i != pinRequested)
                                {
                                    int nbPins = 0;
                                    for(int j=0; j<pins.size(); j++)
                                    {
                                        if(pins.get(j) == i)
                                        {
                                            nbPins++;
                                        }
                                    }
                                    if(nbPins > maxNbPins)
                                    {
                                        maxNbPins = nbPins;
                                        pinOffered = i;
                                    }
                                }
                            }
                            
                            try {
                                offer = new PlayerOffer(id, pinOffered, pinRequested, nbRoundsBlocked);
                                offer.setProposerDeceived(maxNbPins == 0);
                                msg.setContentObject(offer);
                                msg.setOntology(OFFER_PROPOSAL_ONTOLOGY);
                                send(msg);
                                nbOffersSent++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        
                        // If the player is blocked for MAX_ROUNDS_BLOCKED consecutive rounds, the game is over
                        if(nbRoundsBlocked == MAX_ROUNDS_BLOCKED)
                        {
                            // The player is blocked
                            gameOverDescription = "Player" + (id+1) + " is blocked!";
                            // Send a message to the game message to inform it that the game is over
                            ACLMessage msgGM = new ACLMessage(ACLMessage.INFORM);
                            msgGM.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                            msgGM.setOntology(SYNCHRONIZE_GAME_OVER_ONTOLOGY);
                            msgGM.setContent(gameOverDescription);
                            send(msgGM);

                            step = SYNCHRONIZE_CHOOSE_ACTION_STEP;
                            return;
                        }

                        // Send the number of offers sent by this player to the GameMaster
                        // This is also a notification to the GameMaster that this player has finished this step

                        ACLMessage msgGM = new ACLMessage(ACLMessage.INFORM);
                        msgGM.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                        msgGM.setOntology(SYNCHRONIZE_CHOOSE_ACTION_ONTOLOGY);
                        try {
                            if(offer != null)
                                msgGM.setContentObject(offer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        send(msgGM);

                        step = SYNCHRONIZE_CHOOSE_ACTION_STEP;
                        break;
                    }
                    // ============ Synchronize the choose action step with the GameMaster ========
                    case SYNCHRONIZE_CHOOSE_ACTION_STEP:
                    {
                        MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchOntology(SYNCHRONIZE_CHOOSE_ACTION_ONTOLOGY),
                                                                MessageTemplate.MatchOntology(SYNCHRONIZE_GAME_OVER_ONTOLOGY));
                        ACLMessage msg = receive(mt);
                        if(msg != null)
                        {
                            // The GameMaster informs players that the game is over
                            if(msg.getOntology() == SYNCHRONIZE_GAME_OVER_ONTOLOGY)
                            {
                                step = SYNCHRONIZE_GAME_OVER_STEP;
                            }
                            else // Otherwise continue playing
                            {
                                try {
                                    gameNbOffers = (int) msg.getContentObject();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                step = RECEIVE_OFFERS_STEP;
                            }
                        }
                        else
                        {
                            block();
                        }
                        break;
                    }
                    // ====================== Receiving pins offers =========================
                    case RECEIVE_OFFERS_STEP:
                    {
                        if(nbOffersSent == gameNbOffers)
                        {
                            // Send a message to the game master to notify it that this player finished this step
                            ACLMessage msgGM = new ACLMessage(ACLMessage.INFORM);
                            msgGM.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                            msgGM.setOntology(SYNCHRONIZE_RECEIVE_OFFERS_ONTOLOGY);
                            send(msgGM);
                            // Reset received offers state
                            offers.clear();
                            // Update game state
                            step = SYNCHRONIZE_RECEIVE_OFFERS_STEP;
                            return;
                        }

                        // Receiving offers
                        MessageTemplate mt = MessageTemplate.MatchOntology(OFFER_PROPOSAL_ONTOLOGY);
                        ACLMessage msg = receive(mt);
                        if(msg != null)
                        {
                            try {
                                PlayerOffer offer = (PlayerOffer) msg.getContentObject();
                                offers.add(offer);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if(offers.size() + nbOffersSent >= gameNbOffers)
                            {
                                // Selecting the best offer
                                int[] goal = game.getGoal(id);
                                ArrayList<Integer> colors = new ArrayList<Integer>();
                                ArrayList<Integer> distances = new ArrayList<Integer>();

                                // Check adjencent colors
                                if(col - 1 >= 0)
                                {
                                    colors.add(game.getColor(row, col - 1));
                                    distances.add(Math.abs(goal[0] - row) + Math.abs(goal[1] - (col - 1)));
                                }
                                else
                                {
                                    colors.add(-1);
                                    distances.add(Integer.MAX_VALUE);
                                }

                                if(row - 1 >= 0)
                                {
                                    colors.add(game.getColor(row - 1, col));
                                    distances.add(Math.abs(goal[0] - (row - 1)) + Math.abs(goal[1] - col));
                                }
                                else
                                {
                                    colors.add(-1);
                                    distances.add(Integer.MAX_VALUE);
                                }

                                if(col + 1 < game.getNbColumns())
                                {
                                    colors.add(game.getColor(row, col + 1));
                                    distances.add(Math.abs(goal[0] - row) + Math.abs(goal[1] - (col + 1)));
                                }
                                else
                                {
                                    colors.add(-1);
                                    distances.add(Integer.MAX_VALUE);
                                }

                                if(row + 1 < game.getNbRows())
                                {
                                    colors.add(game.getColor(row + 1, col));
                                    distances.add(Math.abs(goal[0] - (row + 1)) + Math.abs(goal[1] - col));
                                }
                                else
                                {
                                    colors.add(-1);
                                    distances.add(Integer.MAX_VALUE);
                                }
                                
                                // Select the next action to take without offers
                                int minDistance = Integer.MAX_VALUE;
                                int minOfferIndex = -1;
                                boolean minOfferhasRequestedPin = false;
                                for(int j=0; j<colors.size(); j++)
                                {
                                    if(colors.get(j) != -1 && distances.get(j) < minDistance && pins.contains(colors.get(j)))
                                    {
                                        minDistance = distances.get(j);
                                        minOfferIndex = -1;
                                    }
                                }
                                // Select the next action to take based on offers
                                for(int i=0; i<offers.size(); i++)
                                {
                                    ArrayList<Integer> tempPins = new ArrayList<Integer>(pins);
                                    tempPins.add(offers.get(i).getPinOffered());
                                    boolean hasRequestedPin = tempPins.remove((Object)(offers.get(i).getPinRequested()));
                                    if(offers.get(i).getNbRoundsBlocked() < 2 || hasRequestedPin)
                                    {
                                        for(int j=0; j<colors.size(); j++)
                                        {
                                            if(colors.get(j) != -1 && distances.get(j) < minDistance && tempPins.contains(colors.get(j)))
                                            {
                                                minDistance = distances.get(j);
                                                minOfferIndex = i;
                                                minOfferhasRequestedPin = hasRequestedPin;
                                            }
                                        }
                                    }
                                }

                                // Accepting offers
                                if(minOfferIndex != -1)
                                {
                                    ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                                    reply.addReceiver(new AID("Player" + (offers.get(minOfferIndex).getProposerId() + 1), AID.ISLOCALNAME));
                                    try {
                                        // Send reply
                                        offers.get(minOfferIndex).setResponderDeceived(!minOfferhasRequestedPin);
                                        offers.get(minOfferIndex).setResponderId(id);
                                        reply.setOntology(RESPONDER_OFFER_REPLY_ONTOLOGY);
                                        reply.setContentObject(offers.get(minOfferIndex));
                                        send(reply);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    // Exchange pins
                                    if(!offers.get(minOfferIndex).hasProposerDeceived())
                                        pins.add(offers.get(minOfferIndex).getPinOffered());
                                    pins.remove((Object)(offers.get(minOfferIndex).getPinRequested()));
                                }
                                
                                // Refuse other offers
                                if(!offers.isEmpty())
                                {
                                    ACLMessage refuseReply = new ACLMessage(ACLMessage.REFUSE);
                                    for(int i = 0; i<offers.size(); i++)
                                    {
                                        if(i != minOfferIndex)
                                            refuseReply.addReceiver(new AID("Player" + (offers.get(i).getProposerId() + 1), AID.ISLOCALNAME));
                                    }
                                    refuseReply.setOntology(RESPONDER_OFFER_REPLY_ONTOLOGY);
                                    send(refuseReply);
                                }
                                
                                // Send a message to the game master to notify it that this player finished this step
                                ACLMessage msgGM = new ACLMessage(ACLMessage.INFORM);
                                msgGM.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                                msgGM.setOntology(SYNCHRONIZE_RECEIVE_OFFERS_ONTOLOGY);
                                try {
                                    if(minOfferIndex != -1)
                                        msgGM.setContentObject(offers.get(minOfferIndex));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                send(msgGM);
                                // Reset received offers state
                                offers.clear();
                                // Update game state
                                step = SYNCHRONIZE_RECEIVE_OFFERS_STEP;
                            }
                        }
                        else
                        {
                            block();
                        }
                        break;
                    }
                    // ============ Synchronize the receive offers step with the GameMaster ========
                    case SYNCHRONIZE_RECEIVE_OFFERS_STEP:
                    {
                        MessageTemplate mt = MessageTemplate.MatchOntology(SYNCHRONIZE_RECEIVE_OFFERS_ONTOLOGY);
                        ACLMessage msg = receive(mt);
                        if(msg != null)
                        {
                            step = OFFERS_REPLY_STEP;
                        }
                        else
                        {
                            block();
                        }
                        break;
                    }
                    // Process responders replies and exchange pins
                    case OFFERS_REPLY_STEP:
                    {
                        if(nbOffersSent == 0)
                        {
                            // Send a message to the game master to notify it that this player finished this step
                            ACLMessage msgGM = new ACLMessage(ACLMessage.INFORM);
                            msgGM.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                            msgGM.setOntology(SYNCHRONIZE_OFFERS_REPLY_ONTOLOGY);
                            send(msgGM);
                            // Reset proposed offers state
                            nbRespondersRepliesReceived = 0;
                            gameNbOffers = 0;
                            nbOffersSent = 0;
                            offers.clear();
                            // Update game state
                            step = SYNCHRONIZE_OFFERS_REPLY_STEP;
                            return;
                        }  

                        MessageTemplate mt = MessageTemplate.MatchOntology(RESPONDER_OFFER_REPLY_ONTOLOGY);
                        ACLMessage msg = receive(mt);
                        if(msg != null)
                        {
                            if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
                            {
                                try {
                                    // Get message from responders
                                    PlayerOffer offer = (PlayerOffer) msg.getContentObject();
                                    offers.add(offer);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            nbRespondersRepliesReceived++;

                            if(nbRespondersRepliesReceived >= (game.getNbPlayers() - 1) * nbOffersSent)
                            {
                                for(int i=0; i<offers.size(); i++)
                                {
                                    PlayerOffer offer = offers.get(i);
                                    if(!offer.hasResponderDeceived())
                                        pins.add(offer.getPinRequested());
                                    pins.remove((Object)(offer.getPinOffered()));
                                }
                                
                                // Send a message to the game master to notify it that this player finished this step
                                ACLMessage msgGM = new ACLMessage(ACLMessage.INFORM);
                                msgGM.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                                msgGM.setOntology(SYNCHRONIZE_OFFERS_REPLY_ONTOLOGY);
                                send(msgGM);
                                // Reset proposed offers state
                                nbRespondersRepliesReceived = 0;
                                gameNbOffers = 0;
                                nbOffersSent = 0;
                                offers.clear();
                                // Update game state
                                step = SYNCHRONIZE_OFFERS_REPLY_STEP;
                            }
                        }
                        else
                        {
                            block();
                        }
                        break;
                    }
                    // ============ Synchronize the offers reply step with the GameMaster ========
                    case SYNCHRONIZE_OFFERS_REPLY_STEP:
                    {
                        MessageTemplate mt = MessageTemplate.MatchOntology(SYNCHRONIZE_OFFERS_REPLY_ONTOLOGY);
                        ACLMessage msg = receive(mt);
                        if(msg != null)
                        {
                            step = CHOOSE_ACTION_STEP;
                        }
                        else
                        {
                            block();
                        }
                        break;
                    }
                    case SYNCHRONIZE_GAME_OVER_STEP:
                    {
                        int[] goal = game.getGoal(id);
                        if(reachedGoal)
                            reward += 100;
                        else
                            reward -= 10 * (Math.abs(row - goal[0]) + Math.abs(col - goal[1]));

                        reward += 5 * pins.size();
                        
                        // Send reward to the game master
                        ACLMessage msgGM = new ACLMessage(ACLMessage.INFORM);
                        msgGM.addReceiver(new AID("GameMaster", AID.ISLOCALNAME));
                        msgGM.setOntology(SYNCHRONIZE_REWARD_ONTOLOGY);
                        try {
                            msgGM.setContentObject(reward);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        send(msgGM);

                        gameOver = true;
                        break;
                    }
                }
            }
            
            @Override
            public boolean done() {
                return gameOver;
            }
        });
    }
}
