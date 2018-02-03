package top.onceio.aop;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import top.onceio.aop.annotation.Transactional;
import top.onceio.beans.BeansEden;
import top.onceio.cache.Cache;
import top.onceio.cache.annotation.CacheEvict;
import top.onceio.cache.annotation.CachePut;
import top.onceio.cache.annotation.Cacheable;
import top.onceio.db.jdbc.JdbcHelper;

public class TransactionProxy implements MethodInterceptor {
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Object result = null;
		Transactional trans = method.getAnnotation(Transactional.class);
		if (trans != null) {
			JdbcHelper jdbcHelper = BeansEden.get().load(JdbcHelper.class);
			boolean created = jdbcHelper.beginTransaction(trans.isolation(),trans.readOnly());
			try {
				result = proxy.invokeSuper(obj, args);
				if(created) {
					jdbcHelper.commit();
				}
			} catch (Exception e) {
				if(created) {
					jdbcHelper.rollback();	
				}
				throw e;
			}
		} else {
			result = proxy.invokeSuper(obj, args);
		}
		return result;
	}
	//TODO
	public Object get(Object obj,Method method,Object[] args) {
		Object result = null;
		Cache cache = BeansEden.get().load(Cache.class);
		if(cache != null) {
			String key = null;
			Object ret = null;
			CacheEvict evict = method.getAnnotation(CacheEvict.class);
			CachePut put = method.getAnnotation(CachePut.class);
			Cacheable cacheable = method.getAnnotation(Cacheable.class);
			if(cacheable != null) {
				result = cache.get(key, method.getReturnType());
			} else if(evict != null) {
				cache.evict(key);
			}else if(put != null) {
				cache.put(key, ret);
			}
		}
		return result;
	}

}
