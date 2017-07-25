/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.iinodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;


public class HistoryImpl implements History {

	private static final long serialVersionUID = -36924832950052038L;
	
	private Map<Player, Sequence> sequencesOfPlayers;
	private int hashCode = -1;
	private List<Integer> playersSequence;
	final private Player[] players;

	public HistoryImpl(Player[] players) {
		this.players = players;
		playersSequence = new ArrayList<>();
		sequencesOfPlayers = new LinkedHashMap<>(players.length);
		for (Player player : players) {
			sequencesOfPlayers.put(player, new ArrayListSequenceImpl(player));
		}
	}

	public HistoryImpl(Map<Player, Sequence> sequencesOfPlayers, Player[] players, List<Integer> playersSequence) {
		this.sequencesOfPlayers = new LinkedHashMap<>(sequencesOfPlayers.size());
		this.players = players;
		for (Entry<Player, Sequence> entry : sequencesOfPlayers.entrySet()) {
			this.sequencesOfPlayers.put(entry.getKey(), new ArrayListSequenceImpl(entry.getValue()));
		}
		this.playersSequence = new ArrayList<>(playersSequence);
		/*
		this.playersSequence = new int[playersSequence.length+1];
		for (int i = 0; i< playersSequence.length; i++) {
			this.playersSequence[i] = playersSequence[i];
		}
		*/
	}

	public Sequence getSequenceOf(Player player) {
		return sequencesOfPlayers.get(player);
	}

	@Override
	public History copy() {
		return new HistoryImpl(sequencesOfPlayers, players, playersSequence );
	}

	@Override
	public void addActionOf(Action action, Player player) {
		sequencesOfPlayers.get(player).addLast(action);
		hashCode = -1;
		
		playersSequence.add(player.getId());
	}
	

    @Override
    public int getLength() {
        int result = 0;
        for (Player p : players) {
            result += sequencesOfPlayers.get(p).size();
        }
        return result;
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
//		hashCode = sequencesOfPlayers.hashCode();
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
	
	@Override
	public Player getLastPlayer(){
		return players[playersSequence.get(playersSequence.size()-1)];
	}
	
	@Override
	public Action getLastAction(){
		return sequencesOfPlayers.get(getLastPlayer()).getLast();
	}
	
	@Override
	public void reverse(){
		hashCode = -1;
		if(sequencesOfPlayers.get(getLastPlayer()).removeLast()==null)
			System.err.println("Unable to reverse the action. Empty history.");;
		if(playersSequence.remove(playersSequence.size()-1) == null)
			System.err.println("Unable to reverse the action. Empty history.");
	}
	
	private void printPlayersSequence(){
		for(int i : playersSequence)
			System.out.printf("%d",i);
		System.out.println();
	}
	
	@Override
	public int getSequencesLength(){
		return playersSequence.size();
	}
	
	@Override
	public List<Integer> getPlayersSequences(){
		return playersSequence;
	}

}
