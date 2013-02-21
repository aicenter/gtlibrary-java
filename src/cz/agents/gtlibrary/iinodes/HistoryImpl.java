package cz.agents.gtlibrary.iinodes;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;


public class HistoryImpl implements History {

	private FixedSizeMap<Player, Sequence> sequencesOfPlayers;
	private int hashCode = -1;
	final private Player[] players;

	public HistoryImpl(Player[] players) {
		this.players = players;
		sequencesOfPlayers = new FixedSizeMap<Player, Sequence>(players.length);
		for (Player player : players) {
			sequencesOfPlayers.put(player, new LinkedListSequenceImpl(player));
		}
	}

	public HistoryImpl(Map<Player, Sequence> sequencesOfPlayers, Player[] players) {
		this.sequencesOfPlayers = new FixedSizeMap<Player, Sequence>(sequencesOfPlayers.size());
		this.players = players;
		for (Entry<Player, Sequence> entry : sequencesOfPlayers.entrySet()) {
			this.sequencesOfPlayers.put(entry.getKey(), new LinkedListSequenceImpl(entry.getValue()));
		}
	}

	public Sequence getSequenceOf(Player player) {
		return sequencesOfPlayers.get(player);
	}

	@Override
	public History copy() {
		return new HistoryImpl(sequencesOfPlayers, players);
	}

	@Override
	public void addActionOf(Action action, Player player) {
		sequencesOfPlayers.get(player).addLast(action);
		hashCode = -1;
	}
	
	@Override
	public Map<Player, Sequence> getSequencesOfPlayers() {
		return sequencesOfPlayers;
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

	@Override
	public int hashCode() {
		if(hashCode != -1)
			return hashCode;
		hashCode = 0;
		final int prime = 31;
		
		for (Player p : players) { 
			hashCode = hashCode * prime + p.hashCode();
			hashCode = hashCode * prime + sequencesOfPlayers.get(p).hashCode();
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		if (this.hashCode() != obj.hashCode())
			return false;
		History other = (History) obj;

		return sequencesOfPlayers.equals(other.getSequencesOfPlayers());
	}

	@Override
	public String toString() {
		return sequencesOfPlayers.toString();
	}


}
