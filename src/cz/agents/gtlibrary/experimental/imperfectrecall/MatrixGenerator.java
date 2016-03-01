package cz.agents.gtlibrary.experimental.imperfectrecall;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.ir.leftright.LRExpander;
import cz.agents.gtlibrary.domain.ir.leftright.LRGameInfo;
import cz.agents.gtlibrary.domain.ir.leftright.LRGameState;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLExpander;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLGameInfo;
import cz.agents.gtlibrary.domain.ir.memoryloss.MLGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;

/**
 * Created by bosansky on 7/31/14.
 */
public class MatrixGenerator {
    private GameState rootState;
    private SequenceFormConfig<SequenceInformationSet> algConfig;
    private Expander<SequenceInformationSet> expander;
    private GameInfo info;

    private ArrayList<InformationSet> numberedP1IS = new ArrayList<>();
    private ArrayList<InformationSet> numberedP2IS = new ArrayList<>();
    private ArrayList<Action> numberedP1Actions = new ArrayList<>();
    private ArrayList<Action> numberedP2Actions = new ArrayList<>();
    private HashMap<InformationSet, Integer> numberISMapping = new HashMap<>();
    private HashMap<GameState, Double> leafUtilities = new HashMap<>();


    public MatrixGenerator(GameState rootState, SequenceFormConfig<SequenceInformationSet> algConfig, Expander<SequenceInformationSet> expander, GameInfo info) {
        this.rootState = rootState;
        this.algConfig = algConfig;
        this.expander = expander;
        this.info = info;
    }

    public static void main(String[] args) {
        MatrixGenerator mg = runLR();
//        MatrixGenerator mg = runML();

        double[][] result = mg.createMatrix();

        System.out.println("Game-"+mg.numberedP1Actions.size()+"-"+mg.numberedP2Actions.size()+"-"+mg.leafUtilities.keySet().size()+"-"+(mg.numberedP1IS.size()+mg.numberedP2IS.size()));

        for (int i=0; i<result.length; i++) {
            for (int j=0; j<result[i].length; j++) {
                if (j>0) System.out.print(",");
                System.out.print(result[i][j]);
            }
            System.out.println();
        }

        MatrixToMINLP2 transformation = new MatrixToMINLP2(result, mg.numberedP1Actions.size(), mg.numberedP2Actions.size(), mg.leafUtilities.keySet().size(), (mg.numberedP1IS.size()+mg.numberedP2IS.size()));
        transformation.reduceYVars();
        transformation.replaceYHat();
        transformation.generateReformulationInequalities();
        transformation.generateKKTEQS();
        transformation.generateXEQs();
        transformation.saveMatrix();
    }

    public static MatrixGenerator runML() {
        GameState rootState = new MLGameState();
        GameInfo gameInfo = new MLGameInfo();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        Expander expander = new MLExpander(algConfig);
        MatrixGenerator matrixGenerator = new MatrixGenerator(rootState, algConfig, expander, gameInfo);
        matrixGenerator.generateCompleteGame();

        GambitEFG gambitEFG = new GambitEFG();
        gambitEFG.write("ML.gbt", rootState, expander);

        return matrixGenerator;
    }

    public static MatrixGenerator runLR() {
        GameState rootState = new LRGameState();
        GameInfo gameInfo = new LRGameInfo();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        Expander expander = new LRExpander(algConfig);
        MatrixGenerator matrixGenerator = new MatrixGenerator(rootState, algConfig, expander, gameInfo);
        matrixGenerator.generateCompleteGame();

//        GambitEFG gambitEFG = new GambitEFG();
//        gambitEFG.write("LR.gbt", rootState, expander);

        return matrixGenerator;
    }

    public double[][] createMatrix() {
        double[][] result = new double[numberedP1Actions.size() + numberedP2Actions.size()][leafUtilities.size()+numberedP1IS.size()+numberedP2IS.size()];

        int column = 0;
        for (GameState state : leafUtilities.keySet()) {
            int i;
            List<Action> p1Actions = state.getHistory().getSequenceOf(state.getAllPlayers()[0]).getAsList();
            for (i=0; i<numberedP1Actions.size(); i++) {
                Action a1 = numberedP1Actions.get(i);
                int position = p1Actions.indexOf(a1);
                if (position >= 0 && p1Actions.get(position).getInformationSet().equals(a1.getInformationSet()) && p1Actions.get(position).toString().equals(a1.toString())) {
                    result[i][column] = leafUtilities.get(state);
                }
            }

            int j;
            List<Action> p2Actions = state.getHistory().getSequenceOf(state.getAllPlayers()[1]).getAsList();
            for (j=0; j<numberedP2Actions.size(); j++) {
                Action a2 = numberedP2Actions.get(j);
                int position = p2Actions.indexOf(a2);
                if (position >= 0 && p2Actions.get(position).getInformationSet().equals(a2.getInformationSet()) && p2Actions.get(position).toString().equals(a2.toString())) {
                    result[j+numberedP1Actions.size()][column] = leafUtilities.get(state);
                }
            }
            column++;
        }

        for (InformationSet is : algConfig.getAllInformationSets().values()) {

            if (!numberedP1IS.contains(is) && !numberedP2IS.contains(is))
                continue;

            int i;
            for (i=0; i<numberedP1Actions.size(); i++) {
                Action a1 = numberedP1Actions.get(i);
                if (is.equals(a1.getInformationSet())) {
                    result[i][column] = 1;
                }
            }

            int j;
            for (j=0; j<numberedP2Actions.size(); j++) {
                Action a2 = numberedP2Actions.get(j);
                if (is.equals(a2.getInformationSet())) {
                    result[j+numberedP1Actions.size()][column] = 1;
                }
            }
            column++;
        }

        return result;
    }

    public void generateCompleteGame() {
        LinkedList<GameState> queue = new LinkedList<GameState>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
            boolean newIS = false;


            if (algConfig.getInformationSetFor(currentState) == null) {
                newIS = true;
            }

            algConfig.addStateToSequenceForm(currentState);

            if (currentState.isGameEnd()) {
                algConfig.setUtility(currentState);
                Double v = (algConfig.getActualNonzeroUtilityValues(currentState) == null) ? 0 : algConfig.getActualNonzeroUtilityValues(currentState);
                leafUtilities.put(currentState,v);
                continue;
            }

            if (newIS) {
                InformationSet is = algConfig.getInformationSetFor(currentState);
                //TODO missing nature
                if (currentState.getPlayerToMove().getId() == 0) {
                    numberedP1IS.add(is);
                } else if (currentState.getPlayerToMove().getId() == 1) {
                    numberedP2IS.add(is);
                } else {
                    throw new IllegalArgumentException("No Nature Implemented for Matrix Generator yet");
                }
            }

            for (Action action : expander.getActions(currentState)) {
                if (newIS) {
                    if (currentState.getPlayerToMove().getId() == 0) {
                        numberedP1Actions.add(action);
                    } else if (currentState.getPlayerToMove().getId() == 1) {
                        numberedP2Actions.add(action);
                    }
                }
                queue.add(currentState.performAction(action));
            }
        }
    }
}
