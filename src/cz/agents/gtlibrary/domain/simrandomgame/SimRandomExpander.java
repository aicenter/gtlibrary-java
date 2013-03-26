package cz.agents.gtlibrary.domain.simrandomgame;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class SimRandomExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private Random random;
	private long firstSeed;

	public SimRandomExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
		this.random = new Random(SimRandomGameInfo.seed);
		firstSeed = random.nextLong();
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		SimRandomGameState gsState = (SimRandomGameState) gameState;
		List<Action> actions = new LinkedList<Action>();

		int actNumber = SimRandomGameInfo.MAX_BF[gsState.getPlayerToMove().getId()];

		for (int i = 0; i < actNumber; i++) {
			String newVal = firstSeed + "";
			
			if (gsState.getSequenceForPlayerToMove().size() > 0)
				newVal = ((SimRandomAction) gsState.getSequenceForPlayerToMove().getLast()).getValue();
			actions.add(new SimRandomAction(newVal + "_" + i, gsState.getPlayerToMove(), getAlgorithmConfig().getInformationSetFor(gameState)));
		}
//		Collections.shuffle(actions, random);
		return actions;
	}
}
