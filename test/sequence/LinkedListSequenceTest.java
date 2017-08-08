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


package sequence;

import static org.junit.Assert.*;

import java.util.HashSet;


import org.junit.Test;

import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerAction;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class LinkedListSequenceTest {

	@Test
	public void addGetTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		
		assertEquals(action2, sequence.getLast());
		assertEquals(action1, sequence.getFirst());
		assertEquals(2, sequence.size());
		assertEquals(player, sequence.getPlayer());
	}
	
	@Test
	public void removeTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		
		assertEquals(action2, sequence.removeLast());
		assertEquals(action1, sequence.removeFirst());
		assertEquals(0, sequence.size());
	}
	
	@Test
	public void iteratorTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		int index = 0;
		
		for (Action action : sequence) {
			if(index==0)
				assertEquals(action1, action);
			else 
				assertEquals(action2, action);
			index++;
		}
	}
	
	@Test
	public void reversedIteratorTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		
		sequence.addFirst(action1);
		sequence.addFirst(action2);
		int index = 0;
		
		for (Action action : sequence) {
			if(index==0)
				assertEquals(action2, action);
			else 
				assertEquals(action1, action);
			index++;
		}
	}
	
	@Test
	public void getAllPrefixesTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		Action action3 = new KuhnPokerAction("a", null, player);
		HashSet<Sequence> expectedPrefixes = new HashSet<Sequence>();
		Sequence prefix0 = new LinkedListSequenceImpl(player);
		Sequence prefix1 = new LinkedListSequenceImpl(player);
		Sequence prefix2 = new LinkedListSequenceImpl(player);
		Sequence prefix3 = new LinkedListSequenceImpl(player);
		
		sequence.addLast(action1);
		prefix1.addLast(action1);
		prefix2.addLast(action1);
		prefix3.addLast(action1);
		sequence.addLast(action2);
		prefix2.addLast(action2);
		prefix3.addLast(action2);
		sequence.addLast(action3);
		prefix3.addLast(action3);
		
		expectedPrefixes.add(prefix0);
		expectedPrefixes.add(prefix1);
		expectedPrefixes.add(prefix2);
		expectedPrefixes.add(prefix3);
		
		assertEquals(expectedPrefixes, sequence.getAllPrefixes());
	}
	
	@Test
	public void isPrefixTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		Action action3 = new KuhnPokerAction("a", null, player);
		Sequence prefix0 = new LinkedListSequenceImpl(player);
		Sequence prefix1 = new LinkedListSequenceImpl(player);
		Sequence notPrefix = new LinkedListSequenceImpl(player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence.addLast(action3);
		
		prefix1.addLast(action1);
		prefix1.addLast(action2);
		
		notPrefix.addLast(action3);
		
		assertEquals(true, prefix0.isPrefixOf(sequence));
		assertEquals(true, prefix1.isPrefixOf(sequence));
		assertEquals(false, notPrefix.isPrefixOf(sequence));
	}

	@Test
	public void getSubsequenceFromSizeTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		Action action3 = new KuhnPokerAction("a", null, player);
		Sequence expectedSubsequence = new LinkedListSequenceImpl(player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence.addLast(action3);
		
		expectedSubsequence.addLast(action1);
		expectedSubsequence.addLast(action2);
		
		
		assertEquals(expectedSubsequence, sequence.getSubSequence(2));
	}
	
	@Test
	public void getSubsequenceFromToTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		Action action3 = new KuhnPokerAction("a", null, player);
		Action action4 = new KuhnPokerAction("k", null, player);
		Sequence expectedSubsequence = new LinkedListSequenceImpl(player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence.addLast(action3);
		sequence.addLast(action4);
		
		expectedSubsequence.addLast(action2);
		expectedSubsequence.addLast(action3);
		
		
		assertEquals(expectedSubsequence, sequence.getSubSequence(1, 2));
	}
	
	@Test
	public void equalsWithSameSequencesTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		Action action3 = new KuhnPokerAction("b", null, player);
		Action action4 = new KuhnPokerAction("c", null, player);
		Sequence sequence1 = new LinkedListSequenceImpl(player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence1.addLast(action3);
		sequence1.addLast(action4);
		
		assertEquals(true, sequence.equals(sequence1));
	}
	
	@Test
	public void equalsWithDifferentSequencesTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		Action action3 = new KuhnPokerAction("b", null, player);
		Action action4 = new KuhnPokerAction("c", null, player);
		Sequence sequence1 = new LinkedListSequenceImpl(new PlayerImpl(1));
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence1.addLast(action3);
		sequence1.addLast(action4);
		
		assertEquals(false, sequence.equals(sequence1));
	}
	
	@Test
	public void equalsWithDifferentSequencesTest1() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		Action action3 = new KuhnPokerAction("b", null, player);
		Action action4 = new KuhnPokerAction("c", null, new PlayerImpl(1));
		Sequence sequence1 = new LinkedListSequenceImpl(player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence1.addLast(action3);
		sequence1.addLast(action4);
		
		assertEquals(false, sequence.equals(sequence1));
	}
	
	@Test
	public void equalsWithDifferentSequencesTest2() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new LinkedListSequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", null, player);
		Action action2 = new KuhnPokerAction("c", null, player);
		Action action3 = new KuhnPokerAction("ch", null, player);
		Action action4 = new KuhnPokerAction("c", null, player);
		Sequence sequence1 = new LinkedListSequenceImpl(player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence1.addLast(action3);
		sequence1.addLast(action4);
		
		assertEquals(false, sequence.equals(sequence1));
	}
}
