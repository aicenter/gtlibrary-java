package cz.agents.gtlibrary.domain.randomgameimproved;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.domain.randomgameimproved.centers.ModificationGenerator;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

public class RandomGameState extends GameStateImpl {

    private static final long serialVersionUID = 6086530572992658181L;
    private static int rootID;

    protected int ID;
    protected int center;
    protected Player playerToMove;
    protected Map<Player, Map<Player, Observations>> observations;

    private int hash = 0;
    private int depth;
    protected ISKey informationSetKey = null;
    protected boolean changed = true;

    private List<Integer> actionProbabilities;
    private int probabilitySum;
    private int[] centers;

    private static final int MAX_ACTION_RND_NUMBER = 10000;
    private ModificationGenerator modificationGenerator;

    private int lastPlayerIndex;
    private int currentPlayerSeries;

    private int actionsCount;

    public static void main(String[] args) {
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(new SequenceFormIRConfig(new RandomGameInfo()));
        GambitEFG gambit = new GambitEFG();

        gambit.buildAndWrite("RG_test.gbt", root, expander);
        System.out.println(expander.getAlgorithmConfig().getAllInformationSets().size());
        System.out.println(((SequenceFormIRConfig)expander.getAlgorithmConfig()).getSequencesFor(RandomGameInfo.FIRST_PLAYER).size()
                + " " + ((SequenceFormIRConfig)expander.getAlgorithmConfig()).getSequencesFor(RandomGameInfo.SECOND_PLAYER).size());
    }

    public RandomGameState() {
        super(RandomGameInfo.ALL_PLAYERS);
        ID = new HighQualityRandom(RandomGameInfo.seed).nextInt();
        rootID = ID;

        modificationGenerator = RandomGameInfo.modificationGenerator.copy();
        initializeObservationsMaps();
        createEmptyObservationLists();

        centers = new int[players.length - 1];
        depth = 0;
        playerToMove = players[randomPlayerIndex(ID)];

        lastPlayerIndex = 0; //Does not matter
        currentPlayerSeries = 1;

        generateActionsCount();
        if (isPlayerToMoveNature()) updateActionProbabilities();
    }

    public RandomGameState(RandomGameState gameState) {
        super(gameState);
        this.ID = gameState.ID;
        this.playerToMove = gameState.playerToMove;

        initializeObservationsMaps();
        copyObservationLists(gameState.observations);

        lastPlayerIndex = gameState.lastPlayerIndex; //Does not matter
        currentPlayerSeries = gameState.currentPlayerSeries;

        modificationGenerator = gameState.modificationGenerator.copy();
        centers = Arrays.copyOf(gameState.centers, gameState.centers.length);
        depth = gameState.depth;
        actionsCount = gameState.getActionsCount();
        if (gameState.isPlayerToMoveNature()) {
            actionProbabilities = new ArrayList<>(gameState.actionProbabilities);
            probabilitySum = gameState.probabilitySum;
        }
    }

    @Override
    public void transformInto(GameState state) {
        super.transformInto(state);
        RandomGameState gameState = (RandomGameState)state;
        this.ID = gameState.ID;
        this.playerToMove = gameState.playerToMove;

        initializeObservationsMaps();
        copyObservationLists(gameState.observations);

        lastPlayerIndex = gameState.lastPlayerIndex; //Does not matter
        currentPlayerSeries = gameState.currentPlayerSeries;

        modificationGenerator = gameState.modificationGenerator.copy();
        centers = Arrays.copyOf(gameState.centers, gameState.centers.length);
        depth = gameState.depth;
        actionsCount = gameState.getActionsCount();
        if (gameState.isPlayerToMoveNature()) {
            actionProbabilities = new ArrayList<>(gameState.actionProbabilities);
            probabilitySum = gameState.probabilitySum;
        }

        this.informationSetKey = null;
    }

    private void copyObservationLists(Map<Player, Map<Player, Observations>> originalObservations) {
        for (Map.Entry<Player, Map<Player, Observations>> observationsEntry : originalObservations.entrySet()) {
            for (Map.Entry<Player, Observations> playerObservationsEntry : observationsEntry.getValue().entrySet()) {
                observations.get(observationsEntry.getKey()).put(playerObservationsEntry.getKey(), playerObservationsEntry.getValue().copy());
            }
        }
    }

    private void initializeObservationsMaps() {
        observations = new HashMap<>();
        for (Player player : players) {
            observations.put(player, new HashMap<>(3));
        }
    }

    private void createEmptyObservationLists() {
        for (Player player : players) {
            for (Player pl : players) {
                Observations newObservations;
                if (RandomGameInfo.IMPERFECT_RECALL && (!RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1 || player.getId() == 0)) {
                    newObservations = RandomGameInfo.OBSERVATIONS_TYPE.getObservations(player, pl);
                } else {
                    newObservations = new Observations(player, pl);
                }
                observations.get(player).put(pl, newObservations);
            }
        }
    }

