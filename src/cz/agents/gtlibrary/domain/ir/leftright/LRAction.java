package cz.agents.gtlibrary.domain.ir.leftright;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.iinodes.ir.ImperfectRecallAction;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

@Deprecated
public class LRAction extends ImperfectRecallAction {


    public LRAction(InformationSet informationSet, String type) {
        super(informationSet, type);
    }

    @Override
    public void perform(GameState gameState) {
        if(type.startsWith("L") || type.startsWith("R"))
            ((LRGameState)gameState).switchPlayers();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LRAction)) return false;
        LRAction lrAction = (LRAction) o;
        if(type.startsWith("P") && lrAction.type.startsWith("P")) return true;
        if (!super.equals(o)) return false;
        if (type != null ? !type.equals(lrAction.type) : lrAction.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + type.substring(0,1).hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        return "LRA: " + type;
    }
}
