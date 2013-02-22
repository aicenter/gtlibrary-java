package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.backprop.SampleWeightedBackPropStrategy;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelector;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MCTSConfig algConfig = new MCTSConfig(new Simulator(2), new SampleWeightedBackPropStrategy.Factory(), new UCTSelector(1));
		MCTSRunner runner = new MCTSRunner(algConfig, new BPGGameState(), new BPGExpander<MCTSInformationSet>(algConfig));
		
		for (int i = 0; i < 200; i++) {
			System.out.println(runner.runMcts(10000));
		}
//		for (int i = 0; i < 200; i++) {
//			System.out.println(runner.runMctsWithIncreasingFixedDepth(10000));
//		}
	}

}
