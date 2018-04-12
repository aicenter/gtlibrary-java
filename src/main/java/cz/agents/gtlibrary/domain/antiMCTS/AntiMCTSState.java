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


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.domain.antiMCTS;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

/**
 *
 * @author vilo
 */
public class AntiMCTSState extends GameStateImpl {
    int curDepth=0;
    boolean gameEnded=false;

    public AntiMCTSState() {
        super(AntiMCTSInfo.players);
    }

    public AntiMCTSState(GameStateImpl gameState) {
        super(gameState);
        AntiMCTSState other = (AntiMCTSState) gameState;
        curDepth = other.curDepth;
        gameEnded = other.gameEnded;
    }
    
    

    @Override
    public Player getPlayerToMove() {
        return AntiMCTSInfo.realPlayer;
    }

    @Override
    public GameState copy() {
        return new AntiMCTSState(this);
    }

    double[] utilities = null;
    @Override
    public double[] getUtilities() {
        assert isGameEnd();
        if (utilities != null) return utilities;
        utilities = new double[2];
        if (AntiMCTSInfo.exponentialRewards)
            utilities[0] = ((AntiMCTSAction) getSequenceFor(AntiMCTSInfo.realPlayer).getLast()).right ? 1 : 1/Math.pow(2, curDepth);
        else 
            utilities[0] = ((AntiMCTSAction) getSequenceFor(AntiMCTSInfo.realPlayer).getLast()).right ? 1 : (AntiMCTSInfo.gameDepth-curDepth)/(2.0*(AntiMCTSInfo.gameDepth-1));
        utilities[1] = -utilities[0];
        return utilities;
    }

    @Override
    public boolean isGameEnd() {
        return gameEnded;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public int hashCode() {
        return curDepth + (gameEnded ? AntiMCTSInfo.gameDepth+1 : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AntiMCTSState other = (AntiMCTSState) obj;
        if (curDepth != other.curDepth) {
            return false;
        }
        if (gameEnded != other.gameEnded) {
            return false;
        }
        return true;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return new PerfectRecallISKey(hashCode(), history.getSequenceOf(getPlayerToMove()));
    }

    @Override
    public String toString() {
        return "D" + curDepth + (gameEnded ? "T" : "F");
    }
    
}
