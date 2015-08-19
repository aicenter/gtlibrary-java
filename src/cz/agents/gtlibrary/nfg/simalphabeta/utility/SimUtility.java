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


package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;

public abstract class SimUtility extends Utility<ActionPureStrategy, ActionPureStrategy> {

    public abstract DOCache getUtilityCache();

	public abstract double getUtility(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta);
	
	public abstract double getUtility(ActionPureStrategy s1, ActionPureStrategy s2);

	public abstract double getUtilityForIncreasedBounds(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta);	
}
