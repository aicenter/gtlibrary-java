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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Selector;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.Pair;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vilo
 */
public class SMConjuctureSelector extends SMDecoupledSelector implements MeanStrategyProvider {
    double w[][];
    double sumw[][];
    double wsum[][];
    BasicStats stats [][];
    double max[][];
    
    public SMConjuctureSelector(List<Action> actions1, List<Action> actions2, Selector p1selector, Selector p2selector) {
        super(actions1, actions2, p1selector, p2selector);
        w = new double[actions1.size()][actions2.size()];
        for (double[] a : w) Arrays.fill(a, 1);
        sumw = new double[actions1.size()][actions2.size()];
        wsum = new double[actions1.size()][actions2.size()];
        max = new double[actions1.size()][actions2.size()];
        stats = new BasicStats[actions1.size()][actions2.size()];
        for (int i=0;i<actions1.size();i++){
            for (int j=0; j<actions2.size();j++){
                stats[i][j]=new BasicStats();
            }
        }
    }

    @Override
    public void update(Pair<Integer, Integer> selection, double value) {
        final int si=selection.getLeft();
        final int sj=selection.getRight();
        stats[si][sj].onBackPropagate(value);
        for (int i=0; i<actions1.size();i++){
            if (i==si) continue;
            w[i][sj]++;
        }
        for (int j=0; j<actions1.size();j++){
            if (j==sj) continue;
            w[si][j]++;
        }
        wsum[si][sj] += w[si][sj]*value;
        sumw[si][sj] += w[si][sj];
        
        max[si][sj] = Math.max(max[si][sj], Math.abs(stats[si][sj].getEV()-(wsum[si][sj]/sumw[si][sj])));
        //if (stats[si][sj].getNbSamples() % 1e5==0) 
        //    System.out.println(stats[si][sj].getNbSamples() + ": " + Math.abs(stats[si][sj].getEV()-(wsum[si][sj]/sumw[si][sj])));
        super.update(selection, value);
    }

    @Override
    public List<Action> getActions() {
        return actions1;
    }

    @Override
    public double[] getMp() {
        return ((MeanStrategyProvider)p1selector).getMp();
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        for (int i=0;i<actions1.size();i++){
            for (int j=0; j<actions2.size();j++){
                out.append("Move" + i + j + " ");
                out.append(stats[i][j].getNbSamples() + " ");
                out.append(Math.abs(stats[i][j].getEV()-(wsum[i][j]/sumw[i][j])) + " ");
                out.append(stats[i][j].getEV() + " ");
                out.append((wsum[i][j]/sumw[i][j]) + " ");
                out.append(max[i][j] + "\n");
                max[i][j]=Math.abs(stats[i][j].getEV()-(wsum[i][j]/sumw[i][j]));
            }
        }
//        out.append("\n");
//        for (int i=0;i<actions1.size();i++){
//            for (int j=0; j<actions2.size();j++){
//                out.append(Math.abs(stats[i][j].getEV()-(wsum[i][j]/sumw[i][j])) + " ");
//            }
//            out.append("\n");
//        }
//        out.append("\n");
//        for (int i=0;i<actions1.size();i++){
//            for (int j=0; j<actions2.size();j++){
//                out.append(stats[i][j].getEV() + " ");
//            }
//            out.append("\n");
//        }
//        out.append("\n");
//        for (int i=0;i<actions1.size();i++){
//            for (int j=0; j<actions2.size();j++){
//                out.append((wsum[i][j]/sumw[i][j]) + " ");
//            }
//            out.append("\n");
//        }
//        out.append("\n");
//        for (int i=0;i<actions1.size();i++){
//            for (int j=0; j<actions2.size();j++){
//                out.append(max[i][j] + " ");
//                max[i][j]=0;
//            }
//            out.append("\n");
//        }
        return out.toString();
    }
    
    
}
