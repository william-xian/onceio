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
		Cache cache = BeansEden.get().load(Cache.class);
		if (cache != null) {
			CachePut put = method.getAnnotation(CachePut.class);
			String argkey = CacheKeyResovler.extractKey(method,put.key(), args);
			for (String cacheName : put.cacheNames()) {
				String key = cacheName + argkey;
				cache.put(key, result);
			}
		}
		return result;
	}
}
