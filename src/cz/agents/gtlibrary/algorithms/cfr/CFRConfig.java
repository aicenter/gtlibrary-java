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
