/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.algorithms.mcts.selectstrat.*;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class SMRMBackPropFactory implements SMBackPropFactory  {
    double gamma = 0.2;
    Random random = new Random();

    private double minUtility=0;
    private double maxUtility=1;
    
    public SMRMBackPropFactory(double gamma) {
        this.gamma = gamma;
    }
    
    @Override
    public SMSelector createSlector(List<Action> actions1, List<Action> actions2) {
        return new SMRMSelector(this, actions1, actions2);
    }
}