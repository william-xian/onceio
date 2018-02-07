package top.onceio.aop;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;

public interface ActionChain {
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy, ActionChain next) throws Throwable;
}
