package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.GameState;

public class ImperfectRecallAlgorithmConfig extends ConfigImpl<IRInformationSetImpl> {
    @Override
    public IRInformationSetImpl createInformationSetFor(GameState gameState) {
        return new IRInformationSetImpl(gameState);
    }
}
