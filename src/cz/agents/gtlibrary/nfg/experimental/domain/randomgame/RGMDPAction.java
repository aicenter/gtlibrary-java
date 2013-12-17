package cz.agents.gtlibrary.nfg.experimental.domain.randomgame;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPActionImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/12/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class RGMDPAction extends MDPActionImpl {

    private Player player;
    private int ID = 0; // {0, .., BF-1}

    private int hash;
    private boolean changed = true;

    public RGMDPAction(Player player, int ID) {
        this.player = player;
        this.ID = ID;
    }

    @Override
    public int hashCode() {
        if (changed) {
            HashCodeBuilder hb = new HashCodeBuilder(31, 17);
            hb.append(player);
            hb.append(ID);
            hash = hb.toHashCode();
            changed = false;
        }
        return hash;
    }

    public Player getPlayer() {
        return player;
    }

    public int getID() {
        return ID;
    }

    @Override
    public String toString() {
        return "RGA_"+getPlayer()+"_ID:"+ID;
    }
}
