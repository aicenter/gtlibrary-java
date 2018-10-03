package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.LeaderGenerationConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.CompleteTwoPlayerSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.iinodes.TLAction;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.iinodes.TLExpander;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.iinodes.TLGameState;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;

/**
 * Created by Jakub Cerny on 14/11/2017.
 */
public class LeaderTLSefceLP extends LeaderTLSimulataneousSefceLP {

    HashSet<GameState> rootsOfTLs;
    ArrayList<Expander<SequenceInformationSet>> expanders;
    HashMap<ISKey, Integer> isSizes;
    final boolean MAINTAIN_IS_CLOSED_GAME = false;
    final boolean COLLECT_EXPANDERS = true;

    public LeaderTLSefceLP(Player leader, GameInfo gameInfo) {
        super(leader, gameInfo);
        rootsOfTLs = new HashSet<>();
        isSizes = new HashMap<>();
        if (COLLECT_EXPANDERS) expanders = new ArrayList<>();
    }

    @Override
    public String getInfo() {
        return "Iterative Sefce with leader TLs w/ gadgets." + "\nSettings: PL = " + USE_PARETO_LEAVES + "; CRG = " + MAINTAIN_IS_CLOSED_GAME;
    }


    @Override
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
                rootsOfTLs.add(currentState);
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }

        System.out.println("initial TLs found");

        HashSet<GameState> tls = new HashSet<>(temporaryLeaves.keySet());
        for (GameState state : tls) {
            GameState middleState = state.copy();
            middleState.performActionModifyingThisState(new TLAction(config.getInformationSetFor(state),0, temporaryLeaves, state));
//            TLGameState middleState = new TLGameState(gameInfo.getAllPlayers(), leader, 13*state.hashCode()+7, state);
            temporaryLeaves.get(state).add(middleState);
            temporaryLeaves.put(middleState, new ArrayList<>());
        }
//        tls = null;


        for (GameState leaf : fullConfig.getAllLeafs())
            for (GameState state : tls) {
                if (state.getSequenceFor(leader).isPrefixOf(leaf.getSequenceFor(leader)) &&
                        state.getSequenceFor(gameInfo.getOpponent(leader)).isPrefixOf(leaf.getSequenceFor(gameInfo.getOpponent(leader)))) {
                    TLGameState tlLeaf = new TLGameState(gameInfo.getAllPlayers(), leaf, leader);
//                    History history = state.getHistory().copy();
//                    history.addActionOf(expander.getActions(state).get(temporaryLeaves.get(state).size()), leader);
//                    tlLeaf.setHistory(history);
                    temporaryLeaves.get(temporaryLeaves.get(state).get(0)).add(tlLeaf);
//                    history.addActionOf(expander.getActions(state).get(temporaryLeaves.get(state).size()), leader);
//                    tlLeaf.setHistory(history);
                    break;
                }
            }

        System.out.println("Initial leaves identified.");
