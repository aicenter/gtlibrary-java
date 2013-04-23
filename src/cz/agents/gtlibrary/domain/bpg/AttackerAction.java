package cz.agents.gtlibrary.domain.bpg;

import cz.agents.gtlibrary.domain.bpg.data.Node;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class AttackerAction extends ActionImpl {

	private static final long serialVersionUID = -8635820135191985365L;
	
	final private Node fromNode;
	final private Node toNode;
	final private AttackerMovementType type;
	private int hashCode = -1;

	public enum AttackerMovementType {
		QUICK, SLOW, WAIT
	};

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
		hashCode = prime * hashCode + ((type == null) ? 0 : type.hashCode());
		hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
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
		return true;
	}

}
