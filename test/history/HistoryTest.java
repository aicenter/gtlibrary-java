package history;

import static org.junit.Assert.*;

import org.junit.Test;

import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerAction;
import cz.agents.gtlibrary.iinodes.HistoryImpl;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.iinodes.SequenceImpl;
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
		Sequence sequence1 = new SequenceImpl(player1);
		Sequence sequence2 = new SequenceImpl(player2);
		
		sequence1.addLast(new KuhnPokerAction("b", 1, player1));
		sequence2.addLast(new KuhnPokerAction("ch", 1, player2));
		history.addActionOf(new KuhnPokerAction("b", 1, player1), player1);
		history.addActionOf(new KuhnPokerAction("ch", 1, player2), player2);
		
		assertEquals(history.getSequenceOf(player1), sequence1);
		assertEquals(history.getSequenceOf(player2), sequence2);
	}
	
	@Test
	public void valuesTest() {
		Player player1 = new PlayerImpl(0);
		Player player2 = new PlayerImpl(1);
		History history = new HistoryImpl(new Player[]{player1, player2});
		Sequence sequence1 = new SequenceImpl(player1);
		Sequence sequence2 = new SequenceImpl(player2);
		
		sequence1.addLast(new KuhnPokerAction("b", 1, player1));
		sequence2.addLast(new KuhnPokerAction("ch", 1, player2));
		history.addActionOf(new KuhnPokerAction("b", 1, player1), player1);
		history.addActionOf(new KuhnPokerAction("ch", 1, player2), player2);
		
		assertEquals(true, history.values().contains(sequence1));
		assertEquals(true, history.values().contains(sequence2));
	}
	
	@Test
	public void equalsWithSameHistoriesTest() {
		Player player1 = new PlayerImpl(0);
		Player player2 = new PlayerImpl(1);
		History history = new HistoryImpl(new Player[]{player1, player2});
		History history1 = new HistoryImpl(new Player[]{player1, player2}); 
		
		history1.addActionOf(new KuhnPokerAction("b", 1, player1), player1);
		history1.addActionOf(new KuhnPokerAction("ch", 1, player2), player2);
		history.addActionOf(new KuhnPokerAction("b", 1, player1), player1);
		history.addActionOf(new KuhnPokerAction("ch", 1, player2), player2);
		
		assertEquals(true, history.equals(history1));
	}

	@Test
	public void equalsWithDifferentHistoriesTest() {
		Player player1 = new PlayerImpl(0);
		Player player2 = new PlayerImpl(1);
		History history = new HistoryImpl(new Player[]{player1, player2});
		History history1 = new HistoryImpl(new Player[]{player1, player2}); 
		
		history1.addActionOf(new KuhnPokerAction("ch", 1, player2), player2);
		history.addActionOf(new KuhnPokerAction("b", 1, player1), player1);
		history.addActionOf(new KuhnPokerAction("ch", 1, player2), player2);
		
		assertEquals(false, history.equals(history1));
	}

}
