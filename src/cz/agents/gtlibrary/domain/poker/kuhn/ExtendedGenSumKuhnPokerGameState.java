package cz.agents.gtlibrary.domain.poker.kuhn;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.GameState;

public class ExtendedGenSumKuhnPokerGameState extends GenSumKuhnPokerGameState {

    public ExtendedGenSumKuhnPokerGameState() {
        super();
    }

    public ExtendedGenSumKuhnPokerGameState(ExtendedGenSumKuhnPokerGameState gameState) {
        super(gameState);
    }

    @Override
    public void raise(PokerAction action) {
        clearCachedValues();
        addToPot(getValueOfCall() + getValueOfAggressive(action));
        addActionToSequence(action);
        switchPlayers();
    }

    @Override
    public GameState copy() {
        return new ExtendedGenSumKuhnPokerGameState(this);
    }
}
