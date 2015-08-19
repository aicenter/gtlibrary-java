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


package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public abstract class ActionImpl implements Action {

    private static final long serialVersionUID = -7380941202647059723L;

    protected InformationSet informationSet;

    public ActionImpl(InformationSet informationSet) {
        //		assert (informationSet != null);
        this.informationSet = informationSet;
    }

    @Override
    public abstract void perform(GameState gameState);

    @Override
    public InformationSet getInformationSet() {
        return informationSet;
    }

    @Override
    public void setInformationSet(InformationSet informationSet) {
        this.informationSet = informationSet;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (this.hashCode() != obj.hashCode())
            return false;
        if (informationSet == null) {
            if (((ActionImpl) obj).informationSet != null)
                return false;
        } else if(informationSet != null && ((ActionImpl) obj).informationSet == null) {
            return false;
        } else if (informationSet.hashCode() != ((ActionImpl) obj).informationSet.hashCode()) {
            return false;
        } else if (informationSet.getPlayersHistory().size() != ((ActionImpl) obj).informationSet.getPlayersHistory().size()) {
            return false;
        } else for (int l = 0; l < informationSet.getPlayersHistory().size(); l++) {
            Action myAction = informationSet.getPlayersHistory().get(l);
            Action otherAction = ((ActionImpl) obj).informationSet.getPlayersHistory().get(l);
            if (myAction.hashCode() != otherAction.hashCode())
                return false;
            if ((myAction.getInformationSet() == null && otherAction.getInformationSet() != null) ||
                    (myAction.getInformationSet() != null && otherAction.getInformationSet() == null))
                return false;
            if (myAction.getInformationSet() != null)
                if (myAction.getInformationSet().hashCode() != otherAction.getInformationSet().hashCode())
                    return false;
        }
//        } else if (!informationSet.equals(((ActionImpl) obj).informationSet))
//            return false;
        return true;
    }

    @Override
    abstract public int hashCode();
}
