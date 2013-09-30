package cz.agents.gtlibrary.nfg.core;

import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.PureStrategy;

public interface ZeroSumGameNESolver<T extends PureStrategy, U extends PureStrategy> {

    public void computeNashEquilibrium();

    public MixedStrategy<T> getPlayerOneStrategy();

    public MixedStrategy<U> getPlayerTwoStrategy();

    public double getGameValue();

    public void addPlayerOneStrategies(PlayerStrategySet<T> strategySet);

    public void addPlayerTwoStrategies(PlayerStrategySet<U> strategySet);

    public void writeProb(String filename);

    public void clearModel();
}
