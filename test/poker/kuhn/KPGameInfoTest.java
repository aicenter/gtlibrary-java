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
