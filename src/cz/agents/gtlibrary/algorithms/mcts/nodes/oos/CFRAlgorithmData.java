/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.nodes.oos;

import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;

/**
 *
 * @author vilo
 */
public class CFRAlgorithmData extends OOSAlgorithmData{
    public int counter=0;
    public double[] vi;
    public double v;
    
    public CFRAlgorithmData(List<Action> actions) {
        super(actions);
        vi = new double[r.length];
    }
    
    
}
