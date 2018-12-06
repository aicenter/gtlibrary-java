package cz.agents.gtlibrary.algorithms.cr;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInnerNode;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceGameState;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PSKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.Collectors;

import static cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm.collectCFRResolvingData;
import static cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm.updateCFRResolvingData;
import static cz.agents.gtlibrary.algorithms.cr.CRAlgorithm.Budget.BUDGET_NUM_SAMPLES;
import static cz.agents.gtlibrary.algorithms.cr.CRAlgorithm.Budget.BUDGET_TIME;
import static cz.agents.gtlibrary.algorithms.cr.CRExperiments.*;
import static cz.agents.gtlibrary.algorithms.cr.ResolvingMethod.RESOLVE_MCCFR;

// Continual Resolving algorithm
public class CRAlgorithm implements GamePlayingAlgorithm {

    public static double totalTimeResolving = 0.;
    public static double totalTimeRoot = 0.;
    private final GameState rootState;
    private final Expander<MCTSInformationSet> expander;
    private final ThreadMXBean threadBean;
    private final MCTSConfig config;
    public ResolvingMethod defaultResolvingMethod = RESOLVE_MCCFR;
    public ResolvingMethod defaultRootMethod = RESOLVE_MCCFR;
    public Budget budgetRoot = BUDGET_NUM_SAMPLES;
    public Budget budgetGadget = BUDGET_NUM_SAMPLES;
    public CFRData rootCfrData;
    public CFRData gadgetCfrData;
    public boolean gadgetIterationsCountFollow = false;
    private Random rnd;
    private double epsilonExploration = 0.6;
    private boolean resetData = true;

    private Player defaultResolvingPlayer;
    private MCTSInformationSet currentIs;
    private InnerNode rootNode;
    private boolean giveUp = false;
    public boolean deallocate = System.getenv("deallocate") != null && System.getenv("deallocate").equals("true");
    private boolean writeEFG = System.getenv("writeEFG") != null && System.getenv("writeEFG").equals("true");
    private boolean skipGadgetResolvingIsMCCFR = System.getenv("skipGadget") != null && System.getenv("skipGadget").equals("true");
    private double actionChosenWithProb = 1.;
    private double[] currentISprobDist;
    public static int buildResolvingGadget = 0;
    private String extraInfo = "";
    private boolean debug = new Boolean(getenv("debug", "false"));
    public static int seed = 49;

    public CRAlgorithm(GameState rootState, Expander<MCTSInformationSet> expander) {
        this(rootState, expander, 0.6);
    }

