package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import java.util.HashSet;
import java.util.Set;

public class Key {
	
	private Set<Object> objects;

	public Key(Object...objects) {
		this.objects = new HashSet<Object>();
		for (Object object : objects) {
			this.objects.add(object);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
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
		if (objects == null) {
			if (other.objects != null)
				return false;
		} else if (!objects.equals(other.objects))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return objects.toString();
	}
	
	
}
