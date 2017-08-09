package cz.agents.gtlibrary.algorithms.stackelberg.milp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.EmptyFeasibilitySequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.PureRealPlanIterator;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;

public class DOBSS extends StackelbergSequenceFormMILP {
    private Map<GameState, IloNumExpr> boundsForLeafs;

    public DOBSS(Player[] players, Player leader, Player follower, GameInfo info, Expander<SequenceInformationSet> expander) {
        super(players, leader, follower, info, expander);
        boundsForLeafs = new HashMap<>();
    }

    @Override
    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        double maxValue = Double.NEGATIVE_INFINITY;
        Set<Sequence> followerBR = new HashSet<Sequence>();
        Map<Sequence, Double> leaderResult = new HashMap<Sequence, Double>();

        try {
            IloCplex cplex = modelsForPlayers.get(leader);
            IloNumVar v0 = objectiveForPlayers.get(leader);
            long startTime = threadBean.getCurrentThreadCpuTime();

            buildInformationSets(algConfig);
            createVariables(cplex, algConfig);
            createConstraintsForSets(cplex, algConfig.getAllInformationSets().values());
            createConstraintsForStates(cplex, algConfig.getAllLeafs());
            createRPConstraints(algConfig.getIterator(follower, expander, new EmptyFeasibilitySequenceFormLP(leader, follower, algConfig, informationSets, sequences)), cplex, algConfig);
            setObjective(cplex, v0, algConfig);
            debugOutput.println("phase 1 done");
            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;

//            cplex.exportModel("stck-DOBBS" + leader + ".lp"); // uncomment for model export
            startTime = threadBean.getCurrentThreadCpuTime();
            debugOutput.println("Solving");
            cplex.solve();
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
            debugOutput.println("Status: " + cplex.getCplexStatus());

            if (cplex.getCplexStatus() == IloCplex.CplexStatus.Optimal || cplex.getCplexStatus() == IloCplex.CplexStatus.OptimalTol) {
                double v = cplex.getValue(v0);
                debugOutput.println("Best reward is " + v);

                maxValue = v;

//                for (Map.Entry<Object, IloNumVar> ee : variables.entrySet()) {
//                    try {
//                        debugOutput.println(ee.getKey().toString() + "=" + cplex.getValue(ee.getValue()));
//                    } catch (IloCplex.UnknownObjectException e) {
//                        continue;
//                    }
//                }
//                debugOutput.println("-------");
//                for (Map.Entry<Object, IloNumVar> ee : slackVariables.entrySet()) {
//                    try {
//                        debugOutput.println(ee.getKey().toString() + "=" + cplex.getValue(ee.getValue()));
//                    } catch (IloCplex.UnknownObjectException e) {
//                        continue;
//                    }
//                }
//                debugOutput.println("leader rp: ");
//                for (Map.Entry<Sequence, Double> entry : createSolution(algConfig, leader, cplex).entrySet()) {
//                    if (entry.getValue() > 0)
//                        debugOutput.println(entry);
//                }
//                for (Map.Entry<Object, IloNumVar> entry : variables.entrySet()) {
//                    if (entry.getKey() instanceof Set)
//                        System.out.println(entry.getKey() + ": " + cplex.getValue(entry.getValue()));
//                }
//                debugOutput.println("follower rp: ");
//                for (Map.Entry<Sequence, Double> entry : createSolution(algConfig, follower, cplex).entrySet()) {
//                    if (entry.getValue() > 0)
//                        debugOutput.println(entry);
//                }
//                debugOutput.println("Leaf probs");
//                for (Map.Entry<Object, IloNumVar> entry : variables.entrySet()) {
//                    if (entry.getKey() instanceof GameState) {
//                        try {
////                            if (cplex.getValue(entry.getValue()) > 0)
//                                debugOutput.println(entry.getKey() + ": " + cplex.getValue(entry.getValue()));
//                        } catch (IloException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
            }
        } catch (IloException e) {
            e.printStackTrace();
        }

        resultStrategies.put(leader, leaderResult);
        resultValues.put(leader, maxValue);

