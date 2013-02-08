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

public class GeneralSequenceFormLP {

	public static Map<Player, Double> resultValues = new FixedSizeMap<Player, Double>(2);
	public static Map<Player, Map<Sequence, Double>> resultStrategies = new FixedSizeMap<Player, Map<Sequence,Double>>(2);
	public static Map<Object, IloRange> constraints = new HashMap<Object, IloRange>();
	public static Map<Object, IloNumVar> variables = new HashMap<Object, IloNumVar>();
	public static Map<Player, IloCplex> modelsForPlayers = new FixedSizeMap<Player, IloCplex>(2);
	public static Map<Player, IloNumVar> objectiveForPlayers = new FixedSizeMap<Player, IloNumVar>(2);
	
	public static boolean init = false;
	
	public static Map<Player, Set<Sequence>> newSequences = new FixedSizeMap<Player, Set<Sequence>>(2);
	public static Map<Player, Set<SequenceInformationSet>> newInformationSets = new FixedSizeMap<Player, Set<SequenceInformationSet>>(2);

	public static Double calculateBothPlStrategy(GameState root, SequenceFormConfig algConfig , Player[] players) {
		try {		
			if (!init) {
				initialize(players);
			}
			createVariables(algConfig, players);
			calculateOnePlStrategy(algConfig, root, players[1], players[0]);
			return calculateOnePlStrategy(algConfig, root, players[0], players[1]);
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}		
	}

	private static void initialize(Player[] players) throws IloException {
		for (Player player : players) {
			if (!resultStrategies.containsKey(player)) 
				resultStrategies.put(player, new HashMap<Sequence, Double>());
			if (!modelsForPlayers.containsKey(player)) {
				createModelFor(player);
			}
			newSequences.put(player, new HashSet<Sequence>());
			newInformationSets.put(player, new HashSet<SequenceInformationSet>());
		}
		init = true;
	}

	private static void createModelFor(Player player) throws IloException {
		IloCplex cplex = new IloCplex();
		IloNumVar v0 = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, "v0");
		
