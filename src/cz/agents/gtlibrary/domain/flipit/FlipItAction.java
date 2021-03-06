package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.AbstractActionProvider;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.ImmediateActionOutcomeProvider;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.graph.Node;

/**
 * Created by Jakub Cerny on 13/03/17.
 */
public class FlipItAction extends ActionImpl implements AbstractActionProvider, ImmediateActionOutcomeProvider {

    private Node controlNode;
    private boolean isNoop;
    private Player controller;
    private int hash;

    // CONTROL action
    public FlipItAction(Node controlNode, InformationSet informationSet) {
        super(informationSet);
        this.controlNode = controlNode;
        this.isNoop = false;
        this.controller = null;
        this.hash = computeHash();
    }

    // NOOP action
    public FlipItAction(InformationSet informationSet) {
        super(informationSet);
        this.controlNode = null;
        this.isNoop = true;
        this.controller = null;
        this.hash = computeHash();
    }

    // RANDOM action
    public FlipItAction(Player controller, InformationSet informationSet) {
        super(informationSet);
        this.controlNode = null;
        this.isNoop = false;
        this.controller = controller;
        this.hash = computeHash();
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

    private int computeHash(){
        int result = controlNode != null ? controlNode.getIntID()+1/*hashCode()*/ : 0;
        result = 31 * result + (isNoop ? 1 : 2);
        result = 31 * result + (controller != null ? (controller.equals(FlipItGameInfo.DEFENDER) ? 3 : 7) : 5);
        result = 31 * result + (getInformationSet()!=null ? getInformationSet().hashCode() : 0);
        return result;
    }

    @Override
    public int hashCode() {
//        if(this.hash != computeHash()){
//            System.out.println("different action hash"); System.exit(0);
//        }
        return hash;
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

    protected Integer dummySituationAbstraction = 0;
    protected Integer dummyActionAbstraction = 0;

    @Override
    public Object getSituationAbstraction() {
        return dummySituationAbstraction;
    }

    @Override
    public Object getActionAbstraction() {
        return controlNode == null ? dummyActionAbstraction : controlNode;
    }

    @Override
    public double getMaximumActionUtility() {
        return controlNode == null ? 0.0 : FlipItGameInfo.graph.getReward(controlNode);
    }

    @Override
    public double[] getAllPossibleOutcomes() {
        return new double[0];
    }


    @Override
    public double getImmediateRewardForAction(Action action) {
        return 0;
    }

    @Override
    public double getImmediateReward() {
        return isNoop ? 0.0 : FlipItGameInfo.graph.getReward(controlNode);
    }

    @Override
    public double getImmediateCost() {
        return isNoop ? 0.0 : FlipItGameInfo.graph.getControlCost(controlNode);
    }
}
