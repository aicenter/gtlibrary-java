package cz.agents.gtlibrary.experimental.imperfectrecall.dag;

import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.interfaces.GameState;

public class DAGInformationSet extends IRInformationSetImpl {

    public DAGInformationSet(DAGGameState state) {
        super((GameState) state);
    }
}
