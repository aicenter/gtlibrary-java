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


import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.util.LinkedList;
import java.util.List;

public class RandomGameExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private static final long serialVersionUID = -4773226796724312872L;
	
	private long firstSeed;

    public RandomGameExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
        RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
        firstSeed = RandomGameInfo.rnd.nextLong();
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        RandomGameState gsState = (RandomGameState) gameState;
        InformationSet informationSet = getAlgorithmConfig().getInformationSetFor(gameState);
        List<Action> actions = new LinkedList<Action>();

        String newVal = firstSeed + "";
        if (gsState.getHistory().getSequenceOf(gameState.getPlayerToMove()).size() > 0)
            newVal = ((RandomGameAction)gsState.getHistory().getSequenceOf(gameState.getPlayerToMove()).getLast()).getValue();

        int MOVES = RandomGameInfo.MAX_BF;
        if (!RandomGameInfo.FIXED_SIZE_BF) {
            MOVES = new HighQualityRandom(firstSeed+((PerfectRecallISKey)gsState.getISKeyForPlayerToMove()).getHash()).nextInt(RandomGameInfo.MAX_BF-1)+2;
        }
        for (int i=0; i<MOVES; i++) {
            RandomGameAction action = new RandomGameAction(informationSet, newVal + "_" + i, i);
            actions.add(action);
        }

        return actions;
    }
}
