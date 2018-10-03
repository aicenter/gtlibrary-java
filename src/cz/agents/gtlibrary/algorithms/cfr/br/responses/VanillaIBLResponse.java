package cz.agents.gtlibrary.algorithms.cfr.br.responses;

import cz.agents.gtlibrary.algorithms.cfr.br.CFRBRAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Jakub Cerny on 05/06/2018.
 *
 *
 * Assumes a simultaneous turntaking game with 2ps and utility accumulation
 */
public class VanillaIBLResponse extends BestResponse {

    protected int iteration;

    protected double decay;
    protected double sigma;
    protected double gamma;
    protected double tau;

    protected Player opponent;

    protected Random rnd;

    protected boolean USE_SOFTMAX_IBL = true;
    protected boolean USE_RANDOM_STARTUP = false;
    protected int RANDOM_STARTUP_DEPTH = 2;

    protected final boolean VERBOSE = !true;

    public VanillaIBLResponse(Player respondingPlayer, Node root, double decay, double noise) {
        super(respondingPlayer, root);
        iteration = 0;
        rnd = new Random();
        this.decay = decay;
        this.sigma = noise;
        this.tau = sigma * Math.sqrt(2);
    }
    public VanillaIBLResponse(Player respondingPlayer, Node root, double decay, double noise, Player opponent) {
        super(respondingPlayer, root);
        iteration = 0;
        rnd = new Random();
        this.decay = decay;
        this.sigma = noise;
        this.tau = sigma * Math.sqrt(2);
        this.opponent = opponent;
    }

    public VanillaIBLResponse(Player respondingPlayer, int respondingPlayerIndex, Node root, double decay, double noise) {
        super(respondingPlayer, respondingPlayerIndex, root);
        iteration = 0;
        rnd = new Random();
        this.decay = decay;
        this.sigma = noise;
        this.tau = sigma * Math.sqrt(2);
    }

