package cz.agents.gtlibrary.domain.informeraos;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class NatureInformerAoSAction extends InformerAoSAction {

    public NatureInformerAoSAction(InformationSet informationSet, String actionType) {
        super(informationSet, actionType);
    }

    @Override
    public void perform(GameState gameState) {
        ((InformerAoSGameState)gameState).performNatureAction(this);
    }
}
