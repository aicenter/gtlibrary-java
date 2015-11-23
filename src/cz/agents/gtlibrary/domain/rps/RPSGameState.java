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


package cz.agents.gtlibrary.domain.rps;

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
import java.util.Arrays;
import java.util.List;

public class RPSGameState extends SimultaneousGameState {

    // lanctot: Note: maybe need to change this
    private static final long serialVersionUID = -1885423234236725674L;

    protected List<Action> sequenceForAllPlayers;

    protected int round;
    protected int[] playerActions; 
    private int currentPlayerIndex;

    protected ISKey key;
    private int hashCode = -1;

    // standard game
    //public static double[][] payoffs = { {   0, -1,  1 }, 
    //                                     {   1,  0, -1 },
    //                                     {  -1,  1,  0 } }; 

    // biased game from Shafiei et al. 2009
    // http://web.cs.du.edu/~sturtevant/papers/uctcfr.pdf
    public static double[][] payoffs = { {     0, -0.25,  0.50 }, 
                                         {  0.25,     0, -0.05 },
                                         { -0.50,  0.05,  0 } }; 

    public RPSGameState() {
        super(RPSGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>();
        round = 0;
        currentPlayerIndex = 0;
        playerActions = new int[2];
    }

    public RPSGameState(Sequence natureSequence) {
        super(RPSGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>();
        round = 0;
        currentPlayerIndex = 0;
        playerActions = new int[2];
    }

    private Sequence createRandomSequence() {
        return null;
    }

    public RPSGameState(RPSGameState gameState) {
        super(gameState);
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.playerActions = Arrays.copyOf(gameState.playerActions, gameState.playerActions.length);

        this.sequenceForAllPlayers = new ArrayList<Action>(gameState.sequenceForAllPlayers);
    }

    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIndex];
    }

    private void addActionToSequenceForAllPlayers(RPSAction action) {
        sequenceForAllPlayers.add(action);
    }

    public void performFirstPlayerAction(RPSAction action) {
        cleanCache();

        //System.out.println("Here1 " + action);

        playerActions[0] = action.getValue();
        currentPlayerIndex = 1 - currentPlayerIndex;
    }

    public void performSecondPlayerAction(RPSAction action) {
        cleanCache();

        // add p1's action to the sequence
        addActionToSequenceForAllPlayers((RPSAction) history.getSequenceOf(RPSGameInfo.FIRST_PLAYER).getLast());
        addActionToSequenceForAllPlayers(action);

        playerActions[1] = action.getValue();

        currentPlayerIndex = 1 - currentPlayerIndex;

        // check game end
        round++;
    }

    public void performNatureAction(RPSAction action) {
        cleanCache();
    }

    private void cleanCache() {
        key = null;
        hashCode = -1;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return currentPlayerIndex == 2;
    }

    @Override
    protected double[] getEndGameUtilities() {
        double p1eval = payoffs[playerActions[0]-1][playerActions[1]-1];
        return new double[]{p1eval, -p1eval, 0};
    }

    @Override
    public boolean isActualGameEnd() {
        return (round >= 1); 
    }

    @Override
    public double[] evaluate() {
        // should not be used in online
        return null;
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
        return new RPSGameState(this);
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
        RPSGameState other = (RPSGameState) obj;

        if (!(currentPlayerIndex == other.currentPlayerIndex
                && round == other.round
                && playerActions[0] == other.playerActions[0]
                && playerActions[1] == other.playerActions[1]))
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
        String str = "Player: " + currentPlayerIndex + ", player actions = " 
                + playerActions[0] + " " + playerActions[1] + "\n\n";
        return str;
    }

}
