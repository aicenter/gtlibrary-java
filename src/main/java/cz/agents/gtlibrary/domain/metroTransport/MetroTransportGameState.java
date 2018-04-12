package cz.agents.gtlibrary.domain.metroTransport;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

/**
 * Created by Jakub Cerny on 05/10/2017.
 */
public class MetroTransportGameState extends GameStateImpl {

    int timeStep;

    int attackerReward;
    int attackerCaughtCount;

    MetroTransportAction defenderAction;
    MetroTransportAction attackerAction;
    Player playerToMove;


    public MetroTransportGameState(Player[] players) {
        super(players);
    }

    public MetroTransportGameState(GameStateImpl gameState) {
        super(gameState);

    }

    protected void performDefenderAction(MetroTransportAction action){
        defenderAction = action;
    }

    protected void performAttackerAction(MetroTransportAction action){
        attackerAction = action;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 1.0;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return null;
    }

    @Override
    public Player getPlayerToMove() {
        // check which player can move (i.e. not traveling between stations)
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new MetroTransportGameState(this);
    }

    @Override
    public double[] getUtilities() {
        if (MetroTransportGameInfo.ZERO_SUM)
            return new double[]{-attackerReward, attackerReward};
        else
            return new double[]{attackerCaughtCount, attackerReward};
    }

    @Override
    public boolean isGameEnd() {
        return timeStep >= MetroTransportGameInfo.NUMBER_OF_STEPS ||
                MetroTransportGameInfo.random.nextDouble() < MetroTransportGameInfo.TERMINATION_PROB ||
                attackerReward == MetroTransportGameInfo.graph.getRewardSum();
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        return false;
    }

    protected boolean isCaught(){
        return false;
    }

}
