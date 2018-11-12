package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.algorithms.cr.ResolvingMethod;
import cz.agents.gtlibrary.algorithms.cr.Subgame;
import cz.agents.gtlibrary.algorithms.cr.SubgameImpl;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;
import java.util.stream.Collectors;

public class PublicStateImpl implements PublicState {
    private static final long serialVersionUID = 3656457672077909L;

    private PSKey psKey;
    private LinkedHashSet<GameState> gameStatesInPublicState = new LinkedHashSet<>();
    private LinkedHashSet<InnerNode> gameNodesInPublicState = new LinkedHashSet<>();
    private int hashCode;
    private MCTSConfig config;
    private PublicStateImpl parentPublicState;
    private PublicStateImpl playerParentPublicState;
    private int depth;
    private Expander expander;
    private int resolvingIterations = 0;
    private ResolvingMethod resolvingMethod;
    private boolean dataKeeping;
    public boolean destroyed = false;

    public PublicStateImpl(MCTSConfig config,
                           Expander expander,
                           InnerNode node,
                           PublicStateImpl parentPublicState,
                           PublicStateImpl playerParentPublicState) {
        this.expander = expander;
        this.config = config;
        this.parentPublicState = parentPublicState;
        this.playerParentPublicState = playerParentPublicState;
        if (parentPublicState != null) {
            depth = parentPublicState.getDepth() + 1;
        } else {
            depth = 0;
        }

        this.gameNodesInPublicState.add(node);
        GameState state = node.getGameState();
        this.gameStatesInPublicState.add(state);

        this.psKey = state.getPSKeyForPlayerToMove();
        assert this.psKey != null;
        this.hashCode = psKey.hashCode();
    }

    @Override
    public Set<GameState> getAllStates() {
        return gameStatesInPublicState;
    }

