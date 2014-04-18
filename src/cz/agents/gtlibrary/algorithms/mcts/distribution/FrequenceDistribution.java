package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import java.util.List;
import java.util.Map;

public class FrequenceDistribution implements Distribution {
        double remGamma=0;
    
        public FrequenceDistribution() {
        }

        public FrequenceDistribution(double gammaToRemove) {
            this.remGamma = gammaToRemove;
        }

        @Override
        public Map<Action, Double> getDistributionFor(AlgorithmData data) {
                ActionFrequencyProvider afp = (ActionFrequencyProvider) data;
                Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(afp.getActions().size());
                List<Action> actions = afp.getActions();
                double[] freqs = afp.getActionFreq();
                
                double willRemove = 0;
                for (double d : freqs) willRemove += Math.min(d,remGamma);
		
                int i=0;
		for (Action a : actions) distribution.put(a, freqs[i++]*1/(1-willRemove));
		return distribution;
        }

}
