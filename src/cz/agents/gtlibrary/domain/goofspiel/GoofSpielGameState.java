package cz.agents.gtlibrary.domain.goofspiel;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;
import java.util.Map.Entry;

public class GoofSpielGameState extends SimultaneousGameState {

    private static final long serialVersionUID = -1885375538236725674L;

    private Map<Player, HashSet<Integer>> playerCards;
    protected List<Action> sequenceForAllPlayers;
    private GoofSpielAction faceUpCard;
    private Sequence natureSequence;

    protected int[] playerScore;

    protected int round;
    private int currentPlayerIndex;

    protected Pair<Integer, Sequence> key;
    private int hashCode = -1;

    public GoofSpielGameState() {
        super(GSGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>(GSGameInfo.CARDS_FOR_PLAYER.length * 3);
        playerCards = new FixedSizeMap<Player, HashSet<Integer>>(3);
        playerScore = new int[2];
        round = 0;
        currentPlayerIndex = 2;
        natureSequence = createRandomSequence();

        createPlayerCards();
    }

    public GoofSpielGameState(int depth) {
        super(GSGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>(GSGameInfo.CARDS_FOR_PLAYER.length * 3);
        playerCards = new FixedSizeMap<Player, HashSet<Integer>>(3);
        playerScore = new int[2];
        round = 0;
        currentPlayerIndex = 2;
        natureSequence = createRandomSequence();

        createPlayerCards();
    }

    public GoofSpielGameState(Sequence natureSequence) {
        super(GSGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>(GSGameInfo.CARDS_FOR_PLAYER.length * 3);
        playerCards = new FixedSizeMap<Player, HashSet<Integer>>(3);
        playerScore = new int[2];
        round = 0;
        currentPlayerIndex = 2;
        this.natureSequence = natureSequence;

        createPlayerCards();
    }

    public GoofSpielGameState(Sequence natureSequence, int depth) {
        super(GSGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>(GSGameInfo.CARDS_FOR_PLAYER.length * 3);
        playerCards = new FixedSizeMap<Player, HashSet<Integer>>(3);
        playerScore = new int[2];
        round = 0;
        currentPlayerIndex = 2;
        this.natureSequence = natureSequence;

        createPlayerCards();
    }

    private Sequence createRandomSequence() {
        ArrayList<Action> actions = new ArrayList(GSGameInfo.CARDS_FOR_PLAYER.length);
        for (int card : GSGameInfo.CARDS_FOR_PLAYER)
            actions.add(new GoofSpielAction(card, GSGameInfo.NATURE, null));
        if (GSGameInfo.useFixedNatureSequence && GSGameInfo.seed == 1) Collections.reverse(actions);
        else Collections.shuffle(actions, new HighQualityRandom(GSGameInfo.seed));
        Sequence natureSequence = new LinkedListSequenceImpl(GSGameInfo.NATURE);
        natureSequence.addAllAsLast(actions);
        return natureSequence;
    }

    public GoofSpielGameState(GoofSpielGameState gameState) {
        super(gameState);
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.playerScore = gameState.playerScore.clone();
        this.playerCards = getDeepCopyOfPlayerCards(gameState.playerCards);
        this.faceUpCard = gameState.faceUpCard;
        this.sequenceForAllPlayers = new ArrayList<Action>(gameState.sequenceForAllPlayers);
        this.natureSequence = new LinkedListSequenceImpl(gameState.natureSequence);
    }

    public Sequence getNatureSequence() {
        return natureSequence;
    }

    private Map<Player, HashSet<Integer>> getDeepCopyOfPlayerCards(Map<Player, HashSet<Integer>> playerCards) {
        Map<Player, HashSet<Integer>> playerCardsCopy = new FixedSizeMap<Player, HashSet<Integer>>(3);

        for (Entry<Player, HashSet<Integer>> entry : playerCards.entrySet()) {
            playerCardsCopy.put(entry.getKey(), new HashSet<Integer>(entry.getValue()));
        }
        return playerCardsCopy;
    }

    private void createPlayerCards() {
        for (Player player : players) {
            HashSet<Integer> cardsForPlayer = new LinkedHashSet<Integer>();

            for (int cardValue : GSGameInfo.CARDS_FOR_PLAYER) {
                cardsForPlayer.add(cardValue);
            }
            playerCards.put(player, cardsForPlayer);
        }
    }

    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIndex];
    }

    private void addActionToSequenceForAllPlayers(GoofSpielAction action) {
        sequenceForAllPlayers.add(action);
    }

    public void performFirstPlayerAction(GoofSpielAction action) {
        cleanCache();
        evaluate(action, getLastActionOf(GSGameInfo.SECOND_PLAYER));
        playerCards.get(GSGameInfo.FIRST_PLAYER).remove(action.getValue());
    }

    public void performSecondPlayerAction(GoofSpielAction action) {
        cleanCache();
        evaluate(getLastActionOf(GSGameInfo.FIRST_PLAYER), action);
        playerCards.get(GSGameInfo.SECOND_PLAYER).remove(action.getValue());
    }

    public void performNatureAction(GoofSpielAction action) {
        cleanCache();
        playerCards.get(GSGameInfo.NATURE).remove(action.getValue());
        addActionToSequenceForAllPlayers(action);
        natureSequence.removeFirst();
        faceUpCard = action;
        currentPlayerIndex = 0;
    }

    private void cleanCache() {
        key = null;
        hashCode = -1;
    }

    private void evaluateRound(GoofSpielAction firstPlayerCard, GoofSpielAction secondPlayerCard) {
        if (firstPlayerCard.compareTo(secondPlayerCard) > 0) {
            playerScore[0] += getLastActionOf(GSGameInfo.NATURE).getValue();
        } else if (firstPlayerCard.compareTo(secondPlayerCard) < 0) {
            playerScore[1] += getLastActionOf(GSGameInfo.NATURE).getValue();
        }
        round++;
        if (isGameEnd())
            currentPlayerIndex = 0;
    }

    private GoofSpielAction getLastActionOf(Player player) {
        if (history.getSequenceOf(player).size() == 0) {
            return null;
        }
        return (GoofSpielAction) history.getSequenceOf(player).getLast();
    }

    private void evaluate(GoofSpielAction fpAction, GoofSpielAction spAction) {
        if (history.getSequenceOf(GSGameInfo.FIRST_PLAYER).size() == history.getSequenceOf(GSGameInfo.SECOND_PLAYER).size()) {
            endRound(fpAction, spAction);
        } else {
            switchPlayers();
        }
    }

    public void endRound(GoofSpielAction fpAction, GoofSpielAction spAction) {
        addActionToSequenceForAllPlayers(fpAction);
        addActionToSequenceForAllPlayers(spAction);
        currentPlayerIndex = 2;
        evaluateRound(fpAction, spAction);
    }

    public void switchPlayers() {
        if (history.getSequenceOf(GSGameInfo.FIRST_PLAYER).size() > history.getSequenceOf(GSGameInfo.SECOND_PLAYER).size())
            currentPlayerIndex = 1;
        else
            currentPlayerIndex = 0;
    }

    public GoofSpielAction getFaceUpCard() {
        return faceUpCard;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return currentPlayerIndex == 2;
    }

    protected double[] getEndGameUtilities() {
        if (playerScore[0] > playerScore[1])
            return new double[]{1, -1, 0};
        if (playerScore[0] < playerScore[1])
            return new double[]{-1, 1, 0};
        return new double[]{0, 0, 0};
    }

    @Override
    public Rational[] getExactUtilities() {
        if (playerScore[0] > playerScore[1])
            return new Rational[]{Rational.ONE, Rational.ONE.negate(), Rational.ZERO};
        if (playerScore[0] < playerScore[1])
            return new Rational[]{Rational.ONE.negate(), Rational.ONE, Rational.ZERO};
        return new Rational[]{Rational.ZERO, Rational.ZERO, Rational.ZERO};
    }

    @Override
    public boolean isActualGameEnd() {
        return round == GSGameInfo.depth;
    }

    @Override
    public boolean isDepthLimit() {
        return round > depth;
    }

    @Override
    public GameState copy() {
        return new GoofSpielGameState(this);
    }

    public HashSet<Integer> getCardsFor(Player actor) {
        return playerCards.get(actor);
    }

    public int getRound() {
        return round;
    }

    public List<Action> getSequenceForAllPlayers() {
        return sequenceForAllPlayers;
    }

    public int[] getPlayerScore() {
        return playerScore;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        if (GSGameInfo.useFixedNatureSequence)
            return 1;
        return 1. / playerCards.get(GSGameInfo.NATURE).size();
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        if (GSGameInfo.useFixedNatureSequence)
            return Rational.ONE;
        return new Rational(1, playerCards.get(GSGameInfo.NATURE).size());
    }

    @Override
    public double[] evaluate() {
        if (round == GSGameInfo.depth)
            return getEndGameUtilities();
        double sum = playerScore[0] + playerScore[1];

        if (sum == 0)
            return new double[]{0, 0, 0};
        if (unwinnableFor(players[0]))
            return new double[]{-1, 1, 0};
        if (unwinnableFor(players[1]))
            return new double[]{1, -1, 0};
        double value =  (playerScore[0] - sum / 2) / (sum / 2);

        if(value > 0)
            value -= 1e-3;
        else if(value < 0)
            value += 1e-3;
        assert value < 1 && value > -1;
        return new double[]{value, -value, 0};
    }

    private int getSum(int[] cards) {
        int sum = 0;

        for (int i = 0; i < cards.length; i++) {
            sum += cards[i];
        }
        return sum;
    }

    private boolean unwinnableFor(Player player) {
        int score = playerScore[player.getId()];
        int opponentScore = playerScore[1 - player.getId()];

        if (score + getSumOfRemainingCards() < opponentScore)
            return true;
        if (score + getHighestAchievableScore(player) < opponentScore)
            return true;
        return false;
    }

    private int getHighestAchievableScore(Player player) {
        List<Integer> sortedCards = new ArrayList<>();
        int possibleWinCount = getPossibleWinCount(player);
        int highestAchievableScore = 0;

        for (Action action : natureSequence) {
            sortedCards.add(((GoofSpielAction) action).getValue());
        }
        Collections.sort(sortedCards);

        if (sortedCards.size() > 0)
            for (int i = sortedCards.size() - 1; i > Math.max(sortedCards.size() - possibleWinCount - 1, -1); i--) {
                highestAchievableScore += sortedCards.get(i);
            }
        return highestAchievableScore;
    }

    private int getPossibleWinCount(Player player) {
        int possibleWinCount = 0;

        for (Integer playerCard : playerCards.get(player)) {
            for (Integer opponentCard : playerCards.get(players[1 - player.getId()])) {
                if (playerCard > opponentCard) {
                    possibleWinCount++;
                    break;
                }
            }
        }
        return possibleWinCount;
    }

    public void setDepth(int depth) {
        this.depth = depth + round;
    }

    @Override
    public int hashCode() {
        if (hashCode == -1)
            hashCode = history.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GoofSpielGameState other = (GoofSpielGameState) obj;
        if (currentPlayerIndex != other.currentPlayerIndex)
            return false;
        if (faceUpCard == null) {
            if (other.faceUpCard != null)
                return false;
        } else if (!faceUpCard.equals(other.faceUpCard))
            return false;
        if (playerCards == null) {
            if (other.playerCards != null)
                return false;
        } else if (!playerCards.equals(other.playerCards))
            return false;
        if (!Arrays.equals(playerScore, other.playerScore))
            return false;
        if (!Arrays.equals(players, other.players))
            return false;
        if (round != other.round)
            return false;
        if (sequenceForAllPlayers == null) {
            if (other.sequenceForAllPlayers != null)
                return false;
        } else if (!sequenceForAllPlayers.equals(other.sequenceForAllPlayers))
            return false;
        if (!history.equals(other.history))
            return false;
        return true;
    }

    @Override
    public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
        if (key == null) {
            if (isPlayerToMoveNature())
                key = new Pair<Integer, Sequence>(0, history.getSequenceOf(getPlayerToMove()));
            else
                key = new Pair<Integer, Sequence>(sequenceForAllPlayers.hashCode(), getSequenceForPlayerToMove());
        }
        return key;
    }

    public Collection<Integer> getCardsForPlayerToMove() {
        return playerCards.get(getPlayerToMove());
    }

    @Override
    public String toString() {
        return history.toString();
    }

    public int getSumOfRemainingCards() {
        int sum = faceUpCard.getValue();

        for (Action action : natureSequence) {
            sum += ((GoofSpielAction) action).getValue();
        }
        return sum;
    }
}
