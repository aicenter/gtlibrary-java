package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.improvedBR;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 5/27/13
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class BestMinmaxImprovementConfig extends DoubleOracleConfig{


    public BestMinmaxImprovementConfig(GameState rootState, GameInfo gameInfo) {
        super(rootState, gameInfo);
    }

}
