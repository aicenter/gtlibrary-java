package cz.agents.gtlibrary.algorithms.cfr.br.responses;

import cz.agents.gtlibrary.algorithms.cfr.br.CFRBRAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

/**
 * Created by Jakub Cerny on 09/04/2018.
 */
public abstract class BestResponse {

    protected Player respondingPlayer;
    protected int respondingPlayerIndex;
    protected Node rootState;

    /* irrelevant constant */
    protected final int FAKE_LEADER_IDX = -1;

    public BestResponse(Player respondingPlayer, Node root){
        this.respondingPlayer = respondingPlayer;
        this.respondingPlayerIndex = respondingPlayer.getId();
        this.rootState = root;
    }

    public BestResponse(Player respondingPlayer, int respondingPlayerIndex, Node root){
        this.respondingPlayer = respondingPlayer;
        this.respondingPlayerIndex = respondingPlayerIndex;
        this.rootState = root;
    }

    protected double getNodeBelief(Node node){
        InnerNode parent = node.getParent();
        Action action = node.getLastAction();
        double belief = 1.0;
        while(parent != null){
            if(belief <= 0.0) break;
            if(parent instanceof ChanceNode){
                belief *= ((ChanceNode)parent).getProbabilityOfNatureFor(action);
            }
            else if(parent.getInformationSet().getPlayer().getId() != respondingPlayer.getId()){
                belief *= ((CFRBRAlgorithmData)parent.getInformationSet().getAlgorithmData())
                        .getProbabilityOfPlaying(action);
            }
            action = parent.getLastAction();
            parent = parent.getParent();
        }
        return belief;
    }

    public abstract double computeBR(Node root);
}