        return maxValue;
    }

    protected void createBoundConstraintsForState(IloCplex cplex, GameState state) throws IloException {
        IloNumVar LS = variables.get(state);
        IloNumVar RSL = variables.get(state.getSequenceFor(leader));
        IloRange cL = cplex.addLe(cplex.diff(LS, RSL), 0, "LRPUB");

        constraints.put(state + "|LRP", cL);
    }


    private void createRPConstraints(PureRealPlanIterator iterator, IloCplex cplex, StackelbergConfig algConfig) throws IloException {
        try {
            while (true) {
                Set<Sequence> pureRP = iterator.next();

                creareRPVariable(pureRP, cplex);
                createRPConstraint(pureRP, cplex, algConfig);
                udpateLeafBounds(algConfig.getRootState(), pureRP, cplex);
            }
        } catch (NoSuchElementException e) {
            setBounds(cplex);
            createRPSumConstraint(cplex);
        }
    }

    private void createRPSumConstraint(IloCplex cplex) throws IloException {
        IloNumExpr sum = cplex.constant(0d);

        for (Map.Entry<Object, IloNumVar> entry : variables.entrySet()) {
            if (entry.getKey() instanceof Set)
                sum = cplex.sum(sum, entry.getValue());
        }
        cplex.addEq(sum, 1);
    }

    private void setBounds(IloCplex cplex) throws IloException {
        for (Map.Entry<GameState, IloNumExpr> entry : boundsForLeafs.entrySet()) {
            cplex.addLe(cplex.sum(variables.get(entry.getKey()), cplex.negative(entry.getValue())), 0, "FRPUB");
        }
    }

    private void udpateLeafBounds(GameState state, Set<Sequence> pureRP, IloCplex cplex) throws IloException {
        if (state.isGameEnd()) {
            addBound(state, pureRP, cplex);
            return;
        }
        if (state.getPlayerToMove().equals(follower)) {
            Sequence sequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());

            for (Action action : expander.getActions(state)) {
                sequence.addLast(action);

                if (pureRP.contains(sequence)) {
                    udpateLeafBounds(state.performAction(action), pureRP, cplex);
                    break;
                }
                sequence.removeLast();
            }
        } else {
            for (Action action : expander.getActions(state)) {
                udpateLeafBounds(state.performAction(action), pureRP, cplex);
            }
        }

    }

    private void addBound(GameState state, Set<Sequence> pureRP, IloCplex cplex) throws IloException {
        IloNumExpr bound = boundsForLeafs.get(state);

        if (bound == null)
            bound = cplex.constant(0d);
        bound = cplex.sum(bound, variables.get(pureRP));
        boundsForLeafs.put(state, bound);
    }

    private void creareRPVariable(Set<Sequence> pureRP, IloCplex cplex) throws IloException {
        IloNumVar rpVar = cplex.numVar(0, 1, IloNumVarType.Int, "Q:" + pureRP);

        variables.put(pureRP, rpVar);
    }

    private void createRPConstraint(Set<Sequence> pureRP, IloCplex cplex, StackelbergConfig algConfig) {
        try {
            IloNumExpr expr = cplex.constant(0d);

            expr = cplex.sum(expr, variables.get("a"));
            for (Sequence followerSequence : pureRP) {
                for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(followerSequence)) {
                    double utility = getUtilityFor(algConfig, followerSequence, leaderSequence, follower);

                    expr = cplex.sum(expr, cplex.prod(-utility, variables.get(leaderSequence)));
                }
            }

            constraints.put(pureRP + "0", cplex.addGe((IloNumExpr) cplex.getCopy(expr), 0, "RP_CON:" + pureRP));
            expr = cplex.sum(expr, cplex.prod(M, cplex.sum(-1d, variables.get(pureRP))));
            constraints.put(pureRP + "1", cplex.addLe(expr, 0));
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private double getUtilityFor(StackelbergConfig algConfig, Sequence followerSequence, Sequence leaderSequence, Player player) {
        Double utility = algConfig.getUtilityFor(followerSequence, leaderSequence, player);

        return utility == null ? 0 : utility;
    }

    protected void createVariables(IloCplex model, StackelbergConfig algConfig) throws IloException {
        for (Sequence sequence : algConfig.getAllSequences()) {
            if (variables.containsKey(sequence))
                continue;
            if (sequence.getPlayer().equals(leader)) {
                createVariableForSequence(model, sequence);
            } else {
                createIntegerVariableForSequence(model, sequence);
                createSlackVariableForSequence(model, sequence);
            }
        }
        for (GameState gs : algConfig.getAllLeafs()) {
            createStateProbVariable(model, gs);
        }
        createFollowerValueVariable(model);
        debugOutput.println("variables created");
    }

    private void createFollowerValueVariable(IloCplex model) throws IloException {
        IloNumVar a = model.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, "a");

        variables.put("a", a);
    }

    private void buildInformationSets(StackelbergConfig algConfig) {
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (!variables.containsKey(informationSet)) {
                if (informationSet.getPlayer().equals(follower)) {
                    informationSets.get(informationSet.getPlayer()).add(informationSet);
                }
            }
        }
    }
}
