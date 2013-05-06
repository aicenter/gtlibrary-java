package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class SizeVisitor extends TreeVisitor {

	Map<Player, Set<Sequence>> continuationKeys;
	Map<Player, Set<Pair<Integer, Sequence>>> isKeys;
	private double minUtility = Double.POSITIVE_INFINITY;
	private double maxUtility = Double.NEGATIVE_INFINITY;

	public SizeVisitor(GameState rootState, Expander<? extends InformationSet> expander, AlgorithmConfig<SequenceInformationSet> algConfig) {
		super(rootState, expander, algConfig);
		continuationKeys = new HashMap<Player, Set<Sequence>>();
		continuationKeys.put(new PlayerImpl(0), new HashSet<Sequence>());
		continuationKeys.put(new PlayerImpl(1), new HashSet<Sequence>());
		isKeys = new HashMap<Player, Set<Pair<Integer, Sequence>>>();
		isKeys.put(new PlayerImpl(0), new HashSet<Pair<Integer, Sequence>>());
		isKeys.put(new PlayerImpl(1), new HashSet<Pair<Integer, Sequence>>());
	}

	@Override
	protected void visitLeaf(GameState state) {
		double currentUtility = state.getUtilities()[0];

		if (minUtility > currentUtility)
			minUtility = currentUtility;
		if (maxUtility < currentUtility)
			maxUtility = currentUtility;
	}

	@Override
	protected void visitNormalNode(GameState state) {
		isKeys.get(state.getPlayerToMove()).add(state.getISKeyForPlayerToMove());
		for (Action action : expander.getActions(state)) {
			GameState child = state.performAction(action);

			continuationKeys.get(state.getPlayerToMove()).add(child.getSequenceFor(state.getPlayerToMove()));
		}
		super.visitNormalNode(state);
	}

	public int getContinuationCountFor(Player player) {
		return continuationKeys.get(player).size() + 1;
	}

	public int getISCountFor(Player player) {
		return isKeys.get(player).size() + 1;
	}
	
	public double getMinUtilityForPlayerOne() {
		return minUtility;
	}
	
	public double getMaxUtilityForPlayerOne() {
		return maxUtility;
	}
}
