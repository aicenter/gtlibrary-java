package cz.agents.gtlibrary.algorithms.sequenceform;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.CplexStatus;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class SequenceFormLP {

    private long overallConstraintGenerationTime = 0;
    private long overallConstraintLPSolvingTime = 0;

	protected Map<Player, Double> resultValues = new FixedSizeMap<Player, Double>(2);
	protected Map<Player, Map<Sequence, Double>> resultStrategies = new FixedSizeMap<Player, Map<Sequence, Double>>(2);
	protected Map<Object, IloRange> constraints = new HashMap<Object, IloRange>();
	protected Map<Object, IloNumVar> variables = new HashMap<Object, IloNumVar>();
	protected Map<Player, IloCplex> modelsForPlayers = new FixedSizeMap<Player, IloCplex>(2);
	protected Map<Player, IloNumVar> objectiveForPlayers = new FixedSizeMap<Player, IloNumVar>(2);
	protected Map<Player, Set<Sequence>> sequences = new FixedSizeMap<Player, Set<Sequence>>(2);
	protected Map<Player, Set<SequenceInformationSet>> informationSets = new FixedSizeMap<Player, Set<SequenceInformationSet>>(2);

	public SequenceFormLP(Player[] players) {
		for (Player player : players) {
			if (!resultStrategies.containsKey(player))
				resultStrategies.put(player, new HashMap<Sequence, Double>());
			if (!modelsForPlayers.containsKey(player)) {
				try {
					createModelFor(player);
				} catch (IloException e) {
					e.printStackTrace();
				}
			}
			sequences.put(player, new HashSet<Sequence>());
			informationSets.put(player, new HashSet<SequenceInformationSet>());
		}
	}

	public Double calculateBothPlStrategy(GameState root, SequenceFormConfig<SequenceInformationSet> algConfig) {
		try {
			createVariables(algConfig, root.getAllPlayers());
			calculateOnePlStrategy(algConfig, root, root.getAllPlayers()[1], root.getAllPlayers()[0]);
			return calculateOnePlStrategy(algConfig, root, root.getAllPlayers()[0], root.getAllPlayers()[1]);
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void createModelFor(Player player) throws IloException {
		IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Dual);
        IloNumVar v0 = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, "v0");
		cplex.setOut(null);
		cplex.addMinimize(v0);
		modelsForPlayers.put(player, cplex);
		objectiveForPlayers.put(player, v0);
	}

	protected void createVariables(SequenceFormConfig<SequenceInformationSet> algConfig, Player[] players) throws IloException {
		for (Sequence sequence : algConfig.getAllSequences()) {
			if (!variables.containsKey(sequence)) {
				createVariableForSequence(modelsForPlayers.get(players[0]), sequence);
				createVariableForSequence(modelsForPlayers.get(players[1]), sequence);
				sequences.get(sequence.getPlayer()).add(sequence);
			}
		}
		for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
			if (!variables.containsKey(informationSet)) {
				createVariableForIS(modelsForPlayers.get(players[0]), informationSet);
				createVariableForIS(modelsForPlayers.get(players[1]), informationSet);
				informationSets.get(informationSet.getPlayer()).add(informationSet);
			}
		}
		System.out.println("variables created");
	}

	protected Double calculateOnePlStrategy(SequenceFormConfig<SequenceInformationSet> algConfig, GameState root, Player firstPlayer, Player secondPlayer) {
		try {
			IloCplex cplex = modelsForPlayers.get(firstPlayer);
			IloNumVar v0 = objectiveForPlayers.get(firstPlayer);
            long startTime = System.currentTimeMillis();
			createConstraintsForSequences(algConfig, cplex, sequences.get(firstPlayer));
			System.out.println("phase 1 done");
			createConstraintsForSets(secondPlayer, cplex, informationSets.get(secondPlayer));
			System.out.println("phase 2 done");
            overallConstraintGenerationTime += System.currentTimeMillis() - startTime;

			cplex.exportModel("gt-lib-sqf-rnd-" + firstPlayer + ".lp"); // uncomment for model export
            startTime = System.currentTimeMillis();
			System.out.println("Solving");
			cplex.solve();
            overallConstraintLPSolvingTime += System.currentTimeMillis() - startTime;
			System.out.println("Status: " + cplex.getStatus());
			
			if (cplex.getCplexStatus() != CplexStatus.Optimal) {
				return null;
			}

			resultStrategies.put(secondPlayer, createSolution(algConfig, secondPlayer, cplex));
			resultValues.put(firstPlayer, cplex.getValue(v0));

			return cplex.getValue(v0);
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected Map<Sequence, Double> createSolution(SequenceFormConfig<SequenceInformationSet> algConfig, Player secondPlayer, IloCplex cplex) throws IloException {
		Map<Sequence, Double> solution = new HashMap<Sequence, Double>();

		for (Sequence sequence : algConfig.getSequencesFor(secondPlayer)) {
			try {
				double relPl = cplex.getValue(variables.get(sequence));

				if (sequence.size() == 0)
					relPl = 1;
				solution.put(sequence, relPl);
			} catch (UnknownObjectException e) {
				if (sequence.size() == 0)
					solution.put(sequence, 1d);
				else
					solution.put(sequence, 0d);
			}
		}
		return solution;
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

	protected void createConstraintsForSequences(SequenceFormConfig<SequenceInformationSet> algConfig, IloCplex cplex, Set<Sequence> VConstraints) throws IloException {
		for (Sequence firstPlayerSequence : VConstraints) {
			if (constraints.containsKey(firstPlayerSequence)) {
				cplex.delete(constraints.get(firstPlayerSequence));
				constraints.remove(firstPlayerSequence);
			}
			createConstraintForSequence(cplex, firstPlayerSequence, algConfig);
		}
	}

	private static double getUtility(SequenceFormConfig<SequenceInformationSet> algConfig, Map<Player, Sequence> sequenceCombination) {
		Double utility = algConfig.getUtilityForSequenceCombination(sequenceCombination);

		if (utility == null) {
			utility = 0d;
		}
		return utility;
	}

	public Map<Player, Sequence> createActions(Sequence firstPlayerSequence, Sequence secondPlayerSequence) {
		Map<Player, Sequence> actions = new HashMap<Player, Sequence>();

		actions.put(firstPlayerSequence.getPlayer(), new LinkedListSequenceImpl(firstPlayerSequence));
		actions.put(secondPlayerSequence.getPlayer(), new LinkedListSequenceImpl(secondPlayerSequence));
		return actions;
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

	protected void createConstraintForSequence(IloCplex cplex, Sequence firstPlayerSequence, SequenceFormConfig<SequenceInformationSet> algConfig) throws IloException {
		Player firstPlayer = firstPlayerSequence.getPlayer();
		InformationSet informationSet = firstPlayerSequence.getLastInformationSet();
		IloNumExpr VI = null;
		IloNumExpr sumV = cplex.constant(0);

		if (informationSet == null) {
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
		IloRange con = cplex.addGe(cplex.diff(cplex.diff(VI, sumV), sumGR), 0, "CON:" + firstPlayerSequence.toString());
		constraints.put(firstPlayerSequence, con);
	}

	private IloNumExpr computeSumGR(IloCplex cplex, Sequence firstPlayerSequence, SequenceFormConfig<SequenceInformationSet> algConfig, Player firstPlayer) throws IloException {
		IloNumExpr sumGR = cplex.constant(0);
		HashSet<Sequence> secondPlayerSequences = new HashSet<Sequence>();

		if (algConfig.getCompatibleSequencesFor(firstPlayerSequence) != null)
			secondPlayerSequences.addAll(algConfig.getCompatibleSequencesFor(firstPlayerSequence));

		for (Sequence secondPlayerSequence : secondPlayerSequences) {
			IloNumExpr prob = variables.get(secondPlayerSequence);

			if (prob == null)
				continue;
			Map<Player, Sequence> actions = createActions(firstPlayerSequence, secondPlayerSequence);
			double utility = getUtility(algConfig, actions);

			utility = utility * ((firstPlayer.getId() == 1) ? -1 : 1);
			sumGR = cplex.sum(sumGR, cplex.prod(utility, prob));
		}
		return sumGR;
	}

	public Double getResultForPlayer(Player p) {
		return resultValues.get(p);
	}
	
	public Map<Sequence, Double> getResultStrategiesForPlayer(Player p) {
		return resultStrategies.get(p);
	}

    public long getOverallConstraintLPSolvingTime() {
        return overallConstraintLPSolvingTime;
    }

    public long getOverallConstraintGenerationTime() {
        return overallConstraintGenerationTime;
    }
}
