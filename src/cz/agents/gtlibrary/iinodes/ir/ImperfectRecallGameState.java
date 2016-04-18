package cz.agents.gtlibrary.iinodes.ir;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public abstract class ImperfectRecallGameState extends GameStateImpl {
    protected List<String> actionTypeSequence;

    public ImperfectRecallGameState(Player[] players) {
        super(players);
        actionTypeSequence = new ArrayList<>();
    }

    public ImperfectRecallGameState(ImperfectRecallGameState gameState) {
        super(gameState);
        actionTypeSequence = new ArrayList<>(gameState.actionTypeSequence);
    }

    @Override
    public void performActionModifyingThisState(Action action) {
        super.performActionModifyingThisState(action);
        actionTypeSequence.add(((ImperfectRecallAction)action).getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImperfectRecallGameState)) return false;

        ImperfectRecallGameState that = (ImperfectRecallGameState) o;

        if (!actionTypeSequence.equals(that.actionTypeSequence))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return actionTypeSequence.hashCode();
    }
}
