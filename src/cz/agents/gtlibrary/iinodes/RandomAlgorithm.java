package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.List;
import java.util.Random;

public class RandomAlgorithm implements GamePlayingAlgorithm {
    protected Player player;
    private Expander<? extends InformationSet> expander;
    private Random random;

    public RandomAlgorithm(Player player, Expander<? extends InformationSet> expander) {
        this.expander = expander;
        this.random = new HighQualityRandom();
        this.player = player;
    }

    public RandomAlgorithm(Player player, Expander<? extends InformationSet> expander, long seed) {
        this.expander = expander;
        this.random = new HighQualityRandom(seed);
        this.player = player;
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        List<Action> actions = expander.getActions(gameState);
        if (actions.get(0).getInformationSet().getPlayer().equals(player)) return actions.get(random.nextInt(actions.size()));
        else {
            actions = expander.getActions(gameState.performAction(actions.get(0)));
            return actions.get(random.nextInt(actions.size()));
        }
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
