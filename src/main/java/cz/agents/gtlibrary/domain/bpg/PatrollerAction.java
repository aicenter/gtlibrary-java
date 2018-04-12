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


package cz.agents.gtlibrary.domain.bpg;

import java.util.Set;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.graph.Node;

public class PatrollerAction extends ActionImpl {

	private static final long serialVersionUID = 5923340650378483826L;
	
	private Set<Node> flaggedNodes;
	private Node fromNodeP1;
	private Node fromNodeP2;
	private Node toNodeP1;
	private Node toNodeP2;
	private int hashCode = -1;

	public PatrollerAction(Node fromNodeP1, Node fromNodeP2, Node toNodeP1, Node toNodeP2, InformationSet informationSet, Set<Node> flaggedNodes) {
		super(informationSet);
		this.fromNodeP1 = fromNodeP1;
		this.fromNodeP2 = fromNodeP2;
		this.toNodeP1 = toNodeP1;
		this.toNodeP2 = toNodeP2;
		this.flaggedNodes = flaggedNodes;
	}

	public Node getFromNodeForP1() {
		return fromNodeP1;
	}

	public Node getFromNodeForP2() {
		return fromNodeP2;
	}

	public Node getToNodeForP1() {
		return toNodeP1;
	}

	public Node getToNodeForP2() {
		return toNodeP2;
	}
	
	public Set<Node> getFlaggedNodes(){
		return flaggedNodes;
	}

	@Override
	public void perform(GameState gameState) {
		((BPGGameState) gameState).executePatrollerAction(this);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("PA:");
		sb.append("[" + buildStringRepFor(fromNodeP1) + " -> " + buildStringRepFor(toNodeP1) + "]");
		sb.append("[" + buildStringRepFor(fromNodeP2) + " -> " + buildStringRepFor(toNodeP2) + "]");
		return sb.toString();
	}

	private String buildStringRepFor(Node node) {
		return node.toString() + (flaggedNodes.contains(node) ? "'" : "");
	}

	@Override
	public int hashCode() {
		if(hashCode != -1)
			return hashCode;
		final int prime = 31;
		hashCode = 1;

		hashCode = prime * hashCode + ((fromNodeP1 == null) ? 0 : fromNodeP1.hashCode());
		hashCode = prime * hashCode + ((fromNodeP2 == null) ? 0 : fromNodeP2.hashCode());
		hashCode = prime * hashCode + ((toNodeP1 == null) ? 0 : toNodeP1.hashCode());
		hashCode = prime * hashCode + ((toNodeP2 == null) ? 0 : toNodeP2.hashCode());
		hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());

		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof PatrollerAction))
		    return false;
	    PatrollerAction other = (PatrollerAction) obj;

		if (!fromNodeP1.equals(other.fromNodeP1))
			return false;
		if (!fromNodeP2.equals(other.fromNodeP2))
			return false;
		if (!toNodeP1.equals(other.toNodeP1))
			return false;
		if (!toNodeP2.equals(other.toNodeP2))
			return false;
		if (!super.equals(obj))
			return false;
		return true;
	}

}
