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

public interface BackPropagationStrategy {

	void onBackPropagate(double value);
	
	double getEV();
	
//	double getStdDev();
//	
//	double getVariance();
	
	int getNbSamples();

//	double getEVStdDev();
//	
//	double getEVVar();

	int getNbSamplesInMean();
	
	public static interface Factory{
		BackPropagationStrategy createForNode(Node node, Player player);
        BackPropagationStrategy createForAction(Node node, Action action, Player player);
	}


	
}
