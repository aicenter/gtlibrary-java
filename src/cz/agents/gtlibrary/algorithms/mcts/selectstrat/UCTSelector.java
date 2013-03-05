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
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropagationStrategy;
import cz.agents.gtlibrary.utils.RunningStats;

public class UCTSelector extends MaxFunctionSelector {

	private final double C;

	public UCTSelector(double C) {
		this.C = C;
	}

	protected double evaluate(RunningStats nodeStats, BackPropagationStrategy actionStats) {
		int nbSamples = actionStats.getNbSamples();
		
		if (nbSamples == 0)
			return Double.MAX_VALUE;
		return actionStats.getEV() + C * Math.sqrt(Math.log(nodeStats.getNbSamples()) / nbSamples);
	}

}
