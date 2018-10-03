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

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.AbstractActionProvider;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.graph.Node;

public class AttackerAction extends ActionImpl implements AbstractActionProvider {

	private static final long serialVersionUID = -8635820135191985365L;

	final private Node fromNode;
	final private Node toNode;
	final private AttackerMovementType type;
	private int hashCode = -1;

	public enum AttackerMovementType {
		QUICK, SLOW, WAIT
	}

	public AttackerAction(Node fromNode, Node toNode, InformationSet informationSet, AttackerMovementType type) {
		super(informationSet);
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.type = type;
	}

	@Override
	public void perform(GameState gameState) {
		((BPGGameState) gameState).executeAttackerAction(this);
	}

	public Node getFromNode() {
		return fromNode;
	}

	public Node getToNode() {
		return toNode;
	}

	public AttackerMovementType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "AA:" + type + " [" + fromNode + " -> " + toNode + "]";
	}

	@Override
	public int hashCode() {
		if(hashCode != -1)
			return hashCode;
		final int prime = 31;
		hashCode = 1;

		hashCode = prime * hashCode + ((fromNode == null) ? 0 : fromNode.hashCode());
		hashCode = prime * hashCode + ((toNode == null) ? 0 : toNode.hashCode());
		hashCode = prime * hashCode + ((type == null) ? 0 : type.ordinal());
		hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof AttackerAction))
			return false;
		AttackerAction other = (AttackerAction) obj;

		if (fromNode == null) {
			if (other.fromNode != null)
				return false;
		} else if (!fromNode.equals(other.fromNode))
			return false;
		if (toNode == null) {
			if (other.toNode != null)
				return false;
		} else if (!toNode.equals(other.toNode))
			return false;
		if (type != other.type)
			return false;
		if (!super.equals(obj))
			return false;
		return true;
	}

	@Override
	public Object getSituationAbstraction() {
		return BPGGameInfo.dummyAttackerSituation;
	}

	@Override
	public Object getActionAbstraction() {
		if(fromNode.getIntID() == toNode.getIntID())
			return BPGGameInfo.SAME;
		if(type.equals(AttackerMovementType.SLOW)){
			switch (fromNode.getIntID()) {
				case 0:
					switch (toNode.getIntID()) {
						case 1:
							return BPGGameInfo.LEFT_S;
						case 4:
							return BPGGameInfo.UP_S;
						case 5:
							return BPGGameInfo.LL_S;
						case 6:
							return BPGGameInfo.RIGHT_S;
					}
				case 1:
					switch (toNode.getIntID()) {
						case 4:
							return BPGGameInfo.UP_S;
						case 5:
							return BPGGameInfo.LEFT_S;
						case 6:
							return BPGGameInfo.DOWN_S;
					}
				case 4:
					switch (toNode.getIntID()) {
						case 5:
							return BPGGameInfo.DOWN_S;
						case 7:
							return BPGGameInfo.RIGHT_S;
					}
					break;
				case 5:
					switch (toNode.getIntID()) {
						case 4:
							return BPGGameInfo.UP_S;
						case 8:
							return BPGGameInfo.RIGHT_S;
						case 6:
							return BPGGameInfo.DOWN_S;
					}
					break;
				case 6:
					switch (toNode.getIntID()) {
						case 5:
							return BPGGameInfo.UP_S;
						case 9:
							return BPGGameInfo.RIGHT_S;
					}
					break;
				case 7:
					switch (toNode.getIntID()) {
						case 8:
							return BPGGameInfo.DOWN_S;
						case 10:
							return BPGGameInfo.RIGHT_S;
					}
					break;
				case 8:
					switch (toNode.getIntID()) {
						case 7:
							return BPGGameInfo.UP_S;
						case 9:
							return BPGGameInfo.DOWN_S;
						case 11:
							return BPGGameInfo.RIGHT_S;
					}
					break;
				case 9:
					switch (toNode.getIntID()) {
						case 8:
							return BPGGameInfo.UP_S;
						case 12:
							return BPGGameInfo.RIGHT_S;
					}
					break;
				case 10:
					switch (toNode.getIntID()) {
						case 11:
							return BPGGameInfo.DOWN_S;
						case 16:
							return BPGGameInfo.RIGHT_S;
					}
					break;
				case 11:
					switch (toNode.getIntID()) {
						case 10:
							return BPGGameInfo.UP_S;
						case 12:
							return BPGGameInfo.DOWN_S;
						case 16:
							return BPGGameInfo.RIGHT_S;
					}
					break;
				case 12:
					switch (toNode.getIntID()) {
						case 11:
							return BPGGameInfo.UP;
						case 16:
							return BPGGameInfo.RIGHT;
					}
					break;
			}
		}
		else {
			switch (fromNode.getIntID()) {
				case 0:
					switch (toNode.getIntID()) {
						case 1:
							return BPGGameInfo.LEFT;
						case 4:
							return BPGGameInfo.UP;
						case 5:
							return BPGGameInfo.LL;
						case 6:
							return BPGGameInfo.RIGHT;
					}
				case 1:
					switch (toNode.getIntID()) {
						case 4:
							return BPGGameInfo.UP;
						case 5:
							return BPGGameInfo.LEFT;
						case 6:
							return BPGGameInfo.DOWN;
					}
				case 4:
					switch (toNode.getIntID()) {
						case 5:
							return BPGGameInfo.DOWN;
						case 7:
							return BPGGameInfo.RIGHT;
					}
					break;
				case 5:
					switch (toNode.getIntID()) {
						case 4:
							return BPGGameInfo.UP;
						case 8:
							return BPGGameInfo.RIGHT;
						case 6:
							return BPGGameInfo.DOWN;
					}
					break;
				case 6:
					switch (toNode.getIntID()) {
						case 5:
							return BPGGameInfo.UP;
						case 9:
							return BPGGameInfo.RIGHT;
					}
					break;
				case 7:
					switch (toNode.getIntID()) {
						case 8:
							return BPGGameInfo.DOWN;
						case 10:
							return BPGGameInfo.RIGHT;
					}
					break;
				case 8:
					switch (toNode.getIntID()) {
						case 7:
							return BPGGameInfo.UP;
						case 9:
							return BPGGameInfo.DOWN;
						case 11:
							return BPGGameInfo.RIGHT;
					}
					break;
				case 9:
					switch (toNode.getIntID()) {
						case 8:
							return BPGGameInfo.UP;
						case 12:
							return BPGGameInfo.RIGHT;
					}
					break;
				case 10:
					switch (toNode.getIntID()) {
						case 11:
							return BPGGameInfo.DOWN;
						case 16:
							return BPGGameInfo.RIGHT;
					}
					break;
				case 11:
					switch (toNode.getIntID()) {
						case 10:
							return BPGGameInfo.UP;
						case 12:
							return BPGGameInfo.DOWN;
						case 16:
							return BPGGameInfo.RIGHT;
					}
					break;
				case 12:
					switch (toNode.getIntID()) {
						case 11:
							return BPGGameInfo.UP;
						case 16:
							return BPGGameInfo.RIGHT;
					}
					break;
			}
		}
		System.out.println("Action not found");
		return null;
	}

	@Override
	public double getMaximumActionUtility() {
		return 0;
	}

	@Override
	public double[] getAllPossibleOutcomes() {
		return new double[0];
	}

}
