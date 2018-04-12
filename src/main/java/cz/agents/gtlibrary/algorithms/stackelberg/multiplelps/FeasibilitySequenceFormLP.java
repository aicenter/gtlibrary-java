package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class FeasibilitySequenceFormLP {

    private IloCplex cplex;
    private Player leader;
    private Player follower;
    private StackelbergConfig algConfig;
    private Map<Player, Set<SequenceInformationSet>> informationSets;

    protected Map<Object, IloNumVar> slackVariables;
    protected Map<Object, IloRange> constraints;
    protected Map<Object, IloNumVar> variables;
    protected Map<Player, Set<Sequence>> sequences;
    protected IloNumVar objective;
    protected IloRange leaderObjConstraint;
    protected ThreadMXBean mxBean;
    private long cplexSolvingTime;

    public FeasibilitySequenceFormLP() {
    }

    public FeasibilitySequenceFormLP(Player leader, Player follower, StackelbergConfig algConfig, Map<Player, Set<SequenceInformationSet>> informationSets, Map<Player, Set<Sequence>> sequences) {
        try {
            cplexSolvingTime = 0;
            mxBean = ManagementFactory.getThreadMXBean();
            slackVariables = new HashMap<>();
            constraints = new HashMap<>();
            variables = new HashMap<>();
            this.sequences = sequences;
            this.informationSets = informationSets;
            this.algConfig = algConfig;
            this.leader = leader;
            this.follower = follower;
            cplex = new IloCplex();
            cplex.setParam(IloCplex.IntParam.RootAlg, SequenceFormLP.CPLEXALG);
            cplex.setParam(IloCplex.IntParam.Threads, SequenceFormLP.CPLEXTHREADS);
            if (SequenceFormLP.CPLEXTHREADS == 1)
                cplex.setParam(IloCplex.IntParam.AuxRootThreads, -1);
            cplex.setOut(null);
            cplex.addMaximize(cplex.constant(0));
            objective = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, "v0");
            createVariables(cplex, algConfig);
            createConstraintsForSets(leader, cplex, informationSets.get(leader));
            createConstraintsForSequences(algConfig, cplex, algConfig.getSequencesFor(follower));
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public boolean checkFeasibilityFor(Iterable<Sequence> partialPureRp) {
        try {
//            cplex.exportModel("feas.lp");
//            setValueForBRSlack(cplex, partialPureRp, 0);
            long start = mxBean.getCurrentThreadCpuTime();
            cplex.solve();
            cplexSolvingTime += mxBean.getCurrentThreadCpuTime() - start;
//            setValueForBRSlack(cplex, partialPureRp, 1);
            if (cplex.getStatus() == IloCplex.Status.Optimal)
                return true;
        } catch (IloException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeSlackFor(Sequence sequence) {
        if (!StackelbergConfig.USE_FEASIBILITY_CUT)
            return;
        IloRange constraint = constraints.get(sequence);
        IloNumVar slack = slackVariables.get(sequence);
        if (constraint == null) {
            if (sequence.size() == 0)
                return;
            assert false;
        }
        try {
            cplex.setLinearCoef(constraint, slack, 0);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public void addSlackFor(Sequence sequence) {
        if (!StackelbergConfig.USE_FEASIBILITY_CUT)
            return;
        IloRange constraint = constraints.get(sequence);
        IloNumVar slack = slackVariables.get(sequence);
        if (constraint == null) {
            if (sequence.size() == 0)
                return;
            assert false;
        }
        try {
            cplex.setLinearCoef(constraint, slack, -1);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public boolean checkFeasibilityFor(Iterable<Sequence> pureRP, double maxValue) {
        try {
            if (!StackelbergConfig.USE_FEASIBILITY_CUT)
                setValueForBRSlack(cplex, pureRP, 0);
            updateObjectiveConstraint(cplex, objective, pureRP, algConfig);
            addBestValueConstraint(cplex, objective, maxValue + 1e-5);
            long start = mxBean.getCurrentThreadCpuTime();
//            cplex.exportModel("feas.lp");
            cplex.solve();
            cplexSolvingTime += mxBean.getCurrentThreadCpuTime() - start;
            if (!StackelbergConfig.USE_FEASIBILITY_CUT)
                setValueForBRSlack(cplex, pureRP, 1);
            removePreviousValueConstraint();
            deleteObjectiveConstraint(cplex);
            if (cplex.getStatus() == IloCplex.Status.Optimal)
                return true;
        } catch (IloException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void removePreviousValueConstraint() throws IloException {
        cplex.delete(constraints.get("maxVal"));
        constraints.remove("maxVal");
    }

    protected void updateObjectiveConstraint(IloCplex cplex, IloNumVar v0, Iterable<Sequence> bestResponse, StackelbergConfig algConfig) throws IloException {
        deleteObjectiveConstraint(cplex);
        IloNumExpr sumG = cplex.constant(0);

        for (Sequence s : bestResponse) {
            HashSet<Sequence> leaderCompSequences = new HashSet<>();

            if (algConfig.getCompatibleSequencesFor(s) != null)
                leaderCompSequences.addAll(algConfig.getCompatibleSequencesFor(s));

            for (Sequence ls : leaderCompSequences) {
                IloNumExpr prob = variables.get(ls);

                if (prob == null)
                    continue;
                Map<Player, Sequence> actions = createActions(ls, s);
                double utility = getUtility(algConfig, actions, leader);

                if (Math.abs(utility) > 1e-13)
                    sumG = cplex.sum(sumG, cplex.prod(utility, prob));
            }
        }
        leaderObjConstraint = cplex.addEq(cplex.diff(v0, sumG), 0);
    }

    protected void deleteObjectiveConstraint(IloCplex cplex) throws IloException {
        if (leaderObjConstraint != null)
            cplex.delete(leaderObjConstraint);
    }

    protected void addBestValueConstraint(IloCplex cplex, IloNumVar v0, double maxValue) throws IloException {
        IloRange maxValueConstraint = constraints.get("maxVal");

        if (maxValueConstraint == null) {
            IloLinearNumExpr rowExpr = cplex.linearNumExpr();

            rowExpr.addTerm(1, v0);
            maxValueConstraint = cplex.addGe(rowExpr, maxValue, "prevBest");
            constraints.put("maxVal", maxValueConstraint);
        }
        maxValueConstraint.setLB(maxValue);
    }


    protected void createConstraintsForSequences(StackelbergConfig algConfig, IloCplex cplex, Collection<Sequence> VConstraints) throws IloException {
        for (Sequence firstPlayerSequence : VConstraints) {
            if (constraints.containsKey(firstPlayerSequence)) {
                cplex.delete(constraints.get(firstPlayerSequence));
                constraints.remove(firstPlayerSequence);
            }
            createConstraintForSequence(cplex, firstPlayerSequence, algConfig);
        }
    }

    protected void createVariables(IloCplex model, StackelbergConfig algConfig) throws IloException {
        for (Sequence sequence : algConfig.getAllSequences()) {
            if (variables.containsKey(sequence)) continue;
            if (sequence.getPlayer().equals(leader)) {
                createVariableForSequence(model, sequence);
                sequences.get(sequence.getPlayer()).add(sequence);
            } else {
                createSlackVariableForSequence(model, sequence);
            }
        }
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (!variables.containsKey(informationSet)) {
                createVariableForIS(model, informationSet);
                informationSets.get(informationSet.getPlayer()).add(informationSet);
            }
        }
    }

    protected IloNumVar createVariableForIS(IloCplex cplex, InformationSet is) throws IloException {
        double ub = Double.POSITIVE_INFINITY;
        IloNumVar v = cplex.numVar(Double.NEGATIVE_INFINITY, ub, IloNumVarType.Float, "V" + is.toString());

        variables.put(is, v);
        return v;
    }

    protected IloNumVar createVariableForSequence(IloCplex cplex, Sequence sequence) throws IloException {
        IloNumVar r = cplex.numVar(0, 1, IloNumVarType.Float, "R" + sequence.toString());

        if (sequence.size() == 0)
            r.setLB(1d);
        variables.put(sequence, r);
        return r;
    }

    protected IloNumVar createSlackVariableForSequence(IloCplex cplex, Sequence sequence) throws IloException {
        IloNumVar s = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "SL" + sequence.toString());
        slackVariables.put(sequence, s);
        return s;
    }

    protected void createConstraintForSequence(IloCplex cplex, Sequence firstPlayerSequence, StackelbergConfig algConfig) throws IloException {
        Player firstPlayer = firstPlayerSequence.getPlayer();
        InformationSet informationSet = firstPlayerSequence.getLastInformationSet();
        IloNumExpr VI = null;
        IloNumExpr sumV = cplex.constant(0);

        if (informationSet == null) {
            if (firstPlayer.equals(follower)) return;
            VI = objective;
            for (SequenceInformationSet reachableSet : algConfig.getReachableSets(firstPlayerSequence)) {
                IloNumVar tmp = variables.get(reachableSet);

                assert (tmp != null);

                if (reachableSet.getOutgoingSequences() != null && reachableSet.getOutgoingSequences().size() > 0) {
                    sumV = cplex.sum(sumV, tmp);
                }
            }
        } else {
            VI = variables.get(informationSet);
            if (algConfig.getReachableSets(firstPlayerSequence) != null)
                for (SequenceInformationSet reachableSet : algConfig.getReachableSets(firstPlayerSequence)) {
                    IloNumVar tmp = variables.get(reachableSet);

                    assert (tmp != null);
                    if (reachableSet.getOutgoingSequences() == null || reachableSet.getOutgoingSequences().size() == 0)
                        continue;
                    sumV = cplex.sum(sumV, tmp);
                }
        }

        IloNumExpr sumGR = computeSumGR(cplex, firstPlayerSequence, algConfig, firstPlayer);
        if (firstPlayer.equals(follower)) {
            IloNumVar slack = slackVariables.get(firstPlayerSequence);
            IloRange con = cplex.addEq(cplex.diff(cplex.diff(cplex.diff(VI, sumV), sumGR), slack), 0, "CON:" + firstPlayerSequence.toString());
            constraints.put(firstPlayerSequence, con);
        } else {
            IloRange con = cplex.addEq(cplex.diff(cplex.diff(VI, sumV), sumGR), 0, "CON:" + firstPlayerSequence.toString());
            constraints.put(firstPlayerSequence, con);
        }
    }

    protected IloNumExpr computeSumGR(IloCplex cplex, Sequence firstPlayerSequence, StackelbergConfig algConfig, Player firstPlayer) throws IloException {
        IloNumExpr sumGR = cplex.constant(0);
        HashSet<Sequence> secondPlayerSequences = new HashSet<>();

        if (algConfig.getCompatibleSequencesFor(firstPlayerSequence) != null)
            secondPlayerSequences.addAll(algConfig.getCompatibleSequencesFor(firstPlayerSequence));

        for (Sequence secondPlayerSequence : secondPlayerSequences) {
            IloNumExpr prob = variables.get(secondPlayerSequence);

            if (prob == null)
                continue;
            Map<Player, Sequence> actions = createActions(firstPlayerSequence, secondPlayerSequence);
            double utility = getUtility(algConfig, actions, firstPlayer);
            sumGR = cplex.sum(sumGR, cplex.prod(utility, prob));
        }
        return sumGR;
    }

    public Map<Player, Sequence> createActions(Sequence firstPlayerSequence, Sequence secondPlayerSequence) {
        Map<Player, Sequence> actions = new HashMap<Player, Sequence>();

        actions.put(firstPlayerSequence.getPlayer(), new ArrayListSequenceImpl(firstPlayerSequence));
        actions.put(secondPlayerSequence.getPlayer(), new ArrayListSequenceImpl(secondPlayerSequence));
        return actions;
    }

    protected static double getUtility(StackelbergConfig algConfig, Map<Player, Sequence> sequenceCombination, Player firstPlayer) {
        Double utility = algConfig.getUtilityFor(sequenceCombination, firstPlayer);

        if (utility == null) {
            utility = 0d;
        }
        return utility;
    }

    protected void setValueForBRSlack(IloCplex cplex, Iterable<Sequence> sequences, int value) throws IloException {
        for (Sequence s : sequences) {
            IloRange constraint = constraints.get(s);
            IloNumVar slack = slackVariables.get(s);
            if (constraint == null) {
                if (s.size() == 0) continue;
                assert false;
            }
            cplex.setLinearCoef(constraint, slack, -value);
        }
    }

    protected void createConstraintsForSets(Player secondPlayer, IloCplex cplex, Set<SequenceInformationSet> RConstraints) throws IloException {
        for (SequenceInformationSet secondPlayerIS : RConstraints) {
            assert (secondPlayerIS.getPlayer().equals(secondPlayer));
            if (constraints.containsKey(secondPlayerIS)) {
                cplex.delete(constraints.get(secondPlayerIS));
                constraints.remove(secondPlayerIS);
            }
            createConstraintForIS(cplex, secondPlayerIS);
        }
    }

    protected IloRange createConstraintForIS(IloCplex cplex, SequenceInformationSet informationSet) throws IloException {
        IloNumExpr sumL = cplex.constant(0);

        if (informationSet.getOutgoingSequences().isEmpty()) {
            return null;
        }
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            if (variables.get(sequence) == null)
                continue;
            sumL = cplex.sum(sumL, variables.get(sequence));
        }

        Sequence sequence = informationSet.getPlayersHistory();
        IloNumExpr sumR = variables.get(sequence);

        if (sumR == null)
            return null;

        IloRange constrain = cplex.addEq(cplex.diff(sumL, sumR), 0, "CON:" + informationSet.toString());

        constraints.put(informationSet, constrain);
        return constrain;
    }

    public long getCplexSolvingTime() {
        return cplexSolvingTime;
    }
}
