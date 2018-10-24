package cz.agents.gtlibrary.algorithms.cr;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.iinodes.PSKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.*;

public class PublicTreeGenerator {

    public static HashMap<PSKey, MCTSPublicState> constructPublicTree(InnerNode rootNode) {
        MCTSConfig config = rootNode.getAlgConfig();
        Expander expander = rootNode.getExpander();

        Map<PerfectRecallISKey, Set<InnerNode>> augInfoSets = buildCompleteTree(rootNode);
        HashMap<PSKey, MCTSPublicState> publicStates = new HashMap<>();

        Set<InnerNode> processed = new HashSet<>();
        int hc = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.add(rootNode);
        while (!q.isEmpty()) {
            InnerNode in = q.removeFirst();
            if (!processed.contains(in) && !(in instanceof ChanceNode)) {
                PSKey psKey = new PSKey(hc++);
                GameState gameState = in.getGameState();
                gameState.setPSKeyForPlayerToMove(psKey);

                MCTSPublicState ps = new MCTSPublicState(config, expander, in,
                        config.getParentPublicState(in),
                        config.getPlayerParentPublicState(in));
                config.addPublicState(ps);

                Set<InnerNode> psNodes = new HashSet<>();
                ArrayDeque<InnerNode> qps = new ArrayDeque<>();

                Set<InnerNode> initialSet = augInfoSets.get(in.getOpponentAugISKey());
                assert initialSet.contains(in);
                qps.addAll(initialSet);

                while(!qps.isEmpty()) {
                    InnerNode ps_in = qps.removeFirst();

                    if(!psNodes.contains(ps_in)) {
                        qps.addAll(augInfoSets.get(ps_in.getOpponentAugISKey()));
                        qps.addAll(ps_in.getInformationSet().getAllNodes());
                    }
                    psNodes.add(ps_in);
                    processed.add(ps_in);
                }

                psNodes.forEach(ps::addNodeToPublicState);
                psNodes.forEach(n -> n.setPublicState(ps));
                publicStates.put(psKey, ps);
            }
            for (Action a : in.getActions()) {
                Node next = in.getChildFor(a);
                if (next instanceof InnerNode) {
                    q.add((InnerNode) next);
                }
            }

        }
        System.err.println("Constructed public tree");

        InnerNodeImpl.attendPS = true;
        return publicStates;
    }

    private static Map<PerfectRecallISKey, Set<InnerNode>> buildCompleteTree(InnerNode r) {
        System.err.println("Building complete tree.");
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);

        Map<PerfectRecallISKey, Set<InnerNode>> augInfoSets = new HashMap<>();
        Set<InnerNode> s;
        while (!q.isEmpty()) {
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if(!(n instanceof ChanceNode)) {
                PerfectRecallISKey augiskey = n.getOpponentAugISKey();
                if (augInfoSets.containsKey(augiskey)) {
                    s = augInfoSets.get(augiskey);
                } else {
                    s = new HashSet<>();
                }
                s.add(n);
                augInfoSets.put(augiskey, s);
            }

            for (Action a : n.getActions()) {
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
        return augInfoSets;
    }
}
