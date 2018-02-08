package top.onceio.aop;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;

public class AopChain {
	private AopAction header;
	private AopAction current;

	public AopChain() {
	}

	public AopChain append(AopAction action) {
		if (this.current == null) {
			this.header = action;
			this.current = action;
		} else {
			this.current.next = action;
			this.current = this.current.next;
		}
		return this;
	}
	
	public Object run(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		if (header != null) {
			return header.intercept(obj, method, args, proxy);
		} else {
			return proxy.invokeSuper(obj, args);
		}
	}
}
