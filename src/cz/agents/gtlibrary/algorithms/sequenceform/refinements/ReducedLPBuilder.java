package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import ilog.concert.IloException;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.numbers.EpsilonPolynom;
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
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class ReducedLPBuilder extends LPBuilder {
	
	protected double utilityShift;
	
	public static void main(String[] args) {
		runAoS();
//		runGoofSpiel();
//		runKuhnPoker();
//		runGenericPoker();
	}

	public static void runKuhnPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new KuhnPokerExpander<SequenceInformationSet>(algConfig), new KuhnPokerGameState(), algConfig, new KPGameInfo());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runGenericPoker() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new GenericPokerExpander<SequenceInformationSet>(algConfig), new GenericPokerGameState(), algConfig, new GPGameInfo());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runAoS() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new AoSExpander<SequenceInformationSet>(algConfig), new AoSGameState(), algConfig, new AoSGameInfo());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public static void runGoofSpiel() {
		AlgorithmConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		LPBuilder lpBuilder = new ReducedLPBuilder(new GoofSpielExpander<SequenceInformationSet>(algConfig), new GoofSpielGameState(), algConfig, new GSGameInfo());

		lpBuilder.buildLP();
		lpBuilder.solve();
	}

	public ReducedLPBuilder(Expander<SequenceInformationSet> expander, GameState rootState,
			AlgorithmConfig<SequenceInformationSet> algConfig, GameInfo info) {
		super(expander, rootState, algConfig);
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
	protected void visitLeaf(GameState state, Player lastPlayer, Key lastKey) {
		updateParentLinks(state, lastPlayer, lastKey);
		lpTable.substractFromConstraint(state.getSequenceFor(players[0]), state.getSequenceFor(players[1]), state.getNatureProbability() * (state.getUtilities()[0] - utilityShift));
	}
	
	public Map<Sequence, Double> createFirstPlayerStrategy(IloCplex cplex, Map<Object, IloRange> watchedDualVars) throws IloException {
		Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();

		for (Entry<Object, IloRange> entry : watchedDualVars.entrySet()) {
			p1Strategy.put((Sequence) entry.getKey(), cplex.getDual(entry.getValue()));
		}
		return p1Strategy;//je tu probléms  tim shoftem, zksuit to nastavit tak aby jedna primár poèítal furt s kladnou a duál furt se zápornou nebo opaènì
	}
	
	public void updateLPForFirstPlayer(GameState state, Player lastPlayer, Key lastKey) {
		Key varKey = new Key("P", new Key(state.getISKeyForPlayerToMove()));

		updateParentLinks(state, lastPlayer, lastKey);
		lpTable.setConstraint(state.getSequenceFor(players[0]), varKey, -1);//E
		lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
		lpTable.setUpperBound(varKey, 0);
		lpTable.watchDualVariable(state.getSequenceFor(players[0]), state.getSequenceForPlayerToMove());
	}

	public void updateForFirstPlayerParent(GameState child, Player lastPlayer, Key varKey) {
		Object eqKey = child.getSequenceFor(lastPlayer);
		Key tmpKey = new Key("U", new Key(child.getSequenceFor(lastPlayer)));

		lpTable.watchDualVariable(eqKey, child.getSequenceFor(lastPlayer));
		lpTable.setConstraint(eqKey, varKey, 1);//E child
		lpTable.setConstraint(eqKey, tmpKey, -1);//u (eye)
		lpTable.setObjective(tmpKey, new EpsilonPolynom(epsilon, child.getSequenceFor(lastPlayer).size()));//k(\epsilon)
	}

	public void updateLPForSecondPlayer(GameState state, Player lastPlayer, Key lastKey) {
		Key eqKey = new Key("Q", new Key(state.getISKeyForPlayerToMove()));

		updateParentLinks(state, lastPlayer, lastKey);
		lpTable.setConstraint(eqKey, state.getSequenceFor(players[1]), -1);//F
		lpTable.setConstraintType(eqKey, 2);
		lpTable.watchPrimalVariable(state.getSequenceFor(players[1]), state.getSequenceForPlayerToMove());
	}

	public void updateForSecondPlayerParent(GameState child, Player lastPlayer, Key eqKey) {
		Object varKey = child.getSequenceFor(lastPlayer);
		Key tmpKey = new Key("V", new Key(child.getSequenceFor(lastPlayer)));

		lpTable.setConstraint(eqKey, varKey, 1);//F child
		lpTable.setConstraintType(eqKey, 2);
		lpTable.watchPrimalVariable(varKey, child.getSequenceFor(lastPlayer));
		lpTable.setConstraint(tmpKey, varKey, 1);//indices y
		lpTable.setConstant(tmpKey, new EpsilonPolynom(epsilon, child.getSequenceFor(lastPlayer).size()).negate());//l(\epsilon)
	}
	
}
