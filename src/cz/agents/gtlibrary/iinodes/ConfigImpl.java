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


package cz.agents.gtlibrary.iinodes;

import java.util.HashMap;
import java.util.LinkedHashMap;

import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public abstract class ConfigImpl<I extends InformationSet> implements AlgorithmConfig<I> {

	protected HashMap<ISKey, I> allInformationSets;

	public ConfigImpl() {
		allInformationSets = new LinkedHashMap<>();
	}

	@Override
	public void addInformationSetFor(GameState gameState, I informationSet) {
		allInformationSets.put(gameState.getISKeyForPlayerToMove(), informationSet);
		informationSet.addStateToIS(gameState);
	}

	@Override
	public void addInformationSetFor(GameState gameState) {
		if(gameState.isPlayerToMoveNature())
			return;
		I informationSet = getInformationSetFor(gameState);

		if(informationSet == null)
			informationSet = createInformationSetFor(gameState);
		addInformationSetFor(gameState, informationSet);
	}

	@Override
	public I getInformationSetFor(GameState gameState) {
		return allInformationSets.get(gameState.getISKeyForPlayerToMove());
	}

	public HashMap<ISKey, I> getAllInformationSets() {
		return allInformationSets;
	}

	public Double getActualNonzeroUtilityValues(GameState leaf) {
		return null;
	}

	public void setUtility(GameState leaf, double utility) {
		//intentionally empty
	}
}
