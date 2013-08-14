package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.Collection;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.Pair;

public interface SimOracle {

	public Pair<ActionPureStrategy, Double> getBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta);

	public ActionPureStrategy getFirstStrategy();
	
	public Collection<ActionPureStrategy> getActions();
}
