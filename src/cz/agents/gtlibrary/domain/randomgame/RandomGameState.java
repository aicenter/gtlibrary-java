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


package cz.agents.gtlibrary.domain.randomgame;


import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.iinodes.TLAction;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

public class RandomGameState extends SimultaneousGameState {

    private static final long serialVersionUID = 6086530572992658181L;
    private static int rootID;

    protected int ID;
    protected int center;
    protected Player playerToMove;
    protected Map<Player, ArrayList<Integer>> observations = new FixedSizeMap<Player, ArrayList<Integer>>(2);

    private int hash = 0;
    protected ISKey ISKey = null;
    protected boolean changed = true;

    public RandomGameState() {
        super(RandomGameInfo.ALL_PLAYERS);
        ID = new HighQualityRandom(RandomGameInfo.seed).nextInt();
        rootID = ID;
        playerToMove = RandomGameInfo.FIRST_PLAYER;
        observations.put(players[0], new ArrayList<Integer>());
        observations.put(players[1], new ArrayList<Integer>());
        center = 0;
    }

    public RandomGameState(RandomGameState gameState) {
        super(gameState);
        this.ID = gameState.ID;
        this.playerToMove = gameState.playerToMove;
        observations.put(players[0], new ArrayList<Integer>(gameState.observations.get(players[0])));
        observations.put(players[1], new ArrayList<Integer>(gameState.observations.get(players[1])));
        center = gameState.center;
    }

    @Override
    public void transformInto(GameState state) {
        super.transformInto(state);
        RandomGameState gameState = (RandomGameState)state;
        this.ID = gameState.ID;
        this.playerToMove = gameState.playerToMove;
        observations.put(players[0], new ArrayList<Integer>(gameState.observations.get(players[0])));
        observations.put(players[1], new ArrayList<Integer>(gameState.observations.get(players[1])));
        center = gameState.center;
    }

    protected void evaluateAction(RandomGameAction action) {
//        assert false; //FIXME !!! the following center modification works for simultaneous-move only, but it does not make sense to use it this way for general II EFG
        int newID = (ID + action.getOrder()) * 31 + 17;
//        if (getPlayerToMove().getId() == 1)
            center += new HighQualityRandom(newID).nextInt(RandomGameInfo.MAX_CENTER_MODIFICATION * 2 + 1) - RandomGameInfo.MAX_CENTER_MODIFICATION;
//        int newID = (ID + action.getOrder()) * 31 + 17;
//        Random r = new HighQualityRandom(rootID+ISKey.getLeft());
//        for (int i=0; i<action.getOrder(); i++) r.nextInt(RandomGameInfo.MAX_CENTER_MODIFICATION * 2 + 1);
//        center += (r.nextInt(RandomGameInfo.MAX_CENTER_MODIFICATION * 2 + 1) - RandomGameInfo.MAX_CENTER_MODIFICATION);
        generateObservations(newID, action);

        this.ID = newID;
        this.ISKey = null;
        this.changed = true;
    }

    protected void generateObservations(int newID, RandomGameAction action) {
        switchPlayers();

        int newObservation = new HighQualityRandom(newID).nextInt(RandomGameInfo.MAX_OBSERVATION);
        observations.get(getPlayerToMove()).add(newObservation);
    }

    @Override
    public Player getPlayerToMove() {
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new RandomGameState(this);
    }

    protected double[] getEndGameUtilities() {
        if (!isGameEnd())
            return new double[]{0, 0};

        double rndValue;

        if (RandomGameInfo.UTILITY_CORRELATION) {
            if (RandomGameInfo.BINARY_UTILITY) {
                rndValue = Math.signum(center); // P-game binary
            } else {
                rndValue = center; // P-game integer
            }
        } else {
            if (RandomGameInfo.BINARY_UTILITY) {
                rndValue = new HighQualityRandom(ID).nextInt(2*RandomGameInfo.MAX_UTILITY + 1) - RandomGameInfo.MAX_UTILITY ; // totally random binary
            } else {
                rndValue = new HighQualityRandom(ID).nextDouble() * 2 * RandomGameInfo.MAX_UTILITY - RandomGameInfo.MAX_UTILITY; // totally random
            }
        }
        return new double[]{rndValue, -rndValue};
    }

    @Override
    public Rational[] getExactUtilities() {
        if (!isGameEnd())
            return new Rational[]{Rational.ZERO, Rational.ZERO};

        Rational rndValue;

        if (RandomGameInfo.UTILITY_CORRELATION) {
            if (RandomGameInfo.BINARY_UTILITY) {
                rndValue = new Rational((int) Math.signum(center)); // P-game binary
            } else {
                rndValue = new Rational(center); // P-game integer
            }
        } else {
            if (RandomGameInfo.BINARY_UTILITY) {
                rndValue = new Rational(new HighQualityRandom(ID).nextInt(RandomGameInfo.MAX_UTILITY + 1)); // totally random binary
            } else {
                double doubleValue = new HighQualityRandom(ID).nextDouble() * RandomGameInfo.MAX_UTILITY;

                rndValue = new Rational(1).fromDouble(doubleValue); // totally random
                assert Math.abs(doubleValue - rndValue.doubleValue()) < 1e-10;
            }
        }
        return new Rational[]{rndValue, rndValue.negate()};
    }

    @Override
    public double[] evaluate() {
        double normalization = 1;

        if (RandomGameInfo.BINARY_UTILITY)
            normalization = 2 * RandomGameInfo.MAX_CENTER_MODIFICATION * RandomGameInfo.MAX_DEPTH;

        return new double[]{center / normalization, -center / normalization};
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return Rational.ZERO;
    }

    @Override
    public boolean isActualGameEnd() {
        return Math.min(history.getSequenceOf(players[0]).size(), history.getSequenceOf(players[1]).size()) == RandomGameInfo.MAX_DEPTH;
    }

    @Override
    public boolean isDepthLimit() {
        return Math.min(history.getSequenceOf(players[0]).size(), history.getSequenceOf(players[1]).size()) >= depth;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth + Math.min(history.getSequenceOf(players[0]).size(), history.getSequenceOf(players[1]).size());
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public cz.agents.gtlibrary.iinodes.ISKey getISKeyForPlayerToMove() {
        if (ISKey == null) {
            ISKey = new PerfectRecallISKey(
                    uniqueHash(observations.get(getPlayerToMove()), Math.max(RandomGameInfo.MAX_OBSERVATION, RandomGameInfo.MAX_BF)),
                    getHistory().getSequenceOf(getPlayerToMove()));
        }
        return ISKey;
    }

    private int uniqueHash(List<Integer> list, int base) {
        int out = 1;
        for (Integer i : list) {
            out *= base;
            out += i;
        }
        Iterator i = getHistory().getSequenceOf(getPlayerToMove()).iterator();
        while (i.hasNext()) {
            out *= base;
            Action a = (Action) i.next();
            if (a instanceof RandomGameAction) {
                out += ((RandomGameAction)a).getOrder();
            }
            if (a instanceof TLAction)
                out += ((TLAction)a).getIndex() + 1;
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

    protected void switchPlayers() {
        int newIndex = (getPlayerToMove().getId() + 1) % 2;
        playerToMove = players[newIndex];
    }

    @Override
    public String toString() {
        return "RG-ID" + ID + "C:" + center;
    }

    public int getCenter() {
        return center;
    }
}
