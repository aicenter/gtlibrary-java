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

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Iterator;

public class IIGoofSpielGameState extends GoofSpielGameState {

    public IIGoofSpielGameState(GoofSpielGameState gameState) {
        super(gameState);
    }

    public IIGoofSpielGameState(Sequence natureSequence, int sequenceIndex) {
        super(natureSequence, sequenceIndex);
    }

    public IIGoofSpielGameState() {
        super();
    }


    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (key == null) {
            if (isPlayerToMoveNature())
                key = new PerfectRecallISKey(0, history.getSequenceOf(getPlayerToMove()));
            else {
                int code = playerScore[0];
                Iterator<Action> it = sequenceForAllPlayers.iterator();
                for (int i = 0; i < round; i++) {
                    it.next();//nature player
                    GoofSpielAction a0 = (GoofSpielAction) it.next();
                    GoofSpielAction a1 = (GoofSpielAction) it.next();
                    code *= 3;
                    code += 1 + Math.signum(a0.compareTo(a1));
                }
                key = new PerfectRecallISKey(code, getSequenceForPlayerToMove());
            }
        }
        return key;
    }

    @Override
    public GameState copy() {
        return new IIGoofSpielGameState(this);
    }

}
