package index.alchemy.util.cache;

import java.util.HashMap;
import java.util.Map;

import index.project.version.annotation.Omega;

@Omega
public class StdCache<K, V> extends Cache<K, V> {
	
	private final Map<K, V> map = new HashMap<K, V>();

	@Override
	public Map<K, V> getCacheMap() { return map; }

}
