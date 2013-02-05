package cz.agents.gtlibrary.utils;

import java.io.Serializable;

public class Pair<L,R> implements Serializable {

	private static final long serialVersionUID = -7545069745849077863L;
	
	private final L left;
	private final R right;
	
	public Pair(L left, R right) {
		if(left==null){
			throw new IllegalArgumentException("Left value is not effective.");
		}
		if(right==null){
			throw new IllegalArgumentException("Right value is not effective.");
		}
		this.left =left;
		this.right = right;
	}
	
	public L getLeft() {
		return this.left;
	}
	
	public R getRight() {
		return this.right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Pair))
			return false;
		Pair<Object, Object> other = (Pair<Object, Object>) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "<"+left.toString()+","+right.toString()+">";
	}
	
}
