package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Result;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.Map;

public interface DOCache {

    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, Result[] strategy);

    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, Result[] strategy);

    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, Result p1Result, Result p2Result);

    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, Result p1Result, Result p2Result);

    public Result[] getStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet);

    public Result[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3);

	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double utilityValue);
	
	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double utility);

	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double bound, Double pesimisticUtilityFromCache);

	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double optimisticUtility, Double pesimisticUtility);

	public Double getPesimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2);

	public Double getUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair);
	
	public Double getUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2);

	public Double getOptimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair);

	public Double getPesimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair);

	public Double getOptimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2);

    public void setTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet, Player player, Result result);

    public Result getP1TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet);

    public Result getP2TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet);

    public Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, Result[]> getStrategies();

    public Result[] getTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet);
}