//        System.out.println(temporaryLeaves);
        for (GameState state : tls)
            for (Action action : expander.getActions(state)) {
//                    System.out.println("ST1: " + state);
                GameState middleState = state.performAction(action);
//                    System.out.println("ST2: " + state);
//                    TLGameState middleState = temporaryLeaves.get(state).get(((TLAction)action).getIndex());
//                    History history = state.getHistory().copy();
//                    history.addActionOf(action, leader);
//                    middleState.setHistory(history);

                config.addStateToSequenceForm(middleState);
//                System.out.println(middleState.getISKeyForPlayerToMove() + (temporaryLeaves.containsKey(middleState) ? " 1" : " 0"));
//                System.out.println(config.getInformationSetFor(middleState).getISKey());
//                System.out.println(expander.getAlgorithmConfig().getInformationSetFor(middleState).getISKey());
//                System.out.println("///");
//                System.out.println(temporaryLeaves.get(middleState).size());
//                System.out.println(expander.getActions(middleState).size());
                for (Action nextAction : expander.getActions(middleState)) {
//                    System.out.println(nextAction);
                    GameState tlLeaf = middleState.performAction(nextAction);
                    config.addStateToSequenceForm(tlLeaf);

                    final double[] utilities = tlLeaf.getUtilities();
//                    System.out.println(state + ", " + middleState + ", " + utilities);
                    Double[] u = new Double[utilities.length];

                    for (Player p : tlLeaf.getAllPlayers()) {
                        if (utilities.length > p.getId())
                            u[p.getId()] = utilities[p.getId()] * tlLeaf.getNatureProbability() * gameInfo.getUtilityStabilizer();
                    }
                    config.setUtility(tlLeaf, u);
                }
            }
        System.out.println("Config initiated.");
    }

    protected HashSet<ISKey> getImmediateFollowerISsUnder(GameState state, Expander<SequenceInformationSet> expander){
        HashSet<ISKey> immediateISs = new HashSet<>();
        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(state);
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
            if (currentState.isGameEnd()) {
                continue;
            }
            if (currentState.getPlayerToMove().equals(gameInfo.getOpponent(leader))){
                immediateISs.add(currentState.getISKeyForPlayerToMove());
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        return immediateISs;
    }

    protected HashSet<GameState> generateOtherTLRootsToBeDiscarded(Expander<SequenceInformationSet> expander, GameState state, LeaderGenerationConfig config){
        HashSet<GameState> influencedRoots = new HashSet<>();

        // find immediate follower ISs of state
        HashSet<ISKey> immediateISs = getImmediateFollowerISsUnder(state, expander);

        // find immediate follower ISs of all roots
        for (GameState root : rootsOfTLs){
            if (root.equals(state)) continue;
            for (ISKey key : getImmediateFollowerISsUnder(root, expander))
                if (immediateISs.contains(key)) {
                    config.removeSequencesFrom(root);
                    config.removeSequencesFrom(temporaryLeaves.get(root).get(0));
                    influencedRoots.add(root);
                    break;
                }
        }
        return  influencedRoots;
    }

    @Override
    protected boolean generateNewRG(Map<Sequence, Double> strategy, LeaderGenerationConfig config, TLExpander expander, StackelbergConfig fullConfig){
        boolean isLarger = false;

//        for (Sequence seq : config.getSequencesFor(leader))
//            System.out.println(seq + " #= " + seq.hashCode());
//        System.out.println();

        // check reachable TL;
//        HashSet<Sequence> sequencesToBeRemoved = new HashSet<>();
        HashSet<GameState> statesToBeDiscarded = new HashSet<>();
//        HashSet<GameState> tlStates = new HashSet<>(temporaryLeaves.keySet());

        for (GameState state : rootsOfTLs){
            if (strategy.containsKey(state.getSequenceFor(leader)) && strategy.get(state.getSequenceFor(leader)) > EPS) {
                isLarger = true;
//                System.out.println("removing state: " + state);
//                System.out.println();
//                for (Sequence seq : config.getSequencesFor(leader))
//                    System.out.println(seq + " #= " + seq.hashCode());
//                System.out.println();
                config.removeSequencesFrom(state);
                config.removeSequencesFrom(temporaryLeaves.get(state).get(0));

                // add also other TLs
                statesToBeDiscarded.add(state);
                if (MAINTAIN_IS_CLOSED_GAME) {
                HashSet<GameState> othersToBeDiscarded = generateOtherTLRootsToBeDiscarded(expander.getExpander(), state, config);
//                if (!statesToBeDiscarded.isEmpty())
//                System.out.println(state + " : " + othersToBeDiscarded.toString());
                statesToBeDiscarded.addAll(othersToBeDiscarded);
//                statesToBeDiscarded.addAll(generateOtherTLRootsToBeDiscarded(expander.getExpander(), state, config));
                }
            }}
        for (GameState state : statesToBeDiscarded){
            if (true){//strategy.containsKey(state.getSequenceFor(leader)) && strategy.get(state.getSequenceFor(leader)) > EPS){

//                config.removeSequencesFrom(state);
                ArrayList<GameState> leaves = temporaryLeaves.get(temporaryLeaves.get(state).get(0));
//                for (Action a : expander.getActions(state)) {
//                    Sequence seq = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
//                    seq.addLast(a);
////                for (TLGameState leaf : leaves)
//                    config.removeSequence(seq);
//                }
//                sequencesToBeRemoved.addAll(config.getInformationSetFor(state).getOutgoingSequences());
                temporaryLeaves.remove(temporaryLeaves.get(state).get(0));
                temporaryLeaves.remove(state);//get(state).clear();
                rootsOfTLs.remove(state);
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
                        rootsOfTLs.add(currentState);
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
//                    TLGameState middleState = new TLGameState(gameInfo.getAllPlayers(), leader, 13*newLeaf.hashCode()+7, newLeaf);
                    GameState middleState = newLeaf.copy();
                    middleState.performActionModifyingThisState(new TLAction(config.getInformationSetFor(newLeaf),0, temporaryLeaves, state));

                    temporaryLeaves.put(newLeaf, new ArrayList<GameState>(){{add(middleState);}});
                    temporaryLeaves.put(middleState, getReachableLeavesFor(newLeaf, fullConfig));
                    config.addStateToSequenceForm(middleState);//newLeaf.performAction(expander.getActions(newLeaf).get(0)));
                    // use expander to update TLs and RG
                    for (Action action : expander.getActions(middleState)) {
                        GameState tlLeaf = middleState.performAction(action);
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

    protected StackelbergConfig fillConfig(GameState root, Expander<SequenceInformationSet> expander, GameInfo gameConfig){
        StackelbergConfig algConfig = new StackelbergConfig(root);
        LinkedList<GameState> queue = new LinkedList<>();

        queue.add(root);

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
//            System.out.println(currentState.toString());

//            System.out.println(currentState);

            algConfig.addStateToSequenceForm(currentState);
            if (currentState.isGameEnd()) {
                final double[] utilities = currentState.getUtilities();
                Double[] u = new Double[utilities.length];

                for (Player p : currentState.getAllPlayers()){
                    if(utilities.length > p.getId())
                        u[p.getId()] = utilities[p.getId()] * currentState.getNatureProbability()*gameConfig.getUtilityStabilizer();
                }
                algConfig.setUtility(currentState, u);
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        return algConfig;
    }

    @Override
    protected double calculateRGRatio(StackelbergConfig fullConfig, GenSumSequenceFormConfig config){
        int fullISs = 0;
        for (SequenceInformationSet set : fullConfig.getAllInformationSets().values())
            if (!set.getOutgoingSequences().isEmpty())
                fullISs++;
        int iterISs = 0;
        for (SequenceInformationSet set : config.getAllInformationSets().values())
            if (!set.getOutgoingSequences().isEmpty())
                iterISs++;
        iterISs -= rootsOfTLs.size();
        return ((double)iterISs + 1) / (fullISs + 1);
    }

    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        LeaderGenerationConfig config = new LeaderGenerationConfig(((StackelbergConfig)algConfig).getRootState());
        CompleteTwoPlayerSefceLP solver = null;
        boolean updated = true;
        TLExpander exp = new TLExpander(config, temporaryLeaves, expander);

        findInitialRG(config, exp, (StackelbergConfig) algConfig);

        StackelbergConfig sc = null;

        int iteration = -1;

        while (updated){

            iteration++;

            solver = new CompleteTwoPlayerSefceLP(leader, gameInfo);
            System.out.println("Solver initiated.");

            sc = fillConfig(config.getRootState(), exp, gameInfo);
            System.out.println("Config filled.");

            Expander<SequenceInformationSet> e = new TLExpander(sc, temporaryLeaves, expander);
            if (COLLECT_EXPANDERS) expanders.add(new TLExpander(sc, new HashMap(temporaryLeaves), expander));
            if (EXPORT_TREE) new GambitEFG().write("tlGame_"+iteration+".gbt", config.getRootState(), e);

            result = solver.calculateLeaderStrategies(sc, e);
            overallConstraintGenerationTime += solver.getOverallConstraintGenerationTime();
            overallConstraintLPSolvingTime += solver.getOverallConstraintLPSolvingTime();
            updated = generateNewRG(solver.getResultStrategiesForPlayer(leader), config, exp, (StackelbergConfig) algConfig);
        }

        updateISSizes(sc);

        restrictedGameRatio = calculateRGRatio((StackelbergConfig) algConfig, sc);//config);
        if (restrictedGameRatio > 1.0) {
            System.err.println("RG too large");
//            System.exit(0);
        }
//                ((double)config.getSequencesFor(leader).size()) / ((StackelbergConfig) algConfig).getSequencesFor(leader).size();

        // iteratively solve for complete sefce until no TL is reachable.


//        this.result = result.getRight();
        return result;
    }

    public HashMap<ISKey, Integer> getISSizes(){
        return isSizes;
    }

    protected void updateISSizes(StackelbergConfig config){
        isSizes.clear();
        for (ISKey key : config.getAllInformationSets().keySet())
            isSizes.put(key, config.getAllInformationSets().get(key).getAllStates().size());
    }

    public ArrayList<Expander<SequenceInformationSet>> getExpanders(){
        if (COLLECT_EXPANDERS) return expanders;
        else return null;
    }

}
