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


package cz.agents.gtlibrary.utils.io;

import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.liarsdice.LDGameInfo;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceExpander;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.rps.RPSExpander;
import cz.agents.gtlibrary.domain.rps.RPSGameInfo;
import cz.agents.gtlibrary.domain.rps.RPSGameState;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallAlgorithmConfig;
import cz.agents.gtlibrary.iinodes.PSKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;

//my GoofSpiel exporter
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
// my PhantomTTT exporter
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;


import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Assumes a two player game.
 * Assumes that nodes with the same hashCode() of information set key pair belong to the same information set (without check on equals())
 */
public class GambitEFG {
    private boolean wActionLabels = true;
    private boolean wNodeLabels = true;
    public boolean wISKeys = false; // if false, writes PS keys
    public boolean wAugISKeys = false; // if false, writes IS keys
    private Map<ISKey, Integer> infSetIndices;
    private int maxIndex;
    private Double normalizingUtils = 1.0;

    public static void main(String[] args) {
//        exportRandomGame();
        exportIIGoofSpiel();
//        exportLD();
//        exportGP();
//        exportRPS();
//        exportPhantomTTT();
    }

    public static void exportRandomGame() {
        GambitEFG exporter = new GambitEFG();

        if (RandomGameInfo.IMPERFECT_RECALL)
            exporter.buildAndWrite("RGGambit1.gbt", new RandomGameState(), new RandomGameExpander<>(new ImperfectRecallAlgorithmConfig()));
        else
            exporter.buildAndWrite("RGGambit2.gbt", new RandomGameState(), new RandomGameExpander<>(new SequenceFormConfig<>()));
    }

    public static void exportPhantomTTT () {
        GambitEFG exporter = new GambitEFG();

        exporter.buildAndWrite("MyPhantomTTT.gbt", new TTTState(), new TTTExpander<>(new SequenceFormConfig<>()));
    }

    public static void exportIIGoofSpiel() {
        // setup Game:
        GSGameInfo.seed = 0;
        GSGameInfo.depth = 3;
        GSGameInfo.BINARY_UTILITIES = true;
        GSGameInfo.useFixedNatureSequence = true;
        GSGameInfo.regenerateCards = true;

        boolean AB = false; //alphaBetaBounds
        boolean DO = false; //doubleOracle
        boolean SORT = false;  //sortingOwnActions
        boolean CACHE = false; //useGlobalCache

        GambitEFG exporter = new GambitEFG();

        GameInfo gameInfo = new GSGameInfo(); // call to init natureSequence

        IIGoofSpielGameState root = new IIGoofSpielGameState();
        //exporter.buildAndWrite("MyGoofSpiel.gbt", root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
        exporter.buildAndWrite("MyIIGoofSpiel_"+(exporter.wISKeys?"IS":"PT")+".gbt", root, new GoofSpielExpander<>(new SequenceFormConfig<>()));
    }

    public static void exportLD() {
        // setup Game:
        LDGameInfo.P1DICE = 1;
        LDGameInfo.P2DICE = 1;
        LDGameInfo.FACES = 3;
        LDGameInfo.CALLBID = (LDGameInfo.P1DICE + LDGameInfo.P2DICE) * LDGameInfo.FACES + 1;

        GambitEFG exporter = new GambitEFG();

        GameInfo gameInfo = new LDGameInfo();
        LiarsDiceGameState root = new LiarsDiceGameState();

        exporter.buildAndWrite("LD_"+(exporter.wISKeys?"IS":"PT")+".gbt", root, new LiarsDiceExpander<>(new SequenceFormConfig<>()));
    }

    public static void exportGP() {
        // setup Game:
        GPGameInfo.MAX_CARD_TYPES = 2;
        GPGameInfo.MAX_CARD_OF_EACH_TYPE = 2;
        GPGameInfo.MAX_RAISES_IN_ROW = 1;
        GPGameInfo.MAX_DIFFERENT_BETS = 1;
        GPGameInfo.MAX_DIFFERENT_RAISES = 1;

        GambitEFG exporter = new GambitEFG();

        GameInfo gameInfo = new GPGameInfo();
        GenericPokerGameState root = new GenericPokerGameState();

        exporter.buildAndWrite("GP_"+(exporter.wISKeys?"IS":"PT")+".gbt", root, new GenericPokerExpander<>(new SequenceFormConfig<>()));
    }

