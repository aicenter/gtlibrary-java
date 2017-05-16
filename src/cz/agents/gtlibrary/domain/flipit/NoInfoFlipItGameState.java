package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Jakub on 13/03/17.
 */
public class NoInfoFlipItGameState extends FlipItGameState {

    double defenderReward;
    HashMap<FollowerType, Double> attackerReward;
    boolean[] defenderOwnedNodes;
    boolean[] attackerPossiblyOwnedNodes;
    int hashCode;

    public NoInfoFlipItGameState(NoInfoFlipItGameState gameState) {
        this.selectedNodeOwner = gameState.selectedNodeOwner;
        this.attackerControlNode = gameState.attackerControlNode;
        this.defenderControlNode = gameState.defenderControlNode;
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.attackerPoints = gameState.attackerPoints;

        this.defenderOwnedNodes = copyNodes(gameState.defenderOwnedNodes);
        this.attackerPossiblyOwnedNodes = copyNodes(gameState.attackerPossiblyOwnedNodes);

        this.history = gameState.getHistory().copy();
        this.natureProbability = gameState.getNatureProbability();
        this.exactNatureProbability = gameState.exactNatureProbability;
        this.players = gameState.getAllPlayers();

        this.defenderReward = gameState.defenderReward;
        this.attackerReward = new HashMap<>(gameState.attackerReward);
        this.hashCode = -1;

    }

    @Override
    protected void init(){

    }

    protected boolean[] copyNodes(boolean[] nodes){
        boolean[] newNodes = new boolean[nodes.length];
        for (int i = 0; i < nodes.length; i++)
            newNodes[i] = nodes[i];
        return newNodes;
    }

    public NoInfoFlipItGameState() {
        super();
        super.init();
        hashCode = -1;
        defenderRewards = null;
        attackerRewards = null;
        attackerObservations = null;
        defenderObservations = null;
        attackerControlledNodes = null;
        defenderControlledNodes = null;
        attackerPossiblyControlledNodes = null;

        defenderReward = 0.0;
        attackerReward = new HashMap<>();
        for (FollowerType type : FlipItGameInfo.types){
            attackerReward.put(type, 0.0);
            attackerReward.put(type, FlipItGameInfo.INITIAL_POINTS);
        }

        attackerPossiblyOwnedNodes = new boolean[FlipItGameInfo.graph.getAllNodes().size()];
        defenderOwnedNodes = new boolean[FlipItGameInfo.graph.getAllNodes().size()];

        for(int i = 0; i < defenderOwnedNodes.length; i++){
            defenderOwnedNodes[i] = true;
            attackerPossiblyOwnedNodes[i] = false;
        }

    }

    @Override
    public boolean isPossiblyOwnedByAttacker(Node node){
        return attackerPossiblyOwnedNodes[node.getIntID()];
    }

    @Override
    public GameState copy() {
        return new NoInfoFlipItGameState(this);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        switch (currentPlayerIndex){
            case 0 : return new PerfectRecallISKey(getSequenceForPlayerToMove().hashCode(), getSequenceForPlayerToMove());
            case 1 : return new PerfectRecallISKey(getSequenceForPlayerToMove().hashCode(), getSequenceForPlayerToMove());
        }
        return new PerfectRecallISKey(0, new ArrayListSequenceImpl(history.getSequenceOf(getPlayerToMove())));
    }


    @Override
    protected double[] getEndGameUtilities() {
        double[] utilities = new double[2+FlipItGameInfo.numTypes];
        utilities[0] += defenderReward;
        for (int i = 0;  i < FlipItGameInfo.numTypes; i++){
                utilities[i+1] += attackerReward.get(FlipItGameInfo.types[i]);
        }
        utilities[utilities.length-1] = 0.0;
        if (FlipItGameInfo.ZERO_SUM_APPROX){
            double attackerCosts = 0.0;
            for (Action action : getSequenceFor(FlipItGameInfo.ATTACKER)){
                if (((FlipItAction)action).getControlNode()!= null){
                    attackerCosts += FlipItGameInfo.graph.getControlCost(((FlipItAction)action).getControlNode());
                }
            }
            utilities[0] += attackerCosts;
            for (int i = 0;  i < FlipItGameInfo.numTypes; i++){
                utilities[i+1] = -utilities[0];
            }
        }
        return utilities;
    }

    public double[] evaluate() {
        double[] utilities = new double[2+FlipItGameInfo.numTypes];
        return utilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NoInfoFlipItGameState that = (NoInfoFlipItGameState) o;

        if (Double.compare(that.defenderReward, defenderReward) != 0) return false;
        if (!attackerReward.equals(that.attackerReward)) return false;
        if (!Arrays.equals(defenderOwnedNodes, that.defenderOwnedNodes)) return false;
//        if (hashCode != that.hashCode) return false;
        return Arrays.equals(attackerPossiblyOwnedNodes, that.attackerPossiblyOwnedNodes);

    }

