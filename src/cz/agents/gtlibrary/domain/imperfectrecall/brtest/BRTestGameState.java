package cz.agents.gtlibrary.domain.imperfectrecall.brtest;

import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.HashMap;
import java.util.Map;

public class BRTestGameState extends GameStateImpl {

    public static void main(String[] args) {
        GambitEFG writer = new GambitEFG();
        BRTestGameState root = new BRTestGameState();
        BRTestExpander expander = new BRTestExpander(new ImperfectRecallAlgorithmConfig());

        writer.buildAndWrite("BRTest.gbt", root, expander);
    }

    private Map<Player, Map<Player, Observations>> observations;

    public BRTestGameState() {
        super(BRTestGameInfo.ALL_PLAYERS);
        observations = new HashMap<>(players.length);
        for (Player player : players) {
            Map<Player, Observations> playerObservations = new HashMap<>(players.length);

            for (Player player1 : players) {
                playerObservations.put(player1, new Observations(player, player1));
            }
            observations.put(player, playerObservations);
        }
    }

    public BRTestGameState(BRTestGameState gameState) {
        super(gameState);
        observations = deepCopy(gameState.observations);
    }

    private Map<Player, Map<Player, Observations>> deepCopy(Map<Player, Map<Player, Observations>> observations) {
        Map<Player, Map<Player, Observations>> copy = new HashMap<>(observations.size());

        for (Map.Entry<Player, Map<Player, Observations>> playerObservationsEntry : observations.entrySet()) {
            Map<Player, Observations> playerObservationsCopy = new HashMap<>(playerObservationsEntry.getValue().size());

            for (Map.Entry<Player, Observations> observationsEntry : playerObservationsEntry.getValue().entrySet()) {
                playerObservationsCopy.put(observationsEntry.getKey(), observationsEntry.getValue().copy());
            }
            copy.put(playerObservationsEntry.getKey(), playerObservationsCopy);
        }
        return copy;
    }

    public void addP1ObservationFor(Player player, int p1Observation) {
        observations.get(player).get(players[0]).add(new ObservationImpl(p1Observation));
    }

    public void addP2ObservationFor(Player player, int p2Observation) {
        observations.get(player).get(players[1]).add(new ObservationImpl(p2Observation));
    }

    @Override
    public Player getPlayerToMove() {
        if (history.getSequenceOf(players[0]).size() == 0 || history.getSequenceOf(players[0]).size() == 1)
            return players[0];
        return players[1];
    }

    @Override
    public GameState copy() {
        return new BRTestGameState(this);
    }

    @Override
    public double[] getUtilities() {
        Sequence p1Sequence = history.getSequenceOf(players[0]);
        Sequence p2Sequence = history.getSequenceOf(players[1]);

        if(((BRTestAction)p1Sequence.get(0)).getId().equals("a")) {
            if(((BRTestAction)p1Sequence.get(1)).getId().equals("c")) {
                if(((BRTestAction)p2Sequence.get(0)).getId().equals("e"))
                    return new double[]{5, -5};
                return new double[]{0, 0};
            }
        }
        if(((BRTestAction)p1Sequence.get(0)).getId().equals("b")) {
            if(((BRTestAction)p1Sequence.get(1)).getId().equals("d")) {
                if(((BRTestAction)p2Sequence.get(0)).getId().equals("f"))
                    return new double[]{5, -5};
                return new double[]{0, 0};
            }
        }
        return new double[]{-10, 10};
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public boolean isGameEnd() {
        return history.getSequenceOf(players[0]).size() == 2 && history.getSequenceOf(players[1]).size() == 1;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        Map<Player, Observations> playerObservations = observations.get(getPlayerToMove());

        return new ImperfectRecallISKey(playerObservations.get(getPlayerToMove()), playerObservations.get(players[1 - getPlayerToMove().getId()]), null);
    }

    @Override
    public int hashCode() {
        return history.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        BRTestGameState other = (BRTestGameState) object;

        return history.equals(other.history);
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
