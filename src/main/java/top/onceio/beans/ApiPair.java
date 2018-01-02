package top.onceio.beans;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
	private String[] fromParam;
	private String[] fromCookie;
	private String[] fromAttr;
	
	
	public ApiPair(ApiMethod apiMethod,String api, Object bean, Method method) {
		super();
		this.apiMethod = apiMethod;
		this.api = api;
		this.bean = bean;
		this.method = method;
		String[] names = api.split("/");
		nameVarIndex = new HashMap<>(names.length);
		for (int i = 0; i < names.length; i++) {
			int end = names.length - 1;
			if (names[i].charAt(0) == '{' && names[i].charAt(end) == '}') {
				nameVarIndex.put(names[i].substring(0, end), i);
			}
		}
		nameType = new HashMap<>(method.getParameterCount());
		List<String> fromP = new ArrayList<>(method.getParameterCount());
		List<String> fromA = new ArrayList<>(method.getParameterCount());
		List<String> fromC = new ArrayList<>(method.getParameterCount());
		for (Parameter param : method.getParameters()) {
			Param paramAnn = param.getAnnotation(Param.class);
			Attr attrAnn = param.getAnnotation(Attr.class);
			top.onceio.mvc.annocations.Cookie cookieAnn = param.getAnnotation(top.onceio.mvc.annocations.Cookie.class);
			String name = null;
			if (paramAnn != null) {
				name = paramAnn.value();
				fromP.add(name);
			} else if (attrAnn != null) {
				name = attrAnn.value();
				fromA.add(name);
			} else if (cookieAnn != null) {
				name = cookieAnn.value();
				fromC.add(name);
			}

			if (name != null) {
				if (nameType.containsKey(name)) {
					Failed.throwError("变量%s，命名冲突", paramAnn.value());
				}
				nameType.put(name, param.getType());
			}
		}
	}
	public void resoveUriParams(Map<String, Object> result, String uri) {
		String[] uris = uri.split("/");
		for (String name : nameVarIndex.keySet()) {
			Integer i = nameVarIndex.get(name);
			String v = uris[i];
			Class<?> type = nameType.get(name);
			Object obj = OReflectUtil.strToBaseType(type, v);
			result.put(name, obj);
		}
	}

	/**
	 * 根据方法参数及其注解，从req（Attr,Param,Body,Cooke)中取出数据
	 * @param result
	 * @param req
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 * @throws IOException
	 */	
	public void resoveReqParams(Map<String,Object> result, HttpServletRequest req) throws JsonSyntaxException, JsonIOException, IOException {
		JsonObject json = GSON.fromJson(req.getReader(), JsonObject.class);
		Map<String,String[]> map = req.getParameterMap();
		for(String key:map.keySet()) {
			String[] val = map.get(key);
			if(val.length == 1) {
				setStringByPath(json,key,val[0]);
			} else if(val.length > 2) {
				setArrayByPath(json,key,val);
			}
		}
		for(String name:nameType.keySet()) {
			Class<?> type = nameType.get(name);
			if(OReflectUtil.isBaseType(type)) {
				result.put(name, OReflectUtil.strToBaseType(type, map.get(name)[0]));
			}
			
			Object v = GSON.fromJson(json.toString(), type);
			result.put(name, v);
		}
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
	
	private static void setStringByPath(JsonObject obj,String path,String val) {
		String[] ps = path.split(".");
		JsonObject result = obtainFather(obj,ps);
		result.addProperty(ps[ps.length-1], val);
	}
	private static void setArrayByPath(JsonObject obj,String path,String[] vals) {
		String[] ps = path.split(".");
		JsonObject result = obtainFather(obj,ps);
		JsonArray ja = new JsonArray();
		for(String v:vals) {
			ja.add(v);
		}
		result.add(ps[ps.length-1], ja);
	}
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		System.out.println("--");
		Object bean = new Object();
		new ApiPair(ApiMethod.DELETE,"api",bean ,ApiPair.class.getMethod("a",String.class));
	
	}
}
