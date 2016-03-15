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


package cz.agents.gtlibrary.algorithms.cfr;

import java.util.HashMap;
import java.util.Map;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;

/**
 * This class will be removed, the implementation of CFR and OOS is obsolete.
 * Use cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm/CFRISAlgorithm instead.
 */
@Deprecated
public abstract class CFRConfig<I extends CFRInformationSet> extends ConfigImpl<I> {
	private GameState rootState;
	private int iterations;
	private Map<History, float[]> terminalStates;
	
	public CFRConfig(GameState rootState) {
		this.rootState = rootState;
		iterations = 0;
		terminalStates = new HashMap<History, float[]>();
	}
	
	public GameState getRootState() {
		return rootState;
	}
	
	public int getIterations() {
		return iterations;
	}
	
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}
	
	public void incrementIterations() {
		iterations++;
	}
	
	public float[] getUtilityFor(History history) {
		return terminalStates.get(history);
	}
	
	public void setUtilityFor(History history, double[] utility) {
		float[] fUtility = new float[utility.length];
		
		for (int i = 0; i < fUtility.length; i++) {
			fUtility[i] = (float) utility[i];
		}
		terminalStates.put(history, fUtility);
	}
}
