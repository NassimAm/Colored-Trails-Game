import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class GameMaster extends Agent {
    private GameGUI gui;
    private int round = 0;
    private int nbPlayersFinishedStep = 0;
    private int gameNbOffers = 0;
    private int stepInRound = 0;
    private boolean initGameOver = false;
    private boolean gameOver = false;
    private String gameOverDescription = "";

    public static final int WAIT_CHOOSE_ACTION_STEP = 0;
    public static final int SYNCHRONIZE_CHOOSE_ACTION_STEP = 1;
    public static final int WAIT_RECEIVE_OFFERS_STEP = 2;
    public static final int SYNCHRONIZE_RECEIVE_OFFERS_STEP = 3;
    public static final int WAIT_OFFERS_REPLY_STEP = 4;
    public static final int SYNCHRONIZE_OFFERS_REPLY_STEP = 5;
    public static final int SYNCHRONIZE_GAME_OVER_STEP = 6;

    @Override
    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        this.gui = (GameGUI) args[0];

        addBehaviour(new Behaviour() {
            @Override
            public void action() {
                switch (stepInRound) {
                    case WAIT_CHOOSE_ACTION_STEP:
                    {
                        MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchOntology(Player.SYNCHRONIZE_CHOOSE_ACTION_ONTOLOGY),
                                                                 MessageTemplate.MatchOntology(Player.SYNCHRONIZE_GAME_OVER_ONTOLOGY));
                        ACLMessage msg = receive(mt);
                        if(msg != null)
                        {
                            // If one of the message is a game over message, we stop the game in the next step
                            if(msg.getOntology() == Player.SYNCHRONIZE_GAME_OVER_ONTOLOGY)
                            {
                                // Get how the game ended
                                String description = msg.getContent();
                                if(!description.isEmpty())
                                    gameOverDescription += description + "\n";

                                initGameOver = true;
                            }
                            else
                            {
                                try {
                                    PlayerOffer offer = (PlayerOffer) msg.getContentObject();
                                    if(offer != null)
                                    {
                                        System.out.println("- Player" + (offer.getProposerId()+1) + " offers " + offer.getPinOffered() + " in exchange of " + offer.getPinRequested() + ". Trials: " + offer.getNbRoundsBlocked());
                                        gameNbOffers ++;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            nbPlayersFinishedStep++;
    
                            if(nbPlayersFinishedStep == gui.getGame().getNbPlayers())
                            {
                                nbPlayersFinishedStep = 0;
                                stepInRound = SYNCHRONIZE_CHOOSE_ACTION_STEP;
                            }
                        }
                        else
                        {
                            block();
                        }
                        break;
                    }
                    case SYNCHRONIZE_CHOOSE_ACTION_STEP:
                    {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        for(int i=0; i<gui.getGame().getNbPlayers(); i++)
                        {
                            msg.addReceiver(new AID("Player"+(i+1), AID.ISLOCALNAME));
                        }
                        // Inform the players that the game is over
                        if(initGameOver)
                        {
                            msg.setOntology(Player.SYNCHRONIZE_GAME_OVER_ONTOLOGY);
                        }
                        else // Otherwise continue playing
                        {
                            msg.setOntology(Player.SYNCHRONIZE_CHOOSE_ACTION_ONTOLOGY);
                            try {
                                msg.setContentObject(gameNbOffers);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // Send the message to all players
                        send(msg);
                        // Update the gui
                        gui.updateBoard();
                        gui.updatePinsPanel();
                        // Reset the number of offers available in the game round
                        gameNbOffers = 0;

                        // Check if the game is over
                        if(initGameOver)
                        {
                            stepInRound = SYNCHRONIZE_GAME_OVER_STEP;
                        }
                        else
                        {
                            stepInRound = WAIT_RECEIVE_OFFERS_STEP;
                        }
                        break;
                    }
                    case WAIT_RECEIVE_OFFERS_STEP:
                    {
                        MessageTemplate mt = MessageTemplate.MatchOntology(Player.SYNCHRONIZE_RECEIVE_OFFERS_ONTOLOGY);
                        ACLMessage msg = receive(mt);
                        if(msg != null)
                        {
                            try {
                                PlayerOffer offer = (PlayerOffer) msg.getContentObject();
                                if(offer != null)
                                {
                                    System.out.println("- Player" + (offer.getProposerId()+1) + " offers " + offer.getPinOffered() + " in exchange of " + offer.getPinRequested() + " to Player" + (offer.getResponderId()+1));
                                    System.out.println("\tPlayer" + (offer.getProposerId()+1) + " has " + (offer.hasProposerDeceived() ? "deceived" : "not deceived") + " and Player" + (offer.getResponderId()+1) + " has " + (offer.hasResponderDeceived() ? "deceived" : "not deceived"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            nbPlayersFinishedStep++;
                            if(nbPlayersFinishedStep == gui.getGame().getNbPlayers())
                            {
                                nbPlayersFinishedStep = 0;
                                stepInRound = SYNCHRONIZE_RECEIVE_OFFERS_STEP;
                            }
                        }
                        else
                        {
                            block();
                        }
                        break;
                    }
                    case SYNCHRONIZE_RECEIVE_OFFERS_STEP:
                    {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        for(int i=0; i<gui.getGame().getNbPlayers(); i++)
                        {
                            msg.addReceiver(new AID("Player"+(i+1), AID.ISLOCALNAME));
                        }
                        msg.setOntology(Player.SYNCHRONIZE_RECEIVE_OFFERS_ONTOLOGY);
                        send(msg);

                        stepInRound = WAIT_OFFERS_REPLY_STEP;
                        break;
                    }
                    case WAIT_OFFERS_REPLY_STEP:
                    {
                        MessageTemplate mt = MessageTemplate.MatchOntology(Player.SYNCHRONIZE_OFFERS_REPLY_ONTOLOGY);
                        ACLMessage msg = receive(mt);
                        if(msg != null)
                        {
                            nbPlayersFinishedStep++;
                            if(nbPlayersFinishedStep == gui.getGame().getNbPlayers())
                            {
                                nbPlayersFinishedStep = 0;
                                stepInRound = SYNCHRONIZE_OFFERS_REPLY_STEP;
                            }
                        }
                        else
                        {
                            block();
                        }
                        break;
                    }
                    case SYNCHRONIZE_OFFERS_REPLY_STEP:
                    {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        for(int i=0; i<gui.getGame().getNbPlayers(); i++)
                        {
                            msg.addReceiver(new AID("Player"+(i+1), AID.ISLOCALNAME));
                        }
                        msg.setOntology(Player.SYNCHRONIZE_OFFERS_REPLY_ONTOLOGY);
                        send(msg);

                        stepInRound = WAIT_CHOOSE_ACTION_STEP;
                        round++;
                        break;
                    }
                    case SYNCHRONIZE_GAME_OVER_STEP:
                    {
                        MessageTemplate mt = MessageTemplate.MatchOntology(Player.SYNCHRONIZE_REWARD_ONTOLOGY);
                        ACLMessage msg = receive(mt);
                        if(msg != null)
                        {
                            try {
                                int reward = (int) msg.getContentObject();
                                System.out.println(msg.getSender().getLocalName() + " has " + reward + " points");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            nbPlayersFinishedStep++;
                            if(nbPlayersFinishedStep == gui.getGame().getNbPlayers())
                            {
                                nbPlayersFinishedStep = 0;
                                gameOver = true;
                                stepInRound = SYNCHRONIZE_GAME_OVER_STEP;
                            }
                        }
                        else
                        {
                            block();
                        }
                        break;
                    }
                }
            }

            @Override
            public boolean done() {
                if(gameOver)
                {
                    gui.getGame().setOver();
                    System.out.println("Game ended in " + round + " rounds");
                    if(!gameOverDescription.isEmpty())
                        System.out.println(gameOverDescription.trim());
                }
                return gameOver;
            }
        });
    }
}
