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
                final Action maxAction = ((UCTMAXSelectionStrategy)infSet.selectionStrategy).getMaxValueAction();
		distribution.put(maxAction, 1d);
		return distribution;
	}

}
