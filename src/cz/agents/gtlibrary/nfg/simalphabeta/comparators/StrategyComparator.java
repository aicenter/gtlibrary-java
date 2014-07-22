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


package cz.agents.gtlibrary.nfg.simalphabeta.comparators;

import java.util.Comparator;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;

public abstract class StrategyComparator implements Comparator<ActionPureStrategy> {
	
	@Override
	public int compare(ActionPureStrategy s1, ActionPureStrategy s2) {
		int result = Double.compare(getValue(s2), getValue(s1));
        if (result == 0)
            result = Integer.compare(s1.hashCode(), s2.hashCode());
        return result;
	}

	protected abstract double getValue(ActionPureStrategy strategy);

}
