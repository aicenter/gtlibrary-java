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

public class EvaderPursuitAction extends ActionImpl {

	private static final long serialVersionUID = 4464689353944463622L;
	
	private Node origin;
	private Node destination;
	private int hashCode = -1;

	public EvaderPursuitAction(Node origin, Node destination, InformationSet informationSet) {
		super(informationSet);
		this.origin = origin;
		this.destination = destination;
	}

	public Node getDestination() {
		return destination;
	}

	public Node getOrigin() {
		return origin;
	}

	@Override
	public void perform(GameState gameState) {
		((PursuitGameState) gameState).executeEvaderAction(this);
	}


	@Override
	public int hashCode() {
		if (hashCode == -1) {
			final int prime = 31;
			int result = 1;
			
			result = prime * result + ((destination == null) ? 0 : destination.hashCode());
			result = prime * result + ((informationSet == null) ? 0 : informationSet.hashCode());
			result = prime * result + ((origin == null) ? 0 : origin.hashCode());
			hashCode = result;
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
		EvaderPursuitAction other = (EvaderPursuitAction) obj;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "E: "+ informationSet.getAllStates().iterator().next() + " [" + origin + "->" + destination + "]";
	}

}
