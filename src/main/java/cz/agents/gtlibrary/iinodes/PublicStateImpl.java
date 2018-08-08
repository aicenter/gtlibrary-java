package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class PublicStateImpl implements PublicState {
    private static final long serialVersionUID = 3656457672077909L;

    private final PSKey psKey;
    private final LinkedHashSet<GameState> gameStatesInPublicState = new LinkedHashSet<>();
    private final LinkedHashSet<InnerNode> gameNodesInPublicState = new LinkedHashSet<>();
    private final int hashCode;
    private final MCTSConfig config;

    public PublicStateImpl(MCTSConfig config, InnerNode node) {
        this.config = config;

        this.gameNodesInPublicState.add(node);
        GameState state = node.getGameState();
        this.gameStatesInPublicState.add(state);

        this.psKey = ((DomainWithPublicState) state).getPSKeyForPlayerToMove();
        this.hashCode = psKey.getHash();
    }

    @Override
    public Set<GameState> getAllStates() {
        return gameStatesInPublicState;
    }

    @Override
    public Set<MCTSInformationSet> getAllInformationSets() {
        Set<MCTSInformationSet> informationSets = new HashSet<>();
        for (GameState gameState : gameStatesInPublicState) {
            informationSets.add(config.getInformationSetFor(gameState));
        }
        return informationSets;
    }

    @Override
    public Set<InnerNode> getAllNodes() {
        return gameNodesInPublicState;
    }

    @Override
    public void addNodeToPublicState(InnerNode node) {
        gameNodesInPublicState.add(node);
        gameStatesInPublicState.add(node.getGameState());
    }

    @Override
    public PSKey getPSKey() {
        return psKey;
    }

    @Override
    public String toString() {
        return "PS:("+psKey+")";
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this.hashCode != obj.hashCode())
            return false;
        if (!(obj instanceof PublicState))
            return false;
        PublicState other = (PublicState) obj;

        return this.psKey.equals(other.getPSKey());
    }
}
