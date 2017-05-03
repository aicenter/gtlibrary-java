package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.FeasibilitySequenceFormLP;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

import java.util.List;

public class NoCutDepthPureRealPlanIterator extends DepthPureRealPlanIterator {

    public NoCutDepthPureRealPlanIterator(Player player, StackelbergConfig config, Expander<SequenceInformationSet> expander, FeasibilitySequenceFormLP solver) {
        super(player, config, expander, solver);
    }

    protected void updateRealizationPlan(int index) {
        for (int i = index; i < stack.size(); i++) {
            Pair<SequenceInformationSet, List<Action>> setActionPair = stack.get(i);

            if (currentSet.contains(setActionPair.getLeft().getPlayersHistory())) {
                Sequence continuation = new ArrayListSequenceImpl(setActionPair.getLeft().getPlayersHistory());
                Action lastAction = setActionPair.getRight().get(setActionPair.getRight().size() - 1);

                continuation.addLast(lastAction);
                currentSet.add(continuation);
                solver.removeSlackFor(continuation);
            }
        }
    }
}
