/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.domain.informeraos;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.LinkedList;
import java.util.List;

public class InformerAoSExpander<I extends InformationSet> extends ExpanderImpl<I> {

    private static final long serialVersionUID = 8770670308276388429L;

    public InformerAoSExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        List<Action> actions = new LinkedList<Action>();

        if (gameState.isPlayerToMoveNature()) {
            actions.add(new NatureInformerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), "AoS"));
            actions.add(new NatureInformerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), "nAoS"));
            return actions;
        }
        if (gameState.getPlayerToMove().equals(InformerAoSGameInfo.FIRST_PLAYER)) {
            actions.add(new P1InformerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), "C"));
            actions.add(new P1InformerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), "N"));
            actions.add(new P1InformerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), "H"));
            return actions;
        }
        if (((InformerAoSAction) gameState.getSequenceFor(InformerAoSGameInfo.FIRST_PLAYER).getLast()).getActionType().equals("H")) {
            actions.add(new P2InformerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), "S"));
            actions.add(new P2InformerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), "G"));
        } else {
            actions.add(new P2InformerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), "AoS"));
            actions.add(new P2InformerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), "nAoS"));
        }
        return actions;
    }

}
