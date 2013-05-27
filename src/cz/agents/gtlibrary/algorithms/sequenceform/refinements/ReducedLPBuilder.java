package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.IloException;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class ReducedLPBuilder extends LPBuilder {
	
	public static void main(String[] args) {
		runAoS();
//		runGoofSpiel();
//		runKuhnPoker();
//		runGenericPoker();
	}

	public static void runKuhnPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new KuhnPokerExpander<SequenceInformationSet>(algConfig), new KuhnPokerGameState(), algConfig);

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runGenericPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new GenericPokerExpander<SequenceInformationSet>(algConfig), new GenericPokerGameState(), algConfig);

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runAoS() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new AoSExpander<SequenceInformationSet>(algConfig), new AoSGameState(), algConfig);

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runGoofSpiel() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new GoofSpielExpander<SequenceInformationSet>(algConfig), new GoofSpielGameState(), algConfig);

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public ReducedLPBuilder(Expander<SequenceInformationSet> expander, GameState rootState, AlgorithmConfig<SequenceInformationSet> algConfig) {
		super(expander, rootState, algConfig);
		lpFileName = "reducedlp.lp";
	}
	
	public void initF() {
		lpTable.setConstraint(new Key("Q", lastKeys[1]), lastKeys[1], 1);//F in root (only 1)
	}

	public void initE() {
		lpTable.setConstraint(lastKeys[0], new Key("P", lastKeys[0]), 1);//E in root (only 1)
	}
	
	@Override
	protected void visitLeaf(GameState state) {
		lpTable.substractFromConstraint(lastKeys[0], lastKeys[1], state.getNatureProbability() * (state.getUtilities()[0] + utilityShift));
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

		lpTable.setConstraint(lastKeys[0], varKey, -1);//E
		lpTable.watchDualVariable(lastKeys[0], state.getSequenceForPlayerToMove());
		for (Action action : expander.getActions(state)) {
			updateLPForFirstPlayerChild(state.performAction(action), state.getPlayerToMove(), varKey);
		}
	}

	public void updateLPForFirstPlayerChild(GameState child, Player lastPlayer, Key varKey) {
		Key eqKey = new Key(child.getSequenceFor(lastPlayer));
		Key tmpKey = new Key("U", new Key(child.getSequenceFor(lastPlayer)));

		lpTable.watchDualVariable(eqKey, child.getSequenceFor(lastPlayer));
		lpTable.setConstraint(eqKey, varKey, 1);//E child

		lpTable.setConstraint(eqKey, tmpKey, -1);//u (eye)
		lpTable.setObjective(tmpKey, new EpsilonPolynom(epsilon, child.getSequenceFor(lastPlayer).size()));//k(\epsilon)
	}

	public void updateLPForSecondPlayer(GameState state) {
		Key eqKey = new Key("Q", new Key(state.getISKeyForPlayerToMove()));

		lpTable.setConstraint(eqKey, lastKeys[1], -1);//F
		lpTable.watchPrimalVariable(lastKeys[1], state.getSequenceForPlayerToMove());
		for (Action action : expander.getActions(state)) {
			updateLPForSecondPlayerChild(state.performAction(action), state.getPlayerToMove(), eqKey);
		}
	}

	public void updateLPForSecondPlayerChild(GameState child, Player lastPlayer, Key eqKey) {
		Key varKey = new Key(child.getSequenceFor(lastPlayer));
		Key tmpKey = new Key("V", new Key(child.getSequenceFor(lastPlayer)));

		lpTable.setConstraint(eqKey, varKey, 1);//F child
		lpTable.watchPrimalVariable(varKey, child.getSequenceFor(lastPlayer));
		lpTable.setConstraint(tmpKey, varKey, 1);//indices y
		lpTable.setConstant(tmpKey, new EpsilonPolynom(epsilon, child.getSequenceFor(lastPlayer).size()).negate());//l(\epsilon)
	}

}