    protected int calculateHashCode(){
        int result = getSequenceFor(FlipItGameInfo.DEFENDER).hashCode();//history.hashCode();
        long temp;
        temp = Double.doubleToLongBits(attackerPoints);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + round;
        result = 31 * result + currentPlayerIndex;
        result = 31 * result + (defenderControlNode != null ? defenderControlNode.hashCode() : 1);
        result = 31 * result + (attackerControlNode != null ? attackerControlNode.hashCode() : 2);
        result = 31 * result + (selectedNodeOwner != null ? selectedNodeOwner.hashCode() : 3);
        temp = Double.doubleToLongBits(defenderReward);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + attackerReward.hashCode();
        result = 31 * result + Arrays.hashCode(defenderOwnedNodes);
        result = 31 * result + Arrays.hashCode(attackerPossiblyOwnedNodes);
//        result = 31 * result + history.hashCode();
        result = 31 * result + getSequenceFor(FlipItGameInfo.ATTACKER).hashCode();
        return result;
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = calculateHashCode();//FlipItGameInfo.hashCodeCounter + 1;
//            FlipItGameInfo.hashCodeCounter = hashCode;
        }
        return hashCode;
    }

    @Override
    public HashSet<Node> getAttackerPossiblyControlledNodes(){
        HashSet<Node> attackerNodes = new HashSet<>(FlipItGameInfo.graph.getAllNodes().size()+1, 1);
        for (Node node : FlipItGameInfo.graph.getAllNodes().values())
            if (attackerPossiblyOwnedNodes[node.getIntID()])
                attackerNodes.add(node);
        return attackerNodes;
    }

    private boolean attackerHasEnoughPointsToControl(FollowerType type){
        return attackerPoints >= FlipItGameInfo.graph.getControlCost(attackerControlNode);
    }

    @Override
    protected boolean attackerHasEnoughPointsToControl(){
        return attackerPoints >= FlipItGameInfo.graph.getControlCost(attackerControlNode);
    }

    @Override
    protected boolean defenderHasEnoughPointsToControl(){
        return defenderReward >= FlipItGameInfo.graph.getControlCost(defenderControlNode);

    }

    @Override
    protected Player getLastOwnerOf(Node node){
        return defenderOwnedNodes[node.getIntID()] ? FlipItGameInfo.DEFENDER : FlipItGameInfo.ATTACKER;
    }

    @Override
    protected void updateAttackerInfo(){
        // recalculate reward for all nodes, but attackNode
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
            if (!defenderOwnedNodes[node.getIntID()]) {
                if (node.equals(attackerControlNode)) continue;
                attackerPoints += FlipItGameInfo.graph.getReward(node);
                for (FollowerType type : FlipItGameInfo.types) {
//                attackerRewards.get(type).put(node,attackerRewards.get(type).get(node) + type.getReward(this,node));
                    attackerReward.put(type, attackerReward.get(type) + type.getReward(this, node));
                }
            }
        }

        // is noop action
        if (attackerControlNode == null)
            return;


        // add control cost no matter of selection result
        for (FollowerType type : FlipItGameInfo.types) {
            if (attackerHasEnoughPointsToControl(type)) {
                attackerReward.put(type, attackerReward.get(type)- FlipItGameInfo.graph.getControlCost(attackerControlNode));
//                attackerRewards.get(type).put(attackerControlNode, attackerRewards.get(type).get(attackerControlNode) - FlipItGameInfo.graph.getControlCost(attackerControlNode));
            }
        }


