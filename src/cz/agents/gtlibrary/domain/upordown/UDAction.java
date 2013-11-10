package cz.agents.gtlibrary.domain.upordown;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.InformationSet;

public abstract class UDAction extends ActionImpl {

	private static final long serialVersionUID = 5987116212811495600L;

	private String type;
	
	public UDAction(InformationSet informationSet, String type) {
		super(informationSet);
		this.type = type;
	}
	
	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UDAction other = (UDAction) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return type;
	}

}
