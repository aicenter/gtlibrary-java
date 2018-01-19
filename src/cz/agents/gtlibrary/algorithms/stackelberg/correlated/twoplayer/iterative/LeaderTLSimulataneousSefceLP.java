package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.LeaderGenerationConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.CompleteTwoPlayerSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.iinodes.TLExpander;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.iinodes.TLGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;

/**
 * Created by Jakub Cerny on 07/11/2017.
 */
public class LeaderTLSimulataneousSefceLP implements Solver {

    protected long overallConstraintGenerationTime;
    protected long overallConstraintLPSolvingTime;

    protected double result;

    protected Player leader;
    protected GameInfo gameInfo;

    HashMap<GameState, ArrayList<GameState>> temporaryLeaves;

    protected final double EPS = 0.0000000001;
    protected double restrictedGameRatio;

    protected final boolean EXPORT_TREE = false;
    protected final boolean USE_PARETO_LEAVES = true;

    public LeaderTLSimulataneousSefceLP(Player leader, GameInfo gameInfo){
        overallConstraintGenerationTime = 0;
        overallConstraintLPSolvingTime = 0;
        result = 0.0;
        this.leader = leader;
        this.gameInfo = gameInfo;
        temporaryLeaves = new HashMap<>();
    }

    protected void findInitialRG(LeaderGenerationConfig config, TLExpander expander, StackelbergConfig fullConfig){
        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(config.getRootState());
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
            config.addStateToSequenceForm(currentState);
            if (currentState.isGameEnd()) {
                final double[] utilities = currentState.getUtilities();
                Double[] u = new Double[utilities.length];

                for (Player p : currentState.getAllPlayers()){
                    if(utilities.length > p.getId())
                        u[p.getId()] = utilities[p.getId()] * currentState.getNatureProbability()*gameInfo.getUtilityStabilizer();
                }
                config.setUtility(currentState, u);
                continue;
            }
            if (!currentState.equals(config.getRootState()) && currentState.getPlayerToMove().equals(leader)){
                temporaryLeaves.put(currentState, new ArrayList<>());
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }

        for (GameState leaf : fullConfig.getAllLeafs())
            for (GameState state : temporaryLeaves.keySet())
                if (state.getSequenceFor(leader).isPrefixOf(leaf.getSequenceFor(leader)) &&
                        state.getSequenceFor(gameInfo.getOpponent(leader)).isPrefixOf(leaf.getSequenceFor(gameInfo.getOpponent(leader)))) {
                    TLGameState tlLeaf = new TLGameState(gameInfo.getAllPlayers(), leaf, leader);
//                    History history = state.getHistory().copy();
//                    history.addActionOf(expander.getActions(state).get(temporaryLeaves.get(state).size()), leader);
//                    tlLeaf.setHistory(history);
                    temporaryLeaves.get(state).add(tlLeaf);
//                    history.addActionOf(expander.getActions(state).get(temporaryLeaves.get(state).size()), leader);
//                    tlLeaf.setHistory(history);
                    break;
                }
//        System.out.println(temporaryLeaves);
        for (GameState state : temporaryLeaves.keySet())
                for (Action action : expander.getActions(state)) {
//                    System.out.println("ST1: " + state);
                    GameState tlLeaf = state.performAction(action);
//                    System.out.println("ST2: " + state);
//                    TLGameState tlLeaf = temporaryLeaves.get(state).get(((TLAction)action).getIndex());
//                    History history = state.getHistory().copy();
//                    history.addActionOf(action, leader);
//                    tlLeaf.setHistory(history);

                    config.addStateToSequenceForm(tlLeaf);
                    final double[] utilities = tlLeaf.getUtilities();
//                    System.out.println(state + ", " + tlLeaf + ", " + utilities);
                    Double[] u = new Double[utilities.length];

                    for (Player p : tlLeaf.getAllPlayers()){
                        if(utilities.length > p.getId())
                            u[p.getId()] = utilities[p.getId()] * tlLeaf.getNatureProbability()*gameInfo.getUtilityStabilizer();
                    }
                    config.setUtility(tlLeaf, u);
                }
    }

