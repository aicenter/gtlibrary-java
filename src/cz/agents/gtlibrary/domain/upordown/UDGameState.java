package cz.agents.gtlibrary.domain.upordown;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class UDGameState extends GameStateImpl {

	private static final long serialVersionUID = -8808441099769774667L;
	private UDAction p1Action;
	private UDAction p2Action;

	public UDGameState() {
		super(UDGameInfo.ALL_PLAYERS);
	}

	public UDGameState(UDGameState udGameState) {
		super(udGameState);
		this.p1Action = udGameState.p1Action;
		this.p2Action = udGameState.p2Action;
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		return 0;
	}

	@Override
	public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
		HashCodeBuilder builder = new HashCodeBuilder(17, 31);
		
		builder.append(p1Action);
		builder.append(p2Action);
		return new Pair<Integer, Sequence>(builder.toHashCode(), getSequenceForPlayerToMove());
	}

	@Override
	public Player getPlayerToMove() {
		if (p1Action == null)
			return players[0];
		if (p2Action == null)
			return players[1];
		return players[0];
	}

	@Override
	public GameState copy() {
		return new UDGameState(this);
	}

	@Override
	public double[] getUtilities() {
		if (p1Action.getType().equals("U")) {
			if (p2Action.getType().equals("l"))
				return new double[] { 0, 0 };
			if (p2Action.getType().equals("r"))
				return new double[] { 1, -1 };
			throw new UnsupportedOperationException();
		} else if (p1Action.getType().equals("D")) {
			if (p2Action.getType().equals("l'"))
				return new double[] { 0, 0 };
			if (p2Action.getType().equals("r'"))
				return new double[] { 2, -2 };
			throw new UnsupportedOperationException();
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isGameEnd() {
		return p1Action != null && p2Action != null;
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return false;
	}

	public void setP1Action(UDAction p1Action) {
		this.p1Action = p1Action;
	}

	public void setP2Action(UDAction p2Action) {
		this.p2Action = p2Action;
	}
	
	public UDAction getP1Action() {
		return p1Action;
	}
	
	public UDAction getP2Action() {
		return p2Action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((p1Action == null) ? 0 : p1Action.hashCode());
		result = prime * result + ((p2Action == null) ? 0 : p2Action.hashCode());
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
		UDGameState other = (UDGameState) obj;
		if (history == null) {
			if (other.history != null)
				return false;
		} else if (!history.equals(other.history))
			return false;
		if (p1Action == null) {
			if (other.p1Action != null)
				return false;
		} else if (!p1Action.equals(other.p1Action))
			return false;
		if (p2Action == null) {
			if (other.p2Action != null)
				return false;
		} else if (!p2Action.equals(other.p2Action))
			return false;
		return true;
	}

}