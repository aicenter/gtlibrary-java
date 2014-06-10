package cz.agents.gtlibrary.algorithms.runner;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.Random;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SMJournalOnlineExperiments {

    static GameInfo gameInfo;
    static GameState rootState;
    static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
    static Expander<MCTSInformationSet> expander;
    static Random rnd = new HighQualityRandom();
    static int compTime = 1000;


    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: SMJournalOnlineExperiments {BI|BIAB|DO|DOAB|DOSAB|CFR|OOS|MCTS} {BI|BIAB|DO|DOAB|DOSAB|CFR|OOS|MCTS} {GS|PE|RG} [domain parameters].");
            System.exit(-1);
        }
        SMJournalOnlineExperiments exp = new SMJournalOnlineExperiments();
        exp.handleDomain(args);
        
        for (int i=0; i<100; i++) exp.runMatch(args);
    }


    public void handleDomain(String[] args) {
        if (args[2].equalsIgnoreCase("GS")) {  // Goofspiel
            if (args.length != 5) {
                throw new IllegalArgumentException("Illegal domain arguments count: 2 parameters are required {SEED} {DEPTH}");
            }
            GSGameInfo.seed = new Integer(args[3]);
            int depth = new Integer(args[4]);
            GSGameInfo.depth = depth;
            GSGameInfo.regenerateCards = true;
        } else if (args[2].equalsIgnoreCase("OZ")) { // Oshi Zumo
            if (args.length != 7) {
                throw new IllegalArgumentException("Illegal domain arguments count: 4 parameters are required {SEED} {COINS} {LOC_K} {MIN_BID}");
            }
            OZGameInfo.seed = new Integer(args[3]);
            OZGameInfo.startingCoins = new Integer(args[4]);
            OZGameInfo.locK = new Integer(args[5]);
            OZGameInfo.minBid = new Integer(args[6]);
        } else if (args[1].equalsIgnoreCase("PE")) { // Generic Poker
            if (args.length != 6) {
                throw new IllegalArgumentException("Illegal poker domain arguments count: 3 parameters are required {SEED} {DEPTH} {GRAPH}");
            }
            PursuitGameInfo.seed = new Integer(args[3]);
            PursuitGameInfo.depth = new Integer(args[4]);
            PursuitGameInfo.graphFile = args[5];
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
        } else throw new IllegalArgumentException("Illegal domain: " + args[2]);
    }

    public void loadGame(String domain) {
        if (domain.equals("GS")) {
            gameInfo = new GSGameInfo();
            rootState = new GoofSpielGameState();
            expander = new GoofSpielExpander<MCTSInformationSet>(new MCTSConfig());
        } else if (domain.equals("PE")) {
            gameInfo = new PursuitGameInfo();
            rootState = new PursuitGameState();
            expander = new PursuitExpander<MCTSInformationSet>(new MCTSConfig());
        } else if (domain.equals("OZ")) {
            gameInfo = new OZGameInfo();
            rootState = new OshiZumoGameState();
            expander = new OshiZumoExpander<MCTSInformationSet>(new MCTSConfig());
        } else if (domain.equals("RG")) {
            gameInfo = new RandomGameInfo();
            rootState = new SimRandomGameState();
            expander = new RandomGameExpander<MCTSInformationSet>(new MCTSConfig());
        }
    }
    
    public GamePlayingAlgorithm getPlayer(int posIndex, String alg, String domain) {
        if (alg.equals("MCTS")){
            loadGame(domain);
            expander.getAlgorithmConfig().createInformationSetFor(rootState);
            
            ISMCTSAlgorithm player = new ISMCTSAlgorithm(
                    rootState.getAllPlayers()[posIndex],
                    new DefaultSimulator(expander),
                    //new UCTBackPropFactory(2),
                    new Exp3BackPropFactory(-1, 1, 0.2),
                    //new RMBackPropFactory(-1,1,0.4),
                    rootState, expander);
            player.returnMeanValue=false;
            player.runIterations(2);
            return player;
        } else if (alg.equals("CFR") || alg.equals("OOS")){
            loadGame(domain);
            expander.getAlgorithmConfig().createInformationSetFor(rootState);
            GamePlayingAlgorithm player = (alg.equals("OOS")) ? new SMOOSAlgorithm(rootState.getAllPlayers()[posIndex],new OOSSimulator(expander),rootState, expander, 0.6) : new CFRAlgorithm(rootState.getAllPlayers()[posIndex],rootState, expander);
            player.runMiliseconds(20);
            return player;
        } else { // backward induction algorithms
            throw new NotImplementedException();
        }
    }
    
    public void runMatch(String[] args){
        StringBuilder moves = new StringBuilder();
        GamePlayingAlgorithm p1 = getPlayer(0, args[0], args[2]);
        GamePlayingAlgorithm p2 = getPlayer(1, args[1], args[2]);
        
        GameState curState = rootState.copy();
        while (!curState.isGameEnd()){
            if (curState.isPlayerToMoveNature()){
                double r = rnd.nextDouble();
                for(Action ca : expander.getActions(curState)){
                    final double ap = curState.getProbabilityOfNatureFor(ca);
                    if (r <= ap) {
                        moves.append(ca + " ");
                        curState = curState.performAction(ca);
                        break;
                    }
                    r -= ap;
                }
            } else {
                Action a1 = p1.runMiliseconds(compTime, curState);
                Action a2 = p2.runMiliseconds(compTime, curState);
                moves.append(a1 + " ");
                moves.append(a2 + " ");
                curState = curState.performAction(a1);
                curState = curState.performAction(a2);
            }
        }
        System.out.println("MATCH: " + moves.toString() + curState.getUtilities()[0]);
    }
}
