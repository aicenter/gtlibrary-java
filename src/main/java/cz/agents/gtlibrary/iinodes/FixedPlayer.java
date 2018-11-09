package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielAction;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;
import java.util.Random;

public class FixedPlayer implements GamePlayingAlgorithm {
    private final InnerNode rootNode;
    protected Player player;
    private Expander<? extends InformationSet> expander;
    private MCTSInformationSet currentIS;

    public FixedPlayer(Player player, Expander<MCTSInformationSet> expander, GameState rootState) {
        this.expander = expander;
        this.player = player;

        if (rootState.isPlayerToMoveNature()) {
            this.rootNode = new ChanceNodeImpl(expander, rootState, new Random(0));
            //InnerNode next = (InnerNode) rootNode.getChildFor((Action) (expander.getActions(rootState).get(0)));
        } else {
            this.rootNode = new InnerNodeImpl(expander, rootState);
        }
    }

    private Action staticAction() {
        if (currentIS == null) {
            return null;
        }
        if(expander instanceof GoofSpielExpander) {
            GoofSpielGameState gs = (GoofSpielGameState) currentIS.getAllStates().iterator().next();
            Sequence seq = gs.getSequenceFor(gs.getAllPlayers()[2]);
            GoofSpielAction lastChanceAction = (GoofSpielAction) seq.getLast();

            List<Action> actions = ((GoofSpielExpander) expander).getActions(currentIS);
            for (Action action : actions) {
                if (((GoofSpielAction) action).getValue() == lastChanceAction.getValue()) {
                    return action;
                }
            }
        } else {
            List<Action> actions = ((GoofSpielExpander) expander).getActions(currentIS);
            return actions.get(0);
        }
        return null;
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        return staticAction();
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        return staticAction();
    }

    @Override
    public Action runIterations(int iterations) {
        return staticAction();
    }

    @Override
    public void setCurrentIS(InformationSet currentIS) {
        this.currentIS = (MCTSInformationSet) currentIS;
    }

    @Override
    public InnerNode getRootNode() {
        return rootNode;
    }
}
