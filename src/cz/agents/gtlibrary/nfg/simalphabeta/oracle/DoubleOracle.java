package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import ilog.cplex.IloCplex.Algorithm;
import cz.agents.gtlibrary.nfg.NegativeUtility;
import cz.agents.gtlibrary.nfg.PureStrategy;
import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;


/**
 * A template for a classic double oracle algorithm.
 *
 * @param <T>: type of strategies of the first player - evader
 * @param <U>: type of strategies of the second player - patroller
 */
public class DoubleOracle<T extends PureStrategy, U extends PureStrategy> extends Algorithm {

    protected static final double EPS = 0.000001;
    protected SimABOracleImpl firstPlayerOracle;
    protected SimABOracleImpl secondPlayerOracle;
    protected Utility<T, U> utilityEvader;
    protected Utility<U, T> utilityPatroller;


    protected static final boolean CHECK_STRATEGY_SET_CHANGES = true;
    protected ZeroSumGameNESolverImpl<T, U> NEsolver;

    public DoubleOracle(/*PlayerGraph graph,*/ SimABOracleImpl evaderOracle, SimABOracleImpl patrollerOracle, Utility<T, U> utility) {
        //super(graph);
        this.firstPlayerOracle = evaderOracle;
        this.secondPlayerOracle = patrollerOracle;
        this.utilityEvader = utility;
        this.utilityPatroller = new NegativeUtility<U, T>(utilityEvader);

        NEsolver = new ZeroSumGameNESolverImpl<T, U>(utility);
    }

    public void execute() {


    }

}
