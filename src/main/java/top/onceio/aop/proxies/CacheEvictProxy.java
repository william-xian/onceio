package top.onceio.aop.proxies;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import top.onceio.aop.ProxyAction;
import top.onceio.aop.annotation.Aop;
import top.onceio.aop.annotation.CacheEvict;
import top.onceio.beans.BeansEden;
import top.onceio.cache.Cache;

@Aop(order = "cache-1-evict")
public class CacheEvictProxy extends ProxyAction {

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Object result = proxy.invokeSuper(obj, args);
		CacheEvict evict = method.getAnnotation(CacheEvict.class);
		if (evict != null) {
			if (evict.cacheNames().length > 0) {
				for (String cacheName : evict.cacheNames()) {
					Cache cache = BeansEden.get().load(Cache.class, cacheName);
					if (cache != null) {
						String argkey = CacheKeyResovler.extractKey(method, evict.key(), args);
						cache.evict(argkey);
					}
				}
			}else {
				Cache cache = BeansEden.get().load(Cache.class, "");
				if (cache != null) {
					String argkey = CacheKeyResovler.extractKey(method, evict.key(), args);
					cache.evict(argkey);
				}
			}
		}
		return result;
	}
}