    protected void evaluateAction(RandomGameAction action) {
        int newID = (ID + action.getOrder()) * 31 + 17;

        center += new HighQualityRandom(newID).nextInt(RandomGameInfo.MAX_CENTER_MODIFICATION * 2 + 1) - RandomGameInfo.MAX_CENTER_MODIFICATION;
        updateCenters(newID);

        depth++;

        int switchingSeed = RandomGameInfo.MULTIPLE_PLAYER_DEPTHS ? ID : (depth + (int) RandomGameInfo.seed);

        this.ID = newID;
        this.informationSetKey = null;
        this.changed = true;

        if (!RandomGameInfo.IMPERFECT_RECALL) switchPlayers(switchingSeed);

        generateObservations(newID, action);

        if (RandomGameInfo.IMPERFECT_RECALL) switchPlayers(switchingSeed);

        changeObservationsLevels(newID);
        generateActionsCount();
        if (isPlayerToMoveNature()) updateActionProbabilities();
    }

    private void changeObservationsLevels(int newID) {
        for (Player pl : players) {
            for (Map.Entry<Player, Observations> playerObservationsEntry : observations.get(pl).entrySet()) {
                playerObservationsEntry.getValue().performDepthChangingOperations(newID + pl.getId() * players.length + playerObservationsEntry.getKey().getId());
            }
        }
    }

    private void generateActionsCount() {
        actionsCount = RandomGameInfo.MAX_BF;
        if (!RandomGameInfo.FIXED_SIZE_BF) {
            int seed = rootID + getISKeyForPlayerToMove().hashCode();
            actionsCount = new HighQualityRandom(seed).nextInt(RandomGameInfo.MAX_BF - RandomGameInfo.MIN_BF + 1) + RandomGameInfo.MIN_BF;
        }
    }

    private void updateActionProbabilities() {
        actionProbabilities = new ArrayList<>();
        HighQualityRandom rnd = new HighQualityRandom(ID);
        probabilitySum = 0;
        for (int i = 0; i < actionsCount; i++) {
            int nextProbability = 1 + rnd.nextInt(MAX_ACTION_RND_NUMBER);
            probabilitySum += nextProbability;
            actionProbabilities.add(nextProbability);
        }
    }

    private void updateCenters(int seed) {
        HighQualityRandom rnd = new HighQualityRandom(seed);
        double p1Value = modificationGenerator.generateUtility(rnd);
        double p2Value = modificationGenerator.generateCorrelatedUtility(rnd, p1Value);

        centers[0] += p1Value;
        centers[1] += p2Value;
    }

