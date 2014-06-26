package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.CplexStatus;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.io.PrintStream;
import java.util.*;

public class StackelbergSequenceFormLP extends SequenceFormLP {

    private Player leader;
    private Player follower;
    private Player[] players;

    private IloRange leaderObj = null;

    protected Map<Object, IloNumVar> slackVariables = new HashMap<Object, IloNumVar>();


	public StackelbergSequenceFormLP(Player[] players) {
        super(players);
        this.players = players;
    }


    protected void resetModel(IloCplex cplex, Player player) throws IloException{
        cplex.clearModel();
        cplex.setParam(IloCplex.IntParam.RootAlg, CPLEXALG);
        cplex.setParam(IloCplex.IntParam.Threads, CPLEXTHREADS);
        if (CPLEXTHREADS == 1) cplex.setParam(IloCplex.IntParam.AuxRootThreads, -1);
        IloNumVar v0 = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, "v0");
        cplex.setOut(null);
        cplex.addMaximize(v0);
        objectiveForPlayers.put(player, v0);
    }

    public double calculateLeaderStrategies(int leaderIdx, int followerIdx, StackelbergConfig algConfig, Expander expander) {
        leader = players[leaderIdx];
        follower = players[followerIdx];

        double maxValue = Double.NEGATIVE_INFINITY;
        Set<Sequence> followerBR = new HashSet<Sequence>();
        Map<Sequence, Double> leaderResult = new HashMap<Sequence, Double>();

        try {



            IloCplex cplex = modelsForPlayers.get(leader);
            IloNumVar v0 = objectiveForPlayers.get(leader);


            long startTime = System.currentTimeMillis();
            createVariables(cplex, algConfig);
            createConstraintsForSets(leader, cplex, informationSets.get(leader));
            createConstraintsForSequences(algConfig, cplex, algConfig.getSequencesFor(follower));
            debugOutput.println("phase 1 done");
            overallConstraintGenerationTime += System.currentTimeMillis() - startTime;

            StackelbergConfig.PureRealizationPlanIterator i = algConfig.getIterator(follower, expander);
            while (i.hasNext()) {
                Set<Sequence> pureRP = i.next();
                setValueForBRSlack(cplex, pureRP, 0);
                updateObjective(cplex, v0, pureRP, algConfig);

			cplex.exportModel("stck-" + leader + ".lp"); // uncomment for model export
                startTime = System.currentTimeMillis();
                debugOutput.println("Solving");
                cplex.solve();
                overallConstraintLPSolvingTime += System.currentTimeMillis() - startTime;
                debugOutput.println("Status: " + cplex.getStatus());

                if (cplex.getCplexStatus() == CplexStatus.Optimal) {
                    double v = cplex.getValue(v0);
                    debugOutput.println("Best value is " + v + " for follower strategy: " + pureRP);
                    if (v > maxValue) {
                        maxValue = v;
                        resultStrategies.put(leader, createSolution(algConfig, leader, cplex));
                        followerBR = pureRP;
                        leaderResult = createSolution(algConfig, leader, cplex);
                    }
                }
                setValueForBRSlack(cplex, pureRP, 1);
            }
        } catch (IloException e) {
            e.printStackTrace();
        }

        resultStrategies.put(leader, leaderResult);
        resultValues.put(leader, maxValue);

        return maxValue;
    }

    protected void createConstraintsForSequences(StackelbergConfig<SequenceInformationSet> algConfig, IloCplex cplex, Collection<Sequence> VConstraints) throws IloException {
        for (Sequence firstPlayerSequence : VConstraints) {
            if (constraints.containsKey(firstPlayerSequence)) {
                cplex.delete(constraints.get(firstPlayerSequence));
                constraints.remove(firstPlayerSequence);
            }
            createConstraintForSequence(cplex, firstPlayerSequence, algConfig);
        }
    }

    protected void createVariables(IloCplex model, StackelbergConfig<SequenceInformationSet> algConfig) throws IloException {
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
        debugOutput.println("variables created");
    }

    protected IloNumVar createSlackVariableForSequence(IloCplex cplex, Sequence sequence) throws IloException {
        IloNumVar s = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "SL" + sequence.toString());
        slackVariables.put(sequence, s);
        return s;
    }

    protected static double getUtility(StackelbergConfig<SequenceInformationSet> algConfig, Map<Player, Sequence> sequenceCombination, Player firstPlayer) {
        Double utility = algConfig.getUtilityFor(sequenceCombination, firstPlayer);

        if (utility == null) {
            utility = 0d;
        }
        return utility;
    }

    protected IloNumExpr computeSumGR(IloCplex cplex, Sequence firstPlayerSequence, StackelbergConfig<SequenceInformationSet> algConfig, Player firstPlayer) throws IloException {
        IloNumExpr sumGR = cplex.constant(0);
        HashSet<Sequence> secondPlayerSequences = new HashSet<Sequence>();

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

    protected void createConstraintForSequence(IloCplex cplex, Sequence firstPlayerSequence, StackelbergConfig<SequenceInformationSet> algConfig) throws IloException {
        Player firstPlayer = firstPlayerSequence.getPlayer();
        InformationSet informationSet = firstPlayerSequence.getLastInformationSet();
        IloNumExpr VI = null;
        IloNumExpr sumV = cplex.constant(0);

        if (informationSet == null) {
            if (firstPlayer.equals(follower)) return;
            VI = objectiveForPlayers.get(firstPlayer);
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

    protected void setValueForBRSlack(IloCplex cplex, Set<Sequence> sequences, int value) throws IloException {
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

    protected void updateObjective(IloCplex cplex, IloNumVar v0, Set<Sequence> bestResponse, StackelbergConfig<SequenceInformationSet> algConfig) throws IloException{
        if (leaderObj != null) cplex.delete(leaderObj);
        IloNumExpr sumG = cplex.constant(0);
        for (Sequence s : bestResponse) {
            HashSet<Sequence> leaderCompSequences = new HashSet<Sequence>();

            if (algConfig.getCompatibleSequencesFor(s) != null)
                leaderCompSequences.addAll(algConfig.getCompatibleSequencesFor(s));

            for (Sequence ls : leaderCompSequences) {
                IloNumExpr prob = variables.get(ls);

                if (prob == null)
                    continue;
                Map<Player, Sequence> actions = createActions(ls, s);
                double utility = getUtility(algConfig, actions, leader);

                sumG = cplex.sum(sumG, cplex.prod(utility, prob));
            }
        }
        leaderObj = cplex.addEq(cplex.diff(v0, sumG),0);
    }
}
