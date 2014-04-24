package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.milp;

import java.util.Map;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.InitialP2PBuilder;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.InitialP2QBuilder;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.InitialPBuilder;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.InitialQBuilder;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.IterationData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.P2PBuilder;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.PBuilder;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
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


public class MILPNFPSolver {
	
	private GameState root;
	private Expander<SequenceInformationSet> expander;

	public static void main(String[] args) {
//		runUpOrDown();
//		runAceOfSpades();
//		runKuhnPoker();
//		runBPG();
		runGenericPoker();
//		runGoofspiel();
	}

	public static void runGoofspiel() {
		runNFPSolver(new GoofSpielGameState(), new GoofSpielExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}

	public static void runGenericPoker() {
		runNFPSolver(new GenericPokerGameState(), new GenericPokerExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}

	public static void runBPG() {
		runNFPSolver(new BPGGameState(), new BPGExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}

	public static void runKuhnPoker() {
		runNFPSolver(new KuhnPokerGameState(), new KuhnPokerExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}

	public static void runUpOrDown() {
		runNFPSolver(new UDGameState(), new UDExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}

	public static void runAceOfSpades() {
		runNFPSolver(new AoSGameState(), new AoSExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
	}
	
	private static void runNFPSolver(GameState root, Expander<SequenceInformationSet> expander) {
		MILPNFPSolver solver = new MILPNFPSolver(root, expander);

		Map<Sequence, Double> p1RealizationPlan = solver.solveForP1();
		Map<Sequence, Double> p2RealizationPlan = solver.solveForP2();

//		for (Entry<Sequence, Double> entry : p1RealizationPlan.entrySet()) {
//			if (entry.getValue() > 0)
//				System.out.println(entry);
//		}
//		for (Entry<Sequence, Double> entry : p2RealizationPlan.entrySet()) {
//			if (entry.getValue() > 0)
//				System.out.println(entry);
//		}
		UtilityCalculator calculator = new UtilityCalculator(root, expander);
		Strategy p1Strategy = new UniformStrategyForMissingSequences();
		Strategy p2Strategy = new UniformStrategyForMissingSequences();
		
		p1Strategy.putAll(p1RealizationPlan);
		p2Strategy.putAll(p2RealizationPlan);
		System.out.println(calculator.computeUtility(p1Strategy, p2Strategy));		
	}

	public MILPNFPSolver(GameState root, Expander<SequenceInformationSet> expander) {
		this.root = root;
		this.expander = expander;
	}

	public Map<Sequence, Double> solveForP1() {
		InitialPBuilder initPbuilder = new InitialPBuilder(expander, root);

		initPbuilder.buildLP();
		double initialValue = initPbuilder.solve();

		InitialQBuilder initQBuilder = new MILPInitialQ(expander, root, initialValue);

		initQBuilder.buildLP();
		IterationData data = initQBuilder.solve();

		while (Math.abs(data.getGameValue()) > 1e-4) {
			assert !data.getLastItSeq().isEmpty();
			System.out.println("Exploitable seq. count " + data.getLastItSeq().size());
			PBuilder pBuilder = new PBuilder(expander, root, data, initialValue);
					

			pBuilder.buildLP();
			double currentValue = pBuilder.solve();

			MILPQBuilder qBuilder = new MILPQBuilder(expander, root, initialValue, currentValue, data);

			qBuilder.buildLP();
			data = qBuilder.solve();
		}
		return data.getRealizationPlan();
	}

	public Map<Sequence, Double> solveForP2() {
		InitialP2PBuilder initPbuilder = new InitialP2PBuilder(expander, root);

		initPbuilder.buildLP();
		double initialValue = initPbuilder.solve();

		InitialP2QBuilder initQBuilder = new MILPInitialP2Q(expander, root, initialValue);

		initQBuilder.buildLP();
		IterationData data = initQBuilder.solve();

		while (Math.abs(data.getGameValue()) > 1e-8) {
			assert !data.getLastItSeq().isEmpty();
			System.out.println("Exploitable seq. count " + data.getLastItSeq().size());
			P2PBuilder pBuilder = new P2PBuilder(expander, root, data, initialValue);

			pBuilder.buildLP();
			double currentValue = pBuilder.solve();

			MILPP2QBuilder qBuilder = new MILPP2QBuilder(expander, root, initialValue, currentValue, data);

			qBuilder.buildLP();
			data = qBuilder.solve();
		}
		return data.getRealizationPlan();
	}

}