    public CRAlgorithm(Player resolvingPlayer, GameState rootState, Expander<MCTSInformationSet> expander, double epsilonExploration) {
        this.rootState = rootState;
        this.expander = expander;
        this.config = ((MCTSConfig) expander.getAlgorithmConfig());
        this.rnd = config.getRandom();
        this.epsilonExploration = epsilonExploration;
        this.defaultResolvingPlayer = resolvingPlayer;
        this.config.useEpsilonRM = false;
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public CRAlgorithm(GameState rootState, Expander<MCTSInformationSet> expander, double epsilonExploration) {
        this(rootState.getAllPlayers()[0], rootState, expander, epsilonExploration);
    }

    public CRAlgorithm(InnerNode rootNode, Expander expander) {
        this(rootNode, expander, 0.6);
    }

    public CRAlgorithm(InnerNode rootNode, Expander<MCTSInformationSet> expander, double epsilonExploration) {
        this(rootNode.getGameState(), expander, epsilonExploration);
    }

    public Expander<MCTSInformationSet> getExpander() {
        return expander;
    }

    public MCTSConfig getConfig() {
        return config;
    }

    private InnerNode buildRootNode() {
        InnerNode rootNode;
        if (rootState.isPlayerToMoveNature()) {
            rootNode = new ChanceNodeImpl(expander, rootState, this.rnd);
        } else {
            rootNode = new InnerNodeImpl(expander, rootState);
        }
        return rootNode;
    }

    public void runRoot(Player resolvingPlayer, InnerNode rootNode, int iterationsInRoot) {
        runRoot(defaultRootMethod, resolvingPlayer, rootNode, iterationsInRoot);
    }

    public void runRoot(ResolvingMethod rootResolveMethod,
                        Player resolvingPlayer,
                        InnerNode rootNode,
                        int iterationsInRoot) {
        long start = threadBean.getCurrentThreadCpuTime();

        int samples = 0;
        switch (rootResolveMethod) {
            case RESOLVE_MCCFR:
                samples = runRootMCCFR(resolvingPlayer, rootNode, iterationsInRoot);
                break;
            case RESOLVE_CFR:
                samples = runRootCFR(resolvingPlayer, rootNode, iterationsInRoot);
        }

        double diff = (threadBean.getCurrentThreadCpuTime() - start) / 1e6;
        totalTimeRoot += diff;
        System.err.println("root iters in " + diff + " ms");

        if (rootNode.getPlayerToMove().equals(resolvingPlayer)) {
            rootNode.getPublicState().incrResolvingIterations(samples);
            rootNode.getPublicState().setResolvingMethod(rootResolveMethod);
        }

        final int finalSamples = samples;
        rootNode.getPublicState().getNextPlayerPublicStates(resolvingPlayer).forEach(nextPs -> {
            nextPs.incrResolvingIterations(finalSamples);
            nextPs.setResolvingMethod(rootResolveMethod);
        });

        updatePlayerRp(rootNode.getPublicState(), resolvingPlayer);
    }

    public Action runStep(Player resolvingPlayer, MCTSInformationSet curIS, int iterationsPerGadgetGame) {
        return runStep(resolvingPlayer, curIS, defaultResolvingMethod, iterationsPerGadgetGame);
    }

    public Action runStep(Player resolvingPlayer,
                          MCTSInformationSet curIS,
                          ResolvingMethod resolvingMethod,
                          int iterationsPerGadgetGame) {
        if (!curIS.getPlayer().equals(resolvingPlayer)) {
            throw new RuntimeException("Cannot resolve in public state that does not belong to the player!");
        }

        Action action;

        PublicState curPS = curIS.getPublicState();
        PublicState parentPS = curPS.getPlayerParentPublicState();

        System.err.println("--------------------------");
        System.err.println("Resolving " +
                "\n\tPS: " + curPS + " (parent " + parentPS + ") " +
                "\n\tIS: " + curIS + " " +
                "\n\t" + curPS.getAllNodes().size() + " nodes, " +
                curPS.getAllInformationSets().size() + " infosets in public state");

        // optimization to speed up resolvings in nice games
        // most public states are at the end of the public tree, but if they have only
        // one action and it is one round before the end of the game it is senseless to resolve here
        // so we can speed up the entire resolving about 2x => faster experiments!
        boolean skipResolving = false;
        if (isNiceGame(curIS.getAllStates().iterator().next())) {
            int maxNumActionsAtPs = curPS.getAllInformationSets().stream().map(
                    is -> {
                        is.getActions();
                        return is.getActions().size();
                    }).max(Integer::compareTo).get();

            if(maxNumActionsAtPs == 1) {
                skipResolving = true;
            }
        }

        if (skipResolving) {
            System.err.println("Only one action possible, skipping resolving");
            action = curIS.getActions().iterator().next();
            actionChosenWithProb = 1.;
        } else {
            Map<Action, Double> distributionBefore = getDistributionFor(curIS.getAlgorithmData());

            // Resolve!
            resolveGadgetGame(resolvingPlayer, curPS, resolvingMethod, iterationsPerGadgetGame);

            Map<Action, Double> distributionAfter = getDistributionFor(curIS.getAlgorithmData());
            Map<Action, Double> diff = distributionBefore.keySet().stream().collect(Collectors.toMap(
                    entry -> entry,
                    distAction -> distributionBefore.get(distAction) - distributionAfter.get(distAction)));

            System.err.println("Before: " + distributionToString(curIS.getActions(), distributionBefore));
            System.err.println("After:  " + distributionToString(curIS.getActions(), distributionAfter));
            System.err.println("Diff:   " + distributionToString(curIS.getActions(), diff));
            action = randomChoice(distributionAfter);
            actionChosenWithProb = distributionAfter.get(action);
            currentISprobDist = ((OOSAlgorithmData) curIS.getAlgorithmData()).getMeanStrategy();
        }

        updatePlayerRp(curPS, resolvingPlayer);

        assert curIS.getActions().contains(action);
        return action;
    }

    public InnerNode solveEntireGame(Player resolvingPlayer, int iterationsInRoot, int iterationsPerGadgetGame) {
        System.err.println("Using " +
                "iterationsInRoot=" + iterationsInRoot + " " +
                "iterationsPerGadgetGame=" + iterationsPerGadgetGame + " " +
                "epsilonExploration=" + epsilonExploration + " " +
                "resetData=" + resetData + " " +
                "gadgetDelta=" + OOSAlgorithm.gadgetDelta + " " +
                "safeResolving=" + safeResolving + " " +
                "resolvingCFV=" + GadgetInnerNode.resolvingCFV + " " +
                "player=" + resolvingPlayer.getId() + " ");

        InnerNode solvingRoot;
        if (rootState.isPlayerToMoveNature()) {
            solvingRoot = new ChanceNodeImpl(expander, rootState, this.rnd);
        } else {
            solvingRoot = new InnerNodeImpl(expander, rootState);
        }
        // to able to calc best response, we need to have the whole tree built
        buildCompleteTree(solvingRoot);

        if (iterationsInRoot < 2) { // no root init
            throw new RuntimeException("Cannot skip root initialization!");
        }
        runRoot(resolvingPlayer, solvingRoot, iterationsInRoot);

        if (iterationsPerGadgetGame < 2) { // uniform resolving
            System.err.println("Skipping resolving.");
            if(skipGadgetResolvingIsMCCFR) return solvingRoot;
        }
        ArrayDeque<PublicState> q = new ArrayDeque<>();
        PublicState maybePlayerRootPs = getRootNode().getPublicState();
        if(maybePlayerRootPs.getPlayer().getId() == resolvingPlayer.getId()) {
            q.add(maybePlayerRootPs); // it really is player's root ps
        } else {
            q.addAll(maybePlayerRootPs.getNextPlayerPublicStates(resolvingPlayer));
        }
        while (!q.isEmpty()) {
            PublicState s = q.removeFirst();

            if (!s.isReachable(resolvingPlayer)) {
                // If public state is not reachable by our player, we can leave whatever strategy was there.
                System.err.println("Skipping resolving public state " + s + " - not reachable.");
                continue;
            }

            q.addAll(s.getNextPlayerPublicStates(resolvingPlayer));
            // don't resolve in chance public states
            if (s.getAllNodes().iterator().next() instanceof ChanceNode) continue;

            MCTSInformationSet is = s.getAllInformationSets().iterator().next();
            runStep(resolvingPlayer, is, iterationsPerGadgetGame);
            System.out.println(">>>"+seed+";"+s+";"+numSamplesDuringRun+";"+numSamplesInCurrentIS+";"+numNodesTouchedDuringRun);
        }

        return solvingRoot;
    }

    public static void updatePlayerRp(PublicState ps, Player updatingPlayer, Set<InnerNode> nextPsNodesBarrier) {
        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.addAll(ps.getAllNodes());

        while (!q.isEmpty()) {
            InnerNode curNode = q.removeFirst();

            Map<Action, Double> avgStrategy = null;
            if (!(curNode instanceof ChanceNode)) {
                avgStrategy = getDistributionFor(curNode.getInformationSet().getAlgorithmData());
            }

            for (Action action : curNode.getActions()) {
                Node nextNode = curNode.getChildOrNull(action);
                if (nextNode == null || nextNode instanceof LeafNode) continue;

                Double pl0_pA = 1.0;
                Double pl1_pA = 1.0;
                if (avgStrategy != null) {
                    if(curNode.getPlayerToMove().getId() == 0) {
                        pl0_pA = avgStrategy.get(action);
                    } else if(curNode.getPlayerToMove().getId() == 1) {
                        pl1_pA = avgStrategy.get(action);
                    } else {
                        throw new IllegalStateException();
                    }
                }

                InnerNode nextInner = (InnerNode) nextNode;
                nextInner.setReachPrByPlayer(0, curNode.getReachPrByPlayer(0) * pl0_pA);
                nextInner.setReachPrByPlayer(1, curNode.getReachPrByPlayer(1) * pl1_pA);
                if (!nextPsNodesBarrier.contains(nextInner)) {
                    q.add(nextInner);
                }
            }
        }
    }

    public static void updatePlayerRp(PublicState ps, Player updatingPlayer) {
        // Top-down update of reach probabilities.
        //
        // From current public state until next public states of the player,
        // update reach probabilities of each node.
        //
        // Basically, we know that between the public states both players
        // will play the resolved average strategy.
        System.err.println("Updating reach probabilities");

        Set<InnerNode> nextPsNodesBarrier = new HashSet<>();
        ps.getNextPlayerPublicStates(updatingPlayer).stream()
                .map(PublicState::getAllNodes)
                .forEach(nextPsNodesBarrier::addAll);
        updatePlayerRp(ps, updatingPlayer, nextPsNodesBarrier);
    }

    private void resolveGadgetGame(Player resolvingPlayer,
                                   PublicState publicState,
                                   ResolvingMethod resolvingMethod,
                                   int iterationsPerGadgetGame) {
        System.err.println("Building gadget");
        Subgame subgame = publicState.getSubgame(currentIs);
        GadgetChanceNode gadgetRootNode = subgame.getGadgetRoot();
        if(subgame.getGadgetNodes().size() < buildResolvingGadget) {
            System.err.println("Building next nodes");
            publicState.getNextPlayerPublicStates(resolvingPlayer, true);
        }

        if(debug) writeExtraInfo(gadgetRootNode);

        if (resetData && !isPublicTreeRootKeeping(publicState)) {
            System.err.println("Resetting data");
            publicState.resetData(true);
            publicState.setDataKeeping(false);
        } else {
            System.err.println("Keeping data");
            publicState.setDataKeeping(true);
        }

        if(writeEFG) {
            GambitEFG gambit = new GambitEFG();
            gambit.wISKeys = true;
            gambit.write(
                    expander.getClass().getSimpleName() + "_PS_" + publicState.getPSKey().hashCode() + ".gbt",
                    gadgetRootNode,
                    false);
        }

        runGadget(resolvingMethod, resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);
    }

    private void writeExtraInfo(GadgetChanceNode gadgetRootNode) {
        extraInfo = "";
        if(currentIs != null) {
            OOSAlgorithmData data = (OOSAlgorithmData) (currentIs.getAlgorithmData());
            extraInfo += "p_dist_before: " + stratToString(data.getMeanStrategy())+"\n";
        }

        gadgetRootNode.getResolvingInnerNodes().values().forEach(gin -> {
            extraInfo += gin.toString() +": "+String.format("% 2.4f", gin.getChanceReachPr())+", ";
            if(gin.getActions().size() == 2) extraInfo+= String.format("% 2.4f", gin.getTerminateNode().getUtilities()[0]);
            extraInfo += " ~ ";
        });
    }


    private void runGadget(ResolvingMethod resolvingMethod,
                           Player resolvingPlayer,
                           PublicState publicState,
                           GadgetChanceNode gadgetRootNode,
                           int iterationsPerGadgetGame) {

        System.err.println("Public state reach pr by this player+chance: "+gadgetRootNode.getRootReachPr());
        System.err.println("Using " + resolvingMethod + " for resolving gadget " +
                "with " + iterationsPerGadgetGame + " " + (budgetGadget == BUDGET_NUM_SAMPLES ? "samples" : "milisec"));

        long start = threadBean.getCurrentThreadCpuTime();
        int iterations = 0;
        switch (resolvingMethod) {
            case RESOLVE_CFR:
                iterations = runGadgetCFR(resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);
                break;
            case RESOLVE_MCCFR:
                iterations = runGadgetMCCFR(resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);
                break;
            case RESOLVE_UNIFORM:
                runGadgetUniform(resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);
                break;
        }
        double diff = (threadBean.getCurrentThreadCpuTime() - start) / 1e6;
        System.err.println("resolved in " + diff + " ms using "+iterations + " iterations");
        totalTimeResolving += diff;

        final Integer totalIterations = iterations;
        publicState.getNextPlayerPublicStates().forEach(ps -> {
            ps.incrResolvingIterations(totalIterations);
            ps.setResolvingMethod(resolvingMethod);
        });
    }

    private boolean isPublicTreeRootKeeping(PublicState publicState) {
        InnerNode aNode = publicState.getAllNodes().iterator().next();

        // todo: chance is in the root, but it's outcomes are hidden, therefore PS spans the entire level
        return publicState.getAllNodes().size() == 1
                && aNode.getReachPr() == 1.
                && (aNode.getParent() == null
                || aNode.getParent() instanceof ChanceNode // there is a trivial chance node with probability 1
        );
    }

    public int runRootMCCFR(Player resolvingPlayer, InnerNode rootNode, int iterations) {
        System.err.println("Calculating initial strategy from root using MCCFR in " + iterations
                + " "  + (budgetRoot == BUDGET_NUM_SAMPLES ? "samples" : "milisec"));
        OOSAlgorithm alg = new OOSAlgorithm(resolvingPlayer, rootNode, epsilonExploration);
        alg.setRnd(rnd);

        if (budgetRoot == BUDGET_TIME) {
            alg.runMiliseconds(iterations);
        } else {
            assert budgetRoot == BUDGET_NUM_SAMPLES;
            alg.runIterations(iterations);
        }

        numSamplesDuringRun = alg.numSamplesDuringRun();
        numSamplesInCurrentIS = alg.numSamplesInCurrentIS();
        numNodesTouchedDuringRun = alg.numNodesTouchedDuringRun();

        return alg.numSamplesDuringRun();
        // debug
//        assert debugDepthSamplingAssert(rootNode, alg) == iterations / 2;
    }

    public int runRootCFR(Player resolvingPlayer, InnerNode rootNode, int iterations) {
        System.err.println("Calculating initial strategy from root using CFR in " + iterations + " iterations");
        CFRAlgorithm alg = new CFRAlgorithm(rootNode);

        if(budgetRoot == BUDGET_TIME) {
            alg.runMiliseconds(iterations);
        } else {
            assert budgetRoot == BUDGET_NUM_SAMPLES;
            alg.runIterations(iterations);
        }

        PublicState publicState = rootNode.getPublicState();

        // get values needed for next resolving
        if (publicState.getPlayer().equals(resolvingPlayer)) {
            rootCfrData = collectCFRResolvingData(publicState);
        } else {
            rootCfrData = collectCFRResolvingData(publicState.getNextPlayerPublicStates(resolvingPlayer));
        }

        if (publicState.getPlayer().equals(resolvingPlayer)) {
            updateCFRResolvingData(publicState, rootCfrData.reachProbs, rootCfrData.historyExpValues);
        }
        publicState.getNextPlayerPublicStates(resolvingPlayer).forEach(ps -> {
            updateCFRResolvingData(ps, rootCfrData.reachProbs, rootCfrData.historyExpValues);
        });

        return alg.iters;
    }

    private int runGadgetMCCFR(Player resolvingPlayer,
                               PublicState publicState,
                               GadgetChanceNode gadgetRoot,
                               int iterations) {
        OOSAlgorithm alg = new OOSAlgorithm(resolvingPlayer, gadgetRoot, epsilonExploration);
        alg.setRnd(rnd);
        alg.setTrackingIS(currentIs);

        if (budgetGadget == BUDGET_TIME) {
            alg.runMiliseconds(iterations);
        } else {
            assert budgetGadget == BUDGET_NUM_SAMPLES;
            alg.runIterations(iterations);
        }

        numSamplesDuringRun = alg.numSamplesDuringRun();
        numSamplesInCurrentIS = alg.numSamplesInCurrentIS();
        numNodesTouchedDuringRun = alg.numNodesTouchedDuringRun();

        return alg.numSamplesDuringRun();
    }

    private int runGadgetCFR(Player resolvingPlayer,
                              PublicState publicState,
                              GadgetChanceNode gadgetRootNode,
                              int iterations) {
        buildCompleteTree(gadgetRootNode);

        CFRAlgorithm alg = new CFRAlgorithm(gadgetRootNode);
        alg.runIterations(iterations);

        if (budgetGadget == BUDGET_TIME) {
            alg.runMiliseconds(iterations);
        } else {
            assert budgetGadget == BUDGET_NUM_SAMPLES;
            alg.runIterations(iterations);
        }

        // update values needed for next resolving
        gadgetCfrData = collectCFRResolvingData(publicState);
        publicState.getNextPlayerPublicStates().forEach(ps -> {
            updateCFRResolvingData(ps, gadgetCfrData.reachProbs, gadgetCfrData.historyExpValues);
        });

        return alg.iters;
    }

    /**
     * Not intended for general use. Overwrite strategy by random numbers.
     */
    private void runGadgetUniform(Player resolvingPlayer,
                                  PublicState publicState,
                                  GadgetChanceNode gadgetRootNode,
                                  int iterationsPerGadgetGame) {
        buildCompleteTree(gadgetRootNode);

        Random rnd = new Random(123456);
        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.addAll(publicState.getAllNodes());
        while (!q.isEmpty()) {
            InnerNode n = q.removeFirst();
            if (!(n instanceof ChanceNode)) {
                MCTSInformationSet is = n.getInformationSet();
                OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

                double[] mp = data.getMp();
                for (int i = 0; i < mp.length; i++) {
                    mp[i] = rnd.nextDouble();
                }

            } else { // expand chance nodes
                for (Action a : n.getActions()) {
                    Node ch = n.getChildFor(a);
                    if (ch instanceof InnerNode) {
                        q.add((InnerNode) ch);
                    }
                }
            }
        }


    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        throw new NotImplementedException();
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        budgetGadget = BUDGET_TIME;
        budgetRoot = BUDGET_TIME;
        if(currentIs == null) {
            runRoot(RESOLVE_MCCFR, defaultResolvingPlayer, getRootNode(), miliseconds);
            return null;
        } else {
            if (currentIs.getAllNodes().isEmpty()) {
                giveUp = true;
                return null;
            }

            Action a = runStep(defaultResolvingPlayer, currentIs, RESOLVE_MCCFR, miliseconds);
            if(deallocate) deallocateNonrelevantNodes(currentIs);
            return a;
        }
    }

    @Override
    public Action runIterations(int iterations) {
        budgetGadget = BUDGET_NUM_SAMPLES;
        budgetRoot = BUDGET_NUM_SAMPLES;
        if(currentIs == null) {
            runRoot(RESOLVE_MCCFR, defaultResolvingPlayer, getRootNode(), iterations);
            return null;
        } else {
            if (currentIs.getAllNodes().isEmpty()) {
                giveUp = true;
                return null;
            }

            Action a = runStep(defaultResolvingPlayer, currentIs, RESOLVE_MCCFR, iterations);
            if(deallocate) deallocateNonrelevantNodes(currentIs);
            return a;
        }
    }

    @Override
    public void setCurrentIS(InformationSet is) {
        currentIs = (MCTSInformationSet) is;
    }

    @Override
    public Double actionChosenWithProb() {
        return actionChosenWithProb;
    }

    private void deallocateNonrelevantNodes(MCTSInformationSet is) {
        // well ... "deallocate" - just set null pointers and hope GC will clean it up
        ArrayDeque<InnerNode> q = new ArrayDeque();

        Set<InnerNode> startNodes = new HashSet<>(is.getPublicState().getAllNodes());
        Set<InnerNode> nonremovalNodes = new HashSet<>();
        Set<ISKey> nonremovalIS = new HashSet<>();
        Set<PSKey> nonremovalPS = new HashSet<>();

        nonremovalNodes.addAll(startNodes);
        nonremovalNodes.addAll(is.getAllNodes());
        nonremovalIS.addAll(nonremovalNodes.stream()
                .map(in->in.getInformationSet().getISKey())
                .collect(Collectors.toSet()));
        nonremovalPS.add(is.getPublicState().getPSKey());

        q.addAll(startNodes);
        // prepare non-removal nodes
        while (!q.isEmpty()) {
            InnerNodeImpl curNode = (InnerNodeImpl) q.removeFirst();

            // add parent to keep
            if (curNode.getParent() != null) {
                InnerNode parNode = curNode.getParent();
                q.add(parNode);
                nonremovalNodes.add(parNode);
                if(parNode.getInformationSet()!=null) {
                    nonremovalIS.add(parNode.getInformationSet().getISKey());
                }
                nonremovalPS.add(parNode.getPublicState().getPSKey());
            }
        }

        // destroy nodes
        q.addAll(startNodes);
        int deallocatedIS = 0;
        int deallocatedPS = 0;

        while (!q.isEmpty()) {
            InnerNodeImpl curNode = (InnerNodeImpl) q.removeFirst();

            if(curNode.destroyed) continue;

            // add parent to keep
            if(curNode.getParent() != null) {
                q.add(curNode.getParent());
            }

            // add child nodes that do not go through nonremoval nodes
            if(curNode.getChildren() != null && !startNodes.contains(curNode)) {
                curNode.getChildren().forEach((a, nextNode) -> {
                    if (nextNode instanceof InnerNodeImpl
                            && !((InnerNodeImpl) nextNode).destroyed // is not destroyed already
                            && !nonremovalNodes.contains(nextNode)
                            ) {
                        q.add((InnerNode) nextNode);
                    }
                });
            }

            if(nonremovalNodes.contains(curNode)) {
                continue;
            }

            // otherwise remove!
            MCTSPublicState curPS = curNode.getPublicState();
            if(curPS != null && !curPS.destroyed && !nonremovalPS.contains(curPS.getPSKey())) {
                deallocatedPS++;
                config.getPsKeyPublicStates().remove(curPS.getPSKey());
                curPS.destroy();
                curPS = null;
            }

            MCTSInformationSet curIS = curNode.getInformationSet();
            if(curIS != null && !curIS.destroyed && !nonremovalIS.contains(curIS.getISKey())) {
                deallocatedIS++;
                config.getAllInformationSets().remove(curIS.getISKey());
                curIS.destroy();
                curIS = null;
            }

            curNode.destroy();
            curNode = null;
        }

        System.err.println("Deallocated PS: "+deallocatedPS+" IS: "+deallocatedIS);
    }

    @Override
    public InnerNode getRootNode() {
        if(rootNode == null) {
            rootNode = buildRootNode();
        }
        return rootNode;
    }

    @Override
    public boolean hasGivenUp() {
        return giveUp;
    }

    public static boolean isNiceGame(GameState gameState) {
        return gameState instanceof IIGoofSpielGameState
                || gameState instanceof LiarsDiceGameState
                || gameState instanceof GenericPokerGameState;
    }

    private Action randomChoice(Map<Action, Double> distribution) {
        double r = rnd.nextDouble();
        for (Map.Entry<Action, Double> entry : distribution.entrySet()) {
            double pA = entry.getValue();
            Action action = entry.getKey();
            if (r <= pA) return action;
            r -= pA;
        }

        return null;
    }

    public void setDoResetData(boolean resetData) {
        this.resetData = resetData;
    }

    private void buildTreeExpandChanceNodes(InnerNode startNode) {
        // basically expand all chance actions in the root, before the first players can act
        System.err.println("Building tree until first public states.");
        int nodes = 0, infosets = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<>();

        q.add(startNode);
        while (!q.isEmpty()) {
            nodes++;
            InnerNode n = q.removeFirst();
            if (!(n instanceof ChanceNode)) {
                MCTSInformationSet is = n.getInformationSet();
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(new OOSAlgorithmData(n.getActions(), config.useEpsilonRM));
                }
            } else { // expand chance nodes
                for (Action a : n.getActions()) {
                    Node ch = n.getChildFor(a);
                    if (ch instanceof InnerNode) {
                        q.add((InnerNode) ch);
                    }
                }
            }
        }
        System.err.println("Created nodes: " + nodes + "; infosets: " + infosets);
    }

