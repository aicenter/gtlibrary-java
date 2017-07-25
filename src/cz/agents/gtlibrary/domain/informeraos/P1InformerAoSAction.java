package cz.agents.gtlibrary.domain.informeraos;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class P1InformerAoSAction extends InformerAoSAction {

    public P1InformerAoSAction(InformationSet informationSet, String actionType) {
        super(informationSet, actionType);
    }

    @Override
    public void perform(GameState gameState) {
        ((InformerAoSGameState) (gameState)).performFirstPlayerAction(this);
    }
}