    protected boolean generateNewRG(Map<Sequence, Double> strategy, LeaderGenerationConfig config, TLExpander expander, StackelbergConfig fullConfig){
        boolean isLarger = false;

//        for (Sequence seq : config.getSequencesFor(leader))
//            System.out.println(seq + " #= " + seq.hashCode());
//        System.out.println();

        // check reachable TL;
        HashSet<Sequence> sequencesToBeRemoved = new HashSet<>();
        HashSet<GameState> tlStates = new HashSet<>(temporaryLeaves.keySet());
        for (GameState state : tlStates){
            if (strategy.containsKey(state.getSequenceFor(leader)) && strategy.get(state.getSequenceFor(leader)) > EPS) {
                isLarger = true;
//                System.out.println("removing state: " + state);
//                System.out.println();
//                for (Sequence seq : config.getSequencesFor(leader))
//                    System.out.println(seq + " #= " + seq.hashCode());
//                System.out.println();
                config.removeSequencesFrom(state);
            }}
        for (GameState state : tlStates){
            if (strategy.containsKey(state.getSequenceFor(leader)) && strategy.get(state.getSequenceFor(leader)) > EPS){

//                config.removeSequencesFrom(state);
                ArrayList<GameState> leaves = temporaryLeaves.get(state);
//                for (Action a : expander.getActions(state)) {
//                    Sequence seq = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
//                    seq.addLast(a);
////                for (TLGameState leaf : leaves)
//                    config.removeSequence(seq);
//                }
//                sequencesToBeRemoved.addAll(config.getInformationSetFor(state).getOutgoingSequences());
                temporaryLeaves.remove(state);//get(state).clear();
//                System.out.println();
//                for (Sequence seq : config.getSequencesFor(leader))
//                    System.out.println(seq + " #= " + seq.hashCode());
//                System.out.println();

//                System.exit(0);
                // use expander to get reachable leader states from TL
                HashSet<GameState> reachableStates = new HashSet<>();
                LinkedList<GameState> queue = new LinkedList<>();
                queue.add(state);
                while (queue.size() > 0) {
                    GameState currentState = queue.removeFirst();
                    if(!currentState.equals(state)) config.addStateToSequenceForm(currentState);
                    if (currentState.isGameEnd()) {
                        final double[] utilities = currentState.getUtilities();
                        Double[] u = new Double[utilities.length];

                        for (Player p : currentState.getAllPlayers()){
                            if(utilities.length > p.getId())
                                u[p.getId()] = utilities[p.getId()] * currentState.getNatureProbability()*gameInfo.getUtilityStabilizer();
                        }
                        config.setUtility(currentState, u);
                        continue;
                    }
                    if (!currentState.equals(state) && currentState.getPlayerToMove().equals(leader)){
                        reachableStates.add(currentState);
                        continue;
                    }
                    for (Action action : expander.getActions(currentState)) {
                        queue.add(currentState.performAction(action));
                    }
                }

//                System.out.println();
//                for (Sequence seq : config.getSequencesFor(leader))
//                    System.out.println(seq + " #= " + seq.hashCode());
//                System.out.println();

                // add states from leaves to new TL
                for (GameState newLeaf : reachableStates){
                    ArrayList<TLGameState> newTLLeaves = new ArrayList<>();
//                    for (TLGameState tlGameState : leaves)
//                        if (tlGameState.isReachableLeaf(newLeaf.getSequenceFor(gameInfo.getOpponent(leader))))
//                            newTLLeaves.add(tlGameState);
//                    temporaryLeaves.put(newLeaf, newTLLeaves);
                    temporaryLeaves.put(newLeaf, getReachableLeavesFor(newLeaf, fullConfig));
                    // use expander to update TLs and RG
                    for (Action action : expander.getActions(newLeaf)) {
                        GameState tlLeaf = newLeaf.performAction(action);
                        config.addStateToSequenceForm(tlLeaf);
                        final double[] utilities = tlLeaf.getUtilities();
                        Double[] u = new Double[utilities.length];

                        for (Player p : tlLeaf.getAllPlayers()){
                            if(utilities.length > p.getId())
                                u[p.getId()] = utilities[p.getId()] * tlLeaf.getNatureProbability()*gameInfo.getUtilityStabilizer();
                        }
                        config.setUtility(tlLeaf, u);
                    }
                }
            }
        }
//        for (Sequence seq : sequencesToBeRemoved)
//            config.removeSequence(seq);
        return  isLarger;
    }

