/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.domain.phantomTTT;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class TTTAction extends ActionImpl  {
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
        
        if (' ' == s.getSymbol(fieldID)){
            s.setSymbol(fieldID, s.toMove);
            s.toMove = (s.toMove == 'x' ? 'o' : 'x' );
        }
        s.moveNum++;
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
        if (!super.equals(obj))	return false;
        final TTTAction other = (TTTAction) obj;
        if (this.fieldID != other.fieldID) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.fieldID;
        if (getInformationSet() != null) hash = 37 * hash + (int) getInformationSet().hashCode();
        return hash;
    }
    
    
    
    
}
