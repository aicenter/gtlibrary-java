package cz.agents.gtlibrary.algorithms.bestresponse;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.MILPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.iinodes.ImperfectRecallAlgorithmConfig;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImperfectRecallBestResponse {


    public static void main(String[] args) {
        GameState root = new BRTestGameState();
//        BRImperfectRecallAlgorithmConfig config = new BRImperfectRecallAlgorithmConfig();
//        BRTestExpander<IRInformationSetImpl> expander = new BRTestExpander<>(config);
//
//        BasicGameBuilder.build(root, config, expander);
//        ImperfectRecallBestResponse br = new ImperfectRecallBestResponse(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());
//
//        System.out.println(br.getBestResponse(getOpponentStrategy(root, expander)));
    }

    private static Map<Action, Double> getOpponentStrategy(GameState root, BRTestExpander<IRInformationSetImpl> expander) {
        Map<Action, Double> strategy = new HashMap<>(2);

        GameState state = root.performAction(expander.getActions(root).get(0));
        
        state = state.performAction(expander.getActions(state).get(0));
        List<Action> actions = expander.getActions(state);

        strategy.put(actions.get(0), 0.6);
        strategy.put(actions.get(1), 0.4);
        return strategy;
    }

    private Player player;
    private Player opponent;
    private SequenceFormIRConfig algConfig;
    private MILPTable milpTable;
    private GameInfo info;
    private Expander<SequenceFormIRInformationSet> expander;
    private double value;

    public ImperfectRecallBestResponse(Player player, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        this.player = player;
        this.opponent = info.getOpponent(player);
        this.info = info;
        this.expander = expander;
        algConfig = (SequenceFormIRConfig)(expander.getAlgorithmConfig());
        milpTable = new MILPTable();
    }

    public Map<Action, Double> getBestResponse(Map<Action, Double> opponentStrategy) {
        milpTable.clearTable();
        addObjective(opponentStrategy);
        addStrategySumConstraints();
        addPUpperBounds();
        addPEquality(opponentStrategy);

        try {
            LPData lpData = milpTable.toCplex();

            lpData.getSolver().exportModel("BRMILP.lp");
            lpData.getSolver().solve();
            setValue(lpData.getSolver().getObjValue());

            return createStrategy(lpData);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<Action, Double> getBestResponseSequence(Map<Sequence, Double> opponentStrategy) {
        milpTable.clearTable();
        addObjectiveSequence(opponentStrategy);
        addStrategySumConstraints();
        addPUpperBounds();
        addPEqualitySequence(opponentStrategy);

        try {
            LPData lpData = milpTable.toCplex();

            lpData.getSolver().exportModel("BRMILP.lp");
            lpData.getSolver().solve();
            setValue(lpData.getSolver().getObjValue());

            return createStrategy(lpData);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<Action, Double> createStrategy(LPData lpData) {
        Map<Action, Double> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            try {
                strategy.put((Action) entry.getKey(), lpData.getSolver().getValue(entry.getValue()));
            } catch (IloException e) {
                e.printStackTrace();
            }
        }
        return strategy;
    }

    private void addPEquality(Map<Action, Double> opponentStrategy) {
        for (GameState terminalState : algConfig.getTerminalStates()) {
            milpTable.setConstraint("pSum", terminalState, terminalState.getNatureProbability() * getProbability(terminalState, opponentStrategy));
        }
        milpTable.setConstant("pSum", 1);
        milpTable.setConstraintType("pSum", 1);
    }

    private void addPEqualitySequence(Map<Sequence, Double> opponentStrategy) {
        for (GameState terminalState : algConfig.getTerminalStates()) {
            milpTable.setConstraint("pSum", terminalState, terminalState.getNatureProbability() * opponentStrategy.get(terminalState.getSequenceFor(opponent)));
        }
        milpTable.setConstant("pSum", 1);
        milpTable.setConstraintType("pSum", 1);
    }

    private void addPUpperBounds() {
        for (GameState terminalState : algConfig.getTerminalStates()) {
            for (Action action : terminalState.getSequenceFor(player)) {
                Object pVar = terminalState;
                Object yVar = action;
                Object eqKey = new Pair<>(action, terminalState);

                milpTable.setConstraint(eqKey, pVar, 1);
                milpTable.setConstraint(eqKey, yVar, -1);
                milpTable.markAsBinary(pVar);
            }
        }

    }

    private void addStrategySumConstraints() {
        for (SequenceFormIRInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(player)) {
                for (Action action : expander.getActions(informationSet)) {
                    Object varKey = action;

                    milpTable.setConstraint(informationSet, varKey, 1);
                    milpTable.markAsBinary(varKey);
                    milpTable.watchPrimalVariable(varKey, varKey);
                }
                milpTable.setConstant(informationSet, 1);
                milpTable.setConstraintType(informationSet, 1);
            }
        }
    }

    private void addObjective(Map<Action, Double> opponentStrategy) {
        for (GameState terminalState : algConfig.getTerminalStates()) {
            milpTable.setObjective(terminalState, getExpectedUtility(opponentStrategy, terminalState));
        }
    }

    private void addObjectiveSequence(Map<Sequence, Double> opponentStrategy) {
        for (GameState terminalState : algConfig.getTerminalStates()) {
            milpTable.setObjective(terminalState, getExpectedUtilitySequence(opponentStrategy, terminalState));
        }
    }

    private double getExpectedUtilitySequence(Map<Sequence, Double> opponentStrategy, GameState terminalState) {
        return terminalState.getNatureProbability() * opponentStrategy.get(terminalState.getSequenceFor(opponent)) * terminalState.getUtilities()[player.getId()];
    }

    private double getExpectedUtility(Map<Action, Double> opponentStrategy, GameState terminalState) {
        return terminalState.getNatureProbability() * getProbability(terminalState, opponentStrategy) * terminalState.getUtilities()[player.getId()];
    }

    private double getProbability(GameState terminalState, Map<Action, Double> opponentStrategy) {
        double probability = 1;

        for (Action action : terminalState.getSequenceFor(info.getOpponent(player))) {
            Double actionProbability = opponentStrategy.get(action);

            if (actionProbability == null || actionProbability < 1e-8)
                return 0;
            probability *= actionProbability;
        }
        return probability;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
