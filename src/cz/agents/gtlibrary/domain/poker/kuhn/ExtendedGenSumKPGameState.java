package cz.agents.gtlibrary.domain.poker.kuhn;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.GameState;

public class ExtendedGenSumKPGameState extends GenSumKPGameState {

    public ExtendedGenSumKPGameState(double rake) {
        super(rake);
    }

    public ExtendedGenSumKPGameState(ExtendedGenSumKPGameState gameState) {
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
        return new ExtendedGenSumKPGameState(this);
    }

    @Override
    public double[] getUtilities() {
        if (utilities != null) {
            return copy(utilities);
        }
        if (isGameEnd()) {
            int result = hasPlayerOneWon();

            if (result > 0)
                utilities = new double[]{(1 - rake) * gainForFirstPlayer + KPGameInfo.p1cardBounties.get(playerCards[0].getActionType()), -gainForFirstPlayer, 0};
            else if (result == 0)
                utilities = new double[]{0, 0, 0};
            else
                utilities = new double[]{gainForFirstPlayer - pot, (1 - rake) * (pot - gainForFirstPlayer) + KPGameInfo.p2cardBounties.get(playerCards[1].getActionType()), 0};
            return copy(utilities);
        }
        return new double[]{0};
    }
}
