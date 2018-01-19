package cz.agents.gtlibrary.domain.testGame;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub Cerny on 20/10/2017.
 */
public class TestGameExpander extends ExpanderImpl {

    public TestGameExpander(AlgorithmConfig algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        ArrayList<Action> actions = new ArrayList<>();
//        System.out.println(TestGameInfo.successors.get(((TestGameState)gameState).getID()).size());
        if (!TestGameInfo.successors.containsKey(((TestGameState)gameState).getID())) return actions;
        int idx = 0;
        for (int action : TestGameInfo.successors.get(((TestGameState)gameState).getID())) {
            actions.add(new TestGameAction(idx, action, getAlgorithmConfig().getInformationSetFor(gameState)));
            idx ++ ;
        }
        return actions;
    }
}
