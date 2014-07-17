/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

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
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.io.PrintStream;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author vilo
 */
public class IIGMCTSMatch {
        private static Random rnd = new HighQualityRandom();
        private static int MCTS_MILISECONDS_PER_CALL = (int)1000;
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
        public static void setupIIGoofSpielExpl(){
            setupIIGoofSpiel();
            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            sfExpander = new GoofSpielExpander<SequenceInformationSet>(sfAlgConfig);
            FullSequenceEFG efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            efg.generateCompleteGame();
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
                } else {
                    MCTSInformationSet curIS = expanderMCTS.getAlgorithmConfig().getInformationSetFor(curState);
                    algMCTS.setCurrentIS(curIS);
                    a = algMCTS.runMiliseconds(MCTS_MILISECONDS_PER_CALL);
                }
                out.print(a + " ");
                curState = curState.performAction(a);
            }
            out.println(curState.getUtilities()[0]);
//            out.println(((InnerNode)firstRunner.rootNode.getChildren().values().iterator().next()).getInformationSet().getInformationSetStats().getNbSamples()
//                    + " " + (new MeanStratDist()).getDistributionFor(((InnerNode)firstRunner.rootNode.getChildren().values().iterator().next()).getInformationSet()));
//            out.println(((InnerNode)secondRunner.rootNode.getChildren().values().iterator().next()).getInformationSet().getInformationSetStats().getNbSamples()
//                    + " " + (new FrequenceDistribution()).getDistributionFor(((InnerNode)secondRunner.rootNode.getChildren().values().iterator().next()).getInformationSet()));
//            out.println((new MeanStratDist()).getDistributionFor(firstRunner.rootNode.getInformationSet()));
//            out.println((new FrequenceDistribution()).getDistributionFor(secondRunner.rootNode.getInformationSet()));
        }
         
         
        public static void ISMCTSvsStrategy(Strategy strategy) throws Exception {
            stitchingDist = new SumFrequenceDistribution();
            Expander<MCTSInformationSet> expander = new GoofSpielExpander<MCTSInformationSet> (new MCTSConfig());
            expander.getAlgorithmConfig().createInformationSetFor(rootState);
            ISMCTSAlgorithm alg = new ISMCTSAlgorithm(
                    rootState.getAllPlayers()[0],
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
                out.print(a + " ");
                curState = curState.performAction(a);
            }
            out.println(curState.getUtilities()[0]);
        }
        
        
        public static void OOSvsStrategy(Strategy strategy) throws Exception {
            stitchingDist = new SumMeanStratDist();
            Expander<MCTSInformationSet> expander = new GoofSpielExpander<MCTSInformationSet> (new MCTSConfig());
            expander.getAlgorithmConfig().createInformationSetFor(rootState);
            OOSAlgorithm alg = new OOSAlgorithm(
                    rootState.getAllPlayers()[0],
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
                out.print(a + " ");
                curState = curState.performAction(a);
            }
            out.println(curState.getUtilities()[0]);
        }
        
        public static double exploitability(Map<Sequence, Double> strategy, Expander expander){
            Map<Sequence, Double> st = ISMCTSExploitability.filterLow(strategy);
            SQFBestResponseAlgorithm mctsBR = new SQFBestResponseAlgorithm(
                    sfExpander,
                    1-strategy.keySet().iterator().next().getPlayer().getId(),
                    new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] },
                    (ConfigImpl)sfExpander.getAlgorithmConfig(), gameInfo);
            double val = mctsBR.calculateBR(rootState, st);
            return -val;
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
        
        static Distribution stitchingDist = new MeanStratDist();
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
                MCTSInformationSet is = in.getInformationSet();
                //if (is.getPlayer().getId() < 2 && is.getInformationSetStats().getNbSamples() < 20) continue;
                if (is.getPlayer().equals(pl) &&
                        !added.contains(in.getGameState().getISKeyForPlayerToMove())){
                    Map<Action, Double> dist = stitchingDist.getDistributionFor(is.getAlgorithmData());
//                    for (Map.Entry<Action,Double> en : dist.entrySet()){
//                        en.setValue(en.getValue()*is.getInformationSetStats().getNbSamples());
//                    }
                    addToStichedStrategy(in.getGameState().getISKeyForPlayerToMove(), dist);
                    added.add(in.getGameState().getISKeyForPlayerToMove());
                }
                q.addAll(in.getChildren().values());
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
            
            for (int r=0; r<matches/10;r++){
                for (int i=0; i<10;i++){
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
        
        
        static int matches=150;
        public static void main(String[] args) throws Exception{
            //out = new PrintStream(StringUtils.join(args, '_'));
            //System.setErr(new PrintStream(StringUtils.join(args, '_')+".err"));=
            //assert GSGameInfo.CARDS_FOR_PLAYER.length==6;
            if (args.length > 3) matches = Integer.parseInt(args[3]);
            if (args[1].length()==2){
                setupIIGoofSpielExpl();
                computeExploitability(args);
            } else {
                setupIIGoofSpiel();
                //setupPTTT();
                runMatches(args);
            }
            //for (;;) runMCTS();
            //for (;;) runOOSvsISMCTS();
            //computeExploitability(new String[]{"IIGS6","P0","OOSd9t1"});
        }
 }
