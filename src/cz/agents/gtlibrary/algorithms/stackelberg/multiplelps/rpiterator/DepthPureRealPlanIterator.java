package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.FeasibilitySequenceFormLP;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;

public class DepthPureRealPlanIterator extends PureRealPlanIterator {

    public DepthPureRealPlanIterator(Player player, StackelbergConfig config, Expander<SequenceInformationSet> expander, FeasibilitySequenceFormLP solver) {
        super(player, config, expander, solver);
    }

    @Override
    protected void addToQueue(Deque<GameState> queue, GameState state) {
        queue.addFirst(state);
    }
}
