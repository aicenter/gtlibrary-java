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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class Exp3BackPropFactory implements BackPropFactory  {
    public double gamma = 0.05;
    public boolean useCFRValues = false;
    public boolean storeExploration = false;
    public boolean useExp3L = false;
    Random random;
    /** Each player's contribution to the probability of being in current IS. */
    double[] pi = new double[]{1,1,1};
    ArrayDeque<Double> pis = new ArrayDeque<Double>();
    
    private double minUtility;
    private double maxUtility;


    public Exp3BackPropFactory(double minUtility, double maxUtility, double gamma, boolean storeExploration) {
        this(minUtility, maxUtility, gamma, storeExploration, new HighQualityRandom());
    }

    public Exp3BackPropFactory(double minUtility, double maxUtility, double gamma, boolean storeExploration, Random random) {
        this(minUtility, maxUtility, gamma, random);
        this.storeExploration = storeExploration;
    }

    public Exp3BackPropFactory(double minUtility, double maxUtility, double gamma) {
        this(minUtility, maxUtility, gamma, new HighQualityRandom());
    }
    
    public Exp3BackPropFactory(double minUtility, double maxUtility, double gamma, Random random) {
        this.minUtility = minUtility;
        this.maxUtility = maxUtility;
        this.gamma = gamma;
        this.random = random;
    }
    
    public double normalizeValue(double value) {       
        assert minUtility <= value + 1e-5 && value <= maxUtility + 1e-5;
        return (value - minUtility) / (maxUtility - minUtility);
//        assert minUtility == 0 && maxUtility == 1;
//        return reward;
    }
    
    public double valuesSpread() {
        return maxUtility - minUtility;
    }

    @Override
    public Selector createSelector(List<Action> actions) {
        return useExp3L ? new Exp3LSelector(actions, this) : new Exp3Selector(actions,this);
    }
    
    @Override
    public Selector createSelector(int N) {
        return useExp3L ? new Exp3LSelector(N, this) : new Exp3Selector(N,this);
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
