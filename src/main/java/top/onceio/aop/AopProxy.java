package top.onceio.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import top.onceio.aop.annotation.Transactional;
import top.onceio.beans.BeansEden;
import top.onceio.db.jdbc.JdbcHelper;

public class AopProxy implements MethodInterceptor {
	
	private Map<Method,ActionChain> aopChain = new HashMap<>();
	
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Object result = null;
		Transactional trans = method.getAnnotation(Transactional.class);
		if (trans != null) {
			JdbcHelper jdbcHelper = BeansEden.get().load(JdbcHelper.class);
			boolean created = jdbcHelper.beginTransaction(trans.isolation(), trans.readOnly());
			try {
				result = proxy.invokeSuper(obj, args);
				if (created) {
					jdbcHelper.commit();
				}
			} catch (Exception e) {
				if (created) {
					jdbcHelper.rollback();
				}
				throw e;
			}
		} else {
			result = proxy.invokeSuper(obj, args);
		}
		ActionChain ac = aopChain.get(method);
		//return ac.intercept(obj, method, args, proxy, ac);
		return result;
	}

}
