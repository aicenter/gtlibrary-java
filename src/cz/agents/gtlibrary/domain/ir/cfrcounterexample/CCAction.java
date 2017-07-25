package cz.agents.gtlibrary.domain.ir.cfrcounterexample;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class CCAction extends ActionImpl {
    String actionType;

    public CCAction(String actionType, InformationSet informationSet) {
        super(informationSet);
        this.actionType = actionType;
    }

    @Override
    public void perform(GameState gameState) {
        ((CCGameState)gameState).checkAndChangePlayer();
    }

    public String getActionType() {
        return actionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CCAction)) return false;
        if (!super.equals(o)) return false;

        CCAction ccAction = (CCAction) o;

        return actionType.equals(ccAction.actionType);

    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return actionType;
    }
}
