package cz.agents.gtlibrary.algorithms.bestresponse;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.RecyclingMILPTable;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oldimpl.BilinearSequenceFormBNB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImperfectRecallBestResponseImpl implements ImperfectRecallBestResponse {


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

    protected static Map<Action, Double> getOpponentStrategy(GameState root, BRTestExpander<IRInformationSetImpl> expander) {
        Map<Action, Double> strategy = new HashMap<>(2);

        GameState state = root.performAction(expander.getActions(root).get(0));
        
        state = state.performAction(expander.getActions(state).get(0));
        List<Action> actions = expander.getActions(state);

        strategy.put(actions.get(0), 0.6);
        strategy.put(actions.get(1), 0.4);
        return strategy;
    }

    protected Player player;
    protected Player opponent;
    protected SequenceFormIRConfig algConfig;
    protected RecyclingMILPTable milpTable;
    protected GameInfo info;
    protected Expander<SequenceFormIRInformationSet> expander;
    protected double value;

    public ImperfectRecallBestResponseImpl(Player player, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        this.player = player;
        this.opponent = info.getOpponent(player);
        this.info = info;
        this.expander = expander;
        algConfig = (SequenceFormIRConfig)(expander.getAlgorithmConfig());
        milpTable = new RecyclingMILPTable();
    }

    public Map<Action, Double> getBestResponse(Map<Action, Double> opponentStrategy) {
        milpTable.clearTable();
        addObjective(opponentStrategy);
        addStrategySumConstraints();
        addPUpperBounds();
        addPEquality(opponentStrategy);

        try {
            LPData lpData = milpTable.toCplex();

            if (BilinearSequenceFormBNB.SAVE_LPS) lpData.getSolver().exportModel("BRMILP.lp");
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

    protected Map<Action, Double> createStrategy(LPData lpData) {
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

    protected void addPEquality(Map<Action, Double> opponentStrategy) {
        for (GameState terminalState : algConfig.getTerminalStates()) {
            milpTable.setConstraint("pSum", terminalState, terminalState.getNatureProbability() * getProbability(terminalState, opponentStrategy));
        }
//        milpTable.setConstant("pSum", 1 - BilinearSeqenceFormLP.BILINEAR_PRECISION);
//        milpTable.setConstant("pSum", 1 - 0.1);
        milpTable.setConstant("pSum", 1 );
        milpTable.setConstraintType("pSum", 2);

    }

    protected void addPEqualitySequence(Map<Sequence, Double> opponentStrategy) {
        for (GameState terminalState : algConfig.getTerminalStates()) {
            milpTable.setConstraint("pSum", terminalState, terminalState.getNatureProbability() * getProbability(opponentStrategy, terminalState.getSequenceFor(opponent)));
        }
//        milpTable.setConstant("pSum", 1 - BilinearSeqenceFormLP.BILINEAR_PRECISION);
//        milpTable.setConstant("pSum", 1 - 0.1);
        milpTable.setConstant("pSum", 1 );
        milpTable.setConstraintType("pSum", 2);
    }

    protected void addPUpperBounds() {
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

    protected void addStrategySumConstraints() {
        algConfig.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(player)).filter(i -> !i.getActions().isEmpty()).forEach(informationSet -> {
            for (Action action : expander.getActions(informationSet)) {
                Object varKey = action;

                milpTable.setConstraint(informationSet, varKey, 1);
                milpTable.markAsBinary(varKey);
                milpTable.watchPrimalVariable(varKey, varKey);
            }
            milpTable.setConstant(informationSet, 1);
            milpTable.setConstraintType(informationSet, 1);
        });
    }

    protected void addObjective(Map<Action, Double> opponentStrategy) {
        for (GameState terminalState : algConfig.getTerminalStates()) {
            milpTable.setObjective(terminalState, getExpectedUtility(opponentStrategy, terminalState));
        }
    }

    protected void addObjectiveSequence(Map<Sequence, Double> opponentStrategy) {
        for (GameState terminalState : algConfig.getTerminalStates()) {
            milpTable.setObjective(terminalState, getExpectedUtilitySequence(opponentStrategy, terminalState));
        }
    }

    protected double getExpectedUtilitySequence(Map<Sequence, Double> opponentStrategy, GameState terminalState) {
        return terminalState.getNatureProbability() * getProbability(opponentStrategy, terminalState.getSequenceFor(opponent)) * terminalState.getUtilities()[player.getId()];
    }

    protected double getExpectedUtility(Map<Action, Double> opponentStrategy, GameState terminalState) {
        return terminalState.getNatureProbability() * getProbability(terminalState, opponentStrategy) * terminalState.getUtilities()[player.getId()];
    }

    protected Double getProbability(Map<Sequence, Double> opponentStrategy, Sequence sequence) {
        return opponentStrategy.get(sequence);
    }

    protected double getProbability(GameState terminalState, Map<Action, Double> opponentStrategy) {
        double probability = 1;

        for (Action action : terminalState.getSequenceFor(info.getOpponent(player))) {
            Double actionProbability = opponentStrategy.get(action);

            if (actionProbability == null || actionProbability < 1e-8)
                return 0;
            probability *= actionProbability;
        }
//        System.out.println(probability);
        return probability;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public long getExpandedNodes() {
        return 0;
    }
}
