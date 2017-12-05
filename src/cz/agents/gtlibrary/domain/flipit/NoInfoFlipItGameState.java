package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Node;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Jakub on 13/03/17.
 */
public class NoInfoFlipItGameState extends NodePointsFlipItGameState {

    double defenderReward;
    double[] attackerReward;
//    boolean[] defenderOwnedNodes;
//    boolean[] attackerPossiblyOwnedNodes;

    public NoInfoFlipItGameState(NoInfoFlipItGameState gameState) {
        this.selectedNodeOwner = gameState.selectedNodeOwner;
        this.attackerControlNode = gameState.attackerControlNode;
        this.defenderControlNode = gameState.defenderControlNode;
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.attackerPoints = gameState.attackerPoints;

//        this.defenderOwnedNodes = copyNodes(gameState.defenderOwnedNodes);
//        this.attackerPossiblyOwnedNodes = copyNodes(gameState.attackerPossiblyOwnedNodes);

        this.defenderControlledNodes = Arrays.copyOf(gameState.defenderControlledNodes, gameState.defenderControlledNodes.length);
        this.attackerPossiblyControlledNodes = Arrays.copyOf(gameState.attackerPossiblyControlledNodes, gameState.attackerPossiblyControlledNodes.length);

        this.history = gameState.getHistory().copy();
        this.natureProbability = gameState.getNatureProbability();
        this.exactNatureProbability = gameState.exactNatureProbability;
        this.players = gameState.getAllPlayers();

        this.defenderReward = gameState.defenderReward;
        this.attackerReward = Arrays.copyOf(gameState.attackerReward, gameState.attackerReward.length);//new HashMap<>(gameState.attackerReward);
//        this.hashCode = -1;
//        this.key = null;

    }

    @Override
    public void transformInto(GameState state) {
        NoInfoFlipItGameState gameState = (NoInfoFlipItGameState)state;
        this.selectedNodeOwner = gameState.selectedNodeOwner;
        this.attackerControlNode = gameState.attackerControlNode;
        this.defenderControlNode = gameState.defenderControlNode;
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.attackerPoints = gameState.attackerPoints;

//        this.defenderOwnedNodes = copyNodes(gameState.defenderOwnedNodes);
//        this.attackerPossiblyOwnedNodes = copyNodes(gameState.attackerPossiblyOwnedNodes);

//        this.defenderControlledNodes = Arrays.copyOf(gameState.defenderControlledNodes, gameState.defenderControlledNodes.length);
        for (int i = 0; i < defenderControlledNodes.length; i++)
            this.defenderControlledNodes[i] = gameState.defenderControlledNodes[i];
//        this.attackerPossiblyControlledNodes = Arrays.copyOf(gameState.attackerPossiblyControlledNodes, gameState.attackerPossiblyControlledNodes.length);
        for (int i = 0; i < attackerPossiblyControlledNodes.length; i++)
            this.attackerPossiblyControlledNodes[i] = gameState.attackerPossiblyControlledNodes[i];

        this.history = gameState.getHistory().copy();
        this.natureProbability = gameState.getNatureProbability();
        this.exactNatureProbability = gameState.exactNatureProbability;
        this.players = gameState.getAllPlayers();

        this.defenderReward = gameState.defenderReward;
        for (int i = 0; i < attackerReward.length; i++)
            this.attackerReward[i] = gameState.attackerReward[i];
//        this.attackerReward = Arrays.copyOf(gameState.attackerReward, gameState.attackerReward.length);//new HashMap<>(gameState.attackerReward);
//        this.hashCode = -1;
//        this.key = null;

    }

    @Override
    protected void init(){}

    public NoInfoFlipItGameState() {
        super();
        super.init();
        hashCode = -1;
        key = null;
        defenderRewards = null;
        attackerRewards = null;
//        attackerObservations = null;
//        defenderObservations = null;
//        attackerControlledNodes = null;
//        defenderControlledNodes = null;
//        attackerPossiblyControlledNodes = null;
        defenderObservationFlags = null;
        attackerObservationFlags = null;

        defenderReward = 0.0;//FlipItGameInfo.INITIAL_POINTS;
        attackerReward = new double[FlipItGameInfo.numTypes];//HashMap<>();
        for (FollowerType type : FlipItGameInfo.types){
            attackerReward[type.getID()] = 0.0;
//            attackerReward[type.getID()] = FlipItGameInfo.INITIAL_POINTS;
        }

//        attackerPossiblyOwnedNodes = new boolean[FlipItGameInfo.graph.getAllNodes().size()];
//        defenderOwnedNodes = new boolean[FlipItGameInfo.graph.getAllNodes().size()];
//
//        for(int i = 0; i < defenderOwnedNodes.length; i++){
//            defenderOwnedNodes[i] = true;
//            attackerPossiblyOwnedNodes[i] = false;
//        }

    }

