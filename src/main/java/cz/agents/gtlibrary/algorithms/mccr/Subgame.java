package cz.agents.gtlibrary.algorithms.mccr;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mccr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.PublicState;

import java.util.Set;

// "original" prefix refers to the original game tree formed in the specific domain
// "gadget" prefix refers to the gadget game tree
public interface Subgame {
    PublicState getOriginalRootPublicState();
    Set<InnerNode> getOriginalRootNodes();
    Set<MCTSInformationSet> getOriginalRootInformationSets();
    Set<GameState> getOriginalRootStates();

    GadgetChanceNode getGadgetRoot();

    void resetData(Player player, Set<PublicState> doNotResetAtPlayerPs);
}
