package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.ArrayDeque;

public class GeneralSumGameBuilder {
    public static void build(GameState rootState, SequenceFormConfig<? extends SequenceInformationSet> algConfig, Expander<? extends SequenceInformationSet> expander) {
        ArrayDeque<GameState> queue = new ArrayDeque<>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();

            algConfig.addStateToSequenceForm(currentState);
            if (currentState.isGameEnd()) {
                algConfig.setUtility(currentState);
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
    }

    public static void buildWithUtilityShift(GameState rootState, GenSumSequenceFormConfig algConfig, Expander<? extends SequenceInformationSet> expander, double utilityShift) {
        ArrayDeque<GameState> queue = new ArrayDeque<>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();

            algConfig.addStateToSequenceForm(currentState);
            if (currentState.isGameEnd()) {
                Double[] utilities = new Double[2];
                double[] stateUtilities = currentState.getUtilities();

                for (int i = 0; i < utilities.length; i++) {
                    utilities[i] = (stateUtilities[i] + utilityShift) * currentState.getNatureProbability();
                    assert utilities[i] > 0;
                }
                algConfig.setUtility(currentState, utilities);
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
    }
}
