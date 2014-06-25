package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import java.util.List;

public class SumFrequenceDistribution implements Distribution {

        @Override
        public Map<Action, Double> getDistributionFor(AlgorithmData data) {
                ActionFrequencyProvider afp = (ActionFrequencyProvider) data;
                Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(afp.getActions().size());
                List<Action> actions = afp.getActions();
                double[] freqs = afp.getActionFreq();
                int i=0;
		for (Action a : actions) distribution.put(a, freqs[i++]*((NbSamplesProvider)data).getNbSamples());
		return distribution;
        }
}
