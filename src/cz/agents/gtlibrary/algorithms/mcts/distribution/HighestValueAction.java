package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTMAXSelectionStrategy;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class HighestValueAction implements Distribution {

	@Override
	public Map<Action, Double> getDistributionFor(MCTSInformationSet infSet) {
            Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(infSet.getActionStats().size());
            Action out = infSet.getActionStats().keySet().iterator().next();
            double max = -Double.MAX_VALUE;
            for (Map.Entry<Action, BasicStats> en : infSet.getActionStats().entrySet()) {
                if (max < en.getValue().getEV()) {
                    max = en.getValue().getEV();
                    out = en.getKey();
                }
            }

            distribution.put(out, 1d);
            return distribution;
    }
}
