package se.elva.lkpg.locktest;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class CacheCreator {
	
	private static final Logger logger = Logger.getLogger(CacheCreator.class);

	public static final String PESSIMISTIC_CACHE = "pessimistic-cache";
	public static final String OPTIMISTIC_CACHE = "optimistic-cache";

	private static Cache<String, String> cache;
	//com.arjuna.common.util.propertyservice.PropertyManagerFactory a;
	private static Cache<String, String> configureCache() throws IOException {
		EmbeddedCacheManager manager = new DefaultCacheManager("infinispan.xml");
/*		EmbeddedCacheManager manager  = new DefaultCacheManager(GlobalConfigurationBuilder.defaultClusteredBuilder()
		         .transport().addProperty("configurationFile", "jgroups-udp.xml")
		         .build());
		Configuration config = new ConfigurationBuilder()
			.clustering().cacheMode(CacheMode.DIST_SYNC).hash().numOwners(1).groups()
			.transaction().lockingMode(LockingMode.PESSIMISTIC)
			.transaction().transactionMode(TransactionMode.TRANSACTIONAL)
			.build();
		manager.defineConfiguration(PESSIMISTIC_CACHE, config);*/
		return manager.getCache(PESSIMISTIC_CACHE);
	}
	
	public static Cache<String, String> getPessimisticCache() {
		if (cache == null) {
			try {
				cache = configureCache();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return cache;
	}
}