    @Override
    public boolean isPossiblyOwnedByAttacker(Node node){
        return attackerPossiblyControlledNodes[node.getIntID()];
    }

    @Override
    public GameState copy() {
        return new NoInfoFlipItGameState(this);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (key == null) {
            switch (currentPlayerIndex) {
                case 0:
                    key = new PerfectRecallISKey(getSequenceForPlayerToMove().hashCode(), getSequenceForPlayerToMove());
                    break;
                case 1:
                    key =  new PerfectRecallISKey(getSequenceForPlayerToMove().hashCode(), getSequenceForPlayerToMove());
                    break;
                case 2:
                    key = new PerfectRecallISKey(0, new ArrayListSequenceImpl(history.getSequenceOf(getPlayerToMove())));
            }
        }
        return key;
    }


    @Override
    protected double[] getEndGameUtilities() {
        double[] utilities = new double[2+FlipItGameInfo.numTypes];
        utilities[0] += defenderReward;
        for (int i = 0;  i < FlipItGameInfo.numTypes; i++){
                utilities[i+1] += attackerReward[i];
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
        // Random utilities test
//        HighQualityRandom random = new HighQualityRandom(FlipItGameInfo.seed);
//        for (int i = 0; i < utilities.length; i++) utilities[i] = random.nextDouble();
        return utilities;
    }

    public double[] evaluate() {
//        double[] utilities = new double[2+FlipItGameInfo.numTypes];
//        return utilities;
        return getEndGameUtilities();
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        if (!super.equals(o)) return false;
//
////        if (o.hashCode() != this.hashCode()) return false;
//
//        NoInfoFlipItGameState that = (NoInfoFlipItGameState) o;
//
//        if(true) return history.equals(that.history);
//
////        if (!history.getSequenceOf(FlipItGameInfo.DEFENDER).equals(that.history.getSequenceOf(FlipItGameInfo.DEFENDER))) return false;
////        if (!history.getSequenceOf(FlipItGameInfo.ATTACKER).equals(that.history.getSequenceOf(FlipItGameInfo.ATTACKER))) return false;
//
//
//        if (Double.compare(that.defenderReward, defenderReward) != 0) return false;
//        if (!attackerReward.equals(that.attackerReward)) return false;
////        if (!Arrays.equals(defenderOwnedNodes, that.defenderOwnedNodes)) return false;
////        if (hashCode != that.hashCode) return false;
//
//
////        if (Double.compare(that.defenderObservedReward, defenderObservedReward) != 0) return false;
////        if (Double.compare(that.attackerObservedReward, attackerObservedReward) != 0) return false;
//        if (Double.compare(that.attackerPoints, attackerPoints) != 0) return false;
//        if (round != that.round) return false;
//        if (currentPlayerIndex != that.currentPlayerIndex) return false;
////        if (!Arrays.equals(defenderControlledNodes, that.defenderControlledNodes)) return false;
////        if (!Arrays.equals(attackerPossiblyControlledNodes, that.attackerPossiblyControlledNodes)) return false;
////        if (!Arrays.equals(defenderObservationFlags, that.defenderObservationFlags)) return false;
////        if (!Arrays.equals(attackerObservationFlags, that.attackerObservationFlags)) return false;
////        if (!Arrays.equals(defenderRewards, that.defenderRewards)) return false;
////        if (!Arrays.deepEquals(attackerRewards, that.attackerRewards)) return false;
//        if (defenderControlNode != null ? !defenderControlNode.equals(that.defenderControlNode) : that.defenderControlNode != null)
//            return false;
//        if (attackerControlNode != null ? !attackerControlNode.equals(that.attackerControlNode) : that.attackerControlNode != null)
//            return false;
//
//        if (!history.getSequenceOf(FlipItGameInfo.DEFENDER).equals(that.history.getSequenceOf(FlipItGameInfo.DEFENDER))) return false;
//        if (!history.getSequenceOf(FlipItGameInfo.ATTACKER).equals(that.history.getSequenceOf(FlipItGameInfo.ATTACKER))) return false;
//
//        if (selectedNodeOwner != null ? !selectedNodeOwner.equals(that.selectedNodeOwner) : that.selectedNodeOwner != null)
//            return false;
//
//        return Arrays.equals(attackerPossiblyControlledNodes, that.attackerPossiblyControlledNodes);
//
//    }

//    protected int calculateHashCode(){
//        if (true) return new HashCodeBuilder().append(getSequenceFor(FlipItGameInfo.DEFENDER))
//                .append(getSequenceFor(FlipItGameInfo.ATTACKER)).append(history).toHashCode();
//        int result = history.hashCode();//getSequenceFor(FlipItGameInfo.DEFENDER).hashCode();//history.hashCode();
//        long temp;
//        temp = Double.doubleToLongBits(attackerPoints);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        result = 31 * result + round;
//        result = 31 * result + currentPlayerIndex;
//        result = 31 * result + (defenderControlNode != null ? defenderControlNode.hashCode() : 1);
//        result = 31 * result + (attackerControlNode != null ? attackerControlNode.hashCode() : 2);
//        result = 31 * result + (selectedNodeOwner != null ? selectedNodeOwner.hashCode() : 3);
//        temp = Double.doubleToLongBits(defenderReward);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        result = 31 * result + attackerReward.hashCode();
//        result = 31 * result + Arrays.hashCode(defenderControlledNodes);
//        result = 31 * result + Arrays.hashCode(attackerPossiblyControlledNodes);
//        result = 31 * result + history.hashCode();
//        result = 31 * result + getSequenceFor(FlipItGameInfo.ATTACKER).hashCode();
//        return result;
////        return new HashCodeBuilder().append(history).append(getSequenceFor(FlipItGameInfo.DEFENDER)).append(getSequenceFor(FlipItGameInfo.ATTACKER)).toHashCode();
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
//        if (!super.equals(o)) return false;

        NoInfoFlipItGameState that = (NoInfoFlipItGameState) o;

        if ((that.history == null || that.history.getLength()==0) && (history==null || history.getLength()==0)) return true;
        if (true) return history.equals(that.history);

        if (Double.compare(that.attackerPoints, attackerPoints) != 0) return false;
        if (round != that.round) return false;
        if (currentPlayerIndex != that.currentPlayerIndex) return false;
        if (!Arrays.equals(defenderControlledNodes, that.defenderControlledNodes)) return false;
        if (!Arrays.equals(attackerPossiblyControlledNodes, that.attackerPossiblyControlledNodes)) return false;
        if (defenderControlNode != null ? !defenderControlNode.equals(that.defenderControlNode) : that.defenderControlNode != null)
            return false;
        if (attackerControlNode != null ? !attackerControlNode.equals(that.attackerControlNode) : that.attackerControlNode != null)
            return false;

        if (!history.getSequenceOf(FlipItGameInfo.DEFENDER).equals(that.history.getSequenceOf(FlipItGameInfo.DEFENDER))) return false;
        if (!history.getSequenceOf(FlipItGameInfo.ATTACKER).equals(that.history.getSequenceOf(FlipItGameInfo.ATTACKER))) return false;

        if (selectedNodeOwner != null ? !selectedNodeOwner.equals(that.selectedNodeOwner) : that.selectedNodeOwner != null)
            return false;

        if (Double.compare(that.defenderReward, defenderReward) != 0) return false;
        return Arrays.equals(attackerReward,that.attackerReward);

    }

    @Override
    public int calculateHashCode() {
//        if (true) return ((history == null) ? 0 : history.hashCode());
        int result = ((history == null) ? 0 : history.hashCode());//super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(defenderReward);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(attackerReward);
        result = 31 * result + history.hashCode();
        return result;
    }


    @Override
    public int hashCode() {
//        if (true) return calculateHashCode();
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
            if (attackerPossiblyControlledNodes[node.getIntID()])
                attackerNodes.add(node);
        return attackerNodes;
    }

//    private boolean attackerHasEnoughPointsToControl(FollowerType type){
//        return attackerPoints >= FlipItGameInfo.graph.getControlCost(attackerControlNode);
//    }

    @Override
    protected boolean attackerHasEnoughPointsToControl(){
        if (FlipItGameInfo.ATTACKER_CAN_ALWAYS_ATTACK) return true;
        return attackerPoints >= FlipItGameInfo.graph.getControlCost(attackerControlNode);
    }

    @Override
    protected boolean defenderHasEnoughPointsToControl(){
        if (FlipItGameInfo.DEFENDER_CAN_ALWAYS_ATTACK) return true;
        return defenderReward >= FlipItGameInfo.graph.getControlCost(defenderControlNode);

    }

    @Override
    protected Player getLastOwnerOf(Node node){
        return defenderControlledNodes[node.getIntID()] ? FlipItGameInfo.DEFENDER : FlipItGameInfo.ATTACKER;
    }

    @Override
    protected void updateAttackerInfo(){
        // recalculate reward for all nodes, but attackNode
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
            if (!defenderControlledNodes[node.getIntID()]) {
                if (node.equals(attackerControlNode)) continue;
                attackerPoints += FlipItGameInfo.graph.getReward(node);
                for (FollowerType type : FlipItGameInfo.types) {
//                attackerRewards.get(type).put(node,attackerRewards.get(type).get(node) + type.getReward(this,node));
                    attackerReward[type.getID()] += type.getReward(this, node);
                }
            }
        }

        // is noop action
        if (attackerControlNode == null)
            return;


        // add control cost no matter of selection result
        for (FollowerType type : FlipItGameInfo.types) {
            if (attackerHasEnoughPointsToControl()) {
                attackerReward[type.getID()] -= FlipItGameInfo.graph.getControlCost(attackerControlNode);
//                attackerRewards.get(type).put(attackerControlNode, attackerRewards.get(type).get(attackerControlNode) - FlipItGameInfo.graph.getControlCost(attackerControlNode));
            }
        }


//        attackerPossiblyControlledNodes.add(attackerControlNode);
        attackerPossiblyControlledNodes[attackerControlNode.getIntID()] = true;
        if (attackerControlsParent() && attackerWasSelected() && defenderControlledNodes[attackerControlNode.getIntID()]){//!attackerControlledNodes.contains(attackerControlNode)){
            if (attackerControlNode == null) System.out.println("NULL node");
            defenderControlledNodes[attackerControlNode.getIntID()] = false;
//            attackerControlledNodes.add(attackerControlNode);
//            defenderControlledNodes.remove(attackerControlNode);

            // add attackNode reward
            for (FollowerType type : FlipItGameInfo.types){
                attackerReward[type.getID()] += type.getReward(this,attackerControlNode);
//                attackerRewards.get(type).put(attackerControlNode,attackerRewards.get(type).get(attackerControlNode) + type.getReward(this,attackerControlNode));
            }
            attackerPoints += FlipItGameInfo.graph.getReward(attackerControlNode);

        }
    }

