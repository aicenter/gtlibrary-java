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
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Node;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Jakub on 13/03/17.
 */
public class NodePointsFlipItGameState extends SimultaneousGameState {

    // real situation ... for computing rewards
//    protected HashSet<Node> defenderControlledNodes;
//    protected HashSet<Node> attackerControlledNodes;
    protected boolean[] defenderControlledNodes;

    // beliefs ... for expander
//    protected HashSet<Node> attackerPossiblyControlledNodes;
    protected boolean[] attackerPossiblyControlledNodes;

    // observations ... { [control,reward], .... }
//    protected ArrayList<Pair<Boolean,Double>> defenderObservations; // (1) true + reward; or (2) false + (2a) 0 (not selected) / (2b) 1 (not enough points)
//    protected ArrayList<Pair<Boolean,Double>> attackerObservations; // (1) true + reward; or (2) false + (2a) 0 (uncontrolled parent) / (2b) 1 (not selected) / (2c) 2 (not enough points)

    protected double defenderObservedReward;
    protected boolean[] defenderObservationFlags;
    protected double attackerObservedReward;
    protected boolean[] attackerObservationFlags;


    // rewards
//    protected HashMap<Node,Double> defenderRewards;
//    protected HashMap<FollowerType,HashMap<Node,Double>> attackerRewards;
    protected double[] defenderRewards;
    protected double[][] attackerRewards;
    protected double attackerPoints;

    protected int round;
    protected int currentPlayerIndex;

    protected Node defenderControlNode;
    protected Node attackerControlNode;
    protected Player selectedNodeOwner;

    protected int hashCode;
    protected ISKey key;

    public NodePointsFlipItGameState(NodePointsFlipItGameState gameState) {
        super(gameState);
        this.selectedNodeOwner = gameState.selectedNodeOwner;
        this.attackerControlNode = gameState.attackerControlNode;
        this.defenderControlNode = gameState.defenderControlNode;
        this.round = gameState.round;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.attackerPoints = gameState.attackerPoints;

//        this.defenderControlledNodes = new HashSet<>(gameState.defenderControlledNodes);
//        this.attackerControlledNodes = new HashSet<>(gameState.attackerControlledNodes);
//        this.attackerPossiblyControlledNodes = new HashSet<>(gameState.attackerPossiblyControlledNodes);
        this.defenderControlledNodes = Arrays.copyOf(gameState.defenderControlledNodes, gameState.defenderControlledNodes.length);
        this.attackerPossiblyControlledNodes = Arrays.copyOf(gameState.attackerPossiblyControlledNodes, gameState.attackerPossiblyControlledNodes.length);

        // observations and rewards
//        this.defenderObservations = new ArrayList<>(gameState.defenderObservations); // no need to copy ?
//        this.attackerObservations = new ArrayList<>(gameState.attackerObservations); // no need to copy ?
//        this.defenderRewards = new HashMap<>(gameState.defenderRewards);
//        this.attackerRewards = new HashMap<>();
//        for (FollowerType type : FlipItGameInfo.types)
//            this.attackerRewards.put(type, new HashMap<>(gameState.attackerRewards.get(type)));
        this.defenderRewards = Arrays.copyOf(gameState.defenderRewards,gameState.defenderRewards.length);//copyRewards(gameState.defenderRewards);
        if (FlipItGameInfo.numTypes == 1)
            this.attackerRewards = new double[][]{Arrays.copyOf(gameState.attackerRewards[0], gameState.attackerRewards[0].length)};//copyAttackerRewards(gameState.attackerRewards);
        else this.attackerRewards = copyAttackerRewards(gameState.attackerRewards);
        this.defenderObservationFlags = Arrays.copyOf(gameState.defenderObservationFlags, gameState.defenderObservationFlags.length);
        this.attackerObservationFlags = Arrays.copyOf(gameState.attackerObservationFlags, gameState.attackerObservationFlags.length);
        this.defenderObservedReward = gameState.defenderObservedReward;
        this.attackerObservedReward = gameState.attackerObservedReward;

//        hashCode = -1;

//        System.out.println("FULL INFO COPY");

    }