		cplex.addMinimize(v0);
		modelsForPlayers.put(player, cplex);
		objectiveForPlayers.put(player, v0);
	}

	private static void createVariables(SequenceFormConfig algConfig, Player[] players) throws IloException {
		for (Sequence sequence : algConfig.getAllSequences()) {
			if (!variables.containsKey(sequence)) {
				createVariableForSequence(modelsForPlayers.get(players[0]), sequence);
				createVariableForSequence(modelsForPlayers.get(players[1]), sequence);
				newSequences.get(sequence.getPlayer()).add(sequence);
			}				
		}
		for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
			if (!variables.containsKey(informationSet)) {
				createVariableForIS(modelsForPlayers.get(players[0]), informationSet);
				createVariableForIS(modelsForPlayers.get(players[1]), informationSet);
				newInformationSets.get(informationSet.getPlayer()).add(informationSet);
			}
		}
		System.out.println("variables created");
	}

	public static Double calculateOnePlStrategy(SequenceFormConfig algConfig, GameState root, Player firstPlayer, Player secondPlayer) {
		try {

			IloCplex cplex = modelsForPlayers.get(firstPlayer);
			IloNumVar v0 = objectiveForPlayers.get(firstPlayer); 
			
			HashSet<Sequence> newVConstraints = new HashSet<Sequence>();
			newVConstraints.addAll(newSequences.get(firstPlayer));


			HashSet<SequenceInformationSet> newRConstraints = new HashSet<SequenceInformationSet>();
			newRConstraints.addAll(newInformationSets.get(secondPlayer));
//			System.out.println("NEWIS:"+newInformationSets.get(firstPlayer).size() + " " + newInformationSets.get(secondPlayer).size());
//			System.out.println("NEWSQ:"+newSequences.get(firstPlayer).size() + " " + newSequences.get(secondPlayer).size());
//			System.out.println("NEWIS:"+newInformationSets.get(secondPlayer)+"\nALLIS:"+dataStorage.getInformationSetsForPlayer(secondPlayer));
//			newRConstraints.addAll(dataStorage.getInformationSetsForPlayer(secondPlayer));

		
/*			for (InformationSet i : newInformationSets.get(firstPlayer)) {
				if (dataStorage.getOutgoingSequencesForIS(i) != null) newVConstraints.addAll(dataStorage.getOutgoingSequencesForIS(i));
				newVConstraints.add(i.getPlayersHistory());
			}
			
			for (Sequence secondPlayerSequence : newSequences.get(secondPlayer)) {
//			for (Sequence secondPlayerSequence : dataStorage.getSequencesFor(secondPlayer)) {
				// all compatible sequences must be recalculated
				if (dataStorage.getCompatibleSequencesFor(secondPlayerSequence) != null) 
					for (Sequence fps : dataStorage.getCompatibleSequencesFor(secondPlayerSequence)) 
						newVConstraints.add(fps);
				
				if (secondPlayerSequence.getSize() > 0) {
					Sequence s222 = secondPlayerSequence.getSubSequence(secondPlayerSequence.getSize()-1);
					if (dataStorage.getCompatibleSequencesFor(s222) != null) 
						for (Sequence fps : dataStorage.getCompatibleSequencesFor(s222)) 
							newVConstraints.add(fps);
				}
				
				// only non-zero incomp. sequences 
				if (dataStorage.getUBSequences(secondPlayerSequence) != null)
					for (Sequence fps : dataStorage.getUBSequences(secondPlayerSequence)) {
						if (getUtility(dataStorage, createActions(fps, secondPlayerSequence), root) != 0) {
							newVConstraints.add(fps);
						}
				}
				if (dataStorage.getISForSequence(secondPlayerSequence) != null)
					newRConstraints.add(dataStorage.getISForSequence(secondPlayerSequence));
			}
			
			for (InformationSet is : newInformationSets.get(secondPlayer)) {
				if (is.getCompatibleSequences() != null)
				for (Tuple<IIGameState, Sequence> t : is.getCompatibleSequences()) {
					newVConstraints.add(t.getSecond());
				}
			} //*/
			
			// all new constraints prepared
			
			for (Sequence firstPlayerSequence : newVConstraints) {
				assert (firstPlayerSequence.getPlayer().equals(firstPlayer));
				if (constraints.containsKey(firstPlayerSequence)) {
					cplex.delete(constraints.get(firstPlayerSequence));					
					constraints.remove(firstPlayerSequence);
				}
				createConstraintForSequence(cplex, firstPlayerSequence, algConfig, root);
			}
			
			System.out.println("phase 1 done");

			for (SequenceInformationSet secondPlayerIS : newRConstraints) {
				assert (secondPlayerIS.getPlayer().equals(secondPlayer));
				if (constraints.containsKey(secondPlayerIS)) {
					cplex.delete(constraints.get(secondPlayerIS));
					constraints.remove(secondPlayerIS);
				}
				createConstraintForIS(cplex, secondPlayerIS);
			}

			System.out.println("phase 2 done");

//			cplex.exportModel("test-" + firstPlayer + ".lp");
			cplex.exportModel("aamas-test-" + firstPlayer + ".lp");
//			cplex.exportModel("newLP-aamasDO-" + firstPlayer + ".lp");
			
			// Reusing the last values
//			loadPreviousBiases(cplex, firstPlayer);
			
			System.out.println("Solving");
			cplex.solve();
			System.out.println("Status: " + cplex.getStatus());
			if (cplex.getCplexStatus() != CplexStatus.Optimal) {
//				assert false;
				return null;
			}

//			saveLastBiases(cplex, firstPlayer);
			
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

			resultStrategies.put(secondPlayer, solution);
			resultValues.put(firstPlayer, cplex.getValue(v0));

//			for (InformationSet informationSet : dataStorage.getInformationSetsForPlayer(firstPlayer)) {
//				try {
//					expValues.put(informationSet, cplex.getValue(variables.get(informationSet)));
//				} catch (UnknownObjectException e) {
//					expValues.put(informationSet, 0d);
//				}
//			}

			return cplex.getValue(v0);

		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static double getUtility(SequenceFormConfig algConfig, Map<Player, Sequence> sequenceCombination) {
		Double utility = algConfig.getUtilityForSequenceCombination(sequenceCombination);
		
//		if (utility == null && dataStorage.getUpperBound(actions) != null) {
//			utility = dataStorage.getUpperBound(actions);
//		} 
		
		if (utility == null) {
			utility = 0d;
		}
		
		return utility;
	}

	public static Map<Player, Sequence> createActions(Sequence firstPlayerSequence, Sequence secondPlayerSequence) {
		Map<Player, Sequence> actions = new HashMap<Player, Sequence>();

		actions.put(firstPlayerSequence.getPlayer(), new LinkedListSequenceImpl(firstPlayerSequence));
		actions.put(secondPlayerSequence.getPlayer(), new LinkedListSequenceImpl(secondPlayerSequence));
		return actions;
	}

	private static IloNumVar createVariableForIS(IloCplex cplex, InformationSet is) throws IloException {
		double ub = Double.POSITIVE_INFINITY;
		
//		if (expValues.containsKey(is)) {
//			ub = expValues.get(is);
//			expValues.remove(is);
//		}
		IloNumVar v = cplex.numVar(Double.NEGATIVE_INFINITY, ub, IloNumVarType.Float, "V" + is.toString());
		
		variables.put(is, v);		
		return v;
	}
	
	private static IloNumVar createVariableForSequence(IloCplex cplex, Sequence sequence) throws IloException{
		IloNumVar r = cplex.numVar(0, 1, IloNumVarType.Float, "R" + sequence.toString());
		
		if (sequence.size() == 0) 
			r.setLB(1d);
		variables.put(sequence, r);
		return r;
	}
	
	private static IloRange createConstraintForIS(IloCplex cplex, SequenceInformationSet informationSet) throws IloException {
		IloNumExpr sumL = cplex.constant(0);
		
		for (Sequence sequence : informationSet.getOutgoingSequences()) {
			if (variables.get(sequence) == null)
				continue;
			sumL = cplex.sum(sumL, variables.get(sequence));
		}
		
		Sequence sequence = informationSet.getPlayersHistory();
		IloNumExpr sumR = variables.get(sequence);
		
		if (sumR == null) 
			return null;
		
		IloRange contsrain = cplex.addEq(cplex.diff(sumL, sumR), 0,"CON:"+informationSet.toString());
		
		constraints.put(informationSet, contsrain);
		return contsrain;
	}

	private static void createConstraintForSequence(IloCplex cplex, Sequence firstPlayerSequence, SequenceFormConfig algConfig, GameState root) throws IloException {
		Player firstPlayer = firstPlayerSequence.getPlayer();
		InformationSet informationSet = firstPlayerSequence.getLastInformationSet();
		IloNumExpr VI = null;
		IloNumExpr sumV = cplex.constant(0);

		if (informationSet == null) {
			VI = objectiveForPlayers.get(firstPlayer);
			for (SequenceInformationSet reachableSet : algConfig.getReachableSets(firstPlayerSequence)) {
				IloNumVar tmp = variables.get(reachableSet);
				
				assert (tmp != null);
				sumV = cplex.sum(sumV, tmp);
			}
		} else {
			VI = variables.get(informationSet);
			for (SequenceInformationSet reachableSet : algConfig.getReachableSets(firstPlayerSequence)) {
				IloNumVar tmp = variables.get(reachableSet);
				
				assert (tmp != null);
				if (reachableSet.getOutgoingSequences() == null || reachableSet.getOutgoingSequences().size() == 0)
					continue;
				sumV = cplex.sum(sumV, tmp);
			}
		}

		IloNumExpr sumGR = cplex.constant(0);
		HashSet<Sequence> secondPlayerSequences = new HashSet<Sequence>();
		
		if (algConfig.getCompatibleSequencesFor(firstPlayerSequence) != null)
			secondPlayerSequences.addAll(algConfig.getCompatibleSequencesFor(firstPlayerSequence));

//		if (algConfig.getUBSequences(firstPlayerSequence) != null)
//			secondPlayerSequences.addAll(algConfig.getUBSequences(firstPlayerSequence));

		for (Sequence secondPlayerSequence : secondPlayerSequences) {
			IloNumExpr prob = variables.get(secondPlayerSequence);
			
			if (prob == null) 
				continue;
			Map<Player, Sequence> actions = createActions(firstPlayerSequence, secondPlayerSequence);
			double utility = getUtility(algConfig, actions);

			utility = utility * ((firstPlayer.getId() == 1) ? -1 : 1);
			sumGR = cplex.sum(sumGR, cplex.prod(utility, prob));
		}

		IloRange con = cplex.addGe(cplex.diff(cplex.diff(VI, sumV), sumGR), 0, "CON:"+firstPlayerSequence.toString());
		constraints.put(firstPlayerSequence, con);
	}
//	
//	private static void loadPreviousBiases(IloCplex cplex, Player firstPlayer) throws IloException {
//		if (lastUsedVariables.get(firstPlayer) == null || lastUsedVariables.get(firstPlayer).length == 0 || 
//			lastUsedConstraints.get(firstPlayer) == null || lastUsedConstraints.get(firstPlayer).length == 0) return;
//		cplex.setBasisStatuses(lastUsedVariables.get(firstPlayer), lastUsedVariableBiases.get(firstPlayer),
//							null, null);
//	}
//	
//	private static void saveLastBiases(IloCplex cplex, Player firstPlayer) throws IloException {
//		lastUsedVariables.put(firstPlayer, cplex.LPMatrix().getNumVars());
//		lastUsedVariableBiases.put(firstPlayer, cplex.getBasisStatuses(cplex.LPMatrix().getNumVars()));
//		lastUsedConstraints.put(firstPlayer, cplex.LPMatrix().getRanges());
//		lastUsedConstraintsBiases.put(firstPlayer, cplex.getBasisStatuses(cplex.LPMatrix().getRanges()));
//	}
}
