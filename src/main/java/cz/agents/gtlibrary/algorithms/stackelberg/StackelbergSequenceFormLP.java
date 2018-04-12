package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.Player;

public abstract class StackelbergSequenceFormLP extends SequenceFormLP {
    protected Player leader;
    protected Player follower;

    public StackelbergSequenceFormLP(Player[] players, Player leader, Player follower) {
        super(players);
        this.leader = leader;
        this.follower = follower;
    }

    public abstract double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander);
}
