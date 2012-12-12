package cz.agents.gtlibrary.utils;

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
	public int hashCode() {
		int sum = 0;
		
		for (K key : keySet()) {
			V value = get(key);
			
			sum += (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}
		return sum;
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

}
