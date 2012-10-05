package se.elva.lkpg.locktest;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class Test
{
	public static void main(String[] args) throws Exception
	{
		EmbeddedCacheManager manager = new DefaultCacheManager("infinispan.xml");
		Cache<String, String> cache = manager.getCache("transactional-pessimistic-dist-sync-cache-fails");
		String nodeid = System.getProperty("nodeid");
		
		if (!nodeid.equals("1")) {
			for (;;) {
				System.out.println("Doing nothing!");
				Thread.sleep(30000);
			}
		}
		
		for (;;) {
			long now = System.nanoTime();
			if (cache.get("EXIT") != null) {
				System.out.println("Cache told me to exit");
				System.exit(1);
			}
			if (null == cache.putIfAbsent("lock", nodeid + " " + now)) {
				// Now we should be alone!
				String lockOwner = cache.remove("lock");
				if (lockOwner == null || !nodeid.equals(lockOwner.substring(0, 1))) {
					System.out.println("Wrong lock owner while unlocking. I'm " + nodeid + " owner was " + lockOwner);
					System.out.println("Telling all nodes to exit");
					cache.put("EXIT", "");
				}
			}

		}
	}

}