package algorithms.cr;

import cz.agents.gtlibrary.algorithms.cr.*;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInfoSet;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInnerNode;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.iinodes.PublicStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.PublicState;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm.updateCFRResolvingData;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertTrue;


public class CRTest extends CRExperiments {

    public CRTest() {
        super(0L);
    }

    @Test
    public void testGamesRpIsSameInAllHistoriesWithinInfoSets() {
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(0, "IIGS", new String[]{"0", "2", "true", "true"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(1, "IIGS", new String[]{"0", "2", "true", "true"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(0, "IIGS", new String[]{"0", "3", "true", "true"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(1, "IIGS", new String[]{"0", "3", "true", "true"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(0, "IIGS", new String[]{"0", "4", "true", "true"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(1, "IIGS", new String[]{"0", "4", "true", "true"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(0, "RPS", new String[]{"0"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(1, "RPS", new String[]{"0"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(0, "LD", new String[]{"1", "1", "3"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(1, "LD", new String[]{"1", "1", "3"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(0, "GP", new String[]{"2", "2", "1", "1"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(1, "GP", new String[]{"2", "2", "1", "1"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(0, "GP", new String[]{"2", "2", "2", "2"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(1, "GP", new String[]{"2", "2", "2", "2"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(0, "GP", new String[]{"3", "3", "2", "2"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets(1, "GP", new String[]{"3", "3", "2", "2"});
    }

    private void checkDomainRpIsSameInAllHistoriesWithinInfoSets(Integer player, String domain, String[] params) {
        for (long seed = 0; seed < 3; seed++) {
            CRTest exp = new CRTest();
            exp.prepareDomain(domain, params);
            Game g = exp.createGame(domain, new Random(seed));
            g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);
            Player resolvingPlayer = g.rootState.getAllPlayers()[player];

            CRAlgorithm alg = new CRAlgorithm(g.rootState, g.expander, 0.6);
            alg.defaultResolvingMethod = ResolvingMethod.RESOLVE_MCCFR;
            alg.defaultRootMethod = ResolvingMethod.RESOLVE_MCCFR;
            alg.solveEntireGame(resolvingPlayer, 100, 100);

            Collection<MCTSInformationSet> infoSets = alg.getConfig().getAllInformationSets().values();
            for (MCTSInformationSet infoSet : infoSets) {
                Double rp = null;
                for (InnerNode node : infoSet.getAllNodes()) {
                    if (rp == null) {
                        rp = node.getReachPrByPlayer(infoSet.getPlayer());
                    }

                    assertEquals(rp, node.getReachPrByPlayer(infoSet.getPlayer()), 1e-7);
                    assertTrue(node.getReachPrByPlayer(infoSet.getPlayer()) <= 1.0);
                    assertTrue(node.getReachPrByPlayer(infoSet.getPlayer()) >= 0.0);
                }
            }
        }
    }

    @Test
    public void testPublicStateHaveNoCommonIS() {
        checkDomainPublicStateHaveNoCommonIS("IIGS", new String[]{"0", "2", "true", "true"});
        checkDomainPublicStateHaveNoCommonIS("IIGS", new String[]{"0", "3", "true", "true"});
        checkDomainPublicStateHaveNoCommonIS("IIGS", new String[]{"0", "4", "true", "true"});
        checkDomainPublicStateHaveNoCommonIS("RPS", new String[]{"0"});
        checkDomainPublicStateHaveNoCommonIS("LD", new String[]{"1", "1", "3"});
        checkDomainPublicStateHaveNoCommonIS("GP", new String[]{"2", "2", "2", "2"});
        checkDomainPublicStateHaveNoCommonIS("GP", new String[]{"2", "2", "1", "1"});
        checkDomainPublicStateHaveNoCommonIS("RG", new String[]{"1", "2", "3", "2", "false", "false", "false"});
        checkDomainPublicStateHaveNoCommonIS("RG", new String[]{"1", "3", "3", "2", "false", "false", "false"});
        checkDomainPublicStateHaveNoCommonIS("RG", new String[]{"1", "4", "3", "2", "false", "false", "false"});
    }

    private void checkDomainPublicStateHaveNoCommonIS(String domain, String[] params) {
        CRTest exp = new CRTest();
        exp.prepareDomain(domain, params);
        Game g = exp.createGame(domain, new Random(0));
        g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);

        buildCompleteTree(g.getRootNode());

        Set<MCTSInformationSet> processed = new HashSet<>();
        g.config.getAllPublicStates().stream()
                .map(PublicStateImpl::getAllInformationSets)
                .forEach(issets -> {
                    issets.stream()
                            .filter(Objects::nonNull)
                            .forEach(is -> assertFalse(processed.contains(is)));
                    processed.addAll(issets);
                });
        g.config.getAllPublicStates().stream()
                .forEach(ps -> ps.getAllNodes().forEach(in -> {
                    assertEquals(in.getPublicState(), ps);
                    if(in.getParent() != null && ps.getParentPublicState() != null) {
                        assertEquals(in.getParent().getPublicState(), ps.getParentPublicState());
                    }
                }));
    }
    private void checkDomainPublicStateHaveNoCommonIS(String[] randomGameParams) {
        CRTest exp = new CRTest();
        exp.prepareDomain("RG", randomGameParams);
        Game g = exp.createGame("RG", new Random(0));
        g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);

        CRAlgorithm alg = new CRAlgorithm(g.rootState, g.expander, 0.6);
        buildCompleteTree(alg.getRootNode());

        Set<MCTSInformationSet> processed = new HashSet<>();
        alg.getConfig().getAllPublicStates().stream()
                .map(ps -> ps.getAllInformationSets())
                .forEach(issets -> {
                    issets.stream()
                            .filter(Objects::nonNull)
                            .forEach(is -> assertFalse(processed.contains(is)));
                    processed.addAll(issets);
                });
    }

    @Test
    public void testAlgorithmIsCorrectlySeeded() {
        for (int seed = 0; seed < 10; seed++) {
            assertEquals(runAlgGetHashCode(seed), runAlgGetHashCode(seed));
        }
    }

    private long runAlgGetHashCode(int seed) {
        long hc = 0;

        String domain = "IIGS";
        String[] params = new String[]{"0", "4", "true", "true"};

        CRTest exp = new CRTest();
        exp.prepareDomain(domain, params);
        Game g = exp.createGame(domain, new Random(seed));
        g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);

        CRAlgorithm alg = new CRAlgorithm(g.rootState, g.expander, 0.6);
        alg.solveEntireGame(g.rootState.getAllPlayers()[0], 1000, 1000);

        Collection<MCTSInformationSet> infoSets = ((MCTSConfig) g.expander.getAlgorithmConfig())
                .getAllInformationSets().values();
        for (MCTSInformationSet infoSet : infoSets) {
            OOSAlgorithmData data = ((OOSAlgorithmData) infoSet.getAlgorithmData());
            if (data != null) {
                hc += data.hashCode();
            }
        }
        return hc;
    }

    @Test
    public void testRunStepChangesStrategyInPublicSubtree() {
        checkDomainPublicStateHaveNoCommonIS("IIGS", new String[]{"0", "2", "true", "true"});
        checkDomainPublicStateHaveNoCommonIS("IIGS", new String[]{"0", "3", "true", "true"});
        checkDomainPublicStateHaveNoCommonIS("IIGS", new String[]{"0", "4", "true", "true"});
        checkDomainPublicStateHaveNoCommonIS("RPS", new String[]{"0"});
        checkDomainPublicStateHaveNoCommonIS("LD", new String[]{"1", "1", "3"});
        checkDomainPublicStateHaveNoCommonIS("GP", new String[]{"2", "2", "2", "2"});

    }

    private void checkDomainRunStepChangesStrategyInPublicSubtree(String domain, String[] params) {
        CRTest exp = new CRTest();
        exp.prepareDomain(domain, params);
        Game g = exp.createGame(domain, new Random(0L));
        g.config.createInformationSetFor(g.rootState);

        Player resolvingPlayer = g.rootState.getAllPlayers()[0];

        CRAlgorithm alg = new CRAlgorithm(g.rootState, g.expander);
        InnerNode rootNode = alg.getRootNode();
        alg.setDoResetData(true);
        buildCompleteTree(rootNode);

        PublicState targetPS = alg.getConfig().getAllPublicStates().stream()
                .filter(ps -> ps.getPlayer().getId() == resolvingPlayer.getId())
                .min(Comparator.comparingInt(PublicState::getDepth)).get()
                .getNextPlayerPublicStates().iterator().next();


        alg.runRootCFR(resolvingPlayer, rootNode, 100);
        Map<ISKey, Map<Action, Double>> stratRoot = exp.getBehavioralStrategy(rootNode);
        Map<ISKey, Map<Action, Double>> stratCFR_copy = exp.cloneBehavStrategy(stratRoot);
        assertEquals(stratRoot, stratCFR_copy);

        alg.runStep(resolvingPlayer, targetPS.getAllInformationSets().iterator().next(),
                ResolvingMethod.RESOLVE_UNIFORM, 0);
        Map<ISKey, Map<Action, Double>> stratResolve = exp.getBehavioralStrategy(rootNode);
        assertFalse(stratRoot.equals(stratResolve));
        assertEquals(stratRoot.size(), stratResolve.size());

        exp.substituteStrategy(stratCFR_copy, stratResolve, targetPS);
        assertFalse(stratRoot.equals(stratCFR_copy));
        assertEquals(stratRoot.size(), stratCFR_copy.size());


        Set<PublicState> psSubtree = new HashSet<>();
        ArrayDeque<PublicState> q = new ArrayDeque<>();
        q.add(targetPS);
        while (!q.isEmpty()) {
            PublicState ps = q.removeFirst();
            psSubtree.add(ps);
            q.addAll(ps.getNextPublicStates());
        }

        Set<ISKey> diffIsKeys = psSubtree.stream()
                .flatMap(ps -> ps.getAllInformationSets().stream())
                .map(InformationSetImpl::getISKey)
                .collect(Collectors.toSet());
        Set<ISKey> sameIsKeys = new HashSet<>(stratRoot.keySet());
        sameIsKeys.removeAll(diffIsKeys);

        System.err.println("Testing same is");
        for (ISKey same : sameIsKeys) {
            assertNotNull(same);
            assertTrue(stratRoot.containsKey(same));
            assertTrue(stratCFR_copy.containsKey(same));
            assertNotNull(stratRoot.get(same));
            assertNotNull(stratCFR_copy.get(same));
            assertEquals(stratRoot.get(same), stratCFR_copy.get(same));
        }

        System.err.println("Testing diff is");
        for (ISKey diff : diffIsKeys) {
            assertNotNull(diff);
            assertTrue(stratRoot.containsKey(diff));
            assertTrue(stratCFR_copy.containsKey(diff));
            assertNotNull(stratRoot.get(diff));
            assertNotNull(stratCFR_copy.get(diff));
            if (stratRoot.get(diff).size() > 1) assertFalse(stratRoot.get(diff).equals(stratCFR_copy.get(diff)));
        }

    }

    @Test
    public void testCFRResolvingLowersExploitability() {
//        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "3", "true", "true"},
//                false, true,100,100);
//        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "4", "true", "true"},
//                false, true, 100,100);
//        checkDomainCFRResolvingLowersExploitability("LD", new String[]{"1", "1", "4"},
//                false, true, 100,100);
        // todo:
//        checkDomainCFRResolvingLowersExploitability("GP", new String[]{"2", "2", "2", "2"},
//                false, true,5000,1000);
//
//        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "3", "true", "true"},
//                false, false, 100,100);
//        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "4", "true", "true"},
//                false, false, 10000, 20000);
//        checkDomainCFRResolvingLowersExploitability("LD", new String[]{"1", "1", "4"},
//                false, false,100,50000);

//        checkDomainCFRResolvingLowersExploitability("GP", new String[]{"2", "2", "2", "2"}, false, false);
//

//        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "4", "true", "true"},
//                true, false, 10000,20000);
//        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "5", "true", "true"}, true);
//        checkDomainCFRResolvingLowersExploitability("RPS", new String[]{"0"}, true);
//        checkDomainCFRResolvingLowersExploitability("LD", new String[]{"1", "1", "6"}, true);
//        checkDomainCFRResolvingLowersExploitability("GP", new String[]{"3", "3", "2", "2"}, true);
    }

    private void checkDomainCFRResolvingLowersExploitability(String domain, String[] params,
                                                             boolean subtreeResolving,
                                                             boolean topmostTarget,
                                                             int rootIterations,
                                                             int resolvingIterations) {
        CRTest exp = new CRTest();
        exp.prepareDomain(domain, params);
        Game g = exp.createGame(domain, new Random(0L));
        g.config.createInformationSetFor(g.rootState);

        Player resolvingPlayer = g.rootState.getAllPlayers()[0];

        CRAlgorithm alg = new CRAlgorithm(g.rootState, g.expander);
        InnerNode rootNode = alg.getRootNode();
        alg.setDoResetData(true);
        buildCompleteTree(rootNode);
        alg.runRootCFR(resolvingPlayer, rootNode, rootIterations);

        PublicState targetPS = alg.getConfig().getAllPublicStates().stream()
                .filter(ps -> ps.getPlayer().getId() == resolvingPlayer.getId())
                .min(Comparator.comparingInt(PublicState::getDepth)).get();
        if (!topmostTarget) {
            targetPS = targetPS.getNextPlayerPublicStates().iterator().next();
        }
        System.out.println("Target ps " + targetPS);

        Subgame subgame = targetPS.getSubgame();
        GadgetChanceNode gadgetRoot = subgame.getGadgetRoot();
        // chance probs must sum up to 1
        assertTrue(
                Math.abs(gadgetRoot.getChanceProbabilities().values().stream().reduce(0., Double::sum) - 1.0) < 1e-10);

        Set<GadgetInfoSet> gadgetInfoSets = subgame.getGadgetInformationSets();
        Set<MCTSInformationSet> origInfoSets = subgame.getOriginalInformationSets();
        // gadget augmented info sets must have same reach prob.
        gadgetInfoSets.forEach(gis -> {
            Double rp = null;
            for (InnerNode in : gis.getAllNodes()) {
                if (rp == null) rp = in.getReachPrPlayerChance();
                assertEquals(rp, in.getReachPrPlayerChance());
            }
        });
        // orig info sets must have same terminate utilities
        Map<InnerNode, GadgetInnerNode> n2gn = new HashMap<>();
        subgame.getGadgetNodes().forEach(gn -> n2gn.put(gn.getOriginalNode(), gn));
        origInfoSets.forEach(is -> {
            Double tu = null;
            for (InnerNode in : is.getAllNodes()) {
                GadgetInnerNode gin = n2gn.get(in);
                if (tu == null) tu = gin.getTerminateNode().getUtilities()[0];
                assertEquals(tu, gin.getTerminateNode().getUtilities()[0]);
                assertEquals(tu, -gin.getTerminateNode().getUtilities()[1]);
            }
        });


        System.out.println("RootReachPr " + gadgetRoot.getRootReachPr());

        Map<ISKey, Map<Action, Double>> stratRoot = exp.getBehavioralStrategy(rootNode);
        Exploitability cfrExpl = exp.calcExploitability(g, stratRoot);
        System.out.println("Root " + cfrExpl);

        targetPS.resetData(true);
        targetPS.setResolvingIterations(rootIterations);
        targetPS.setResolvingMethod(ResolvingMethod.RESOLVE_CFR);
        updateCFRResolvingData(targetPS, alg.rootCfrData.reachProbs, alg.rootCfrData.historyExpValues);

        ArrayDeque<PublicState> q = new ArrayDeque<>();
        q.add(targetPS);

        Map<ISKey, Map<Action, Double>> stratCFR_copy;
        Map<ISKey, Map<Action, Double>> stratCR;
        while (!q.isEmpty()) {
            PublicState ps = q.removeFirst();
            MCTSInformationSet is = ps.getAllInformationSets().iterator().next();

            alg.runStep(resolvingPlayer, is, ResolvingMethod.RESOLVE_CFR, resolvingIterations);

            stratCFR_copy = exp.cloneBehavStrategy(stratRoot);

            Exploitability resolvedExpl;
            resolvedExpl = exp.calcExploitability(g, stratCFR_copy);
            System.out.println("stratCFR_copy " + resolvedExpl);

            stratCR = exp.getBehavioralStrategy(rootNode);

            resolvedExpl = exp.calcExploitability(g, stratCR);
            System.out.println("stratCR " + resolvedExpl);

            exp.substituteStrategy(stratCFR_copy, stratCR, ps);
            resolvedExpl = exp.calcExploitability(g, stratCFR_copy);
            System.out.println("stratCFR_copy+CR " + resolvedExpl);

            assertTrue(cfrExpl.total() - resolvedExpl.total() >= 0);

            if (subtreeResolving) {
                q.addAll(ps.getNextPlayerPublicStates());
            }
        }
    }
}
