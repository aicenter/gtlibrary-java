package cz.agents.gtlibrary.domain.honeypotGame;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Petr Tomasek on 29.4.2017.
 */
public class HoneypotAction extends ActionImpl {
    HoneypotGameNode node;
    private Player player;

    HoneypotAction(HoneypotGameNode node, InformationSet informationSet, Player player) {
        super(informationSet);
        this.player = player;
        this.node = node;
    }

    @Override
    public void perform(GameState gameState) {
        if (gameState.getPlayerToMove().equals(HoneypotGameInfo.DEFENDER)){
            ((HoneypotGameState)gameState).executeDefenderAction(this);
            return;
        }
        if (gameState.getPlayerToMove().equals(HoneypotGameInfo.ATTACKER)) {
            ((HoneypotGameState) gameState).executeAttackerAction(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HoneypotAction)) return false;
        if (!super.equals(obj)) return false;

        HoneypotAction other = (HoneypotAction) obj;

        if (!player.equals(other.player)) return false;
        if (!node.equals(other.node)) return false;
        if (!informationSet.equals(other.informationSet)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;

        result = node.hashCode();
        result = result * 31 + player.getId();
        result = result * 31 + informationSet.hashCode();

        return result;
//        return new HashCodeBuilder(17,31).append(node).append(player).append(informationSet).toHashCode();
    }

    @Override
    public String toString() {
//        return "node " + node.getID();
        return "("+node+")";
//        return player + " - " + node;
    }
}