    protected ArrayList<GameState> getReachableLeavesFor(GameState state, StackelbergConfig config){
        ArrayList<GameState> reachableLeafs = new ArrayList<>();
        Player follower = gameInfo.getOpponent(leader);
        for (GameState leaf : config.getAllLeafs()){
            if (state.getSequenceFor(leader).isPrefixOf(leaf.getSequenceFor(leader)) &&
                    state.getSequenceFor(follower).isPrefixOf(leaf.getSequenceFor(follower)))
                reachableLeafs.add(new TLGameState(gameInfo.getAllPlayers(), leaf, leader));
        }
        return getUpperConvexHullOfLeaves(reachableLeafs);//getParetoOptimalLeaves(reachableLeafs);
    }

    protected ArrayList<GameState> getParetoOptimalLeaves(ArrayList<GameState> reachableLeaves){
        if (!USE_PARETO_LEAVES) return reachableLeaves;
//        HashSet<double[]> allUtilities = new HashSet<>();
//        HashSet<double[]> paretoUtilities = new HashSet<>();
        ArrayList<GameState> leafs = new ArrayList<GameState>();
        for (GameState state : reachableLeaves){
            double[] utility = state.getUtilities();
//            allUtilities.add(utility);
            boolean isParetoOptimal = true;
            for (GameState other : reachableLeaves){
                if (state.equals(other)) continue;
                double[] otherUtility = other.getUtilities();
                if ((otherUtility[0]  > utility[0] && otherUtility[1] >= utility[1]) ||
                        (otherUtility[0] >= utility[0] && otherUtility[1] > utility[1])) {
                    isParetoOptimal = false;
                    break;
                }
            }
            if (isParetoOptimal){
//                paretoUtilities.add(utility);
                leafs.add(state);}
        }
//        System.out.println("U/P:");
//        String pareto = "";
//        for (double[] d : paretoUtilities) pareto += Arrays.toString(d) + "; ";
//        System.out.println(pareto);
//        pareto = "";
//        for (double[] d : allUtilities) pareto += Arrays.toString(d) + "; ";
//        System.out.println(pareto);
//        System.out.println(allUtilities.toString());
        if (leafs.size() == 1) return reachableLeaves;
        return leafs;
    }

    protected ArrayList<GameState> getUpperConvexHullOfLeaves(ArrayList<GameState> reachableLeaves){
        if (!USE_PARETO_LEAVES) return reachableLeaves;
        int n = reachableLeaves.size(), k = 0;
        GameState[] H = new GameState[2*n];

        Collections.sort(reachableLeaves, new Comparator<GameState>() {
            @Override
            public int compare(GameState o1, GameState o2) {
                if (o1.getUtilities()[0] == o2.getUtilities()[0])
                    return Double.compare(o1.getUtilities()[1], o2.getUtilities()[1]);
                else
                    return Double.compare(o1.getUtilities()[0], o2.getUtilities()[0]);
            }
        });

        // Build lower hull
        for (int i = 0; i < n; ++i) {
            while (k >= 2 && cross(H[k - 2], H[k - 1], reachableLeaves.get(i)) <= 0)
                k--;
            H[k++] = reachableLeaves.get(i);
        }

        // Build upper hull
        for (int i = n - 2, t = k + 1; i >= 0; i--) {
            while (k >= t && cross(H[k - 2], H[k - 1], reachableLeaves.get(i)) <= 0)
                k--;
            H[k++] = reachableLeaves.get(i);
        }
        if (k > 1) {
            return new ArrayList<>(Arrays.asList(Arrays.copyOfRange(H, 0, k - 1)));
//            H = H.subList(0, k-1);//Arrays.copyOfRange(H, 0, k - 1); // remove non-hull vertices after k; remove k - 1 which is a duplicate
        }
        return null;//(ArrayList<GameState>) H;
    }

