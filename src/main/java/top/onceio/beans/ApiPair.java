package top.onceio.beans;

import java.lang.reflect.Method;
import java.util.Map;

public class ApiPair {
	private ApiMethod apiMethod;
	private String api;
	private Object bean;
	private Method method;
	
	public ApiPair(ApiMethod apiMethod,String api, Object bean, Method method) {
		super();
		this.apiMethod = apiMethod;
		this.api = api;
		this.bean = bean;
		this.method = method;
	}

	public Map<String,Object> resoveUri(String uri) {
		String[] uris = uri.split("/");
		return null;
	}
}
