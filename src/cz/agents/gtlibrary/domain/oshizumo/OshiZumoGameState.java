package cz.agents.gtlibrary.domain.oshizumo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.util.Collections;

public class OshiZumoGameState extends GameStateImpl {

    // lanctot: Note: maybe need to change this
    private static final long serialVersionUID = -1885423234236725674L;

    protected List<Action> sequenceForAllPlayers;
    private OshiZumoAction faceUpCard;

    protected int wrestlerLoc;
    protected int round;
    protected int p1Coins;
    protected int p2Coins;
    protected int p1Bid;
    protected int p2Bid;
    private int currentPlayerIndex;

    protected Pair<Integer, Sequence> key;
    private int hashCode = -1;

    public OshiZumoGameState() {
        super(OZGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>();
        round = 0;
        currentPlayerIndex = 0;
        p1Bid = p2Bid = -1;
        p1Coins = p2Coins = OZGameInfo.startingCoins;

        // if K = 3, there are 7 locations:      ---W---
        // wrestler starts in middle (3):        0123456
        wrestlerLoc = OZGameInfo.locK;
    }

    public OshiZumoGameState(Sequence natureSequence) {
        super(OZGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>();
        round = 0;
        currentPlayerIndex = 0;
        p1Bid = p2Bid = -1;
        p1Coins = p2Coins = OZGameInfo.startingCoins;

        // if K = 3, there are 7 locations:      ---W---
        // wrestler starts in middle (3):        0123456
        wrestlerLoc = OZGameInfo.locK;
    }

    private Sequence createRandomSequence() {
        return null;
    }

    public OshiZumoGameState(OshiZumoGameState gameState) {
        super(gameState);
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.wrestlerLoc = gameState.wrestlerLoc;
        this.p1Bid = gameState.p1Bid;
        this.p2Bid = gameState.p2Bid;
        this.p1Coins = gameState.p1Coins;
        this.p2Coins = gameState.p2Coins;
        this.sequenceForAllPlayers = new ArrayList<Action>(gameState.sequenceForAllPlayers);
    }

    public Sequence getNatureSequence() {
        return null;
    }

    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIndex];
    }

    private void addActionToSequenceForAllPlayers(OshiZumoAction action) {
        sequenceForAllPlayers.add(action);
    }

    public void performFirstPlayerAction(OshiZumoAction action) {
        cleanCache();

        //System.out.println("Here1 " + action);

        p1Bid = action.getValue();
        currentPlayerIndex = 1 - currentPlayerIndex;
    }

    public void performSecondPlayerAction(OshiZumoAction action) {
        cleanCache();

        // add p1's action to the sequence
        addActionToSequenceForAllPlayers((OshiZumoAction) history.getSequenceOf(OZGameInfo.FIRST_PLAYER).getLast());
        addActionToSequenceForAllPlayers(action);

        //System.out.println("Here2" + action);

        p2Bid = action.getValue();

        if (p1Bid > p2Bid) {
            wrestlerLoc++;
        } else if (p2Bid > p1Bid) {
            wrestlerLoc--;
        }

        p1Coins -= p1Bid;
        p2Coins -= p2Bid;

        p1Bid = p2Bid = -1;

        currentPlayerIndex = 1 - currentPlayerIndex;

        // check game end
        round++;
//        if (isGameEnd())
//            currentPlayerIndex = 0;
    }

    public void performNatureAction(OshiZumoAction action) {
        cleanCache();
    }

    private void cleanCache() {
        key = null;
        hashCode = -1;
    }

    public ArrayList<Integer> getBidsForPlayerToMove() {
        ArrayList<Integer> list = new ArrayList<Integer>();

        int curCoins = (currentPlayerIndex == 0 ? p1Coins : p2Coins);

        if (curCoins >= OZGameInfo.minBid) {
            for (int b = OZGameInfo.minBid; b <= curCoins; b++)
                list.add(b);
        } else {
            // always allowed to bid 0
            list.add(0);
        }

        return list;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return currentPlayerIndex == 2;
    }

    @Override
    public double[] getUtilities() {
        if (isGameEnd()) {
            if (wrestlerLoc < OZGameInfo.locK)
                return new double[]{-1, 1, 0};
            else if (wrestlerLoc > OZGameInfo.locK)
                return new double[]{1, -1, 0};
        }
        return new double[]{0, 0, 0};
    }

    @Override
    public boolean isGameEnd() {
        return ((p1Bid == 0 && p2Bid == 0) || wrestlerLoc < 0 || wrestlerLoc >= (2 * OZGameInfo.locK + 1) || (p1Coins < OZGameInfo.minBid && p2Coins < OZGameInfo.minBid));
    }

    @Override
    public GameState copy() {
        return new OshiZumoGameState(this);
    }

    public int getRound() {
        return round;
    }

    public List<Action> getSequenceForAllPlayers() {
        return sequenceForAllPlayers;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 1;
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
        OshiZumoGameState other = (OshiZumoGameState) obj;

        if (!(currentPlayerIndex == other.currentPlayerIndex
                && round == other.round
                && p1Bid == other.p1Bid
                && p2Bid == other.p2Bid
                && p1Coins == other.p1Coins
                && p2Coins == other.p2Coins))
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

    @Override
    public String toString() {
        //return history.toString();
        String str = "Player: " + currentPlayerIndex + ", coins: "
                + p1Coins + " " + p2Coins + ", bids: " + p1Bid + " " + p2Bid + "\n\n";
        for (int i = 0; i <= 2 * OZGameInfo.locK; i++)
            str += (i == wrestlerLoc ? "W" : "-");
        str += "\n";
        return str;
    }

}