    protected void generateObservations(int newID, RandomGameAction action) {
        for (Player player : players) {
            int newObservation;
            if (RandomGameInfo.IMPERFECT_RECALL && (!RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1 || player.getId() != 1 || action.getInformationSet().getPlayer().getId() != 1)) {
                double p = new HighQualityRandom(newID + action.getOrder()).nextDouble();
                if (player.equals(getPlayerToMove()) && !RandomGameInfo.ABSENT_MINDEDNESS) {
                    newObservation = (int) (p * RandomGameInfo.MAX_OBSERVATION);
                } else {
                    if (p < RandomGameInfo.EMPTY_OBSERVATION_PROBABILITY) {
                        newObservation = -1;
                    } else {
                        p = (p - RandomGameInfo.EMPTY_OBSERVATION_PROBABILITY) / (1 - RandomGameInfo.EMPTY_OBSERVATION_PROBABILITY);
                        newObservation = (int) (p * RandomGameInfo.MAX_OBSERVATION);
                    }
                }
            } else {
                newObservation = action.getOrder();
            }
            observations.get(player).get(getPlayerToMove()).add((newObservation == -1 ? ObservationImpl.EMPTY_OBSERVATION : new ObservationImpl(newObservation)));
        }
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
    public boolean isGameEnd() {
        return depth >= RandomGameInfo.MAX_DEPTH;
    }

    public double[] getUtilities() {
        double[] utilities = new double[RandomGameInfo.ALL_PLAYERS.length];

        if (!isGameEnd())
            return utilities;

        double rndValue;

        if (RandomGameInfo.UTILITY_CORRELATION) {
            if (RandomGameInfo.INTEGER_UTILITY) {
                for (int i = 0; i < centers.length; i++)
                    utilities[i] = Math.signum(centers[i]);
            } else {
                for (int i = 0; i < centers.length; i++)
                    utilities[i] = centers[i];
            }
        } else {
            if (RandomGameInfo.INTEGER_UTILITY) {
                rndValue = new HighQualityRandom(ID).nextInt(2 * RandomGameInfo.MAX_UTILITY + 1) - RandomGameInfo.MAX_UTILITY; // totally random binary
            } else {
                rndValue = new HighQualityRandom(ID).nextDouble() * 2 * RandomGameInfo.MAX_UTILITY - RandomGameInfo.MAX_UTILITY; // totally random
            }
            utilities[0] = rndValue;
            utilities[1] = -rndValue;
        }
        return utilities;
    }

    @Override
    public Rational[] getExactUtilities() {
        if (!isGameEnd())
            return new Rational[]{Rational.ZERO, Rational.ZERO};

        Rational rndValue;
        Rational[] utilities = new Rational[centers.length];

        if (RandomGameInfo.UTILITY_CORRELATION) {
            if (RandomGameInfo.INTEGER_UTILITY) {
                for (int i = 0; i < centers.length; i++)
                    utilities[i] = new Rational((int) Math.signum(centers[i]));
            } else {
                for (int i = 0; i < centers.length; i++)
                    utilities[i] = new Rational(centers[i]);
            }
        } else {
            if (RandomGameInfo.INTEGER_UTILITY) {
                rndValue = new Rational(new HighQualityRandom(ID).nextInt(RandomGameInfo.MAX_UTILITY + 1)); // totally random binary
            } else {
                double doubleValue = new HighQualityRandom(ID).nextDouble() * RandomGameInfo.MAX_UTILITY;

                rndValue = new Rational(1).fromDouble(doubleValue); // totally random
                assert Math.abs(doubleValue - rndValue.doubleValue()) < 1e-10;
            }
            utilities[0] = rndValue;
            utilities[1] = rndValue.negate();
        }
        return utilities;
    }

    @Override
    public double[] evaluate() {
        double normalization = 1;

        if (RandomGameInfo.INTEGER_UTILITY)
            normalization = 2 * RandomGameInfo.MAX_CENTER_MODIFICATION * RandomGameInfo.MAX_DEPTH;

        return new double[]{centers[0] / normalization, centers[1] / normalization};
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        if (!isPlayerToMoveNature())
            return 0;
        return (double) actionProbabilities.get(((RandomGameAction) action).getOrder()) / probabilitySum;
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        if (!isPlayerToMoveNature())
            return Rational.ZERO;
        return new Rational(actionProbabilities.get(((RandomGameAction) action).getOrder()), probabilitySum);
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return getPlayerToMove().getId() == RandomGameInfo.ALL_PLAYERS.length - 1;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (informationSetKey == null) {
            if (RandomGameInfo.IMPERFECT_RECALL) {
                Map<Player, Observations> playerObservationsMap = observations.get(getPlayerToMove());
                informationSetKey = new ImperfectRecallISKey(
                        playerObservationsMap.get(getPlayerToMove()),
                        playerObservationsMap.get(getOpponent(getPlayerToMove())),
                        playerObservationsMap.get(players[players.length - 1]));
            } else {
                informationSetKey = new PerfectRecallISKey(
                        observations.get(getPlayerToMove()).get(getPlayerToMove()).hashCode(),
                        getHistory().getSequenceOf(getPlayerToMove()));
            }
        }
        return informationSetKey;
    }

    private Player getOpponent(Player player) {
        if (player.getId() == players.length - 1) return players[players.length - 1];
        return players[1 - player.getId()];
    }

    private int uniqueHash(Observations observations, int base) {
        int out = 1;
        for (Observation i : observations) {
            out *= base;
            out += ((ObservationImpl) i).getIndex();
        }
        Iterator i = getHistory().getSequenceOf(getPlayerToMove()).iterator();
        while (i.hasNext()) {
            RandomGameAction a = (RandomGameAction) i.next();
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

    protected void switchPlayers(int seed) {
        int playerIndex = randomPlayerIndex(seed);
        playerToMove = players[playerIndex];
        if (!isPlayerToMoveNature()) {
            if (playerIndex != lastPlayerIndex) currentPlayerSeries = 0;
            currentPlayerSeries++;
            lastPlayerIndex = playerIndex;
        }
    }

    private int randomPlayerIndex(long seed) {
        double p = new HighQualityRandom(seed).nextDouble();
        int natureIndex = RandomGameInfo.ALL_PLAYERS.length - 1;

        int playerIndex;
        if (p < RandomGameInfo.NATURE_STATE_PROBABILITY && isNatureValid()) {
            playerIndex = natureIndex;
        } else {
            p = (p - RandomGameInfo.NATURE_STATE_PROBABILITY) / (1 - RandomGameInfo.NATURE_STATE_PROBABILITY);
            double currentPlayerProbability = Math.pow(0.5, currentPlayerSeries);
            playerIndex = p < currentPlayerProbability ? lastPlayerIndex : 1 - lastPlayerIndex;
        }

        return playerIndex;
    }

    private boolean isNatureValid() {
        return depth < RandomGameInfo.MAX_DEPTH - 1;
    }

    @Override
    public String toString() {
        return "RG-ID: " + ID + " C: " + Arrays.toString(centers) + conciseHistoryToString();
    }

    private String conciseHistoryToString() {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<Player, Sequence> entry : history.entrySet()) {
            builder.append(entry.getKey()).append(": ");
            for (Action action : entry.getValue()) {
                builder.append(/*((RandomGameAction)*/action/*).getOrder()*/);
            }
            builder.append(", ");
        }
        return builder.toString();
    }

    public int[] getCenters() {
        return centers;
    }

    public int getActionsCount() {
        return actionsCount;
    }

    public int getRootID() {
        return rootID;
    }

    public int getID() {
        return ID;
    }
}
