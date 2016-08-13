package cz.agents.gtlibrary.utils.io;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Allows export of restricted game to gambit format, the restriction is made by providing Map<GameState, double[]> leafUtilities,
 * which are expected to contain all the leafs of the restricted game.
 * */
public class PartialGambitEFG {
    private boolean wActionLabels = false;
    private Map<ISKey, Integer> infSetIndices;
    private int maxIndex;


    public PartialGambitEFG() {
        infSetIndices = new HashMap<>();
        maxIndex = 0;
    }

    public void writeZeroSum(String filename, GameState root, Expander<? extends InformationSet> expander, Map<GameState, Double> leafUtilities) {
        write(filename, root, expander, convert(leafUtilities), Integer.MAX_VALUE);
    }

    private Map<GameState, double[]> convert(Map<GameState, Double> leafUtilities) {
        return leafUtilities.entrySet().stream().collect(Collectors.toMap(e ->  e.getKey(), e -> new double []{e.getValue(), -e.getValue()}));
    }

    public void write(String filename, GameState root, Expander<? extends InformationSet> expander, Map<GameState, double[]> leafUtilities) {
        write(filename, root, expander, leafUtilities, Integer.MAX_VALUE);
    }

    public void write(String filename, GameState root, Expander<? extends InformationSet> expander, Map<GameState, double[]> leafUtilities, int cut_off_depth) {
//        HashCodeEvaluator evaluator = new HashCodeEvaluator();
//
//        evaluator.build(root, expander);
//        assert evaluator.getCollisionCount() == 0;
        try {
            PrintStream out = new PrintStream(filename);

            out.print("EFG 2 R \"" + root.getClass() + expander.getClass() + "\" {");
            Player[] players = root.getAllPlayers();
            for (int i = 0; i < 2; i++) {//assumes 2 player games (possibly with nature) nature is the last player and always present!!!
                if (i != 0) out.print(" ");
                out.print("\"" + players[i] + "\"");
            }
            out.println("}");
            nextOutcome = 1;
            nextChance = 1;
            writeRec(out, root, expander, leafUtilities, cut_off_depth);
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    int nextOutcome = 1;
    int nextChance = 1;

    private void writeRec(PrintStream out, GameState node, Expander<? extends InformationSet> expander, Map<GameState, double[]> leafUtilities, int cut_off_depth) {
        if (node.isGameEnd() || cut_off_depth == 0 || leafUtilities.containsKey(node)) {
            out.print("t \"" + node.toString() + "\" " + nextOutcome++ + " \"\" { ");
            double[] u = leafUtilities.getOrDefault(node, new double[]{-1e6, -1e6});

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
//                ((SequenceFormConfig<SequenceInformationSet>) expander.getAlgorithmConfig()).addStateToSequenceForm(next);
                writeRec(out, next, expander, leafUtilities, cut_off_depth - 1);
            }
        }
    }

    private Integer getUniqueHash(ISKey key) {
        if (!infSetIndices.containsKey(key))
            infSetIndices.put(key, ++maxIndex);
        return infSetIndices.get(key);
    }
}