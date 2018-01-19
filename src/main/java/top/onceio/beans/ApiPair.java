package top.onceio.beans;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import top.onceio.exception.Failed;
import top.onceio.mvc.annocations.Attr;
import top.onceio.mvc.annocations.Param;
import top.onceio.util.OReflectUtil;

public class ApiPair {
	
	private static final Gson GSON = new Gson();
	
	private ApiMethod apiMethod;
	private String api;
	private Object bean;
	private Method method;
	private Map<String,Integer> nameVarIndex;
	private Map<String,Class<?>> nameType;
	private List<String> argNames;
	
	
	public ApiMethod getApiMethod() {
		return apiMethod;
	}
	public void setApiMethod(ApiMethod apiMethod) {
		this.apiMethod = apiMethod;
	}
	public String getApi() {
		return api;
	}
	public void setApi(String api) {
		this.api = api;
	}
	public Object getBean() {
		return bean;
	}
	public void setBean(Object bean) {
		this.bean = bean;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public ApiPair(ApiMethod apiMethod,String api, Object bean, Method method) {
		super();
		this.apiMethod = apiMethod;
		this.api = api;
		this.bean = bean;
		this.method = method;
		String[] names = api.split("/");
		nameVarIndex = new HashMap<>(names.length);
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if(!name.isEmpty()) {
				int end = name.length() - 1;
				if (name.charAt(0) == '{' && name.charAt(end) == '}') {
					nameVarIndex.put(name.substring(1, end), i);
				}	
			}
			
		}
		nameType = new HashMap<>(method.getParameterCount());
		argNames = new ArrayList<>(method.getParameterCount());
		for (Parameter param : method.getParameters()) {
			Param paramAnn = param.getAnnotation(Param.class);
			Attr attrAnn = param.getAnnotation(Attr.class);
			top.onceio.mvc.annocations.Cookie cookieAnn = param.getAnnotation(top.onceio.mvc.annocations.Cookie.class);
			String name = null;
			if (paramAnn != null) {
				name = "P-"+paramAnn.value();
			} else if (attrAnn != null) {
				name = "A-"+attrAnn.value();
			} else if (cookieAnn != null) {
				name = "C-"+cookieAnn.value();
			}
			if (name != null) {
				if (nameType.containsKey(name)) {
					Failed.throwError("变量%s，命名冲突", paramAnn.value());
				}
				nameType.put(name, param.getType());
			}
			argNames.add(name);
		}
	}
	/**
	 * 根据方法参数及其注解，从req（Attr,Param,Body,Cooke)中取出数据
	 * @param result
	 * @param req
	 */	
	public Object[] resoveReqParams(Map<String,Object> result, HttpServletRequest req) {
		String[] uris = req.getRequestURI().split("/");
		for (String name : nameVarIndex.keySet()) {
			Integer i = nameVarIndex.get(name);
			String v = uris[i];
			Class<?> type = nameType.get(name);
			Object obj = OReflectUtil.strToBaseType(type, v);
			result.put(name, obj);
		}
		JsonObject json = null;
		try {
			json = GSON.fromJson(req.getReader(), JsonObject.class);
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			e.printStackTrace();
		}
		if(json == null) {
			json = new JsonObject();
		}
		Map<String,String[]> map = req.getParameterMap();
		Set<String> keys = new HashSet<>(map.keySet());
		for(String name:nameType.keySet()) {
			Class<?> type = nameType.get(name);
			if(OReflectUtil.isBaseType(type)) {
				String[] vals = map.get(name);
				if(vals != null) {
					keys.remove(name);
					if(vals.length == 1) {
						result.put(name, OReflectUtil.strToBaseType(type, vals[0]));
					}
				}
			}
		}
		Object[] args = new Object[argNames.size()];
		//TODO
		for(String pName:argNames) {
			String name = pName.substring(2);
			String p = pName.substring(0, 2);
			if(p.equals("P-")) {
				
			}else if(p.equals("A-")) {
				
			}else if(p.equals("C-")) {
				
			}
		}
		return args;
	}
	
	public Object invoke(HttpServletRequest req) {
    	Map<String,Object> result = new HashMap<>();
    	resoveReqParams(result, req);
    	Object[] args = new Object[method.getParameterCount()];
		Object obj = null;
		try {
			obj = method.invoke(bean, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return obj;
	}
	

	private static JsonObject obtainFather(JsonObject obj,String[] path) {
		JsonObject result = obj;
		for(int i = 0; i < path.length -1; i++) {
			String pi = path[i];
			JsonElement je = result.get(pi);
			if(je == null) {
				je = new JsonObject();
				result.add(pi, je);
			}
			result = je.getAsJsonObject();
		}
		return result;
	}
}
