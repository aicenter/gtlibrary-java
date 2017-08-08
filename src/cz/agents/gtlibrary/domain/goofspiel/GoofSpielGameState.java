/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.agents.gtlibrary.domain.goofspiel;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FastTanh;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.*;
import java.util.Map.Entry;

public class GoofSpielGameState extends SimultaneousGameState {

    private static final long serialVersionUID = -1885375538236725674L;

    private Map<Player, HashSet<Integer>> playerCards;
    protected List<Action> sequenceForAllPlayers;
    private GoofSpielAction faceUpCard;
    private int natureSequenceIndex;

    protected int[] playerScore;

    protected int round;
    private int currentPlayerIndex;

    protected ISKey key;
    private int hashCode = -1;

    public GoofSpielGameState() {
        super(GSGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>(
                GSGameInfo.CARDS_FOR_PLAYER.length * 3);
        playerCards = new FixedSizeMap<Player, HashSet<Integer>>(3);
        playerScore = new int[2];
        round = 0;
        currentPlayerIndex = 2;
        natureSequenceIndex = 0;

        createPlayerCards();
    }

    public GoofSpielGameState(int depth) {
        super(GSGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>(
                GSGameInfo.CARDS_FOR_PLAYER.length * 3);
        playerCards = new FixedSizeMap<Player, HashSet<Integer>>(3);
        playerScore = new int[2];
        round = 0;
        currentPlayerIndex = 2;
        natureSequenceIndex = 0;

        createPlayerCards();
    }

    public GoofSpielGameState(Sequence natureSequence, int natureSequenceIndex) {
        super(GSGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>(
                GSGameInfo.CARDS_FOR_PLAYER.length * 3);
        playerCards = new FixedSizeMap<Player, HashSet<Integer>>(3);
        playerScore = new int[2];
        round = 0;
        currentPlayerIndex = 2;
        this.natureSequenceIndex = natureSequenceIndex;

        GSGameInfo.natureSequence = natureSequence;

        createPlayerCards();
    }

    public GoofSpielGameState(GoofSpielGameState gameState) {
        super(gameState);
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.playerScore = gameState.playerScore.clone();
        this.playerCards = getDeepCopyOfPlayerCards(gameState.playerCards);
        this.faceUpCard = gameState.faceUpCard;
        this.sequenceForAllPlayers = new ArrayList<Action>(
                gameState.sequenceForAllPlayers);
        this.natureSequenceIndex = gameState.natureSequenceIndex;

    }

    public Sequence getNatureSequence() {
        return GSGameInfo.natureSequence.getSubSequence(natureSequenceIndex,
                GSGameInfo.natureSequence.size() - natureSequenceIndex);
    }

    private Map<Player, HashSet<Integer>> getDeepCopyOfPlayerCards(
            Map<Player, HashSet<Integer>> playerCards) {
        Map<Player, HashSet<Integer>> playerCardsCopy = new FixedSizeMap<Player, HashSet<Integer>>(
                3);

        for (Entry<Player, HashSet<Integer>> entry : playerCards.entrySet()) {
            playerCardsCopy.put(entry.getKey(),
                    new HashSet<Integer>(entry.getValue()));
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

    public int getNatureSequenceIndex() {
        return natureSequenceIndex;
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
        natureSequenceIndex++;
        faceUpCard = action;
        currentPlayerIndex = 0;
    }

    private void cleanCache() {
        key = null;
        hashCode = -1;
    }

    private void evaluateRound(GoofSpielAction firstPlayerCard,
                               GoofSpielAction secondPlayerCard) {
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
        if (history.getSequenceOf(GSGameInfo.FIRST_PLAYER).size() == history
                .getSequenceOf(GSGameInfo.SECOND_PLAYER).size()) {
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
        if (history.getSequenceOf(GSGameInfo.FIRST_PLAYER).size() > history
                .getSequenceOf(GSGameInfo.SECOND_PLAYER).size())
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
        if (!GSGameInfo.BINARY_UTILITIES) {
            return new double[]{playerScore[0] - playerScore[1], playerScore[1] - playerScore[0], 0};
        } else {
            if (playerScore[0] > playerScore[1])
                return new double[]{1, -1, 0};
            if (playerScore[0] < playerScore[1])
                return new double[]{-1, 1, 0};
            return new double[]{0, 0, 0};
        }
    }

    @Override
    public Rational[] getExactUtilities() {
        if (playerScore[0] > playerScore[1])
            return new Rational[]{Rational.ONE, Rational.ONE.negate(),
                    Rational.ZERO};
        if (playerScore[0] < playerScore[1])
            return new Rational[]{Rational.ONE.negate(), Rational.ONE,
                    Rational.ZERO};
        return new Rational[]{Rational.ZERO, Rational.ZERO, Rational.ZERO};
    }

    @Override
    public boolean isActualGameEnd() {
        return round == GSGameInfo.depth;
    }

    @Override
    public boolean isDepthLimit() {
        return round >= depth;
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
        double scale = (GSGameInfo.BINARY_UTILITIES) ? 1 : (Math.max(1, GSGameInfo.CARDS_FOR_PLAYER.length * (GSGameInfo.CARDS_FOR_PLAYER.length - 1) / 2));
        if (round == GSGameInfo.depth)
            return getEndGameUtilities();
        double sum = playerScore[0] + playerScore[1];

        if (sum == 0)
            return new double[]{0, 0, 0};
        if (unwinnableFor(players[0]))
            return new double[]{-scale, scale, 0};
        if (unwinnableFor(players[1]))
            return new double[]{scale, -scale, 0};
        // double reward = (playerScore[0] - sum / 2) / (sum / 2);
        // if(reward > 0)
        // reward -= 1e-3;
        // else if(reward < 0)
        // reward += 1e-3;
        // assert reward < 1 && reward > -1;
        // return new double[]{reward, -reward, 0};
        int sumRemCardsP1 = 0;
        for (Integer i : playerCards.get(GSGameInfo.FIRST_PLAYER))
            sumRemCardsP1 += i;
        int sumRemCardsP2 = 0;
        for (Integer i : playerCards.get(GSGameInfo.SECOND_PLAYER))
            sumRemCardsP2 += i;
        double value = FastTanh
                .tanh((playerScore[0] - playerScore[1])
                        / sum
                        * round * 0.0
                        / GSGameInfo.CARDS_FOR_PLAYER.length
                        + (getSumOfRemainingCards() / (GSGameInfo.CARDS_FOR_PLAYER.length
                        * (GSGameInfo.CARDS_FOR_PLAYER.length + 1) / 2))
                        * (sumRemCardsP1 - sumRemCardsP2)
                        / (sumRemCardsP1 + sumRemCardsP2 + 1)) * scale;
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
        // if (score + getHighestAchievableScore(player) < opponentScore)
        // return true;
        return false;
    }

    private int getHighestAchievableScore(Player player) {
        List<Integer> sortedCards = new ArrayList<>();
        int possibleWinCount = getPossibleWinCount(player);
        int highestAchievableScore = 0;

        for (Action action : GSGameInfo.natureSequence.getSubSequence(
                natureSequenceIndex, GSGameInfo.natureSequence.size()
                        - natureSequenceIndex)) {
            sortedCards.add(((GoofSpielAction) action).getValue());
        }
        Collections.sort(sortedCards);

        if (sortedCards.size() > 0)
            for (int i = sortedCards.size() - 1; i > Math.max(
                    sortedCards.size() - possibleWinCount - 1, -1); i--) {
                highestAchievableScore += sortedCards.get(i);
            }
        return highestAchievableScore;
    }

    private int getPossibleWinCount(Player player) {
        int possibleWinCount = 0;

        for (Integer playerCard : playerCards.get(player)) {
            for (Integer opponentCard : playerCards.get(players[1 - player
                    .getId()])) {
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
        if (round != other.round)
            return false;
        if (!Arrays.equals(playerScore, other.playerScore))
            return false;
        if (playerCards == null) {
            if (other.playerCards != null)
                return false;
        } else if (!playerCards.equals(other.playerCards))
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
    public ISKey getISKeyForPlayerToMove() {
        if (key == null) {
            if (isPlayerToMoveNature())
                key = new PerfectRecallISKey(0, new ArrayListSequenceImpl(
                        getSequenceForPlayerToMove()));
            else
                key = new PerfectRecallISKey(
                        sequenceForAllPlayers.hashCode(),
                        new ArrayListSequenceImpl(getSequenceForPlayerToMove()));
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

        for (Action action : GSGameInfo.natureSequence.getSubSequence(
                natureSequenceIndex, GSGameInfo.natureSequence.size()
                        - natureSequenceIndex)) {
            sum += ((GoofSpielAction) action).getValue();
        }
        return sum;
    }

    private void reverseFirstPlayerAction(GoofSpielAction lastAction) {
        playerCards.get(GSGameInfo.FIRST_PLAYER).add(lastAction.getValue());
    }

    private void reverseSecondPlayerAction(GoofSpielAction lastAction) {
        GoofSpielAction firstPlayerAction = (GoofSpielAction) history
                .getSequenceOf(GSGameInfo.FIRST_PLAYER).getLast();

        playerCards.get(GSGameInfo.SECOND_PLAYER).add(lastAction.getValue());
        round--;

        if (firstPlayerAction.compareTo(lastAction) > 0) {
            playerScore[0] -= getLastActionOf(GSGameInfo.NATURE).getValue();
        } else if (firstPlayerAction.compareTo(lastAction) < 0) {
            playerScore[1] -= getLastActionOf(GSGameInfo.NATURE).getValue();
        }

        Action removedAction = sequenceForAllPlayers
                .remove(sequenceForAllPlayers.size() - 1);

        assert removedAction.equals(lastAction);
        removedAction = sequenceForAllPlayers.remove(sequenceForAllPlayers
                .size() - 1);
        assert removedAction.equals(firstPlayerAction);
    }

    @Override
    public void reverseAction() {
        GoofSpielAction lastAction = (GoofSpielAction) history.getLastAction();

        currentPlayerIndex = lastAction.getPlayer().getId();
        cleanCache();

        if (currentPlayerIndex == 0) {
            reverseFirstPlayerAction(lastAction);
        } else if (currentPlayerIndex == 1) {
            reverseSecondPlayerAction(lastAction);
        } else {
            reverseNaturePlayerAction(lastAction);
        }

        super.reverseAction();
    }

    private void reverseNaturePlayerAction(GoofSpielAction lastAction) {
        playerCards.get(GSGameInfo.NATURE).add(lastAction.getValue());
        Action removedaction = sequenceForAllPlayers
                .remove(sequenceForAllPlayers.size() - 1);

        if (history.getSequenceOf(GSGameInfo.NATURE).size() != 1)
            faceUpCard = (GoofSpielAction) (history
                    .getSequenceOf(GSGameInfo.NATURE).get(history
                            .getSequenceOf(GSGameInfo.NATURE).size() - 2));
        else
            faceUpCard = null;
        natureSequenceIndex--;
    }

}
