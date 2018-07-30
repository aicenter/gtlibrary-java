package cz.agents.gtlibrary.algorithms.cfr.br;

import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Player;

/**
 * Created by Jakub Cerny on 09/04/2018.
 */
public abstract class BestResponse {

    protected Player respondingPlayer;
    protected Node rootState;

    public BestResponse(Player respondingPlayer, Node root){
        this.respondingPlayer = respondingPlayer;
        this.rootState = root;
    }

    public abstract void computeBR(Node root);
}
