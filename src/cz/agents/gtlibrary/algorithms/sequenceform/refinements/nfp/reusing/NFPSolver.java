package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.reusing;

import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
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

public class NFPSolver {

	private GameState root;
	private Expander<SequenceInformationSet> expander;

	public static void main(String[] args) {
//		runUpOrDown();
//				runAceOfSpades();
//				runKuhnPoker();
				runBPG();
//				runGenericPoker();
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
		NFPSolver solver = new NFPSolver(root, expander);

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

	public NFPSolver(GameState root, Expander<SequenceInformationSet> expander) {
		this.root = root;
		this.expander = expander;
	}

	public Map<Sequence, Double> solveForP1() {
		InitialPBuilder initPbuilder = new InitialPBuilder(expander, root);

		initPbuilder.buildLP();
		double initialValue = initPbuilder.solve();

		InitialQBuilder initQBuilder = new InitialQBuilder(expander, root, initialValue);

		initQBuilder.buildLP();
		IterationData data = initQBuilder.solve();

		if (data.getGameValue() > 1e-6) {
			PBuilder pBuilder = new PBuilder(expander, root, data, initialValue);

			pBuilder.buildLP();
			double gameValue = pBuilder.solve();

			QBuilder qBuilder = new QBuilder(expander, root, initialValue, gameValue, data);

			qBuilder.buildLP();
			data = qBuilder.solve();

			PUpdater pUpdater = new PUpdater(expander, root, pBuilder.lpTable);//proč se přeindexuje to t, zkusit najít výskyt toho 2t a zjistit co je jinak v tom faillim p
			QUpdater qUpdater = new QUpdater(expander, root, initialValue, qBuilder.lpTable);

			while (Math.abs(data.getGameValue()) > 1e-6) {
				assert !data.getLastItSeq().isEmpty();
				System.out.println("Exploitable seq. count " + data.getLastItSeq().size());

				pUpdater.buildLP(data);
				double currentValue = pUpdater.solve();

				qUpdater.buildLP(data, currentValue);
				data = qUpdater.solve();
			}
		}
		return data.getRealizationPlan();
	}

	public Map<Sequence, Double> solveForP2() {
		InitialP2PBuilder initPbuilder = new InitialP2PBuilder(expander, root);

		initPbuilder.buildLP();
		double initialValue = initPbuilder.solve();

		InitialP2QBuilder initQBuilder = new InitialP2QBuilder(expander, root, initialValue);

		initQBuilder.buildLP();
		IterationData data = initQBuilder.solve();

		if (data.getGameValue() > 1e-6) {
			P2PBuilder pBuilder = new P2PBuilder(expander, root, data, initialValue);

			pBuilder.buildLP();
			double gameValue = pBuilder.solve();

			P2QBuilder qBuilder = new P2QBuilder(expander, root, initialValue, gameValue, data);

			qBuilder.buildLP();
			data = qBuilder.solve();

			P2PUpdater pUpdater = new P2PUpdater(expander, root, pBuilder.lpTable);
			P2QUpdater qUpdater = new P2QUpdater(expander, root, initialValue, qBuilder.lpTable);

			while (Math.abs(data.getGameValue()) > 1e-6) {
				assert !data.getLastItSeq().isEmpty();
				System.out.println("Exploitable seq. count " + data.getLastItSeq().size());

				pUpdater.buildLP(data);
				double currentValue = pUpdater.solve();

				qUpdater.buildLP(currentValue, data);
				data = qUpdater.solve();
			}
		}
		return data.getRealizationPlan();
	}

}