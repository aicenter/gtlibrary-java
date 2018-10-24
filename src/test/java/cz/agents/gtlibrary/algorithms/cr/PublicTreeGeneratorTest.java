package cz.agents.gtlibrary.algorithms.cr;

import algorithms.cr.CRTest;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import org.junit.Test;

import java.util.HashMap;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class PublicTreeGeneratorTest extends CRExperiments {

    public PublicTreeGeneratorTest() {
        super(0L);
    }

    @Test
    public void constructPublicTree() {
//        checkDomainConstructPublicTree("RPS", new String[]{"0"});;
        checkDomainConstructPublicTree("IIGS", new String[]{"0", "3", "true", "true"});;
//        checkDomainConstructPublicTree("LD", new String[]{"1", "1", "3"});;
//        checkDomainConstructPublicTree("LD", new String[]{"2", "1", "4"});;
//        checkDomainConstructPublicTree("GP", new String[]{"2", "2", "2", "2"});;
    }

    private void checkDomainConstructPublicTree(String domain, String[] params) {

        // expected
        CRTest expected = new CRTest();
        expected.prepareDomain(domain, params);
        InnerNode expRootNode;
        Game g_exp = expected.createGame(domain, new Random(0));
        if (g_exp.rootState.isPlayerToMoveNature()) {
            expRootNode = new ChanceNodeImpl(g_exp.expander, g_exp.rootState, g_exp.config.getRandom());
        } else {
            expRootNode = new InnerNodeImpl(g_exp.expander, g_exp.rootState);
        }
        buildCompleteTree(expRootNode);
        HashMap<InnerNode, MCTSPublicState> expectedMap = new HashMap<>();
        g_exp.config.getAllInformationSets().values().stream()
                .flatMap(is -> is.getAllNodes().stream())
                .forEach(in -> {
                    MCTSPublicState ps = in.getPublicState();
                    assertNotNull(ps);
                    expectedMap.put(in, ps);
                });

        // actual
        CRTest actual = new CRTest();
        actual.prepareDomain(domain, params);
        InnerNode actRootNode;
        InnerNodeImpl.attendPS = false;
        Game g_act = actual.createGame(domain, new Random(0));
        if (g_act.rootState.isPlayerToMoveNature()) {
            actRootNode = new ChanceNodeImpl(g_act.expander, g_act.rootState, g_act.config.getRandom());
        } else {
            actRootNode = new InnerNodeImpl(g_act.expander, g_act.rootState);
        }

        HashMap<InnerNode, MCTSPublicState> actualMap = new HashMap<>();
        PublicTreeGenerator.constructPublicTree(actRootNode);

        new GambitEFG().write("gs_actual.gbt",  actRootNode);
        new GambitEFG().write("gs_expected.gbt", expRootNode);

        g_act.config.getAllInformationSets().values().stream()
                .flatMap(is -> is.getAllNodes().stream())
                .forEach(in -> actualMap.put(in, in.getPublicState()));

        assertEquals(expectedMap.size(), actualMap.size());

        Player[] pl = g_exp.rootState.getAllPlayers();

        expectedMap.forEach((ein, eps) -> {
            MCTSPublicState aps = actualMap.get(ein);
            InnerNode ain = actualMap.keySet().stream().filter(k -> k.equals(ein)).findFirst().get();
            assertEquals(ein, ain);
            assertEquals(eps.getAllNodes().size(), aps.getAllNodes().size());
            assertEquals(eps.getAllInformationSets().size(), aps.getAllInformationSets().size());
            assertEquals(ein.getPublicState(), ain.getPublicState());
            assertEquals(eps.getNextPublicStates(), aps.getNextPublicStates());
            assertEquals(eps.getParentPublicState(), aps.getParentPublicState());
            assertEquals(eps.getNextPlayerPublicStates(pl[0]), aps.getNextPlayerPublicStates(pl[0]));
            assertEquals(eps.getNextPlayerPublicStates(pl[1]), aps.getNextPlayerPublicStates(pl[1]));
            if (!domain.equals("RPS")) {
                assertEquals(eps.getNextPlayerPublicStates(pl[2]), aps.getNextPlayerPublicStates(pl[2]));
            }
        });

        g_act.config.getAllPublicStates().removeAll(g_exp.config.getAllPublicStates());
        assertEquals(g_exp.config.getAllPublicStates(), g_act.config.getAllPublicStates());
    }
}
