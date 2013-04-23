package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.Map;

public interface Distribution {
	public Map<Action, Double> getDistributionFor(MCTSInformationSet infSet);
}
