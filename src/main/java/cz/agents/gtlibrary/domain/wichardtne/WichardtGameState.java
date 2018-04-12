package cz.agents.gtlibrary.domain.wichardtne;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRConfig;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.domain.randomabstraction.IDObservation;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

public class WichardtGameState extends GameStateImpl {

    public static void main(String[] args) {
        GameState root = new WichardtGameState();
        Expander<IRCFRInformationSet> expander = new WichardtExpander<>(new IRCFRConfig());
        GambitEFG gambit = new GambitEFG();

        gambit.buildAndWrite("wichardtTest.gbt", root, expander);
    }

    private int round;

    public WichardtGameState() {
        super(WichardtGameInfo.ALL_PLAYERS);
        round = 0;
    }

    public WichardtGameState(WichardtGameState gameState) {
        super(gameState);
        this.round = gameState.round;
    }

    public void increaseRound() {
        round++;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        Observations observations = new Observations(getPlayerToMove(), getPlayerToMove());

        observations.add(new IDObservation(round));
        return new ImperfectRecallISKey(observations, null, null);
    }

    @Override
    public Player getPlayerToMove() {
        return round <= 1 ? players[0] : players[1];
    }

    @Override
    public GameState copy() {
        return new WichardtGameState(this);
    }

    @Override
    public double[] getUtilities() {
        Sequence p1Sequence = getSequenceFor(players[0]);
        Sequence p2Sequence = getSequenceFor(players[1]);

        if (((WichardtAction) p1Sequence.get(0)).getType().equals("l1")) {
            if (((WichardtAction) p1Sequence.get(1)).getType().equals("l2")) {
                if (((WichardtAction) p2Sequence.get(0)).getType().equals("L"))
                    return new double[]{1, -1};
                if (((WichardtAction) p2Sequence.get(0)).getType().equals("R"))
                    return new double[]{0, 0};
            }
            if (((WichardtAction) p2Sequence.get(0)).getType().equals("L"))
                return new double[]{-5, 5};
            return new double[]{-20, 20};
        } else {
            if (((WichardtAction) p1Sequence.get(1)).getType().equals("r2")) {
                if (((WichardtAction) p2Sequence.get(0)).getType().equals("L"))
                    return new double[]{0, 0};
                if (((WichardtAction) p2Sequence.get(0)).getType().equals("R"))
                    return new double[]{1, -1};
            }
            if (((WichardtAction) p2Sequence.get(0)).getType().equals("L"))
                return new double[]{-5, 5};
            return new double[]{-20, 20};
        }
    }


    @Override
    public boolean isGameEnd() {
        return round == 3;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WichardtGameState)) return false;

        WichardtGameState that = (WichardtGameState) o;

        return history.equals(that.history);
    }

    @Override
    public int hashCode() {
        return history.hashCode();
    }

    public int getRound() {
        return round;
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
