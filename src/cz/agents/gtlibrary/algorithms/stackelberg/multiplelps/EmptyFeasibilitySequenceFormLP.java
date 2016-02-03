package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class EmptyFeasibilitySequenceFormLP extends FeasibilitySequenceFormLP {
    public EmptyFeasibilitySequenceFormLP() {
        super();
    }

    public EmptyFeasibilitySequenceFormLP(Player leader, Player follower, StackelbergConfig algConfig, Map<Player, Set<SequenceInformationSet>> informationSets, Map<Player, Set<Sequence>> sequences) {
        super(leader, follower, algConfig, informationSets, sequences);
    }

    @Override
    public boolean checkFeasibilityFor(Iterable<Sequence> partialPureRp) {
        return true;
    }

    @Override
    public boolean checkFeasibilityFor(Iterable<Sequence> pureRP, double maxValue) {
        return true;
    }

    @Override
    public void removeSlackFor(Sequence sequence) {
    }

    @Override
    public void addSlackFor(Sequence sequence) {
    }
}
