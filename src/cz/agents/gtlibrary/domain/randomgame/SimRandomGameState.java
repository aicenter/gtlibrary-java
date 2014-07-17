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

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.HighQualityRandom;

/**
 *
 * @author vilo
 */
public class SimRandomGameState extends RandomGameState {

	private static final long serialVersionUID = -3801266782217001643L;

	public SimRandomGameState(SimRandomGameState gameState) {
        super(gameState);
    }

    public SimRandomGameState() {
        super();
    }
    
    @Override
    protected void generateObservations(int newID, RandomGameAction action) {
        Player[] allPlayers = getAllPlayers();
        if (getPlayerToMove().equals(allPlayers[1])){
            observations.get(allPlayers[0]).add(action.getOrder());
            observations.get(allPlayers[1]).add(((RandomGameAction)getHistory().getSequenceOf(allPlayers[0]).getLast()).getOrder());
        }
        switchPlayers();
    }

    @Override
    public GameState copy() {
        return new SimRandomGameState(this);
    }

    @Override
    protected void evaluateAction(RandomGameAction action) {
        int newID = (ID + action.getOrder()) * 31 + 17;
        if (getPlayerToMove().getId() == 1)
            center += new HighQualityRandom(newID).nextInt(RandomGameInfo.MAX_CENTER_MODIFICATION * 2 + 1) - RandomGameInfo.MAX_CENTER_MODIFICATION;
        generateObservations(newID, action);

        this.ID = newID;
        this.ISKey = null;
        this.changed = true;
    }

}
