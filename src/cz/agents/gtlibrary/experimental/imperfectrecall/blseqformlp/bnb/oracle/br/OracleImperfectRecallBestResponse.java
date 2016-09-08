package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br;

import cz.agents.gtlibrary.algorithms.bestresponse.ImperfectRecallBestResponseImpl;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OracleImperfectRecallBestResponse extends ImperfectRecallBestResponseImpl {

    private boolean built = false;
    private Set<Pair<Action, String>> enforcedActionsEqKeys;

    public OracleImperfectRecallBestResponse(Player player, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        super(player, expander, info);
        enforcedActionsEqKeys = new HashSet<>();
    }

    @Override
    public Map<Action, Double> getBestResponse(Map<Action, Double> opponentStrategy) {
        if (!built) {
            addStrategySumConstraints();
            addPUpperBounds();
            built = true;
        }
        redoObjective(opponentStrategy);
        redoPEquality(opponentStrategy);
        try {
            LPData lpData = milpTable.toCplex();

            if (OracleBilinearSequenceFormBnB.SAVE_LPS) lpData.getSolver().exportModel("BRMILP.lp");
            lpData.getSolver().solve();
            setValue(lpData.getSolver().getObjValue());

            return createStrategy(lpData);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<Action, Double> getBestResponseSequence(Map<Sequence, Double> opponentStrategy) {
        if (!built) {
            addStrategySumConstraints();
            addPUpperBounds();
            built = true;
        }
        redoObjectiveSequence(opponentStrategy);
        redoPEqualitySequence(opponentStrategy);
        try {
            LPData lpData = milpTable.toCplex();

            if (OracleBilinearSequenceFormBnB.SAVE_LPS) lpData.getSolver().exportModel("BRMILP.lp");
            lpData.getSolver().solve();
            setValue(lpData.getSolver().getObjValue());

            return createStrategy(lpData);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<Action, Double> getBestResponseSequenceIn(GameState state, Map<Sequence, Double> opponentStrategy) {
        removePreviousEnforcements();
        if (!built) {
            addStrategySumConstraints();
            addPUpperBounds();
            built = true;
        }
        forceStateToBeReached(state);
        redoObjectiveSequence(state, opponentStrategy);
        redoPEqualitySequence(opponentStrategy);
        try {
            LPData lpData = milpTable.toCplex();

            if (OracleBilinearSequenceFormBnB.SAVE_LPS) lpData.getSolver().exportModel("BRMILP.lp");
            lpData.getSolver().solve();
            setValue(lpData.getSolver().getObjValue());

            return createStrategy(lpData);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<Action, Double> getBestResponseIn(GameState state, Map<Action, Double> opponentStrategy) {
        removePreviousEnforcements();
        if (!built) {
            addStrategySumConstraints();
            addPUpperBounds();
            built = true;
        }
        forceStateToBeReached(state);
        redoObjective(state, opponentStrategy);
        redoPEquality(opponentStrategy);
        try {
            LPData lpData = milpTable.toCplex();

            if (OracleBilinearSequenceFormBnB.SAVE_LPS) lpData.getSolver().exportModel("BRMILP.lp");
            lpData.getSolver().solve();
            setValue(lpData.getSolver().getObjValue());

            return createStrategy(lpData);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void removePreviousEnforcements() {
        for (Pair<Action, String> enforcedActionsEqKey : enforcedActionsEqKeys) {
            milpTable.deleteConstraint(enforcedActionsEqKey);
        }
    }

    private void forceStateToBeReached(GameState state) {
        enforcedActionsEqKeys.clear();
        for (Action action : state.getSequenceFor(player)) {
            Pair<Action, String> eqKey = new Pair<>(action, "force");

            milpTable.setConstraint(eqKey, action, 1);
            milpTable.setConstant(eqKey, 1);
            milpTable.setConstraintType(eqKey, LPTable.ConstraintType.EQ);
            enforcedActionsEqKeys.add(eqKey);
        }
    }

    private void redoObjectiveSequence(Map<Sequence, Double> opponentStrategy) {
        milpTable.removeObjective();
        addObjectiveSequence(opponentStrategy);
    }

    private void redoObjectiveSequence(GameState state, Map<Sequence, Double> opponentStrategy) {
        milpTable.removeObjective();
        addObjectiveSequence(state, opponentStrategy);
    }

    private void redoObjective(GameState state, Map<Action, Double> opponentStrategy) {
        milpTable.removeObjective();
        addObjective(state, opponentStrategy);
    }

    protected void addObjectiveSequence(GameState state, Map<Sequence, Double> opponentStrategy) {
        algConfig.getTerminalStates().stream()
                .filter(terminalState -> isReachable(terminalState, state))
                .forEach(terminalState -> milpTable.setObjective(terminalState, getExpectedUtilitySequence(opponentStrategy, terminalState)));
    }

    protected void addObjective(GameState state, Map<Action, Double> opponentStrategy) {
        algConfig.getTerminalStates().stream()
                .filter(terminalState -> isReachable(terminalState, state))
                .forEach(terminalState -> milpTable.setObjective(terminalState, getExpectedUtility(opponentStrategy, terminalState)));
    }


    private boolean isReachable(GameState terminalState, GameState state) {
        return state.getSequenceFor(player).isPrefixOf(terminalState.getSequenceFor(player)) &&
                state.getSequenceFor(opponent).isPrefixOf(terminalState.getSequenceFor(opponent));
    }

    private void redoPEqualitySequence(Map<Sequence, Double> opponentStrategy) {
        milpTable.deleteConstraint("pSum");
        addPEqualitySequence(opponentStrategy);
    }

    private void redoPEquality(Map<Action, Double> opponentStrategy) {
        milpTable.deleteConstraint("pSum");
        addPEquality(opponentStrategy);
    }

    private void redoObjective(Map<Action, Double> opponentStrategy) {
        milpTable.removeObjective();
        addObjective(opponentStrategy);
    }

    /**
     * Reimplementation allowing for partial startegy, default strategy playing first available action is assumed in the rest of the tree
     *
     * @param opponentStrategy
     * @param sequence
     * @return
     */
    protected Double getProbability(Map<Sequence, Double> opponentStrategy, Sequence sequence) {
        double sequenceProbability = opponentStrategy.getOrDefault(sequence, 0d);
        Sequence lastPrefix = new ArrayListSequenceImpl(sequence.getPlayer());

        if (sequenceProbability > 0)
            return sequenceProbability;
        sequenceProbability = 1;
        for (Sequence prefix : sequence.getAllPrefixesArray()) {
            double probability = opponentStrategy.getOrDefault(prefix, 0d);

            if (probability > 0) {
                sequenceProbability = probability;
            } else if (!prefix.isEmpty()) {
                SequenceFormIRInformationSet informationSet = (SequenceFormIRInformationSet) prefix.getLastInformationSet();
                Set<Sequence> sameLevelSequences = informationSet.getOutgoingSequencesFor(lastPrefix);

//                assert informationSet.getOutgoingSequences().entrySet().stream().map(entry -> entry.getValue().iterator().next().getLast()).collect(Collectors.toSet()).size() == 1;
                if (sameLevelSequences.stream().anyMatch(s -> opponentStrategy.getOrDefault(s, 0d) > 0) || !isFirst(prefix.getLast(), informationSet))
                    return 0d;
            }
            lastPrefix = prefix;
        }
        return sequenceProbability;
    }

    /**
     * Reimplementation allowing for partial strategy, default strategy playing first available action is assumed in the rest of the tree
     *
     * @param opponentStrategy
     * @param terminalState
     * @return
     */
    protected double getProbability(GameState terminalState, Map<Action, Double> opponentStrategy) {
        double probability = 1;

        for (Action action : terminalState.getSequenceFor(info.getOpponent(player))) {
            double actionProbability = opponentStrategy.getOrDefault(action, 0d);

            if (actionProbability < 1e-8) {
                SequenceFormIRInformationSet informationSet = (SequenceFormIRInformationSet) action.getInformationSet();
                Set<Sequence> sameLevelSequences = informationSet.getOutgoingSequences().values().iterator().next();

//                assert informationSet.getOutgoingSequences().entrySet().stream().map(entry -> entry.getValue().iterator().next()).collect(Collectors.toSet()).size() == 1;
                if (sameLevelSequences.stream().anyMatch(s -> opponentStrategy.getOrDefault(s.getLast(), 0d) > 0) || !isFirst(action, informationSet))
                    return 0d;
            } else {
                probability *= actionProbability;
            }
        }
        return probability;
    }

    private boolean isFirst(Action action, SequenceFormIRInformationSet informationSet) {
        return action.equals(informationSet.getFirst());
    }
}
