package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.simpleGeneralSum.SimpleGSExpander;
import cz.agents.gtlibrary.domain.simpleGeneralSum.SimpleGSInfo;
import cz.agents.gtlibrary.domain.simpleGeneralSum.SimpleGSState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.io.GambitEFG;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 12/2/13
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class StackelbergMultipleLP {

    private GameState rootState;
    private Expander<SequenceInformationSet> expander;
    private GameInfo gameConfig;
    private StackelbergConfig<SequenceInformationSet> algConfig;

    private PrintStream debugOutput = System.out;
    final private static boolean DEBUG = false;
    private ThreadMXBean threadBean;

    private double gameValue = Double.NaN;

    public StackelbergMultipleLP(GameState rootState, Expander<SequenceInformationSet> expander, GameInfo gameInfo, StackelbergConfig<SequenceInformationSet> algConfig) {
        this.rootState = rootState;
        this.expander = expander;
        this.gameConfig = gameInfo;
        this.algConfig = algConfig;
    }

    public static void main(String[] args) {
//        runBPG();
        runSGSG();
    }


    public static void runBPG() {
        GameState rootState = new BPGGameState();
        BPGGameInfo gameInfo = new BPGGameInfo();
        StackelbergConfig<SequenceInformationSet> algConfig = new StackelbergConfig<SequenceInformationSet>(rootState);
        StackelbergMultipleLP smlp = new StackelbergMultipleLP(rootState, new BPGExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);
        smlp.generate();
    }

    public static void runSGSG() {
        SimpleGSInfo gameInfo = new SimpleGSInfo();
        GameState rootState = new SimpleGSState(gameInfo.getAllPlayers());
        StackelbergConfig<SequenceInformationSet> algConfig = new StackelbergConfig<SequenceInformationSet>(rootState);
        Expander expander = new SimpleGSExpander(algConfig);

        StackelbergMultipleLP smlp = new StackelbergMultipleLP(rootState, expander, gameInfo, algConfig);
        smlp.generate();
        GambitEFG.write("simpleGSG.gbt", rootState, expander);
    }

    private Map<Player, Map<Sequence, Double>> generate() {
        debugOutput.println("Full Sequence Multiple LP Stackelberg");
        debugOutput.println(gameConfig.getInfo());
        threadBean = ManagementFactory.getThreadMXBean();

        long start = threadBean.getCurrentThreadCpuTime();
        long overallSequenceGeneration = 0;
        long overallCPLEX = 0;
        Map<Player, Map<Sequence, Double>> realizationPlans = new HashMap<Player, Map<Sequence, Double>>();
        long startGeneration = threadBean.getCurrentThreadCpuTime();

        generateCompleteGame();
        System.out.println("Game tree built...");
        System.out.println("Information set count: " + algConfig.getAllInformationSets().size());
        overallSequenceGeneration = (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;

        Player[] actingPlayers = new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] };
        long startCPLEX = threadBean.getCurrentThreadCpuTime();
        StackelbergSequenceFormLP sequenceFormLP = new StackelbergSequenceFormLP(actingPlayers);

//        Iterator i = algConfig.getIterator(rootState.getAllPlayers()[0], expander);
//        while (i.hasNext()) {
//            System.out.println(i.next());
//        }

        sequenceFormLP.calculateLeaderStrategies(0,1,algConfig,expander);

        long thisCPLEX = (threadBean.getCurrentThreadCpuTime() - startCPLEX) / 1000000l;

        overallCPLEX += thisCPLEX;

        for (Player player : rootState.getAllPlayers()) {
            realizationPlans.put(player, sequenceFormLP.getResultStrategiesForPlayer(player));
        }

        System.out.println("done.");
        long finishTime = (threadBean.getCurrentThreadCpuTime() - start) / 1000000l;

        int[] support_size = new int[] { 0, 0 };
        for (Player player : actingPlayers) {
            for (Sequence sequence : realizationPlans.get(player).keySet()) {
                if (realizationPlans.get(player).get(sequence) > 0) {
                    support_size[player.getId()]++;
                    if (DEBUG)
                        System.out.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
                }
            }
        }

        try {
            Runtime.getRuntime().gc();
            Thread.sleep(500l);
        } catch (InterruptedException e) {
        }

        gameValue = sequenceFormLP.getResultForPlayer(actingPlayers[0]);
        System.out.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());
        System.out.println("final support_size: FirstPlayer: " + support_size[0] + " \t SecondPlayer: " + support_size[1]);
        System.out.println("final result:" + gameValue);
        System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        System.out.println("final time: " + finishTime);
        System.out.println("final CPLEX time: " + overallCPLEX);
        System.out.println("final BR time: " + 0);
        System.out.println("final RGB time: " + 0);
        System.out.println("final StrategyGenerating time: " + overallSequenceGeneration);

        if (DEBUG) {
            // sanity check -> calculation of Full BR on the solution of SQF LP
            SQFBestResponseAlgorithm brAlg = new SQFBestResponseAlgorithm(expander, 0, actingPlayers, algConfig, gameConfig);
            System.out.println("BR: " + brAlg.calculateBR(rootState, realizationPlans.get(actingPlayers[1])));

            SQFBestResponseAlgorithm brAlg2 = new SQFBestResponseAlgorithm(expander, 1, actingPlayers, algConfig, gameConfig);
            System.out.println("BR: " + brAlg2.calculateBR(rootState, realizationPlans.get(actingPlayers[0])));

            algConfig.validateGameStructure(rootState, expander);
        }
        return realizationPlans;
    }

    public void generateCompleteGame() {
        LinkedList<GameState> queue = new LinkedList<GameState>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

            algConfig.addStateToSequenceForm(currentState);
            if (currentState.isGameEnd()) {
                algConfig.setUtility(currentState);
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
    }

    public double getGameValue() {
        return gameValue;
    }
}
