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

package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vilo
 */
public class ConjuctureSelector implements Selector, MeanStrategyProvider{
    Selector coreSelector;
    double w[];
    double sumw[];
    double wsum[];
    BasicStats stats [];
    double max[];
    
    public ConjuctureSelector(Selector coreSelector, int N) {
        this.coreSelector = coreSelector;
        w = new double[N];
        Arrays.fill(w, 1);
        sumw = new double[N];
        wsum = new double[N];
        max = new double[N];
        stats = new BasicStats[N];
        for (int i=0;i<N;i++){
            stats[i]=new BasicStats();
        }
    }

    @Override
    public void update(int si, double value) {
        stats[si].onBackPropagate(value);
        for (int i=0; i<w.length;i++){
            if (i==si) continue;
            w[i]++;
        }
        wsum[si] += w[si]*value;
        sumw[si] += w[si];
        w[si] = 1;
        
        max[si] = Math.max(max[si], Math.abs(stats[si].getEV()-(wsum[si]/sumw[si])));
        coreSelector.update(si, value);
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        for (int i=0;i<stats.length;i++){
            out.append("Move" + i + "0 ");
            out.append(stats[i].getNbSamples() + " ");
            out.append(Math.abs(stats[i].getEV()-(wsum[i]/sumw[i])) + " ");
            out.append(stats[i].getEV() + " ");
            out.append((wsum[i]/sumw[i]) + " ");
            out.append(max[i] + "\n");
            max[i]=Math.abs(stats[i].getEV()-(wsum[i]/sumw[i]));
        }
        return out.toString();
    }

    @Override
    public int select() {
        return coreSelector.select();
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
