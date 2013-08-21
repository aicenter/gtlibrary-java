package cz.agents.gtlibrary.nfg.experimental.MDP;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.core.MDPBestResponse;
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
        long startTime = System.nanoTime();
        debugOutput.println("Testing Full CostPaired MDP.");
        firstPlayerStrategy = new MDPStrategy(config.getAllPlayers().get(0),config,expander);
        secondPlayerStrategy = new MDPStrategy(config.getAllPlayers().get(1),config,expander);
        firstPlayerStrategy.generateCompleteStrategy();

//        for (MDPState s : firstPlayerStrategy.getStates()) {
//            debugOutput.println(s.toString() + ":" + s.hashCode());
//            if (!s.isRoot()) debugOutput.println("Predecessors:" + firstPlayerStrategy.getPredecessors(s));
//        } //*/
        secondPlayerStrategy.generateCompleteStrategy();

/*        for (MDPState s : secondPlayerStrategy.getStates()) {
            debugOutput.println(s.toString() + ":" + s.hashCode());
            if (!s.isRoot()) debugOutput.println("Predecessors:" + secondPlayerStrategy.getPredecessors(s));
        }

/*        for (MDPStateActionMarginal m1 : firstPlayerStrategy.getStrategy().keySet()) {
            for (MDPStateActionMarginal m2 : secondPlayerStrategy.getStrategy().keySet()) {
                double utility = config.getUtility(m1, m2);
                if (utility != 0)
                   debugOutput.println("["+m1+","+m2+"] = " + utility);
            }
        } //*/

//        debugOutput.println(secondPlayerStrategy.getSuccessors(new MDPStateActionMarginal(secondPlayerStrategy.getRootState(), secondPlayerStrategy.getActions(secondPlayerStrategy.getRootState()).get(0))));

        firstPlayerStrategy.storeAllUtilityToCache(firstPlayerStrategy.getAllActionStates(), secondPlayerStrategy.getAllActionStates());

        Map<Player, MDPStrategy> playerStrategy = new HashMap<Player, MDPStrategy>();
        playerStrategy.put(config.getAllPlayers().get(0), firstPlayerStrategy);
        playerStrategy.put(config.getAllPlayers().get(1), secondPlayerStrategy);

        MDPCoreLP lp = new MDPCoreLP(config.getAllPlayers(), playerStrategy, config);
        double r1 = lp.solveForPlayer(config.getAllPlayers().get(0));
        debugOutput.println("Result: " + r1);

        double r2 = lp.solveForPlayer(config.getAllPlayers().get(1));
        debugOutput.println("Result: " + r2);

        lp.extractStrategyForPlayer(config.getAllPlayers().get(0));
//        for (MDPStateActionMarginal m1 : firstPlayerStrategy.getStrategy().keySet()) {
//            debugOutput.println(m1 + " = " + firstPlayerStrategy.getStrategy().get(m1));
//        }

        lp.extractStrategyForPlayer(config.getAllPlayers().get(1));
//        for (MDPStateActionMarginal m2 : secondPlayerStrategy.getStrategy().keySet()) {
//            debugOutput.println(m2 + " = " + secondPlayerStrategy.getStrategy().get(m2));
//        }

//        firstPlayerStrategy.sanityCheck();
//        secondPlayerStrategy.sanityCheck();
//
//        MDPBestResponse br1 = new MDPBestResponse(config, config.getAllPlayers().get(0));
//        debugOutput.println("BR : " + br1.calculateBR(firstPlayerStrategy,  secondPlayerStrategy));
//        debugOutput.println(br1.extractBestResponse(firstPlayerStrategy));
//
//        MDPBestResponse br2 = new MDPBestResponse(config, config.getAllPlayers().get(1));
//        debugOutput.println("BR : " + br2.calculateBR(secondPlayerStrategy, firstPlayerStrategy));
//        debugOutput.println(br2.extractBestResponse(secondPlayerStrategy));

//*/

        long endTime = System.nanoTime() - startTime;
        debugOutput.println("Overall Time: " + (endTime / 1000000));
    }
}
