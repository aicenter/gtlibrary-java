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
    protected double attackerPoints;

    protected int round;
    protected int currentPlayerIndex;

    protected Node defenderControlNode;
    protected Node attackerControlNode;
    protected Player selectedNodeOwner;

    public FlipItGameState(FlipItGameState gameState) {
        super(gameState);
        this.selectedNodeOwner = gameState.selectedNodeOwner;
        this.attackerControlNode = gameState.attackerControlNode;
        this.defenderControlNode = gameState.defenderControlNode;
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.attackerPoints = gameState.attackerPoints;

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

//        System.out.println("FULL INFO COPY");

    }

    protected void init(){
        // init all structures
        defenderControlledNodes = new HashSet<Node>(FlipItGameInfo.graph.getAllNodes().values());
        attackerControlledNodes = new HashSet<Node>();
        attackerPossiblyControlledNodes = new HashSet<Node>();
        defenderObservations = new ArrayList<>();
        attackerObservations = new ArrayList<>();
        defenderRewards = new HashMap<>();
        attackerRewards = new HashMap<>();
        attackerPoints = FlipItGameInfo.INITIAL_POINTS;

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
                if (node.getIntID() == 0) attackerRewards.get(type).put(node,FlipItGameInfo.INITIAL_POINTS);
            }
        }

