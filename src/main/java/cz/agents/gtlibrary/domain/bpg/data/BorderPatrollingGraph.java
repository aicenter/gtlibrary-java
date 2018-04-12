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


package cz.agents.gtlibrary.domain.bpg.data;

import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.Map;

public class BorderPatrollingGraph extends Graph {

	protected static final long serialVersionUID = -5572263962229886222L;
	
	protected Node origin;
	protected Node destination;
	protected Node p1Start;
	protected Node p2Start;

    public BorderPatrollingGraph(String graphFile) {
        super(graphFile);
    }

    protected void init() {
		super.init();
		int n = dl.getNodesInOriginalGraph();

		origin = allNodes.get("ID0");
		destination = allNodes.get("ID" + (n - 3));
		p1Start = allNodes.get("ID" + (n - 2));
		p2Start = allNodes.get("ID" + (n - 1));

	}

	public Node getOrigin() {
		return origin;
	}

	public Node getDestination() {
		return destination;
	}

	public Node getP1Start() {
		return p1Start;
	}

	public Node getP2Start() {
		return p2Start;
	}

    public Node getNodeByID(int ID) {
        return allNodes.get("ID" + ID);
    }
}
