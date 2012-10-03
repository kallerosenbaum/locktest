package se.elva.lkpg.locktest;

import org.apache.log4j.Logger;
import org.infinispan.Cache;

public class LockTest {;
	private static final Logger logger = Logger.getLogger(LockTest.class);

	private static final String NO_LOCK = "none";
	private static final String LOCK_KEY = "lock";
	private static final String VALUE_KEY = "value";
	private static final String NODEID = System.getProperty("nodeid");
	private static final String lockString = NODEID;
	private Cache<String, String> cache;
	private javax.transaction.TransactionManager tm; 
		
	public void initializeCache() {
		cache = CacheCreator.getPessimisticCache();
		tm = cache.getAdvancedCache().getTransactionManager();
		
		i("TransactionManager: " + tm.getClass());
		
		i(lockString + " Initializing");
		
		String oldValue = cache.putIfAbsent(VALUE_KEY, "0");
		d("InitializeCache: oldValue=" + oldValue);
		oldValue = cache.putIfAbsent(LOCK_KEY, NO_LOCK);
		d("InitializeCache: oldLock=" + oldValue);
		
	}
	
	private boolean lock() {
		d("Locking...");
		String otherLock = cache.get(LOCK_KEY);
		try {
			tm.begin();
			if (cache.replace(LOCK_KEY, NO_LOCK, lockString)) {
				d("Locked!");
				tm.commit();
				return true;
			} else {
				d("Failed to Lock!");
				tm.commit();
				return false;
			}
		} catch (Exception e) {
		    if (tm != null) {
		        try {
		            tm.rollback();
		        } catch (Exception e1) {}
		    } 
		    e.printStackTrace();
		}
		d("Other lock was " + otherLock + " and is now " + cache.get(LOCK_KEY));
		d("Lock already locked by someone else. I'm " + lockString);
		return false;
	}
	
	private boolean unlock() {
		d("Unlocking...");
		String otherLock = cache.get(LOCK_KEY);
		try {
			tm.begin();
			if (cache.replace(LOCK_KEY, lockString, NO_LOCK)) {
				d("Unlocked!");
				tm.commit();
				return true;
			} else {
				d("Unlock failed!");
				tm.commit();
				return false;
			}
		} catch (Exception e) {
		    if (tm != null) {
		        try {
		            tm.rollback();
		        } catch (Exception e1) {}
		    } 
		    e.printStackTrace();
		}
		e("Other lock was " + otherLock + " and is now " + cache.get(LOCK_KEY));
		e("Cannot release lock, the lock is not mine. My lockString is " + lockString + "");
		System.exit(1);
		return false;
	}
	
	public void runTestDefaultCache() {
		if (!lock()) {
			return;
		}

		try {
			String oldValue = cache.get(VALUE_KEY);
			int oldValueInt = Integer.parseInt(oldValue);
			i("Putting " + (oldValueInt + 1));
			
			int checkValue = Integer.parseInt(cache.put(VALUE_KEY, Integer.toString(oldValueInt+1)));
			if (oldValueInt != checkValue) {				
				e("Concurrent modification to \"" + VALUE_KEY +"\". Tried to set " + (oldValueInt + 1) + " while someone else set " + checkValue);
				System.exit(1);
			}
		} finally {
			unlock();
		}
	}
	
	private static void e(String message) {
		logger.error(NODEID + " " + message);
	}
	
	private static void d(String message) {
		logger.debug(NODEID + " " + message);
	}

	private static void i(String message) {
		logger.info(NODEID + " " + message);
	}

}
