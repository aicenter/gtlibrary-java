package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.FeasibilitySequenceFormLP;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;

public class WidthPureRealPlanIterator extends PureRealPlanIterator {

    public WidthPureRealPlanIterator(Player player, StackelbergConfig config, Expander<SequenceInformationSet> expander, FeasibilitySequenceFormLP solver) {
        super(player, config, expander, solver);
    }

    protected void addToQueue(Deque<GameState> queue, GameState state) {
        if (state.getPlayerToMove().equals(follower))
            queue.addLast(state);
        else
            queue.addFirst(state);
    }
}