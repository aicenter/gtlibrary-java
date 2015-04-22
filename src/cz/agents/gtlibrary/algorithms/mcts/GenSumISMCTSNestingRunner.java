package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.FrequenceDistribution;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class GenSumISMCTSNestingRunner {

    static public GameInfo gameInfo;
    static public GameState rootState;
    static public Expander<MCTSInformationSet> expander;
    static public FullSequenceEFG efg;
    static Distribution dist = new FrequenceDistribution(); //MostFrequentAction();//
    static public GenSumISMCTSAlgorithm alg;
    static Strategy strategy = new UniformStrategyForMissingSequences.Factory().create();
    static HashSet<MCTSInformationSet> processed = new HashSet();

    public static void buildStichedStrategy(Player pl, MCTSInformationSet parentIS, InnerNode curNode, int iterations) {
        MCTSInformationSet curNodeIS = curNode.getInformationSet();
        if (curNodeIS != null && curNodeIS.getPlayer().equals(pl) && !processed.contains(curNode.getInformationSet())) {
            if (parentIS != null)
                alg.setCurrentIS(parentIS);
            alg.runIterations(iterations);
            Map<Action, Double> actionDistribution = dist.getDistributionFor(curNodeIS.getAlgorithmData());
            double prefix = strategy.get(curNodeIS.getPlayersHistory());
            for (Map.Entry<Action, Double> en : actionDistribution.entrySet()) {
                Sequence sq = new ArrayListSequenceImpl(curNodeIS.getPlayersHistory());
                sq.addLast(en.getKey());
                strategy.put(sq, en.getValue() * prefix);
            }
            processed.add(curNodeIS);
        } else curNodeIS = parentIS;
        //depth cut-off
        //int depth=0;
        //for (Player p : curNode.getGameState().getAllPlayers())
        //    depth += curNode.getGameState().getHistory().getSequenceOf(p).size();
        //if (depth == 3) return;

        for (Node n : curNode.getChildren().values()) {
            if (!(n instanceof InnerNode))
                continue;
            buildStichedStrategy(pl, curNodeIS, (InnerNode) n, iterations);
        }
    }

    public static void clear() {
        processed = new HashSet();
        strategy = new UniformStrategyForMissingSequences.Factory().create();
    }

    public static Map<Sequence, Double> filterLow(Map<Sequence, Double> s) {
        for (Iterator<Map.Entry<Sequence, Double>> it = s.entrySet().iterator(); it.hasNext(); ) {
            if (it.next().getValue() < 1e-4) it.remove();
        }
        return s;
    }


    static Strategy br;

    public static double exploitability(Map<Sequence, Double> strategy) {
        Map<Sequence, Double> st = filterLow(strategy);
        //Map<Sequence, Double> st = strategy;
        SQFBestResponseAlgorithm mctsBR = new SQFBestResponseAlgorithm(
                expander,
                1 - strategy.keySet().iterator().next().getPlayer().getId(),
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);
        double val = mctsBR.calculateBR(rootState, st);
        br = mctsBR.getBRStategy();
        return -val;
    }

//    static int playerID=0;
//    public static void main(String[] args) throws Exception{
//        ArrayList<Strategy> allStrategies = new ArrayList();
//        for (int i=0;i<1;i++){
//            //setupRnd(11);
//            //setupPTTT();
//            setupIIGoofSpiel();
//            alg = new ISMCTSAlgorithm(rootState.getAllPlayers()[playerID], new DefaultSimulator(expander), fact, rootState, expander);
//            alg.runMiliseconds(100);
//            strategy = new UniformStrategyForMissingSequences();
//            strategy.put(new ArrayListSequenceImpl(rootState.getAllPlayers()[playerID]), 1.0);
//            processed.clear();
//            buildStichedStrategy(rootState.getAllPlayers()[0],alg.getRootNode().getInformationSet(), alg.getRootNode());
//            allStrategies.add(strategy);
//            System.out.print(exploitability(strategy) + " ");
//        }
//        System.out.println();
//        //Strategy mean = UniformStrategyForMissingSequences.computeMeanStrategy(allStrategies, rootState, expander);
//        System.out.println("Extracting strategy:" + (new Date()).toString());
//        Strategy mean = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[playerID], dist);
//
//        System.out.println(mean);
//        System.out.println();
//        System.out.println(mean.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
//        mean.sanityCheck(rootState, expander);
////            System.out.println("Storing strategy.");
////            try {
////                FileOutputStream file = new FileOutputStream("PTTT_ISMCTS_Strategy.ser");
////                ObjectOutputStream stream = new ObjectOutputStream(file);
////                stream.writeObject(mean);
////                stream.close();
////                file.close();
////            } catch (Exception ex){
////                ex.printStackTrace();
////            }
//        alg = null;
//        System.gc();
//
//        System.out.println(exploitability(mean));
//        System.out.println();
//        System.out.println(br.fancyToString(rootState, expander, rootState.getAllPlayers()[1]));
//        //IIGMCTSMatch.setupPTTT();
//        IIGMCTSMatch.setupIIGoofSpiel();
//        for (;;) IIGMCTSMatch.ISMCTSvsStrategy(br);
//
//        //System.out.println(br.fancyToString(rootState, expander, rootState.getAllPlayers()[1]));
//        //System.out.println(exploitability(optStrategies.get(rootState.getPlayerToMove())));
//    }
}