    public double getUpperBoundForUtility(){
        double upperBound = 0.0;
        for (Node node : FlipItGameInfo.graph.getAllNodes().values())
            upperBound += FlipItGameInfo.graph.getReward(node);
        upperBound = (FlipItGameInfo.depth - round + 1) * upperBound;
//        if (currentPlayerIndex == 0){
        for (double utility : defenderRewards)
            upperBound += utility;
//        }
//        else{
//            for (double utility : attackerRewards[0])
//                upperBound += utility;
//        }
        return upperBound;
    }

    public double getUpperBoundForUtilityFor(int playerIndex){
//        double[] bounds = FlipItGameInfo.determineMinMaxBoundsFor(this);
//        if (true) return playerIndex==0 ? bounds[1] : -bounds[0];

        if (!FlipItGameInfo.CALCULATE_UTILITY_BOUNDS) {
            return FlipItGameInfo.MAX_UTILITY;
        }
//        if (!FlipItGameInfo.maxUtility.containsKey(getISKeyForPlayerToMove()) || !FlipItGameInfo.maxUtility.get(getISKeyForPlayerToMove()).containsKey(this) &&)
        if (!FlipItGameInfo.maxUtility.containsKey(getISKeyForPlayerToMove()) || !FlipItGameInfo.maxUtility.get(getISKeyForPlayerToMove()).containsKey(this)){
            System.out.println("chybka"); //FlipItGameInfo.calculateMinMaxBoundsFor(this);
            System.out.println(history);
            double max = 0.0;
            for (Node node : FlipItGameInfo.graph.getAllNodes().values())
                max += FlipItGameInfo.graph.getReward(node);
            max = FlipItGameInfo.depth * max;
            return max;
        }
        else{
//            System.out.println("mam ho");
        }
//        return FlipItGameInfo.maxUtility.get(this);
        return (playerIndex==0) ? FlipItGameInfo.maxUtility.get(getISKeyForPlayerToMove()).get(this) : -FlipItGameInfo.minUtility.get(getISKeyForPlayerToMove()).get(this);
    }

    public double getLowerBoundForUtilityFor(int playerIndex){
        if (!FlipItGameInfo.CALCULATE_UTILITY_BOUNDS) {
            return -FlipItGameInfo.MAX_UTILITY;
        }
//        return -FlipItGameInfo.maxUtility.get(this);
        return (playerIndex==0) ? FlipItGameInfo.minUtility.get(getISKeyForPlayerToMove()).get(this) : -FlipItGameInfo.maxUtility.get(getISKeyForPlayerToMove()).get(this);
    }

