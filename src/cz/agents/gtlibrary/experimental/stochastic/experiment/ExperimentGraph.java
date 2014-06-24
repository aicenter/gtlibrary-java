package cz.agents.gtlibrary.experimental.stochastic.experiment;

import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

public class ExperimentGraph extends Graph {

	private static final long serialVersionUID = -2017298729798053283L;
	
	private Node patrollerStart;

	public ExperimentGraph(String graphFile) {
		super(graphFile);
	}

	@Override
	protected void init() {
		super.init();
		patrollerStart = allNodes.get("ID" + ExperimentInfo.patrollerStartId);
	}
	
	public Node getPatrollerStart() {
		return patrollerStart;
	}

}
