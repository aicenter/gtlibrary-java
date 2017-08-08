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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSSimulator;
import cz.agents.gtlibrary.algorithms.mcts.distribution.*;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.*;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;

import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author vilo
 */
public class IIGMCTSMatch {
        private static Random rnd = new HighQualityRandom();
        private static int MCTS_MILISECONDS_PER_CALL = (int)10000;
	private static PrintStream out = System.out;
    
        
        static GameInfo gameInfo;
        static GameState rootState;
        
        public static void setupPTTT(){
            gameInfo = new TTTInfo();
            rootState = new TTTState();
        }
        
        
        public static void setupIIGoofSpiel(){
            gameInfo = new GSGameInfo();
            rootState = new IIGoofSpielGameState();
        }
        
        static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
        static Expander<SequenceInformationSet> sfExpander;
        //static Strategy[] rationalStrategy = new Strategy[2];
        public static void setupIIGoofSpielExpl(){
            setupIIGoofSpiel();
            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            sfExpander = new GoofSpielExpander<SequenceInformationSet>(sfAlgConfig);
            FullSequenceEFG efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            efg.generateCompleteGame();
            //rationalStrategy[0] = new StrategyImpl(res.get(rootState.getAllPlayers()[0]));
            //rationalStrategy[1] = new StrategyImpl(res.get(rootState.getAllPlayers()[1]));
            //Map<Sequence, Double> bothRPs = res.get(rootState.getAllPlayers()[0]);
            //bothRPs.putAll(res.get(rootState.getAllPlayers()[1]));
            //svCalc = new StateValueCalculator(gameInfo, rootState, sfExpander, bothRPs);
        }

         public static void setupRnd(long seed){
            if (seed == RandomGameInfo.seed && rootState != null) return;
            RandomGameInfo.seed = seed;
            gameInfo = new RandomGameInfo();
            rootState = new RandomGameState();
            
        }