    protected void init(){
        // init all structures
//        defenderControlledNodes = new HashSet<Node>(FlipItGameInfo.graph.getAllNodes().values());
//        attackerControlledNodes = new HashSet<Node>();
//        attackerPossiblyControlledNodes = new HashSet<Node>();
        defenderControlledNodes = new boolean[FlipItGameInfo.graph.getAllNodes().size()];
        Arrays.fill(defenderControlledNodes, true);
        attackerPossiblyControlledNodes = new boolean[FlipItGameInfo.graph.getAllNodes().size()];
//        defenderObservations = new ArrayList<>();
//        attackerObservations = new ArrayList<>();
//        defenderRewards = new HashMap<>();
//        attackerRewards = new HashMap<>();
        defenderRewards = new double[FlipItGameInfo.graph.getAllNodes().size()];//HashMap<>();
        attackerRewards = new double[FlipItGameInfo.numTypes][FlipItGameInfo.graph.getAllNodes().size()];//HashMap<>();
        attackerPoints = FlipItGameInfo.INITIAL_POINTS;

        attackerObservationFlags = new boolean[3];
        defenderObservationFlags = new boolean[2];

        // set current player
        currentPlayerIndex = 0;
        //set round
        round = 0;

        // init rewards
//        for (FollowerType type : FlipItGameInfo.types){
////            attackerRewards.put(type, new HashMap<>());
//        }
//        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
//            defenderRewards.put(node,0.0);
//            for (FollowerType type : FlipItGameInfo.types){
////                attackerRewards.get(type).put(node,0.0);
////                if (node.getIntID() == 0) attackerRewards.get(type).put(node,FlipItGameInfo.INITIAL_POINTS);
//            }
//        }
//        for (FollowerType type : FlipItGameInfo.types){
//            attackerRewards[type.getID()][0] = FlipItGameInfo.INITIAL_POINTS;
//        }
//        defenderRewards[0] = FlipItGameInfo.INITIAL_POINTS;

        hashCode = -1;
//
//        double[][] aa = new double[][]{{2.2}, {3.4, 2.5}, {5.1}};
//        double[][] bb = new double[][]{{2.2}, {3.4, 2.5}, {4.1}};
//        bb[2][0] = 5.1;
//
//        System.out.println(Arrays.deepHashCode(aa) + " ; " + Arrays.deepHashCode(bb) + " ; " + Arrays.deepEquals(aa,bb));

//        System.out.println("FULL INFO INIT");
    }

    protected double[] copyRewards(double[] nodes){
        double[] newNodes = new double[nodes.length];
        for (int i = 0; i < nodes.length; i++)
            newNodes[i] = nodes[i];
        return newNodes;
    }
    protected double[][] copyAttackerRewards(double[][] nodes){
//        System.out.println(nodes.length);
        double[][] newNodes = new double[nodes.length][nodes[0].length];
        for (int i = 0; i < nodes.length; i++) {
            newNodes[i] = Arrays.copyOf(nodes[i],nodes[i].length);//copyRewards(nodes[i]);
        }
        return newNodes;
    }

    public NodePointsFlipItGameState() {
        super(FlipItGameInfo.ALL_PLAYERS);
        init();
    }

    @Override
    public GameState copy() {
        return new NodePointsFlipItGameState(this);
    }

//    public HashSet<Node> getAttackerPossiblyControlledNodes(){
//        return attackerPossiblyControlledNodes;
//    }


