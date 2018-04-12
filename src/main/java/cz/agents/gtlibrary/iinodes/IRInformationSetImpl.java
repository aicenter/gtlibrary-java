/*
Best solution would be to create a common ancestor of IRInformationSetImpl and
InformationSetImpl, which in fact implements perfect recall information set.
 */
package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class IRInformationSetImpl implements InformationSet, Serializable {
    protected Player player;
    protected LinkedHashSet<GameState> statesInformationSet = new LinkedHashSet<>();
    private final int hashCode;
    private final ImperfectRecallISKey key;

    public IRInformationSetImpl(GameState state) {
        this.player = state.getPlayerToMove();
        this.statesInformationSet.add(state);
//        if (state.getISKeyForPlayerToMove() instanceof PerfectRecallISKey)
//            this.key = wrap((PerfectRecallISKey) state.getISKeyForPlayerToMove());
//        else
            this.key = (ImperfectRecallISKey) state.getISKeyForPlayerToMove();
        this.hashCode = key.hashCode();
    }

    public IRInformationSetImpl(GameState state, ImperfectRecallISKey isKey) {
        this.player = state.getPlayerToMove();
        this.statesInformationSet.add(state);
//        if (state.getISKeyForPlayerToMove() instanceof PerfectRecallISKey)
//            this.key = wrap((PerfectRecallISKey) state.getISKeyForPlayerToMove());
//        else
        this.key = isKey;
        this.hashCode = key.hashCode();
    }

    private ImperfectRecallISKey wrap(PerfectRecallISKey isKeyForPlayerToMove) {
        Observations perfectRecallObservations = new Observations(player, player);

        perfectRecallObservations.add(new PerfectRecallObservation(isKeyForPlayerToMove));
        return new ImperfectRecallISKey(perfectRecallObservations, null, null);
    }

    @Override
    public Player getPlayer() {
        return player;
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
        if (!(obj instanceof IRInformationSetImpl))
            return false;
        IRInformationSetImpl other = (IRInformationSetImpl) obj;

        if (!this.player.equals(other.getPlayer()))
            return false;
        if (!this.key.equals(other.key))
            return false;
        return true;
    }

    public void addStateToIS(GameState state) {
        assert state.getPlayerToMove().equals(player);
        statesInformationSet.add(state);
    }

    public void addAllStatesToIS(Collection<GameState> states) {
        states.forEach(this::addStateToIS);
    }

    @Override
    public Set<GameState> getAllStates() {
        return statesInformationSet;
    }

    @Override
    public ISKey getISKey() {
        return key;
    }

    @Override
    public String toString() {
        return "IS:(" + statesInformationSet + ")";
    }
}
