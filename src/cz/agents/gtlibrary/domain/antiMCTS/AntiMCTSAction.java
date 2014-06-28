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
