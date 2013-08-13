package cz.agents.gtlibrary.nfg.experimental.domain.bpg;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPActionImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/25/13
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class BPAction extends MDPActionImpl {

    private Player player;
    private UnitMove[] moves;

    private int hash;
    private boolean changed = true;

    public BPAction(Player player, UnitMove[] moves) {
        this.player = player;
        this.moves = moves;
    }

    @Override
    public void perform(MDPState state) {
        state.performAction(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        BPAction other = (BPAction)obj;
        if (!this.player.equals(other.getPlayer()))
            return false;
        if (this.moves.length != other.moves.length)
            return false;
        for (int i=0; i<this.moves.length; i++) {
            if (!this.moves[i].equals(other.moves[i]))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
       if (changed) {
           HashCodeBuilder hb = new HashCodeBuilder(31, 17);
           hb.append(player);
           hb.append(moves);
           hash = hb.toHashCode();
       }
       changed = false;
       return hash;
    }

    public Player getPlayer() {
        return player;
    }

    public UnitMove[] getMoves() {
        return moves;
    }

    static public class UnitMove {
        private int unitNumber;
        private int fromNode;  // if equals to '-1', it is a starting action from the root
        private int toNode;
        private boolean willSeeTheFlag = false;

        private int hash;
        private boolean changed = true;

        public UnitMove(int unitNumber, int fromNode, int toNode) {
            this.unitNumber = unitNumber;
            this.fromNode = fromNode;
            this.toNode = toNode;
        }

        public int getUnitNumber() {
            return unitNumber;
        }

        public int getFromNode() {
            return fromNode;
        }

        public int getToNode() {
            return toNode;
        }

        @Override
        public int hashCode() {
            if (changed) {
                HashCodeBuilder hb = new HashCodeBuilder(31, 17);
                hb.append(unitNumber);
                hb.append(fromNode);
                hb.append(toNode);
                hash = hb.toHashCode();
            }
            changed = false;
            return hash;
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
            UnitMove m = (UnitMove)obj;
            if (this.unitNumber != m.unitNumber) return false;
            if (this.fromNode != m.fromNode) return false;
            if (this.toNode != m.toNode) return false;
            return true;
        }

        @Override
        public String toString() {
            return "UM:"+unitNumber+":"+fromNode+"->"+toNode+(isWillSeeTheFlag()?"\'":"");
        }

        protected void setWillSeeTheFlag(boolean willSeeTheFlag) {
            this.willSeeTheFlag = willSeeTheFlag;
        }

        public boolean isWillSeeTheFlag() {
            return willSeeTheFlag;
        }
    }

    @Override
    public String toString() {
        return "BPAction:"+getPlayer()+":"+ Arrays.toString(getMoves());
    }
}
