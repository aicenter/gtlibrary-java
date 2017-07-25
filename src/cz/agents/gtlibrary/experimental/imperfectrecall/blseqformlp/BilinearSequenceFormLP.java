package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.bestresponse.ImperfectRecallBestResponseImpl;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.imperfectrecall.IRBPGGameState;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.ir.IROshiZumoGameState;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionExpander;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionGameInfo;
import cz.agents.gtlibrary.domain.randomabstraction.P1RandomAbstractionGameStateFactory;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oldimpl.BilinearTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleIRConfig;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import ilog.concert.IloException;

import java.util.*;

public class BilinearSequenceFormLP {
    private final Player player;
    private final Player opponent;
    private cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oldimpl.BilinearTable table;
    private Expander expander;
    private GameInfo gameInfo;
    private Double finalValue = null;

    private boolean DEBUG = false;
    public static boolean SAVE_LPS = false;


    static public final double BILINEAR_PRECISION = 0.0001;
    private final double EPS = 0.000001;

    public static void main(String[] args) {
//        runAbstractedRandomGame();
//        runRandomGame();
        runBPG();
//        runBRTest();
//        runOZ();
    }

    public static double runAbstractedRandomGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> wrappedConfig = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(wrappedConfig);
        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), wrappedConfig);
        efg.generateCompleteGame();

        DoubleOracleIRConfig config = new DoubleOracleIRConfig(new RandomAbstractionGameInfo(new RandomGameInfo()));
        GameState root = new P1RandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<SequenceFormIRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, config);
        BilinearSequenceFormLP solver = new BilinearSequenceFormLP(RandomGameInfo.FIRST_PLAYER, new RandomAbstractionGameInfo(new RandomGameInfo()));

        cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);

        solver.solve(config);
        System.out.println("IS count: " + config.getAllInformationSets().size());
        System.out.println("Sequence count: " + config.getSequencesFor(BPGGameInfo.DEFENDER).size() + ", " + config.getSequencesFor(BPGGameInfo.ATTACKER).size());

        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        return solver.finalValue;
    }

    public static double runRandomGame() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new RandomGameInfo());
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

        builder.build(root, config, expander);

//        if (config.isPlayer2IR()) {
//            System.out.println(" Player 2 has IR ... skipping ...");
//            return;
//        }

        BilinearSequenceFormLP solver = new BilinearSequenceFormLP(BRTestGameInfo.FIRST_PLAYER, new RandomGameInfo());

        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
        System.out.println("IS count: " + config.getAllInformationSets().size());
        System.out.println("Sequence count: " + config.getSequencesFor(BPGGameInfo.DEFENDER).size() + ", " + config.getSequencesFor(BPGGameInfo.ATTACKER).size());
        solver.solve(config);

        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        return solver.finalValue;

//        System.out.println("IR SETS");
//        for (SequenceFormIRInformationSet is : config.getAllInformationSets().values()) {
//            if (is.isHasIR()) {
//                System.out.println(is.getISKey());
//            }
//        }
    }

    public static double runBPG() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BPGGameInfo());
        GameState root = new IRBPGGameState();
        Expander<SequenceFormIRInformationSet> expander = new BPGExpander<>(config);

        builder.build(root, config, expander);

//        if (config.isPlayer2IR()) {
//            System.out.println(" Player 2 has IR ... skipping ...");
//            return;
//        }

        BilinearSequenceFormLP solver = new BilinearSequenceFormLP(BPGGameInfo.DEFENDER, new BPGGameInfo());

        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        solver.solve(config);

        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        return solver.finalValue;

//        System.out.println("IR SETS");
//        for (SequenceFormIRInformationSet is : config.getAllInformationSets().values()) {
//            if (is.isHasIR()) {
//                System.out.println(is.getISKey());
//            }
//        }
    }

    public static double runOZ() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new OZGameInfo());
        GameState root = new IROshiZumoGameState();
        Expander<SequenceFormIRInformationSet> expander = new OshiZumoExpander<>(config);

        builder.build(root, config, expander);

