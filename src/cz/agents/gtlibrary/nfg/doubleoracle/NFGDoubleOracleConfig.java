/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.nfg.doubleoracle;

import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 5/29/13
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class NFGDoubleOracleConfig implements AlgorithmConfig {
    @Override
    public InformationSet getInformationSetFor(GameState gameState) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addInformationSetFor(GameState gameState, InformationSet informationSet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addInformationSetFor(GameState gameState) {

    }

    @Override
    public InformationSet createInformationSetFor(GameState gameState) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Double getActualNonzeroUtilityValues(GameState gameState) {
        return null;
    }

    @Override
    public void setUtility(GameState gameState, double utility) {

    }

    @Override
    public HashMap getAllInformationSets() {
        return null;
    }
}