//        attackerPossiblyControlledNodes.add(attackerControlNode);
        attackerPossiblyOwnedNodes[attackerControlNode.getIntID()] = true;
        if (attackerControlsParent() && attackerWasSelected() && defenderOwnedNodes[attackerControlNode.getIntID()]){//!attackerControlledNodes.contains(attackerControlNode)){
            if (attackerControlNode == null) System.out.println("NULL node");
            defenderOwnedNodes[attackerControlNode.getIntID()] = false;
//            attackerControlledNodes.add(attackerControlNode);
//            defenderControlledNodes.remove(attackerControlNode);

            // add attackNode reward
            for (FollowerType type : FlipItGameInfo.types){
                attackerReward.put(type, attackerReward.get(type)+ type.getReward(this,attackerControlNode));
//                attackerRewards.get(type).put(attackerControlNode,attackerRewards.get(type).get(attackerControlNode) + type.getReward(this,attackerControlNode));
            }
            attackerPoints += FlipItGameInfo.graph.getReward(attackerControlNode);

        }
    }

    @Override
    protected boolean attackerControlsParent(){
        if (FlipItGameInfo.graph.getPublicNodes().contains(attackerControlNode)) return true;
        if (!defenderOwnedNodes[attackerControlNode.getIntID()]) return true;
        for (Edge edge : FlipItGameInfo.graph.getEdgesOf(attackerControlNode)){
            if(edge.getTarget().equals(attackerControlNode) && !defenderOwnedNodes[edge.getSource().getIntID()]) {
                return true;
            }
        }
        return false;
    }

    protected boolean attackerWasSelected(){
        if (selectedNodeOwner == null)
            return attackerHasEnoughPointsToControl();
        else
            return attackerHasEnoughPointsToControl() && selectedNodeOwner.equals(FlipItGameInfo.ATTACKER);
    }

    protected boolean defenderWasSelected(){
        if (selectedNodeOwner == null)
            return defenderHasEnoughPointsToControl();
        else
            return defenderHasEnoughPointsToControl() && selectedNodeOwner.equals(FlipItGameInfo.DEFENDER);
    }

    @Override
    protected void updateDefenderInfo(){

        // is not noop action
        if (defenderControlNode != null && defenderWasSelected()) {
            defenderOwnedNodes[defenderControlNode.getIntID()] = true;
        }

        // recalculate reward for all noded
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
            if (defenderOwnedNodes[node.getIntID()]) {
//            defenderRewards.put(node, defenderRewards.get(node) + FlipItGameInfo.graph.getReward(node));
                defenderReward += FlipItGameInfo.graph.getReward(node);
            }
        }

        // is noop action
        if (defenderControlNode == null)
            return;

        if (defenderHasEnoughPointsToControl()){
//            defenderRewards.put(defenderControlNode, defenderRewards.get(defenderControlNode) - FlipItGameInfo.graph.getControlCost(defenderControlNode));
            defenderReward -= FlipItGameInfo.graph.getControlCost(defenderControlNode);
        }
    }

    @Override
    protected void endRound() {

        updateAttackerInfo();
        updateDefenderInfo();


        round = round + 1;
        currentPlayerIndex = 0;
//        selectedNodeOwner = null;
    }

    @Override
    public void reverseAction(){
        if (getPlayerToMove().equals(FlipItGameInfo.DEFENDER)){
            reverseDefenderAction();
        }
        else{
            reverseAttackerAction();
        }
        super.reverseAction();
    }

    private void reverseAttackerAction() {
        // update node
        if (getSequenceForPlayerToMove().size() == 1)
            attackerControlNode = null;
        else
            attackerControlNode = ((FlipItAction)getSequenceForPlayerToMove().get(getSequenceForPlayerToMove().size() - 2)).getControlNode();

        // reverse endRound
        round = round - 1;
        currentPlayerIndex = 1;

        // reverse infos -- rewards, points, cost(?), update ownership (?)

        for (Node node : attackerControlledNodes){
            attackerPoints -= FlipItGameInfo.graph.getReward(node);
            for (FollowerType type : FlipItGameInfo.types){
                attackerReward.put(type, attackerReward.get(type) - type.getReward(this,node));
            }
        }
        for (Node node : defenderControlledNodes) {
            defenderReward -= FlipItGameInfo.graph.getReward(node);
        }


//        this.selectedNodeOwner = gameState.selectedNodeOwner;
//        OK this.attackerControlNode = gameState.attackerControlNode;
//        OK this.defenderControlNode = gameState.defenderControlNode;
//        OK this.round = gameState.round;
//        OK this.currentPlayerIndex = gameState.currentPlayerIndex;
//        OK this.attackerPoints = gameState.attackerPoints;
//
//        this.defenderOwnedNodes = copyNodes(gameState.defenderOwnedNodes);
//        this.attackerPossiblyOwnedNodes = copyNodes(gameState.attackerPossiblyOwnedNodes);
//
//
//        OK this.defenderReward = gameState.defenderReward;
//        OK this.attackerReward = new HashMap<>(gameState.attackerReward);
    }

    private void reverseDefenderAction() {
//        if ((FlipItAction)(getSequenceForPlayerToMove().getLast()))
        // set as null or set as noop
        if (getSequenceForPlayerToMove().size() == 1)
            defenderControlNode = null;
        else
            defenderControlNode = ((FlipItAction)getSequenceForPlayerToMove().get(getSequenceForPlayerToMove().size() - 2)).getControlNode();
        currentPlayerIndex = 0;
    }

    @Override
    public String toString() {
        return "FlipIt : No Info GS of " + getPlayerToMove() + ": "+getSequenceFor(FlipItGameInfo.DEFENDER) + " / " +getSequenceFor(FlipItGameInfo.ATTACKER);
    }
}
