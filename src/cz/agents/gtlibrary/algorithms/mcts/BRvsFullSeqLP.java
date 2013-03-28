package cz.agents.gtlibrary.algorithms.mcts;

import java.util.Map;

import cz.agents.gtlibrary.algorithms.mcts.backprop.DefaultBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MostFrequentAction;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelector;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.UtilityCalculator;

public class BRvsFullSeqLP {

	public static void main(String[] args) {
//		runKuhnPoker();
//		runGenericPoker();
//		runBPG();
//		runGoofSpiel();
//      runRandomGame();
//		runSimRandomGame();
		runPursuit();
	}
	
	public static void runPursuit() {
		GameState rootState = new PursuitGameState();
		GameInfo gameInfo = new PursuitGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		MCTSConfig secondMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		Expander<MCTSInformationSet> firstMCTSExpander = new PursuitExpander<MCTSInformationSet>(firstMCTSConfig);
		Expander<MCTSInformationSet> secondMCTSExpander = new PursuitExpander<MCTSInformationSet>(secondMCTSConfig);
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new PursuitExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		runMCTS(rootState, gameInfo, firstMCTSConfig, secondMCTSConfig, firstMCTSExpander, secondMCTSExpander, efg.generate());
	}

	public static void runKuhnPoker() {
		GameState rootState = new KuhnPokerGameState();
		KPGameInfo gameInfo = new KPGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		MCTSConfig secondMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		Expander<MCTSInformationSet> firstMCTSExpander = new KuhnPokerExpander<MCTSInformationSet>(firstMCTSConfig);
		Expander<MCTSInformationSet> secondMCTSExpander = new KuhnPokerExpander<MCTSInformationSet>(secondMCTSConfig);
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new KuhnPokerExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		runMCTS(rootState, gameInfo, firstMCTSConfig, secondMCTSConfig, firstMCTSExpander, secondMCTSExpander, efg.generate());
	}

	public static void runRandomGame() {
		GameState rootState = new RandomGameState();
		GameInfo gameInfo = new RandomGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		MCTSConfig secondMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		Expander<MCTSInformationSet> firstMCTSExpander = new RandomGameExpander<MCTSInformationSet>(firstMCTSConfig);
		Expander<MCTSInformationSet> secondMCTSExpander = new RandomGameExpander<MCTSInformationSet>(secondMCTSConfig);
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new RandomGameExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		runMCTS(rootState, gameInfo, firstMCTSConfig, secondMCTSConfig, firstMCTSExpander, secondMCTSExpander, efg.generate());
	}
	
	public static void runSimRandomGame() {
		GameState rootState = new SimRandomGameState();
		GameInfo gameInfo = new RandomGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		MCTSConfig secondMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		Expander<MCTSInformationSet> firstMCTSExpander = new RandomGameExpander<MCTSInformationSet>(firstMCTSConfig);
		Expander<MCTSInformationSet> secondMCTSExpander = new RandomGameExpander<MCTSInformationSet>(secondMCTSConfig);
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new RandomGameExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		runMCTS(rootState, gameInfo, firstMCTSConfig, secondMCTSConfig, firstMCTSExpander, secondMCTSExpander, efg.generate());
	}

	public static void runGoofSpiel() {
		GameState rootState = new GoofSpielGameState();
		GSGameInfo gameInfo = new GSGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		MCTSConfig secondMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		Expander<MCTSInformationSet> firstMCTSExpander = new GoofSpielExpander<MCTSInformationSet>(firstMCTSConfig);
		Expander<MCTSInformationSet> secondMCTSExpander = new GoofSpielExpander<MCTSInformationSet>(secondMCTSConfig);
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new GoofSpielExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		runMCTS(rootState, gameInfo, firstMCTSConfig, secondMCTSConfig, firstMCTSExpander, secondMCTSExpander, efg.generate());
	}

	public static void runGenericPoker() {
		GameState rootState = new GenericPokerGameState();
		GPGameInfo gameInfo = new GPGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		MCTSConfig secondMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		Expander<MCTSInformationSet> firstMCTSExpander = new GenericPokerExpander<MCTSInformationSet>(firstMCTSConfig);
		Expander<MCTSInformationSet> secondMCTSExpander = new GenericPokerExpander<MCTSInformationSet>(secondMCTSConfig);
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new GenericPokerExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		runMCTS(rootState, gameInfo, firstMCTSConfig, secondMCTSConfig, firstMCTSExpander, secondMCTSExpander, efg.generate());
	}

	public static void runBPG() {
		GameState rootState = new BPGGameState();
		BPGGameInfo gameInfo = new BPGGameInfo();
		SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
		MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		MCTSConfig secondMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(getC(gameInfo.getMaxUtility())));
		Expander<MCTSInformationSet> firstMCTSExpander = new BPGExpander<MCTSInformationSet>(firstMCTSConfig);
		Expander<MCTSInformationSet> secondMCTSExpander = new BPGExpander<MCTSInformationSet>(secondMCTSConfig);
		FullSequenceEFG efg = new FullSequenceEFG(rootState, new BPGExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

		runMCTS(rootState, gameInfo, firstMCTSConfig, secondMCTSConfig, firstMCTSExpander, secondMCTSExpander, efg.generate());
	}

	private static double getC(double maxUtility) {
		return Math.sqrt(2) * maxUtility;
	}
	
	public static void runMCTS(GameState rootState, GameInfo gameInfo, MCTSConfig firstMCTSConfig, MCTSConfig secondMCTSConfig, Expander<MCTSInformationSet> firstMCTSExpander, Expander<MCTSInformationSet> secondMCTSExpander, Map<Player, Map<Sequence, Double>> realizationPlans) {
		BestResponseMCTSRunner mctsRunner = new BestResponseMCTSRunner(firstMCTSConfig, rootState, firstMCTSExpander, realizationPlans.get(gameInfo.getAllPlayers()[1]), gameInfo.getAllPlayers()[1]);
		UtilityCalculator utility = new UtilityCalculator(rootState, firstMCTSExpander);

		Strategy firstPlayerRP = firstMCTSConfig.getEmptyStrategy();
		Strategy secondPlayerRP = firstMCTSConfig.getEmptyStrategy();

		firstPlayerRP.putAll(realizationPlans.get(gameInfo.getAllPlayers()[0]));
		secondPlayerRP.putAll(realizationPlans.get(gameInfo.getAllPlayers()[1]));
		System.out.println("MCTS response: " + utility.computeUtility(mctsRunner.runMCTS(gameInfo.getAllPlayers()[0], new MostFrequentAction()), secondPlayerRP));

		utility = new UtilityCalculator(rootState, secondMCTSExpander);
		mctsRunner = new BestResponseMCTSRunner(secondMCTSConfig, rootState, secondMCTSExpander, realizationPlans.get(gameInfo.getAllPlayers()[0]), gameInfo.getAllPlayers()[0]);

		System.out.println("MCTS response: " + utility.computeUtility(firstPlayerRP, mctsRunner.runMCTS(gameInfo.getAllPlayers()[1], new MostFrequentAction())));
	}
}