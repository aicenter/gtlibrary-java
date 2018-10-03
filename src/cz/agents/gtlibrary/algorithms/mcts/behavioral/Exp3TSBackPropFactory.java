package cz.agents.gtlibrary.algorithms.mcts.behavioral;

import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3LSelector;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3Selector;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Selector;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.List;

/**
 * Created by Jakub Cerny on 03/09/2018.
 */
public class Exp3TSBackPropFactory extends Exp3BackPropFactory {

    protected Player opponent;

    public Exp3TSBackPropFactory(double minUtility, double maxUtility, double gamma, boolean storeExploration, Player opponent) {
        super(minUtility, maxUtility, gamma, storeExploration);
        this.opponent = opponent;
    }

    @Override
    public Selector createSelector(List<Action> actions) {
        return new Exp3TSSelector(actions,this);
    }

    @Override
    public Selector createSelector(int N) {
        return new Exp3TSSelector(N,this);
    }


}