    public double cross(GameState O, GameState A, GameState B) {
        double[] aUtilities = A.getUtilities();
        double[] bUtilities = B.getUtilities();
        double[] oUtilities = O.getUtilities();
        return (aUtilities[0] - oUtilities[0]) * (bUtilities[1] - oUtilities[1])
                - (aUtilities[1] - oUtilities[1]) * (bUtilities[0] - oUtilities[0]);
    }

    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        LeaderGenerationConfig config = new LeaderGenerationConfig(((StackelbergConfig)algConfig).getRootState());
        CompleteTwoPlayerSefceLP solver = null;
        boolean updated = true;
        TLExpander exp = new TLExpander(config, temporaryLeaves, expander);

        findInitialRG(config, exp, (StackelbergConfig) algConfig);


        while (updated){

//            System.out.println();
//            for (Sequence seq : config.getSequencesFor(leader))
//                System.out.println(seq);
//            System.out.println();

            if (EXPORT_TREE) new GambitEFG().write("tlGame.gbt", config.getRootState(), exp);
            solver = new CompleteTwoPlayerSefceLP(leader, gameInfo);
            result = solver.calculateLeaderStrategies(config, exp);
            overallConstraintGenerationTime += solver.getOverallConstraintGenerationTime();
            overallConstraintLPSolvingTime += solver.getOverallConstraintLPSolvingTime();
            updated = generateNewRG(solver.getResultStrategiesForPlayer(leader), config, exp, (StackelbergConfig) algConfig);
        }

        restrictedGameRatio = calculateRGRatio((StackelbergConfig) algConfig, config);
        if (restrictedGameRatio > 1.0) {
            System.err.println("RG too large");
//            System.exit(0);
        }
//                ((double)config.getSequencesFor(leader).size()) / ((StackelbergConfig) algConfig).getSequencesFor(leader).size();

        // iteratively solve for complete sefce until no TL is reachable.


//        this.result = result.getRight();
        return result;
    }

    protected double calculateRGRatio(StackelbergConfig fullConfig, GenSumSequenceFormConfig config){
        int fullISs = 0;
        for (SequenceInformationSet set : fullConfig.getAllInformationSets().values())
            if (!set.getOutgoingSequences().isEmpty())
                fullISs++;
        int iterISs = 0;
        for (SequenceInformationSet set : config.getAllInformationSets().values())
            if (!set.getOutgoingSequences().isEmpty())
                iterISs++;
        return ((double)iterISs + 1) / (fullISs + 1);
    }

    @Override
    public Double getResultForPlayer(Player leader) {
        return result;
    }

    @Override
    public Map<Sequence, Double> getResultStrategiesForPlayer(Player player) {
        return null;
    }

    @Override
    public long getOverallConstraintGenerationTime() {
        return overallConstraintGenerationTime;
    }

    @Override
    public long getOverallConstraintLPSolvingTime() {
        return overallConstraintLPSolvingTime;
    }

    @Override
    public String getInfo() {
        return "Iterative Sefce for simultaneous games with leader TLs.";
    }

    public double getRestrictedGameRatio(){
        return restrictedGameRatio;
    }
}
