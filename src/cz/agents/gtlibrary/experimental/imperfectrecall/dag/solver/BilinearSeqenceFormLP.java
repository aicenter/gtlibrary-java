package cz.agents.gtlibrary.experimental.imperfectrecall.dag.solver;

import cz.agents.gtlibrary.algorithms.bestresponse.ImperfectRecallBestResponseImpl;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oldimpl.BilinearTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.dag.DAGConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.dag.DAGInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.dag.rg.DAGRandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.dag.ttt.DAGTTTState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.concert.IloRange;

import java.util.*;

public class BilinearSeqenceFormLP {
//    private final Player player;
//    private final Player opponent;
//    private final GameState root;
//    private BilinearTable table;
//    private Expander expander;
//    private GameInfo gameInfo;
//    private Double finalValue = null;
//
//    private boolean DEBUG = false;
//    public static boolean SAVE_LPS = false;
//
//
//    static public final double BILINEAR_PRECISION = 0.0001;
//    private final double EPS = 0.000001;
//    private Set<GameState> finishedStates = new HashSet<>();
//
//    public static void main(String[] args) {
////        runTTT();
//        runRandomGame();
////        runBRTest();
//    }
//
//    public static double runRandomGame() {
//        BasicGameBuilder builder = new BasicGameBuilder();
//        DAGConfig config = new DAGConfig();
//        GameState root = new DAGRandomGameState(config);
//        Expander<DAGInformationSet> expander = new RandomGameExpander<>(config);
//
//        builder.build(root, config, expander);
//
////        if (config.isPlayer2IR()) {
////            System.out.println(" Player 2 has IR ... skipping ...");
////            return;
////        }
//
//        BilinearSeqenceFormLP solver = new BilinearSeqenceFormLP(root, BRTestGameInfo.FIRST_PLAYER, new RandomGameInfo());
//
//        solver.setExpander(expander);
//        solver.solve(config);
//
//        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
//        return solver.finalValue;
//
////        System.out.println("IR SETS");
////        for (SequenceFormIRInformationSet is : config.getAllInformationSets().values()) {
////            if (is.isHasIR()) {
////                System.out.println(is.getISKey());
////            }
////        }
//    }
//
////    private static void runBRTest() {
////        BasicGameBuilder builder = new BasicGameBuilder();
////        SequenceFormIRConfig config = new SequenceFormIRConfig();
////
////        builder.build(new BRTestGameState(), config, new BRTestExpander<>(config));
////        BilinearSeqenceFormLP solver = new BilinearSeqenceFormLP(BRTestGameInfo.FIRST_PLAYER, new BRTestGameInfo());
////
////        solver.solve(config);
////    }
//
//    private static void runTTT() {
//        BasicGameBuilder builder = new BasicGameBuilder();
//        DAGConfig config = new DAGConfig();
//        GameState root = new DAGTTTState(config);
//
//        builder.build(root, config, new TTTExpander<>(config));
//        System.out.println("Game build");
//        BilinearSeqenceFormLP solver = new BilinearSeqenceFormLP(root, TTTInfo.XPlayer, new BRTestGameInfo());
//
//        solver.solve(config);
//    }
//
//    public BilinearSeqenceFormLP(GameState root, Player player, GameInfo info) {
//        this.table = new BilinearTable();
//        this.player = player;
//        this.opponent = info.getOpponent(player);
//        this.gameInfo = info;
//        this.root = root;
//    }
//
//    private void addValidBehaviorConstraints(DAGConfig config) {
//        config.getAllInformationSets().values().stream().filter(informationSet -> informationSet.getPlayer().equals(player)).forEach(informationSet -> {
//            Object eqKey = new Pair<>("behav", informationSet);
//
//            for (Action action : informationSet.getActions()) {
//                table.setConstraint(eqKey, action, 1);
//            }
//            table.setConstraintType(eqKey, LPTable.ConstraintType.EQ);
//            table.setConstant(eqKey, 1);
//        });
//    }
//
//    public void solve(DAGConfig config) {
//        addObjective();
//        addBehaviorStrategyConstraints(config);
////        addRPConstraintsForMaxPlayer(config);
//        addRPConstraintsForAll(config);
//        addStateValueConstraintsForMaxPlayer(config);
//        addStateValueConstraintsForMinPlayer(config);
//        addValueConstraints(config);
////        addBilinearBehavRPConstraints(config);
//        addBinaryBehvioralConstraints(config);
//        addValidBehaviorConstraints(config);
//        System.out.println("table build");
//        try {
//            LPData lpData = table.toCplex();
//
//            if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQF.lp");
//            System.out.println("starting cplex");
//            lpData.getSolver().solve();
//            System.out.println("solved");
//            System.out.println(lpData.getSolver().getStatus());
//            System.out.println(lpData.getSolver().getObjValue());
//            double lastSolution = lpData.getSolver().getObjValue();
//
//            Set<Action> sequencesToTighten = findMostViolatedBilinearConstraints(lpData);
//            Map<Action, Double> P1Strategy = extractBehavioralStrategy(lpData);
//
//            while (!sequencesToTighten.isEmpty()) {
//
//                if (DEBUG) System.out.println(sequencesToTighten);
//
//                if (table.isFixPreviousDigits()) table.storeWValues(lpData);
//                for (Action s : sequencesToTighten) {
//                    table.refinePrecision(lpData, s);
//                }
////                System.out.println("----------------------------------------------------------------------------------------------------------");
//                if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQF.lp");
//                lpData.getSolver().solve();
//                System.out.println(lpData.getSolver().getObjValue());
//
////                P1Strategy = extractBehavioralStrategy(config, lpData);
////                System.out.println("-------------------\nP1 Actions " + P1Strategy);
//
//
////                if (Math.abs(lastSolution - lpData.getSolver().getObjValue()) < BILINEAR_PRECISION) {
////                    break;
////                } else {
////                    lastSolution = lpData.getSolver().getObjValue();
////                }
//                sequencesToTighten = findMostViolatedBilinearConstraints(lpData);
//            }
//            ImperfectRecallBestResponseImpl br = new ImperfectRecallBestResponseImpl(RandomGameInfo.SECOND_PLAYER, expander, gameInfo);
//
//            if (DEBUG) System.out.println("-------------------");
////            if (DEBUG) {
////                for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
////                    for (Map.Entry<Sequence, Set<Sequence>> entry : i.getOutgoingSequences().entrySet()) {
////                        Sequence s = entry.getKey();
////                        Object o = new Pair<>(i, s);
////                        if (table.exists(o)) {
////                            System.out.println(i + " = " + lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(o)]));
////                        }
////                    }
////                }
////            }
////            IRSequenceBestResponse br2 = new IRSequenceBestResponse(RandomGameInfo.SECOND_PLAYER, expander, gameInfo);
////            br2.getBestResponseSequence(extractRPStrategy(config, lpData));
//            br.getBestResponse(P1Strategy);
////            br.getBestResponseSequence(P1StrategySeq);
////            System.out.println("-------------------\nP1 Actions " + extractRPStrategy(config, lpData));
//            finalValue = -br.getValue();
////            System.out.println("NEW BR = " + br2.getValue());
//
////            finalValue = lpData.getSolver().getObjValue();
////            for (Sequence sequence : config.getSequencesFor(player)) {
////                System.out.println(sequence + ": " + lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(sequence)]));
////            }
//        } catch (IloException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private Map<Action, Double> extractBehavioralStrategy(LPData lpData) {
//        Map<Action, Double> behavStrategy = new HashMap<>();
//
//        try {
//            for (Map.Entry<Object, IloRange> entry : lpData.getWatchedDualVariables().entrySet()) {
//                if (entry instanceof Action) {
//                    behavStrategy.put((Action) entry.getKey(), lpData.getSolver().getValue(entry.getValue()));
//                }
//            }
//        } catch (IloException e) {
//            e.printStackTrace();
//        }
//        return behavStrategy;
//    }
//
//    private void addBinaryBehvioralConstraints(DAGConfig config) {
//        for (DAGInformationSet informationSet : config.getAllInformationSets().values()) {
//            if (informationSet.getPlayer().equals(gameInfo.getOpponent(player))) {
//                for (Action action : informationSet.getActions()) {
//                    Object eqKey = new Pair<>("behav-i", action);
//
//                    table.setConstraint(eqKey, action, 300);
//                    table.markAsBinary(action);
//                    table.setConstraint(eqKey, new Pair<>("s", action), 1);
//                    table.setConstant(eqKey, 300);
//                    table.setConstraintType(eqKey, LPTable.ConstraintType.LE);
//                }
//            }
//        }
//
//    }
//
//    private void addStateValueConstraintsForMinPlayer(DAGConfig config) {
//        for (Map.Entry<GameState, Map<Action, GameState>> entry : config.getSuccessors().entrySet()) {
//            if (!entry.getKey().getPlayerToMove().equals(gameInfo.getOpponent(player)))
//                continue;
//            Object key = new Pair<>("v", entry.getKey());
//
//            table.setConstraint(key, key, 1d);
//            table.setConstraintType(key, LPTable.ConstraintType.EQ);
//            table.setLowerBound(key, Double.NEGATIVE_INFINITY);
//            for (Map.Entry<Action, GameState> actionEntry : entry.getValue().entrySet()) {
//                Object binVarKey = actionEntry.getKey();
//
//                table.markAsBinary(binVarKey);
//                if (actionEntry.getValue().isGameEnd()) {
//                    table.setConstraint(key, binVarKey, -config.getActualNonzeroUtilityValues(actionEntry.getValue()));
//                    continue;
//                }
//                Object stateValVar = new Pair<>("v", entry.getValue());
//                Object productVar = new Pair<>(binVarKey, stateValVar);
//
//                addBinContProduct(productVar, binVarKey, stateValVar);
//                table.setLowerBound(stateValVar, Double.NEGATIVE_INFINITY);
//                table.setConstraint(key, productVar, -1d);
//            }
//        }
//    }
//
////    private Object getBinVar(GameState source, GameState destination) {
////        Sequence sourceOpponentSequence = source.getSequenceFor(gameInfo.getOpponent(player));
////        Sequence destOpponentSequence = destination.getSequenceFor(gameInfo.getOpponent(player));
////        assert sourceOpponentSequence.isPrefixOf(destOpponentSequence);
////        Pair<Sequence, Sequence> sourceDestinationPair = new Pair<>(sourceOpponentSequence, destOpponentSequence);
////
////        table.markAsBinary(sourceDestinationPair);
////        for (int i = sourceOpponentSequence.size() - 1; i < destOpponentSequence.size(); i++) {
////            Object eqKey = new Pair<>(sourceDestinationPair, i);
////            table.setConstraint(eqKey, sourceDestinationPair, 1);
////            table.setConstraintType(eqKey, LPTable.ConstraintType.LE);
////            table.setConstraint(eqKey, new Pair<>("b", destOpponentSequence.get(i)), -1);
////        }
////        return sourceDestinationPair;
////    }
//
//    private void addBinContProduct(Object productVar, Object binVarKey, Object stateValVar) {
//        Object eqKey = new Pair<>(0, productVar);
//
//        table.setConstraint(eqKey, productVar, 1d);
//        table.setConstraintType(eqKey, LPTable.ConstraintType.LE);
//        table.setConstraint(eqKey, stateValVar, -1d);
//        table.setConstraint(eqKey, binVarKey, 300);
//
//        eqKey = new Pair<>(1, productVar);
//
//        table.setConstraint(eqKey, productVar, 1d);
//        table.setConstraintType(eqKey, LPTable.ConstraintType.GE);
//        table.setConstraint(eqKey, stateValVar, -1d);
//        table.setConstraint(eqKey, binVarKey, -300);
//    }
//
//    private void addStateValueConstraintsForMaxPlayer(DAGConfig config) {
//        for (Map.Entry<GameState, Map<Action, GameState>> entry : config.getSuccessors().entrySet()) {
//            if (!entry.getKey().getPlayerToMove().equals(player))
//                continue;
//            Object key = new Pair<>("v", entry.getKey());
//
//            table.setConstraint(key, key, 1d);
//            table.setLowerBound(key, Double.NEGATIVE_INFINITY);
//            table.setConstraintType(key, LPTable.ConstraintType.EQ);
//            for (Map.Entry<Action, GameState> actionEntry : entry.getValue().entrySet()) {
//                Object behavKey = actionEntry.getKey();
//
//                if (actionEntry.getValue().isGameEnd()) {
//                    table.setConstraint(key, behavKey, -config.getActualNonzeroUtilityValues(actionEntry.getValue()));
//                    continue;
//                }
//                Object succKey = new Pair<>("v", actionEntry.getValue());
//                Object bilinKey = new Pair<>(behavKey, succKey);
//
//                table.setLowerBound(succKey, Double.NEGATIVE_INFINITY);
//                table.setConstraint(key, bilinKey, -1d);
//                table.markAsBilinear(bilinKey, behavKey, succKey);
//            }
//        }
//    }
//
//    private void addBilinearBehavRPConstraints(DAGConfig config) {
//        config.getSuccessors().entrySet().stream().filter(entry -> entry.getKey().getPlayerToMove().equals(player)).forEach(entry -> {
//            for (Action action : entry.getValue().keySet()) {
//                Object key = new Triplet<>("p", entry.getKey(), action);
//                Object behavKey = action;
//                Object stateKey = entry.getKey();
//                Object productKey = new Pair<>(stateKey, behavKey);
//
//                table.setConstraint(key, key, 1);
//                table.setConstraintType(key, LPTable.ConstraintType.EQ);
//                table.setConstraint(key, productKey, -1);
//                table.markAsBilinear(productKey, behavKey, stateKey);
//                table.setUpperBound(behavKey, 1);
//                table.setUpperBound(stateKey, 1);
//                table.setUpperBound(productKey, 1);
//            }
//        });
//    }
//
//    private void addValueConstraints(DAGConfig config) {
//        for (DAGInformationSet informationSet : config.getAllInformationSets().values()) {
//            if (!informationSet.getPlayer().equals(gameInfo.getOpponent(player)))
//                continue;
//            Object infsetVarKey = new Pair<>("v", informationSet);
//
//            for (Action action : informationSet.getActions()) {
//                Object eqKey = new Pair<>(informationSet, action);
//
//                table.setConstraint(eqKey, infsetVarKey, 1);
//                table.setConstraintType(eqKey, LPTable.ConstraintType.EQ);
//                table.setLowerBound(infsetVarKey, Double.NEGATIVE_INFINITY);
//                for (GameState gameState : informationSet.getAllStates()) {
//                    Object stateProbVar = new Pair<>("p", gameState);
//                    GameState successor = config.getSuccessor(gameState, action);
//
//                    if(successor.isGameEnd()) {
//                        table.setConstraint(eqKey, stateProbVar, config.getActualNonzeroUtilityValues(successor));
//                    } else {
//                        Object contValueVar = new Pair<>("v", successor);
//                        Object productVar = new Pair<>(stateProbVar, contValueVar);
//
//                        table.markAsBilinear(productVar, stateProbVar, contValueVar);
//                        table.setConstraint(eqKey, productVar, -1);
//                    }
//                    table.setConstraint(eqKey, new Pair<>("s", action), -1);
//                }
//            }
//        }
//
////        for (Sequence sequence : config.getSequencesFor(opponent)) {
////            Object eqKey;
////            Object informationSet;
////            Sequence subsequence;
////
////            if (sequence.isEmpty()) {
////                eqKey = "v_init";
////                informationSet = "root";
////                subsequence = new ArrayListSequenceImpl(opponent);
////            } else {
////                subsequence = sequence.getSubSequence(sequence.size() - 1);
////                informationSet = sequence.getLastInformationSet();
////                eqKey = new Triplet<>(informationSet, subsequence, sequence.getLast());
////            }
////            Object varKey = new Pair<>(informationSet, subsequence);
////
////            table.setConstraint(eqKey, varKey, 1);
////            table.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
////            for (SequenceFormIRInformationSet reachableSet : config.getReachableSets(sequence)) {
////                if (!reachableSet.getActions().isEmpty() && reachableSet.getOutgoingSequences().get(sequence) != null && !reachableSet.getOutgoingSequences().get(sequence).isEmpty() && reachableSet.getPlayer().equals(opponent))
////                    table.setConstraint(eqKey, new Pair<>(reachableSet, sequence), -1);
////            }
////            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(sequence)) {
////                Double utility = config.getUtilityFor(sequence, compatibleSequence);
////
////                if (utility != null)
////                    table.setConstraint(eqKey, compatibleSequence, -utility);
////            }
////        }
//    }
//
//    private void addBehaviorStrategyConstraints(DAGConfig config) {
//        config.getAllInformationSets().values().stream().filter(informationSet -> informationSet.getPlayer().equals(player)).forEach(informationSet -> {
//            for (Action action : informationSet.getActions()) {
//                table.setConstraint(informationSet, action, 1);
//                table.setLowerBound(action, 0);
//                table.setUpperBound(action, 1);
//            }
//            table.setConstant(informationSet, 1);
//            table.setConstraintType(informationSet, 1);
//        });
//    }
//
//    private void addRPConstraintsForMaxPlayer(DAGConfig config) {
//        config.getSuccessors().entrySet().stream().filter(entry -> entry.getKey().getPlayerToMove().equals(player)).forEach(entry -> {
//            Object stateKey = new Pair<>("p", entry.getKey());
//
//            table.setConstraint(stateKey, stateKey, 1d);
//            table.setUpperBound(stateKey, 1d);
//            table.setLowerBound(stateKey, 0d);
//            table.setConstraintType(stateKey, LPTable.ConstraintType.EQ);
//            for (Map.Entry<Action, GameState> actionEntry : entry.getValue().entrySet()) {
//                Object actionKey = new Triplet<>("p", entry.getKey(), actionEntry.getKey());
//
//                table.setConstraint(stateKey, actionKey, -1d);
//                table.setUpperBound(actionKey, 1d);
//                table.setLowerBound(actionKey, 0d);
//            }
//        });
//    }
//
//    private void addRPConstraintsForAll(DAGConfig config) {
//        Pair<String, GameState> rootKey = new Pair<>("p", root);
//
//        table.setConstraint(rootKey, rootKey, 1);
//        table.setConstant(rootKey, 1);
//        table.setConstraintType(rootKey, LPTable.ConstraintType.EQ);
//        table.setUpperBound(rootKey, 1d);
//        table.setLowerBound(rootKey, 0d);
//
//        for (Map.Entry<GameState, Map<Action, GameState>> entry : config.getSuccessors().entrySet()) {
//            if (entry.getKey().getPlayerToMove().equals(player)) {
//                Object parentKey = new Pair<>("p", entry.getKey());
//
//                for (Map.Entry<Action, GameState> actionEntry : entry.getValue().entrySet()) {
//                    Object key = new Pair<>("p", actionEntry.getValue());
//
//                    if (!finishedStates.contains(actionEntry.getValue())) {
//                        finishedStates.add(actionEntry.getValue());
//                        table.setConstraint(key, key, 1);
//                        table.setConstraintType(key, LPTable.ConstraintType.EQ);
//                    }
//                    Object behavKey = actionEntry.getKey();
//                    Object product = new Pair<>(behavKey, parentKey);
//
//                    table.setConstraint(key, product, -1);
//                    table.markAsBilinear(product, behavKey, parentKey);
//                }
//            } else if (entry.getKey().getPlayerToMove().equals(gameInfo.getOpponent(player))) {
//                Object parentKey = new Pair<>("p", entry.getKey());
//
//                for (Map.Entry<Action, GameState> actionEntry : entry.getValue().entrySet()) {
//                    Object key = new Pair<>("p", actionEntry.getValue());
//
//                    if (!finishedStates.contains(actionEntry.getValue())) {
//                        finishedStates.add(actionEntry.getValue());
//                        table.setConstraint(key, key, 1);
//                        table.setConstraintType(key, LPTable.ConstraintType.EQ);
//                    }
//                    Object behavKey = actionEntry.getKey();
//                    Object binProduct = new Pair<>(behavKey, parentKey);
//
//                    table.setConstraint(key, binProduct, -1);
//                    table.markAsBinary(binProduct);
//                    addBinProbProduct(binProduct, behavKey, parentKey);
//                }
//            }
//        }
//    }
//
//    private void addBinProbProduct(Object binProduct, Object behavKey, Object parentKey) {
//        Object eqKey = new Pair<>(0, binProduct);
//
//        table.setConstraint(eqKey, binProduct, 1);
//        table.setConstraint(eqKey, behavKey, -1);
//        table.setConstraintType(eqKey, LPTable.ConstraintType.LE);
//        eqKey = new Pair<>(1, binProduct);
//
//        table.setConstraint(eqKey, binProduct, 1);
//        table.setConstraint(eqKey, parentKey, -1);
//        table.setConstraintType(eqKey, LPTable.ConstraintType.LE);
//    }
//
//    private void addRPVarBounds(SequenceFormIRConfig config) {
//        for (Sequence sequence : config.getSequencesFor(player)) {
//            table.setLowerBound(sequence, 0);
//            table.setUpperBound(sequence, 1);
//        }
//    }
//
//    private void addRPConstraint(SequenceFormIRInformationSet informationSet) {
//        for (Map.Entry<Sequence, Set<Sequence>> outgoingEntry : informationSet.getOutgoingSequences().entrySet()) {
//            Object eqKey = new Pair<>(informationSet, outgoingEntry.getKey());
//
//            table.setConstraint(eqKey, outgoingEntry.getKey(), 1);
//            table.setConstraintType(eqKey, 1);
//            for (Sequence sequence : outgoingEntry.getValue()) {
//                table.setConstraint(eqKey, sequence, -1);
//            }
//        }
//    }
//
//    private void addObjective() {
//        table.setObjective(new Pair<>("v", root), 1);
//    }
//
//    private Set<Action> findMostViolatedBilinearConstraints(LPData data) throws IloException {
//        HashSet<Action> result = new HashSet<>();
//        HashSet<Action> result2 = new HashSet<>();
//
//        for (Object productSequence : table.getBilinearVars().keySet()) {
//            Object sequence = table.getBilinearVars().get(productSequence).getLeft();
//            Object action = table.getBilinearVars().get(productSequence).getRight();
//
//            if (data.getSolver().getValue(table.getDeltaBehavioralVariables().get(action)) > BILINEAR_PRECISION) {
//                if (DEBUG)
//                    System.out.println("X DELTA " + action + " = " + data.getSolver().getValue(table.getDeltaBehavioralVariables().get(action)));
//                result.add((Action) action);
//            }
//
//            if (data.getSolver().getValue(table.getDeltaSequenceVariables().get(productSequence)) > BILINEAR_PRECISION) {
//                if (DEBUG)
//                    System.out.println("SEQ DELTA " + action + " = " + data.getSolver().getValue(table.getDeltaSequenceVariables().get(productSequence)));
//                result.add((Action) action);
//            }
//        }
//
////        if (1+1 == 2)
////            return result;
//
//        double maxDifference = Double.NEGATIVE_INFINITY;
//
//        for (Action a : result) {
//            SequenceFormIRInformationSet is = (SequenceFormIRInformationSet) a.getInformationSet();
//            double average = 0;
//            ArrayList<Double> specValues = new ArrayList<>();
//
//            for (Map.Entry<Sequence, Set<Sequence>> entry : is.getOutgoingSequences().entrySet()) {
//                if (data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]) > 0) {
//                    Sequence productSequence = new ArrayListSequenceImpl(entry.getKey());
//                    productSequence.addLast(a);
//                    double sV = data.getSolver().getValue(data.getVariables()[table.getVariableIndex(productSequence)]);
//                    sV = sV / data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]);
//                    average += sV;
//                    specValues.add(sV);
//                }
//            }
//            if (specValues.size() == 0) average = 0;
//            else average = average / specValues.size();
//
//            double error = 0;
//            for (double d : specValues) {
//                error += Math.abs(average - d);
//            }
//
//            if (error > maxDifference) {
//                result2.clear();
//                result2.add(a);
//                maxDifference = error;
//            }
//
////            if (error > maxDifference || error > 1e-4) {
////                result2.add(a);
////                maxDifference = error;
////            }
//        }
//
//        return result2;
//    }
//
//    public Expander getExpander() {
//        return expander;
//    }
//
//    public void setExpander(Expander expander) {
//        this.expander = expander;
//    }
//
//    public Map<Action, Double> extractBehavioralStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException {
//        if (DEBUG) System.out.println("----- P1 Actions -----");
//        Map<Action, Double> P1Strategy = new HashMap<>();
//        for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
//            if (!i.getPlayer().equals(player)) continue;
//            boolean allZero = true;
//            for (Action a : i.getActions()) {
//                double average = 0;
//                int count = 0;
//                for (Sequence subS : i.getOutgoingSequences().keySet()) {
//                    Sequence s = new ArrayListSequenceImpl(subS);
//                    s.addLast(a);
//
//                    if (lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]) > 0) {
//                        double sV = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
//                        sV = sV / lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]);
//                        average += sV;
//                        count++;
//                    }
//                }
//                if (count == 0) average = 0;
//                else average = average / count;
//
//                if (DEBUG) System.out.println(a + " = " + average);
//                P1Strategy.put(a, average);
//
//                if (average > 0) allZero = false;
//            }
//            if (allZero && i.getActions().size() > 0) {
//                P1Strategy.put(i.getActions().iterator().next(), 1d);
//            }
//        }
//        return P1Strategy;
//    }
//
//    public Map<Sequence, Double> extractRPStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException {
//        Map<Sequence, Double> P1StrategySeq = new HashMap<>();
//        for (Sequence s : config.getSequencesFor(player)) {
//            if (s.isEmpty()) continue;
//            Action a = s.getLast();
//            double seqValue = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
//            if (DEBUG) System.out.println(s + " = " + seqValue);
//
//            P1StrategySeq.put(s, lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]));
//        }
//        return P1StrategySeq;
//    }

}
