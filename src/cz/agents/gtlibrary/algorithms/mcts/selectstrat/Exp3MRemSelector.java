/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;

/**
 *
 * @author vilo
 */
public class Exp3MRemSelector extends Exp3SelectionStrategy {

    public Exp3MRemSelector(Exp3BackPropFactory fact, MCTSInformationSet infSet) {
        super(fact, infSet);
    }
    

    @Override
    public double onBackPropagate(InnerNode node, Action action, double value) {
        final int K = actions.size();
        final double gamma = fact.gamma;
        int i = actions.indexOf(action);
        r[i] += fact.normalizeValue(value) / p[i];
        
        int j=0, sum=0;
        try {
            double[] n = new double[actions.size()];
            for (Action a : actions){
                n[j] = infSet.getActionStats().get(a).getNbSamples();
                sum += n[j];
                j++;
            }
            int toRem = (int)(gamma * sum / K);
            sum = 0;
            for(j=0; j<n.length;j++){
                n[j] = Math.max(0, n[j]-toRem);
                sum += n[j];
            }

            double val = 0;
            j=0;
            for (Action a : actions){
                //!!!! getEV je stale zatazene biasom!!!
                
                val += ((double)n[j]/sum) * node.getChildOrNull(a).getEV()[infSet.getPlayer().getId()];
                //val += p[j] * node.getChildOrNull(a).getEV()[infSet.getPlayer().getId()];
                j++;
            }
            return val;
        } catch(NullPointerException ex){
            //intentionally empty
        }
        return value;
    }
}
