package top.onceio.aop.proxies;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import top.onceio.aop.ProxyAction;
import top.onceio.aop.annotation.Aop;
import top.onceio.aop.annotation.CacheEvict;
import top.onceio.beans.BeansEden;
import top.onceio.cache.Cache;

@Aop(order="cache")
public class CacheEvictProxy extends ProxyAction {

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Object result = null;
		Cache cache = BeansEden.get().load(Cache.class);
		if (cache != null) {
			result = proxy.invokeSuper(obj, args);
			CacheEvict evict = method.getAnnotation(CacheEvict.class);
			/** TODO */
			String key = evict.key();
			cache.evict(key);
		} else {
			result = proxy.invokeSuper(obj, args);
		}
		return result;
	}
}
