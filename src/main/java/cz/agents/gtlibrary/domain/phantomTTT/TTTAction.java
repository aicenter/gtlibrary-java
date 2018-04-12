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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.domain.phantomTTT;

import cz.agents.gtlibrary.domain.phantomTTT.imperfectrecall.IRTTTState;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class TTTAction extends ActionImpl  {
	
	private static final long serialVersionUID = -542752552988722459L;
    private int hashCode = -1;
	
	public byte fieldID;

    public TTTAction(byte fieldID, InformationSet i) {
        super(i);
        this.fieldID = fieldID;
    }
    
    @Override
    public void perform(GameState gameState) {
        final TTTState s = (TTTState) gameState;
        assert !s.getTried(s.toMove, fieldID);
        s.setTried(s.toMove, fieldID);

        if(s instanceof IRTTTState)
            ((IRTTTState)s).key = null;
        
        if (' ' == s.getSymbol(fieldID)){
            s.setSymbol(fieldID, s.toMove);
            s.toMove = (s.toMove == 'x' ? 'o' : 'x' );
        }
        s.moveNum++;
        s.hashCode = -1;
    }

    @Override
    public String toString() {
        return getInformationSet().getPlayer().toString() + fieldID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TTTAction other = (TTTAction) obj;

        if (this.fieldID != other.fieldID) {
            return false;
        }
        if (!super.equals(obj))	return false;
        return true;
    }

    @Override
    public int hashCode() {
        if(hashCode == -1) {
            hashCode = 7;
            hashCode = 37 * hashCode + this.fieldID;
            if (getInformationSet() != null) hashCode = 37 * hashCode + getInformationSet().hashCode();
        }
        return hashCode;
    }
    
    
    
    
}
