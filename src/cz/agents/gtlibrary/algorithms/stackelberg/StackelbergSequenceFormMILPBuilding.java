package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.*;

public class StackelbergSequenceFormMILPBuilding extends StackelbergSequenceFormMILP {
    private Map<GameState, BoundLeafInfo> boundLeafMap;
    private Set<GameState> tempLeafs;
    Map<Pair<Integer, Sequence>, Set<GameState>> tempLeafMap;
    Map<Pair<Integer, Sequence>, Set<GameState>> addedNonTerminals;

    public StackelbergSequenceFormMILPBuilding(Player[] players, GameInfo info, Expander expander) {
        super(players, info, expander);
        boundLeafMap = new HashMap<>();
        tempLeafs = new HashSet<>();
        tempLeafMap = new HashMap<>();
        addedNonTerminals = new HashMap<>();
    }

    @Override
    public double calculateLeaderStrategies(int leaderIdx, int followerIdx, StackelbergConfig algConfig, Expander expander) {
        leader = players[leaderIdx];
        follower = players[followerIdx];
        buildBoundAndLeafMap(algConfig);
        createTempLeafs(algConfig.getRootState());

        IloCplex cplex = modelsForPlayers.get(leader);
        IloNumVar v0 = objectiveForPlayers.get(leader);

        try {
            while (true) {
                StackelbergConfig partialConfig = generateRestrictedGame(algConfig.getRootState());
                long startTime = threadBean.getCurrentThreadCpuTime();

                cplex.clearModel();
                cplex.addMaximize(v0);
                slackVariables.clear();
                variables.clear();
                constraints.clear();
                slackConstraints.clear();

                createVariables(cplex, partialConfig);
                createConstraintsForSets(cplex, partialConfig.getAllInformationSets().values());
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
                    return value;
                }
                expand(nonZeroTempLeafs, partialConfig);
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return Double.NaN;
    }

    private void expand(List<GameState> nonZeroTempLeafs, StackelbergConfig algConfig) {
        for (GameState state : nonZeroTempLeafs) {
            BoundLeafInfo info = boundLeafMap.get(state);

            tempLeafs.remove(state);
            expand(info, state, algConfig);
        }
    }

    private void expand(BoundLeafInfo info, GameState state, StackelbergConfig algConfig) {    //   když tam vložim tempLeaf a pak teprve někdy expanduju nějakej jinej stav ve stejnym setu tak to neni dobře, u všech templeafů co nejsou opravdové leafy si to musim hlídat tzn asi udělat mapu pair na stavy co tam už jsou a ve chvli kdy expanduju nějakej stav taks e podivat jestli tam nejsou jiný co je potřeba expandovat
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
        boolean isOnPath = true;

        for (Player player : state.getAllPlayers()) {
            isOnPath &= state.getSequenceFor(player).isPrefixOf(leaf.getSequenceFor(player));
        }
        return isOnPath;
    }

    private void expandTempLeafsFromISOf(BoundLeafInfo info, StackelbergConfig algConfig, GameState nextState) {
        Set<GameState> tempLeafsInIS = tempLeafMap.get(nextState.getISKeyForPlayerToMove());

        if (tempLeafsInIS != null)
            for (GameState tempLeaf : tempLeafsInIS) {
                tempLeafs.remove(tempLeaf);
                addToMap(addedNonTerminals, tempLeaf);
                expand(info, tempLeaf, algConfig);
            }
    }

    private void addToMap(Map<Pair<Integer, Sequence>, Set<GameState>> map, GameState tempLeaf) {
        Pair<Integer, Sequence> key = tempLeaf.getISKeyForPlayerToMove();
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

                config.setUtility(currentState, getUtilityArray(currentState, info));
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        return config;
    }

    private Double[] getUtilityArray(GameState currentState, BoundLeafInfo info) {
        if (currentState.isGameEnd())
            return wrap(currentState.getUtilities());
        Double[] utilityArray = new Double[2];

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
            tempLeafs.add(rootState.performAction(action));
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

    public List<GameState> getNonZeroTempLeafs(IloCplex cplex) throws IloException {
        List<GameState> nonZeroTempLeafs = new ArrayList<>();

        for (Map.Entry<Object, IloNumVar> entry : variables.entrySet()) {
            if (entry.getKey() instanceof GameState) {
                GameState possibleLeaf = (GameState) entry.getKey();

                if (!possibleLeaf.isGameEnd()) {
                    if (cplex.getValue(entry.getValue()) > 0)
                        nonZeroTempLeafs.add(possibleLeaf);
                }
            }
        }
        return nonZeroTempLeafs;
    }
}
