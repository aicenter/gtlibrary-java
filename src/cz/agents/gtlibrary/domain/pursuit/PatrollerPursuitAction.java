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


package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.graph.Node;

public class PatrollerPursuitAction extends ActionImpl {
	
	private static final long serialVersionUID = 7653401367755825764L;
	
	private Node p1Origin;
	private Node p1Destination;
	private Node p2Origin;
	private Node p2Destination;
	private int hashCode = -1;
	
	public PatrollerPursuitAction(Node p1Origin, Node p1Destination, Node p2Origin, Node p2Destination, InformationSet informationSet) {
		super(informationSet);
		this.p1Origin = p1Origin;
		this.p1Destination = p1Destination;
		this.p2Origin = p2Origin;
		this.p2Destination = p2Destination;
	}
	
	public Node getP1Origin() {
		return p1Origin;
	}
	
	public Node getP1Destination() {
		return p1Destination;
	}
	
	public Node getP2Origin() {
		return p2Origin;
	}
	
	public Node getP2Destination() {
		return p2Destination;
	}
	
	@Override
	public void perform(GameState gameState) {
		((PursuitGameState)gameState).executePatrollerAction(this);
	}

	@Override
	public int hashCode() {
		if(hashCode == -1) {
			final int prime = 31;
			
			hashCode = 1;
			hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
			hashCode = prime * hashCode + ((p1Destination == null) ? 0 : p1Destination.hashCode());
			hashCode = prime * hashCode + ((p1Origin == null) ? 0 : p1Origin.hashCode());
			hashCode = prime * hashCode + ((p2Destination == null) ? 0 : p2Destination.hashCode());
			hashCode = prime * hashCode + ((p2Origin == null) ? 0 : p2Origin.hashCode());
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PatrollerPursuitAction other = (PatrollerPursuitAction) obj;
		if (p1Destination == null) {
			if (other.p1Destination != null)
				return false;
		} else if (!p1Destination.equals(other.p1Destination))
			return false;
		if (p1Origin == null) {
			if (other.p1Origin != null)
				return false;
		} else if (!p1Origin.equals(other.p1Origin))
			return false;
		if (p2Destination == null) {
			if (other.p2Destination != null)
				return false;
		} else if (!p2Destination.equals(other.p2Destination))
			return false;
		if (p2Origin == null) {
			if (other.p2Origin != null)
				return false;
		} else if (!p2Origin.equals(other.p2Origin))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "P: [[" + p1Origin + "->" + p1Destination + "], [" + p2Origin + "->" + p2Destination + "]]";
	}

}
