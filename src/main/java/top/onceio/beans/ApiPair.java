package top.onceio.beans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import top.onceio.mvc.annocations.Attr;
import top.onceio.mvc.annocations.Param;

public class ApiPair {
	
	private static final Gson GSON = new Gson();
	
	private ApiMethod apiMethod;
	private String api;
	private Object bean;
	private Method method;
	private Map<String,Integer> nameVarIndex;
	private Map<String,Class<?>> nameType;
	private Map<Integer,String> paramNameArgIndex;
	private Map<Integer,String> attrNameArgIndex;
	private List<Integer> reqIndex;
	private List<Integer> respIndex;
	
	
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
		if(method.getParameterCount() > 0) {
			nameType = new HashMap<>(method.getParameterCount());
			paramNameArgIndex = new HashMap<>(method.getParameterCount());
			attrNameArgIndex = new HashMap<>(method.getParameterCount());
			reqIndex = new ArrayList<>(method.getParameterCount());
			respIndex = new ArrayList<>(method.getParameterCount());
			Parameter[] params = method.getParameters();
			for (int i = 0; i < params.length; i++) {
				Parameter param = method.getParameters()[i];
				Param paramAnn = param.getAnnotation(Param.class);
				Attr attrAnn = param.getAnnotation(Attr.class);
				if (paramAnn != null) {
					paramNameArgIndex.put(i,paramAnn.value());
					nameType.put(paramAnn.value(), param.getType());
				} else if (attrAnn != null) {
					attrNameArgIndex.put(i,attrAnn.value());
				}else if(HttpServletRequest.class.isAssignableFrom(param.getType())) {
					reqIndex.add(i);
				}else if(HttpServletResponse.class.isAssignableFrom(param.getType())) {
					respIndex.add(i);
				}
			}
		}
	}
	/**
	 * 根据方法参数及其注解，从req（Attr,Param,Body,Cookie)中取出数据
	 * @param result
	 * @param req
	 */	
	public Object[] resoveReqParams(HttpServletRequest req, HttpServletResponse resp) {
		JsonObject json = null;
		try {
			json = GSON.fromJson(req.getReader(), JsonObject.class);
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			e.printStackTrace();
		}
		if(json == null) {
			json = new JsonObject();
		}
		String uri = req.getRequestURI();
		try {
			uri = URLDecoder.decode(req.getRequestURI(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String[] uris = uri.split("/");
		for (String name : nameVarIndex.keySet()) {
			Integer i = nameVarIndex.get(name);
			String v = uris[i];
			json.addProperty(name, v);
		}
		Map<String,String[]> map = req.getParameterMap();
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			String[] vals = entry.getValue();
			String name = entry.getKey();
			String[] ps = name.split("\\.");
			String pname = name;
			JsonObject jobj = json;
			if(ps.length > 0) {
				pname = ps[ps.length-1];
				jobj = getOrCreateFatherByPath(json,ps);
			}
			if(vals != null && vals.length == 1) {
				jobj.addProperty(pname, vals[0]);
			} else {
				JsonArray ja = new JsonArray();
				for(String v:vals) {
					ja.add(v);
				}
				jobj.add(pname, ja);
			}
		}
		Object[] args = new Object[method.getParameterCount()];
		Class<?>[] types = method.getParameterTypes();
		if(paramNameArgIndex != null && !paramNameArgIndex.isEmpty()) {
			for (Map.Entry<Integer,String> entry : paramNameArgIndex.entrySet()) { 
				Class<?> type = types[entry.getKey()];
				if (entry.getValue().equals("")) {
					args[entry.getKey()] = GSON.fromJson(json, type);	
				} else {
					args[entry.getKey()] = GSON.fromJson(json.get(entry.getValue()), type);
				}
			}
		}
		if(paramNameArgIndex != null && !paramNameArgIndex.isEmpty()) {
			for (Map.Entry<Integer,String> entry : paramNameArgIndex.entrySet()) { 
				Class<?> type = types[entry.getKey()];
				if (entry.getValue().equals("")) {
					args[entry.getKey()] = GSON.fromJson(json, type);
				} else {
					args[entry.getKey()] = GSON.fromJson(json.get(entry.getValue()), type);
				}
			}
		}
		if(attrNameArgIndex != null && !attrNameArgIndex.isEmpty()) {
			for (Map.Entry<Integer,String> entry : attrNameArgIndex.entrySet()) {
				args[entry.getKey()] = req.getAttribute(entry.getValue());
			}
		}
		if(reqIndex != null && !reqIndex.isEmpty()) {
			for(Integer i:reqIndex) {
				args[i] = req;
			}
		}
		if(respIndex != null && !respIndex.isEmpty()) {
			for(Integer i:respIndex) {
				args[i] = resp;
			}
		}
		return args;
	}
	
	private static JsonObject getOrCreateFatherByPath(JsonObject json,String[] ps) {
		JsonObject jobj = json;
		for(int i = 0; i < ps.length -1 ; i++) {
			String p = ps[i];
			jobj = jobj.getAsJsonObject(p);
			if(jobj == null) {
				jobj = new JsonObject();
				jobj.add(p, jobj);
			}
		}
		return jobj;
	}
	public Object invoke(HttpServletRequest req, HttpServletResponse resp) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	Object[] args = resoveReqParams(req, resp);
		Object obj = method.invoke(bean, args);
		return obj;
	}
}