//        if (config.isPlayer2IR()) {
//            System.out.println(" Player 2 has IR ... skipping ...");
//            return;
//        }

        BilinearSequenceFormLP solver = new BilinearSequenceFormLP(OZGameInfo.FIRST_PLAYER, new OZGameInfo());

        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        solver.solve(config);

        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        return solver.finalValue;

//        System.out.println("IR SETS");
//        for (SequenceFormIRInformationSet is : config.getAllInformationSets().values()) {
//            if (is.isHasIR()) {
//                System.out.println(is.getISKey());
//            }
//        }
    }

    private static void runBRTest() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());

        builder.build(new BRTestGameState(), config, new BRTestExpander<>(config));
        BilinearSequenceFormLP solver = new BilinearSequenceFormLP(BRTestGameInfo.FIRST_PLAYER, new BRTestGameInfo());

        solver.solve(config);
    }

    public BilinearSequenceFormLP(Player player, GameInfo info) {
        this.table = new BilinearTable();
        this.player = player;
        this.opponent = info.getOpponent(player);
        this.gameInfo = info;
    }

    public void solve(SequenceFormIRConfig config) {
        addObjective();
        addRPConstraints(config);
        addBehaviorStrategyConstraints(config);
        addBilinearConstraints(config);
        addValueConstraints(config);
        try {
            LPData lpData = table.toCplex();

            if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQF.lp");
            lpData.getSolver().solve();
            System.out.println(lpData.getSolver().getStatus());
            System.out.println(lpData.getSolver().getObjValue());
            double lastSolution = lpData.getSolver().getObjValue();

            Set<Action> sequencesToTighten = findMostViolatedBilinearConstraints(lpData);
            Map<Action, Double> P1Strategy = extractBehavioralStrategy(config, lpData);

            while (!sequencesToTighten.isEmpty()) {

                if (DEBUG) System.out.println(sequencesToTighten);

                if (table.isFixPreviousDigits()) table.storeWValues(lpData);
                for (Action s : sequencesToTighten) {
                    table.refinePrecision(lpData, s);
                }
//                System.out.println("----------------------------------------------------------------------------------------------------------");
                if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQF.lp");
                lpData.getSolver().solve();
                System.out.println(lpData.getSolver().getObjValue());

                P1Strategy = extractBehavioralStrategy(config, lpData);
//                System.out.println("-------------------\nP1 Actions " + P1Strategy);


//                if (Math.abs(lastSolution - lpData.getSolver().getObjValue()) < BILINEAR_PRECISION) {
//                    break;
//                } else {
//                    lastSolution = lpData.getSolver().getObjValue();
//                }
                sequencesToTighten = findMostViolatedBilinearConstraints(lpData);
            }
            ImperfectRecallBestResponseImpl br = new ImperfectRecallBestResponseImpl(opponent, expander, gameInfo);

            if (DEBUG) System.out.println("-------------------");
            if (DEBUG) {
                for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
                    for (Map.Entry<Sequence, Set<Sequence>> entry : i.getOutgoingSequences().entrySet()) {
                        Sequence s = entry.getKey();
                        Object o = new Pair<>(i, s);
                        if (table.exists(o)) {
                            System.out.println(i + " = " + lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(o)]));
                        }
                    }
                }
            }
            Map<Sequence, Double> rp = extractRPStrategy(config, lpData);

            System.out.println("Support: " + rp.values().stream().filter(v -> v > 1e-8).count());
//            IRSequenceBestResponse br2 = new IRSequenceBestResponse(RandomGameInfo.SECOND_PLAYER, expander, gameInfo);
//            br2.getBestResponseSequence(extractRPStrategy(config, lpData));
            br.getBestResponse(P1Strategy);
//            br.getBestResponseSequence(P1StrategySeq);
//            System.out.println("-------------------\nP1 Actions " + extractRPStrategy(config, lpData));
            finalValue = lpData.getSolver().getObjValue();
