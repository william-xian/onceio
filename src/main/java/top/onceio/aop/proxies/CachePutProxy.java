package top.onceio.aop.proxies;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import top.onceio.aop.ProxyAction;
import top.onceio.aop.annotation.Aop;
import top.onceio.aop.annotation.CachePut;
import top.onceio.beans.BeansEden;
import top.onceio.cache.Cache;

@Aop(order = "cache-2-put")
public class CachePutProxy extends ProxyAction {

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Object result = proxy.invokeSuper(obj, args);
		CachePut put = method.getAnnotation(CachePut.class);
		if (put != null ) {
			if(put.cacheNames().length > 0) {
				for (String cacheName : put.cacheNames()) {
					Cache cache = BeansEden.get().load(Cache.class, cacheName);
					if (cache != null) {
						String key = CacheKeyResovler.extractKey(method, put.key(), args);
						cache.put(key, result);
					}
				}	
			} else {
				Cache cache = BeansEden.get().load(Cache.class, "");
				if (cache != null) {
					String key = CacheKeyResovler.extractKey(method, put.key(), args);
					cache.put(key, result);
				}
			}
			
		}
		return result;
	}
}
