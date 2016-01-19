/*
Best solution would be to create a common ancestor of IRInformationSetImpl and
InformationSetImpl, which in fact implements perfect recall information set.
 */
package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.PerfectRecallInformationSet;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class IRInformationSetImpl implements InformationSet {
    protected Player player;
    protected LinkedHashSet<GameState> statesInInformationSet = new LinkedHashSet<GameState>();
    private final int hashCode;

    public IRInformationSetImpl(GameState state) {
        this.player = state.getPlayerToMove();
        this.statesInInformationSet.add(state);
        this.hashCode = state.getISKeyForPlayerToMove().hashCode();
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
        return true;
    }

    public void addStateToIS(GameState state) {
        assert state.getPlayerToMove().equals(player);
        statesInInformationSet.add(state);
    }

    public void addAllStatesToIS(Collection<GameState> states) {
        for (GameState gameState : states) {
            assert gameState.getPlayerToMove().equals(player);
        }
        statesInInformationSet.addAll(states);
    }

    @Override
    public Set<GameState> getAllStates() {
        return statesInInformationSet;
    }

    @Override
    public ISKey getISKey() {
        return statesInInformationSet.iterator().next().getISKeyForPlayerToMove();
    }

    @Override
    public String toString() {
        return "IS:(" + player + ")";
    }
}
