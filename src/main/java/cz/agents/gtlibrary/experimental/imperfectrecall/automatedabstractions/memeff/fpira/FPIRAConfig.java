package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.fpira;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;

import java.io.Serializable;

public class FPIRAConfig extends ConfigImpl<MCTSInformationSet> {
    @Override
    public MCTSInformationSet createInformationSetFor(GameState gameState) {
        return new MCTSInformationSet(gameState);
    }

    @Override
    public MCTSInformationSet getInformationSetFor(GameState gameState) {
        return new MCTSInformationSet(gameState);
    }
}
