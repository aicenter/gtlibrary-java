package cz.agents.gtlibrary.io;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import java.io.PrintStream;
import java.util.List;

/**
 * Assumes a two player game.
 * Assumes that nodes with the same hashCode() of information set key pair belong to the same information set (without check on equals())
 */
public class GambitEFG {
    private static boolean wActionLabels = false;
    
    public static void write(String filename, GameState root, Expander<SequenceInformationSet> expander) {
        write(filename, root, expander, Integer.MAX_VALUE);
    }
    
    public static void write(String filename, GameState root, Expander<SequenceInformationSet> expander, int cut_off_depth) {
        try {
            PrintStream out = new PrintStream(filename);

            out.print("EFG 2 R \"" + root.getClass() + expander.getClass() + "\" {");
            Player[] players  = root.getAllPlayers();
            for (int i=0; i < 2; i++){//assumes 2 playter games (possibly with nature) nature is the last player and always present!!!
                if (i != 0) out.print(" ");
                out.print("\"" + players[i] + "\"");
            }
            out.println("}");
            nextOutcome = 1;
            writeRec(out, root, expander, cut_off_depth);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    static int nextOutcome = 1;
    
    private static void writeRec(PrintStream out, GameState node, Expander<SequenceInformationSet> expander, int cut_off_depth){
        if (node.isGameEnd() || cut_off_depth == 0){
            out.print("t \"" + node.toString() + "\" " + nextOutcome++ + " \"\" { ");
            double[] u = node.getUtilities();
            for (int i=0; i<u.length; i++){
                out.print((i==0 ? "" : ", ") + u[i]);
            }
            out.println("}");
        } else {
            List<Action> actions = expander.getActions(node);
            if (node.isPlayerToMoveNature()){
                out.print("c \"" + node.toString() + "\" " + 0 + " \"\" { ");
                for (Action a : actions){
                    out.print("\"" + (wActionLabels ? a.toString() : "")  + "\" " + node.getProbabilityOfNatureFor(a) + " ");
                }
            } else {
                out.print("p \"" + node.toString() + "\" " + (node.getPlayerToMove().getId()+1) + " " + Math.abs(node.getISKeyForPlayerToMove().hashCode()) + " \"\" { ");
                for (Action a : actions){
                    out.print("\"" + (wActionLabels ? a.toString() : "") + "\" ");
                }
            }
            out.println("} 0");
            for (Action a : actions){
                GameState next = node.performAction(a);
                ((SequenceFormConfig<SequenceInformationSet>) expander.getAlgorithmConfig()).addStateToSequenceForm(next);
                writeRec(out, next, expander, cut_off_depth-1);
            }
        }
    }
    
}