    @Override
    protected boolean attackerControlsParent(){
        if (FlipItGameInfo.graph.getPublicNodes().contains(attackerControlNode)) return true;
        if (!defenderControlledNodes[attackerControlNode.getIntID()]) return true;
        for (Edge edge : FlipItGameInfo.graph.getEdgesOf(attackerControlNode)){
            if(edge.getTarget().equals(attackerControlNode) && !defenderControlledNodes[edge.getSource().getIntID()]) {
                return true;
            }
        }
        return false;
    }

//    protected boolean attackerWasSelected(){
//        if (selectedNodeOwner == null)
//            return attackerHasEnoughPointsToControl();
//        else
//            return attackerHasEnoughPointsToControl() && selectedNodeOwner.equals(FlipItGameInfo.ATTACKER);
//    }
//
//    protected boolean defenderWasSelected(){
//        if (selectedNodeOwner == null)
//            return defenderHasEnoughPointsToControl();
//        else
//            return defenderHasEnoughPointsToControl() && selectedNodeOwner.equals(FlipItGameInfo.DEFENDER);
//    }

    @Override
    protected void updateDefenderInfo(){

        // is not noop action
        if (defenderControlNode != null && defenderWasSelected()) {
            defenderControlledNodes[defenderControlNode.getIntID()] = true;
        }

        // recalculate reward for all noded
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
            if (defenderControlledNodes[node.getIntID()]) {
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

//        for (Node node : attackerControlledNodes){
//            attackerPoints -= FlipItGameInfo.graph.getReward(node);
//            for (FollowerType type : FlipItGameInfo.types){
//                attackerReward.put(type, attackerReward.get(type) - type.getReward(this,node));
//            }
//        }
//        for (Node node : defenderControlledNodes) {
//            defenderReward -= FlipItGameInfo.graph.getReward(node);
//        }


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
