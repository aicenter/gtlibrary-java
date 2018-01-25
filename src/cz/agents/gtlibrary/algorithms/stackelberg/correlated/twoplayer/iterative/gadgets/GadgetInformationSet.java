package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.PerfectRecallInformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;

/**
 * Created by Jakub Cerny on 11/12/2017.
 */
public class GadgetInformationSet extends SequenceInformationSet {

    private final int hashCode;
    private final ISKey key;

    public GadgetInformationSet(GameState state, Sequence sequence) {
        super(state);
        hashCode = state.hashCode();
        key = new PerfectRecallISKey(hashCode, sequence);
    }

    @Override
    public ISKey getISKey() {
        return key;
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
        if (!(obj instanceof PerfectRecallInformationSet))
            return false;
        PerfectRecallInformationSet other = (PerfectRecallInformationSet) obj;

        if (!this.player.equals(other.getPlayer()))
            return false;
        if (!this.playerHistory.equals(other.getPlayersHistory()))
            return false;
        return true;
    }

}
