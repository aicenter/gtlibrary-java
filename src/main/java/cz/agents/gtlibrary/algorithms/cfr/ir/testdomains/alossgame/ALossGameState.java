package cz.agents.gtlibrary.algorithms.cfr.ir.testdomains.alossgame;

import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashMap;
import java.util.Map;

public class ALossGameState extends GameStateImpl {

    private Player playerToMove;
    private Map<Player, Map<Player, Observations>> observations;

    public ALossGameState() {
        super(ALossGameInfo.ALL_PLAYERS);
        playerToMove = ALossGameInfo.FIRST_PLAYER;
        observations = new HashMap<>();
        for (Player player : players) {
            Map<Player, Observations> playerObservations = new HashMap<>();

            for (Player player1 : players) {
                playerObservations.put(player1, new Observations(player, player1));
            }
            observations.put(player, playerObservations);
        }
    }

    public ALossGameState(ALossGameState gameState) {
        super(gameState);
        playerToMove = gameState.playerToMove;
        observations = deepCopy(gameState.observations)  ;
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

    @Override
    public Player getPlayerToMove() {
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new ALossGameState(this);
    }

    @Override
    public double[] getUtilities() {
        ALossAction p1Last = (ALossAction) getSequenceFor(ALossGameInfo.FIRST_PLAYER).getLast();
        ALossAction p2Last = (ALossAction) getSequenceFor(ALossGameInfo.SECOND_PLAYER).getLast();

        if (p1Last.getName().equals("a") && p2Last.getName().equals("c"))
            return new double[]{0, 0};
        if (p1Last.getName().equals("b") && p2Last.getName().equals("d"))
            return new double[]{0, 0};
        if (p1Last.getName().equals("g") && p2Last.getName().equals("d"))
            return new double[]{-1, 1};
        if (p1Last.getName().equals("g") && p2Last.getName().equals("c"))
            return new double[]{-5, 5};
        if (p1Last.getName().equals("h") && p2Last.getName().equals("d"))
            return new double[]{6, -6};
        if (p1Last.getName().equals("h") && p2Last.getName().equals("c"))
            return new double[]{1, -1};
        return new double[0];
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public boolean isGameEnd() {
        Sequence p1Sequence = getSequenceFor(ALossGameInfo.FIRST_PLAYER);
        Sequence p2Sequence = getSequenceFor(ALossGameInfo.SECOND_PLAYER);

        if(p1Sequence.isEmpty() || p2Sequence.isEmpty())
            return false;
        if (p1Sequence.size() >= 2)
            return true;
        if (((ALossAction) p1Sequence.getLast()).getName().equals("a") && ((ALossAction) p2Sequence.getLast()).getName().equals("c"))
            return true;
        if (((ALossAction) p1Sequence.getLast()).getName().equals("b") && ((ALossAction) p2Sequence.getLast()).getName().equals("d"))
            return true;
        return false;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        Map<Player, Observations> playerToMoveObservations = observations.get(playerToMove);

        return new ImperfectRecallISKey(playerToMoveObservations.get(playerToMove), playerToMoveObservations.get(players[1 - playerToMove.getId()]), null);
    }

    @Override
    public int hashCode() {
        return history.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return history.equals(((ALossGameState) object).history);
    }

    public void addObservations() {
        Map<Player, Observations> playerToMoveObservations = observations.get(playerToMove);
        Map<Player, Observations> opponentObservations = observations.get(players[1 - playerToMove.getId()]);

        playerToMoveObservations.get(playerToMove).add(new ObservationImpl(1));
        opponentObservations.get(playerToMove).add(new ObservationImpl(1));
    }

    public void switchPlayers() {
        playerToMove = players[1 - playerToMove.getId()];
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
