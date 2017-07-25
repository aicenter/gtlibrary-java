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
package cz.agents.gtlibrary.domain.antiMCTS;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class AntiMCTSAction extends ActionImpl  {
    boolean right;

    public AntiMCTSAction(boolean rightMove, InformationSet i) {
        super(i);
        this.right = rightMove;
    }
    
    @Override
    public void perform(GameState gameState) {
        final AntiMCTSState s = (AntiMCTSState) gameState;
        s.curDepth++;
        if (!right || s.curDepth==AntiMCTSInfo.gameDepth) s.gameEnded = true;
        if (right && s.gameEnded) s.curDepth=0;
    }

    @Override
    public String toString() {
        return (right?"R":"W");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj))	return false;
        final AntiMCTSAction other = (AntiMCTSAction) obj;
        if (this.right != other.right) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (right ? 1 : 0);
        if (getInformationSet() != null) hash = 37 * hash + (int) getInformationSet().hashCode();
        return hash;
    }
    
    
    
    
}
