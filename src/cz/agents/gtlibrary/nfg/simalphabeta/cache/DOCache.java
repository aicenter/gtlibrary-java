package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.Map;

public interface DOCache {

    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, MixedStrategy<ActionPureStrategy>[] strategy);

    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, MixedStrategy<ActionPureStrategy>[] strategy);

    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, MixedStrategy<ActionPureStrategy> p1Strategy, MixedStrategy<ActionPureStrategy> p2Strategy);

    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, MixedStrategy<ActionPureStrategy> p1strategy, MixedStrategy<ActionPureStrategy> p2strategy);

    public MixedStrategy<ActionPureStrategy>[] getStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet);

    public MixedStrategy<ActionPureStrategy>[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3);

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

    public void setTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet, Player player, MixedStrategy<ActionPureStrategy> strategiesFromAlphaBeta);

    public MixedStrategy<ActionPureStrategy> getP1TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet);

    public MixedStrategy<ActionPureStrategy> getP2TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet);

    public Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>[]> getStrategies();

    public MixedStrategy<ActionPureStrategy>[] getTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet);
}
