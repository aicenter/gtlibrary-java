package cz.agents.gtlibrary.domain.ir.memoryloss;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.iinodes.ir.ImperfectRecallAction;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

@Deprecated
public class MLAction extends ImperfectRecallAction {

    public MLAction(InformationSet informationSet, String type) {
        super(informationSet, type);
    }

    @Override
    public void perform(GameState gameState) {
        ((MLGameState)gameState).increaseRound();
        if(type.endsWith("2"))
            ((MLGameState)gameState).switchPlayer();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MLAction)) return false;
        MLAction mlAction = (MLAction) o;

        if(!type.substring(0,1).equals(mlAction.type.substring(0,1))) {
            if (type.endsWith("1") && mlAction.type.endsWith("1")) return true;
        }
        if (!super.equals(o)) return false;
        if (type != null ? !type.equals(mlAction.type) : mlAction.type != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
        return hashCode;
    }

    @Override
    public String toString() {
        return type;
    }
}
