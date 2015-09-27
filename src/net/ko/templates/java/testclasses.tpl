package %packageTest%;

import %kernelPackage%.*;
import net.ko.cache.KCache;
import net.ko.kobject.KListObject;
import net.ko.framework.KoSession;
import net.ko.utils.KScriptTimer;
import net.ko.framework.Ko;

public class %classNameTest% {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Ko.useCache=true;
		KCache.loadAllCache();

		Ko.kstart();

		KScriptTimer.start();
		%testLists%
		KScriptTimer.stop();
		Ko.cacheShutdown();
	}
	

}