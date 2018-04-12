package cz.agents.gtlibrary.algorithms.cfr.ir;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.iinodes.ImperfectRecallAlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;

public class IRCFRConfig extends ConfigImpl<IRCFRInformationSet> {
    @Override
    public IRCFRInformationSet createInformationSetFor(GameState gameState) {
        return new IRCFRInformationSet(gameState);
    }
}