    public static void exportRPS() {
        GambitEFG exporter = new GambitEFG();
        GameInfo gameInfo = new RPSGameInfo();
        GameState root = new RPSGameState();

        exporter.buildAndWrite("RPS_"+(exporter.wISKeys?"IS":"PT")+".gbt", root, new RPSExpander<>(new SequenceFormConfig<>()));
    }

    public GambitEFG() {
        infSetIndices = new HashMap<>();
        maxIndex = 0;
    }

    public void write(String filename, GameState root, Expander<? extends InformationSet> expander) {
        write(filename, root, expander, Integer.MAX_VALUE);
    }

    public void write(String filename, InnerNode root) {
        write(filename, root, false);
    }

    public void write(String filename, InnerNode root, boolean expandNodes) {
        if(root instanceof GadgetChanceNode) {
            this.normalizingUtils = ((GadgetChanceNode) root).getRootReachPr();
        }
        write(filename, root, Integer.MAX_VALUE, expandNodes);
        normalizingUtils = 1.0;
    }

    public void write(String filename, GameState root, Expander<? extends InformationSet> expander, int cut_off_depth) {
//        HashCodeEvaluator evaluator = new HashCodeEvaluator();
//
//        evaluator.build(root, expander);
//        assert evaluator.getCollisionCount() == 0;
        System.err.println("Writing to "+filename);
        try {
            PrintStream out = new PrintStream(filename);

            out.print("EFG 2 R \"" + (wISKeys ? "IS" : "PT" ) + " "+ root.getClass().getSimpleName() + " " + expander.getClass().getSimpleName() + " " + filename + "\" {");
            Player[] players = root.getAllPlayers();
            for (int i = 0; i < 2; i++) {//assumes 2 playter games (possibly with nature) nature is the last player and always present!!!
                if (i != 0) out.print(" ");
                out.print("\"" + players[i] + "\"");
            }
            out.println("}");
            nextOutcome = 1;
            nextChance = 1;
            writeRec(out, root, expander, cut_off_depth);
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void write(String filename, Node root, int cut_off_depth, boolean expandNodes) {
        try {
            PrintStream out = new PrintStream(filename);

            out.print("EFG 2 R \"" + (wISKeys ? "IS" : "PT" ) + " "+ root.getClass().getSimpleName() + " " + filename + "\" {");
            Player[] players = root.getAllPlayers();
            for (int i = 0; i < 2; i++) {//assumes 2 playter games (possibly with nature) nature is the last player and always present!!!
                if (i != 0) out.print(" ");
                out.print("\"" + players[i] + "\"");
            }
            out.println("}");
            nextOutcome = 1;
            nextChance = 1;
            writeRec(out, root, cut_off_depth, expandNodes);
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    int nextOutcome = 1;
    int nextChance = 1;

    private void writeRec(PrintStream out, GameState node, Expander<? extends InformationSet> expander, int cut_off_depth) {
        if (node.isGameEnd() || cut_off_depth == 0) {
            out.print("t \"" + (wNodeLabels ? node.toString() : "") + "\" " + nextOutcome++ + " \"\" { ");
            double[] u = node.getUtilities();
            for (int i = 0; i < 2; i++) {
                out.print((i == 0 ? "" : ", ") + u[i] * normalizingUtils);
            }
            out.println("}");
        } else {
            List<Action> actions = expander.getActions(node);
            if (node.isPlayerToMoveNature()) {
                out.print("c \"" + (wNodeLabels ? node.toString() : "") + "\" "+(wISKeys ? nextChance++ : getUniqueHash(((DomainWithPublicState) node).getPSKeyForPlayerToMove()))+" \"\" { ");
                for (Action a : actions) {
                    out.print("\"" + (wActionLabels ? a.toString() : "") + "\" " + node.getProbabilityOfNatureFor(a) + " ");
                }
            } else {
                out.print("p \"" + (wNodeLabels ? node.toString() : "") + "\" " + (node.getPlayerToMove().getId() + 1) + " " + getUniqueHash(wISKeys ? node.getISKeyForPlayerToMove() : ((DomainWithPublicState) node).getPSKeyForPlayerToMove()) + " \"\" { ");
                for (Action a : actions) {
                    out.print("\"" + (wActionLabels ? a.toString() : "") + "\" ");
                }
            }
            out.println("} 0");
            for (Action a : actions) {
                GameState next = node.performAction(a);
                expander.getAlgorithmConfig().addInformationSetFor(next);
                writeRec(out, next, expander, cut_off_depth - 1);
            }
        }
    }

    private void writeRec(PrintStream out, Node node, int cut_off_depth, boolean expandNodes) {
        if (node.isGameEnd() || cut_off_depth == 0 ||
                ((node instanceof InnerNode) && (((InnerNode) node).getChildren().size() == 0))) {
            for (int i = 0; i < node.getDepth(); i++) out.print("\t");
            out.print("t \"" + (wNodeLabels ? node.toString() : "") + "\" " + nextOutcome++ + " \"\" { ");
            if(node instanceof LeafNode) {
                double[] u = ((LeafNode) node).getUtilities();
                for (int i = 0; i < 2; i++) {
                    out.print((i == 0 ? "" : ", ") + u[i] * normalizingUtils);
                }
            } else {
                double _u = ((InnerNode) node).getEVWeighted();
                double[] u = new double[2];
                u[0] = _u;
                u[1] = -_u;
                for (int i = 0; i < 2; i++) {
                    out.print((i == 0 ? "" : ", ") + u[i]);
                }
            }
            out.println("}");
        } else {
            InnerNode inNode = ((InnerNode) node);
            GameState state = inNode.getGameState();
            List<Action> actions = inNode.getActions();
            if(!expandNodes) {
                // keep only existing actions
                actions = actions.stream().filter(a ->
                    inNode.getChildOrNull(a) != null
                ).collect(Collectors.toList());
            }
            for (int i = 0; i < node.getDepth(); i++) out.print("\t");
            if (state.isPlayerToMoveNature()) {
                out.print("c \"" + (wNodeLabels ? inNode.toString()  : "") + "\" "+(wISKeys ? nextChance++ : getUniqueHash(((DomainWithPublicState) state).getPSKeyForPlayerToMove()))+" \"\" { ");
                for (Action a : actions) {
                    out.print("\"" + (wActionLabels ? a.toString() : "") + "\" " + inNode.getProbabilityOfNatureFor(a) + " ");
                }
            } else {
                ISKey key = wAugISKeys ? inNode.getOpponentAugISKey() :
                        wISKeys ? state.getISKeyForPlayerToMove() : inNode.getPublicState().getPSKey();
                out.print("p \"" + (wNodeLabels ? inNode.toString()  : "") + "\" " + (inNode.getPlayerToMove().getId() + 1) + " " + getUniqueHash(key) + " \"\" { ");
                for (Action a : actions) {
                    out.print("\"" + (wActionLabels ? a.toString() : "") + "\" ");
                }
            }
            out.println("} 0");
            for (Action a : actions) {
                assert inNode.getChildOrNull(a) != null;
                Node next = inNode.getChildFor(a);
                writeRec(out, next,cut_off_depth - 1, expandNodes);

//                if(expandNodes) {
//                    Node next = ((InnerNode) node).getChildFor(a);
//                    writeRec(out, next,cut_off_depth - 1, expandNodes);
//                } else {
//                    Node next = ((InnerNode) node).getChildOrNull(a);
//                    if(next != null) writeRec(out, next,cut_off_depth - 1, expandNodes);
//                }

            }
        }
    }

    private Integer getUniqueHash(ISKey key) {
        if(key instanceof PSKey) {
            return ((PSKey) key).getId();
        }

        if (!infSetIndices.containsKey(key))
            infSetIndices.put(key, ++maxIndex);
        return infSetIndices.get(key);
    }

    public void buildAndWrite(String filename, GameState root, Expander<? extends InformationSet> expander) {
        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        write(filename, root, expander, Integer.MAX_VALUE);
    }

    public void buildAndWrite(String filename, InnerNode root) {
        BasicGameBuilder.build(root);
        write(filename, root, Integer.MAX_VALUE, false);
    }
}

