package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;
import java.util.Random;

public class RandomAlgorithm implements GamePlayingAlgorithm {

    private Expander<? extends InformationSet> expander;
    private Random random;

    public RandomAlgorithm(Expander<? extends InformationSet> expander) {
        this.expander = expander;
        this.random = new Random();
    }

    public RandomAlgorithm(Expander<? extends InformationSet> expander, long seed) {
        this.expander = expander;
        this.random = new Random(seed);
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        List<Action> actions = expander.getActions(gameState);

        return actions.get(random.nextInt(actions.size()));
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        return null;
    }

    @Override
    public void setCurrentIS(InformationSet currentIS) {

    }

    @Override
    public InnerNode getRootNode() {
        return null;
    }
}
