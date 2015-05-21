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


package cz.agents.gtlibrary.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface History extends Serializable {
	public Collection<Sequence> values();

	public Set<Player> keySet();

	public Set<Entry<Player, Sequence>> entrySet();

	public Sequence getSequenceOf(Player player);

	public Map<Player, Sequence> getSequencesOfPlayers();

	public History copy();

	public void addActionOf(Action action, Player player);

    public int getLength();

	public Action getLastAction();

	public void reverse();

	public Player getLastPlayer();

	public int getSequencesLength();

	public List<Integer> getPlayersSequences();

}
