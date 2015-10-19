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
package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.Simulator;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.util.List;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class OOSSimulator implements Simulator {
    final private Random rnd;
    final private Expander expander;
    final private int simLenght;

    public OOSSimulator(int simLength, Expander expander, Random random) {
        rnd = random;
        this.expander = expander;
        this.simLenght = simLength;
    }
    
    public OOSSimulator(Expander expander, Random random) {
        this(Integer.MAX_VALUE, expander, random);
    }

    public OOSSimulator(Expander expander) {
        this(Integer.MAX_VALUE, expander, new HighQualityRandom());
    }
    
    public OOSSimulator(Expander expander, long seed) {
        this(Integer.MAX_VALUE, expander, new HighQualityRandom(seed));
    }

    public double playOutProb;
    public double playersProb;
    public double[] playerProb = new double[]{1,1};
    
    @Override
    public double[] simulate(GameState gameState) {
        //fact.l = (fact.bs == 1 && fact.us == 1 ? fact.s : fact.delta*fact.bs + (1-fact.delta)*fact.us);
        playOutProb = 1;
        playersProb = 1;
        playerProb[0]=playerProb[1]=1;

        GameStateImpl state = (GameStateImpl) gameState.copy();
        
        int step=0;
        while (!state.isGameEnd()) {
                if (step==simLenght) return state.evaluate();
                List<Action> actions = expander.getActions(new MCTSInformationSet(state));
                playOutProb *= 1.0/actions.size();//TODO: use correct chance probability
                if (!state.isPlayerToMoveNature()) {
                        playersProb *= 1.0/actions.size();
                        playerProb[state.getPlayerToMove().getId()] *= 1.0/actions.size();
                }
                state.performActionModifyingThisState(actions.get(rnd.nextInt(actions.size())));
                step++;
        }
        return state.getUtilities();
    }
    
    
}
