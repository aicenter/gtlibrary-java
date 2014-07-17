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
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.nfg.PureStrategy;
import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolver;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;

/**
 *
 * @author vilo
 */
public class GreedyConfidenceSelector implements SelectionStrategy {
    ZeroSumGameNESolver<ActionIdx, ActionIdx> NEsolver;
    double[][] curMatrix;

    public GreedyConfidenceSelector(BackPropFactory fact, MCTSInformationSet infSet){
        this.NEsolver = new ZeroSumGameNESolverImpl<ActionIdx, ActionIdx>(null);
        //make curmatrix
    }


    @Override
    public Action select() {
        //
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double onBackPropagate(InnerNode node, Action actions, double value) {
        //TODO: substitute matrix
        //TODO: check if the strategy has to be recomputed = is in support or got better by more than slack
        NEsolver.computeNashEquilibrium();
        return NEsolver.getGameValue();
    }
    
    public static class ActionIdx  implements PureStrategy {
        int i;
        public ActionIdx(int i) {
            this.i = i;
        }
    }
    
    public static class UtilityComp extends Utility<ActionIdx, ActionIdx> {
        double[][] curMatrix;

        public UtilityComp(double[][] curMatrix) {
            this.curMatrix = curMatrix;
        }
                
        @Override
        public double getUtility(ActionIdx s1, ActionIdx s2) {
            return curMatrix[s1.i][s2.i];
        }
        
    }
    
}
