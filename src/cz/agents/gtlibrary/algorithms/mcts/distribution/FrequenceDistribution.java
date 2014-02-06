package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.io.GambitEFG;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class FrequenceDistribution implements Distribution {
        double remGamma=0;
    
        public FrequenceDistribution() {
        }

        public FrequenceDistribution(double gammaToRemove) {
            this.remGamma = gammaToRemove;
        }

	@Override
	public Map<Action, Double> getDistributionFor(MCTSInformationSet infSet) {
                Map<Action, BasicStats> informationSetStats = infSet.getActionStats();
		long sum = getSumOfVisits(informationSetStats);
                int toRemove = (int)(sum * remGamma);
                sum -= toRemove;
                toRemove /= informationSetStats.size(); 
                        
		Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(informationSetStats.size());
		
		for (Entry<Action, BasicStats> entry : informationSetStats.entrySet()) {
			distribution.put(entry.getKey(), Math.max(0.0,entry.getValue().getNbSamples()-toRemove)/sum);
		}
		return distribution;
	}

	private long getSumOfVisits(Map<Action, BasicStats> actionStats) {
		long sum = 0;

		for (BasicStats backPropagationStrategy : actionStats.values()) {
			sum += backPropagationStrategy.getNbSamples();
		}
		return sum;
	}

}
