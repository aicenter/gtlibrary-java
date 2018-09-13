package algorithms.cr;

import cz.agents.gtlibrary.algorithms.cr.CRAlgorithm;
import cz.agents.gtlibrary.algorithms.cr.CRExperiments;
import cz.agents.gtlibrary.algorithms.cr.ResolvingMethod;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.PublicState;
import org.junit.Test;

import java.util.*;

import static cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm.updateCFRResolvingData;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class CRTest extends CRExperiments {

    public CRTest() {
        super(0L);
    }

    @Test
    public void testGamesRpIsSameInAllHistoriesWithinInfoSets() {
        checkDomainRpIsSameInAllHistoriesWithinInfoSets("IIGS", new String[]{"0", "4", "true", "true"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets("RPS", new String[]{"0"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets("LD", new String[]{"1", "1", "3"});
        checkDomainRpIsSameInAllHistoriesWithinInfoSets("GP", new String[]{"2", "2", "2", "2"});
    }


    private void checkDomainRpIsSameInAllHistoriesWithinInfoSets(String domain, String[] params) {
        for (long seed = 0; seed < 3; seed++) {
            CRTest exp = new CRTest();
            exp.prepareDomain(domain, params);
            exp.createGame(domain, new Random(seed));
            exp.expander.getAlgorithmConfig().createInformationSetFor(exp.rootState);
            Player resolvingPlayer = exp.rootState.getAllPlayers()[0];

            CRAlgorithm alg = new CRAlgorithm(exp.rootState, exp.expander, 0.6);
            alg.defaultResolvingMethod = ResolvingMethod.RESOLVE_MCCFR;
            alg.defaultRootMethod = ResolvingMethod.RESOLVE_MCCFR;
            alg.solveEntireGame(resolvingPlayer,100, 100); // first gadget

            Collection<MCTSInformationSet> infoSets = alg.getConfig().getAllInformationSets().values();
            for (MCTSInformationSet infoSet : infoSets) {
                Double rp = null;
                for (InnerNode node : infoSet.getAllNodes()) {
                    if (rp == null) {
                        rp = node.getReachPrPlayerChance();
                    }

                    assertEquals(rp, node.getReachPrPlayerChance(), 1e-7);
                    assertTrue(node.getReachPrPlayerChance() <= 1.0);
                    assertTrue(node.getReachPrPlayerChance() >= 0.0);
                }
            }
        }
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
        exp.createGame(domain, new Random(seed));
        exp.expander.getAlgorithmConfig().createInformationSetFor(exp.rootState);

        CRAlgorithm alg = new CRAlgorithm(exp.rootState, exp.expander, 0.6);
        alg.solveEntireGame(exp.rootState.getAllPlayers()[0],1000, 1000);

        Collection<MCTSInformationSet> infoSets = ((MCTSConfig) exp.expander.getAlgorithmConfig())
                .getAllInformationSets().values();
        for (MCTSInformationSet infoSet : infoSets) {
            OOSAlgorithmData  data = ((OOSAlgorithmData) infoSet.getAlgorithmData());
            if(data!=null) {
                hc += data.hashCode();
            }
        }
        return hc;
    }

    @Test
    public void testCFRResolvingLowersExploitability() {
        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "4", "true", "true"}, false, false);
        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "5", "true", "true"}, false, false);
        checkDomainCFRResolvingLowersExploitability("LD", new String[]{"1", "1", "6"}, false, false);
        checkDomainCFRResolvingLowersExploitability("GP", new String[]{"3", "3", "2", "2"}, false, false);

        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "4", "true", "true"}, false, true);
        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "5", "true", "true"}, false, true);
        checkDomainCFRResolvingLowersExploitability("LD", new String[]{"1", "1", "6"}, false, true);
        checkDomainCFRResolvingLowersExploitability("GP", new String[]{"3", "3", "2", "2"}, false, true);

//        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "4", "true", "true"}, true);
//        checkDomainCFRResolvingLowersExploitability("IIGS", new String[]{"0", "5", "true", "true"}, true);
//        checkDomainCFRResolvingLowersExploitability("RPS", new String[]{"0"}, true);
//        checkDomainCFRResolvingLowersExploitability("LD", new String[]{"1", "1", "6"}, true);
//        checkDomainCFRResolvingLowersExploitability("GP", new String[]{"3", "3", "2", "2"}, true);
    }

    private void checkDomainCFRResolvingLowersExploitability(String domain, String[] params,
                                                             boolean subtreeResolving,
                                                             boolean topmostTarget) {
        CRTest exp = new CRTest();
        exp.prepareDomain(domain, params);
        exp.createGame(domain, new Random(0L));
        exp.expander.getAlgorithmConfig().createInformationSetFor(exp.rootState);

        Player resolvingPlayer = exp.rootState.getAllPlayers()[0];

        CRAlgorithm alg = new CRAlgorithm(exp.rootState, exp.expander);
        InnerNode rootNode = alg.getRootNode();
        alg.setDoResetData(false);
        buildCompleteTree(rootNode);
        alg.runRootCFR(resolvingPlayer, rootNode, 1000);

        PublicState targetPS = alg.getConfig().getAllPublicStates().stream()
                .filter(ps -> ps.getPlayer().getId() == resolvingPlayer.getId())
                .min(Comparator.comparingInt(PublicState::getDepth)).get();
        if(!topmostTarget) {
            targetPS = targetPS.getNextPlayerPublicStates().iterator().next();
        }

        Map<ISKey, Map<Action, Double>> stratRoot = exp.getBehavioralStrategy(rootNode);
        Exploitability cfrExpl = exp.calcExploitability(stratRoot);
        System.out.println("Root "+cfrExpl);

        targetPS.resetData(true);
        targetPS.setResolvingIterations(1000);
        targetPS.setResolvingMethod(ResolvingMethod.RESOLVE_CFR);
        updateCFRResolvingData(targetPS, alg.rootCfrData.reachProbs, alg.rootCfrData.historyExpValues);

        ArrayDeque<PublicState> q = new ArrayDeque<>();
        q.add(targetPS);

        Map<ISKey, Map<Action, Double>> stratCFR_copy;
        Map<ISKey, Map<Action, Double>> stratCR;
        while (!q.isEmpty()) {
            PublicState ps = q.removeFirst();
            InnerNode node = ps.getAllNodes().iterator().next();

            alg.runStep(resolvingPlayer, node, ResolvingMethod.RESOLVE_CFR, 1000, 1000);

            stratCFR_copy = exp.cloneBehavStrategy(stratRoot);
            stratCR = exp.getBehavioralStrategy(rootNode);
            exp.substituteStrategy(stratCFR_copy, stratCR, targetPS);
            Exploitability resolvedExpl = exp.calcExploitability(stratCFR_copy);
            System.out.println("Resolved "+resolvedExpl);


            assertTrue(cfrExpl.expl0 - resolvedExpl.expl0 >= 0);
            assertTrue(cfrExpl.expl1 - resolvedExpl.expl1 >= 0);

            if (subtreeResolving) {
                q.addAll(ps.getNextPlayerPublicStates());
            }
        }
    }
}
