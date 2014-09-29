/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Epsilon;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.EpsilonPolynom;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LPBuilder extends TreeVisitor {

	protected String lpFileName;
	protected EpsilonLPTable lpTable;
	public Epsilon epsilon;

	public static void main(String[] args) {
//		runAoS();
//		runGoofSpiel();
		runKuhnPoker();
//		runGenericPoker();
	}

	public static void runKuhnPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new LPBuilder(new KuhnPokerExpander<>(algConfig), new KuhnPokerGameState());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runGenericPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new LPBuilder(new GenericPokerExpander<>(algConfig), new GenericPokerGameState());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runAoS() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new LPBuilder(new AoSExpander<>(algConfig), new AoSGameState());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runGoofSpiel() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new LPBuilder(new GoofSpielExpander<>(algConfig), new GoofSpielGameState());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public LPBuilder(Expander<SequenceInformationSet> expander, GameState rootState) {
		super(rootState, expander);
		this.expander = expander;
		epsilon = new Epsilon();
		lpFileName = "lp.lp";
	}

	public void buildLP() {
		initTable();
		visitTree(rootState);
		computeEpsilon();
		System.out.println("epsilon: " + epsilon);
	}

	public void solve() {
		try {
			LPData lpData = lpTable.toCplex();

//			lpData.getSolver().setParam(IloCplex.DoubleParam.EpMrk, 0.9999);
			lpData.getSolver().exportModel(lpFileName);
			System.out.println(lpData.getSolver().solve());
			System.out.println(lpData.getSolver().getStatus());
			System.out.println(lpData.getSolver().getObjValue());

			Map<Sequence, Double> p1RealizationPlan = createFirstPlayerStrategy(lpData.getSolver(), lpData.getWatchedDualVariables());
			Map<Sequence, Double> p2RealizationPlan = createSecondPlayerStrategy(lpData.getSolver(), lpData.getWatchedPrimalVariables());
//			System.out.println(p1RealizationPlan);
//			System.out.println(p2RealizationPlan);
			
			for (Entry<Sequence, Double> entry : p1RealizationPlan.entrySet()) {
				if(entry.getValue() > 0)
					System.out.println(entry);
			}
			for (Entry<Sequence, Double> entry : p2RealizationPlan.entrySet()) {
				if(entry.getValue() > 0)
					System.out.println(entry);
			}

			UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
			Strategy p1Strategy = new UniformStrategyForMissingSequences();
			Strategy p2Strategy = new UniformStrategyForMissingSequences();

			p1Strategy.putAll(p1RealizationPlan);
			p2Strategy.putAll(p2RealizationPlan);

//			System.out.println(p1Strategy.fancyToString(rootState, expander, new PlayerImpl(0)));
//			System.out.println("************************************");
//			System.out.println(p2Strategy.fancyToString(rootState, expander, new PlayerImpl(1)));
//			System.out.println("Solution: " + Arrays.toString(lpData.getSolver().getValues(lpData.getVariables())));
//			System.out.println("Dual solution: " + Arrays.toString(lpData.getSolver().getDuals(lpData.getConstraints())));
//			System.out.println(lpData.getWatchedPrimalVariables());
//			System.out.println(lpData.getWatchedDualVariables());
			System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));
			p1Strategy.sanityCheck(rootState, expander);
			p2Strategy.sanityCheck(rootState, expander);
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

	public void initTable() {
		Sequence p1EmptySequence = new LinkedListSequenceImpl(players[0]);
		Sequence p2EmptySequence = new LinkedListSequenceImpl(players[1]);
		
		lpTable = new EpsilonLPTable();

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

	@Override
	protected void visitLeaf(GameState state) {
		updateParentLinks(state);
		lpTable.substractFromConstraint(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), state.getNatureProbability() * state.getUtilities()[0]);
	}

	@Override
	protected void visitNormalNode(GameState state) {
		if (state.getPlayerToMove().getId() == 0) {
			updateLPForFirstPlayer(state);
		} else {
			updateLPForSecondPlayer(state);
		}
		super.visitNormalNode(state);
	}

	public void updateLPForFirstPlayer(GameState state) {
		Key varKey = new Key("P", new Key(state.getISKeyForPlayerToMove()));

		updateParentLinks(state);
		lpTable.setConstraint(state.getSequenceFor(players[0]), varKey, -1);//E
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
		lpTable.watchDualVariable(state.getSequenceFor(players[0]), state.getSequenceForPlayerToMove());
	}

	public void updateLPForSecondPlayer(GameState state) {
		Key eqKey = new Key("Q", new Key(state.getISKeyForPlayerToMove()));

		updateParentLinks(state);
		lpTable.setConstraint(eqKey, state.getSequenceFor(players[1]), -1);//F
		lpTable.setConstraintType(eqKey, 1);
		lpTable.watchPrimalVariable(state.getSequenceFor(players[1]), state.getSequenceForPlayerToMove());
	}

	@Override
	protected void visitChanceNode(GameState state) {
		updateParentLinks(state);
		super.visitChanceNode(state);
	}

	public void updateParentLinks(GameState state) {
		updateP1Parent(state);
		updateP2Parent(state);
	}
	
	protected void updateP1Parent(GameState state) {
		Sequence p1Sequence = state.getSequenceFor(players[0]);
		
		if(p1Sequence.size() == 0) 
			return;
		Object varKey = getLastISKey(p1Sequence);
		Key tmpKey = new Key("U", p1Sequence);

		lpTable.watchDualVariable(p1Sequence, p1Sequence);
		lpTable.setConstraint(p1Sequence, varKey, 1);//E child
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);

		lpTable.setConstraint(p1Sequence, tmpKey, -1);//u (eye)
		lpTable.setObjective(tmpKey, new EpsilonPolynom(epsilon, p1Sequence.size()));//k(\epsilon)
	}

	protected void updateP2Parent(GameState state) {
		Sequence p2Sequence = state.getSequenceFor(players[1]);
		
		if(p2Sequence.size() == 0) 
			return;
		Object eqKey = getLastISKey(p2Sequence);
		Key tmpKey = new Key("V", p2Sequence);

		lpTable.setConstraint(eqKey, p2Sequence, 1);//F child
		lpTable.setConstraintType(eqKey, 1);
		lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
		lpTable.setConstraint(tmpKey, p2Sequence, 1);//indices y
		lpTable.setConstant(tmpKey, new EpsilonPolynom(epsilon, p2Sequence.size()).negate());//l(\epsilon)
	}
	
	protected void computeEpsilon() {
		double equationCount = lpTable.rowCount();
		double maxCoefficient = lpTable.getMaxCoefficient();

		epsilon.setValue(0.5 * Math.pow(equationCount, -equationCount - 1) * Math.pow(maxCoefficient, -2 * equationCount - 1));
	}

}
