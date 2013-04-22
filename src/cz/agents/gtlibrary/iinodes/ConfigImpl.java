package cz.agents.gtlibrary.iinodes;

import java.util.HashMap;
import java.util.LinkedHashMap;

import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public abstract class ConfigImpl<I extends InformationSetImpl> implements AlgorithmConfig<I> {

	private HashMap<Pair<Integer, Sequence>, I> allInformationSets;

	public ConfigImpl() {
		allInformationSets = new LinkedHashMap<Pair<Integer, Sequence>, I>();
	}

	@Override
	public void addInformationSetFor(GameState gameState, I informationSet) {
		allInformationSets.put(gameState.getISKeyForPlayerToMove(), informationSet);
		informationSet.addStateToIS(gameState);
	}

        @Override
	public I getInformationSetFor(GameState gameState) {
		return allInformationSets.get(gameState.getISKeyForPlayerToMove());
	}
	
	public HashMap<Pair<Integer, Sequence>, I> getAllInformationSets() {
		return allInformationSets;
	}

       public Double getActualNonzeroUtilityValues(GameState leaf) {
          return null;
      }

      public void setUtility(GameState leaf, double utility) {
         //intentionally empty
      }
}
