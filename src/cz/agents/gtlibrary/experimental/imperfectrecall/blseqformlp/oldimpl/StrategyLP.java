package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.oldimpl;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StrategyLP {

    private static StrategyLP instance = null;
    private LPTable table;
    private SequenceFormIRConfig config;
    private Map<Sequence, Set<Triplet<String, Sequence, Action>>> sequenceToVars;
    private Map<Sequence, Double> incomingRealizationProbs;
    private Set<Action> actions;
    private Pair<Action, Double> mostExpensiveActionCostPair;
    private Map<Action, Double> lbs;
    private Map<Action, Double> ubs;

    public static StrategyLP getInstance(SequenceFormIRConfig config) {
        if(instance == null)
            instance = new StrategyLP(config);
        return instance;
    }

    private StrategyLP(SequenceFormIRConfig config) {
        this.config = config;
        this.table = new LPTable();
        sequenceToVars = new HashMap<>();
        incomingRealizationProbs = new HashMap<>();
        actions = new HashSet<>();
        lbs = new HashMap<>();
        ubs = new HashMap<>();
    }

    public void clear() {
        sequenceToVars.clear();
        incomingRealizationProbs.clear();
        actions.clear();
        table.clearTable();
        lbs.clear();
        ubs.clear();
        mostExpensiveActionCostPair = null;
    }

    public void add(Sequence incomingSequence, Sequence outgoingSequence, double incomingSeqProb, double outgoingSeqProb) {
        if (incomingSeqProb < 1e-8) {
            assert outgoingSeqProb < 1e-8;
            return;
        }
        Action action = outgoingSequence.getLast();
        Triplet<Sequence, Action, Integer> eqKey = new Triplet<>(incomingSequence, action, 1);
        Triplet<String, Sequence, Action> varKey = new Triplet<>("L", incomingSequence, action);
        double behavStrat = outgoingSeqProb / incomingSeqProb;

        updateLBs(action, behavStrat);
        updateUBs(action, behavStrat);
        actions.add(action);
        updateSequenceToVars(incomingSequence, varKey);
        incomingRealizationProbs.put(incomingSequence, incomingSeqProb);

        assert !Double.isNaN(behavStrat * config.getHighestReachableUtilityFor(outgoingSequence));
        table.setConstraint(eqKey, varKey, 1);
        table.setConstraint(eqKey, action, config.getHighestReachableUtilityFor(outgoingSequence));
        table.setConstant(eqKey, behavStrat * config.getHighestReachableUtilityFor(outgoingSequence));
        table.setConstraintType(eqKey, 2);

        eqKey = new Triplet<>(incomingSequence, action, 2);

        assert !Double.isNaN(behavStrat * config.getLowestReachableUtilityFor(outgoingSequence));
        table.setConstraint(eqKey, varKey, 1);
        table.setConstraint(eqKey, action, config.getLowestReachableUtilityFor(outgoingSequence));
        table.setConstant(eqKey, behavStrat * config.getLowestReachableUtilityFor(outgoingSequence));
        table.setConstraintType(eqKey, 2);

        table.setLowerBound(action, 0);
        table.setUpperBound(action, 1);
        table.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        table.watchPrimalVariable(varKey, varKey);
    }

    private void updateLBs(Action action, double behavStrat) {
        Double lb = lbs.get(action);

        if (lb == null || lb > behavStrat)
            lbs.put(action, behavStrat);
    }

    private void updateUBs(Action action, double behavStrat) {
        Double ub = ubs.get(action);

        if (ub == null || ub < behavStrat)
            ubs.put(action, behavStrat);
    }

    private void updateSequenceToVars(Sequence incomingSequence, Triplet<String, Sequence, Action> varKey) {
        Set<Triplet<String, Sequence, Action>> sequenceVars = sequenceToVars.get(incomingSequence);

        if (sequenceVars == null)
            sequenceVars = new HashSet<>();
        sequenceVars.add(varKey);
        sequenceToVars.put(incomingSequence, sequenceVars);
    }

    public Map<Action, Double> getStartegy() {
        addVarContinuationConstraints();
        addObjective();
        addBehavSumConstraint();
        addLBConstraints();
        addUBConstraints();
        try {
            LPData lpData = table.toCplex();

            if (BilinearSeqenceFormBNB.SAVE_LPS) lpData.getSolver().exportModel("strategyLP.lp");
            lpData.getSolver().solve();
//            System.out.println(lpData.getSolver().getStatus());
            updateMostExpensiveActionCostPair(lpData);
            return extractStrategy(lpData);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addLBConstraints() {
        for (Map.Entry<Action, Double> entry : lbs.entrySet()) {
            Object eqKey = new Pair<>("lb", entry.getKey());

            table.setConstraint(eqKey, entry.getKey(), 1);
            table.setConstant(eqKey, entry.getValue());
            table.setConstraintType(eqKey, 2);
        }
    }

    private void addUBConstraints() {
        for (Map.Entry<Action, Double> entry : ubs.entrySet()) {
            Object eqKey = new Pair<>("ub", entry.getKey());

            table.setConstraint(eqKey, entry.getKey(), 1);
            table.setConstant(eqKey, entry.getValue());
            table.setConstraintType(eqKey, 0);
        }
    }

    private void updateMostExpensiveActionCostPair(LPData lpData) throws IloException {
        Map<Action, Double> actionCosts = new HashMap<>(actions.size());

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Triplet && ((Triplet) entry.getKey()).getFirst().equals("L")) {
                Triplet<String, Sequence, Action> triplet = (Triplet<String, Sequence, Action>) entry.getKey();
                Double currentValue = actionCosts.get(triplet.getThird());

                if (currentValue == null)
                    currentValue = 0d;
                currentValue += incomingRealizationProbs.get(triplet.getSecond()) *
                        lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(triplet)]);
                actionCosts.put(triplet.getThird(), currentValue);
            }
        }
        mostExpensiveActionCostPair = getMaxPair(actionCosts);
    }

    private Pair<Action, Double> getMaxPair(Map<Action, Double> actionCosts) {
        double max = Double.NEGATIVE_INFINITY;
        Action currentBest = null;

        for (Map.Entry<Action, Double> entry : actionCosts.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                currentBest = entry.getKey();
            }
        }
        return new Pair<>(currentBest, max);
    }

    private void addBehavSumConstraint() {
        for (Action action : actions) {
            table.setConstraint("sum", action, 1);
        }
        table.setConstraintType("sum", 1);
        table.setConstant("sum", 1);
    }

    private Map<Action, Double> extractStrategy(LPData lpData) throws IloException {
        Map<Action, Double> strategy = new HashMap<>();

        for (Action action : actions) {
            strategy.put(action, lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(action)]));
        }
        fixNumericalImprecision(strategy);
        return strategy;
    }

    private void fixNumericalImprecision(Map<Action, Double> strategy) {
        for (Action action : strategy.keySet()) {
            double value = strategy.get(action);
            double lb = lbs.get(action);
            double ub = ubs.get(action);
            if (Math.abs(ub - lb) < 1e-8)
                value = (lb + ub) / 2;
            else if (value < lbs.get(action))
                value = lbs.get(action) + 1e-8;
            else if (value > ubs.get(action))
                value = ubs.get(action) - 1e-8;
            strategy.put(action, value);
        }

    }

    private void addObjective() {
        for (Sequence sequence : sequenceToVars.keySet()) {
            Pair<String, Sequence> varKey = new Pair<>("L", sequence);

            table.addToObjective(varKey, -incomingRealizationProbs.get(sequence));
        }
    }

    private void addVarContinuationConstraints() {
        for (Map.Entry<Sequence, Set<Triplet<String, Sequence, Action>>> entry : sequenceToVars.entrySet()) {
            Pair<String, Sequence> key = new Pair<>("L", entry.getKey());

            table.setConstraint(key, key, 1);
            for (Triplet<String, Sequence, Action> varKey : entry.getValue()) {
                table.setConstraint(key, varKey, -1);
            }
            table.setConstraintType(key, 1);
            table.setLowerBound(key, Double.NEGATIVE_INFINITY);
        }

    }

    public Pair<Action, Double> getMostExpensiveActionCostPair() {
        return mostExpensiveActionCostPair;
    }
}
