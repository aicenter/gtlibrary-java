package cz.agents.gtlibrary.experimental.stochastic;

import cz.agents.gtlibrary.algorithms.sequenceform.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public abstract class StochasticGameState extends GameStateImpl {

	private static final long serialVersionUID = -4093532858874209051L;

	public StochasticGameState(Player[] players) {
		super(players);
	}
	
	public StochasticGameState(StochasticGameState state) {
		super(state);
	}

	@Override
	public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
		return null;
	}
	
	@Override
	public boolean checkConsistency(Action action) {
		return true;
	}

    @Override
    public Rational[] getExactUtilities() {
        throw new UnsupportedOperationException("Not supported...");
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        throw new UnsupportedOperationException("Not supported...");
    }
}
