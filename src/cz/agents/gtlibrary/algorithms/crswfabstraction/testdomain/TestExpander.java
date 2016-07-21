package cz.agents.gtlibrary.algorithms.crswfabstraction.testdomain;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

public class TestExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public TestExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        List<Action> actions = new ArrayList<>();
        TestState state = (TestState) gameState;
        InformationSet informationSet = getAlgorithmConfig().getInformationSetFor(gameState);
        for (int i = 0; i < state.value.actionNo; i++) {
            actions.add(new TestAction(informationSet, i));
        }
        return actions;
    }
}
