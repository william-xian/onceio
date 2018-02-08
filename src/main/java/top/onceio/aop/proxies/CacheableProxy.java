package top.onceio.aop.proxies;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import top.onceio.aop.ProxyAction;
import top.onceio.aop.annotation.Aop;
import top.onceio.aop.annotation.Cacheable;
import top.onceio.beans.BeansEden;
import top.onceio.cache.Cache;

@Aop(order="cache-3-cacheable")
public class CacheableProxy extends ProxyAction {

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Object result = null;
		Cache cache = BeansEden.get().load(Cache.class);
		if (cache != null) {
			Cacheable cacheable = method.getAnnotation(Cacheable.class);
			String argkey = CacheKeyResovler.extractKey(cacheable.key(), args);
			String key = cacheable.cacheName()+argkey;
			result = cache.get(key, method.getReturnType());
			if(result == null) {
				result = proxy.invokeSuper(obj, args);
				cache.put(key, result);
			}
		} else {
			result = proxy.invokeSuper(obj, args);
		}
		return result;
	}
	

}