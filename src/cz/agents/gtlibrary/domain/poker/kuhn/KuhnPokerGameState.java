package cz.agents.gtlibrary.domain.poker.kuhn;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.domain.poker.PokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public class KuhnPokerGameState extends PokerGameState {

	public KuhnPokerGameState() {
		super(new Player[] { KPGameInfo.FIRST_PLAYER, KPGameInfo.SECOND_PLAYER, KPGameInfo.NATURE }, KPGameInfo.ANTE);
	}

	public KuhnPokerGameState(KuhnPokerGameState gameState) {
		super(gameState);
	}

	@Override
	protected int hasPlayerOneWon() {
		if (sequenceForAllPlayers.getLast().getActionType().equals("f")) {
			return currentPlayerIndex == 1 ? -1 : 1;
		}
		return compareCards(playerCards[0], playerCards[1]);
	}

	private int compareCards(PokerAction fpCard, PokerAction spCard) {
		return Integer.parseInt(fpCard.getActionType()) - Integer.parseInt(spCard.getActionType());
	}

	@Override
	public GameState copy() {
		return new KuhnPokerGameState(this);
	}

	@Override
	protected int getValueOfAggressive(PokerAction action) {
		return KPGameInfo.BET;
	}

	@Override
	protected int getValueOfCall() {
		return KPGameInfo.BET;
	}

	@Override
	protected int getTerminalRound() {
		return 2;
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		if (!isPlayerToMoveNature()) {
			return 0;
		}
		if (playerCards[0] != null) {
			return 0.5;
		}
		return 1./3;
	}

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        if (!isPlayerToMoveNature()) {
            return Rational.ZERO;
        }
        if (playerCards[0] != null) {
            return new Rational(1, 2);
        }
        return new Rational(1, 3);
    }
	
	@Override
	public void raise(PokerAction action) {
		throw new UnsupportedOperationException("Raise is not defined in Kuhn-Poker");
	}

	@Override
	public void attendCard(PokerAction action) {
		if (round == 0) {
			clearCachedValues();
			dealCardToPlayer(action);
		}
	}

}
