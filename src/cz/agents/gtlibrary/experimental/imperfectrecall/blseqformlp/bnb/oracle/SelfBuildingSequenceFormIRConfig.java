package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.interfaces.GameState;

public class SelfBuildingSequenceFormIRConfig extends SequenceFormIRConfig {
    @Override
    public SequenceFormIRInformationSet getInformationSetFor(GameState gameState) {
        SequenceFormIRInformationSet is = super.getInformationSetFor(gameState);

        if (is == null)
            return createInformationSetFor(gameState);
        return is;
    }
}
