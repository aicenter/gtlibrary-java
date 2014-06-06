package cz.agents.gtlibrary.algorithms.sequenceform.refinements.undominated;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.upordown.UDExpander;
import cz.agents.gtlibrary.domain.upordown.UDGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;

import java.util.Map;
import java.util.Map.Entry;

public class UndominatedSolver {

	private GameState root;
	private Expander<SequenceInformationSet> expander;

	public static void main(String[] args) {
        System.out.println("****Undominated solver****");
//		runUpOrDown();
//		runAceOfSpades();
//		runKuhnPoker();
//		runBPG();
		runGenericPoker();
//		runGoofspiel();
	}

	public static void runGoofspiel() {
		runUndominatedSolver(new GoofSpielGameState(), new GoofSpielExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}

	public static void runGenericPoker() {
        new GPGameInfo();
		runUndominatedSolver(new GenericPokerGameState(), new GenericPokerExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}

	public static void runBPG() {
		runUndominatedSolver(new BPGGameState(), new BPGExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}

	public static void runKuhnPoker() {
		runUndominatedSolver(new KuhnPokerGameState(), new KuhnPokerExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}

	public static void runUpOrDown() {
		runUndominatedSolver(new UDGameState(), new UDExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}

	public static void runAceOfSpades() {
		runUndominatedSolver(new AoSGameState(), new AoSExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}
	
	private static void runUndominatedSolver(GameState root, Expander<SequenceInformationSet> expander) {
		UndominatedSolver solver = new UndominatedSolver(root, expander);

		Map<Sequence, Double> p1RealizationPlan = solver.solveForP1();
		Map<Sequence, Double> p2RealizationPlan = solver.solveForP2();

		for (Entry<Sequence, Double> entry : p1RealizationPlan.entrySet()) {
			if (entry.getValue() > 0)
				System.out.println(entry);
		}
		for (Entry<Sequence, Double> entry : p2RealizationPlan.entrySet()) {
			if (entry.getValue() > 0)
				System.out.println(entry);
		}
		UtilityCalculator calculator = new UtilityCalculator(root, expander);
		Strategy p1Strategy = new UniformStrategyForMissingSequences();
		Strategy p2Strategy = new UniformStrategyForMissingSequences();
		
		p1Strategy.putAll(p1RealizationPlan);
		p2Strategy.putAll(p2RealizationPlan);
		System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));		
	}

	public UndominatedSolver(GameState root, Expander<SequenceInformationSet> expander) {
		this.root = root;
		this.expander = expander;
	}

	public Map<Sequence, Double> solveForP1() {
		InitialP1Builder initPbuilder = new InitialP1Builder(expander, root);

		initPbuilder.buildLP();
		double initialValue = initPbuilder.solve();

        P1Builder builder = new P1Builder(expander, root, initialValue);

        builder.buildLP();
        return builder.solve();
	}

	public Map<Sequence, Double> solveForP2() {
		InitialP2Builder initPbuilder = new InitialP2Builder(expander, root);

		initPbuilder.buildLP();
		double initialValue = initPbuilder.solve();

        P2Builder builder = new P2Builder(expander, root, initialValue);

        builder.buildLP();
        return builder.solve();
	}

}
