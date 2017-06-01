package cz.agents.gtlibrary.domain.ir.aaaidomain;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class AAAIAction extends ActionImpl {

    private String actionType;

    public AAAIAction(String actionType, InformationSet informationSet) {
        super(informationSet);
        this.actionType = actionType;
    }

    @Override
    public void perform(GameState gameState) {
        ((AAAIGameState)gameState).changePlayer();
    }

    public String getActionType() {
        return actionType;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof AAAIAction))
            return false;
        AAAIAction other = (AAAIAction) obj;

        if(actionType.equals(other.actionType))
            return true;
        return false;
//        return (actionType.equals("a") && other.actionType.equals("b")) || (actionType.equals("b") && other.actionType.equals("a"));
    }

    @Override
    public String toString() {
        return actionType;
    }
}
