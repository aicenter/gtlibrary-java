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


package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMRMSelector;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class MCTSConfig extends ConfigImpl<MCTSInformationSet> {

    private Random random;

    public MCTSConfig() {
        this.random = new HighQualityRandom();
    }

    public MCTSConfig(Random random) {
        this.random = random;
    }

    @Override
    public MCTSInformationSet createInformationSetFor(GameState gameState) {
        return new MCTSInformationSet(gameState);
    }

    @Override
    public MCTSInformationSet getInformationSetFor(GameState gameState) {
        if(gameState.isPlayerToMoveNature())
            return null;
        MCTSInformationSet infoSet = super.getInformationSetFor(gameState);

        if (infoSet == null) {
            infoSet = new MCTSInformationSet(gameState);
        }
        return infoSet;
    }

    /**
     * This method assumes setting where the only uncertainty is caused by simultaneous moves
     *
     * @param p1Action
     * @param p1ActionPosition
     * @param p2Action
     * @param p2ActionPosition
     */
    public void cleanSetsNotContaining(Action p1Action, int p1ActionPosition, Action p2Action, int p2ActionPosition) {
        Iterator<Map.Entry<ISKey, MCTSInformationSet>> iterator = allInformationSets.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ISKey, MCTSInformationSet> entry = iterator.next();
            MCTSInformationSet is = entry.getValue();
            GameState state = is.getAllNodes().iterator().next().getGameState();


            if (isDirectSuccesor(p1Action, p1ActionPosition, p2Action, p2ActionPosition, state)) {
                AlgorithmData data = is.getAlgorithmData();

                if (data instanceof SMRMSelector)
                    ((SMRMSelector) data).setP1Actions(null);
                is.getAllNodes().clear();
                is.setAlgorithmData(null);
                iterator.remove();
            }
        }
    }

    private boolean isDirectSuccesor(Action p1Action, int p1ActionPosition, Action p2Action, int p2ActionPosition, GameState state) {
        return !containsAction(p1Action, p1ActionPosition, state.getSequenceFor(state.getAllPlayers()[0])) ||
                !containsAction(p2Action, p2ActionPosition, state.getSequenceFor(state.getAllPlayers()[1]));
    }

    private boolean containsAction(Action action, int actionPosition, Sequence sequence) {
        if (actionPosition == -1)
            return true;
        return sequence.size() - 1 >= actionPosition && sequence.get(actionPosition).equals(action);
    }

    public Random getRandom() {
        return random;
    }
}
