package cz.agents.gtlibrary.algorithms.crswfabstraction;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

/**
 * The most basic possible config
 */
public class CrswfConfig extends ConfigImpl<InformationSet> {

    public CrswfConfig() {
        super();
        CrswfInformationSet.resetIDCounter();
    }

    @Override
    public InformationSet createInformationSetFor(GameState gameState) {
        return new CrswfInformationSet(gameState);
    }
}
