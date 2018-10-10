package publicstate;

import cz.agents.gtlibrary.algorithms.cr.Game;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.cr.CRExperiments;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PublicStateTest extends CRExperiments {

    public PublicStateTest() {
        super(0L);
    }

    @Test
    public void testGamesForSatisfyingPublicStateRepresentationConstraints() {
        checkDomain("IIGS", new String[]{"0", "4", "true", "true"});
        checkDomain("IIGS", new String[]{"0", "5", "true", "true"});
        checkDomain("RPS", new String[]{"0"});
        checkDomain("LD", new String[]{"1", "1", "6"});
    }

    private void checkDomain(String domain, String[] params) {
        PublicStateTest exp = new PublicStateTest();
        exp.prepareDomain(domain, params);
        Game g = exp.createGame(domain, new Random(0L));

        g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);

        InnerNodeImpl rootNode;
        if (g.rootState.isPlayerToMoveNature()) {
            rootNode = new ChanceNodeImpl(g.expander, g.rootState, 0);
        } else {
            rootNode = new InnerNodeImpl(g.expander, g.rootState);
        }

        // build set of "inner" game states to compare with
        Set<GameState> expectedInnerGameStates = new HashSet<>();
        ArrayDeque<InnerNodeImpl> q = new ArrayDeque<InnerNodeImpl>();
        expectedInnerGameStates.add(g.rootState);
        q.add(rootNode);
        HashMap<ISKey, MCTSInformationSet> infoSets = ((MCTSConfig) g.expander.getAlgorithmConfig()).getAllInformationSets();
        while (!q.isEmpty()) {
            InnerNodeImpl n = q.removeFirst();
            for (Action a : n.getActions()) {
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNodeImpl) {
                    q.add((InnerNodeImpl) ch);
                    // added state should always be novel (states are unique within the game)
                    assertTrue(expectedInnerGameStates.add(ch.getGameState()));
                }
            }
        }

        Set<GameState> actualInnerGameStates = new HashSet<>();
        Set<MCTSPublicState> publicStates = ((MCTSConfig) g.expander.getAlgorithmConfig()).getAllPublicStates();
        for (MCTSPublicState publicState : publicStates) {
            GameState firstState = null;
            for (GameState state : publicState.getAllStates()) {
                // added state should always be novel (states are unique within the game)
                assertTrue(actualInnerGameStates.add(state));

                if(firstState == null) {
                    firstState = state;
                } else {
                    // this does not hold in games where information sets are multi level
                    // but works for the games which are tested
                    assertEquals(firstState.getHistory().getLength(), state.getHistory().getLength());
                }
            }
            for (GameState state : publicState.getAllStates()) {

                // check that all nodes from the state's IS are also in the public state
                MCTSInformationSet informationSet = infoSets.get(state.getISKeyForPlayerToMove());
                if(informationSet == null) {
                    assertTrue(state.isPlayerToMoveNature());
                } else {
                    assertTrue(publicState.getAllStates().containsAll(informationSet.getAllStates()));
                }
            }
        }

        // check that all the game inner states are also represented within the public states
        assertEquals(expectedInnerGameStates, actualInnerGameStates);
    }


}
