package cz.agents.gtlibrary.algorithms.crswfabstraction;

import cz.agents.gtlibrary.algorithms.crswfabstraction.mergeddomain.CompoundISKey;
import cz.agents.gtlibrary.algorithms.crswfabstraction.mergeddomain.MergedExpander;
import cz.agents.gtlibrary.algorithms.crswfabstraction.mergeddomain.MergedGameInfo;
import cz.agents.gtlibrary.algorithms.crswfabstraction.mergeddomain.MergedGameState;
import cz.agents.gtlibrary.algorithms.crswfabstraction.testdomain.TestExpander;
import cz.agents.gtlibrary.algorithms.crswfabstraction.testdomain.TestInfo;
import cz.agents.gtlibrary.algorithms.crswfabstraction.testdomain.TestState;
import cz.agents.gtlibrary.algorithms.crswfabstraction.utils.MappingPermutator;
import cz.agents.gtlibrary.algorithms.crswfabstraction.utils.Permutator;
import cz.agents.gtlibrary.algorithms.crswfabstraction.utils.RandomListPermutator;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.domain.randomgameimproved.io.GambitEFG;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Assumes:
 *      perfect recall
 *      all leaf nodes at the same level
 */
public class CrswfAlgorithm {

    private static final boolean DEBUG = false;

    private boolean scaling;
    private boolean bestLeafMapping;
    private int maxLeafMappingTries;
    private double solveTimeLimit;
    private boolean adaptiveEpsilon;

    private boolean mergedSomething;
    private double solveTime;
    private double preprocessingTime;
    private double buildTime;
    private long originalNumberOfSets;
    private long numberOfNatureSets;
    private long mergedNumberOfSets;
    private boolean encounteredError;
    private Map<ISKey, CompoundISKey> mergedKeys;
    private Map<Action, Action> mergedActions;

    private Map<Action, Set<LeafNode>> leafsForActions;
    private List<Set<CrswfInformationSet>> levels;
    private double utilityCorrection;

    private Map<InformationSet, IloNumVar> setVariables;

    private static IloCplex cplex;

    private double minUtility;
    private double maxUtility;

    public static void main(String[] args) {
        String exportFileName = args[0];
        boolean scaling = Boolean.parseBoolean(args[1]);
        boolean bestLeafMapping = Boolean.parseBoolean(args[2]);
        int seed = Integer.parseInt(args[3]);
        boolean isEpsilonAdaptive = Boolean.parseBoolean(args[4]);
        double epsilon = Double.parseDouble(args[5]);
        int maxDepth = Integer.parseInt(args[6]);
        int minBF = Integer.parseInt(args[7]);
        int maxBF = Integer.parseInt(args[8]);
        int maxObservation = Integer.parseInt(args[9]);
        double natureStateProbability = Double.parseDouble(args[10]);
        double timeLimit = Double.parseDouble(args[11]);
        //runTest();
        //runSingleExperiment("small_bf_big_depth/2", true, false, 101, true, 0.02, 7, 3, 3, 6, 0.15, 300);
        runSingleExperiment(exportFileName, scaling, bestLeafMapping, seed, isEpsilonAdaptive, epsilon, maxDepth, minBF, maxBF, maxObservation, natureStateProbability, timeLimit);
        //runExperiment("small_bf_big_depth/always_merge", true, false, 20, 100, false, Double.POSITIVE_INFINITY, 7, 3, 3, 6, 0.15, 300);
        //runExperiment("small_bf_big_depth/5C", true, false, 1, 119, true, 0.05, 7, 3, 3, 6, 0.15, 300);
        /*runExperiment("small_bf_big_depth/10", true, false, 20, 100, true, 0.10, 7, 3, 3, 6, 0.15, 300);
        runExperiment("small_bf_big_depth/20", true, false, 20, 100, true, 0.20, 7, 3, 3, 6, 0.15, 300);
        runExperiment("small_bf_big_depth/50", true, false, 20, 100, true, 0.50, 7, 3, 3, 6, 0.15, 300);*/
        /*runExperiment("small_bf_big_depth/100", true, false, 20, 100, true, 1, 7, 3, 3, 6, 0.15, 300);
        runExperiment("small_bf_big_depth/200", true, false, 20, 100, true, 2, 7, 3, 3, 6, 0.15, 300);*/
    }

