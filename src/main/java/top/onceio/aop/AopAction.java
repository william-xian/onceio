package top.onceio.aop;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;

public abstract class AopAction {
	AopAction next = null;
	public AopAction next() {
		return next;
	}
	public abstract Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;
}
