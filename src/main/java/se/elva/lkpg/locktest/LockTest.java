package se.elva.lkpg.locktest;

import javax.transaction.TransactionManager;

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
	private TransactionManager tm;
		
	public void initializeCache() {
		cache = CacheCreator.getPessimisticCache();
		tm = cache.getAdvancedCache().getTransactionManager();
	}
	
	private boolean lock() throws Exception {
		d("Locking...");
		String otherLock = cache.get(LOCK_KEY);
		boolean success = false;
		try {
			tm.begin();
			if (otherLock == null) {
				if (cache.putIfAbsent(LOCK_KEY, lockString) == null) {
					d("Locked absent lock!");
					success = true;
				}
			} else if (cache.replace(LOCK_KEY, NO_LOCK, lockString)) {
				d("Locked!");
				success = true;
			} else {
				d("Failed to Lock! Other lock was " + otherLock + " and is now " + cache.get(LOCK_KEY));
				success = false;
			}
			tm.commit();
		} catch (Exception e) {
			tm.rollback();
		}
		return success;
	}
	
	private boolean unlock() {
		d("Unlocking...");
		String otherLock = cache.get(LOCK_KEY);
		if (!otherLock.equals(lockString)) {
			e("Other lock was " + otherLock + " and my lockString is " + lockString);
			System.exit(1);
			return false;
		}
		try {
			if (cache.replace(LOCK_KEY, lockString, NO_LOCK)) {
				d("Unlocked!");
				return true;
			} else {
				e("Other lock was " + otherLock + " and is now " + cache.get(LOCK_KEY));
				e("Cannot release lock, the lock is not mine. My lockString is " + lockString + "");
				System.exit(1);
				return false;
			}
		} catch (Exception e) {
			e("Exception while unlocking, will force unlock!", e);
			try {
				cache.remove(LOCK_KEY);
			} catch (Exception e2) {
				e("Force unlock failed", e2);				
			}
		}
		return false;
	}
	
	public void runTestDefaultCache() {

		try {
			if (!lock()) {
				return;
			}
		} catch (Exception e) {
			e("Exception while locking", e);
			return;
		}
		try {
			String oldValue = cache.get(VALUE_KEY);
			if (oldValue == null) {
				oldValue = "-1";
				i("Initializing value to 0");
			}
			int oldValueInt = Integer.parseInt(oldValue);
			i("Putting " + (oldValueInt + 1));
			String replacedValue = cache.put(VALUE_KEY, Integer.toString(oldValueInt+1));
			
			int checkValue = Integer.parseInt(replacedValue);
			
			if (oldValueInt != checkValue) {				
				e("Concurrent modification to \"" + VALUE_KEY +"\". Tried to set " + (oldValueInt + 1) + " while someone else set " + checkValue);
				System.exit(1);
			}
		} catch (Exception e) {
			e("Exception caught", e);
		} finally {
			unlock();
		}
	}
	
	private static void e(String message) {
		e(message, null);
	}
	
	private static void e(String message, Throwable t) {
		logger.error(NODEID + " " + message, t);
	}

	private static void d(String message) {
		logger.debug(NODEID + " " + message);
	}

	private static void i(String message) {
		logger.info(NODEID + " " + message);
	}

}