//        System.out.println("FULL INFO INIT");

    }

    public FlipItGameState() {
        super(FlipItGameInfo.ALL_PLAYERS);
        init();
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
        if (FlipItGameInfo.ZERO_SUM_APPROX) return utilities;
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

        if (Double.compare(that.attackerPoints, attackerPoints) != 0) return false;
        if (round != that.round) return false;
        if (currentPlayerIndex != that.currentPlayerIndex) return false;
        if (defenderControlledNodes != null ? !defenderControlledNodes.equals(that.defenderControlledNodes) : that.defenderControlledNodes != null)
            return false;
        if (attackerControlledNodes != null ? !attackerControlledNodes.equals(that.attackerControlledNodes) : that.attackerControlledNodes != null)
            return false;
        if (attackerPossiblyControlledNodes != null ? !attackerPossiblyControlledNodes.equals(that.attackerPossiblyControlledNodes) : that.attackerPossiblyControlledNodes != null)
            return false;
        if (defenderObservations != null ? !defenderObservations.equals(that.defenderObservations) : that.defenderObservations != null)
            return false;
        if (attackerObservations != null ? !attackerObservations.equals(that.attackerObservations) : that.attackerObservations != null)
            return false;
        if (defenderRewards != null ? !defenderRewards.equals(that.defenderRewards) : that.defenderRewards != null)
            return false;
        if (attackerRewards != null ? !attackerRewards.equals(that.attackerRewards) : that.attackerRewards != null)
            return false;
        if (defenderControlNode != null ? !defenderControlNode.equals(that.defenderControlNode) : that.defenderControlNode != null)
            return false;
        if (attackerControlNode != null ? !attackerControlNode.equals(that.attackerControlNode) : that.attackerControlNode != null)
            return false;
        if (!history.equals(that.history)) return false;
        return selectedNodeOwner != null ? selectedNodeOwner.equals(that.selectedNodeOwner) : that.selectedNodeOwner == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = defenderControlledNodes != null ? defenderControlledNodes.hashCode() : 0;
        result = 31 * result + (attackerControlledNodes != null ? attackerControlledNodes.hashCode() : 0);
        result = 31 * result + (attackerPossiblyControlledNodes != null ? attackerPossiblyControlledNodes.hashCode() : 0);
        result = 31 * result + (defenderObservations != null ? defenderObservations.hashCode() : 0);
        result = 31 * result + (attackerObservations != null ? attackerObservations.hashCode() : 0);
        result = 31 * result + (defenderRewards != null ? defenderRewards.hashCode() : 0);
        result = 31 * result + (attackerRewards != null ? attackerRewards.hashCode() : 0);
        temp = Double.doubleToLongBits(attackerPoints);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + round;
        result = 31 * result + currentPlayerIndex;
        result = 31 * result + (defenderControlNode != null ? defenderControlNode.hashCode() : 0);
        result = 31 * result + (attackerControlNode != null ? attackerControlNode.hashCode() : 0);
        result = 31 * result + (selectedNodeOwner != null ? selectedNodeOwner.hashCode() : 0);
        result = 31 * result + history.hashCode();
        return result;
    }

    public boolean isPossiblyOwnedByAttacker(Node node){
        return attackerPossiblyControlledNodes.contains(node);
    }

    public void executeAttackerAction(FlipItAction attackerAction) {
        // execute

//        System.out.println("attacker execute");
        attackerControlNode = attackerAction.getControlNode();

        if (attackerControlNode!= null && attackerControlNode.equals(defenderControlNode)){

            if (FlipItGameInfo.RANDOM_TIE) currentPlayerIndex = 2;
            else{
                if (FlipItGameInfo.PREDETERMINED_RANDOM_TIE_WINNER) {
                    selectedNodeOwner = FlipItGameInfo.RANDOM_TIE_WINNER;
                }
                else{
                    // select owner according their ability to control
                    if (attackerHasEnoughPointsToControl() && defenderHasEnoughPointsToControl())
                        selectedNodeOwner = getLastOwnerOf(attackerControlNode);
                    else{
                        // if none can control
                        selectedNodeOwner = getLastOwnerOf(attackerControlNode);
                        if (attackerHasEnoughPointsToControl())
                            selectedNodeOwner = FlipItGameInfo.ATTACKER;
                        if (defenderHasEnoughPointsToControl())
                            selectedNodeOwner = FlipItGameInfo.DEFENDER;
                    }
                }
                endRound();
            }
        }
        else{
            selectedNodeOwner = null;
            endRound();
        }
    }

    protected Player getLastOwnerOf(Node node){
        for (Node defenderNode : defenderControlledNodes)
            if (defenderNode.equals(node))
                return FlipItGameInfo.DEFENDER;
        return FlipItGameInfo.ATTACKER;
    }

    public void executeDefenderAction(FlipItAction flipItAction) {
        // proste jen execute
//        System.out.println("defender execute");
        defenderControlNode = flipItAction.getControlNode();
        currentPlayerIndex = 1;

//        System.out.println("ex " + currentPlayerIndex);

    }

    public void executeNatureAction(FlipItAction flipItAction) {
        selectedNodeOwner = flipItAction.getController();
        endRound();
    }

    private boolean attackerHasEnoughPointsToControl(FollowerType type){
        return attackerPoints >= FlipItGameInfo.graph.getControlCost(attackerControlNode);
    }

    protected boolean attackerHasEnoughPointsToControl(){
        return attackerPoints >= FlipItGameInfo.graph.getControlCost(attackerControlNode);
    }

    protected boolean defenderHasEnoughPointsToControl(){
        Double points = 0.0;
        for (Double reward : defenderRewards.values())
            points += reward;
        return points >= FlipItGameInfo.graph.getControlCost(defenderControlNode);

    }

    protected boolean attackerControlsParent(){
        if (FlipItGameInfo.graph.getPublicNodes().contains(attackerControlNode)) return true;
        if (attackerControlledNodes.contains(attackerControlNode)) return true;
        for (Edge edge : FlipItGameInfo.graph.getEdgesOf(attackerControlNode)){
            if(edge.getTarget().equals(attackerControlNode) && attackerControlledNodes.contains(edge.getSource())) {
                return true;
            }
        }
        return false;
    }

    // TODO : update it according to No INFO, add selectedPlayer check, multiple attackNode reward calculation etc.
    protected void updateAttackerInfo(){
        // recalculate reward for all nodes, but attackNode
        for (Node node : attackerControlledNodes){
            if (node.equals(attackerControlNode)) continue;
//            if (node == null) System.out.println("NULL node");
            attackerPoints += FlipItGameInfo.graph.getReward(node);
            for (FollowerType type : FlipItGameInfo.types){
                attackerRewards.get(type).put(node,attackerRewards.get(type).get(node) + type.getReward(this,node));
            }
        }

        // is noop action
        if (attackerControlNode == null)
            return;


        // add control cost
        for (FollowerType type : FlipItGameInfo.types) {
            if (attackerHasEnoughPointsToControl(type)) {
                attackerRewards.get(type).put(attackerControlNode, attackerRewards.get(type).get(attackerControlNode) - FlipItGameInfo.graph.getControlCost(attackerControlNode));
            }
        }


        if (attackerControlsParent() && attackerHasEnoughPointsToControl()){
            if (attackerControlNode == null) System.out.println("NULL node");
            attackerControlledNodes.add(attackerControlNode);
            defenderControlledNodes.remove(attackerControlNode);

            // add attackNode reward
            for (FollowerType type : FlipItGameInfo.types){
                attackerRewards.get(type).put(attackerControlNode,attackerRewards.get(type).get(attackerControlNode) + type.getReward(this,attackerControlNode));
            }
            attackerPoints += FlipItGameInfo.graph.getReward(attackerControlNode);

            // attacker knows he controls the node
            if (FlipItGameInfo.INFORMED_PLAYERS) {
                attackerObservations.add(new Pair<>(true, attackerRewards.get(FlipItGameInfo.types[0]).get(attackerControlNode)));
            }
            attackerPossiblyControlledNodes.add(attackerControlNode);
        }
        else{
            // attacker knows his control failed
            if (FlipItGameInfo.INFORMED_PLAYERS) {
                attackerPossiblyControlledNodes.remove(attackerControlNode);
                //TODO: pick different constants to signify fail by uncontrolled parent or not enough points (or both ?)
                attackerObservations.add(new Pair<>(false, 0.0));
            }
            else{
                attackerPossiblyControlledNodes.add(attackerControlNode);
            }
        }
    }

    protected void updateDefenderInfo(){

        // is not noop action
        if (defenderControlNode != null && defenderHasEnoughPointsToControl()) {
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

        if (defenderHasEnoughPointsToControl()){
            defenderRewards.put(defenderControlNode, defenderRewards.get(defenderControlNode) - FlipItGameInfo.graph.getControlCost(defenderControlNode));
        }

        // defender knows he controls the node
        if (FlipItGameInfo.INFORMED_PLAYERS) {
            defenderObservations.add(new Pair<>(true, defenderRewards.get(defenderControlNode)));
        }
    }

    protected void endRound() {

//        System.out.println("ending round");

        //  1. update control nodes
        //  2. updare beliefs
        //  3. update observations
        // else random node

        // update rewards
        // for all nodes the players control
        // for current control action

        if (selectedNodeOwner == null){
            updateAttackerInfo();
            updateDefenderInfo();
        }
        else {

            if (selectedNodeOwner.equals(FlipItGameInfo.DEFENDER)) {
                updateDefenderInfo();

                // attacker knows his control failed
                if (FlipItGameInfo.INFORMED_PLAYERS) {
                    attackerPossiblyControlledNodes.remove(attackerControlNode);
                    attackerObservations.add(new Pair<>(false, 1.0));
                }
            }

            if (selectedNodeOwner.equals(FlipItGameInfo.ATTACKER)) {
                updateAttackerInfo();

                // defender knows his control failed
                if (FlipItGameInfo.INFORMED_PLAYERS) {
                    defenderObservations.add(new Pair<>(false, 0.0));
                }
            }
        }

        round = round + 1;
        currentPlayerIndex = 0;

        if (!FlipItGameInfo.INFORMED_PLAYERS){
            assert attackerObservations.size() == 0;
            assert defenderObservations.size() == 0;
        }

    }

    public Node getAttackerControlNode(){
        return attackerControlNode;
    }

    @Override
    public int getDepth(){ return round; }
}
