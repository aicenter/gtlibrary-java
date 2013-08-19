package cz.agents.gtlibrary.nfg.experimental.MDP;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.core.MDPBestResponse;
import cz.agents.gtlibrary.nfg.experimental.MDP.core.MDPCoreLP;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle.MDPIterativeStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPConfig;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPExpander;

import java.io.PrintStream;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/25/13
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SingleOracleCostPairedMDP {

    private MDPExpander expander;
    private MDPConfig config;
    private MDPStrategy firstPlayerStrategy;
    private MDPIterativeStrategy secondPlayerStrategy;

    private PrintStream debugOutput = System.out;
    final private static boolean DEBUG = false;
    private ThreadMXBean threadBean ;

    private double gameValue = Double.NaN;

    public static void main(String[] args) {
		runBPG();
    }

    public SingleOracleCostPairedMDP(MDPExpander expander, MDPConfig config) {
        this.expander = expander;
        this.config = config;
    }

    private static void runBPG() {
        MDPExpander expander = new BPExpander();
        MDPConfig config = new BPConfig();
        SingleOracleCostPairedMDP mdp = new SingleOracleCostPairedMDP(expander, config);
        mdp.test();
    }

    private void test() {
        debugOutput.println("Testing SO CostPaired MDP.");
        firstPlayerStrategy = new MDPStrategy(config.getAllPlayers().get(0),config,expander);
        secondPlayerStrategy = new MDPIterativeStrategy(config.getAllPlayers().get(1),config,expander);
        firstPlayerStrategy.generateCompleteStrategy();
        secondPlayerStrategy.calculateDefaultUtility(firstPlayerStrategy);

//        debugOutput.println(secondPlayerStrategy.getDefaultUtilityCache().get(secondPlayerStrategy.getRootState()));
        debugOutput.println(secondPlayerStrategy.getDefaultUtilityCache());
    }
}
