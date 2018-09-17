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
import cz.agents.gtlibrary.iinodes.PSKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.util.Iterator;

public class IIGoofSpielGameState extends GoofSpielGameState implements DomainWithPublicState {

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
        if (isKey == null) {
            if (isPlayerToMoveNature())
                isKey = new PerfectRecallISKey(0, history.getSequenceOf(getPlayerToMove()));
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
                isKey = new PerfectRecallISKey(code, getSequenceForPlayerToMove());
            }
        }
        return isKey;
    }

    @Override
    public PSKey getPSKeyForPlayerToMove() {
        PSKey maybeHasForcedKey = super.getPSKeyForPlayerToMove();
        if(maybeHasForcedKey != null) return maybeHasForcedKey;

        if (psKey == null) {
            if(!GSGameInfo.useFixedNatureSequence) {
                throw new RuntimeException("pskey implemented only for fixed nature sequences!");
            }
            if(GSGameInfo.depth > 15) {
                throw new RuntimeException("pskey works up to depth of 15");
            }

            // hash code idea:
            // For each round except the current one, keep 2 bits of information
            // as for win/lose/draw at that round. Move that to the left (so the
            // latest round is the leftest). The current round keeps 2 bits as for
            // chance playing/player 1 playing.

            int hash = 0;
            int round = 0;
            int roundHash;
            int p1Action = 0, p2Action;
            int totalRounds = (history.getHistory().size()) / 3;
            for (Pair<Player, Action> edge: history.getHistory()) {
                GoofSpielAction goofSpielAction = (GoofSpielAction) edge.getRight();
                Integer playerIdx = edge.getLeft().getId();

                if(round < totalRounds) {
                    if (playerIdx == GSGameInfo.NATURE.getId()) {
                        continue;
                    }
                    if (playerIdx == GSGameInfo.FIRST_PLAYER.getId()) {
                        p1Action = goofSpielAction.getValue();
                        continue;
                    }
                    if (playerIdx == GSGameInfo.SECOND_PLAYER.getId()) {
                        p2Action = goofSpielAction.getValue();
                        roundHash = ((int) Math.signum(p1Action - p2Action) + 2);
                        round++;
                        hash |= roundHash << 2 * round;
                    }
                } else {
                    if (playerIdx == GSGameInfo.NATURE.getId()) {
                        roundHash = 1; // nature moved
                    } else  {
                        assert (playerIdx == GSGameInfo.FIRST_PLAYER.getId());
                        roundHash = 2; // nature and 1st player moved
                    }
                    hash |= roundHash;
                }
            }


            psKey = new PSKey(hash);
        }
        return psKey;
    }

    @Override
    public GameState copy() {
        return new IIGoofSpielGameState(this);
    }

}
