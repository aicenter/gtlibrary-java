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


package cz.agents.gtlibrary.domain.nfptest;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class TestExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private static final long serialVersionUID = 6455853080379013793L;

	public TestExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		if (gameState.getPlayerToMove().equals(gameState.getAllPlayers()[0]))
			return getActionsForP1(gameState);
		return getActionsForP2(gameState);
	}

	private List<Action> getActionsForP2(GameState gameState) {
		List<Action> actions = new ArrayList<Action>(2);
		
		actions.add(new P2TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "u"));
		actions.add(new P2TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "d"));
		return actions;
	}

	private List<Action> getActionsForP1(GameState gameState) {
		TestGameState tState = (TestGameState) gameState;

		if (tState.getLastActionOfP2() == null)
			return getFirstP1Actions(gameState);
		if (tState.getLastActionOfP2().getActionType().equals("u"))
			return getActionsAfterU(gameState);
		return getActionsAfterD(gameState);
	}

	private List<Action> getActionsAfterD(GameState gameState) {
		List<Action> actions = new ArrayList<Action>(2);

		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "L'"));
		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "R'"));
		return actions;
	}

	private List<Action> getActionsAfterU(GameState gameState) {
		List<Action> actions = new ArrayList<Action>(2);

		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "L"));
		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "R"));
		return actions;
	}

	private List<Action> getFirstP1Actions(GameState gameState) {
		List<Action> actions = new ArrayList<Action>(2);

		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "U"));
		actions.add(new P1TestAction(getAlgorithmConfig().getInformationSetFor(gameState), "D"));
		return actions;
	}

}
