package cz.agents.gtlibrary.domain.poker;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public abstract class PokerAction extends ActionImpl {

	private static final long serialVersionUID = 6347157865176927070L;
	
	protected final String action;
	protected final Player player;

	protected int cachedHash = 0;
	protected int cachedHashWithoutIS;

	public PokerAction(String action, InformationSet i, Player player) {
		super(i);
		this.action = action;
		this.player = player;
		cachedHash = computeHashCode();
		cachedHashWithoutIS = computeHashCodeWithoutIS();
	}

	public abstract int computeHashCode();

	public abstract int computeHashCodeWithoutIS();

	@Override
	public void perform(GameState gameState) {
		PokerGameState state = (PokerGameState) gameState;

		if (!getPlayer().equals(state.getPlayerToMove())) {
			throw new IllegalStateException("Wrong player attempts to make move.");
		}

		if (action.equals("b")) {
			state.bet(this);
		} else if (action.equals("c")) {
			state.call(this);
		} else if (action.equals("ch")) {
			state.check(this);
		} else if (action.equals("f")) {
			state.fold(this);
		} else if (action.equals("r")) {
			state.raise(this);
		} else {
			state.attendCard(this);
		}
	}

	public String getActionType() {
		return action;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		return "[" + action + ", " + player + ", " + getPlayersCard() + "]";
	}

	@Override
	public int hashCode() {
		return cachedHash;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		PokerAction other = (PokerAction) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		return true;
	}

	public boolean observableEquals(PokerAction other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (getClass() != other.getClass())
			return false;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		return true;
	}

	public int observableISHash() {
		return cachedHashWithoutIS;
	}

    public String getPlayersCard() {
        if (informationSet != null && player.getId() != 2 && !informationSet.getAllStates().isEmpty()) {
            return ((PokerGameState)informationSet.getAllStates().iterator().next()).getCardForActingPlayer().getActionType();
        } else
            return "none";
    }

}
