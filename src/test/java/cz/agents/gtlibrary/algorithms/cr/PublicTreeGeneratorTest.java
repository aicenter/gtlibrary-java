package cz.agents.gtlibrary.algorithms.cr;

import algorithms.cr.CRTest;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.interfaces.Player;
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
        checkDomainConstructPublicTree("RPS", new String[]{"0"});;
        checkDomainConstructPublicTree("IIGS", new String[]{"0", "4", "true", "true"});;
        checkDomainConstructPublicTree("LD", new String[]{"1", "1", "3"});;
        checkDomainConstructPublicTree("GP", new String[]{"2", "2", "2", "2"});;
    }

    private void checkDomainConstructPublicTree(String domain, String[] params) {
        InnerNode rootNode;

        // expected
        CRTest expected = new CRTest();
        expected.prepareDomain(domain, params);
        expected.createGame(domain, new Random(0));
        if (expected.rootState.isPlayerToMoveNature()) {
            rootNode = new ChanceNodeImpl(expected.expander, expected.rootState, expected.config.getRandom());
        } else {
            rootNode = new InnerNodeImpl(expected.expander, expected.rootState);
        }
        buildCompleteTree(rootNode);
        HashMap<InnerNode, MCTSPublicState> expectedMap = new HashMap<>();
        expected.config.getAllInformationSets().values().stream()
                .flatMap(is -> is.getAllNodes().stream())
                .forEach(in -> {
                    MCTSPublicState ps = in.getPublicState();
                    assertNotNull(ps);
                    expectedMap.put(in, ps);
                });

        // actual
        CRTest actual = new CRTest();
        actual.prepareDomain(domain, params);
        actual.createGame(domain, new Random(0));
        if (actual.rootState.isPlayerToMoveNature()) {
            rootNode = new ChanceNodeImpl(actual.expander, actual.rootState, actual.config.getRandom());
        } else {
            rootNode = new InnerNodeImpl(actual.expander, actual.rootState);
        }

        HashMap<InnerNode, MCTSPublicState> actualMap = new HashMap<>();
        PublicTreeGenerator.constructPublicTree(rootNode);
        actual.config.getAllInformationSets().values().stream()
                .flatMap(is -> is.getAllNodes().stream())
                .forEach(in -> actualMap.put(in, in.getPublicState()));

        assertEquals(expectedMap, actualMap);

        Player[] pl = expected.rootState.getAllPlayers();

        expectedMap.forEach((ein, eps) -> {
            MCTSPublicState aps = actualMap.get(ein);
            assertEquals(eps.getNextPublicStates(), aps.getNextPublicStates());
            assertEquals(eps.getParentPublicState(), aps.getParentPublicState());
            assertEquals(eps.getNextPlayerPublicStates(pl[0]), aps.getNextPlayerPublicStates(pl[0]));
            assertEquals(eps.getNextPlayerPublicStates(pl[1]), aps.getNextPlayerPublicStates(pl[1]));
            if (!domain.equals("RPS")) {
                assertEquals(eps.getNextPlayerPublicStates(pl[2]), aps.getNextPlayerPublicStates(pl[2]));
            }
        });
    }
}
