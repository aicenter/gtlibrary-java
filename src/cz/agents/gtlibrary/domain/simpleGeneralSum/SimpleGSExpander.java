package cz.agents.gtlibrary.domain.simpleGeneralSum;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/5/13
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleGSExpander extends ExpanderImpl {

    public SimpleGSExpander(AlgorithmConfig algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        List<Action> result = new ArrayList<Action>(SimpleGSInfo.MAX_ACTIONS);
        for (int i=0; i<SimpleGSInfo.MAX_ACTIONS; i++) {
            result.add(new SimpleGSAction(getAlgorithmConfig().getInformationSetFor(gameState), i, gameState.getPlayerToMove()));
        }
        return result;
    }
}
