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
import static cz.agents.gtlibrary.algorithms.mcts.experiments.IIGConvergenceExperiment.rnd;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.liarsdice.LDGameInfo;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceExpander;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceGameState;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.List;

public class IIGMatchesExperiment extends IIGConvergenceExperiment {
    static boolean printDebugInfo = false;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: IIGMatchesExperiment {OOS|MCTS-UCT|MCTS-EXP3|MCTS-RM|RAND} {OOS|MCTS-UCT|MCTS-EXP3|MCTS-RM|RAND} {IIGS|GP|PTTT|RG} [domain parameters].");
            System.exit(-1);
        }
        
        String s = System.getProperty("SEED");
        if (s != null) rnd = new HighQualityRandom(Long.parseLong(s));
        else rnd = new HighQualityRandom();
        
        IIGMatchesExperiment exp = new IIGMatchesExperiment();
        exp.handleDomain(args);

        s = System.getProperty("COMPTIME");
        if (s != null) compTime = new Integer(s);


        double sum = 0;
        int iterationCount = 1000;

        s = System.getProperty("MATCHES");
        if (s != null) iterationCount = new Integer(s);

        for (int i = 0; i < iterationCount; i++) {
            sum += exp.runMatch(args);
            System.out.println("avg: " + sum / (i + 1));
        }
        System.out.println("Overall avg: " + sum / iterationCount);
    }

    @Override
    public void loadGame(String domain) {
        if (domain.equals("IIGS")) {
            gameInfo = new GSGameInfo();
            rootState = new IIGoofSpielGameState();
            expander = new GoofSpielExpander<MCTSInformationSet>(new MCTSConfig());
        } else if (domain.equals("RG")) {
            gameInfo = new RandomGameInfo();
            rootState = new RandomGameState();
            expander = new RandomGameExpander<MCTSInformationSet>(new MCTSConfig());
        } else if (domain.equals("GP")) {
            gameInfo = new GPGameInfo();
            rootState = new GenericPokerGameState();
            expander = new GenericPokerExpander<MCTSInformationSet>(new MCTSConfig());
        } else if (domain.equals("PTTT")) {
            gameInfo = new TTTInfo();
            rootState = new TTTState();
            expander = new TTTExpander<MCTSInformationSet>(new MCTSConfig());
        }else if (domain.equals("LD")) {
            gameInfo = new LDGameInfo();
            rootState = new LiarsDiceGameState();
            expander = new LiarsDiceExpander<MCTSInformationSet>(new MCTSConfig());
        } else {
            throw new IllegalArgumentException("Incorrect game:" + domain);
        }
    }


    public double runMatch(String[] args) {
        StringBuilder moves = new StringBuilder();
        GamePlayingAlgorithm p1 = initGameAndAlg(args[0], 0, args[2]);
        GamePlayingAlgorithm p2 = initGameAndAlg(args[1], 1, args[2]);
        
        String s = System.getProperty("RIR");//run in root
        if (s != null && Boolean.parseBoolean(s)){
            p1.runMiliseconds(compTime);
            p2.runMiliseconds(compTime);
        }

        GameState curState = rootState.copy();
        while (!curState.isGameEnd()) {
            if (printDebugInfo) {
                System.out.println("");
                System.out.println(curState);
            }
            Action a=null;
            if (curState.isPlayerToMoveNature()) {
                double r = rnd.nextDouble();
                for (Action ca : (List<Action>) expander.getActions(curState)) {
                    final double ap = curState.getProbabilityOfNatureFor(ca);
                    if (r <= ap) {
                        a=ca;
                        break;
                    }
                    r -= ap;
                }
            } else if (curState.getPlayerToMove().getId()==0) {
                if (printDebugInfo)
                    System.out.println("Searching player 1...");
                if (p1.getRootNode()!=null){//mainly for the random player
                    MCTSInformationSet curIS = p1.getRootNode().getExpander().getAlgorithmConfig().getInformationSetFor(curState);
                    p1.setCurrentIS(curIS);
                }
                a = p1.runMiliseconds(compTime);
                if (printDebugInfo)
                    System.out.println("P1 chose: " + a);
            } else {
                if (printDebugInfo)
                    System.out.println("Searching player 2...");
                if (p2.getRootNode()!=null){//mainly for the random player
                    MCTSInformationSet curIS = p2.getRootNode().getExpander().getAlgorithmConfig().getInformationSetFor(curState);
                    p2.setCurrentIS(curIS);
                }
                a = p2.runMiliseconds(compTime);
                if (printDebugInfo)
                    System.out.println("P2 chose: " + a);
            }
            
            List<Action> actions = expander.getActions(curState);
            if (a == null){
                System.out.println("Warning!!! Selecting random move for " + curState.getPlayerToMove());
                a = actions.get(rnd.nextInt(actions.size()));
            } else {
                a = actions.get(actions.indexOf(a));//just to prevent memory leaks
            }
                
            moves.append(a + " ");
            curState = curState.performAction(a);
        }
        System.out.println("MATCH: " + moves.toString() + curState.getUtilities()[0]);
        
        p1=null;
        p2=null;
        gameInfo=null;
        expander=null;
        rootState=null;
        System.gc();
        
        return curState.getUtilities()[0];
    }
}
