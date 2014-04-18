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
        private AlgorithmData algorithmData;

	public MCTSInformationSet(GameState state) {
		super(state);
		allNodes = new HashSet<InnerNode>();
	}

	public void addNode(InnerNode node) {
		allNodes.add(node);
	}

	public Set<InnerNode> getAllNodes() {
		return allNodes;
	}
        
        public AlgorithmData getAlgorithmData() {
            return algorithmData;
        }

        public void setAlgorithmData(AlgorithmData algorithmData) {
            this.algorithmData = algorithmData;
        }

}