    @Override
    public double computeBR(Node root) {

        if (iteration > 0) return -2.0;

        Queue<Node> queue = new LinkedList<>();
        queue.add(root);

        while(!queue.isEmpty()){
            Node node = queue.poll();
            if( (node instanceof InnerNode) ) {
                InnerNode in = (InnerNode) node;
                MCTSInformationSet is = in.getInformationSet();
                CFRBRAlgorithmData data = (CFRBRAlgorithmData) is.getAlgorithmData();


                for (Action ai : in.getActions()) {
                    queue.add(in.getChildFor(ai));
                }
                if (in.getInformationSet().getPlayer().getId() == respondingPlayer.getId() &&
                        data.getIterationForPlayerIdx(respondingPlayerIndex) < iteration) {

                    if (USE_RANDOM_STARTUP && in.getDepth() / 2.0 < RANDOM_STARTUP_DEPTH){
//                        data.resetDataOfPlayer(respondingPlayerIndex, 0.0);
                        for(int i = 0; i < in.getActions().size(); i++) {
                            data.setDataOfPlayerIdx(respondingPlayerIndex, i, 1.0/in.getActions().size());
                        }
                        data.setIterationForPlayerIdx(respondingPlayerIndex, iteration);
                        continue;
                    }

                    // calculate predicted action with highest blended value
                    Action maxAction = null;
                    double maxBV = Double.NEGATIVE_INFINITY;
                    int currentDepth = is.getPlayersHistory().size() + 1;
                    double updatedValue;

                    // identify current situation
                    Object currentSituation = ((AbstractActionProvider)in.getActions().get(0)).getSituationAbstraction();

                    HashMap<Object, HashMap<Double, Double>> blendedValues = new HashMap<>();

                    // calculate observation decay
                    for(Action a : is.getPlayersHistory()){
                        // check if the same situation
                        if(((AbstractActionProvider)a).getSituationAbstraction().equals(currentSituation)){

                            // check what was the outcome
                            if(!blendedValues.containsKey(((AbstractActionProvider) a).getActionAbstraction())){
                                blendedValues.put(((AbstractActionProvider) a).getActionAbstraction(), new HashMap<>());
                            }
                            // TODO : find the outcome (function of state : NOT action !)
                            double outcome = ((ImmediateActionOutcomeProvider)in.getGameState()).getImmediateRewardForAction(a);
                            HashMap<Double, Double> blendedValue = blendedValues.get(((AbstractActionProvider) a).getActionAbstraction());
                            if(!blendedValue.containsKey(outcome)) blendedValue.put(outcome, 0.0);

                            updatedValue = blendedValue.get(outcome) +
                                    Math.pow(currentDepth - ((PerfectRecallISKey)a.getInformationSet().getISKey()).getSequence().size() - 1, decay);
//                            if (updatedValue < 0.0){ System.out.println("A : " + updatedValue); }
                            blendedValue.put(outcome, updatedValue);


                        }
                    }

                    // PREPOPULATION
                    // all payoffs of all actions are observed at the beginning of the game
                    for (Action ai : in.getActions()) {
                        Object abstraction = ((AbstractActionProvider)ai).getActionAbstraction();
                        if(!blendedValues.containsKey(abstraction)){
                            blendedValues.put(abstraction, new HashMap<>());
                        }
                        HashMap<Double, Double> blendedValue = blendedValues.get(abstraction);
                        for(double outcome : ((AbstractActionProvider)ai).getAllPossibleOutcomes()) {
                            if (!blendedValue.containsKey(outcome)) blendedValue.put(outcome, 0.0);
                            updatedValue = blendedValue.get(outcome) + Math.pow(currentDepth, decay);
//                            if (updatedValue < 0.0){ System.out.println("B : " + updatedValue); }
                            blendedValue.put(outcome, updatedValue);
                        }
                        // add also unreachable maximum outcome
                        double outcome = ((AbstractActionProvider)ai).getMaximumActionUtility();
                        if (!blendedValue.containsKey(outcome)) blendedValue.put(outcome, 0.0);
                        updatedValue = blendedValue.get(outcome) + Math.pow(currentDepth, decay);
//                        if (updatedValue < 0.0){ System.out.println("B : " + updatedValue); }
                        blendedValue.put(outcome, updatedValue);
                    }

                    double gamma = Math.min(0.95, rnd.nextDouble() + 0.05);

                    // calculate the activation
                    for(Object action : blendedValues.keySet()) {
                        for (Double outcome : blendedValues.get(action).keySet()) {
                            updatedValue = Math.log(blendedValues.get(action).get(outcome));
//                            if (updatedValue < 0.0){ System.out.println("C : " + updatedValue); }
                            updatedValue += sigma * Math.log((1 - gamma) / gamma);
//                            if (updatedValue < 0.0){ System.out.println("D : " + updatedValue); }
                            blendedValues.get(action).put(outcome, updatedValue);
                        }
                    }


                    // calculate bvs, find the highest
                    double bvsum = 0.0;
                    double minBV = Double.POSITIVE_INFINITY;
                    double[] bvs = new double[in.getActions().size()];
                    int actionIdx = 0;
                    for (Action ai : in.getActions()) {
                        // get abstracted action
                        Object abstraction = ((AbstractActionProvider)ai).getActionAbstraction();
                        if(blendedValues.containsKey(abstraction)){
                            double sum = 0.0;
                            for(Double d : blendedValues.get(abstraction).values()){
                                sum += Math.exp(d/tau);
                            }
//                            double blendedValue = 0.0;
                            for(Double outcome : blendedValues.get(abstraction).keySet()){
                                bvs[actionIdx] += outcome * Math.exp(blendedValues.get(abstraction).get(outcome)/tau);
//                                if(bvs[actionIdx] < 0.0){ System.out.println("E : " + bvs[actionIdx]);}
                            }
                            bvs[actionIdx] /= sum;
                            bvsum += bvs[actionIdx];

                            if (bvs[actionIdx] < minBV){
                                minBV = bvs[actionIdx];
                            }

                            if(bvs[actionIdx] > maxBV){
                                maxAction = ai;
                                maxBV = bvs[actionIdx];
                            }
                        }
                        actionIdx++;
                    }

                    // set as strategy
                    // store strategy to data
//                    System.out.println(maxAction + " : " + maxBV);
                    if(!USE_SOFTMAX_IBL) {
                        // reset strategy
                        data.resetDataOfPlayer(respondingPlayerIndex, 0.0);
                        data.setDataOfPlayerIdx(respondingPlayerIndex, maxAction, 1.0);
                    }
                    else{
                        if (VERBOSE){
                            System.out.println(node.getGameState().getSequenceFor(respondingPlayer));
                            System.out.println(node.getGameState().getSequenceFor(opponent));
                        }
                        if (minBV < 0.0){
                            bvsum += -minBV*bvs.length;
                        }
                        double strategy = 0.0;
                        for(int i = 0; i < bvs.length; i++) {
                            strategy = bvsum > 0.0 ? (bvs[i] - (minBV < 0.0 ? minBV : 0.0))/bvsum : 1.0/bvs.length;
                            data.setDataOfPlayerIdx(respondingPlayerIndex, i, strategy);
                            if(VERBOSE){
                                System.out.println("\t" + ((InnerNode) node).getActions().get(i) + "\t : " + strategy);
                            }
                        }
                        if(VERBOSE){
                            System.out.println("**************************************************************");
                        }
                    }

                    // increase iteration
                    data.setIterationForPlayerIdx(respondingPlayerIndex, iteration);

                }
            }
        }

        iteration++;
        return -1.0; // undefined
    }


}
