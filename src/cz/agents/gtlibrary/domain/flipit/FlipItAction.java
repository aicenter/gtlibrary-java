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
            ((NodePointsFlipItGameState)gameState).executeDefenderAction(this);
            return;
        }
        if (gameState.getPlayerToMove().equals(FlipItGameInfo.ATTACKER)) {
            ((NodePointsFlipItGameState) gameState).executeAttackerAction(this);
            return;
        }
        if (gameState.getPlayerToMove().equals(FlipItGameInfo.NATURE))
            ((NodePointsFlipItGameState)gameState).executeNatureAction(this);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlipItAction that = (FlipItAction) o;

        if (isNoop != that.isNoop) return false;
        if (controlNode != null ? !controlNode.equals(that.controlNode) : that.controlNode != null) return false;
        if (controller  != null ? !controller.equals(that.controller)   : that.controller != null)  return false;
        return super.equals(o);

    }

    @Override
    public int hashCode() {
        int result = controlNode != null ? controlNode.getIntID()+1/*hashCode()*/ : 0;
        result = 31 * result + (isNoop ? 1 : 2);
        result = 31 * result + (controller != null ? (controller.equals(FlipItGameInfo.DEFENDER) ? 3 : 7) : 5);
        result = 31 * result + (getInformationSet()!=null ? getInformationSet().hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        if(isNoop) return "(PASS:"+informationSet.hashCode()+")";
        else return "("+controlNode.toString()+":"+informationSet.hashCode()+")";
//        return "{" + (controlNode == null ? "_" : controlNode) +
//                ", " + (isNoop ? "NOOP" : "_") +
//                ", " + (controller == null ? "_" : ((controller.equals(FlipItGameInfo.DEFENDER)) ? "D" : "A")) + ", " + getInformationSet().hashCode() +
//                '}';
    }
}
