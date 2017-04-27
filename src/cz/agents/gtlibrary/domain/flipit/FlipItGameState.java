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
public class FlipItGameState extends SimultaneousGameState {

    // real situation ... for computing rewards
    protected HashSet<Node> defenderControlledNodes;
    protected HashSet<Node> attackerControlledNodes;

    // beliefs ... for expander
    protected HashSet<Node> attackerPossiblyControlledNodes;

    // observations ... { [control,reward], .... }
    protected ArrayList<Pair<Boolean,Double>> defenderObservations; // (1) true + reward; or (2) false
    protected ArrayList<Pair<Boolean,Double>> attackerObservations; // (1) true + reward; or (2) false + (2a) 0 (non-controlled parent) / (2b) 1 (unlucky random)

    // rewards
    protected HashMap<Node,Double> defenderRewards;
    protected HashMap<FollowerType,HashMap<Node,Double>> attackerRewards;

    protected int round;
    protected int currentPlayerIndex;

    protected Node defenderControlNode;
    protected Node attackerControlNode;
    protected Player randomSelectedPlayer;

    public FlipItGameState(FlipItGameState gameState) {
        super(gameState);
        this.randomSelectedPlayer = gameState.randomSelectedPlayer;
        this.attackerControlNode = gameState.attackerControlNode;
        this.defenderControlNode = gameState.defenderControlNode;
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;


//        System.out.println(currentPlayerIndex + " " + getPlayerToMove());

        this.defenderControlledNodes = new HashSet<>(gameState.defenderControlledNodes);
        this.attackerControlledNodes = new HashSet<>(gameState.attackerControlledNodes);
        this.attackerPossiblyControlledNodes = new HashSet<>(gameState.attackerPossiblyControlledNodes);

        // observations and rewards
        this.defenderObservations = new ArrayList<>(gameState.defenderObservations);
        this.attackerObservations = new ArrayList<>(gameState.attackerObservations);
        this.defenderRewards = new HashMap<>(gameState.defenderRewards);
        this.attackerRewards = new HashMap<>();
        for (FollowerType type : FlipItGameInfo.types)
            this.attackerRewards.put(type, new HashMap<>(gameState.attackerRewards.get(type)));

    }

