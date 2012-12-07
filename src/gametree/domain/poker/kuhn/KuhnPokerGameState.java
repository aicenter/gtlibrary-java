package gametree.domain.poker.kuhn;

import gametree.domain.poker.PokerAction;
import gametree.domain.poker.PokerGameState;
import gametree.interfaces.GameState;
import gametree.interfaces.Player;

public class KuhnPokerGameState extends PokerGameState{
	
	public KuhnPokerGameState() {
		super(new Player[]{KPGameInfo.FIRST_PLAYER, KPGameInfo.SECOND_PLAYER, KPGameInfo.NATURE}, KPGameInfo.ANTE);
	}

	public KuhnPokerGameState(KuhnPokerGameState gameState) {
		super(gameState);
	}

	@Override
	protected int hasPlayerOneWon() {
		if (sequenceForAllPlayers.getLast().getAction().equals("f")) {
			return currentPlayerIndex == 1 ? -1 : 1;
		}
		return compareCards(playerCards[0], playerCards[1]);
	}

	private int compareCards(PokerAction fpCard, PokerAction spCard) {
		return Integer.parseInt(fpCard.getAction()) - Integer.parseInt(spCard.getAction());
	}

	@Override
	public GameState copy() {
		return new KuhnPokerGameState(this);
	}

	@Override
	protected int getValueOfBet(PokerAction action) {
		return KPGameInfo.BET;
	}

	@Override
	protected int getValueOfCall(PokerAction action) {
		return KPGameInfo.BET;
	}

	@Override
	protected int getTerminalRound() {
		return 2;
	}

}
