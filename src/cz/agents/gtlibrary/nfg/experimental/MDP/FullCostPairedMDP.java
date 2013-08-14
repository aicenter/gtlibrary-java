package cz.agents.gtlibrary.nfg.experimental.MDP;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.core.MDPCoreLP;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPConfig;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPExpander;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPState;

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
public class FullCostPairedMDP {

    private MDPExpander expander;
    private MDPConfig config;
    private MDPStrategy firstPlayerStrategy;
    private MDPStrategy secondPlayerStrategy;

    private PrintStream debugOutput = System.out;
    final private static boolean DEBUG = false;
    private ThreadMXBean threadBean ;

    private double gameValue = Double.NaN;

    public static void main(String[] args) {
		runBPG();
    }

    public FullCostPairedMDP(MDPExpander expander, MDPConfig config) {
        this.expander = expander;
        this.config = config;
    }

    private static void runBPG() {
//        MDPState rootState = new BPState();
        MDPExpander expander = new BPExpander();
        MDPConfig config = new BPConfig();
        FullCostPairedMDP mdp = new FullCostPairedMDP(expander, config);
        mdp.test();
    }

    private void test() {
        debugOutput.println("Testing Full CostPaired MDP.");
        firstPlayerStrategy = new MDPStrategy(config.getAllPlayers().get(0),config,expander);
        secondPlayerStrategy = new MDPStrategy(config.getAllPlayers().get(1),config,expander);
        firstPlayerStrategy.generateCompleteStrategy();
        /*
        for (MDPState s : firstPlayerStrategy.getFrequency().keySet()) {
            debugOutput.println(s.toString() + ":" + s.hashCode());
//            if (!s.isRoot()) debugOutput.println("Predecessors:" + firstPlayerStrategy.getPredecessors(s));
        } //*/
        secondPlayerStrategy.generateCompleteStrategy();

/*        for (MDPState s : secondPlayerStrategy.getFrequency().keySet()) {
            debugOutput.println(s.toString() + ":" + s.hashCode());
            if (!s.isRoot()) debugOutput.println("Predecessors:" + secondPlayerStrategy.getPredecessors(s));
        }

/*        for (MDPStateActionMarginal m1 : firstPlayerStrategy.getStrategy().keySet()) {
            for (MDPStateActionMarginal m2 : secondPlayerStrategy.getStrategy().keySet()) {
                double utility = config.getUtility(m1, m2);
                if (utility > 0)
                   debugOutput.println("["+m1+","+m2+"] = " + utility);
            }
        } //*/

        Map<Player, MDPStrategy> playerStrategy = new HashMap<Player, MDPStrategy>();
        playerStrategy.put(config.getAllPlayers().get(0), firstPlayerStrategy);
        playerStrategy.put(config.getAllPlayers().get(1), secondPlayerStrategy);

        MDPCoreLP lp = new MDPCoreLP(config.getAllPlayers(), playerStrategy, config);
        double r1 = lp.solveForPlayer(config.getAllPlayers().get(0));
        debugOutput.println("Result: " + r1);

//        double r2 = lp.solveForPlayer(config.getAllPlayers().get(1));
//        debugOutput.println("Result: " + r2);
//*/
    }
}