    public FlipItGameState() {
        super(FlipItGameInfo.ALL_PLAYERS);
        // init all structures
        defenderControlledNodes = new HashSet<Node>();
        attackerControlledNodes = new HashSet<Node>();
        attackerPossiblyControlledNodes = new HashSet<Node>();
        defenderObservations = new ArrayList<>();
        attackerObservations = new ArrayList<>();
        defenderRewards = new HashMap<>();
        attackerRewards = new HashMap<>();

        // set current player
        currentPlayerIndex = 0;
        //set round
        round = 0;

        // init rewards
        for (FollowerType type : FlipItGameInfo.types){
            attackerRewards.put(type, new HashMap<>());
        }
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
            defenderRewards.put(node,0.0);
            for (FollowerType type : FlipItGameInfo.types){
                attackerRewards.get(type).put(node,0.0);
            }
        }

    }

    @Override
    public GameState copy() {
        return new FlipItGameState(this);
    }

    public HashSet<Node> getAttackerPossiblyControlledNodes(){
        return attackerPossiblyControlledNodes;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return (getPlayerToMove().equals(FlipItGameInfo.NATURE)) ? 0.5 : 0.0;
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return (getPlayerToMove().equals(FlipItGameInfo.NATURE)) ? new Rational(1,2) : Rational.ZERO;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        switch (currentPlayerIndex){
            case 0 : return new PerfectRecallISKey(getSequenceForPlayerToMove().hashCode() + defenderObservations.hashCode(), getSequenceForPlayerToMove());
            case 1 : return new PerfectRecallISKey(getSequenceForPlayerToMove().hashCode() + attackerObservations.hashCode(), getSequenceForPlayerToMove());
        }
        return new PerfectRecallISKey(0, new ArrayListSequenceImpl(history.getSequenceOf(getPlayerToMove())));
    }

    @Override
    public void setDepth(int depth) {
        throw new UnsupportedOperationException("Depth cannot be set.");
    }

    @Override
    protected double[] getEndGameUtilities() {
        double[] utilities = new double[2+FlipItGameInfo.numTypes];
        for (Node node : FlipItGameInfo.graph.getAllNodes().values())
            utilities[0] += defenderRewards.get(node);
        for (int i = 0;  i < FlipItGameInfo.numTypes; i++){
            for (Node node : FlipItGameInfo.graph.getAllNodes().values())
                utilities[i+1] += attackerRewards.get(FlipItGameInfo.types[i]).get(node);
        }
        utilities[utilities.length-1] = 0.0;
        return utilities;
    }

    public double[] evaluate() {
        double[] utilities = new double[2+FlipItGameInfo.numTypes];
        for (Node node : FlipItGameInfo.graph.getAllNodes().values())
            utilities[0] += defenderRewards.get(node);
        for (int i = 0;  i < FlipItGameInfo.numTypes; i++){
            for (Node node : FlipItGameInfo.graph.getAllNodes().values())
                utilities[i+1] += attackerRewards.get(FlipItGameInfo.types[i]).get(node);
        }
        utilities[utilities.length-1] = 0.0;
        return utilities;
    }

    @Override
    public boolean isActualGameEnd() {
        return round == FlipItGameInfo.depth;
    }

    @Override
    public boolean isDepthLimit() {
        return round == FlipItGameInfo.depth;
    }

    @Override
    public Player getPlayerToMove() {
        return FlipItGameInfo.ALL_PLAYERS[currentPlayerIndex];
    }


    @Override
    public boolean isPlayerToMoveNature() {
        return (currentPlayerIndex == 2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlipItGameState that = (FlipItGameState) o;

        if (round != that.round) return false;
        if (currentPlayerIndex != that.currentPlayerIndex) return false;
        if (!defenderControlledNodes.equals(that.defenderControlledNodes)) return false;
        if (!attackerControlledNodes.equals(that.attackerControlledNodes)) return false;
        if (!attackerPossiblyControlledNodes.equals(that.attackerPossiblyControlledNodes)) return false;
        if (!defenderObservations.equals(that.defenderObservations)) return false;
        if (!attackerObservations.equals(that.attackerObservations)) return false;
        if (!defenderRewards.equals(that.defenderRewards)) return false;
        if (!attackerRewards.equals(that.attackerRewards)) return false;
        if (defenderControlNode != null ? !defenderControlNode.equals(that.defenderControlNode) : that.defenderControlNode != null)
            return false;
        if (attackerControlNode != null ? !attackerControlNode.equals(that.attackerControlNode) : that.attackerControlNode != null)
            return false;
        return randomSelectedPlayer != null ? randomSelectedPlayer.equals(that.randomSelectedPlayer) : that.randomSelectedPlayer == null;

    }

    @Override
    public int hashCode() {
        int result = defenderControlledNodes.hashCode();
        result = 31 * result + attackerControlledNodes.hashCode();
        result = 31 * result + attackerPossiblyControlledNodes.hashCode();
        result = 31 * result + defenderObservations.hashCode();
        result = 31 * result + attackerObservations.hashCode();
        result = 31 * result + defenderRewards.hashCode();
        result = 31 * result + attackerRewards.hashCode();
        result = 31 * result + round;
        result = 31 * result + currentPlayerIndex;
        result = 31 * result + (defenderControlNode != null ? defenderControlNode.hashCode() : 23);
        result = 31 * result + (attackerControlNode != null ? attackerControlNode.hashCode() : 29);
        result = 31 * result + (randomSelectedPlayer != null ? randomSelectedPlayer.hashCode() : 37);
        result = 31 * result + history.hashCode();
        return result;
    }

    public void executeAttackerAction(FlipItAction attackerAction) {
        // execute

//        System.out.println("attacker execute");
        attackerControlNode = attackerAction.getControlNode();

        if (attackerControlNode!= null && attackerControlNode.equals(defenderControlNode)){

            if (FlipItGameInfo.RANDOM_TIE) currentPlayerIndex = 2;
            else{
                randomSelectedPlayer = FlipItGameInfo.DEFENDER;
                endRound();
            }
        }
        else{
            randomSelectedPlayer = null;
            endRound();
        }


    }

    public void executeDefenderAction(FlipItAction flipItAction) {
        // proste jen execute
//        System.out.println("defender execute");
        defenderControlNode = flipItAction.getControlNode();
        currentPlayerIndex = 1;

//        System.out.println("ex " + currentPlayerIndex);

    }

    public void executeNatureAction(FlipItAction flipItAction) {
        randomSelectedPlayer = flipItAction.getController();
        endRound();
    }

    private boolean attackerControlsParent(){
        if (FlipItGameInfo.graph.getPublicNodes().contains(attackerControlNode)) return true;
        if (attackerControlledNodes.contains(attackerControlNode)) return true;
        for (Edge edge : FlipItGameInfo.graph.getEdgesOf(attackerControlNode)){
            if(edge.getTarget().equals(attackerControlNode) && attackerControlledNodes.contains(edge.getSource())) {
                return true;
            }
        }
        return false;
    }

    private void updateAttackerInfo(){
        // recalculate reward for all nodes, but attackNode
        for (Node node : attackerControlledNodes){
            if (node.equals(attackerControlNode)) continue;
//            if (node == null) System.out.println("NULL node");
            for (FollowerType type : FlipItGameInfo.types){
                attackerRewards.get(type).put(node,attackerRewards.get(type).get(node) + type.getReward(this,node));
            }
        }

        // is noop action
        if (attackerControlNode == null)
            return;


        // add control cost
        for (FollowerType type : FlipItGameInfo.types){
            attackerRewards.get(type).put(attackerControlNode,attackerRewards.get(type).get(attackerControlNode) - FlipItGameInfo.graph.getControlCost(attackerControlNode));
        }


        if (attackerControlsParent()){
            if (attackerControlNode == null) System.out.println("NULL node");
            attackerControlledNodes.add(attackerControlNode);
            defenderControlledNodes.remove(attackerControlNode);

            // add attackNode reward
            for (FollowerType type : FlipItGameInfo.types){
                attackerRewards.get(type).put(attackerControlNode,attackerRewards.get(type).get(attackerControlNode) + type.getReward(this,attackerControlNode));
            }

            // attacker knows he controls the node
            attackerPossiblyControlledNodes.add(attackerControlNode);
            attackerObservations.add(new Pair<>(true,attackerRewards.get(FlipItGameInfo.types[0]).get(attackerControlNode)));
        }
        else{
            // attacker knows his control failed
            attackerPossiblyControlledNodes.remove(attackerControlNode);
            attackerObservations.add(new Pair<>(false,0.0));
        }
    }

    private void updateDefenderInfo(){

        // is not noop action
        if (defenderControlNode != null) {
            defenderControlledNodes.add(defenderControlNode);
            attackerControlledNodes.remove(defenderControlNode);
        }

        // recalculate reward for all noded
        for (Node node : defenderControlledNodes) {
            defenderRewards.put(node, defenderRewards.get(node) + FlipItGameInfo.graph.getReward(node));
        }

        // is noop action
        if (defenderControlNode == null)
            return;

        defenderRewards.put(defenderControlNode, defenderRewards.get(defenderControlNode) - FlipItGameInfo.graph.getControlCost(defenderControlNode));

        // defender knows he controls the node
        defenderObservations.add(new Pair<>(true,defenderRewards.get(defenderControlNode)));
    }

    private void endRound() {

//        System.out.println("ending round");

        //  1. update control nodes
        //  2. updare beliefs
        //  3. update observations
        // else random node

        // update rewards
        // for all nodes the players control
        // for current control action

        if (randomSelectedPlayer == null){
            updateAttackerInfo();
            updateDefenderInfo();
        }
        else {

            if (randomSelectedPlayer.equals(FlipItGameInfo.DEFENDER)) {
                updateDefenderInfo();

                // attacker knows his control failed
                attackerPossiblyControlledNodes.remove(attackerControlNode);
                attackerObservations.add(new Pair<>(false, 1.0));
            }

            if (randomSelectedPlayer.equals(FlipItGameInfo.ATTACKER)) {
                updateAttackerInfo();

                // defender knows his control failed
                defenderObservations.add(new Pair<>(false, 0.0));
            }
        }

        round = round + 1;
        currentPlayerIndex = 0;
    }

    public Node getAttackerControlNode(){
        return attackerControlNode;
    }

    @Override
    public int getDepth(){ return round; }
}
