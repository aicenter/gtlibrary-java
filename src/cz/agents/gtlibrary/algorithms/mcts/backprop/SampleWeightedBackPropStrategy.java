/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package cz.agents.gtlibrary.algorithms.mcts.backprop;

import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.RunningStats;

public class SampleWeightedBackPropStrategy implements BackPropagationStrategy{

	private final RunningStats stats = new RunningStats();

	public SampleWeightedBackPropStrategy() {
	}

	@Override
	public double getEV() {
		return stats.getMean();
	}

	@Override
	public int getNbSamples() {
		return stats.getNbSamples();
	}

//	@Override
//	public double getStdDev() {
//		return stats.getStdDev();
//	}
//	
//	@Override
//	public double getEVStdDev() {
//		return stats.getEVStdDev();
//	}
//	
//	@Override
//	public double getEVVar() {
//		return stats.getEVVar();
//	}
//	
//	@Override
//	public double getVariance() {
//		return stats.getVariance();
//	}
	
	@Override
	public int getNbSamplesInMean() {
		return stats.getNbSamples();
	}

	@Override
	public void onBackPropagate(double value) {
		stats.add(value);
	}

    @Override
    public String toString() {
        return "S_BP:" + getNbSamples()  +":"+ getEV();
    }



	public static class Factory implements BackPropagationStrategy.Factory{

		@Override
		public SampleWeightedBackPropStrategy createForNode(Node node, Player player) {
			return new SampleWeightedBackPropStrategy();
		}

        @Override
        public BackPropagationStrategy createForAction(Node node, Action a, Player player) {
            return new SampleWeightedBackPropStrategy();
        }
        
        
	}

}
