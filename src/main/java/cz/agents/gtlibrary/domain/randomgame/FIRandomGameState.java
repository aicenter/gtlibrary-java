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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

/**
 *
 * @author vilo
 */
public class FIRandomGameState extends RandomGameState {

	private static final long serialVersionUID = -3801266782217001643L;

	public FIRandomGameState(FIRandomGameState gameState) {
        super(gameState);
    }

    public FIRandomGameState() {
        super();
    }
    
    @Override
    protected void generateObservations(int newID, RandomGameAction action) {
        switchPlayers();
    }

    @Override
    public cz.agents.gtlibrary.iinodes.ISKey getISKeyForPlayerToMove() {
        if (ISKey == null) {
            ISKey = new PerfectRecallISKey(
                    hashCode(),
                    getHistory().getSequenceOf(getPlayerToMove()));
        }
        return ISKey;
    }
    
    @Override
    public GameState copy() {
        return new FIRandomGameState(this);
    }

    @Override
    public String toString() {
        return "";
        //return "FIGS" + getHistory();
    }
}