    private String distributionToString(List<Action> actions, Map<Action, Double> distribution) {
        StringBuilder s = new StringBuilder("{");
        for (Action a : actions) {
            s.append(a);
            s.append(" = ");
            s.append(String.format("% 1.6f", distribution.get(a)));
            s.append(",\t");
        }
        s.append("}");
        return s.toString();
    }

    public void printDomainStatistics() {
        MCTSConfig config = getRootNode().getAlgConfig();

        Integer inners = config.getAllInformationSets().values()
                .stream().map(MCTSInformationSet::getAllNodes)
                .map(Set::size).reduce(0, Integer::sum);
        Long leafs = config.getAllInformationSets().values().stream()
                .map(MCTSInformationSet::getAllNodes)
                .map(setIN -> setIN.stream()
                        .map(InnerNode::getChildren)
                        .map(Map::values)
                        .map(mapCh -> mapCh.stream().filter(Node::isGameEnd).count())
                        .reduce(0L, Long::sum))
                .reduce(0L, Long::sum);

        PublicState deepestPS = config.getAllPublicStates().stream()
                .sorted((ps1, ps2) -> Integer.compare(ps2.getDepth(), ps1.getDepth()))
                .findFirst().get();
        int numC = 0;
        PublicState currentPs = deepestPS;
        while (currentPs.getParentPublicState() != null) {
            currentPs = currentPs.getParentPublicState();
            if (currentPs.getAllNodes().stream().anyMatch(n -> (n instanceof ChanceNode))) {
                numC++;
            }
        }
        Integer maxPTdepth = deepestPS.getDepth() - numC + 1;

        int augIs = config.getAllPublicStates().stream()
                .filter(ps -> ps.getPlayer().getId() <= 1) // exclude chance
                .map(ps -> ps.getSubgame().getGadgetInformationSets().size())
                .reduce(0, Integer::sum);

        System.err.println("Game has: \n" +
                "public states & info sets & aug info sets & inner nodes & leaf nodes & max PT depth");
        System.err.println(config.getAllPublicStates().size() + " & " +
                config.getAllInformationSets().size() + " & " +
                augIs + " & " +
                inners + " & " +
                leafs + " & " +
                (maxPTdepth));
    }

    private static Map<Action, Double> getDistributionFor(AlgorithmData algorithmData) {
        return (new MeanStratDist()).getDistributionFor(algorithmData);
    }

    public enum Budget {
        BUDGET_NUM_SAMPLES, // num of samples
        BUDGET_TIME // in milisec
    }

    private int numSamplesDuringRun;
    private int numSamplesInCurrentIS;
    private int numNodesTouchedDuringRun;

    public int numSamplesDuringRun() {
        return numSamplesDuringRun;
    }

    public int numSamplesInCurrentIS() {
        return numSamplesInCurrentIS;
    }
    public int numNodesTouchedDuringRun() {
        return numNodesTouchedDuringRun;
    }

    @Override
    public double[] currentISprobDist() {
        return currentISprobDist;
    }

    @Override
    public String extraInfo() {
        return extraInfo;
    }
}
