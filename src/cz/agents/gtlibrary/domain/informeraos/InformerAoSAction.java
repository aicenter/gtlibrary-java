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

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public abstract class InformerAoSAction extends ActionImpl {

    private static final long serialVersionUID = -1465461941414355860L;

    private String actionType;

    public InformerAoSAction(InformationSet informationSet, String actionType) {
        super(informationSet);
        this.actionType = actionType;
    }

    @Override
    public abstract void perform(GameState gameState);

    public String getActionType() {
        return actionType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + actionType.hashCode();
        if (informationSet != null)
            result = prime * result + informationSet.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        InformerAoSAction other = (InformerAoSAction) obj;
        if (!actionType.equals(other.actionType))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return actionType;
    }

}
