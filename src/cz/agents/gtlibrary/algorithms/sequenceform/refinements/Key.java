package cz.agents.gtlibrary.algorithms.sequenceform.refinements;


public class Key {

	private String string;
	private Object object;
	private int hashCode;

	public Key(String string, Object object) {
		this.string = string;
		this.object = object;
		hashCode = computeHashCode();
	}

	public Key(Object object) {
		this.string = "";
		this.object = object;
		hashCode = computeHashCode();
	}

	public int computeHashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((string == null) ? 0 : string.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (string == null) {
			if (other.string != null)
				return false;
		} else if (!string.equals(other.string))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString() {
		return "[" + object + (string.equals("")?"":(", " + string)) + "]";
	}

}
