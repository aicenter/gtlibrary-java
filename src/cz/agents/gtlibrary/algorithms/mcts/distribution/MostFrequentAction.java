package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import java.util.List;
import java.util.Map;

public class MostFrequentAction implements Distribution {

        @Override
        public Map<Action, Double> getDistributionFor(AlgorithmData data) {
                ActionFrequencyProvider afp = (ActionFrequencyProvider) data;
                Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(afp.getActions().size());
		int best = 0;
                List<Action> actions = afp.getActions();
                double[] freqs = afp.getActionFreq();
		
                int i=0;
		for (Action a : actions) {
			if(freqs[i] > freqs[best]) best=i;
			distribution.put(a, 0d);
                        i++;
		}
		distribution.put(actions.get(best), 1d);
		return distribution;
        }

            
}
