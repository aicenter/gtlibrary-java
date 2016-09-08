/*
Best solution would be to create a common ancestor of IRInformationSetImpl and
InformationSetImpl, which in fact implements perfect recall information set.
 */
package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class IRInformationSetImpl implements InformationSet {
    protected Player player;
    protected LinkedHashSet<GameState> statesInformationSet = new LinkedHashSet<GameState>();
    private final int hashCode;
    private final ImperfectRecallISKey key;

    public IRInformationSetImpl(GameState state) {
        this.player = state.getPlayerToMove();
        this.statesInformationSet.add(state);
        this.key = (ImperfectRecallISKey) state.getISKeyForPlayerToMove();
        this.hashCode = key.hashCode();
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
