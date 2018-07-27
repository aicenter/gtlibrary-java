package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.*;

import java.util.LinkedHashSet;
import java.util.Set;

public class PublicStateImpl implements PublicState {
    private static final long serialVersionUID = 3656457672077909L;

    private final PSKey psKey;
    private final LinkedHashSet<GameState> gameStatesInPublicState = new LinkedHashSet<>();
    private final int hashCode;

    public PublicStateImpl(GameState state) {
        this.psKey = state.getPSKeyForPlayerToMove();
        this.gameStatesInPublicState.add(state);
        this.hashCode = psKey.getHash();
    }

    @Override
    public Set<GameState> getAllStates() {
        return gameStatesInPublicState;
    }

    @Override
    public void addStateToPublicState(GameState state) {
        gameStatesInPublicState.add(state);
    }

    @Override
    public void addInfoSetToPublicState(InformationSet informationSet) {
        gameStatesInPublicState.addAll(informationSet.getAllStates());
    }

    @Override
    public PSKey getPSKey() {
        return psKey;
    }

    @Override
    public String toString() {
        return "PS:("+psKey+")";
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
        if (!(obj instanceof PublicState))
            return false;
        PublicState other = (PublicState) obj;

        return this.psKey.equals(other.getPSKey());
    }
}
