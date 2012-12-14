package cz.agents.gtlibrary.utils;

public class Triplet<T, U, V> {
	
	private T first;
	private U second;
	private V third;
	private int hashCode;
	
	public Triplet(T first, U second, V third) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
		this.hashCode = computeHashCode();
	}
	
	public T getFirst() {
		return first;
	}
	
	public U getSecond() {
		return second;
	}
	
	public V getThird() {
		return third;
	}

	public int computeHashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
		return result;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Triplet<T, U, V> other = (Triplet<T, U, V>) obj;
		
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}
	
}
