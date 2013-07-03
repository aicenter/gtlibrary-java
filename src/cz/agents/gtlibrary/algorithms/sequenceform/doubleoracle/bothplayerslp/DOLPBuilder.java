package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.bothplayerslp;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class DOLPBuilder {

	protected String lpFileName;
	protected LPTable lpTable;
	protected Player[] players;
	protected PrintStream output;
	protected Double p1Value;
	protected Map<Sequence, Double> p1RealizationPlan;
	protected Map<Sequence, Double> p2RealizationPlan;
	protected long generationTime;
	protected long constraintGenerationTime;
	protected long lpSolvingTime;

	public DOLPBuilder(Player[] players) {
		this.players = players;
		lpFileName = "DO_LP_mod.lp";
		p1Value = Double.NaN;
		
		initTable();
	}
	
	public void initTable() {
		Sequence p1EmptySequence = new LinkedListSequenceImpl(players[0]);
		Sequence p2EmptySequence = new LinkedListSequenceImpl(players[1]);
		
		if(lpTable == null)
			lpTable = new LPTable();
		lpTable.clearTable();

		initCost(p1EmptySequence);
		initE(p1EmptySequence);
		initF(p2EmptySequence);
		initf(p2EmptySequence);
	}

	public void initf(Sequence p2EmptySequence) {
		lpTable.setConstant(new Key("Q", p2EmptySequence), -1);//f for root
	}

	public void initF(Sequence p2EmptySequence) {
		lpTable.setConstraint(new Key("Q", p2EmptySequence), p2EmptySequence, 1);//F in root (only 1)
		lpTable.setConstraintType(new Key("Q", p2EmptySequence), 1);
	}

	public void initE(Sequence p1EmptySequence) {
		lpTable.setConstraint(p1EmptySequence, new Key("P", p1EmptySequence), 1);//E in root (only 1)
		lpTable.setLowerBound(new Key("P", p1EmptySequence), Double.NEGATIVE_INFINITY);
	}

	public void initCost(Sequence p1EmptySequence) {
		lpTable.setObjective(new Key("P", p1EmptySequence), -1);
	}

	public void solve() {
		try {
			long generationStart = System.currentTimeMillis();
			LPData lpData = lpTable.toCplex();
			constraintGenerationTime += System.currentTimeMillis() - generationStart;

//			lpData.getSolver().setParam(IloCplex.DoubleParam.EpMrk, 0.9999);
//			lpData.getSolver().exportModel(lpFileName);
			
			long lpStart = System.currentTimeMillis();
			
			output.println(lpData.getSolver().solve());
			lpSolvingTime += System.currentTimeMillis() - lpStart;
//			output.println(lpData.getSolver().getStatus());
//			output.println(lpData.getSolver().getObjValue());

			p1Value = -lpData.getSolver().getObjValue();

			p1RealizationPlan = createFirstPlayerStrategy(lpData.getSolver(), lpData.getWatchedDualVariables());
			p2RealizationPlan = createSecondPlayerStrategy(lpData.getSolver(), lpData.getWatchedPrimalVariables());
//			output.println(p1RealizationPlan);
//			output.println(p2RealizationPlan);
//			output.println("Solution: " + Arrays.toString(lpData.getSolver().getValues(lpData.getVariables())));
//			output.println("Dual solution: " + Arrays.toString(lpData.getSolver().getDuals(lpData.getConstraints())));

//			for (Entry<Sequence, Double> entry : p1RealizationPlan.entrySet()) {
//				if (entry.getValue() > 0)
//					output.println(entry);
//			}
//			for (Entry<Sequence, Double> entry : p2RealizationPlan.entrySet()) {
//				if (entry.getValue() > 0)
//					output.println(entry);
//			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public Map<Sequence, Double> createFirstPlayerStrategy(IloCplex cplex, Map<Object, IloRange> watchedDualVars) throws IloException {
		Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();

		for (Entry<Object, IloRange> entry : watchedDualVars.entrySet()) {
			p1Strategy.put((Sequence) entry.getKey(), cplex.getDual(entry.getValue()));
		}
		return p1Strategy;
	}

	public Map<Sequence, Double> createSecondPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
		Map<Sequence, Double> p2Strategy = new HashMap<Sequence, Double>();

		for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
			p2Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
		}
		return p2Strategy;
	}

	public void calculateStrategyForPlayer(int playerIndex, GameState root, DoubleOracleConfig<DoubleOracleInformationSet> config, double currentBoundSize) {
		long startTime = System.currentTimeMillis();
		
		p1Value = Double.NaN;
		initTable();
		for (Sequence sequence : config.getSequencesFor(players[0])) {
			updateForP1(sequence);
		}
		for (Sequence sequence : config.getSequencesFor(players[1])) {
			updateForP2(sequence);
		}
		addUtilities(config, config.getSequencesFor(players[0]), config.getSequencesFor(players[1]));
		constraintGenerationTime += System.currentTimeMillis() - startTime;
		generationTime += System.currentTimeMillis() - startTime;
	}

	public void updateForP2(Sequence sequence) {
		if (sequence.size() == 0)
			return;
		Object eqKey = getKeyForIS("Q", sequence);
		Object varKey = getSubsequenceKey(sequence);

		lpTable.setConstraint(eqKey, varKey, -1);//F
		lpTable.setConstraintType(eqKey, 1);
		lpTable.watchPrimalVariable(varKey, sequence.getSubSequence(sequence.size() - 1));

		addLinksToPrevISForP2(sequence, eqKey);
	}

	public void updateForP1(Sequence sequence) {
		if (sequence.size() == 0)
			return;
		Object varKey = getKeyForIS("P", sequence);
		Object eqKey = getSubsequenceKey(sequence);

		lpTable.setConstraint(eqKey, varKey, -1);//E
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
		lpTable.watchDualVariable(eqKey, sequence.getSubSequence(sequence.size() - 1));

		addLinksToPrevISForP1(sequence, varKey);
	}

	protected void addLinksToPrevISForP2(Sequence sequence, Object eqKey) {
		SequenceInformationSet set = (SequenceInformationSet) sequence.getLastInformationSet();

		for (Sequence outgoingSequence : set.getOutgoingSequences()) {
			Key tmpKey = new Key("V", outgoingSequence);

			lpTable.setConstraint(eqKey, outgoingSequence, 1);//F child
			lpTable.setConstraintType(eqKey, 1);
			lpTable.watchPrimalVariable(outgoingSequence, outgoingSequence);
			lpTable.setConstraint(tmpKey, outgoingSequence, 1);//indices y
//			lpTable.setConstant(tmpKey, 0);//l(\epsilon)
		}

	}

	public void addLinksToPrevISForP1(Sequence sequence, Object varKey) {
		SequenceInformationSet set = (SequenceInformationSet) sequence.getLastInformationSet();

		for (Sequence outgoingSequence : set.getOutgoingSequences()) {
			Key tmpKey = new Key("U", outgoingSequence);

			lpTable.watchDualVariable(outgoingSequence, outgoingSequence);
			lpTable.setConstraint(outgoingSequence, varKey, 1);//E child
			lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);

			lpTable.setConstraint(outgoingSequence, tmpKey, -1);//u (eye)
//			lpTable.setObjective(tmpKey, 0);//k(\epsilon)
		}
	}

	protected Object getSubsequenceKey(Sequence sequence) {
		return sequence.getSubSequence(sequence.size() - 1);
	}

	protected Object getKeyForIS(String string, Sequence sequence) {
		if (sequence.size() == 0)
			return sequence.getPlayer();
		return new Key(string, sequence.getLastInformationSet());
	}

	protected void addUtilities(DoubleOracleConfig<DoubleOracleInformationSet> config, Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
		for (Sequence p1Sequence : p1Sequences) {
			for (Sequence p2Sequence : p2Sequences) {
				Double utility = config.getUtilityForSequences(p1Sequence, p2Sequence);

				if (utility != null) {
					lpTable.substractFromConstraint(p1Sequence, p2Sequence, utility);
				}
			}
		}
	}

	public Double getResultForPlayer(Player player) {
		if (Double.isNaN(p1Value))
			solve();
		return (player.equals(players[0]) ? 1 : -1) * p1Value;
	}

	public void setDebugOutput(PrintStream debugOutput) {
		output = debugOutput;
	}

	public Map<Sequence, Double> getResultStrategiesForPlayer(Player player) {
		if (Double.isNaN(p1Value))
			solve();
		return player.equals(players[0]) ? p1RealizationPlan : p2RealizationPlan;
	}

	public long getOverallGenerationTime() {
		return generationTime;
	}

	public long getOverallConstraintGenerationTime() {
		return constraintGenerationTime;
	}

	public long getOverallConstraintLPSolvingTime() {
		return lpSolvingTime;
	}

}