    public HashSet<Node> getAttackerPossiblyControlledNodes(){
    HashSet<Node> attackerNodes = new HashSet<>(FlipItGameInfo.graph.getAllNodes().size()+1, 1);
    for (Node node : FlipItGameInfo.graph.getAllNodes().values())
        if (attackerPossiblyControlledNodes[node.getIntID()])
            attackerNodes.add(node);
    return attackerNodes;
    }


    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return (getPlayerToMove().equals(FlipItGameInfo.NATURE)) ? 0.5 : 0.0;
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return (getPlayerToMove().equals(FlipItGameInfo.NATURE)) ? new Rational(1,2) : Rational.ZERO;
    }

    protected int calculateDefenderISKeyHash(){
        return new HashCodeBuilder().append(defenderObservedReward).append(defenderObservationFlags).append(getHistory().getSequenceOf(FlipItGameInfo.DEFENDER)).toHashCode();
//        int result = getSequenceForPlayerToMove().hashCode();
//        long temp = Double.doubleToLongBits(defenderObservedReward);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        result = 31 * result + Arrays.hashCode(defenderObservationFlags);
//        result = 31 * result + (getSequenceForPlayerToMove().size() > 0 ? getSequenceForPlayerToMove().getLast().hashCode() : 0);
//        return  result;
    }

    protected int calculateAttackerISKeyHash(){
        return new HashCodeBuilder().append(attackerObservedReward).append(attackerObservationFlags).append(getHistory().getSequenceOf(FlipItGameInfo.ATTACKER)).toHashCode();
//        int result = getSequenceForPlayerToMove().hashCode();
//        long temp = Double.doubleToLongBits(attackerObservedReward);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        result = 31 * result + Arrays.hashCode(attackerObservationFlags);
//        return  result;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (key == null) {
            switch (currentPlayerIndex) {
                case 0:
                    key = new PerfectRecallISKey(calculateDefenderISKeyHash(), new ArrayListSequenceImpl(history.getSequenceOf(FlipItGameInfo.DEFENDER)));
                    break;
                case 1:
                    key = new PerfectRecallISKey(calculateAttackerISKeyHash(), new ArrayListSequenceImpl(history.getSequenceOf(FlipItGameInfo.ATTACKER)));
                    break;
                case 2:
                    key = new PerfectRecallISKey(0, new ArrayListSequenceImpl(history.getSequenceOf(getPlayerToMove())));
            }
        }
        return key;
    }

    @Override
    public void setDepth(int depth) {
//        this.depth = depth + round;
        throw new UnsupportedOperationException("Depth cannot be set.");
    }

    @Override
    protected double[] getEndGameUtilities() {
        double[] utilities = new double[2+FlipItGameInfo.numTypes];
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()) {
//            utilities[0] += defenderRewards.get(node);
            utilities[0] += defenderRewards[node.getIntID()];//.get(node);
        }
        for (int i = 0;  i < FlipItGameInfo.numTypes; i++){
            for (Node node : FlipItGameInfo.graph.getAllNodes().values()) {
//                utilities[i + 1] += attackerRewards.get(FlipItGameInfo.types[i]).get(node);
                utilities[i + 1] += attackerRewards[i][node.getIntID()];//.get(FlipItGameInfo.types[i]).get(node);
            }
        }
        utilities[utilities.length-1] = 0.0;
        // TODO : tohle neni uplne presny, utocnik obcas nemuze utocit, protoze nema dost bodu, takze se mu cost nezapocte
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
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
//            utilities[0] += defenderRewards.get(node);
            utilities[0] += defenderRewards[node.getIntID()];//.get(node);
        }
        for (int i = 0;  i < FlipItGameInfo.numTypes; i++){
            for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
//                utilities[i + 1] += attackerRewards.get(FlipItGameInfo.types[i]).get(node);
                utilities[i + 1] += attackerRewards[i][node.getIntID()];//.get(FlipItGameInfo.types[i]).get(node);
            }
        }
        utilities[utilities.length-1] = 0.0;
        return utilities;
    }

    @Override
    public boolean isActualGameEnd() {
        if (currentPlayerIndex == 0 && FlipItGameInfo.RANDOM_TERMINATION){
            HighQualityRandom random = new HighQualityRandom(FlipItGameInfo.seed);
            if (random.nextDouble() < FlipItGameInfo.RANDOM_TERMINATION_PROBABILITY)
                return true;
        }
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

        NodePointsFlipItGameState that = (NodePointsFlipItGameState) o;

        if (Double.compare(that.defenderObservedReward, defenderObservedReward) != 0) return false;
        if (Double.compare(that.attackerObservedReward, attackerObservedReward) != 0) return false;
        if (Double.compare(that.attackerPoints, attackerPoints) != 0) return false;
        if (round != that.round) return false;
        if (currentPlayerIndex != that.currentPlayerIndex) return false;
//        if (defenderControlledNodes != null ? !defenderControlledNodes.equals(that.defenderControlledNodes) : that.defenderControlledNodes != null)
//            return false;
//        if (attackerControlledNodes != null ? !attackerControlledNodes.equals(that.attackerControlledNodes) : that.attackerControlledNodes != null)
//            return false;
//        if (attackerPossiblyControlledNodes != null ? !attackerPossiblyControlledNodes.equals(that.attackerPossiblyControlledNodes) : that.attackerPossiblyControlledNodes != null)
//            return false;
        if (!Arrays.equals(defenderControlledNodes, that.defenderControlledNodes)) return false;
        if (!Arrays.equals(attackerPossiblyControlledNodes, that.attackerPossiblyControlledNodes)) return false;
        if (!Arrays.equals(defenderObservationFlags, that.defenderObservationFlags)) return false;
        if (!Arrays.equals(attackerObservationFlags, that.attackerObservationFlags)) return false;
        if (!Arrays.equals(defenderRewards, that.defenderRewards)) return false;
        if (!Arrays.deepEquals(attackerRewards, that.attackerRewards)) return false;
        if (defenderControlNode != null ? !defenderControlNode.equals(that.defenderControlNode) : that.defenderControlNode != null)
            return false;
        if (attackerControlNode != null ? !attackerControlNode.equals(that.attackerControlNode) : that.attackerControlNode != null)
            return false;

        if (!history.getSequenceOf(FlipItGameInfo.DEFENDER).equals(that.history.getSequenceOf(FlipItGameInfo.DEFENDER))) return false;
        if (!history.getSequenceOf(FlipItGameInfo.ATTACKER).equals(that.history.getSequenceOf(FlipItGameInfo.ATTACKER))) return false;

        return selectedNodeOwner != null ? selectedNodeOwner.equals(that.selectedNodeOwner) : that.selectedNodeOwner == null;

    }

    @Override
    public int hashCode() {
        if (hashCode == -1)
        {
            hashCode = calculateHashCode();//FlipItGameInfo.hashCodeCounter + 1;
//            FlipItGameInfo.hashCodeCounter = hashCode;
        }
        return hashCode;
    }

    protected int calculateHashCode(){
        if (true) return ((history == null) ? 0 : history.hashCode());
        int result;
        long temp;
        result = Arrays.hashCode(defenderControlledNodes);
        //defenderControlledNodes != null ? defenderControlledNodes.hashCode() : 0;
//        result = 31 * result + (attackerControlledNodes != null ? attackerControlledNodes.hashCode() : 0);
        // != null ? attackerPossiblyControlledNodes.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(attackerPossiblyControlledNodes);
        temp = Double.doubleToLongBits(defenderObservedReward);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(defenderObservationFlags);
        temp = Double.doubleToLongBits(attackerObservedReward);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(attackerObservationFlags);
        result = 31 * result + Arrays.hashCode(defenderRewards);
        result = 31 * result + Arrays.deepHashCode(attackerRewards);
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
        return attackerPossiblyControlledNodes[node.getIntID()];
    }

    protected void clearCache() {
        hashCode = -1;
        key = null;
    }

    public void executeAttackerAction(FlipItAction attackerAction) {
        clearCache();

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

    public boolean[] getDefenderControlledNodes(){
        return  defenderControlledNodes;
    }

    protected Player getLastOwnerOf(Node node){
        if (defenderControlledNodes[node.getIntID()])
            return FlipItGameInfo.DEFENDER;
//        for (Node defenderNode : defenderControlledNodes)
//            if (defenderNode.equals(node))
//                return FlipItGameInfo.DEFENDER;
        return FlipItGameInfo.ATTACKER;
    }

    public void executeDefenderAction(FlipItAction flipItAction) {
        clearCache();
        // proste jen execute
//        System.out.println("defender execute");
        defenderControlNode = flipItAction.getControlNode();
        currentPlayerIndex = 1;

    }

    public void executeNatureAction(FlipItAction flipItAction) {
        clearCache();
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
        if (FlipItGameInfo.DEFENDER_CAN_ALWAYS_ATTACK) return true;
        Double points = 0.0;
        for (Double reward : defenderRewards)//.values())
            points += reward;
        return points >= FlipItGameInfo.graph.getControlCost(defenderControlNode);

    }

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

    // TODO : update it according to No INFO, add selectedPlayer check, multiple attackNode reward calculation etc.
    protected void updateAttackerInfo(){
        // recalculate reward for all nodes, but attackNode
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
            if(defenderControlledNodes[node.getIntID()]) continue;
            if (node.equals(attackerControlNode)) continue;
//            if (node == null) System.out.println("NULL node");
            attackerPoints += FlipItGameInfo.graph.getReward(node);
            for (FollowerType type : FlipItGameInfo.types){
//                attackerRewards.get(type).put(node,attackerRewards.get(type).get(node) + type.getReward(this,node));
                attackerRewards[type.getID()][node.getIntID()] = attackerRewards[type.getID()][node.getIntID()] + type.getReward(this,node);
            }
        }

        // is noop action
        if (attackerControlNode == null)
            return;


        // add control cost
        for (FollowerType type : FlipItGameInfo.types) {
            if (attackerHasEnoughPointsToControl(type)) {
//                attackerRewards.get(type).put(attackerControlNode, attackerRewards.get(type).get(attackerControlNode) - FlipItGameInfo.graph.getControlCost(attackerControlNode));
                attackerRewards[type.getID()][attackerControlNode.getIntID()] -= FlipItGameInfo.graph.getControlCost(attackerControlNode);
            }
        }


        if (attackerControlsParent() && attackerWasSelected()){
            if (attackerControlNode == null) System.out.println("NULL node");
            defenderControlledNodes[attackerControlNode.getIntID()] = false;
//            attackerControlledNodes.add(attackerControlNode);
//            defenderControlledNodes.remove(attackerControlNode);

            // add attackNode reward
            for (FollowerType type : FlipItGameInfo.types){
//                attackerRewards.get(type).put(attackerControlNode,attackerRewards.get(type).get(attackerControlNode) + type.getReward(this,attackerControlNode));
                attackerRewards[type.getID()][attackerControlNode.getIntID()] += type.getReward(this,attackerControlNode);
            }
            attackerPoints += FlipItGameInfo.graph.getReward(attackerControlNode);

            // attacker knows he controls the node
//            if (FlipItGameInfo.INFORMED_ATTACKERS) {
//                attackerObservations.add(new Pair<>(true, attackerRewards[0][attackerControlNode.getIntID()]));//attackerRewards.get(FlipItGameInfo.types[0]).get(attackerControlNode)));
////                attackerObservations.add(new Pair<>(true, attackerRewards.get(FlipItGameInfo.types[0]).get(attackerControlNode)));
//            }
            attackerPossiblyControlledNodes[attackerControlNode.getIntID()] = true;
        }
        else{
            if (!attackerHasEnoughPointsToControl()) {
                // not enough points
//                attackerObservations.add(new Pair<>(false, 2.0));
                attackerObservationFlags[2] = true;
            }
            if (FlipItGameInfo.INFORMED_ATTACKERS) {
                attackerPossiblyControlledNodes[attackerControlNode.getIntID()] = false;
                // attacker knows his control failed
                // unsatisfied parent
                if (!attackerControlsParent())
                    attackerObservationFlags[0] = true;
//                    attackerObservations.add(new Pair<>(false, 0.0));
                // not selected
                if (FlipItGameInfo.DEFENDER.equals(selectedNodeOwner))
                    attackerObservationFlags[1] = true;
//                    attackerObservations.add(new Pair<>(false, 1.0));
            }
            else{
                // if attacker should not be informed ...
                attackerPossiblyControlledNodes[attackerControlNode.getIntID()] = true;
            }
        }

        attackerObservedReward = attackerRewards[0][attackerControlNode.getIntID()];
    }

    protected void updateDefenderInfo(){

        // is not noop action
        if (defenderControlNode != null && defenderWasSelected()) {
            defenderControlledNodes[defenderControlNode.getIntID()] = true;
//            defenderControlledNodes.add(defenderControlNode);
//            attackerControlledNodes.remove(defenderControlNode);
        }

        // recalculate reward for all noded
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()) {
            if (!defenderControlledNodes[node.getIntID()]) continue;
//            defenderRewards.put(node, defenderRewards.get(node) + FlipItGameInfo.graph.getReward(node));
            defenderRewards[node.getIntID()] = defenderRewards[node.getIntID()] + FlipItGameInfo.graph.getReward(node);
        }

        // is noop action
        if (defenderControlNode == null)
            return;

        if (defenderHasEnoughPointsToControl()){
//            defenderRewards.put(defenderControlNode, defenderRewards.get(defenderControlNode) - FlipItGameInfo.graph.getControlCost(defenderControlNode));
            defenderRewards[defenderControlNode.getIntID()] = defenderRewards[defenderControlNode.getIntID()] - FlipItGameInfo.graph.getControlCost(defenderControlNode);
        }

        // defender knows he controls the node
