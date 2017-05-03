package algorithm.imperfectrecall.bestresponse;

import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.OracleImperfectRecallBestResponse;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class OracleBestResponseTest {
    @Test
    public void BRTestTest() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Action, Double> strategy = new HashMap<>(2);

        GameState state = rootState.performAction(expander.getActions(rootState).get(0));

        state = state.performAction(expander.getActions(state).get(0));
        List<Action> actions = expander.getActions(state);

        strategy.put(actions.get(0), 0.5);
        strategy.put(actions.get(1), 0.5);

        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponse(strategy);
        assertEquals(2.5, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestTest1() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Action, Double> strategy = new HashMap<>(2);

        GameState state = rootState.performAction(expander.getActions(rootState).get(0));

        state = state.performAction(expander.getActions(state).get(0));
        List<Action> actions = expander.getActions(state);

        strategy.put(actions.get(0), 0.6);
        strategy.put(actions.get(1), 0.4);

        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponse(strategy);
        assertEquals(3, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestDefaultStrategyTest() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Action, Double> strategy = new HashMap<>(2);
        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponse(strategy);
        assertEquals(5, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestDefaultRPTest() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Sequence, Double> strategy = new HashMap<>(2);
        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponseSequence(strategy);
        assertEquals(5, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestDefaultRPTest1() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Sequence, Double> strategy = new HashMap<>(2);

        GameState state = rootState.performAction(expander.getActions(rootState).get(0));

        strategy.put(rootState.getSequenceFor(BRTestGameInfo.FIRST_PLAYER), 1d);
        strategy.put(state.getSequenceFor(BRTestGameInfo.FIRST_PLAYER), 1d);
        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.SECOND_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponseSequence(strategy);
        assertEquals(10, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestDefaultRPTest2() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Sequence, Double> strategy = new HashMap<>(2);
        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.SECOND_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponseSequence(strategy);
        assertEquals(0, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestRepeatedComputationTest() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Sequence, Double> strategy = new HashMap<>(2);

        GameState state = rootState.performAction(expander.getActions(rootState).get(0));

        strategy.put(rootState.getSequenceFor(BRTestGameInfo.FIRST_PLAYER), 1d);
        strategy.put(state.getSequenceFor(BRTestGameInfo.FIRST_PLAYER), 1d);
        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.SECOND_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponseSequence(strategy);
        assertEquals(10, br.getValue(), 1e-4);

        strategy = new HashMap<>(2);

        br.getBestResponseSequence(strategy);
        assertEquals(0, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestRepeatedComputationTest1() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Sequence, Double> strategy = new HashMap<>(2);
        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.SECOND_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponseSequence(strategy);
        assertEquals(0, br.getValue(), 1e-4);

        strategy = new HashMap<>(2);

        GameState state = rootState.performAction(expander.getActions(rootState).get(0));

        strategy.put(rootState.getSequenceFor(BRTestGameInfo.FIRST_PLAYER), 1d);
        strategy.put(state.getSequenceFor(BRTestGameInfo.FIRST_PLAYER), 1d);

        br.getBestResponseSequence(strategy);
        assertEquals(10, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestInStateTest() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Action, Double> strategy = new HashMap<>(2);

        GameState state = rootState.performAction(expander.getActions(rootState).get(0));

        GameState nextState = state.performAction(expander.getActions(state).get(0));
        List<Action> actions = expander.getActions(nextState);

        strategy.put(actions.get(1), 1d);

        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponseIn(state, strategy);
        assertEquals(0, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestInStateTest1() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Action, Double> strategy = new HashMap<>(2);

        GameState state = rootState.performAction(expander.getActions(rootState).get(1));

        GameState nextState = state.performAction(expander.getActions(state).get(1));
        List<Action> actions = expander.getActions(nextState);

        strategy.put(actions.get(0), 1d);

        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponseIn(state, strategy);
        assertEquals(0, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestInStateTest2() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Action, Double> strategy = new HashMap<>(2);

        GameState state = rootState.performAction(expander.getActions(rootState).get(0));

        GameState nextState = state.performAction(expander.getActions(state).get(0));
        List<Action> actions = expander.getActions(nextState);

        strategy.put(actions.get(0), 1d);

        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponseIn(state, strategy);
        assertEquals(5, br.getValue(), 1e-4);
    }

    @Test
    public void BRTestInStateTest3() {
        GameState rootState = new BRTestGameState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        BasicGameBuilder.build(rootState, config, expander);

        Map<Action, Double> strategy = new HashMap<>(2);

        GameState state = rootState.performAction(expander.getActions(rootState).get(1));

        GameState nextState = state.performAction(expander.getActions(state).get(1));
        List<Action> actions = expander.getActions(nextState);

        strategy.put(actions.get(1), 1d);

        OracleImperfectRecallBestResponse br = new OracleImperfectRecallBestResponse(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());

        br.getBestResponseIn(state, strategy);
        assertEquals(5, br.getValue(), 1e-4);
    }
}
