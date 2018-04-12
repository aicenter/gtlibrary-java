package cz.agents.gtlibrary.algorithms.bestresponse;

import cz.agents.gtlibrary.iinodes.ImperfectRecallAlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.HashSet;
import java.util.Set;

public class BRImperfectRecallAlgorithmConfig extends ImperfectRecallAlgorithmConfig {
    private Set<GameState> terminalStates;

    public BRImperfectRecallAlgorithmConfig() {
        terminalStates = new HashSet<>();
    }

    @Override
    public void addInformationSetFor(GameState gameState) {
        super.addInformationSetFor(gameState);
        if(gameState.isGameEnd())
            terminalStates.add(gameState);
    }

    public Set<GameState> getTerminalStates() {
        return terminalStates;
    }
}
