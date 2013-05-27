package cz.agents.gtlibrary.nfg;

/**
 * Interface for solving zero-sum game with two diferent strategy types, T and U.
 *
 * @param <T> Strategy type of the first player.
 * @param <U> Strategy type of the second player.
 */
public interface IZeroSumGameNESolver<T extends PureStrategy, U extends PureStrategy> {

    public void computeNashEquilibrium();

    public MixedStrategy<T> getPlayerOneStrategy();

    public MixedStrategy<U> getPlayerTwoStrategy();

    public double getGameValue();

    public void setPlayerOneStrategies(PlayerStrategySet<T> strategySet);

    public void setPlayerTwoStrategies(PlayerStrategySet<U> strategySet);

    public void writeProb(String filename);

}
