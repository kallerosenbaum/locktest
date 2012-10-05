package se.elva.lkpg.locktest;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class Test
{
	public static void main(String[] args) throws Exception
	{
		EmbeddedCacheManager manager = new DefaultCacheManager("infinispan.xml");
		Cache<Object, Object> cache = manager.getCache("default-dist-cache-works");
		String nodeid = System.getProperty("nodeid");

		for (;;) {
			if (cache.get("EXIT") != null) {
				System.out.println("Cache told me to exit");
				System.exit(1);
			}
			if (null == cache.putIfAbsent("lock", nodeid)) {
				// Now we should be alone!
				Object lockOwner = cache.remove("lock");
				if (!nodeid.equals(lockOwner)) {
					System.out.println("Wrong lock owner while unlocking. I'm " + nodeid + " owner was " + lockOwner);
					System.out.println("Telling all nodes to exit");
					cache.put("EXIT", "");
				}
			}

		}
	}

}