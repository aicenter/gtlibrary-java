package poker.kuhn;

import static org.junit.Assert.*;

import org.junit.Test;

import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.iinodes.PlayerImpl;

public class KPGameInfoTest {

	@Test
	public void test() {
		KPGameInfo.ANTE = 1;
		KPGameInfo.BET = 1;
		KPGameInfo info = new KPGameInfo();
		assertEquals(new PlayerImpl(0), KPGameInfo.FIRST_PLAYER);
		assertEquals(new PlayerImpl(1), KPGameInfo.SECOND_PLAYER);
		assertEquals(new PlayerImpl(2), KPGameInfo.NATURE);
		assertEquals(2, info.getMaxUtility(), 0.00001);
		assertEquals(5, info.getMaxDepth());
		assertEquals(KPGameInfo.SECOND_PLAYER, info.getOpponent(KPGameInfo.FIRST_PLAYER));
		assertEquals(KPGameInfo.FIRST_PLAYER, info.getOpponent(KPGameInfo.SECOND_PLAYER));
	}

}
