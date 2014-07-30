/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.mcts.experiments;

import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.NbSamplesProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.distribution.SumMeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.RMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelector;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class IIGConvergenceExperiment {

    static GameInfo gameInfo;
    static GameState rootState;
    static SQFBestResponseAlgorithm brAlg0;
    static SQFBestResponseAlgorithm brAlg1;
    static Expander expander;

    static long samplingTimeLimit = 1800000; // default: 30min
    static Random rnd;
    static int compTime = 1000;
    static int matches=500;

    static enum Alg {UCT, EXP3, RM, OOS};

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: IIGConvergenceExperiment {P0|P1|ROOT} {OOS|MCTS-RUCT|MCTS-UCT|MCTS-EXP3|MCTS-RM} {IIGS|RG|GP|PTTT} [domain parameters].");
            System.exit(-1);
        }

        String s = System.getProperty("tLimit");
        if (s != null) samplingTimeLimit = Long.parseLong(s);
        s = System.getProperty("SEED");
        if (s != null) rnd = new HighQualityRandom(Long.parseLong(s));
        else rnd = new HighQualityRandom();
        s = System.getProperty("COMPTIME");
        if (s != null) compTime = Integer.parseInt(s);
        s = System.getProperty("MATCHES");
        if (s != null) matches = Integer.parseInt(s);
        
        String algString = args[0];
        
        IIGConvergenceExperiment exp = new IIGConvergenceExperiment();
        exp.handleDomain(args);
        if (args[1].equals("ROOT")){
            GamePlayingAlgorithm alg = exp.initGameAndAlg(algString, 0, args[2]);
            exp.runRootExploitability(alg);
        } else {
            initializeSfConfig=true;
            int playerID = Integer.parseInt(args[1].substring(1));
            exp.runAggregatedExploitability(algString, playerID, args[2]);
        }
        
    }


    public void handleDomain(String[] args) {
        if (args[2].equalsIgnoreCase("IIGS")) {  // Goofspiel
            if (args.length != 5) {
                throw new IllegalArgumentException("Illegal domain arguments count: 2 parameters are required {SEED} {DEPTH}");
            }
            GSGameInfo.seed = new Integer(args[3]);
            int depth = new Integer(args[4]);
            GSGameInfo.depth = depth;
            GSGameInfo.regenerateCards = true;
        } else if (args[2].equalsIgnoreCase("GP")) { // Generic Poker
            if (args.length != 7) {
                throw new IllegalArgumentException("Illegal poker domain arguments count. 4 are required {MAX_RISES} {MAX_BETS} {MAX_CARD_TYPES} {MAX_CARD_OF_EACH_TYPE}");
            }
            GPGameInfo.MAX_RAISES_IN_ROW = new Integer(args[3]);
            GPGameInfo.MAX_DIFFERENT_BETS = new Integer(args[4]);
            GPGameInfo.MAX_DIFFERENT_RAISES = GPGameInfo.MAX_DIFFERENT_BETS;
            GPGameInfo.MAX_CARD_TYPES = new Integer(args[5]);
            GPGameInfo.MAX_CARD_OF_EACH_TYPE = new Integer(args[6]);
        } else if (args[2].equalsIgnoreCase("RG")) { // Random Games
            if (args.length != 9) {
                throw new IllegalArgumentException("Illegal random game domain arguments count. 7 are required {SEED} {DEPTH} {BF} {CENTER_MODIFICATION} {BINARY_UTILITY} {FIXED BF}");
            }
            RandomGameInfo.seed = new Integer(args[3]);
            RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
            RandomGameInfo.MAX_DEPTH = new Integer(args[4]);
            RandomGameInfo.MAX_BF = new Integer(args[5]);
            RandomGameInfo.MAX_CENTER_MODIFICATION = new Integer(args[6]);
            RandomGameInfo.BINARY_UTILITY = new Boolean(args[7]);
            RandomGameInfo.FIXED_SIZE_BF = new Boolean(args[8]);
        } else if (args[2].equalsIgnoreCase("PTTT")) { // Phantom TicTacToe
            if (args.length != 5) {
                throw new IllegalArgumentException("Illegal Phantom Tic-Tac-Toe domain arguments count. 2 are required {Force first move?} {Use biesed version?}");
            }
            TTTState.forceFirstMoves = new Boolean(args[3]);
            TTTState.skewed = new Boolean(args[4]);
        } else throw new IllegalArgumentException("Illegal domain: " + args[2]);
    }

    static boolean initializeSfConfig = false;
    static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
    static Expander<SequenceInformationSet> sfExpander;
    public void loadGame(String domain) {
        if (sfAlgConfig==null) sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        if (domain.equals("IIGS")) {
            gameInfo = new GSGameInfo();
            rootState = new IIGoofSpielGameState();
            expander = new GoofSpielExpander<MCTSInformationSet>(new MCTSConfig());
            if (sfExpander==null) sfExpander = new GoofSpielExpander<SequenceInformationSet>(sfAlgConfig);
        } else if (domain.equals("RG")) {
            gameInfo = new RandomGameInfo();
            rootState = new RandomGameState();
            expander = new RandomGameExpander<MCTSInformationSet>(new MCTSConfig());
            if (sfExpander==null) sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
        } else if (domain.equals("GP")) {
            gameInfo = new GPGameInfo();
            rootState = new GenericPokerGameState();
            expander = new GenericPokerExpander<MCTSInformationSet>(new MCTSConfig());
            if (sfExpander==null) sfExpander = new GenericPokerExpander<SequenceInformationSet>(sfAlgConfig);
        } else if (domain.equals("PTTT")) {
            gameInfo = new TTTInfo();
            rootState = new TTTState();
            expander = new TTTExpander<MCTSInformationSet>(new MCTSConfig());
            if (sfExpander==null) sfExpander = new TTTExpander<SequenceInformationSet>(sfAlgConfig);
        }else {
            throw new IllegalArgumentException("Incorrect game:" + domain);
        }
        if (initializeSfConfig && sfAlgConfig.getAllSequences().isEmpty()){
            FullSequenceEFG efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            efg.generateCompleteGame();
        }
    }

    public GamePlayingAlgorithm initGameAndAlg(String algString, int playerID, String domain) {
        loadGame(domain);
        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        BackPropFactory bpFactory = null;
        GamePlayingAlgorithm alg = null;
        
        if (algString.equals("OOS")) {
            Double expl = 0.6d;
            Double targ = 0.4d;
            String s = System.getProperty("EXPL");
            if (s != null) expl = new Double(s);
            s = System.getProperty("TARG");
            if (s != null) expl = new Double(s);
            alg = new OOSAlgorithm(rootState.getAllPlayers()[playerID], new OOSSimulator(expander), rootState, expander, expl, targ);
            //((OOSAlgorithm) alg).runIterations(2);
        } else {
            switch (algString) {
                case "MCTS-UCT":
                case "MCTS-RUCT":
                    if (algString.equals("MCTS-RUCT"))
                        UCTSelector.useDeterministicUCT = false;
                    else 
                        UCTSelector.useDeterministicUCT = true;
                    String cS = System.getProperty("EXPL");
                    Double c = 2d*gameInfo.getMaxUtility();
                    if (cS != null) c = new Double(cS);
                    bpFactory = new UCTBackPropFactory(c);
                    break;
                case "MCTS-EXP3":
                    cS = System.getProperty("EXPL");
                    c = 0.1d;
                    if (cS != null) c = new Double(cS);
                    bpFactory = new Exp3BackPropFactory(-gameInfo.getMaxUtility(), gameInfo.getMaxUtility(), c);
                    break;
                case "MCTS-RM":
                    cS = System.getProperty("EXPL");
                    c = 0.1d;
                    if (cS != null) c = new Double(cS);
                    bpFactory = new RMBackPropFactory(-gameInfo.getMaxUtility(), gameInfo.getMaxUtility(), c);
                    break;
            }

            alg = new ISMCTSAlgorithm(
                    rootState.getAllPlayers()[playerID],
                    new DefaultSimulator(expander),
                    bpFactory,
                    rootState, expander);
            ((ISMCTSAlgorithm) alg).returnMeanValue = false;
            //((ISMCTSAlgorithm) alg).runMiliseconds(matches)Iterations(2);
        }
        alg.runMiliseconds(100);
        return alg;
    }
    
    private void runRootExploitability(GamePlayingAlgorithm alg) {
        double secondsIteration = 0.1;
        
        Distribution dist = new MeanStratDist();
        
        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);

        Strategy strategy0 = null;
        Strategy strategy1 = null;
        System.out.print("P1BRs: ");

        double br1Val = Double.POSITIVE_INFINITY;
        double br0Val = Double.POSITIVE_INFINITY;
        double cumulativeTime = 0;

        for (int i = 0; cumulativeTime < samplingTimeLimit && (br0Val + br1Val > 0.005); i++) {
            alg.runMiliseconds((int) (secondsIteration * 1000));
            cumulativeTime += secondsIteration * 1000;

            System.out.println("Cumulative Time: " + (Math.ceil(cumulativeTime)));
            strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);

            br1Val = brAlg1.calculateBR(rootState, strategy0);
            br0Val = brAlg0.calculateBR(rootState, strategy1);

            System.out.println("Precision: " + (br0Val + br1Val));
            System.out.flush();
            secondsIteration *= 1.2;
        }
    }
    
    
    
        private static HashMap<Pair<Integer,Sequence>, Map<Action,Double>> stitchedStrategy = new HashMap();
        public static void addToStichedStrategy(Pair<Integer,Sequence> isKey, Map<Action,Double> distribution){
            double sum=0;
            for (double d : distribution.values()) sum += d;
            if (sum==0)return;
            Map<Action,Double> old = stitchedStrategy.get(isKey);
            if (old==null) {
                //all this is just to allow freeing all the searches from the memory
                GameState s = distribution.keySet().iterator().next().getInformationSet().getAllStates().iterator().next();
                List<Action> actions = sfExpander.getActions(s);
                assert actions.size()==distribution.size();
                
                Map<Action,Double> d = new FixedSizeMap(actions.size());
                for (Action a : actions) d.put(a, distribution.get(a));
                stitchedStrategy.put(new Pair<Integer,Sequence>(isKey.getLeft(),sfAlgConfig.getInformationSetFor(s).getPlayersHistory()), d);
            } else {
                for (Map.Entry<Action,Double> en : old.entrySet()){
                    en.setValue(en.getValue() + distribution.get(en.getKey()));
                }
            }
        }
        
        static boolean stitchOnlyCurrentIS = false;
        static Distribution stitchingDist = new SumMeanStratDist();
        public static void addAllToStichedStrategy(Collection<InnerNode> isNodes){
            if (isNodes.isEmpty()) return;
            Player pl = isNodes.iterator().next().getGameState().getPlayerToMove();
            HashSet<Pair> added = new HashSet();
            ArrayDeque<Node> q = new ArrayDeque(isNodes);
            while (!q.isEmpty()){
                Node n = q.removeFirst();
                if (!(n instanceof InnerNode)) continue;
                InnerNode in = (InnerNode) n;
                if (in.getChildren() == null) continue;
                if (!(n instanceof ChanceNode)){
                    MCTSInformationSet is = in.getInformationSet();
                    if (is.getAlgorithmData()==null) continue;
                    if (is.getPlayer().getId() < 2 && ((NbSamplesProvider)is.getAlgorithmData()).getNbSamples() < 20) continue;
                    if (is.getPlayer().equals(pl) &&
                            !added.contains(in.getGameState().getISKeyForPlayerToMove())){
                        Map<Action, Double> dist = stitchingDist.getDistributionFor(is.getAlgorithmData());
    //                    for (Map.Entry<Action,Double> en : dist.entrySet()){
    //                        en.setValue(en.getValue()*is.getInformationSetStats().getNbSamples());
    //                    }
                        addToStichedStrategy(in.getGameState().getISKeyForPlayerToMove(), dist);
                        added.add(in.getGameState().getISKeyForPlayerToMove());
                    }
                }
                if (!stitchOnlyCurrentIS) q.addAll(in.getChildren().values());
            }
        }
    
    private void runAggregatedExploitabilityMatch(GamePlayingAlgorithm alg, int playerID) {
        Strategy rndStrategy = new UniformStrategyForMissingSequences();
        StringBuilder moves = new StringBuilder();
        GameState curState = rootState;
        while (!curState.isGameEnd()){
            Action a;
            if (curState.isPlayerToMoveNature()){
                List<Action> actions = expander.getActions(curState);
                a=actions.get(rnd.nextInt(actions.size()));
            } else if (curState.getPlayerToMove().getId()==1-playerID){
                a = Strategy.selectAction(rndStrategy.getDistributionOfContinuationOf(curState.getSequenceForPlayerToMove(), expander.getActions(curState)), rnd);
            } else {                  
                MCTSInformationSet curIS = (MCTSInformationSet) expander.getAlgorithmConfig().getInformationSetFor(curState);
                alg.setCurrentIS(curIS);
                a = alg.runMiliseconds(compTime);
                addAllToStichedStrategy(curIS.getAllNodes());
            }
            if (a==null){
                System.out.println("Warning: playing a random move!!!");
                List<Action> actions = expander.getActions(curState);
                a=actions.get(rnd.nextInt(actions.size()));
            }
            moves.append(a + " ");
            curState = curState.performAction(a);

        }
        System.out.println("MATCH: " + moves.toString() + curState.getUtilities()[0]);
    }
    
    public static double exploitability(Map<Sequence, Double> strategy, Expander expander){
        Map<Sequence, Double> st = ISMCTSExploitability.filterLow(strategy);
        SQFBestResponseAlgorithm mctsBR = new SQFBestResponseAlgorithm(
                expander,
                1-strategy.keySet().iterator().next().getPlayer().getId(),
                new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] },
                (ConfigImpl)expander.getAlgorithmConfig(), gameInfo);
        return mctsBR.calculateBR(rootState, st);
    }
    
    private void runAggregatedExploitability(String algString, int playerID, String domain){        
        for (int r=0; r<matches/50;r++){
            for (int i=0; i<50;i++){
                GamePlayingAlgorithm alg = initGameAndAlg(algString, playerID, domain);
                runAggregatedExploitabilityMatch(alg, playerID);
            }
            System.out.println("Number of IS in the stiched strategy: " + stitchedStrategy.size());
            Strategy s = UniformStrategyForMissingSequences.fromBehavioralStrategy(stitchedStrategy, rootState, sfExpander);
            System.out.println("Stiched strategy exploitability:" + exploitability(s, sfExpander));
        }
        
    }
}