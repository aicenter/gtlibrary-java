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

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallAlgorithmConfig;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABConfig;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.utils.BasicGameBuilder;

//my GoofSpiel exporter
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
// my PhantomTTT exporter
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;


import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Assumes a two player game.
 * Assumes that nodes with the same hashCode() of information set key pair belong to the same information set (without check on equals())
 */
public class GambitEFG {
    private boolean wActionLabels = true;
    private Map<ISKey, Integer> infSetIndices;
    private int maxIndex;

    public static void main(String[] args) {
        //exportRandomGame();
        //exportGoofSpiel();
        exportPhantomTTT();
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

    public static void exportGoofSpiel() {
        // setup Game:
        //GSGameInfo.seed = 2;
        //GSGameInfo.depth = 2;
        //Integer depth = 2;
        //GSGameInfo.BINARY_UTILITIES = true;
        //GSGameInfo.useFixedNatureSequence = true;
        //GSGameInfo.regenerateCards = true;

        boolean AB = false; //alphaBetaBounds
        boolean DO = false; //doubleOracle
        boolean SORT = false;  //sortingOwnActions
        boolean CACHE = false; //useGlobalCache

        GambitEFG exporter = new GambitEFG();


        System.out.println("GoofSpielGameState");

        GameInfo gameInfo = new GSGameInfo(); // call to init natureSequence

        GoofSpielGameState root = new GoofSpielGameState();

        System.out.println(root);


        System.out.println("getNatureSequence");
        System.out.println(root.getNatureSequence());

        System.out.println("buildAndWrite");

        //exporter.buildAndWrite("MyGoofSpiel.gbt", root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
        exporter.buildAndWrite("MyGoofSpiel.gbt", root, new GoofSpielExpander<>(new SequenceFormConfig<>()));
    }

    public GambitEFG() {
        infSetIndices = new HashMap<>();
        maxIndex = 0;
    }

    public void write(String filename, GameState root, Expander<? extends InformationSet> expander) {
        write(filename, root, expander, Integer.MAX_VALUE);
    }

    public void write(String filename, GameState root, Expander<? extends InformationSet> expander, int cut_off_depth) {
//        HashCodeEvaluator evaluator = new HashCodeEvaluator();
//
//        evaluator.build(root, expander);
//        assert evaluator.getCollisionCount() == 0;
        try {
            PrintStream out = new PrintStream(filename);

            out.print("EFG 2 R \"" + root.getClass() + expander.getClass() + "\" {");
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

    int nextOutcome = 1;
    int nextChance = 1;

    private void writeRec(PrintStream out, GameState node, Expander<? extends InformationSet> expander, int cut_off_depth) {
        if (node.isGameEnd() || cut_off_depth == 0) {
            out.print("t \"" + node.toString() + "\" " + nextOutcome++ + " \"\" { ");
            double[] u = node.getUtilities();
            for (int i = 0; i < 2; i++) {
                out.print((i == 0 ? "" : ", ") + u[i]);
            }
            out.println("}");
        } else {
            List<Action> actions = expander.getActions(node);
            if (node.isPlayerToMoveNature()) {
                out.print("c \"" + node.toString() + "\" " + nextChance++ + " \"\" { ");
                for (Action a : actions) {
                    out.print("\"" + (wActionLabels ? a.toString() : "") + "\" " + node.getProbabilityOfNatureFor(a) + " ");
                }
            } else {
                out.print("p \"" + node.toString() + "\" " + (node.getPlayerToMove().getId() + 1) + " " + getUniqueHash(node.getISKeyForPlayerToMove()) + " \"\" { ");
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

    private Integer getUniqueHash(ISKey key) {
        if (!infSetIndices.containsKey(key))
            infSetIndices.put(key, ++maxIndex);
        return infSetIndices.get(key);
    }

    public void buildAndWrite(String filename, GameState root, Expander<? extends InformationSet> expander) {
        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        write(filename, root, expander, Integer.MAX_VALUE);
    }
}
