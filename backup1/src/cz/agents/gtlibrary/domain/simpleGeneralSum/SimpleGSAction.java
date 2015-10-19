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


package cz.agents.gtlibrary.domain.simpleGeneralSum;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/5/13
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleGSAction extends ActionImpl {

    protected int ID;
    protected Player player;

    protected int hashCode;
    protected boolean hashChange = true;

    public SimpleGSAction(InformationSet informationSet, int id, Player player) {
        super(informationSet);
        this.ID = id;
        this.player = player;
    }

    @Override
    public void perform(GameState gameState) {
        ((SimpleGSState)gameState).executeAction(this);
    }

    @Override
    public int hashCode() {
        if(hashChange) {
            final int prime = 31;
            hashCode = 1;

            hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
            hashCode = prime * hashCode + ID;

            hashChange = false;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        SimpleGSAction other = (SimpleGSAction)obj;
        if (this.ID != other.ID)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SGSA:" + player + " " + ID;
    }
}
