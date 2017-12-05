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


package cz.agents.gtlibrary.domain.oshizumo;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FastTanh;
import cz.agents.gtlibrary.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class OshiZumoGameState extends SimultaneousGameState {

    // lanctot: Note: maybe need to change this
    private static final long serialVersionUID = -1885423234236725674L;

    protected List<Action> sequenceForAllPlayers;

    protected int wrestlerLoc;
    protected int round;
    protected int p1Coins;
    protected int p2Coins;
    protected int p1Bid;
    protected int p2Bid;
    protected int currentPlayerIndex;

    protected ISKey key;
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
    protected double[] getEndGameUtilities() {
        double p1CoinsUti = (double)p1Coins / OZGameInfo.startingCoins;
        double p2CoinsUti = (double)p2Coins / OZGameInfo.startingCoins;
        if (!OZGameInfo.GENERAL_SUM){
            p1CoinsUti = 0.0;
            p2CoinsUti = 0.0;
        }
        if (OZGameInfo.BINARY_UTILITIES) {
            if (wrestlerLoc < OZGameInfo.locK)
                return new double[]{-1 + p1CoinsUti, 1 + p2CoinsUti, 0};
            else if (wrestlerLoc > OZGameInfo.locK)
                return new double[]{1 + p1CoinsUti, -1 + p2CoinsUti, 0};
            return new double[]{0 + p1CoinsUti, 0 + p2CoinsUti, 0};
        } else {
            return new double[] {wrestlerLoc - OZGameInfo.locK + p1CoinsUti, OZGameInfo.locK - wrestlerLoc + p2CoinsUti, 0};
        }
    }

    @Override
    public boolean isActualGameEnd() {
        return ((p1Bid == 0 && p2Bid == 0) || wrestlerLoc < 0 || wrestlerLoc >= (2 * OZGameInfo.locK + 1)
                || (p1Coins < OZGameInfo.minBid && p2Coins < OZGameInfo.minBid));
    }

    public double[] oldEvaluate() {
        if (wrestlerLoc > OZGameInfo.locK && p1Coins >= p2Coins)//on the side of p2 and p1 has more money than p2
            return new double[]{1, -1, 0};
        if (wrestlerLoc == OZGameInfo.locK && p1Coins > p2Coins)//in the middle and p1 has more money than p2
            return new double[]{1, -1, 0};
        if (wrestlerLoc < OZGameInfo.locK && p1Coins <= p2Coins)//on the side of p1 and p2 has more money than p1
            return new double[]{-1, 1, 0};
        if (wrestlerLoc == OZGameInfo.locK && p1Coins < p2Coins)//in the middle and p2 has more money than p1
            return new double[]{-1, 1, 0};
        if (p2Coins < (wrestlerLoc - OZGameInfo.locK) * OZGameInfo.minBid)//on the side of p2 and p2 has not enough money to change that
            return new double[]{1, -1, 0};
        if (p1Coins < (OZGameInfo.locK - wrestlerLoc) * OZGameInfo.minBid)//on the side of p1 and p1 has not enough money to change that
            return new double[]{1, -1, 0}; // <-- should be -1, 1, 0 ?
        double value = ((double) p1Coins) / OZGameInfo.minBid - ((double) p2Coins) / OZGameInfo.minBid + wrestlerLoc - OZGameInfo.locK;

        return new double[]{value, -value, 0};
    }
    
    @Override
    public double[] evaluate() {
        int minBid = (OZGameInfo.minBid == 0 ? 1 : OZGameInfo.minBid);
        double p1base = 0;

        if (wrestlerLoc > OZGameInfo.locK && p1Coins >= p2Coins)//on the side of p2 and p1 has more money than p2
            p1base = 0.5;
        if (wrestlerLoc == OZGameInfo.locK && p1Coins > p2Coins)//in the middle and p1 has more money than p2
            p1base = 0.5;
        if (wrestlerLoc < OZGameInfo.locK && p1Coins <= p2Coins)//on the side of p1 and p2 has more money than p1
            p1base = -0.5;
        if (wrestlerLoc == OZGameInfo.locK && p1Coins < p2Coins)//in the middle and p2 has more money than p1
            p1base = -0.5;
        if (p2Coins < (wrestlerLoc - OZGameInfo.locK) * OZGameInfo.minBid)//on the side of p2 and p2 has not enough money to change that
            p1base = 0.5;
        if (p1Coins < (OZGameInfo.locK - wrestlerLoc) * OZGameInfo.minBid)//on the side of p1 and p1 has not enough money to change that
            p1base = -0.5;

        //bb: this eval function puts more penalty when the wrestler is far away from the center, however, can't really say is better than the original one
//        double p1bonus = ((double) p1Coins) / minBid - ((double) p2Coins) / minBid + Math.signum(wrestlerLoc - OZGameInfo.locK)*(Math.pow(2,Math.abs(wrestlerLoc - OZGameInfo.locK))-1);
//        p1bonus /= 5.0;

        double p1bonus = ((double) p1Coins) / minBid - ((double) p2Coins) / minBid + wrestlerLoc - OZGameInfo.locK;
        p1bonus /= 3.0;

        double delta = p1base + p1bonus;
        
        // seems to play too gredily with this
        double p1eval = 0;
        if (OZGameInfo.BINARY_UTILITIES)
                p1eval = FastTanh.tanh(delta);
        else p1eval = delta;
        //double p1eval = delta;

        return new double[]{p1eval, -p1eval, 0};
    }


    @Override
    public boolean isDepthLimit() {
        return round >= depth;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = round + depth;
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
    public ISKey getISKeyForPlayerToMove() {
        if (key == null) {
            if (isPlayerToMoveNature())
                key = new PerfectRecallISKey(0, history.getSequenceOf(getPlayerToMove()));
            else
                key = new PerfectRecallISKey(sequenceForAllPlayers.hashCode(), getSequenceForPlayerToMove());
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
