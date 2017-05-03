package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Jakub on 13/03/17.
 */
public class NoInfoFlipItGameState extends FlipItGameState {

    double defenderReward;
    HashMap<FollowerType, Double> attackerReward;
    boolean[] defenderNodes;
    boolean[] attackerPossibleNodes;

    public NoInfoFlipItGameState(NoInfoFlipItGameState gameState) {
//        super(gameState);
        this.selectedNodeOwner = gameState.selectedNodeOwner;
        this.attackerControlNode = gameState.attackerControlNode;
        this.defenderControlNode = gameState.defenderControlNode;
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.attackerPoints = gameState.attackerPoints;

//        this.defenderControlledNodes = new HashSet<>(gameState.defenderControlledNodes);
//        this.attackerControlledNodes = new HashSet<>(gameState.attackerControlledNodes);
//        this.attackerPossiblyControlledNodes = new HashSet<>(gameState.attackerPossiblyControlledNodes);
        this.defenderNodes = copyNodes(gameState.defenderNodes);
        this.attackerPossibleNodes = copyNodes(gameState.attackerPossibleNodes);

        this.history = gameState.getHistory().copy();
        this.natureProbability = gameState.getNatureProbability();
        this.exactNatureProbability = gameState.exactNatureProbability;
        this.players = gameState.getAllPlayers();

        this.defenderReward = gameState.defenderReward;
        this.attackerReward = new HashMap<>(gameState.attackerReward);

    }

    protected boolean[] copyNodes(boolean[] nodes){
        boolean[] newNodes = new boolean[nodes.length];
        for (int i = 0; i < nodes.length; i++)
            newNodes[i] = nodes[i];
        return newNodes;
    }

    public NoInfoFlipItGameState() {
        super();
        defenderRewards = null;
        attackerRewards = null;
        attackerObservations = null;
        defenderObservations = null;

        defenderReward = 0.0;
        attackerReward = new HashMap<>();
        for (FollowerType type : FlipItGameInfo.types){
            attackerReward.put(type, 0.0);
        }

        attackerPossibleNodes = new boolean[FlipItGameInfo.graph.getAllNodes().size()];
        defenderNodes = new boolean[FlipItGameInfo.graph.getAllNodes().size()];

        for(int i = 0; i < defenderNodes.length; i++){
            defenderNodes[i] = true;
            attackerPossibleNodes[i] = false;
        }

//        System.out.println("NO INFO INIT");
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

        NoInfoFlipItGameState that = (NoInfoFlipItGameState) o;

        if (attackerPoints != that.attackerPoints) return false;
        if (defenderReward != that.defenderReward) return false;
        if (round != that.round) return false;
        if (currentPlayerIndex != that.currentPlayerIndex) return false;
        if (!defenderNodes.equals(that.defenderNodes)) return false;
        if (!attackerPossibleNodes.equals(that.attackerPossibleNodes)) return false;
//        if (!defenderControlledNodes.equals(that.defenderControlledNodes)) return false;
//        if (!attackerControlledNodes.equals(that.attackerControlledNodes)) return false;
//        if (!attackerPossiblyControlledNodes.equals(that.attackerPossiblyControlledNodes)) return false;
        if (defenderControlNode != null ? !defenderControlNode.equals(that.defenderControlNode) : that.defenderControlNode != null)
            return false;
        if (attackerControlNode != null ? !attackerControlNode.equals(that.attackerControlNode) : that.attackerControlNode != null)
            return false;
        if (!history.equals(that.history)) return false;
        if (!attackerReward.equals(that.attackerReward)) return false;
        return selectedNodeOwner != null ? selectedNodeOwner.equals(that.selectedNodeOwner) : that.selectedNodeOwner == null;

    }

    @Override
    public int hashCode() {
        int result = 0;//defenderControlledNodes.hashCode();
//        result = 31 * result + attackerControlledNodes.hashCode();
//        result = 31 * result + attackerPossiblyControlledNodes.hashCode();
        result = 31 * result + round;
        result = 31 * result + (int)(attackerPoints*100);
        result = 31 * result + (int)(defenderReward*100);
        result = 31 * result + attackerReward.hashCode();
        result = 31 * result + currentPlayerIndex;
        result = 31 * result + defenderNodes.hashCode();
        result = 31 * result + attackerPossibleNodes.hashCode();
        result = 31 * result + (defenderControlNode != null ? defenderControlNode.hashCode() : 23);
        result = 31 * result + (attackerControlNode != null ? attackerControlNode.hashCode() : 29);
        result = 31 * result + (selectedNodeOwner != null ? selectedNodeOwner.hashCode() : 37);
        result = 31 * result + history.hashCode();
        return result;
    }

    @Override
    public HashSet<Node> getAttackerPossiblyControlledNodes(){
        HashSet<Node> attackerNodes = new HashSet<>();
        for (Node node : FlipItGameInfo.graph.getAllNodes().values())
            if (attackerPossibleNodes[node.getIntID()])
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
        return defenderNodes[node.getIntID()] ? FlipItGameInfo.DEFENDER : FlipItGameInfo.ATTACKER;
    }

    @Override
    protected void updateAttackerInfo(){
        // recalculate reward for all nodes, but attackNode
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
            if (!defenderNodes[node.getIntID()]) {
                if (node.equals(attackerControlNode)) continue;
//            if (node == null) System.out.println("NULL node");
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
        attackerPossibleNodes[attackerControlNode.getIntID()] = true;
        if (attackerControlsParent() && attackerWasSelected() && defenderNodes[attackerControlNode.getIntID()]){//!attackerControlledNodes.contains(attackerControlNode)){
            if (attackerControlNode == null) System.out.println("NULL node");
            defenderNodes[attackerControlNode.getIntID()] = false;
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
            defenderNodes[defenderControlNode.getIntID()] = true;
//            defenderControlledNodes.add(defenderControlNode);
//            attackerControlledNodes.remove(defenderControlNode);
        }

        // recalculate reward for all noded
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
            if (defenderNodes[node.getIntID()]) {
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

    // TODO tady je bug !! v pripade vybrani viteze se musi delat update obou !! a na zacatku dalsiho kola musi byt seletedNodeOwner opet null !!

    @Override
    protected void endRound() {

        updateAttackerInfo();
        updateDefenderInfo();

//        if (selectedNodeOwner == null){
//            updateAttackerInfo();
//            updateDefenderInfo();
//        }
//        else {
//
//            if (selectedNodeOwner.equals(FlipItGameInfo.DEFENDER)) {
//                updateDefenderInfo();
//            }
//
//            if (selectedNodeOwner.equals(FlipItGameInfo.ATTACKER)) {
//                updateAttackerInfo();
//            }
//        }

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


}
