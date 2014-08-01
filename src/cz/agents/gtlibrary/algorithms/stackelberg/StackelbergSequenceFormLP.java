package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.Player;

public abstract class StackelbergSequenceFormLP extends SequenceFormLP {
    public StackelbergSequenceFormLP(Player[] players) {
        super(players);
    }

    public abstract double calculateLeaderStrategies(int leaderIdx, int followerIdx, StackelbergConfig algConfig, Expander<SequenceInformationSet> expander);
}
