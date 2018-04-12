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


package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import java.util.List;
import java.util.Map;

public class FrequenceDistribution implements Distribution {
        double remGamma=0;
    
        public FrequenceDistribution() {
        }

        public FrequenceDistribution(double gammaToRemove) {
            this.remGamma = gammaToRemove;
        }

        @Override
        public Map<Action, Double> getDistributionFor(AlgorithmData data) {
                ActionFrequencyProvider afp = (ActionFrequencyProvider) data;
                Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(afp.getActions().size());
                List<Action> actions = afp.getActions();
                double[] freqs = afp.getActionFreq();
                
                double willRemove = 0;
                for (double d : freqs) willRemove += Math.min(d,remGamma);
		
                int i=0;
		for (Action a : actions) distribution.put(a, freqs[i++]*1/(1-willRemove));
		return distribution;
        }

}
