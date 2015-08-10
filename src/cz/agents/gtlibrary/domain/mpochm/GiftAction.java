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


package cz.agents.gtlibrary.domain.mpochm;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class GiftAction extends ActionImpl {
    private MPoCHMGameState.GiftState state;
    private int hashCode = -1;

    public GiftAction(MPoCHMGameState.GiftState state, InformationSet informationSet) {
        super(informationSet);
        this.state = state;
    }

    @Override
    public void perform(GameState gameState) {
        ((MPoCHMGameState) gameState).setGiftState(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GiftAction)) return false;
        if (!super.equals(o)) return false;

        GiftAction that = (GiftAction) o;

        if (state != that.state) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode != -1)
            return hashCode;
        final int prime = 31;

        hashCode = 1;
        hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
        hashCode = prime * hashCode + state.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        return "[" + state + "]";
    }
}
