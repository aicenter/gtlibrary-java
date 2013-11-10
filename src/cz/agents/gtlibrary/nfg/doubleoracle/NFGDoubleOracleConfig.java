package cz.agents.gtlibrary.nfg.doubleoracle;

import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 5/29/13
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class NFGDoubleOracleConfig implements AlgorithmConfig{
    @Override
    public InformationSet getInformationSetFor(GameState gameState) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addInformationSetFor(GameState gameState, InformationSet informationSet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InformationSet createInformationSetFor(GameState gameState) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
