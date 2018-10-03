package cz.agents.gtlibrary.algorithms.mcts.behavioral;

import cz.agents.gtlibrary.algorithms.mcts.selectstrat.RMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.RMSelector;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Selector;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.List;
import java.util.Random;

/**
 * Created by Jakub Cerny on 04/09/2018.
 */
public class RMTSBackPropFactory extends RMBackPropFactory {

    protected Player opponent;

    public RMTSBackPropFactory(double minUtility, double maxUtility, double gamma, Player opponent) {
        super(minUtility, maxUtility, gamma);
        this.opponent = opponent;
    }

    public RMTSBackPropFactory(double minUtility, double maxUtility, double gamma, Random random, Player opponent) {
        super(minUtility, maxUtility, gamma, random);
        this.opponent = opponent;
    }

    public RMTSBackPropFactory(double gamma, Player opponent) {
        super(gamma);
        this.opponent = opponent;
    }

    @Override
    public Selector createSelector(List<Action> actions) {
        return new RMTSSelector(actions, this);
    }

    @Override
    public Selector createSelector(int N) {
        return new RMTSSelector(N, this);
    }
}
