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

package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.Pair;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vilo
 */
public class SMConjuctureSelector implements SMSelector, MeanStrategyProvider{
    SMSelector coreSelector;
    double w[][];
    double sumw[][];
    double wsum[][];
    BasicStats stats [][];
    double max[][];
    
    public SMConjuctureSelector(SMSelector coreSelector, int n1, int n2) {
        this.coreSelector = coreSelector;
        w = new double[n1][n2];
        for (double[] a : w) Arrays.fill(a, 1);
        sumw = new double[n1][n2];
        wsum = new double[n1][n2];
        max = new double[n1][n2];
        stats = new BasicStats[n1][n2];
        for (int i=0;i<n1;i++){
            for (int j=0; j<n2;j++){
                stats[i][j]=new BasicStats();
            }
        }
    }

    @Override
    public void update(Pair<Integer, Integer> selection, double value) {
        final int si=selection.getLeft();
        final int sj=selection.getRight();
        stats[si][sj].onBackPropagate(value);
        for (int i=0; i<w.length;i++){
            if (i==si) continue;
            w[i][sj]++;
        }
        for (int j=0; j<w[0].length;j++){
            if (j==sj) continue;
            w[si][j]++;
        }
        wsum[si][sj] += w[si][sj]*value;
        sumw[si][sj] += w[si][sj];
        w[si][sj] = 1;
        
        max[si][sj] = Math.max(max[si][sj], Math.abs(stats[si][sj].getEV()-(wsum[si][sj]/sumw[si][sj])));
        //if (stats[si][sj].getNbSamples() % 1e5==0) 
        //    System.out.println(stats[si][sj].getNbSamples() + ": " + Math.abs(stats[si][sj].getEV()-(wsum[si][sj]/sumw[si][sj])));
        coreSelector.update(selection, value);
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        for (int i=0;i<stats.length;i++){
            for (int j=0; j<stats[0].length;j++){
                out.append("Move" + i + j + " ");
                out.append(stats[i][j].getNbSamples() + " ");
                out.append(Math.abs(stats[i][j].getEV()-(wsum[i][j]/sumw[i][j])) + " ");
                out.append(stats[i][j].getEV() + " ");
                out.append((wsum[i][j]/sumw[i][j]) + " ");
                out.append(max[i][j] + "\n");
                max[i][j]=Math.abs(stats[i][j].getEV()-(wsum[i][j]/sumw[i][j]));
            }
        }
        return out.toString();
    }

    @Override
    public Pair<Integer, Integer> select() {
        return coreSelector.select();
    }

    @Override
    public AlgorithmData getBottomData() {
        return coreSelector.getBottomData();
    }

    @Override
    public List<Action> getActions() {
        return ((MeanStrategyProvider)coreSelector).getActions();
    }

    @Override
    public double[] getMp() {
        return ((MeanStrategyProvider)coreSelector).getMp();
    }
}
