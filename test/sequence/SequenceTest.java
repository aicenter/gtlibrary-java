package sequence;

import static org.junit.Assert.*;

import java.util.HashSet;

import gametree.IINodes.PlayerImpl;
import gametree.IINodes.SequenceImpl;
import gametree.domain.poker.kuhn.KuhnPokerAction;
import gametree.interfaces.Action;
import gametree.interfaces.Player;
import gametree.interfaces.Sequence;

import org.junit.Test;

public class SequenceTest {

	@Test
	public void addGetTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		
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
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		
		assertEquals(action2, sequence.removeLast());
		assertEquals(action1, sequence.removeFirst());
		assertEquals(0, sequence.size());
	}
	
	@Test
	public void iteratorTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		
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
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		
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
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		Action action3 = new KuhnPokerAction("a", 3, player);
		HashSet<Sequence> expectedPrefixes = new HashSet<Sequence>();
		Sequence prefix0 = new SequenceImpl(player);
		Sequence prefix1 = new SequenceImpl(player);
		Sequence prefix2 = new SequenceImpl(player);
		Sequence prefix3 = new SequenceImpl(player);
		
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
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		Action action3 = new KuhnPokerAction("a", 3, player);
		Sequence prefix0 = new SequenceImpl(player);
		Sequence prefix1 = new SequenceImpl(player);
		Sequence notPrefix = new SequenceImpl(player);
		
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
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		Action action3 = new KuhnPokerAction("a", 3, player);
		Sequence expectedSubsequence = new SequenceImpl(player);
		
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
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		Action action3 = new KuhnPokerAction("a", 3, player);
		Action action4 = new KuhnPokerAction("k", 3, player);
		Sequence expectedSubsequence = new SequenceImpl(player);
		
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
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		Action action3 = new KuhnPokerAction("b", 1, player);
		Action action4 = new KuhnPokerAction("c", 2, player);
		Sequence sequence1 = new SequenceImpl(player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence1.addLast(action3);
		sequence1.addLast(action4);
		
		assertEquals(true, sequence.equals(sequence1));
	}
	
	@Test
	public void equalsWithDifferentSequencesTest() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		Action action3 = new KuhnPokerAction("b", 1, player);
		Action action4 = new KuhnPokerAction("c", 2, player);
		Sequence sequence1 = new SequenceImpl(new PlayerImpl(1));
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence1.addLast(action3);
		sequence1.addLast(action4);
		
		assertEquals(false, sequence.equals(sequence1));
	}
	
	@Test
	public void equalsWithDifferentSequencesTest1() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		Action action3 = new KuhnPokerAction("b", 1, player);
		Action action4 = new KuhnPokerAction("c", 2, new PlayerImpl(1));
		Sequence sequence1 = new SequenceImpl(player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence1.addLast(action3);
		sequence1.addLast(action4);
		
		assertEquals(false, sequence.equals(sequence1));
	}
	
	@Test
	public void equalsWithDifferentSequencesTest2() {
		Player player = new PlayerImpl(0);
		Sequence sequence = new SequenceImpl(player);
		Action action1 = new KuhnPokerAction("b", 1, player);
		Action action2 = new KuhnPokerAction("c", 2, player);
		Action action3 = new KuhnPokerAction("ch", 1, player);
		Action action4 = new KuhnPokerAction("c", 2, player);
		Sequence sequence1 = new SequenceImpl(player);
		
		sequence.addLast(action1);
		sequence.addLast(action2);
		sequence1.addLast(action3);
		sequence1.addLast(action4);
		
		assertEquals(false, sequence.equals(sequence1));
	}
}
