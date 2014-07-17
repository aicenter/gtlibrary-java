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

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class ACAction extends ActionImpl {

	public enum ActionType {
		bet, check, call, fold, J, Q, K, ;
	}

	private static final long serialVersionUID = 3297749964173254484L;

	private ActionType actionType;
	private int hashCode = -1;

	public ACAction(InformationSet informationSet, ActionType actionType) {
		super(informationSet);
		this.actionType = actionType;
	}

	public ActionType getActionType() {
		return actionType;
	}

	@Override
	public void perform(GameState gameState) {
		ACGameState acState = (ACGameState) gameState;

		acState.addActionToSequence(this);
		switch (actionType) {
		case bet:
			acState.bet();
			break;
		case check:
			acState.check();
			break;
		case call:
			acState.call();
			break;
		case fold:
			acState.fold();
			break;
		default:
			acState.dealCard(actionType);
			break;
		}
	}

	@Override
	public int hashCode() {
		if (hashCode == -1) {
			final int prime = 31;
			
			hashCode = 1;
			hashCode = prime * hashCode + ((actionType == null) ? 0 : actionType.hashCode());
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ACAction other = (ACAction) obj;
		if (actionType != other.actionType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return actionType.toString();
	}
}
