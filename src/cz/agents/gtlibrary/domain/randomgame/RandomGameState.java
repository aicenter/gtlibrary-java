package cz.agents.gtlibrary.domain.randomgame;


import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.CombinationGenerator;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

public class RandomGameState extends GameStateImpl {

	private static final long serialVersionUID = 6086530572992658181L;
	
	private int ID;
    private double center;
    private Player playerToMove;
    protected Map<Player, LinkedList<Integer>> observations = new FixedSizeMap<Player, LinkedList<Integer>>(2);

    private int hash = 0;
    protected Pair<Integer, Sequence> ISKey = null;
    private boolean changed = true;

    public RandomGameState() {
        super(RandomGameInfo.ALL_PLAYERS);
        ID = RandomGameInfo.rnd.nextInt();
        playerToMove = RandomGameInfo.FIRST_PLAYER;
        observations.put(players[0], new LinkedList<Integer>());
        observations.put(players[1], new LinkedList<Integer>());
        center = 0;
    }

    public RandomGameState(RandomGameState gameState) {
        super(gameState);
        this.ID = gameState.ID;
        this.playerToMove = gameState.playerToMove;
        observations.put(players[0], new LinkedList<Integer>(gameState.observations.get(players[0])));
        observations.put(players[1], new LinkedList<Integer>(gameState.observations.get(players[1])));
        center = gameState.center;
    }

    protected void evaluateAction(RandomGameAction action) {
        int newID = (ID + action.getOrder())*31 + 17;
        center += new HighQualityRandom(newID).nextInt(RandomGameInfo.MAX_CENTER_MODIFICATION*2+1)-RandomGameInfo.MAX_CENTER_MODIFICATION;
//        if (new HighQualityRandom(newID).nextBoolean()) {
//            center++;
//        } else {
//            center--;
//        }
        generateObservations(newID, action);
        
        this.ID = newID;
        this.ISKey = null;
        this.changed = true;
    }
    
    protected void generateObservations(int newID, RandomGameAction action){
        switchPlayers();

//        int newObservation = -1;
//        if (RandomGameInfo.MAX_OBSERVATION == 1) {
//            newObservation = 0;
//        } else {
//            int observationCombinationID = new HighQualityRandom(ID).nextInt(CombinationGenerator.binomCoef(RandomGameInfo.MAX_BF-1, RandomGameInfo.MAX_OBSERVATION-1));
//            int[] tmp = CombinationGenerator.generateCombinationWithoutRepeating(RandomGameInfo.ACTIONS, RandomGameInfo.MAX_OBSERVATION-1, observationCombinationID);
//            assert (tmp != null);
//            if (action.getOrder() <= tmp[0]) {
//                newObservation = 0;
//            } else {
//                for (int obs=1; obs<RandomGameInfo.MAX_OBSERVATION-1; obs++) {
//                    if (action.getOrder() > tmp[obs-1] && action.getOrder() <= tmp[obs]) {
//                        newObservation = obs;
//                        break;
//                    }
//                }
//                if (newObservation < 0) newObservation = RandomGameInfo.MAX_OBSERVATION-1;
//            }
////            System.out.println(this.ID + "|" + action + " = " + Arrays.toString(tmp) + " | " + newObservation);
////            assert (newObservation >= 0);
//        }
//        observations.get(getPlayerToMove()).add(newObservation);

        int newObservation;
        if (new HighQualityRandom(newID).nextDouble() < RandomGameInfo.KEEP_OBS_PROB) {
            newObservation = action.getOrder() % RandomGameInfo.MAX_OBSERVATION;
        } else {
            newObservation = new HighQualityRandom(newID).nextInt(RandomGameInfo.MAX_OBSERVATION);
        }
        observations.get(getPlayerToMove()).add(newObservation);
    }

    @Override
    public Player getPlayerToMove() {
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new RandomGameState(this);
    }

    @Override
    public double[] getUtilities() {
        if (!isGameEnd())
            return new double[] { 0, 0 };

        double rndValue;

        if (RandomGameInfo.UTILITY_CORRELATION) {
            if (RandomGameInfo.BINARY_UTILITY) {
                rndValue = Math.signum(center); // P-game binary
            } else {
                rndValue = center; // P-game integer
            }
        } else {
            if (RandomGameInfo.BINARY_UTILITY) {
                rndValue = new HighQualityRandom(ID).nextInt(RandomGameInfo.MAX_UTILITY+1); // totally random binary
            } else {
                rndValue = new HighQualityRandom(ID).nextDouble()*RandomGameInfo.MAX_UTILITY; // totally random
            }
        }

        return new double[] { rndValue, -rndValue };
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public boolean isGameEnd() {
        return Math.min(history.getSequenceOf(players[0]).size(), history.getSequenceOf(players[1]).size()) == RandomGameInfo.MAX_DEPTH;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
        if (ISKey == null) {
            ISKey = new Pair<Integer, Sequence>(
                    uniqueHash(observations.get(getPlayerToMove()), Math.max(RandomGameInfo.MAX_OBSERVATION, RandomGameInfo.MAX_BF)),
                    getHistory().getSequenceOf(getPlayerToMove()));
        }
        return ISKey;
    }
    
    private int uniqueHash(List<Integer> list, int base){
        int out = 1;
        for (Integer i : list){
            out *= base;
            out += i;
        }
        Iterator i = getHistory().getSequenceOf(getPlayerToMove()).iterator();
        while (i.hasNext()) {
            RandomGameAction a = (RandomGameAction)i.next();
            out *= base;
            out += a.getOrder();
        }
        return out;
    }

    @Override
    public int hashCode() {
        if (changed) {
            hash = new HashCodeBuilder(17, 31).append(history).append(observations).append(ID).toHashCode();
            changed = false;
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;
        RandomGameState other = (RandomGameState) object;
        if (ID != other.ID)
            return false;
        if (!this.getISKeyForPlayerToMove().equals(other.getISKeyForPlayerToMove()))
            return false;
        return true;

    }

    protected void switchPlayers() {
        int newIndex = (getPlayerToMove().getId() + 1) % 2;
        playerToMove = players[newIndex];
    }

    @Override
    public String toString() {
        return "";
    }
    
    
}