//            System.out.println("NEW BR = " + br2.getValue());

//            finalValue = lpData.getSolver().getObjValue();
//            for (Sequence sequence : config.getSequencesFor(player)) {
//                System.out.println(sequence + ": " + lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(sequence)]));
//            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void addBilinearConstraints(SequenceFormIRConfig config) {
        for (Sequence sequence : config.getSequencesFor(player)) {
            if (sequence.isEmpty())
                continue;

            if (!((SequenceFormIRInformationSet) sequence.getLastInformationSet()).hasIR())
                continue;

            table.markAsBilinear(sequence, sequence.getSubSequence(sequence.size() - 1), sequence.getLast());
        }
    }

    private void addValueConstraints(SequenceFormIRConfig config) {
        for (Sequence sequence : config.getSequencesFor(opponent)) {
            Object eqKey;
            Object informationSet;
            Sequence subsequence;

            if (sequence.isEmpty()) {
                eqKey = "v_init";
                informationSet = "root";
                subsequence = new ArrayListSequenceImpl(opponent);
            } else {
                subsequence = sequence.getSubSequence(sequence.size() - 1);
                informationSet = sequence.getLastInformationSet();
                eqKey = new Triplet<>(informationSet, subsequence, sequence.getLast());
            }
            Object varKey = new Pair<>(informationSet, subsequence);

            table.setConstraint(eqKey, varKey, 1);
            table.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            for (SequenceFormIRInformationSet reachableSet : config.getReachableSets(sequence)) {
                if (!reachableSet.getActions().isEmpty() && reachableSet.getOutgoingSequences().get(sequence) != null && !reachableSet.getOutgoingSequences().get(sequence).isEmpty() && reachableSet.getPlayer().equals(opponent))
                    table.setConstraint(eqKey, new Pair<>(reachableSet, sequence), -1);
            }
            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(sequence)) {
                Double utility = config.getUtilityFor(sequence, compatibleSequence);

                if (utility != null)
                    table.setConstraint(eqKey, compatibleSequence, (player.getId() == 0 ? -1 : 1) * utility);
            }
        }
    }

    private void addBehaviorStrategyConstraints(SequenceFormIRConfig config) {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (!informationSet.hasIR())
                continue;
            if (informationSet.getPlayer().equals(player)) {
                for (Action action : informationSet.getActions()) {
                    table.setConstraint(informationSet, action, 1);
                    table.setLowerBound(action, 0);
                    table.setUpperBound(action, 1);
                }
                table.setConstant(informationSet, 1);
                table.setConstraintType(informationSet, 1);
            }
        }
    }

    private void addRPConstraints(SequenceFormIRConfig config) {
        table.setConstraint("rpInit", new ArrayListSequenceImpl(player), 1);
        table.setConstant("rpInit", 1);
        table.setConstraintType("rpInit", 1);
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(player))
                addRPConstraint(informationSet);
        }
        addRPVarBounds(config);
    }

    private void addRPVarBounds(SequenceFormIRConfig config) {
        for (Sequence sequence : config.getSequencesFor(player)) {
            table.setLowerBound(sequence, 0);
            table.setUpperBound(sequence, 1);
        }
    }

    private void addRPConstraint(SequenceFormIRInformationSet informationSet) {
        for (Map.Entry<Sequence, Set<Sequence>> outgoingEntry : informationSet.getOutgoingSequences().entrySet()) {
            Object eqKey = new Pair<>(informationSet, outgoingEntry.getKey());

            table.setConstraint(eqKey, outgoingEntry.getKey(), 1);
            table.setConstraintType(eqKey, 1);
            for (Sequence sequence : outgoingEntry.getValue()) {
                table.setConstraint(eqKey, sequence, -1);
            }
        }
    }

    private void addObjective() {
        table.setObjective(new Pair<>("root", new ArrayListSequenceImpl(opponent)), 1);
    }

    private Set<Action> findMostViolatedBilinearConstraints(LPData data) throws IloException {
        HashSet<Action> result = new HashSet<>();
        HashSet<Action> result2 = new HashSet<>();

        for (Object productSequence : table.getBilinearVars().keySet()) {
            Object sequence = table.getBilinearVars().get(productSequence).getLeft();
            Object action = table.getBilinearVars().get(productSequence).getRight();

            if (data.getSolver().getValue(table.getDeltaBehavioralVariables().get(action)) > BILINEAR_PRECISION) {
                if (DEBUG)
                    System.out.println("X DELTA " + action + " = " + data.getSolver().getValue(table.getDeltaBehavioralVariables().get(action)));
                result.add((Action) action);
            }

            if (data.getSolver().getValue(table.getDeltaSequenceVariables().get(productSequence)) > BILINEAR_PRECISION) {
                if (DEBUG)
                    System.out.println("SEQ DELTA " + action + " = " + data.getSolver().getValue(table.getDeltaSequenceVariables().get(productSequence)));
                result.add((Action) action);
            }

        }

//        if (1+1 == 2)
//            return result;

        double maxDifference = Double.NEGATIVE_INFINITY;

        for (Action a : result) {
            SequenceFormIRInformationSet is = (SequenceFormIRInformationSet) a.getInformationSet();
            double average = 0;
            ArrayList<Double> specValues = new ArrayList<>();

            for (Map.Entry<Sequence, Set<Sequence>> entry : is.getOutgoingSequences().entrySet()) {
                if (data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]) > 0) {
                    Sequence productSequence = new ArrayListSequenceImpl(entry.getKey());
                    productSequence.addLast(a);
                    double sV = data.getSolver().getValue(data.getVariables()[table.getVariableIndex(productSequence)]);
                    sV = sV / data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]);
                    average += sV;
                    specValues.add(sV);
                }
            }
            if (specValues.size() == 0) average = 0;
            else average = average / specValues.size();

            double error = 0;
            for (double d : specValues) {
                error += Math.abs(average - d);
            }

            if (error > maxDifference) {
                result2.clear();
                result2.add(a);
                maxDifference = error;
            }

