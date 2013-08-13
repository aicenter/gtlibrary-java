package cz.agents.gtlibrary.nfg;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 5/29/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class NFGActionUtilityComputer extends Utility<ActionPureStrategy, ActionPureStrategy> {

    private GameState root;
    private Expander expander;

    public NFGActionUtilityComputer(GameState root, Expander expander) {
        this.root = root;
        this.expander = expander;
    }

    @Override
    public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2) {
        //TODO can be done a bit faster if avoiding building the map -- rewrite
        double utility = 0;

        if (s1 == null || s2 == null || s1.getAction().getInformationSet() == null || s2.getAction().getInformationSet() == null)
            throw new IllegalArgumentException();

        Map<Player, Action> actions = new FixedSizeMap<Player, Action>(2);
        actions.put(s1.getAction().getInformationSet().getPlayer(), s1.getAction());
        actions.put(s2.getAction().getInformationSet().getPlayer(), s2.getAction());

        GameState newState = root.performAction(actions.get(root.getPlayerToMove()));
        newState = newState.performAction(actions.get(newState.getPlayerToMove()));

        assert (newState.isGameEnd());

        utility = newState.getUtilities()[0];

        return utility;
    }
}
