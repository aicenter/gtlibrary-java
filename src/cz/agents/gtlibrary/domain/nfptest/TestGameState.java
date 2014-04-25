package cz.agents.gtlibrary.domain.nfptest;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class TestGameState extends GameStateImpl {

	private static final long serialVersionUID = 2837241225140778346L;

	private int round;

	public TestGameState() {
		super(TestGameInfo.ALL_PLAYERS);
		round = 0;
    }

	public TestGameState(TestGameState gameState) {
		super(gameState);
		this.round = gameState.round;
	}

	public void performP1Action(P1TestAction action) {
		if (action.getActionType().equals("D"))
			round = 3;
		else
			round++;
	}

	public void performP2Action(P2TestAction action) {
		round++;
	}

	public P2TestAction getLastActionOfP2() {
		if (getSequenceFor(players[1]).size() == 0)
			return null;
		return (P2TestAction) getSequenceFor(players[1]).getLast();
	}

	public P1TestAction getLastActionOfP1() {
		if (getSequenceFor(players[0]).size() == 0)
			return null;
		return (P1TestAction) getSequenceFor(players[0]).getLast();
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		return 0;
	}

	@Override
	public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
		return new Pair<Integer, Sequence>(history.hashCode(), getSequenceForPlayerToMove());
	}

	@Override
	public Player getPlayerToMove() {
		return players[round % 2];
	}

	@Override
	public GameState copy() {
		return new TestGameState(this);
	}

	@Override
	public double[] getUtilities() {
		if (!isGameEnd())
			return new double[] { 0 };
		if (getLastActionOfP1().getActionType().equals("L"))
			return new double[] { -2, 2 };
		if (getLastActionOfP1().getActionType().equals("R"))
			return new double[] { -1, 1 };
		if (getLastActionOfP1().getActionType().equals("L'"))
			return new double[] { 1, -1 };
		if (getLastActionOfP1().getActionType().equals("R'"))
			return new double[] { 2, -2 };
		return new double[] { -1, 1 };
	}

	@Override
	public boolean isGameEnd() {
		return round == 3;
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + round;
		result = prime * result + history.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestGameState other = (TestGameState) obj;

		if (round != other.round)
			return false;
		if (!history.equals(other.history))
			return false;
		return true;
	}

}
