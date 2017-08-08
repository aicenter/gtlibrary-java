package cz.agents.gtlibrary.algorithms.stackelberg.milp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.PrintStream;
import java.util.*;

public class StackelbergSequenceFormMILPBuilding extends StackelbergSequenceFormMILP {
    private Map<GameState, BoundLeafInfo> boundLeafMap;
    private Set<GameState> tempLeafs;
    Map<ISKey, Set<GameState>> tempLeafMap;
    Map<ISKey, Set<GameState>> addedNonTerminals;

    public StackelbergSequenceFormMILPBuilding(Player[] players, Player leader, Player follower, GameInfo info, Expander<SequenceInformationSet>expander) {
        super(players, leader, follower, info, expander);
        boundLeafMap = new HashMap<>();
        tempLeafs = new HashSet<>();
        tempLeafMap = new HashMap<>();
        addedNonTerminals = new HashMap<>();
    }

    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        buildBoundAndLeafMap(algConfig);
        createTempLeafs(algConfig.getRootState());

        IloCplex cplex = modelsForPlayers.get(leader);
        IloNumVar v0 = objectiveForPlayers.get(leader);
        int it = 0;

        try {
            while (true) {
                StackelbergConfig partialConfig = generateRestrictedGame(algConfig.getRootState());
//                PartialGambitEFG gambit = new PartialGambitEFG();
//
//                gambit.write("MILPBuild" + it++ + ".gbt", algConfig.getRootState(), expander);

                long startTime = threadBean.getCurrentThreadCpuTime();

                cplex.clearModel();
                cplex.addMaximize(v0);
                slackVariables.clear();
                variables.clear();
                constraints.clear();
                slackConstraints.clear();

                createVariables(cplex, partialConfig);
                createConstraintsForSets(cplex, partialConfig.getAllInformationSets().values());
                assert partialConfig.getAllLeafs().equals(tempLeafs);
                createConstraintsForStates(cplex, partialConfig.getAllLeafs());
                createConstraintsForSequences(partialConfig, cplex, partialConfig.getSequencesFor(follower));
                setObjective(cplex, v0, partialConfig);
                debugOutput.println("phase 1 done");
                overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;

                cplex.exportModel("stck-Building" + leader + ".lp"); // uncomment for model export
                startTime = threadBean.getCurrentThreadCpuTime();
                debugOutput.println("Solving");
                cplex.solve();
                overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
                debugOutput.println("Status: " + cplex.getCplexStatus());

                assert cplex.getCplexStatus() == IloCplex.CplexStatus.Optimal || cplex.getCplexStatus() == IloCplex.CplexStatus.OptimalTol;
                List<GameState> nonZeroTempLeafs = getNonZeroTempLeafs(cplex);

                if (nonZeroTempLeafs.isEmpty()) {
                    double value = cplex.getValue(v0);

                    resultValues.put(leader, value);
                    System.out.println("actual leaf count: " + tempLeafs.size() + " vs " + algConfig.getAllLeafs().size());
                    System.out.println("actual IS count: " + partialConfig.getAllInformationSets().size());
                    System.out.println("actual size: FirstPlayer Sequences: " + partialConfig.getSequencesFor(players[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(players[1]).size());
//                    gambit = new PartialGambitEFG();
//
//                    gambit.write("finalMILPBuild.gbt", algConfig.getRootState(), expander);
                    Map<Sequence, Double> leaderRP = createSolution(partialConfig, leader, cplex);

                    debugOutput.println("Leader rp:");
                    for (Map.Entry<Sequence, Double> entry : leaderRP.entrySet()) {
                        if(entry.getValue() > 0)
                            debugOutput.println(entry);
                    }
                    Map<Sequence, Double> followerRP = createSolution(partialConfig, leader, cplex);

                    debugOutput.println("Follower rp:");
                    for (Map.Entry<Sequence, Double> entry : followerRP.entrySet()) {
                        if(entry.getValue() > 0)
                            debugOutput.println(entry);
                    }
                    debugOutput.println("Leaf probs");
                    for (Map.Entry<Object, IloNumVar> entry : variables.entrySet()) {
                        if (entry.getKey() instanceof GameState) {
                            try {
                                if (cplex.getValue(entry.getValue()) > 0)
                                    debugOutput.println(entry.getKey() + ": " + cplex.getValue(entry.getValue()));
                            } catch (IloException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    assert !areTempLeafsNotReachable(algConfig.getRootState(), leaderRP, followerRP);
                    return value;
                }
                expand(nonZeroTempLeafs, partialConfig);
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return Double.NaN;
    }

    private boolean areTempLeafsNotReachable(GameState state, Map<Sequence, Double> leaderRP, Map<Sequence, Double> followerRP) {
        if (tempLeafs.contains(state))
            return !state.isGameEnd();
        for (Action action : expander.getActions(state)) {
            Sequence continuation = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());

            continuation.addLast(action);
            Double probability = continuation.getPlayer().equals(leader)?leaderRP.get(continuation):followerRP.get(continuation);

            if(probability != null && probability > 0)
                if(areTempLeafsNotReachable(state.performAction(action), leaderRP, followerRP))
                    return true;
        }
        return false;
    }

    private void expand(List<GameState> nonZeroTempLeafs, StackelbergConfig algConfig) {
        for (GameState state : nonZeroTempLeafs) {
            BoundLeafInfo info = boundLeafMap.get(state);

            tempLeafs.remove(state);
            removeFrom(tempLeafMap, state);
            if (!state.isGameEnd())
                addToMap(addedNonTerminals, state);
            expandTempLeafsFromISOf(info, algConfig, state);
            expand(info, state, algConfig);
        }
    }

    private void expand(BoundLeafInfo info, GameState state, StackelbergConfig algConfig) {
        if (state.isGameEnd()) {
//            assert info.leaf.equals(state);
            tempLeafs.add(state);
            return;
        }
        for (Action action : expander.getActions(state)) {
            GameState nextState = state.performAction(action);

            if (isOnPathTo(nextState, info.leaf)) {
                addToMap(addedNonTerminals, nextState);
                expand(info, nextState, algConfig);
                expandTempLeafsFromISOf(info, algConfig, nextState);
            } else {
                Set<GameState> addedNonTerminalsForIS = addedNonTerminals.get(nextState.getISKeyForPlayerToMove());

                if (addedNonTerminalsForIS != null) {
                    addToMap(addedNonTerminals, nextState);
                    expand(info, nextState, algConfig);
                } else {
                    addToMap(tempLeafMap, nextState);
                    tempLeafs.add(nextState);
                }
            }
        }
    }

    private boolean isOnPathTo(GameState state, GameState leaf) {
        for (Player player : state.getAllPlayers()) {
            if(!state.getSequenceFor(player).isPrefixOf(leaf.getSequenceFor(player)))
                return false;
        }
        return true;
    }

    private void expandTempLeafsFromISOf(BoundLeafInfo info, StackelbergConfig algConfig, GameState state) {
        ISKey key = state.getISKeyForPlayerToMove();
        Set<GameState> tempLeafsInIS = tempLeafMap.get(key);

        if (tempLeafsInIS != null) {
            for (GameState tempLeaf : tempLeafsInIS) {
                tempLeafs.remove(tempLeaf);
                if (!tempLeaf.isGameEnd())
                    addToMap(addedNonTerminals, tempLeaf);
                expand(info, tempLeaf, algConfig);
            }
            tempLeafMap.remove(key);
        }
    }

    private void removeFrom(Map<ISKey, Set<GameState>> map, GameState state) {
        ISKey key = state.getISKeyForPlayerToMove();
        Set<GameState> states = map.get(key);

        if (states == null)
            return;
        states.remove(state);
        if (states.isEmpty())
            map.remove(key);
    }

    private void addToMap(Map<ISKey, Set<GameState>> map, GameState tempLeaf) {
        ISKey key = tempLeaf.getISKeyForPlayerToMove();
        Set<GameState> tempLeafs = map.get(key);

        if (tempLeafs == null) {
            tempLeafs = new HashSet<>();
            map.put(key, tempLeafs);
        }
        tempLeafs.add(tempLeaf);
    }

    private StackelbergConfig generateRestrictedGame(GameState root) {
        ArrayDeque<GameState> queue = new ArrayDeque<>();
        StackelbergConfig config = new StackelbergConfig(root);

        queue.add(root);
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

            config.addStateToSequenceForm(currentState);
            if (tempLeafs.contains(currentState)) {
                BoundLeafInfo info = boundLeafMap.get(currentState);

                config.setUtility(currentState, getWrappedUtilityArray(currentState, info));
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        return config;
    }

    private Double[] getWrappedUtilityArray(GameState currentState, BoundLeafInfo info) {
        if (currentState.isGameEnd())
            return wrap(currentState.getUtilities());
        Double[] utilityArray = new Double[2];

        utilityArray[leader.getId()] = info.leaderUpperBound;
        utilityArray[follower.getId()] = info.followerUpperBound;
        return utilityArray;
    }

    private double[] getUtilityArray(GameState currentState, BoundLeafInfo info) {
        if (currentState.isGameEnd())
            return currentState.getUtilities();
        double[] utilityArray = new double[2];

        utilityArray[leader.getId()] = info.leaderUpperBound;
        utilityArray[follower.getId()] = info.followerUpperBound;
        return utilityArray;
    }

    private Double[] wrap(double[] primitiveArray) {
        Double[] wrapperArray = new Double[primitiveArray.length];

        for (int i = 0; i < wrapperArray.length; i++) {
            wrapperArray[i] = primitiveArray[i];
        }
        return wrapperArray;
    }


    private void createTempLeafs(GameState rootState) {
        for (Action action : expander.getActions(rootState)) {
            GameState nextState = rootState.performAction(action);

            tempLeafs.add(nextState);
            addToMap(tempLeafMap, nextState);
        }
    }

//    private void buildBoundAndLeafMap(StackelbergConfig algConfig) {
//        recursive(algConfig.getRootState(), algConfig);
//    }
//
//    private BoundLeafInfo recursive(GameState state, StackelbergConfig algConfig) {
//        if (state.isGameEnd()) {
//            return getBoundLeafInfo(state);
//        }
//        InformationSet set = algConfig.getInformationSetFor(state);
//        BoundLeafInfo info = getOrCreateInfo(set);
//
//        for (Action action : expander.getActions(state)) {
//            BoundLeafInfo tempInfo = recursive(state.performAction(action), algConfig);
//
//            info.followerUpperBound = Math.max(info.followerUpperBound, tempInfo.followerUpperBound);
//            if (tempInfo.leaderUpperBound > info.leaderUpperBound) {
//                info.leaderUpperBound = tempInfo.leaderUpperBound;
//                info.leaf = tempInfo.leaf;
//            }
//        }
//        return info;
//    }
//
//    private BoundLeafInfo getOrCreateInfo(InformationSet set) {
//        BoundLeafInfo info = boundLeafMap.get(set);
//
//        if (info == null) {
//            info = new BoundLeafInfo(null, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
//            boundLeafMap.put(set, info);
//        }
//        return info;
//    }

    private void buildBoundAndLeafMap(StackelbergConfig algConfig) {
        recursive(algConfig.getRootState());
    }

    private BoundLeafInfo recursive(GameState state) {
        if (state.isGameEnd())
            return getBoundLeafInfo(state);
        BoundLeafInfo info = createAndPutInfo(state);

        for (Action action : expander.getActions(state)) {
            BoundLeafInfo tempInfo = recursive(state.performAction(action));

            info.followerUpperBound = Math.max(info.followerUpperBound, tempInfo.followerUpperBound);
            if (tempInfo.leaderUpperBound > info.leaderUpperBound) {
                info.leaderUpperBound = tempInfo.leaderUpperBound;
                info.leaf = tempInfo.leaf;
            }
        }
        return info;
    }

    private BoundLeafInfo createAndPutInfo(GameState state) {
        BoundLeafInfo info = new BoundLeafInfo(null, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

        boundLeafMap.put(state, info);
        return info;
    }

    private BoundLeafInfo getBoundLeafInfo(GameState state) {
        double[] utilities = state.getUtilities();

        return new BoundLeafInfo(state, utilities[leader.getId()], utilities[follower.getId()]);
    }

    public List<GameState> getNonZeroTempLeafs(IloCplex cplex) {
        List<GameState> nonZeroTempLeafs = new ArrayList<>();

        for (Map.Entry<Object, IloNumVar> entry : variables.entrySet()) {
            if (entry.getKey() instanceof GameState) {
                GameState possibleLeaf = (GameState) entry.getKey();

                if (!possibleLeaf.isGameEnd()) {
                    try {
                        if (cplex.getValue(entry.getValue()) > 0)
                            nonZeroTempLeafs.add(possibleLeaf);
                    } catch (IloException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return nonZeroTempLeafs;
    }

}
