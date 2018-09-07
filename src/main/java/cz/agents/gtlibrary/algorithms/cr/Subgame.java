package cz.agents.gtlibrary.algorithms.cr;

import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInfoSet;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInnerNode;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInnerState;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.PublicState;

import java.util.Set;

// "original" prefix refers to the original game tree formed in the specific domain
// "gadget" prefix refers to the gadget game tree
public interface Subgame {
    PublicState getPublicState();
    Set<InnerNode> getOriginalNodes();
    Set<MCTSInformationSet> getOriginalInformationSets();
    Set<GameState> getOriginalStates();

    Set<GadgetInfoSet> getGadgetInformationSets();
    Set<GadgetInnerNode> getGadgetNodes();
    Set<GadgetInnerState> getGadgetStates();

    GadgetChanceNode getGadgetRoot();
}
