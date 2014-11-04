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


package cz.agents.gtlibrary.domain.artificialchance;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.domain.artificialchance.ACAction.ActionType;
import cz.agents.gtlibrary.domain.artificialchance.ACGameState.StageOfGame;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class ACExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private static final long serialVersionUID = -8873659641112197536L;

	public ACExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		if (gameState.getPlayerToMove().equals(ACGameInfo.FIRST_PLAYER))
			return getActionsForP1((ACGameState) gameState);
		if (gameState.getPlayerToMove().equals(ACGameInfo.SECOND_PLAYER))
			return getActionsForP2((ACGameState) gameState);
		return getNatureActions((ACGameState) gameState);
	}

	private List<Action> getNatureActions(ACGameState gameState) {
		if(gameState.getStageOfGame().equals(StageOfGame.p1Deal))
			return createActions(ACGameInfo.P1_NATURE, gameState);
		else if(gameState.getStageOfGame().equals(StageOfGame.p2Deal))
			return createActions(ACGameInfo.P2_NATURE, gameState);
		else 
			return createActions(ACGameInfo.TABLE_NATURE, gameState);
	}

	private List<Action> getActionsForP2(ACGameState gameState) {
		if (gameState.getStageOfGame().equals(StageOfGame.postTableP2))
			return getP2Reaction(ACGameInfo.P2_ACTIONS_AGGR2, ACGameInfo.P2_ACTIONS_PASSIVE2, gameState);
		return getP2Reaction(ACGameInfo.P2_ACTIONS_AGGR1, ACGameInfo.P2_ACTIONS_PASSIVE1, gameState);
	}

	private List<Action> getP2Reaction(ActionType[] afterAggressive, ActionType[] afterPassive, GameState gameState) {
		if(isLastActionAggressive(gameState))
			return createActions(afterAggressive, gameState);
		return createActions(afterPassive, gameState);
	}

	private List<Action> createActions(ActionType[] actionTypes, GameState gameState) {
		List<Action> actions = new LinkedList<Action>();
		
		for (ActionType actionType : actionTypes) {
			actions.add(createAction(gameState, actionType));
		}
		return actions;
	}

	private List<Action> getActionsForP1(ACGameState gameState) {
		if (gameState.getStageOfGame().equals(StageOfGame.postTableP1))
			return createActions(ACGameInfo.P1_ACTIONS2, gameState);
		return createActions(ACGameInfo.P1_ACTIONS1, gameState);
	}

	private ACAction createAction(GameState gameState, ActionType type) {
		return new ACAction(getAlgorithmConfig().getInformationSetFor(gameState), type);
	}

	private boolean isLastActionAggressive(GameState gameState) {
		return ((ACAction) gameState.getHistory().getSequenceOf(ACGameInfo.FIRST_PLAYER).getLast()).getActionType().equals(ActionType.bet);
	}
}