    private static void runSingleExperiment(String exportFileName,
                                               boolean scaling,
                                               boolean bestLeafMapping,
                                               int seed,
                                               boolean isEpsilonAdaptive,
                                               double epsilon,
                                               int maxDepth,
                                               int minBF,
                                               int maxBF,
                                               int maxObservation,
                                               double natureStateProbability,
                                               double timeLimit) {
        File exportFile = new File("/home/mima/Dropbox/Skola/SVP/Experiments/" + exportFileName + ".txt");
        if (!exportFile.isFile()) {
            createHeader(exportFile, scaling, bestLeafMapping, isEpsilonAdaptive, epsilon, maxDepth, minBF, maxBF, maxObservation, natureStateProbability, timeLimit);
        }
        PrintWriter out;
        try {
            out = new PrintWriter(new FileOutputStream(exportFile, true), true);
            RandomGameInfo.MAX_DEPTH = maxDepth;
            RandomGameInfo.MIN_BF = minBF;
            RandomGameInfo.MAX_BF = maxBF;
            RandomGameInfo.MAX_OBSERVATION = maxObservation;
            RandomGameInfo.NATURE_STATE_PROBABILITY = natureStateProbability;
            RandomGameInfo.seed = seed;

            System.out.println("Running game with seed " + seed);

            GameState root = new RandomGameState();
            GameInfo info = new RandomGameInfo();
            AlgorithmConfig<InformationSet> config = new CrswfConfig();
            Expander<InformationSet> expander = new RandomGameExpander<>(config);

            CrswfAlgorithm algorithm = new CrswfAlgorithm(scaling, bestLeafMapping, isEpsilonAdaptive);
            algorithm.setSolveTimeLimit(timeLimit);
            algorithm.run(root, expander, config, epsilon);

            out.printf("%20d|%20d|%20d|%20f|%20f|%20f|%20f\n",
                    seed, algorithm.getOriginalNumberOfSets(), algorithm.getMergedNumberOfSets(), algorithm.getPreprocessingTime(), algorithm.getBuildTime(), algorithm.getSolveTime(), algorithm.getMaxUtility());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void createHeader(File file,
                                     boolean scaling,
                                     boolean bestLeafMapping,
                                     boolean isEpsilonAdaptive,
                                     double epsilon,
                                     int maxDepth,
                                     int minBF,
                                     int maxBF,
                                     int maxObservation,
                                     double natureStateProbability,
                                     double timeLimit) {
        PrintWriter out;
        try {
            out = new PrintWriter(new FileWriter(file));
            out.printf("Test on random games.\n" +
                            (scaling ? "U" : "Not u") + "sing scaling.\n" +
                            (bestLeafMapping ? "U" : "Not u") + "sing best leaf mapping.\n" +
                            "Epsilon is %f" + (isEpsilonAdaptive ? "%%" : "") + ".\n" +
                            "Branching factor is from %d to %d.\n" +
                            "Depth is %d.\n" +
                            "Maximum observation is %d.\n" +
                            "Nature state probability is %f.\n" +
                            "Time limit is %f.\n",
                    (isEpsilonAdaptive ? 100*epsilon : epsilon),
                    minBF,
                    maxBF,
                    maxDepth,
                    maxObservation,
                    natureStateProbability,
                    timeLimit);
            out.printf("%20s|%20s|%20s|%20s|%20s|%20s|%20s\n", "Seed", "Original Sets", "Merged Sets", "Preprocessing Time", "Build Time", "Solve Time", "Maximum utility");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Various tests and experiments.
     */
    private static boolean runExperiment(String exportFileName,
                                         boolean scaling,
                                         boolean bestLeafMapping,
                                         int sampleSize,
                                         int startingSeed,
                                         boolean isEpsilonAdaptive,
                                         double epsilon,
                                         int maxDepth,
                                         int minBf,
                                         int maxBf,
                                         int maxObservation,
                                         double natureStateProbability,
                                         double timeLimit) {
        RandomGameInfo.MAX_DEPTH = maxDepth;
        RandomGameInfo.MIN_BF = minBf;
        RandomGameInfo.MAX_BF = maxBf;
        RandomGameInfo.MAX_OBSERVATION = maxObservation;
        RandomGameInfo.NATURE_STATE_PROBABILITY = natureStateProbability;


        List<Integer> successfulMerges = new ArrayList<>();

        PrintWriter out;
        try {
            out = new PrintWriter(new FileWriter("Experiments/" + exportFileName + ".txt"));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        out.printf("Time test on random games.\n" +
                        (scaling ? "U" : "Not u") + "sing scaling.\n" +
                        (bestLeafMapping ? "U" : "Not u") + "sing best leaf mapping.\n" +
                        "Epsilon is %f" + (isEpsilonAdaptive ? "%%" : "") + ".\n" +
                        "Branching factor is from %d to %d.\n" +
                        "Depth is %d.\n" +
                        "Maximum observation is %d.\n" +
                        "Nature state probability is %f.\n" +
                        "Starting seed is %d.\n" +
                        "Time limit is %f.\n",
                epsilon,
                RandomGameInfo.MIN_BF,
                RandomGameInfo.MAX_BF,
                RandomGameInfo.MAX_DEPTH,
                RandomGameInfo.MAX_OBSERVATION,
                RandomGameInfo.NATURE_STATE_PROBABILITY,
                startingSeed,
                timeLimit);
        out.printf("%20s|%20s|%20s|%20s|%20s|%20s|%20s\n", "Game Number", "Preprocessing Time", "Build Time", "Solve Time", "Original Sets", "Merged Sets", "Maximum utility");

        for (int gameNumber = 0; gameNumber < sampleSize; gameNumber++) {
            RandomGameInfo.seed = startingSeed + gameNumber;
            System.out.println("Running game number " + gameNumber);
            GameState root = new RandomGameState();
            GameInfo info = new RandomGameInfo();
            AlgorithmConfig<InformationSet> config = new CrswfConfig();
            Expander<InformationSet> expander = new RandomGameExpander<>(config);

            CrswfAlgorithm algorithm = new CrswfAlgorithm(scaling, bestLeafMapping, isEpsilonAdaptive);
            algorithm.setSolveTimeLimit(timeLimit);
            algorithm.run(root, expander, config, epsilon);
            if (algorithm.mergedSomething()) successfulMerges.add(gameNumber);
            out.printf("%20d|%20f|%20f|%20f|%20d|%20d|%20f\n",
                    gameNumber, algorithm.getPreprocessingTime(), algorithm.getBuildTime(), algorithm.getSolveTime(), algorithm.getOriginalNumberOfSets(), algorithm.getMergedNumberOfSets(), algorithm.getMaxUtility());
        }

        System.out.println("Merged something in " + ((double) successfulMerges.size()/sampleSize*100) + "% of games");
        System.out.println("Merges occured in games number " + successfulMerges);
        out.close();

        return true;
    }

    private static void randomGameConsistencyTest(int i) {
        RandomGameInfo.MAX_DEPTH = 3;
        RandomGameInfo.MIN_BF = 2;
        RandomGameInfo.MAX_BF = 4;
        RandomGameInfo.MAX_OBSERVATION = 3;
        RandomGameInfo.NATURE_STATE_PROBABILITY = 0.3;
        RandomGameInfo.seed = 105;
        GameState root = new RandomGameState();
        AlgorithmConfig<InformationSet> config = new CrswfConfig();
        Expander<InformationSet> expander = new RandomGameExpander<>(config);

        BasicGameBuilder.build(root, config, expander);
        export(root, expander, i + "");
    }


    private static void runTest() {
        GameState root = new TestState();
        AlgorithmConfig<InformationSet> config = new CrswfConfig();
        Expander<InformationSet> expander = new TestExpander<>(config);
        GameInfo info = new TestInfo();
        CrswfAlgorithm algorithm = new CrswfAlgorithm(true, false, true);
        algorithm.run(root, expander, config, 0.5);
        System.out.println("Preprocessing lasted " + algorithm.getPreprocessingTime() + " seconds");
        System.out.println("Building of the LP lasted " + algorithm.getBuildTime() + " seconds");
        System.out.println("Cplex ran for " + algorithm.getSolveTime() + " seconds");
        System.out.println("Original game had " + algorithm.getOriginalNumberOfSets() + " sets");
        System.out.println("Merged game has " + algorithm.getMergedNumberOfSets() + " sets");
        export(root, expander, "Before");
        buildAndExportMergedGame(root, expander, info, "After", algorithm.getMergedKeys());
    }

    private static void kuhnPoker() {
        double epsilon = 1;
        GameState root = new KuhnPokerGameState();
        AlgorithmConfig<InformationSet> config = new CrswfConfig();
        Expander<InformationSet> expander = new KuhnPokerExpander<>(config);

        BasicGameBuilder.build(root, config, expander);
        export(root, expander, "KuhnPoker");
    }

    private static void bpg() {
        double epsilon = 1;
        GameState root = new BPGGameState();
        AlgorithmConfig<InformationSet> config = new CrswfConfig();
        Expander<InformationSet> expander = new BPGExpander<>(config);

        BasicGameBuilder.build(root, config, expander);
        export(root, expander, "BPG");

        CrswfAlgorithm algorithm = new CrswfAlgorithm();
        algorithm.run(root, expander, config, epsilon);
        System.out.printf("%20s|%20s|%20s\n%20f|%20f|%20f",
                "Preprocessing Time", "Buld Time", "Solve Time",
                algorithm.getPreprocessingTime(), algorithm.getBuildTime(), algorithm.getSolveTime());
    }

    /*
    Static methods.
     */


    /**
     * Builds and exports a game previously processed by the CRSWF abstraction algorithm.
     * @param root Root state of the game to be exported.
     * @param expander Expander to be used for export.
     * @param info Game info of the game to be exported.
     * @param filename Name of the file the game should be exported to. This function appends the ".gbt" extension and saves the game
     *                 into the folder "Exports" in the project directory.
     * @param mergedKeys The output from the CRSWF abstraction algorithm describing which information sets should be
     *                   merged.
     */
    private static void buildAndExportMergedGame(GameState root, Expander<InformationSet> expander, GameInfo info,
                                                 String filename, Map<ISKey, CompoundISKey> mergedKeys) {
        MergedGameInfo mergedInfo = new MergedGameInfo(info, mergedKeys);
        GameState mergedRoot = new MergedGameState(root, mergedInfo);
        MergedExpander<InformationSet> mergedExpander = new MergedExpander<>(expander);
        BasicGameBuilder.build(mergedRoot, mergedExpander.getAlgorithmConfig(), mergedExpander);
        export(mergedRoot, mergedExpander, filename);
    }

    /**
     * Exports a given game into a file.
     * @param root Root state of the game to be exported.
     * @param expander Expander to be used for export.
     * @param filename Name of the file the game should be exported to. This function appends the ".gbt" extension and saves the game
     *                 into the folder "Exports" in the project directory.
     */
    private static void export(GameState root, Expander<InformationSet> expander, String filename) {
        GambitEFG exporter = new GambitEFG();
        exporter.write("Exports/" + filename + ".gbt", root, expander, Integer.MAX_VALUE);
    }

    /*
    Main body of the algorithm.
     */

    /*
    Constructor
     */
    public CrswfAlgorithm(boolean scaling, boolean bestLeafMapping, boolean adaptiveEpsilon) {
        this.scaling = scaling;
        this.bestLeafMapping = bestLeafMapping;
        this.adaptiveEpsilon = adaptiveEpsilon;
        maxLeafMappingTries = 1500;
        solveTimeLimit = 60;
        leafsForActions = new HashMap<>();
        levels = new ArrayList<>();
        mergedKeys = new HashMap<>();
        mergedActions = new HashMap<>();
        mergedSomething = false;
        encounteredError = false;
        minUtility = Double.POSITIVE_INFINITY;
        maxUtility = Double.NEGATIVE_INFINITY;
        utilityCorrection = 0;
        solveTime = 0;
        buildTime = 0;
        preprocessingTime = 0;
    }

    public CrswfAlgorithm() {
        this(true, false, true);
    }

    /**
     * Runs the crswf abstraction algorithm.
     * @param root Root of the game
     * @param expander Expander of the game
     * @param config Algorithm config (use CrswfConfig)
     */
    public void run(GameState root, Expander<InformationSet> expander, AlgorithmConfig<? extends InformationSet> config, double epsilon) {
        try {
            if (cplex == null) cplex = new IloCplex();
            cplex.clearModel();
            cplex.setParam(IloCplex.DoubleParam.TiLim, solveTimeLimit);
        } catch (IloException e) {
            e.printStackTrace();
            System.out.println("Failed to create cplex object.");
            encounteredError = true;
            return;
        }
        CrswfInformationSet.resetIDCounter();
        build(root, expander, config, 0, new ArrayList<>(), new ArrayList<>());
        utilityCorrection = -minUtility + 1;
        if (adaptiveEpsilon) epsilon = epsilon*(maxUtility + utilityCorrection);
        for (int level = levels.size() - 1; level > 0; level--) {
            System.out.println("\nStarting work on level " + level);
            if (levels.get(level).contains(null)) continue; //Nature plays on this level

            long beforePreprocessingTime = System.nanoTime();

            Map<InformationSet, Map<Map<PrunedHistory, Integer>, List<Action>>> partitionedActions = new HashMap<>();
            Map<Action, Map<PrunedHistory, List<LeafNode>>> partitionedLeafs = new HashMap<>();
            List<Set<InformationSet>> partitionedSets = partitionSets(level, expander, partitionedActions, partitionedLeafs);
            Map<InformationSet, List<List<Action>>> orderedPartitionedActions = orderPartitionedActions(partitionedSets, partitionedActions);
            Map<Action, List<List<LeafNode>>> orderedPartitionedLeafs = orderPartitionedLeafs(partitionedSets, orderedPartitionedActions, partitionedLeafs);
            countSets(partitionedSets);
            System.out.println("Partitioned sets");

            sortMappings(orderedPartitionedActions, orderedPartitionedLeafs);
            System.out.println("Sorted leafs and actions");

            Map<InformationSet, Map<InformationSet, Double>> distances;
            try {
                distances = computeDistances(partitionedSets, expander, orderedPartitionedActions, orderedPartitionedLeafs);
            } catch (IloException e) {
                e.printStackTrace();
                System.out.println("Could not compute distances");
                encounteredError = true;
                return;
            }
            System.out.println("Computed distances");

            long afterPreprocessingTime = System.nanoTime();
            preprocessingTime += (double) (afterPreprocessingTime - beforePreprocessingTime) / 1000000000;

            Collection<List<InformationSet>> setsToMerge = runILP(partitionedSets, root, expander, level, distances, epsilon);

            if (setsToMerge == null) {
                System.out.println("Cplex failed to solve ILP");
                encounteredError = true;
                continue;
            }

            setsToMerge.forEach(group -> mergeSets(group, orderedPartitionedActions));
        }
        if (!root.isPlayerToMoveNature()) {
            originalNumberOfSets += 1; //ROOT
            mergedNumberOfSets += 1; //ROOT wasn't merged with anything
        }
    }

    private void countSets(Collection<Set<InformationSet>> partitionedSets) {
        for (Set<InformationSet> partition : partitionedSets) {
            originalNumberOfSets += partition.size();
            mergedNumberOfSets += partition.size();
        }
    }

    /**
     * Recursively builds the game. Fills {@see leafsForActions} with all leaf nodes reachable from the given action.
     * @param currentState Game state to be expanded.
     * @param expander Expander used to get actions from currentState
     * @param algorithmConfig Config into which information states are added.
     */
    private void build(GameState currentState, Expander<? extends InformationSet> expander,
                       AlgorithmConfig<? extends InformationSet> algorithmConfig, int level,
                       List<Action> actionList, List<GameState> parentNodeList) {
        if (currentState.isGameEnd()) {
            if (level < levels.size()) throw new IllegalArgumentException("The game has leafs on different levels.");
            rememberActionsLeaf(currentState, actionList, parentNodeList);
            for (double utility : currentState.getUtilities()) {
                minUtility = Math.min(minUtility, utility);
                maxUtility = Math.max(maxUtility, utility);
            }
            return;
        }

        algorithmConfig.addInformationSetFor(currentState);

        addISToLevel(algorithmConfig.getInformationSetFor(currentState), level);

        if (currentState.isPlayerToMoveNature()) {
            numberOfNatureSets += 1;
            originalNumberOfSets += 1;
            mergedNumberOfSets += 1;
        }

        for (Action action : expander.getActions(currentState)) {
            GameState nextState = currentState.performAction(action);

            if (!currentState.isPlayerToMoveNature()) {
                actionList.add(action);
                parentNodeList.add(currentState);
                mergedActions.put(action, action);
            }
            build(nextState, expander, algorithmConfig, level + 1, actionList, parentNodeList);
            if (!currentState.isPlayerToMoveNature()) {
                actionList.remove(actionList.size() - 1);
                parentNodeList.remove(parentNodeList.size() - 1);
            }
        }
    }

    /**
     * Adds given state to sets belonging to all actions in given list.
     * @param currentState State to be added.
     * @param actionList List of all actions from which the given state is reachable.
     * @param parentNodeList List of nodes in which the corresponding action was played.
     */
    private void rememberActionsLeaf(GameState currentState, List<Action> actionList, List<GameState> parentNodeList) {
        for (int i = 0; i < actionList.size(); i++) {
            Action action = actionList.get(i);
            GameState parentNode = parentNodeList.get(i);

            if (!leafsForActions.containsKey(action)) {
                leafsForActions.put(action, new HashSet<>());
            }
            leafsForActions.get(action).add(new LeafNode(currentState, parentNode));
        }
    }

    /**
     * Remembers that the given information set is in the given level in the game tree.
     * @param informationSet Set to be remembered.
     * @param level The level the information set is on.
     */
    private void addISToLevel(InformationSet informationSet, int level) {
        if (levels.size() <= level) {
            levels.add(new HashSet<>());
        }
        levels.get(level).add((CrswfInformationSet) informationSet);
    }

    /**
     * Merges all information sets in the given list into one information set. It does so by inserting the corresponding
     * ISKeys into mergedKeys.
     * @param informationSetGroup Group of information sets to be merged.
     */
    private void mergeSets(List<InformationSet> informationSetGroup, Map<InformationSet, List<List<Action>>> partitionedActions) {
        if (informationSetGroup.size() > 1) mergedSomething = true;
        mergedNumberOfSets -= (informationSetGroup.size() - 1);
        mergeISKeys(informationSetGroup, partitionedActions);
        mergeActions(informationSetGroup, partitionedActions);
    }

    private void mergeISKeys(List<InformationSet> informationSetGroup, Map<InformationSet, List<List<Action>>> partitionedActions) {
        Iterator<InformationSet> it = informationSetGroup.iterator();
        CrswfInformationSet prototype = (CrswfInformationSet) it.next();
        CompoundISKey compoundKey = new CompoundISKey();
        List<ISKey> keys = new ArrayList<>(informationSetGroup.size());
        ISKey key = prototype.getISKey();
        keys.add(key);
        mergedKeys.put(key, compoundKey);
        while (it.hasNext()) {
            CrswfInformationSet informationSet = (CrswfInformationSet) it.next();
            key = informationSet.getISKey();
            keys.add(key);
            mergedKeys.put(key, compoundKey);
            informationSet.mergeWith(prototype);
        }
        compoundKey.addKeys(keys);
    }

    private void mergeActions(List<InformationSet> informationSetGroup, Map<InformationSet, List<List<Action>>> partitionedActions) {
        List<List<Action>> prototypes = partitionedActions.get(informationSetGroup.get(0));
        for (InformationSet informationSet : informationSetGroup) {
            List<List<Action>> actions = partitionedActions.get(informationSet);
            for (int i = 0; i < actions.size(); i++) {
                List<Action> actionPart = actions.get(i);
                for (int j = 0; j < actionPart.size(); j++) {
                    Action originalAction = actionPart.get(j);
                    Action prototypeAction = prototypes.get(i).get(j);
                    mergedActions.put(originalAction, prototypeAction);
                }
            }
        }
    }

    /*
    LP computation
     */

    /**
     * The main method of the algorithm. It builds and runs the MILP encoding of the CRSWF abstraction problem and
     * finds which information set on a given level should be merged.
     * @param partitionedSets A list of information set partitions. The two sets are in the same partition iff they
     *                        satisfy the conditions given in definition of CRSWF games.
     * @param root The root state of the game.
     * @param expander The expander to be used.
     * @param level Level for which the ILP should be built and solved.
     * @param epsilon The upper bound on error the clustering can inflict. Note that the bound is additive over levels.
     * @return A collection of lists of information set. Each list contains information sets which should be merged.
     */
    private Collection<List<InformationSet>> runILP(List<Set<InformationSet>> partitionedSets, GameState root,
                                                   Expander<InformationSet> expander, int level,
                                                    Map<InformationSet, Map<InformationSet, Double>> distances, double epsilon) {
        List<Map<InformationSet, List<IloNumVar>>> clusteringVariables = new ArrayList<>();
        try {
            long beforeBuildTime = System.nanoTime();
            createILP(partitionedSets, clusteringVariables, root, expander, level, epsilon, distances);
            long afterBuildTime = System.nanoTime();
            buildTime += (double) (afterBuildTime - beforeBuildTime) / 1000000000;

            if (DEBUG) cplex.exportModel("LP_level_" + level + ".lp");
            System.out.println("Created ILP");

            double beforeSolveTime = cplex.getCplexTime();
            cplex.solve();
            double afterSolveTime = cplex.getCplexTime();
            solveTime += afterSolveTime - beforeSolveTime;

            System.out.println("Solved ILP");

            if (DEBUG) {
                int partitionNo = 0;
                for (Map<InformationSet, List<IloNumVar>> partitionsClusteringVariables : clusteringVariables) {
                    System.out.println("Partition " + partitionNo++);
                    for (List<IloNumVar> varList : partitionsClusteringVariables.values()) {
                        for (IloNumVar var : varList) {
                            System.out.println(var + " = " + cplex.getValue(var));
                        }
                    }
                }
            }
            return getSetsToMerge(partitionedSets, clusteringVariables, cplex);
        } catch (IloException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extracts information about which sets to be merged from a solved instance of cplex.
     * @param partitionedSets A list of information set partitions. The order should be the same as the order of the
     *                        corresponding clustering variables.
     * @param clusteringVariables A list of clustering variables used to indicate which information sets belong to which
     *                            cluster. The order should be the same as the order of the corresponding set partitions.
     * @param cplex The cplex object containg a solved instance of MILP used in the Crswf Algorithm.
     * @return A collection of lists of information sets. All sets in each lists should be merged.
     * @throws IloException The exception is thrown when cplex cannot read the reward of clustering variables.
     */
    private Collection<List<InformationSet>> getSetsToMerge(List<Set<InformationSet>> partitionedSets,
                                                            List<Map<InformationSet, List<IloNumVar>>> clusteringVariables,
                                                            IloCplex cplex) throws IloException {
        Collection<List<InformationSet>> setsToMerge = new LinkedList<>();
        Iterator<Set<InformationSet>> partitionIterator = partitionedSets.iterator();
        Iterator<Map<InformationSet, List<IloNumVar>>> clusteringVariableIterator = clusteringVariables.iterator();
        while (partitionIterator.hasNext()) {
            Map<Integer, List<InformationSet>> setsToMergeInPartition = new HashMap<>();
            Set<InformationSet> partition = partitionIterator.next();
            Map<InformationSet, List<IloNumVar>> partitionsClusteringVariables = clusteringVariableIterator.next();
            int maxClusterNo = partition.size();
            for (InformationSet informationSet : partition) {
                List<IloNumVar> setsClusteringVariables = partitionsClusteringVariables.get(informationSet);
                for (int cluster = 0; cluster < maxClusterNo; cluster++) {
                    if (cplex.getValue(setsClusteringVariables.get(cluster)) != 0) {
                        if (!setsToMergeInPartition.containsKey(cluster)) setsToMergeInPartition.put(cluster, new LinkedList<>());
                        setsToMergeInPartition.get(cluster).add(informationSet);
                        break;
                    }
                }
            }
            setsToMerge.addAll(setsToMergeInPartition.values());
        }
        return setsToMerge;
    }

    private IloNumVar createILP(Collection<Set<InformationSet>> informationSetPartitioning,
                                List<Map<InformationSet, List<IloNumVar>>> clusteringVariables, GameState root,
                                Expander<InformationSet> expander, int level, double epsilon,
                                Map<InformationSet, Map<InformationSet, Double>> distances) throws IloException {
        cplex.clearModel();

        setVariables = new HashMap<>();
        List<List<IloNumVar>> clusterEmptinessVariables = new ArrayList<>();

        for (Set<InformationSet> partition : informationSetPartitioning) {
            List<IloNumVar> partitionsClusterEmptinessVariables = createClusterEmptinessVariables(partition);
            Map<InformationSet, List<IloNumVar>> partitionsClusteringVariables = createClusteringVariables(partition);
            createConstraints(partitionsClusteringVariables, partitionsClusterEmptinessVariables);
            clusterEmptinessVariables.add(partitionsClusterEmptinessVariables);
            clusteringVariables.add(partitionsClusteringVariables);
        }

        IloNumVar vRoot;
        if (root.isPlayerToMoveNature()) {
            vRoot = buildILPRecNature(root, 0, level, expander,
                                      getPartitioningMap(informationSetPartitioning), mergeMaps(clusteringVariables), distances);
        } else {
            vRoot = buildILPRec(expander.getAlgorithmConfig().getInformationSetFor(root), 0, level, expander,
                                getPartitioningMap(informationSetPartitioning), mergeMaps(clusteringVariables), distances);
        }

        cplex.addLe(vRoot, cplex.constant(epsilon));
        cplex.addMinimize(createEmptinessSum(clusterEmptinessVariables, cplex));
        return vRoot;
    }

    private IloNumExpr createEmptinessSum(List<List<IloNumVar>> clusterEmptinessVariables, IloCplex cplex) throws IloException {
        IloNumExpr sum = cplex.constant(0);
        for (List<IloNumVar> partitionsClusterEmptinessVariables : clusterEmptinessVariables) {
            for (IloNumVar var : partitionsClusterEmptinessVariables) {
                sum = cplex.sum(sum, var);
            }
        }
        return sum;
    }

    private IloNumVar buildILPRec(InformationSet currentSet, int currentLevel, int targetLevel,
                                  Expander<InformationSet> expander, Map<InformationSet, Set<InformationSet>> partitioning,
                                  Map<InformationSet, List<IloNumVar>> clusteringVariables,
                                  Map<InformationSet, Map<InformationSet, Double>> distances) throws IloException {
        if (currentLevel == targetLevel) {
            IloNumVar vI = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "v" + currentSet);
            for (InformationSet informationSet : partitioning.get(currentSet)) {
                int clusterNo = clusteringVariables.get(informationSet).size();
                double distanceValue = distances.get(currentSet).get(informationSet);
                IloNumExpr distance = cplex.constant(distanceValue);
                for (int cluster = 0; cluster < clusterNo; cluster++) {
                    cplex.addGe(vI, cplex.sum(cplex.prod(distance, clusteringVariables.get(currentSet).get(cluster)),
                                            cplex.prod(distance, clusteringVariables.get(informationSet).get(cluster)),
                                            cplex.negative(distance)));
                }
            }
            return vI;
        }

        if (setVariables.containsKey(currentSet)) return setVariables.get(currentSet); //This set was already computed

        IloNumVar vI = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "v" + currentSet);
        Set<InformationSet> visitedSets = new HashSet<>();
        for (Action action : expander.getActions(currentSet)) {
            for (GameState currentState : currentSet.getAllStates()) {
                GameState nextState = currentState.performAction(action);
                IloNumVar nextVar;
                if (nextState.isPlayerToMoveNature()) {
                    nextVar = buildILPRecNature(nextState, currentLevel + 1, targetLevel, expander, partitioning, clusteringVariables, distances);
                } else {
                    InformationSet nextSet = expander.getAlgorithmConfig().getInformationSetFor(nextState);
                    if (visitedSets.contains(nextSet)) continue;
                    visitedSets.add(nextSet);
                    nextVar = buildILPRec(nextSet, currentLevel + 1, targetLevel, expander, partitioning, clusteringVariables, distances);
                }
                cplex.addGe(vI, nextVar);
            }
        }
        setVariables.put(currentSet, vI);
        return vI;
    }

    private IloNumVar buildILPRecNature(GameState currentState, int currentLevel, int targetLevel,
                                        Expander<InformationSet> expander, Map<InformationSet, Set<InformationSet>> partitioning,
                                        Map<InformationSet, List<IloNumVar>> clusteringVariables,
                                        Map<InformationSet, Map<InformationSet, Double>> distances) throws IloException {
        IloNumVar vI = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "v" + currentState);
        Set<InformationSet> visitedSets = new HashSet<>();
        IloNumExpr sum = cplex.constant(0);
        for (Action action : expander.getActions(currentState)) {
            GameState nextState = currentState.performAction(action);
            IloNumVar nextVar;
            if (nextState.isPlayerToMoveNature()) {
                nextVar = buildILPRecNature(nextState, currentLevel + 1, targetLevel, expander, partitioning, clusteringVariables, distances);
            } else {
                InformationSet nextSet = expander.getAlgorithmConfig().getInformationSetFor(nextState);
                if (visitedSets.contains(nextSet)) continue;
                visitedSets.add(nextSet);
                nextVar = buildILPRec(nextSet, currentLevel + 1, targetLevel, expander, partitioning, clusteringVariables, distances);
            }
            sum = cplex.sum(sum, cplex.prod(cplex.constant(currentState.getProbabilityOfNatureFor(action)), nextVar));
        }
        cplex.addGe(vI, sum);
        return vI;
    }

    private Map<InformationSet, List<IloNumVar>> createClusteringVariables(Set<InformationSet> partition) throws IloException {
        Map<InformationSet, List<IloNumVar>> clusteringVariables = new HashMap<>();
        for (InformationSet informationSet : partition) {
            List<IloNumVar> variableList = new ArrayList<>(partition.size());
            for (int cluster = 0; cluster < partition.size(); cluster++) {
                IloNumVar zIk = cplex.boolVar("z" + informationSet + ";" + cluster);
                variableList.add(zIk);
            }
            clusteringVariables.put(informationSet, variableList);
        }
        return clusteringVariables;
    }

    private List<IloNumVar> createClusterEmptinessVariables(Set<InformationSet> partition) throws IloException {
        List<IloNumVar> clusterEmptinessVariables = new ArrayList<>();
        for (int cluster = 0; cluster < partition.size(); cluster++) {
            IloNumVar Sk = cplex.boolVar("S" + cluster);
            clusterEmptinessVariables.add(Sk);
        }
        return clusterEmptinessVariables;
    }

    private void createConstraints(Map<InformationSet, List<IloNumVar>> clusteringVariables, List<IloNumVar> clusterEmptinessVariables) throws IloException {
        int clustersNo = clusteringVariables.size(); //Number of clusters is the same as number of information sets
        for (List<IloNumVar> varsOfInformationSet : clusteringVariables.values()) {
            IloNumExpr informationSetInOneCluster = cplex.constant(0);
            for (int cluster = 0; cluster < clustersNo; cluster++) {
                IloNumVar zIk = varsOfInformationSet.get(cluster);
                informationSetInOneCluster = cplex.sum(informationSetInOneCluster, zIk);

                IloNumVar Sk = clusterEmptinessVariables.get(cluster);
                cplex.addGe(Sk, zIk);
            }
            cplex.addEq(informationSetInOneCluster, cplex.constant(1));
        }
    }

    /*
    Distance computation.
     */
    private Map<InformationSet, Map<InformationSet, Double>> computeDistances(Collection<Set<InformationSet>> partitionedSets,
                                                                                    Expander<InformationSet> expander,
                                                                                    Map<InformationSet, List<List<Action>>> partitionedActions,
                                                                                    Map<Action, List<List<LeafNode>>> partitionedLeafs) throws IloException {
        cplex.end();
        cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, solveTimeLimit);
        cplex.setOut(null);
        Map<InformationSet, Map<InformationSet, Double>> distances = new HashMap<>();
        System.out.println("Number of distances to be computed: " + partitionedSets.stream().mapToInt(Set::size).map(x -> x*x).sum());
        for (Set<InformationSet> partition : partitionedSets) {
            for (InformationSet from : partition) {
                Map<InformationSet, Double> fromMap = new HashMap<>();
                for (InformationSet to : partition) {
                    Map<InformationSet, List<Action>> actionMapping = getSingleListActionMapping(from, to, partitionedActions);
                    fromMap.put(to, computeDistance(from, to, expander, actionMapping, partitionedLeafs));
                }
                distances.put(from, fromMap);
            }
        }
        cplex.setOut(System.out);
        return distances;
    }

    private double computeDistance(InformationSet from, InformationSet to, Expander<InformationSet> expander,
                                   Map<InformationSet, List<Action>> actionMapping,
                                   Map<Action, List<List<LeafNode>>> partitionedLeafs) throws IloException {
        if (from.equals(to)) return 0;
        double distance;
        if (bestLeafMapping) {
            distance = getBestLeafMappingDistance(from, to, partitionedLeafs, actionMapping, expander);
        } else {
            Map<InformationSet, List<List<LeafNode>>> leafMapping = getLeafMapping(from, to, partitionedLeafs, actionMapping);
            Map<LeafNode, LeafNode> bijection = getBijection(from, to, leafMapping);
            distance = computeDistance(from, to, expander, bijection);
        }
        return distance;
    }

    private void sortMappings (Map<InformationSet, List<List<Action>>> partitionedActions,
                               Map<Action, List<List<LeafNode>>> partitionedLeafs) {
        sortLeafs(partitionedLeafs);
        sortActions(partitionedActions, partitionedLeafs);
    }

    private void sortLeafs(Map<Action, List<List<LeafNode>>> partitionedLeafs) {
        for (List<List<LeafNode>> actionsLeafs : partitionedLeafs.values()) {
            for (List<LeafNode> leafList : actionsLeafs) {
                leafList.sort((l1, l2) -> Double.compare(l1.getUtility(), l2.getUtility()));
            }
        }
    }

    private void sortActions(Map<InformationSet, List<List<Action>>> partitionedActions,
                             Map<Action, List<List<LeafNode>>> partitionedLeafs) {
        Map<Action, Double> heuristicValues = new HashMap<>();
        Map<InformationSet, Double> minimums = new HashMap<>();
        partitionedActions.forEach((informationSet, actions) -> minimums.put(informationSet, getSetsMinimumUtility(actions, partitionedLeafs)));
        partitionedLeafs.forEach((action, actionsLeafs) -> heuristicValues.put(action, getHeuristicValue(actionsLeafs, minimums.get(action.getInformationSet()))));
        for (List<List<Action>> setsActions : partitionedActions.values()) {
            for (List<Action> actionList : setsActions) {
                actionList.sort((a1, a2) -> heuristicValues.get(a1).compareTo(heuristicValues.get(a2)));
            }
        }
    }

    private double getSetsMinimumUtility(List<List<Action>> actions, Map<Action, List<List<LeafNode>>> partitionedLeafs) {
        return actions.stream()
                      .flatMap(List::stream)
                      .mapToDouble(action -> getActionsMinimumUtility(partitionedLeafs.get(action)))
                      .min()
                      .getAsDouble();
    }

    private double getActionsMinimumUtility(List<List<LeafNode>> actionsLeafs) {
        return actionsLeafs.stream().mapToDouble(list -> list.get(0).getUtility()).min().getAsDouble();
    }

    private double getActionsMaximumUtility(List<List<LeafNode>> actionsLeafs) {
        return actionsLeafs.stream().mapToDouble(list -> list.get(list.size() - 1).getUtility()).max().getAsDouble();
    }

    private double getHeuristicValue(List<List<LeafNode>> actionsLeafs, double normalization) {
        return actionsLeafs.stream()
                           .flatMap(List::stream)
                           .mapToDouble(leafNode -> leafNode.getUtility()/normalization)
                           .sum();
    }

    private double getBestLeafMappingDistance(InformationSet from, InformationSet to,
                                              Map<Action, List<List<LeafNode>>> partitionedLeafs,
                                              Map<InformationSet, List<Action>> actionMapping,
                                              Expander<InformationSet> expander) throws IloException {
        Map<InformationSet, List<List<LeafNode>>> currentMapping = getLeafMapping(from, to, partitionedLeafs, actionMapping);
        double distance = Double.POSITIVE_INFINITY;
        Permutator permutator = new MappingPermutator<>(currentMapping.get(to));
        if (permutator.getNumberOfPermutations().compareTo(BigInteger.valueOf(maxLeafMappingTries)) > 0) {
            System.out.println("Too much leaf mappings: " + permutator.getNumberOfPermutations());
            permutator = new RandomListPermutator<>(currentMapping.get(to), maxLeafMappingTries);
        }
        while(permutator.hasNext()) {
            Map<LeafNode, LeafNode> bijection = getBijection(from, to, currentMapping);
            distance = Math.min(distance, computeDistance(from, to, expander, bijection));
            permutator.permute();
        }
        return distance;
    }

    private double computeDistance(InformationSet from, InformationSet to, Expander<InformationSet> expander,
                                   Map<LeafNode, LeafNode> bijection) throws IloException {
        if (scaling) return computeScaledDistance(from, to, expander, bijection);
        else return computeUnscaledDistance(from, to, expander, bijection);
    }

    private double computeScaledDistance(InformationSet from, InformationSet to, Expander<InformationSet> expander,
                                         Map<LeafNode, LeafNode> bijection) throws IloException {
        cplex.clearModel();
        Map<GameState, ScaledErrors> errors = computeScaledErrors(from, to, expander, bijection);
        IloNumVar r0Error = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "r0Error");
        IloNumExpr dError = cplex.constant(0);
        for (ScaledErrors stateErrors : errors.values()) {
            cplex.addGe(r0Error, cplex.sum(stateErrors.getLeafProbabilityError(), stateErrors.getRewardError()));
            dError = cplex.sum(dError, cplex.prod(stateErrors.getScaledUtility(), cplex.constant(stateErrors.getDistributionError())));
        }
        cplex.addMinimize(cplex.sum(cplex.prod(cplex.constant(2), r0Error), dError));

        cplex.solve();
        return cplex.getObjValue();
    }

    private double computeUnscaledDistance(InformationSet from, InformationSet to, Expander<InformationSet> expander,
                                           Map<LeafNode, LeafNode> bijection) {
        Map<GameState, UnscaledErrors> errors = computeErrors(from, to, expander, bijection);
        double r0Error = 0;
        double dError = 0;
        for (UnscaledErrors stateErrors : errors.values()) {
            r0Error = Math.max(r0Error, stateErrors.getLeafProbabilityError() + stateErrors.getRewardError());
            dError += stateErrors.getScaledUtility()*stateErrors.getDistributionError();
        }
        return 2*r0Error + dError;
    }

    private Map<GameState, ScaledErrors> computeScaledErrors(InformationSet from, InformationSet to,
                                                             Expander<InformationSet> expander,
                                                             Map<LeafNode, LeafNode> bijection) throws IloException {
        Map<GameState, ScaledErrors> scaledErrors = new HashMap<>();
        for (GameState state : from.getAllStates()) {
            IloNumVar scalingVariable = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "Scaling(" + from + "," + to + ")");
            ScaledErrors errorsForState = computeScaledErrorsRec(state, state, expander, bijection, scalingVariable);
            scaledErrors.put(state, errorsForState);
        }
        return scaledErrors;
    }

    private ScaledErrors computeScaledErrorsRec(GameState state, GameState parentState,
                                                Expander<InformationSet> expander, Map<LeafNode, LeafNode> bijection,
                                                IloNumVar scalingVariable) throws IloException {
        if (state.isGameEnd()) {
            LeafNode leafNode = new LeafNode(state, parentState);
            LeafNode otherLeafNode = bijection.get(leafNode);
            return new ScaledErrors(leafNode, otherLeafNode, expander, utilityCorrection, cplex, scalingVariable);
        }

        ScaledErrors errors = new ScaledErrors(cplex, state);
        for (Action action : expander.getActions(state)) {
            ScaledErrors childErrors = computeScaledErrorsRec(state.performAction(action), parentState, expander, bijection, scalingVariable);
            if (state.isPlayerToMoveNature()) {
                errors.addNatureChildErrors(childErrors, state.getProbabilityOfNatureFor(action), cplex);
            } else {
                errors.addPlayerChildErrors(childErrors, cplex);
            }
        }
        if (state.isPlayerToMoveNature()) errors.finalizeNatureErrors(cplex);
        return errors;
    }

    private Map<GameState, UnscaledErrors> computeErrors(InformationSet from, InformationSet to,
                                                 Expander<InformationSet> expander, Map<LeafNode, LeafNode> bijection) {
        Map<GameState, UnscaledErrors> errors = new HashMap<>();
        for (GameState state : from.getAllStates()) {
            UnscaledErrors errorsForState = computeErrorsRec(state, state, expander, bijection);
            errors.put(state, errorsForState);
        }
        return errors;
    }

    private UnscaledErrors computeErrorsRec(GameState state, GameState parentState, Expander<InformationSet> expander,
                                    Map<LeafNode, LeafNode> leafBijection) {
        if (state.isGameEnd()) {
            LeafNode leafNode = new LeafNode(state, parentState);
            LeafNode otherLeafNode = leafBijection.get(leafNode);
            return new UnscaledErrors(leafNode, otherLeafNode, expander, utilityCorrection);
        }

        UnscaledErrors errors = new UnscaledErrors();
        for (Action action : expander.getActions(state)) {
            UnscaledErrors childErrors = computeErrorsRec(state.performAction(action), parentState, expander, leafBijection);
            if (state.isPlayerToMoveNature()) {
                errors.addNatureChildErrors(childErrors, state.getProbabilityOfNatureFor(action));
            } else {
                errors.addPlayerChildErrors(childErrors);
            }
        }
        return errors;
    }

    /*
    Mapping creation.
     */

    private Map<InformationSet, List<Action>> getSingleListActionMapping(InformationSet from, InformationSet to,
                                                                         Map<InformationSet, List<List<Action>>> partitionedActions) {
        Map<InformationSet, List<Action>> actionMapping = new HashMap<>();
        actionMapping.put(from, partitionedActions.get(from).stream().flatMap(List::stream).collect(Collectors.toList()));
        actionMapping.put(to, partitionedActions.get(to).stream().flatMap(List::stream).collect(Collectors.toList()));
        return actionMapping;
    }

    private Map<InformationSet, List<List<LeafNode>>> getLeafMapping(InformationSet from, InformationSet to,
                                                                     Map<Action, List<List<LeafNode>>> partitionedLeafs,
                                                                     Map<InformationSet, List<Action>> actionMapping) {
        Map<InformationSet, List<List<LeafNode>>> possibleLeafMappings = new HashMap<>();
        possibleLeafMappings.put(from, getLeafMappingForSingleSet(from, partitionedLeafs, actionMapping));
        possibleLeafMappings.put(to, getLeafMappingForSingleSet(to, partitionedLeafs, actionMapping));
        return possibleLeafMappings;
    }

    private List<List<LeafNode>> getLeafMappingForSingleSet(InformationSet informationSet,
                                                            Map<Action, List<List<LeafNode>>> partitionedLeafs,
                                                            Map<InformationSet, List<Action>> actionMapping) {
        List<List<LeafNode>> possibleLeafMappings = new ArrayList<>();
        for (Action action : actionMapping.get(informationSet)) {
            possibleLeafMappings.addAll(partitionedLeafs.get(action));
        }
        return possibleLeafMappings;
    }

    private Map<LeafNode, LeafNode> getBijection(InformationSet from, InformationSet to,
                                                 Map<InformationSet, List<List<LeafNode>>> leafMapping) {
        Map<LeafNode, LeafNode> bijection = new HashMap<>();
        Iterator<List<LeafNode>> fromLeafGroupIterator = leafMapping.get(from).listIterator();
        Iterator<List<LeafNode>> toLeafGroupIterator = leafMapping.get(to).listIterator();
        while (fromLeafGroupIterator.hasNext()) {
            Iterator<LeafNode> fromLeafIterator = fromLeafGroupIterator.next().iterator();
            Iterator<LeafNode> toLeafIterator = toLeafGroupIterator.next().iterator();
            while (fromLeafIterator.hasNext()) {
                LeafNode fromNode = fromLeafIterator.next();
                LeafNode toNode = toLeafIterator.next();
                bijection.put(fromNode, toNode);
                bijection.put(toNode, fromNode);
            }
        }
        return bijection;
    }

    private Map<InformationSet, Set<InformationSet>> getPartitioningMap(Collection<Set<InformationSet>> partitioning) {
        Map<InformationSet, Set<InformationSet>> partitioningMap = new HashMap<>();
        for (Set<InformationSet> partition : partitioning) {
            for (InformationSet informationSet : partition) {
                partitioningMap.put(informationSet, partition);
            }
        }
        return partitioningMap;
    }

    /*
    Partition processing
     */
    private Map<InformationSet, List<List<Action>>> orderPartitionedActions(Collection<Set<InformationSet>> partitionedSets,
                                                                         Map<InformationSet, Map<Map<PrunedHistory, Integer>, List<Action>>> partitionedActions) {
        Map<InformationSet, List<List<Action>>> orderedActions = new HashMap<>();
        for (Set<InformationSet> partition : partitionedSets) {
            orderedActions.putAll(orderPartitionsPartitionedActions(partition, partitionedActions));
        }
        return orderedActions;
    }

    private Map<InformationSet, List<List<Action>>> orderPartitionsPartitionedActions(Set<InformationSet> partition,
                                                                                   Map<InformationSet, Map<Map<PrunedHistory, Integer>, List<Action>>> partitionedActions) {
        Map<InformationSet, List<List<Action>>> orderedActions = new HashMap<>(partition.size());
        partition.forEach(is -> orderedActions.put(is, new ArrayList<>()));
        Set<Map<PrunedHistory, Integer>> keys = partitionedActions.get(partition.iterator().next()).keySet();
        for (Map<PrunedHistory, Integer> key : keys) {
            for (InformationSet informationSet : partition) {
                orderedActions.get(informationSet).add(partitionedActions.get(informationSet).get(key));
            }
        }
        return orderedActions;
    }

    private Map<Action, List<List<LeafNode>>> orderPartitionedLeafs(Collection<Set<InformationSet>> partitionedSets,
                                                                    Map<InformationSet, List<List<Action>>> partitionedActions,
                                                                    Map<Action, Map<PrunedHistory, List<LeafNode>>> partitionedLeafs) {
        Map<Action, List<List<LeafNode>>> orderedLeafs = new HashMap<>();
        for (Set<InformationSet> partition : partitionedSets) {
            orderedLeafs.putAll(orderPartitionsPartitionedLeafs(partition, partitionedActions, partitionedLeafs));
        }
        return orderedLeafs;
    }

    private Map<Action, List<List<LeafNode>>> orderPartitionsPartitionedLeafs(Set<InformationSet> partition,
                                                                              Map<InformationSet, List<List<Action>>> partitionedActions,
                                                                              Map<Action, Map<PrunedHistory, List<LeafNode>>> partitionedLeafs) {
        Map<Action, List<List<LeafNode>>> orderedLeafs = new HashMap<>();
        List<List<PrunedHistory>> keyList = new ArrayList<>();
        int actionListSize = partitionedActions.get(partition.iterator().next()).size();
        for (int i = 0; i < actionListSize; i++) keyList.add(new ArrayList<>());
        for (InformationSet informationSet : partition) {
            orderedLeafs.putAll(orderSetsPartitionedLeafs(partitionedActions.get(informationSet), partitionedLeafs, keyList));
        }
        return orderedLeafs;
    }

    private Map<Action, List<List<LeafNode>>> orderSetsPartitionedLeafs(List<List<Action>> setsPartitionedActions,
                                                                        Map<Action, Map<PrunedHistory, List<LeafNode>>> partitionedLeafs,
                                                                        List<List<PrunedHistory>> keyList) {
        Map<Action, List<List<LeafNode>>> orderedLeafs = new HashMap<>();
        Iterator<List<PrunedHistory>> keyIterator = keyList.iterator();
        for (List<Action> actionGroup : setsPartitionedActions) {
            orderedLeafs.putAll(orderActionGroupsPartitionedLeafs(actionGroup, partitionedLeafs, keyIterator.next()));
        }
        return orderedLeafs;
    }

    private Map<Action, List<List<LeafNode>>> orderActionGroupsPartitionedLeafs(List<Action> actionGroup,
                                                                                Map<Action, Map<PrunedHistory, List<LeafNode>>> partitionedLeafs,
                                                                                List<PrunedHistory> keys) {
        Map<Action, List<List<LeafNode>>> orderedLeafs = new HashMap<>();
        actionGroup.forEach(a -> orderedLeafs.put(a, new ArrayList<>()));
        if (keys.isEmpty()) keys.addAll(partitionedLeafs.get(actionGroup.iterator().next()).keySet());
        for (PrunedHistory key : keys) {
            for (Action action : actionGroup) {
                orderedLeafs.get(action).add(partitionedLeafs.get(action).get(key));
            }
        }
        return orderedLeafs;
    }

    /*
    Set partitioning.
     */

    private List<Set<InformationSet>> partitionSets(int level,
                                                    Expander<InformationSet> expander,
                                                    Map<InformationSet, Map<Map<PrunedHistory, Integer>, List<Action>>> mappableActions,
                                                    Map<Action, Map<PrunedHistory, List<LeafNode>>> mappableLeafs) {
        Set<CrswfInformationSet> sets = levels.get(level);
        Map<Map<Map<PrunedHistory, Integer>, Integer>, Set<InformationSet>> partitionedSets = new HashMap<>();
        for (CrswfInformationSet informationSet : sets) {
            List<Action> actions = expander.getActions(informationSet);
            Map<Action, Map<PrunedHistory, List<LeafNode>>> partitionedLeafs = partitionLeafsByHistories(actions);
            mappableLeafs.putAll(partitionedLeafs);
            Map<Map<PrunedHistory, Integer>, List<Action>> partitionedActions = partitionActions(actions, partitionedLeafs);
            mappableActions.put(informationSet, partitionedActions);
            Map<Map<PrunedHistory, Integer>, Integer> sizeMap = getSizeMap(partitionedActions);
            if(!partitionedSets.containsKey(sizeMap)) partitionedSets.put(sizeMap, new HashSet<>());
            partitionedSets.get(sizeMap).add(informationSet);
        }
        return new LinkedList<>(partitionedSets.values());
    }

    private Map<Map<PrunedHistory, Integer>, List<Action>> partitionActions(List<Action> actions,
                                                                           Map<Action, Map<PrunedHistory, List<LeafNode>>> partitionedLeafs) {
        Map<Map<PrunedHistory, Integer>, List<Action>> partitionedActions = new HashMap<>();
        for (Action action : actions) {
            Map<PrunedHistory, Integer> sizeMap = getSizeMap(partitionedLeafs.get(action));
            if (!partitionedActions.containsKey(sizeMap)) partitionedActions.put(sizeMap, new ArrayList<>());
            partitionedActions.get(sizeMap).add(action);
        }
        return partitionedActions;
    }

    private Map<Action, Map<PrunedHistory, List<LeafNode>>> partitionLeafsByHistories(List<Action> actions) {
        Map<Action, Map<PrunedHistory, List<LeafNode>>> partitionedLeafs = new HashMap<>();
        for (Action action : actions) {
            Map<PrunedHistory, List<LeafNode>> partitionedLeafsForAction = new HashMap<>();
            for (LeafNode leaf : leafsForActions.get(action)) {
                PrunedHistory prunedHistory = new PrunedHistory(leaf.getState(), leaf.getParentState(), mergedActions);
                if (!partitionedLeafsForAction.containsKey(prunedHistory)) {
                    partitionedLeafsForAction.put(prunedHistory, new ArrayList<>());
                }
                partitionedLeafsForAction.get(prunedHistory).add(leaf);
            }
            partitionedLeafs.put(action, partitionedLeafsForAction);
        }
        return partitionedLeafs;
    }

    private <T, C extends Collection> Map<T, Integer> getSizeMap(Map<T, C> map) {
        Map<T, Integer> sizeMap = new HashMap<>();
        map.forEach((key, value) -> sizeMap.put(key, value.size()));
        return sizeMap;
    }


    private <K, V> Map<K, V> mergeMaps(Collection<Map<K, V>> maps) {
        Map<K, V> mergedMap = new HashMap<>();
        maps.forEach(mergedMap::putAll);
        return mergedMap;
    }

    /*
    Getters and setters
     */

    public boolean isScaled() {
        return scaling;
    }

    public void setScaling(boolean scaling) {
        this.scaling = scaling;
    }

    public int getMaxLeafMappingTries() {
        return maxLeafMappingTries;
    }

    public void setMaxLeafMappingTries(int maxLeafMappingTries) {
        this.maxLeafMappingTries = maxLeafMappingTries;
    }

    public boolean mergedSomething() {
        return mergedSomething;
    }

    public double getSolveTime() {
        return solveTime;
    }

    public double getPreprocessingTime() {
        return preprocessingTime;
    }

    public double getBuildTime() {
        return buildTime;
    }

    public Map<ISKey, CompoundISKey> getMergedKeys() {
        return mergedKeys;
    }

    public Map<Action, Action> getMergedActions() {
        return mergedActions;
    }

    public boolean isBestLeafMapping() {
        return bestLeafMapping;
    }

    public void setBestLeafMapping(boolean bestLeafMapping) {
        this.bestLeafMapping = bestLeafMapping;
    }

    public long getOriginalNumberOfSets() {
        return originalNumberOfSets;
    }

    public long getMergedNumberOfSets() {
        return mergedNumberOfSets;
    }

    public double getSolveTimeLimit() {
        return solveTimeLimit;
    }

    public void setSolveTimeLimit(double solveTimeLimit) {
        this.solveTimeLimit = solveTimeLimit;
    }

    public boolean encounteredError() {
        return encounteredError;
    }

    public boolean isAdaptiveEpsilon() {
        return adaptiveEpsilon;
    }

    public double getMaxUtility() {
        return maxUtility + utilityCorrection;
    }
}
