package cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.bestresponse.ImperfectRecallBestResponse;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.*;

public class BilinearSeqenceFormBNB {
    private final Player player;
    private final Player opponent;
    private BilinearTableBNB table;
    private Expander expander;
    private GameInfo gameInfo;
    private Double finalValue = null;

    private boolean DEBUG = false;
    public static boolean SAVE_LPS = true;


    static public final double BILINEAR_PRECISION = 0.0001;
    private final double EPS = 0.000001;

    public static void main(String[] args) {
        runRandomGame();
//        runBRTest();
    }

    private static void runRandomGame() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig();
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

        builder.build(root, config, expander);

//        if (config.isPlayer2IR()) {
//            System.out.println(" Player 2 has IR ... skipping ...");
//            return;
//        }

        BilinearSeqenceFormBNB solver = new BilinearSeqenceFormBNB(BRTestGameInfo.FIRST_PLAYER, new RandomGameInfo());

        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        solver.solve(config);

        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);


//        System.out.println("IR SETS");
//        for (SequenceFormIRInformationSet is : config.getAllInformationSets().values()) {
//            if (is.isHasIR()) {
//                System.out.println(is.getISKey());
//            }
//        }
    }

    private static void runBRTest() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig();

        builder.build(new BRTestGameState(), config, new BRTestExpander<>(config));
        BilinearSeqenceFormBNB solver = new BilinearSeqenceFormBNB(BRTestGameInfo.FIRST_PLAYER, new BRTestGameInfo());

        solver.solve(config);
    }

    public BilinearSeqenceFormBNB(Player player, GameInfo info) {
        this.table = new BilinearTableBNB();
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

            Map<Action, Double> P1Strategy = extractBehavioralStrategy(config, lpData);
            ImperfectRecallBestResponse br = new ImperfectRecallBestResponse(RandomGameInfo.SECOND_PLAYER, expander, gameInfo);
            br.getBestResponse(P1Strategy);

            TreeSet<BNBCandidate> fringe = new TreeSet<>();
            double globalLB = -br.getValue();
            double globalUB = lastSolution;


            BNBCandidate c = new BNBCandidate(globalLB, globalUB);
            BNBCandidate bestCandidate = c;
            Set<Action> actionToFocus = findMostViolatedBilinearConstraints(config, lpData);

            if (!actionToFocus.isEmpty()) {
                Action a = actionToFocus.iterator().next();
//                double strategy = Math.floor(Math.pow(10, 2) * P1Strategy.get(a))/Math.pow(10, 2);
                double strategy = P1Strategy.get(a);
                c.setActionToFocusOn(a, strategy);
                fringe.add(c);
            }

            while (!fringe.isEmpty()) {

                BNBCandidate node = null;
                while (node == null || node.getUb() < bestCandidate.getLb()) {
                    node = fringe.pollFirst();
                }
                if (node == null) break;



                globalUB = Math.max(node.getUb(),bestCandidate.getUb());
                for (BNBCandidate f : fringe) if (f.getUb() > globalUB) globalUB = f.getUb();

                System.out.println("Node LB: " + node.getLb() + " \t\t Node UB: " + node.getUb());
                System.out.println("Global LB: " + globalLB + " \t\t Global UB: " + globalUB);

                System.out.println(node.getActionToFocusOn() + " = " + node.getCurrentProbOfAction());

                if (Math.abs(globalUB - globalLB) < 0.001) break;


                Set<IloRange> constraints = table.applyChanges(node.getChanges(), lpData);

                int fixedDigits = 1;
                for (Pair<BNBCandidate.ChangeType, Triplet<Integer, Action, Double>> x : node.getChanges()) {
                    if (!x.getRight().getSecond().equals(node.getActionToFocusOn())) continue;
                    if (!x.getLeft().equals(BNBCandidate.ChangeType.MIDDLE)) continue;
                    fixedDigits = Math.max(x.getRight().getFirst(),fixedDigits);
                }
                int currentPrecision = table.getBilinearPrecision(node.getActionToFocusOn());
                if (currentPrecision <= fixedDigits + 1 ) table.refinePrecision(lpData, node.getActionToFocusOn());

                for (BNBCandidate.ChangeType t : BNBCandidate.ChangeType.values()) {
                    Set<Pair<BNBCandidate.ChangeType, Triplet<Integer, Action, Double>>> newChanges = new HashSet<> (node.getChanges());
                    double probability = node.getCurrentProbOfAction();
                    if (t.equals(BNBCandidate.ChangeType.LEFT)) {
                        probability = Math.floor(Math.pow(10, fixedDigits) * probability)/Math.pow(10, fixedDigits);
                    } else if (t.equals(BNBCandidate.ChangeType.RIGHT)) {
                        probability = (Math.floor(Math.pow(10, fixedDigits) * probability) + 1)/Math.pow(10, fixedDigits);
                    }
                    Pair<BNBCandidate.ChangeType, Triplet<Integer, Action, Double>> change = new Pair<>(t, new Triplet<>(fixedDigits + (t == BNBCandidate.ChangeType.MIDDLE ? 1 : 0), node.getActionToFocusOn(), probability));
                    newChanges.add(change);
                    Set<IloRange> r = table.applyChange(change, lpData);
                    if (table.applyChangeW(change, lpData)) {

                        if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQF.lp");
                        lpData.getSolver().solve();
                        if (lpData.getSolver().getStatus().equals(IloCplex.Status.Optimal)) {
                            lastSolution = lpData.getSolver().getObjValue();
                            P1Strategy = extractBehavioralStrategy(config, lpData);
                            br.getBestResponse(P1Strategy);

                            table.storeWValues(lpData);

                            c = new BNBCandidate(-br.getValue(), lastSolution, newChanges);
                            if (Math.abs(c.getUb() - c.getLb()) < 0.001) {
                                if (c.getLb() > globalLB) {
                                    globalLB = c.getLb();
                                    bestCandidate = c;
                                }
                            } else if (c.getUb() > globalLB) {
                                actionToFocus = findMostViolatedBilinearConstraints(config, lpData);
                                if (!actionToFocus.isEmpty()) {
                                    Action a = actionToFocus.iterator().next();
//                                    double strategy = (Math.floor(Math.pow(10, change.getRight().getFirst()+1) * P1Strategy.get(a)))/Math.pow(10, change.getRight().getFirst()+1);
                                    double strategy = P1Strategy.get(a);
                                    c.setActionToFocusOn(a, strategy);

                                    fringe.add(c);
                                }
                            }
                        } else {
                            if (DEBUG) System.out.println("Infeasible");
                        }
                    }
                    table.deleteSingleChange(node.getChanges(), r, node.getActionToFocusOn(), lpData);
                }

                table.deleteChanges(constraints, lpData);

            }

            System.out.println("BEST LB: " + bestCandidate.getLb() + "\t\tUB: " + bestCandidate.getUb());
            table.applyChanges(bestCandidate.getChanges(), lpData);
            if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQF.lp");
            lpData.getSolver().solve();
            System.out.println(lpData.getSolver().getStatus());
            P1Strategy = extractBehavioralStrategy(config,lpData);

            br.getBestResponse(P1Strategy);
            finalValue = -br.getValue();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void addBilinearConstraints(SequenceFormIRConfig config) {
        for (Sequence sequence : config.getSequencesFor(player)) {
            if (sequence.isEmpty())
                continue;

            if (!((SequenceFormIRInformationSet)sequence.getLastInformationSet()).hasIR())
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
                    table.setConstraint(eqKey, compatibleSequence, -utility);
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

    private Set<Action> findMostViolatedBilinearConstraints(SequenceFormIRConfig config, LPData data) throws IloException{
        HashSet<Action> result = new HashSet<>();
        HashSet<Action> result2 = new HashSet<>();

//        for (Object productSequence : table.getBilinearVars().keySet()) {
//            Object sequence = table.getBilinearVars().get(productSequence).getLeft();
//            Object action = table.getBilinearVars().get(productSequence).getRight();
//
//            if (data.getSolver().getValue(table.getDeltaBehavioralVariables().get(action)) > BILINEAR_PRECISION) {
//                if (DEBUG) System.out.println("X DELTA " + action + " = " + data.getSolver().getValue(table.getDeltaBehavioralVariables().get(action)));
//                result.add((Action)action);
//            }
//
//            if (data.getSolver().getValue(table.getDeltaSequenceVariables().get(productSequence)) > BILINEAR_PRECISION) {
//                if (DEBUG) System.out.println("SEQ DELTA " + action + " = " + data.getSolver().getValue(table.getDeltaSequenceVariables().get(productSequence)));
//                result.add((Action)action);
//            }

        for (Sequence s : config.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            result.add(s.getLast());
        }

//        if (1+1 == 2)
//            return result;

        double maxDifference = Double.NEGATIVE_INFINITY;

        for (Action a : result) {
            SequenceFormIRInformationSet is = (SequenceFormIRInformationSet)a.getInformationSet();
            double average = 0;
            ArrayList<Double> specValues = new ArrayList<>();

            for (Map.Entry<Sequence, Set<Sequence>> entry : is.getOutgoingSequences().entrySet()) {
                if (data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]) > 0) {
                    Sequence productSequence = new ArrayListSequenceImpl(entry.getKey());
                    productSequence.addLast(a);
                    double sV = data.getSolver().getValue(data.getVariables()[table.getVariableIndex(productSequence)]);
                    sV =  sV / data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]);
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

            if (error > 0) if (DEBUG) System.out.println("DIFF " + a + " = " + error);

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

    public Map<Action, Double> extractBehavioralStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException{
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
                        sV =  sV / lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]);
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

    public Map<Sequence, Double> extractRPStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException{
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
