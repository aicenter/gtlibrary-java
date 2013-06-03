package cz.agents.gtlibrary.nfg.doubleoracle;

import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PureStrategy;
import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.utils.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 5/29/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class NFGBROracle implements NFGOracle{

    private GameInfo gameInfo;
    private Expander expander;
    private Player searchingPlayer;
    private Player opponentPlayer;
    private GameState root;
    private List<Action> myActions;

    public NFGBROracle(GameInfo gameInfo, GameState root, Expander expander, Player searchingPlayer, Player opponentPlayer) {
        this.gameInfo = gameInfo;
        this.expander = expander;
        this.searchingPlayer = searchingPlayer;
        this.opponentPlayer = opponentPlayer;
        this.root = root;

        if (root.getPlayerToMove().equals(searchingPlayer)) {
            myActions = expander.getActions(root);
        } else {
            myActions = expander.getActions(root.performAction((Action)expander.getActions(root).get(0)));
        }
    }

    @Override
    public Pair<PureStrategy, Double> getNewStrategy(Utility utilityCalculator, MixedStrategy opponentStrategy) {
        Action resultAction = null;
        double resultValue = (searchingPlayer.getId() == 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        if (opponentStrategy.size() == 0) {
            resultAction = myActions.get(0);
            resultValue = gameInfo.getMaxUtility();
            if (searchingPlayer.getId() == 1) resultValue *= -1;
            return  new Pair<PureStrategy, Double>(new ActionPureStrategy(resultAction), resultValue);
        }

        for (Action a : myActions) {
            double currValue = 0;
            Iterator<Map.Entry<PureStrategy, Double>> i = opponentStrategy.iterator();
            while (i.hasNext()) {
                Map.Entry<PureStrategy, Double> opp = i.next();
                currValue += utilityCalculator.getUtility(new ActionPureStrategy(a), opp.getKey()) * opp.getValue();
            }

            if ((searchingPlayer.getId() == 0 && currValue > resultValue) ||
                (searchingPlayer.getId() == 1 && currValue < resultValue)) {
                resultAction = a;
                resultValue = currValue;
            }
        }

        return  new Pair<PureStrategy, Double>(new ActionPureStrategy(resultAction), resultValue);
    }
}