    @Override
    public Set<MCTSInformationSet> getAllInformationSets() {
        Set<MCTSInformationSet> informationSets = new HashSet<>();
        for (InnerNode node : gameNodesInPublicState) {
            informationSets.add(node.getInformationSet());
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
    public Set<PublicState> getNextPlayerPublicStates(Player player, boolean buildStates) {
        boolean playerSpecified = player != null;

        Set<PublicState> nextPS = new HashSet<>();
        ArrayDeque<Node> q = new ArrayDeque<>();
        if(buildStates) gameNodesInPublicState.forEach(InnerNode::buildChildren);
        gameNodesInPublicState.forEach(node -> q.addAll(node.getChildren().values()));

        while (!q.isEmpty()) {
            Node nextNode = q.removeFirst();

            if (nextNode instanceof LeafNode) continue;
            if (nextNode instanceof ChanceNode) {
                if(buildStates) ((ChanceNode) nextNode).buildChildren();
                q.addAll(((ChanceNode) nextNode).getChildren().values());
                continue;
            }
            InnerNode innerNode = (InnerNode) nextNode;

            // init data
            if (innerNode.getInformationSet().getAlgorithmData() == null) {
                innerNode.getInformationSet().setAlgorithmData(
                        new OOSAlgorithmData(innerNode.getActions()));
            }

            if(innerNode.getPublicState().equals(this)
                    || (playerSpecified && !innerNode.getPlayerToMove().equals(player))) {
                if(buildStates) innerNode.buildChildren();
                q.addAll(innerNode.getChildren().values());
                continue;
            }

            nextPS.add(innerNode.getPublicState());
        }

        return nextPS;
    }


    @Override
    public Set<PublicState> getNextPlayerPublicStates(Player player) {
        return getNextPlayerPublicStates(player, false);
    }

    @Override
    public Set<PublicState> getNextPublicStates() {
        return getNextPlayerPublicStates(null);
    }

    @Override
    public Player getPlayer() {
        // todo: PS with chance
        return getAllNodes().iterator().next().getPlayerToMove();
    }


    @Override
    public void resetData(boolean includingThisPublicState) {
        ArrayDeque<Node> q = new ArrayDeque<Node>();

        if (includingThisPublicState) {
            q.addAll(getAllNodes());
            setResolvingMethod(null);
            setResolvingIterations(0);
            setDataKeeping(false);
        } else {
            // todo: only works in nice games!
            getAllNodes().forEach(in -> q.addAll(in.getChildren().values()));
        }

        // reset only walks on existing nodes, it doesn't expand the tree!
        // (difference between `innerNode.getChildren()` and `innerNode.getChildFor(action)`
        while (!q.isEmpty()) {
            Node n = q.removeFirst();
            if (n instanceof LeafNode) continue;
            ;
            InnerNode in = (InnerNode) n;
            in.resetData();

            MCTSInformationSet is = in.getInformationSet();
            if (is != null && is.getAlgorithmData() != null) {
                OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();
                data.resetData();
            }

            for (Map.Entry<Action, Node> entry : in.getChildren().entrySet()) {
                Node ch = entry.getValue();
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
    }

    @Override
    public ResolvingMethod getResolvingMethod() {
        return resolvingMethod;
    }

    @Override
    public void setResolvingMethod(ResolvingMethod resolvingMethod) {
        this.resolvingMethod = resolvingMethod;
    }

    @Override
    public PSKey getPSKey() {
        return psKey;
    }

    @Override
    public String toString() {
        return "PS:(" + psKey + ")";
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this.hashCode() != obj.hashCode())
            return false;
        if (!(obj instanceof PublicState))
            return false;
        PublicState other = (PublicState) obj;

        if(other.getPlayer().getId() != this.getPlayer().getId())
            return false;
        if(other.getDepth() != this.getDepth())
            return false;
        if(other.getParentPublicState() == null ^ this.getParentPublicState() == null)
           return false;
        if(other.getPlayerParentPublicState() == null ^ this.getPlayerParentPublicState() == null)
           return false;
        if(other.getParentPublicState() != null && !other.getParentPublicState().getPSKey().equals(this.getParentPublicState().getPSKey()))
            return false;
        if(other.getPlayerParentPublicState() != null && !other.getPlayerParentPublicState().getPSKey().equals(this.getPlayerParentPublicState().getPSKey()))
            return false;
        return this.psKey.equals(other.getPSKey());
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public PublicState getParentPublicState() {
        return parentPublicState;
    }

    @Override
    public PublicState getPlayerParentPublicState() {
        return playerParentPublicState;
    }

    @Override
    public int getResolvingIterations() {
        return resolvingIterations;
    }

    @Override
    public void setResolvingIterations(int iterations) {
        this.resolvingIterations = iterations;
    }

    @Override
    public void incrResolvingIterations(int iterations) {
        resolvingIterations += iterations;
    }

    @Override
    public boolean isDataKeeping() {
        return dataKeeping;
    }

    @Override
    public void setDataKeeping(boolean b) {
        this.dataKeeping = b;
    }

    @Override
    public Subgame getSubgame() {
        return new SubgameImpl(this, config, expander, null);
    }

    @Override
    public Subgame getSubgame(MCTSInformationSet currentIs) {
        return new SubgameImpl(this, config, expander, currentIs);
    }

    @Override
    public boolean isReachable(Player player) {
        return getAllNodes().stream()
                .map(in -> in.getReachPrByPlayer(player))
                .anyMatch(p -> p > 0);
    }

    @Override
    public void destroy() {
        destroyed = true;
        resolvingMethod = null;
        psKey = null;
        gameStatesInPublicState = null;
        gameNodesInPublicState = null;
        config = null;
        parentPublicState = null;
        playerParentPublicState = null;
        expander = null;
    }

    @Override
    public Set<InnerNode> getTopMostOriginalNodes() {
        return gameNodesInPublicState.stream()
                .filter(n -> !gameNodesInPublicState.contains(n.getParent()))
                .collect(Collectors.toSet());

    }
}
