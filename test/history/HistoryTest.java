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


package history;

import static org.junit.Assert.*;

import org.junit.Test;

import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerAction;
import cz.agents.gtlibrary.iinodes.HistoryImpl;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class HistoryTest {

	@Test
	public void keySetTest() {
		Player player1 = new PlayerImpl(0);
		Player player2 = new PlayerImpl(1);
		History history = new HistoryImpl(new Player[]{new PlayerImpl(0), new PlayerImpl(1)});
		
		assertEquals(true, history.keySet().contains(player1));		
		assertEquals(true, history.keySet().contains(player2));
	}
	
	@Test
	public void getSequenceOfTest() {
		Player player1 = new PlayerImpl(0);
		Player player2 = new PlayerImpl(1);
		History history = new HistoryImpl(new Player[]{player1, player2});
		Sequence sequence1 = new LinkedListSequenceImpl(player1);
		Sequence sequence2 = new LinkedListSequenceImpl(player2);
		
		sequence1.addLast(new KuhnPokerAction("b", null, player1));
		sequence2.addLast(new KuhnPokerAction("ch", null, player2));
		history.addActionOf(new KuhnPokerAction("b", null, player1), player1);
		history.addActionOf(new KuhnPokerAction("ch", null, player2), player2);
		
		assertEquals(history.getSequenceOf(player1), sequence1);
		assertEquals(history.getSequenceOf(player2), sequence2);
	}
	
	@Test
	public void valuesTest() {
		Player player1 = new PlayerImpl(0);
		Player player2 = new PlayerImpl(1);
		History history = new HistoryImpl(new Player[]{player1, player2});
		Sequence sequence1 = new LinkedListSequenceImpl(player1);
		Sequence sequence2 = new LinkedListSequenceImpl(player2);
		
		sequence1.addLast(new KuhnPokerAction("b", null, player1));
		sequence2.addLast(new KuhnPokerAction("ch", null, player2));
		history.addActionOf(new KuhnPokerAction("b", null, player1), player1);
		history.addActionOf(new KuhnPokerAction("ch", null, player2), player2);
		
		assertEquals(true, history.values().contains(sequence1));
		assertEquals(true, history.values().contains(sequence2));
	}
	
	@Test
	public void equalsWithSameHistoriesTest() {
		Player player1 = new PlayerImpl(0);
		Player player2 = new PlayerImpl(1);
		History history = new HistoryImpl(new Player[]{player1, player2});
		History history1 = new HistoryImpl(new Player[]{player1, player2}); 
		
		history1.addActionOf(new KuhnPokerAction("b", null, player1), player1);
		history1.addActionOf(new KuhnPokerAction("ch", null, player2), player2);
		history.addActionOf(new KuhnPokerAction("b", null, player1), player1);
		history.addActionOf(new KuhnPokerAction("ch", null, player2), player2);
		
		assertEquals(true, history.equals(history1));
	}

	@Test
	public void equalsWithDifferentHistoriesTest() {
		Player player1 = new PlayerImpl(0);
		Player player2 = new PlayerImpl(1);
		History history = new HistoryImpl(new Player[]{player1, player2});
		History history1 = new HistoryImpl(new Player[]{player1, player2}); 
		
		history1.addActionOf(new KuhnPokerAction("ch", null, player2), player2);
		history.addActionOf(new KuhnPokerAction("b", null, player1), player1);
		history.addActionOf(new KuhnPokerAction("ch", null, player2), player2);
		
		assertEquals(false, history.equals(history1));
	}

}
