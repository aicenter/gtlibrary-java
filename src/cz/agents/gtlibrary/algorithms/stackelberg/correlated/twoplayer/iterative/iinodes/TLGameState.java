package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.iinodes;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;

/**
 * Created by Jakub Cerny on 07/11/2017.
 */
public class TLGameState extends GameStateImpl {

    GameState gameState;
    Sequence originalPlayerSequence;
    Player leader;
    boolean isMiddleState;
    int hash;

    public TLGameState(Player[] players, GameState gameState, Player leader) {
        super(players);
        this.gameState = gameState;
        this.history = gameState.getHistory().copy();
        this.originalPlayerSequence = gameState.getSequenceFor(leader);
        this.leader = leader;
        this.isMiddleState = false;
    }

    public TLGameState(Player[] players, Player leader, int hash, GameState gameState) {
        super(players);
        this.gameState = gameState;
        this.history = gameState.getHistory().copy();
        this.originalPlayerSequence = gameState.getSequenceFor(leader);
        this.isMiddleState = true;
        this.leader = leader;
        this.hash = hash;
    }

    @Override
    public void transformInto(GameState gameState){
        super.transformInto(gameState);
        this.isMiddleState = false;
        this.gameState = gameState;
        this.history = gameState.getHistory().copy();
        this.originalPlayerSequence = gameState.getSequenceFor(leader);
    }

    public Sequence getOriginalPlayerSequence(){return originalPlayerSequence; }

    public GameState getGameState(){ return  gameState; }

    public boolean isReachableLeaf(Sequence sequence){
        System.out.println(sequence + " " + originalPlayerSequence);
        return sequence.isPrefixOf(originalPlayerSequence);
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 1.0;
    }

    public void setHistory(History history){
        this.history = history;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return new PerfectRecallISKey(getSequenceForPlayerToMove().hashCode(), getSequenceForPlayerToMove());
    }

    @Override
    public Player getPlayerToMove() {
        return leader;
    }

    @Override
    public GameState copy() {
        if (isMiddleState) return  new TLGameState(getAllPlayers(), leader, hash, gameState);
        return new TLGameState(getAllPlayers(), gameState.copy(), leader);
    }

    @Override
    public double[] getUtilities() {
        if (isMiddleState) return null;
        return gameState.getUtilities();
    }

    @Override
    public boolean isGameEnd() {
        if (isMiddleState) return false;
        return gameState.isGameEnd();
    }

    @Override
    public boolean isPlayerToMoveNature() {
        if (isMiddleState) return false;
        return gameState.isPlayerToMoveNature();
    }

    @Override
    public int hashCode() {
        if (isMiddleState) return hash;
        return gameState.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (isMiddleState){
            if (object instanceof TLGameState){
                if (((TLGameState)object).isMiddleState && ((TLGameState)object).hash == hash)
                    return true;
            }
            return false;
        }
        return gameState.equals(object);
    }

    @Override
    public String toString() {
        if (isMiddleState) return "[MS]";
        return "[TL of "+gameState.toString()+"]";
    }
}