//            if (error > maxDifference || error > 1e-4) {
//                result2.add(a);
//                maxDifference = error;
//            }
        }

        return result2;
    }

    public Expander getExpander() {
        return expander;
    }

    public void setExpander(Expander expander) {
        this.expander = expander;
    }

    public Map<Action, Double> extractBehavioralStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException {
        if (DEBUG) System.out.println("----- P1 Actions -----");
        Map<Action, Double> P1Strategy = new HashMap<>();
        for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
            if (!i.getPlayer().equals(player)) continue;
            boolean allZero = true;
            for (Action a : i.getActions()) {
                double average = 0;
                int count = 0;
                for (Sequence subS : i.getOutgoingSequences().keySet()) {
                    Sequence s = new ArrayListSequenceImpl(subS);
                    s.addLast(a);

                    if (lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]) > 0) {
                        double sV = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
                        sV = sV / lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]);
                        average += sV;
                        count++;
                    }
                }
                if (count == 0) average = 0;
                else average = average / count;

                if (DEBUG) System.out.println(a + " = " + average);
                P1Strategy.put(a, average);

                if (average > 0) allZero = false;
            }
            if (allZero && i.getActions().size() > 0) {
                P1Strategy.put(i.getActions().iterator().next(), 1d);
            }
        }
        return P1Strategy;
    }

    public Map<Sequence, Double> extractRPStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException {
        Map<Sequence, Double> P1StrategySeq = new HashMap<>();
        for (Sequence s : config.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            Action a = s.getLast();
            double seqValue = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
            if (DEBUG) System.out.println(s + " = " + seqValue);

            P1StrategySeq.put(s, lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]));
        }
        return P1StrategySeq;
    }

}
