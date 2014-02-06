package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.SelectionStrategy;
import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MCTSInformationSet extends InformationSetImpl {

	private Set<InnerNode> allNodes;
	public SelectionStrategy selectionStrategy;
        private Map<Action, BasicStats> actionStats;
	private BasicStats informationSetStats;
//        transient static private PrintStream log;
//        
//        static {
//            try {
//                    log = new PrintStream("selections.txt");
//                } catch (FileNotFoundException ex) {}
//        }

	public MCTSInformationSet(GameState state) {
		super(state);
		allNodes = new HashSet<InnerNode>();
		informationSetStats = new BasicStats();
		actionStats = new LinkedHashMap<Action, BasicStats>();      
	}

	public void addNode(InnerNode node) {
		allNodes.add(node);
	}

	public Set<InnerNode> getAllNodes() {
		return allNodes;
	}
        
        public double backPropagate(InnerNode node, Action action, double value){
//            if (this.playerHistory.size()==0) log.println(this.getPlayer().toString() + ";" 
//                    //+ this.playerHistory.size() + ";" 
//                    + (informationSetStats.getNbSamples()+1) + ";"
//                    +  action.toString() + ";" + value);
            informationSetStats.onBackPropagate(value);
            if (!actionStats.containsKey(action)){
                System.out.println("WARNING: Rebuilding HashMap!!!");
                actionStats = new LinkedHashMap(actionStats);
            }
            actionStats.get(action).onBackPropagate(value);
            return selectionStrategy.onBackPropagate(node, action, value);
        }

        
        
        public static boolean oos = false;
        
	public void updateActionStatsFor(Action action, double[] values) {
            if (oos) {
                //make sure the sum is propagated up and the EV is still what it is supposed to be
//                double sum = 0;
//                for (Map.Entry<Action, BasicStats> en : actionStats.entrySet()) {
//                    OOSActionBPStrategy stra = (OOSActionBPStrategy) en.getValue();
//                    if (en.getClass().equals(action)){
//                        sum += stra.p * values[getPlayer().getId()];
//                    } else {
//                        sum += stra.p * stra.getEV();
//                    }
//                }
//                ((OOSActionBPStrategy)actionStats.get(action)).r += values[getPlayer().getId()] - sum;
                
            } else {
		actionStats.get(action).onBackPropagate(values[player.getId()]);
            }
	}

	public void initStats(List<Action> actions, BackPropFactory backPropagationStrategyFactory) {
		if (actionStats.isEmpty()) {
			for (Action action : actions) {
				actionStats.put(action, new BasicStats());
			}
                        if (this.getPlayer().getId() < 2) selectionStrategy = backPropagationStrategyFactory.createForIS(this);
		}
	}

	public Map<Action, BasicStats> getActionStats() {
		return actionStats;
	}

    public BasicStats getInformationSetStats() {
        return informationSetStats;
    }


}
