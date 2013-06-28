package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class SizeVisitor extends TreeVisitor {

	Map<Player, Set<Sequence>> sequences;
	Map<Player, Set<Pair<Integer, Sequence>>> isKeys;

	public SizeVisitor(GameState rootState, Expander<? extends InformationSet> expander, AlgorithmConfig<SequenceInformationSet> algConfig) {
		super(rootState, expander, algConfig);
		sequences = new HashMap<Player, Set<Sequence>>();
		sequences.put(new PlayerImpl(0), new HashSet<Sequence>());
		sequences.put(new PlayerImpl(1), new HashSet<Sequence>());
		isKeys = new HashMap<Player, Set<Pair<Integer, Sequence>>>();
		isKeys.put(new PlayerImpl(0), new HashSet<Pair<Integer, Sequence>>());
		isKeys.put(new PlayerImpl(1), new HashSet<Pair<Integer, Sequence>>());
	}

	@Override
	protected void visitLeaf(GameState state, Player lastPlayer, Key lastKey) {
		updateSequences(state);
	}

	@Override
	protected void visitNormalNode(GameState state, Player lastPlayer, Key lastKey) {
		isKeys.get(state.getPlayerToMove()).add(state.getISKeyForPlayerToMove());
		updateSequences(state);
		super.visitNormalNode(state, lastPlayer, lastKey);
	}

	public void updateSequences(GameState state) {
		sequences.get(state.getAllPlayers()[0]).add(state.getSequenceFor(state.getAllPlayers()[0]));
		sequences.get(state.getAllPlayers()[1]).add(state.getSequenceFor(state.getAllPlayers()[1]));
	}
	
	@Override
	protected void visitChanceNode(GameState state, Player lastPlayer, Key lastKey) {
		updateSequences(state);
		super.visitChanceNode(state, lastPlayer, lastKey);
	}

	public int getISCountFor(Player player) {
		return isKeys.get(player).size();
	}
}
