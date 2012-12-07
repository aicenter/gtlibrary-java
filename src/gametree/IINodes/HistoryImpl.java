package gametree.IINodes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import gametree.interfaces.Action;
import gametree.interfaces.History;
import gametree.interfaces.Player;
import gametree.interfaces.Sequence;

public class HistoryImpl implements History {
	
	private Map<Player, Sequence> sequencesOfPlayers;

	public HistoryImpl(Player[] players) {
		sequencesOfPlayers = new HashMap<Player, Sequence>();
		for (Player player : players) {
			sequencesOfPlayers.put(player, new SequenceImpl(player));
		}
	}

	public HistoryImpl(Map<Player, Sequence> sequencesOfPlayers) {
		this.sequencesOfPlayers = new HashMap<Player, Sequence>();
		for (Entry<Player, Sequence> entry : sequencesOfPlayers.entrySet()) {
			this.sequencesOfPlayers.put(entry.getKey(), new SequenceImpl(entry.getValue()));
		}		
	}

	public Sequence getSequenceOf(Player player) {
		return sequencesOfPlayers.get(player);
	}

	@Override
	public History copy() {
		return new HistoryImpl(sequencesOfPlayers);
	}

	@Override
	public void addActionOf(Action action, Player player) {
		sequencesOfPlayers.get(player).addLast(action);
	}

	@Override
	public Set<Entry<Player, Sequence>> entrySet() {
		return sequencesOfPlayers.entrySet();
	}

	@Override
	public Collection<Sequence> values() {
		return sequencesOfPlayers.values();
	}

	@Override
	public Set<Player> keySet() {
		return sequencesOfPlayers.keySet();
	}
}