//        if (defenderWasSelected()){//FlipItGameInfo.INFORMED_ATTACKERS) {
//            defenderObservations.add(new Pair<>(true, defenderRewards[defenderControlNode.getIntID()]));//defenderRewards.get(defenderControlNode)));
////            defenderObservations.add(new Pair<>(true, defenderRewards.get(defenderControlNode)));
//        }
        else{
            // not selected
            if (!defenderHasEnoughPointsToControl() && FlipItGameInfo.INFORMED_ATTACKERS)
                defenderObservationFlags[1] = true;
//                defenderObservations.add(new Pair<>(false, 1.0));
            // not enough points
            if (FlipItGameInfo.ATTACKER.equals(selectedNodeOwner))
                defenderObservationFlags[0] = true;
//                defenderObservations.add(new Pair<>(false, 0.0));

        }
        defenderObservedReward = defenderRewards[defenderControlNode.getIntID()];
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

    protected void endRound() {

//        this.defenderObservations = new ArrayList<>();
//        this.attackerObservations = new ArrayList<>();
        attackerObservedReward = 0.0;
        defenderObservedReward = 0.0;
        defenderObservationFlags = new boolean[defenderObservationFlags.length];
        attackerObservationFlags = new boolean[attackerObservationFlags.length];

//        System.out.println("ending round");

        //  1. update control nodes
        //  2. updare beliefs
        //  3. update observations
        // else random node

        // update rewards
        // for all nodes the players control
        // for current control action

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
//
//                // attacker knows his control failed
//                if (FlipItGameInfo.INFORMED_ATTACKERS) {
//                    attackerPossiblyControlledNodes.remove(attackerControlNode);
//                    attackerObservations.add(new Pair<>(false, 1.0));
//                }
//            }
//
//            if (selectedNodeOwner.equals(FlipItGameInfo.ATTACKER)) {
//                updateAttackerInfo();
//
//                // defender knows his control failed
//                if (FlipItGameInfo.INFORMED_ATTACKERS) {
//                    defenderObservations.add(new Pair<>(false, 0.0));
//                }
//            }
//        }

        round = round + 1;
        currentPlayerIndex = 0;

//        if (!FlipItGameInfo.INFORMED_ATTACKERS){
//            assert attackerObservations.size() == 0;
//            assert defenderObservations.size() == 0;
//        }

    }

    public Node getAttackerControlNode(){
        return attackerControlNode;
    }

    @Override
    public int getDepth(){
        int depth = getSequenceFor(FlipItGameInfo.ATTACKER).size();
        depth += getSequenceFor(FlipItGameInfo.DEFENDER).size();
        if (FlipItGameInfo.ALL_PLAYERS[FlipItGameInfo.ALL_PLAYERS.length-1].getName() == "Nature")
            depth += getSequenceFor(FlipItGameInfo.NATURE).size() + 1;
        return depth;
    }

    @Override
    public String toString() {
        return "D = [" + getLowerBoundForUtilityFor(0) + ", " + getUpperBoundForUtilityFor(0) +"]; A = [" + + getLowerBoundForUtilityFor(1) + ", " + getUpperBoundForUtilityFor(1) +"]";
    }

}
