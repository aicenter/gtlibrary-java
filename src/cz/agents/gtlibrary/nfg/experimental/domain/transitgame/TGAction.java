package cz.agents.gtlibrary.nfg.experimental.domain.transitgame;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPActionImpl;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/16/13
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class TGAction extends MDPActionImpl {

    private Player player;
    private int[] targetCol;
    private int[] targetRow;

    private int hash;
    private boolean changed = true;

    public TGAction(Player player, int[] targetCol, int[] targetRow) {
        this.player = player;
        this.targetCol = targetCol;
        this.targetRow = targetRow;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        TGAction other = (TGAction)obj;
        if (!this.player.equals(other.getPlayer()))
            return false;
        if (this.targetCol.length != other.targetCol.length)
            return false;
        for (int i=0; i<this.targetCol.length; i++) {
            if (this.targetCol[i] != other.targetCol[i])
                return false;
            if (this.targetRow[i] != other.targetRow[i])
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (changed) {
            HashCodeBuilder hb = new HashCodeBuilder(31, 17);
            hb.append(player);
            for (int i=0; i<this.targetCol.length; i++) {
                hb.append(targetCol[i]);
                hb.append(targetRow[i]);
            }
            hash = hb.toHashCode();
            changed = false;
        }
        return hash;
    }

    public Player getPlayer() {
        return player;
    }

    public int[] getTargetCol() {
        return targetCol;
    }

    public int[] getTargetRow() {
        return targetRow;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TGAction:"+getPlayer()+":");
        for (int i=0; i<targetCol.length; i++) {
            sb.append("[");
            sb.append(targetRow[i]);
            sb.append(",");
            sb.append(targetCol[i]);
            sb.append("]");
        }
        return sb.toString();
    }
}
