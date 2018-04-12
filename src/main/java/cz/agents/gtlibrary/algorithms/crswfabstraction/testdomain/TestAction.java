package cz.agents.gtlibrary.algorithms.crswfabstraction.testdomain;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class TestAction extends ActionImpl {

    private int value;

    public TestAction(InformationSet informationSet, int value) {
        super(informationSet);
        this.value = value;
    }

    @Override
    public void perform(GameState gameState) {
        TestState state = (TestState) gameState;
        state.value = state.value.successors[value];
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        return "Action[" + informationSet + ":" + value + "]";
    }
}