        public static void runOOSvsISMCTS() throws Exception {
            Expander<MCTSInformationSet> expanderOOS = new GoofSpielExpander<MCTSInformationSet> (new MCTSConfig());
            expanderOOS.getAlgorithmConfig().createInformationSetFor(rootState);
            OOSAlgorithm algOOS = new OOSAlgorithm(
                    rootState.getAllPlayers()[playerID],
                    new OOSSimulator(expanderOOS),
                    rootState, expanderOOS, delta, 0.6);
            algOOS.runMiliseconds(100);
            
            Expander<MCTSInformationSet> expanderMCTS = new GoofSpielExpander<MCTSInformationSet> (new MCTSConfig());
            expanderMCTS.getAlgorithmConfig().createInformationSetFor(rootState);
            ISMCTSAlgorithm algMCTS = new ISMCTSAlgorithm(
                    rootState.getAllPlayers()[1-playerID],
                    new DefaultSimulator(expanderMCTS),
                    new UCTBackPropFactory(2),
                    rootState, expanderMCTS);
            algMCTS.runMiliseconds(100);
            
            Distribution dist = new MeanStratDist();
            StringBuilder moves = new StringBuilder();
            GameState curState = rootState;
            while (!curState.isGameEnd()){
                Action a;
                if (curState.isPlayerToMoveNature()){
                    List<Action> actions = expanderOOS.getActions(curState);
                    assert actions.size()==1;
                    a=actions.get(0);
                }else if (curState.getPlayerToMove().getId()==playerID){
                    MCTSInformationSet curIS = expanderOOS.getAlgorithmConfig().getInformationSetFor(curState);
                    algOOS.setCurrentIS(curIS);
                    a = algOOS.runMiliseconds(MCTS_MILISECONDS_PER_CALL);
                    Strategy strategy0 = StrategyCollector.getStrategyFor(algOOS.getRootNode(), rootState.getAllPlayers()[0], dist);
                    Strategy strategy1 = StrategyCollector.getStrategyFor(algOOS.getRootNode(), rootState.getAllPlayers()[1], dist);
                    double err =  exploitability(ISMCTSExploitability.filterLow(strategy0),expanderOOS) + exploitability(ISMCTSExploitability.filterLow(strategy0),expanderOOS);
                    out.println("Current OOS error: " + err);
                } else {
                    MCTSInformationSet curIS = expanderMCTS.getAlgorithmConfig().getInformationSetFor(curState);
                    algMCTS.setCurrentIS(curIS);
                    a = algMCTS.runMiliseconds(MCTS_MILISECONDS_PER_CALL);
                }
                moves.append(a + " ");
                curState = curState.performAction(a);
            }
            System.out.println("MATCH: " + moves.toString() + curState.getUtilities()[0]);
//            out.println(((InnerNode)firstRunner.rootNode.getChildren().values().iterator().next()).getInformationSet().getInformationSetStats().getNbSamples()
//                    + " " + (new MeanStratDist()).getDistributionFor(((InnerNode)firstRunner.rootNode.getChildren().values().iterator().next()).getInformationSet()));
//            out.println(((InnerNode)secondRunner.rootNode.getChildren().values().iterator().next()).getInformationSet().getInformationSetStats().getNbSamples()
//                    + " " + (new FrequenceDistribution()).getDistributionFor(((InnerNode)secondRunner.rootNode.getChildren().values().iterator().next()).getInformationSet()));
//            out.println((new MeanStratDist()).getDistributionFor(firstRunner.rootNode.getInformationSet()));
//            out.println((new FrequenceDistribution()).getDistributionFor(secondRunner.rootNode.getInformationSet()));
        }
        
        
        public static void ISMCTSvsStrategy(Strategy strategy) throws Exception {
            stitchingDist = new SumFrequenceDistribution();
            StringBuilder moves = new StringBuilder();
            Expander<MCTSInformationSet> expander = new GoofSpielExpander<MCTSInformationSet> (new MCTSConfig());
            expander.getAlgorithmConfig().createInformationSetFor(rootState);
            ISMCTSAlgorithm alg = new ISMCTSAlgorithm(
                    rootState.getAllPlayers()[playerID],
                    new DefaultSimulator(expander),
                    new UCTBackPropFactory(2),
                    rootState, expander);
            alg.runMiliseconds(100);
            GameState curState = rootState;
            while (!curState.isGameEnd()){
                Action a;
                if (curState.isPlayerToMoveNature()){
                    List<Action> actions = expander.getActions(curState);
                    a=actions.get(rnd.nextInt(actions.size()));
                    assert curState.getProbabilityOfNatureFor(a)==1.0/actions.size();
                } else if (curState.getPlayerToMove().getId()==1-playerID){
                    a = Strategy.selectAction(strategy.getDistributionOfContinuationOf(curState.getSequenceForPlayerToMove(), expander.getActions(curState)), rnd);
                } else {                  
                    MCTSInformationSet curIS = expander.getAlgorithmConfig().getInformationSetFor(curState);
                    alg.setCurrentIS(curIS);
                    a = alg.runMiliseconds(MCTS_MILISECONDS_PER_CALL);
                    addAllToStichedStrategy(curIS.getAllNodes());
                }
                moves.append(a + " ");
                curState = curState.performAction(a);
            }
            out.println(curState.getUtilities()[0]);
            out.println("MATCH: " + moves.toString() + curState.getUtilities()[0]);
        }
        
        
        public static void OOSvsStrategy(Strategy strategy) throws Exception {
            stitchingDist = new SumMeanStratDist();
            StringBuilder moves = new StringBuilder();
            Expander<MCTSInformationSet> expander = new GoofSpielExpander<MCTSInformationSet> (new MCTSConfig());
            expander.getAlgorithmConfig().createInformationSetFor(rootState);
            OOSAlgorithm alg = new OOSAlgorithm(
                    rootState.getAllPlayers()[playerID],
                    new OOSSimulator(expander),
                    rootState, expander, delta, 0.6);
            alg.runMiliseconds(100);
            GameState curState = rootState;
            while (!curState.isGameEnd()){
                Action a;
                if (curState.isPlayerToMoveNature()){
                    List<Action> actions = expander.getActions(curState);
                    a=actions.get(rnd.nextInt(actions.size()));
                    assert curState.getProbabilityOfNatureFor(a)==1.0/actions.size();
                } else if (curState.getPlayerToMove().getId()==1-playerID){
                    a = Strategy.selectAction(strategy.getDistributionOfContinuationOf(curState.getSequenceForPlayerToMove(), expander.getActions(curState)), rnd);
                } else {                  
                    MCTSInformationSet curIS = expander.getAlgorithmConfig().getInformationSetFor(curState);
                    alg.setCurrentIS(curIS);
                    a = alg.runMiliseconds(MCTS_MILISECONDS_PER_CALL);
                    addAllToStichedStrategy(curIS.getAllNodes());
                }
                if (a==null){
                    out.println("Warning: playing a random move!!!");
                    List<Action> actions = expander.getActions(curState);
                    a=actions.get(rnd.nextInt(actions.size()));
                }
                moves.append(a + " ");
                curState = curState.performAction(a);
                
            }
            out.println("MATCH: " + moves.toString() + curState.getUtilities()[0]);
        }
        
