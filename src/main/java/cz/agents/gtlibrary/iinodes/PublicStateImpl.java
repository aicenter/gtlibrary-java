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

public class PublicStateImpl implements PublicState {
    private static final long serialVersionUID = 3656457672077909L;

    private final PSKey psKey;
    private final LinkedHashSet<GameState> gameStatesInPublicState = new LinkedHashSet<>();
    private final LinkedHashSet<InnerNode> gameNodesInPublicState = new LinkedHashSet<>();
    private final int hashCode;
    private final MCTSConfig config;
    private final PublicStateImpl parentPublicState;
    private final PublicStateImpl playerParentPublicState;
    private final int depth;
    private final Expander expander;
    private int resolvingIterations = 0;
    private boolean dataKeeping;

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
        for (InnerNode node: gameNodesInPublicState) {
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
    public Set<PublicState> getNextPlayerPublicStates(Player player) {
        Set<PublicState> nextPS = new HashSet<>();
        ArrayDeque<Node> q = new ArrayDeque<>();
        gameNodesInPublicState.forEach(node -> q.addAll(node.buildChildren().values()));

        while(!q.isEmpty()) {
            Node nextNode = q.removeFirst();

            if(nextNode instanceof LeafNode) continue;
            if (nextNode instanceof ChanceNode) {
                q.addAll(  ((ChanceNode) nextNode).buildChildren().values());
            } else {
                InnerNode innerNode = (InnerNode) nextNode;

                // init data
                if(innerNode.getInformationSet().getAlgorithmData() == null) {
                    innerNode.getInformationSet().setAlgorithmData(
                            new OOSAlgorithmData(innerNode.getActions()));
                }
                if(innerNode.getPlayerToMove().equals(player)) {
                    nextPS.add(innerNode.getPublicState());
                } else {
                    q.addAll(innerNode.buildChildren().values());
                }
            }
        }

        return nextPS;
    }

    @Override
    public Set<PublicState> getNextPublicStates() {
        Set<PublicState> nextPS = new HashSet<>();
        for (InnerNode node : gameNodesInPublicState) {
            for(Action a : node.getActions()) {
                Node nextNode = node.getChildFor(a);
                if(nextNode instanceof LeafNode) continue;
                if (nextNode instanceof ChanceNode) {
                    nextPS.addAll(((ChanceNode) nextNode).getPublicState().getNextPublicStates());
                } else {
                    InnerNode innerNode = (InnerNode) nextNode;
                    // init data
                    if(innerNode.getInformationSet().getAlgorithmData() == null) {
                        innerNode.getInformationSet().setAlgorithmData(
                                new OOSAlgorithmData(innerNode.getActions()));
                    }
                    nextPS.add(innerNode.getPublicState());
                }
            }
        }
        return nextPS;
    }

    @Override
    public Player getPlayer() {
        // todo: PS with chance
        return getAllNodes().iterator().next().getPlayerToMove();
    }


    @Override
    public void resetData() {
        setResolvingMethod(null);
        setResolvingIterations(0);
        setDataKeeping(false);

        ArrayDeque<Node> q = new ArrayDeque<Node>();
        q.addAll(getAllNodes());

        while (!q.isEmpty()) {
            Node n = q.removeFirst();
            if(n instanceof LeafNode) continue;;
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
    public void setResolvingMethod(ResolvingMethod resolveMccfr) {

    }

    @Override
    public ResolvingMethod getResolvingMethod() {
        return null;
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
        if (this.hashCode != obj.hashCode())
            return false;
        if (!(obj instanceof PublicState))
            return false;
        PublicState other = (PublicState) obj;

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
    public void setDataKeeping(boolean b) {
        this.dataKeeping = b;
    }

    @Override
    public boolean isDataKeeping() {
        return dataKeeping;
    }

    @Override
    public Subgame getSubgame() {
        return new SubgameImpl(this, config, expander);
    }
}
