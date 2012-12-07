package gametree.interfaces;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

public interface History {
	public Collection<Sequence> values();
	public Set<Player> keySet();
	public Set<Entry<Player, Sequence>> entrySet();
	public Sequence getSequenceOf(Player player);
	public History copy();
	public void addActionOf(Action action, Player player);
}
