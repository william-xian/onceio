package top.onceio.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class AopProxy implements MethodInterceptor {

	private static final Map<Method, AopChain> aopChain = new HashMap<>();

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		AopChain ac = aopChain.get(method);
		if (ac != null) {
			return ac.run(obj, method, args, proxy);
		} else {
			return proxy.invokeSuper(obj, args);
		}
	}

}
