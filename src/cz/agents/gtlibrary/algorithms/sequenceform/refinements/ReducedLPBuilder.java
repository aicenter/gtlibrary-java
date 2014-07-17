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
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.EpsilonPolynom;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.*;
import ilog.concert.IloException;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ReducedLPBuilder extends LPBuilder {

	protected double utilityShift;

	public static void main(String[] args) {
//		runAoS();
//		runGoofSpiel();
//		runKuhnPoker();
		runGenericPoker();
	}

	public static void runKuhnPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new KuhnPokerExpander<SequenceInformationSet>(algConfig), new KuhnPokerGameState(), new KPGameInfo());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runGenericPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new GenericPokerExpander<SequenceInformationSet>(algConfig), new GenericPokerGameState(), new GPGameInfo());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runAoS() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new AoSExpander<SequenceInformationSet>(algConfig), new AoSGameState(), new AoSGameInfo());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runGoofSpiel() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new GoofSpielExpander<SequenceInformationSet>(algConfig), new GoofSpielGameState(), new GSGameInfo());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public ReducedLPBuilder(Expander<SequenceInformationSet> expander, GameState rootState, GameInfo info) {
		super(expander, rootState);
		lpFileName = "reducedlp.lp";
		utilityShift = info.getMaxUtility() + 1;
	}

	public void initF(Sequence p2EmptySequence) {
		lpTable.setConstraint(new Key("Q", p2EmptySequence), p2EmptySequence, 1);//F in root (only 1)
		lpTable.setConstraintType(new Key("Q", p2EmptySequence), 2);
	}

	public void initE(Sequence p1EmptySequence) {
		lpTable.setConstraint(p1EmptySequence, new Key("P", p1EmptySequence), 1);//E in root (only 1)
		lpTable.setLowerBound(new Key("P", p1EmptySequence), Double.NEGATIVE_INFINITY);
		lpTable.setUpperBound(new Key("P", p1EmptySequence), 0);
	}

	@Override
	protected void visitLeaf(GameState state) {
		updateParentLinks(state);
		lpTable.substractFromConstraint(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), state.getNatureProbability() * (state.getUtilities()[0] - utilityShift));
	}

	public Map<Sequence, Double> createFirstPlayerStrategy(IloCplex cplex, Map<Object, IloRange> watchedDualVars) throws IloException {
		Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();

		for (Entry<Object, IloRange> entry : watchedDualVars.entrySet()) {
			p1Strategy.put((Sequence) entry.getKey(), cplex.getDual(entry.getValue()));
		}
		return p1Strategy;
	}

	public void updateLPForFirstPlayer(GameState state) {
		Key varKey = new Key("P", new Key(state.getISKeyForPlayerToMove()));

		updateParentLinks(state);
		lpTable.setConstraint(state.getSequenceFor(players[0]), varKey, -1);//E
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
		lpTable.setUpperBound(varKey, 0);
		lpTable.watchDualVariable(state.getSequenceFor(players[0]), state.getSequenceForPlayerToMove());
	}

	@Override
	protected void updateP1Parent(GameState state) {
		Sequence p1Sequence = state.getSequenceFor(players[0]);

		if (p1Sequence.size() == 0)
			return;
		Object varKey = getLastISKey(p1Sequence);
		Key tmpKey = new Key("U", p1Sequence);

		lpTable.watchDualVariable(p1Sequence, p1Sequence);
		lpTable.setConstraint(p1Sequence, varKey, 1);//E child
		lpTable.setConstraint(p1Sequence, tmpKey, -1);//u (eye)
		lpTable.setObjective(tmpKey, new EpsilonPolynom(epsilon, p1Sequence.size()));//k(\epsilon)
	}

	public void updateLPForSecondPlayer(GameState state) {
		Key eqKey = new Key("Q", new Key(state.getISKeyForPlayerToMove()));

		updateParentLinks(state);
		lpTable.setConstraint(eqKey, state.getSequenceFor(players[1]), -1);//F
		lpTable.setConstraintType(eqKey, 2);
		lpTable.watchPrimalVariable(state.getSequenceFor(players[1]), state.getSequenceForPlayerToMove());
	}

	@Override
	protected void updateP2Parent(GameState state) {
		Sequence p2Sequence = state.getSequenceFor(players[1]);

		if (p2Sequence.size() == 0)
			return;
		Object eqKey = getLastISKey(p2Sequence);
		Key tmpKey = new Key("V", p2Sequence);

		lpTable.setConstraint(eqKey, p2Sequence, 1);//F child
		lpTable.setConstraintType(eqKey, 2);
		lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
		lpTable.setConstraint(tmpKey, p2Sequence, 1);//indices y
		lpTable.setConstant(tmpKey, new EpsilonPolynom(epsilon, p2Sequence.size()).negate());//l(\epsilon)
	}

}
