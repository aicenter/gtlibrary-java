package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.graph.Node;

/**
 * Created by Jakub Cerny on 13/03/17.
 */
public class FlipItAction extends ActionImpl {

    private Node controlNode;
    private boolean isNoop;
    private Player controller;

    // CONTROL action
    public FlipItAction(Node controlNode, InformationSet informationSet) {
        super(informationSet);
        this.controlNode = controlNode;
        this.isNoop = false;
        this.controller = null;
    }

    // NOOP action
    public FlipItAction(InformationSet informationSet) {
        super(informationSet);
        this.controlNode = null;
        this.isNoop = true;
        this.controller = null;
    }

    // RANDOM action
    public FlipItAction(Player controller, InformationSet informationSet) {
        super(informationSet);
        this.controlNode = null;
        this.isNoop = false;
        this.controller = controller;
    }

    public boolean isNoop(){
        return  isNoop;
    }

    public Node getControlNode(){
        return controlNode;
    }

    public Player getController(){
        return controller;
    }

    @Override
    public void perform(GameState gameState) {
        if (gameState.getPlayerToMove().equals(FlipItGameInfo.DEFENDER)){
            ((FlipItGameState)gameState).executeDefenderAction(this);
            return;
        }
        if (gameState.getPlayerToMove().equals(FlipItGameInfo.ATTACKER)) {
            ((FlipItGameState) gameState).executeAttackerAction(this);
            return;
        }
        if (gameState.getPlayerToMove().equals(FlipItGameInfo.NATURE))
            ((FlipItGameState)gameState).executeNatureAction(this);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FlipItAction that = (FlipItAction) o;

        if (isNoop != that.isNoop) return false;
        if (controlNode != null ? !controlNode.equals(that.controlNode) : that.controlNode != null) return false;
        if (informationSet != null ? !informationSet.equals(that.informationSet) : that.informationSet != null) return false;
        return controller != null ? controller.equals(that.controller) : that.controller == null;

    }

    @Override
    public int hashCode() {
        int result = controlNode != null ? controlNode.hashCode() : 23;
        result = 31 * result + (isNoop ? 13 : 29);
        result = 31 * result + (controller != null ? controller.hashCode() : 37);
        result = 31 * result + informationSet.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "{" + (controlNode == null ? "_" : controlNode) +
                ", " + (isNoop ? "NOOP" : "_") +
                ", " + (controller == null ? "_" : ((controller.equals(FlipItGameInfo.DEFENDER)) ? "D" : "A")) + ", " + getInformationSet().hashCode() +
                '}';
    }
}
