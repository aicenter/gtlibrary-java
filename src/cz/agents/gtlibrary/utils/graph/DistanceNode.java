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


package cz.agents.gtlibrary.utils.graph;

public class DistanceNode implements Comparable<DistanceNode>{
	
	private Node node;
	private int distance;
	
	public DistanceNode(Node node, int distance) {
		super();
		this.node = node;
		this.distance = distance;
	}

	@Override
	public int compareTo(DistanceNode o) {
		return this.distance - o.distance;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public Node getNode() {
		return node;
	}
	
	@Override
	public String toString() {
		return "{" + node.toString() + ", " + distance + "}";
	}
}
