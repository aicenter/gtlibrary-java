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


package cz.agents.gtlibrary.domain.antiMCTS;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import java.util.ArrayList;
import java.util.List;

public class AntiMCTSExpander<I extends cz.agents.gtlibrary.interfaces.InformationSet>  extends ExpanderImpl<I> {
    
	public AntiMCTSExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }
    
     @Override
     public List<Action> getActions(GameState gameState) {
		AntiMCTSState s = (AntiMCTSState) gameState;
                InformationSet curIS = getAlgorithmConfig().getInformationSetFor(gameState);
		ArrayList<Action> actions = new ArrayList<Action>();
                actions.add(new AntiMCTSAction(false, curIS));
                actions.add(new AntiMCTSAction(true, curIS));
		return actions;
	}
}