        public static double exploitability(Map<Sequence, Double> strategy, Expander expander){
            Map<Sequence, Double> st = ISMCTSExploitability.filterLow(strategy);
            SQFBestResponseAlgorithm mctsBR = new SQFBestResponseAlgorithm(
                    expander,
                    1-strategy.keySet().iterator().next().getPlayer().getId(),
                    new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] },
                    (ConfigImpl)expander.getAlgorithmConfig(), gameInfo);
            double val = mctsBR.calculateBR(rootState, st);
            return val;
        }
        
        private static HashMap<ISKey, Map<Action,Double>> stitchedStrategy = new HashMap();
        public static void addToStichedStrategy(ISKey isKey, Map<Action,Double> distribution){
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
                stitchedStrategy.put(new PerfectRecallISKey(((PerfectRecallISKey)isKey).getHash(),sfAlgConfig.getInformationSetFor(s).getPlayersHistory()), d);
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
            HashSet<ISKey> added = new HashSet();
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
        
        private static double delta=0.8;
        private static int playerID=0;
        private static String parsePlayer(int position, String pDesc){
            String alg = pDesc.substring(0,3);
            int tPos = pDesc.lastIndexOf('t');
            if (alg.equals("OOS")){
                delta = Double.parseDouble(pDesc.substring(4,tPos))/10;
                if (pDesc.charAt(4) == '0'){
                    delta /=10;
                }
            } else if (alg.equals("UCT")){
                assert pDesc.charAt(3)=='2';
            } else {
                assert false;
            }
            int time = 1000*Integer.parseInt(pDesc.substring(tPos+1));
            if (pDesc.charAt(tPos+1) == '0') time /= 10;
            if (position==0) MCTS_MILISECONDS_PER_CALL = time;
            else assert MCTS_MILISECONDS_PER_CALL == time;
            return alg;
        }
        
        
        
        
        //String[]{"IIGS6","P0","OOSd9t1"/"UCT2t5"}
        private static void computeExploitability(String[] args) throws Exception{
            playerID = Integer.parseInt(args[1].substring(1,2));
            String alg = parsePlayer(0, args[2]);
            
            for (int r=0; r<matches/50;r++){
                for (int i=0; i<50;i++){
                    if (alg.equals("OOS")) OOSvsStrategy(new UniformStrategyForMissingSequences());
                    else ISMCTSvsStrategy(new UniformStrategyForMissingSequences());
                }
                out.println("Number of IS in the stiched strategy: " + stitchedStrategy.size());
                Strategy s = UniformStrategyForMissingSequences.fromBehavioralStrategy(stitchedStrategy, rootState, sfExpander);
                out.println("Stiched strategy exploitability:" + exploitability(s, sfExpander));
            }
        }
        
        //String[]{"IIGS6","OOSd9t1","UCT2t1"}
        private static void runMatches(String[] args) throws Exception{
            String alg = parsePlayer(0, args[1]);
            parsePlayer(1, args[2]);
            if (alg.equals("OOS")) playerID=0;
            else playerID=1;
            for (int i=0;i<matches;i++) runOOSvsISMCTS();
        }
        
        
        static int matches=500;
        public static void main(String[] args) throws Exception{
            //out = new PrintStream(StringUtils.join(args, '_'));
            //System.setErr(new PrintStream(StringUtils.join(args, '_')+".err"));=
            //assert GSGameInfo.CARDS_FOR_PLAYER.length==6;
            String s = System.getProperty("STITCHONE");
            if (s != null) stitchOnlyCurrentIS = new Boolean(s);
            
            if (args[1].length()==2){
                setupIIGoofSpielExpl();
                computeExploitability(args);
            } else {
                //setupIIGoofSpiel();                
                setupIIGoofSpielExpl();
                //setupPTTT();
                runMatches(args);
            }
            //for (;;) runMCTS();
            //for (;;) runOOSvsISMCTS();
            //computeExploitability(new String[]{"IIGS6","P0","OOSd9t1"});
        }
 }
