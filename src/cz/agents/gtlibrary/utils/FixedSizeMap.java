package cz.agents.gtlibrary.utils;

import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.util.collections.MiniMap;

public class FixedSizeMap<K, V> extends MiniMap<K, V> {

	private static final long serialVersionUID = 2308220975214930664L;

	public FixedSizeMap(int maxEntries) {
		super(maxEntries);
	}

	public FixedSizeMap(Map<? extends K, ? extends V> map, int maxEntries) {
		super(map, maxEntries);
	}

	@Override
	public V put(K arg0, V arg1) {
		return super.put(arg0, arg1);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		super.putAll(arg0);
	}

	@Override
	public V remove(Object arg0) {
		return super.remove(arg0);
	}

	@Override
	public int hashCode() {
		int result = 0;

		for (K key : keySet()) {
			V value = get(key);

			result += (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		if (this.hashCode() != obj.hashCode())
			return false;
		Map<K, V> other = (Map<K, V>) obj;

		if (other.size() != size())
			return false;

		for (K key : keySet()) {
			if (!get(key).equals(other.get(key)))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		Iterator<K> iterator = keySet().iterator();
		builder.append("{");
		while (iterator.hasNext()) {
			K next = iterator.next();
			builder.append("[" + next + " = " + get(next) + "]");
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		builder.append("}");
		return builder.toString();
	}

}
