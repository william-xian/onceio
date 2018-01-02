package top.onceio.beans;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ApiResover {

	private TreeMap<String,Map<String,ApiPair>> fixedUri = new TreeMap<>();
	
	public void push(ApiMethod apiMethod,String api,Object bean,Method method) {
		int sp = api.indexOf('{');
		String head = api;
		String tail = "";
		if(sp > 0) {
			head = api.substring(0, sp-1);
			tail = api.substring(sp-1);
			Map<String,ApiPair> map = fixedUri.get(head);
			if(map == null) {
				map = new HashMap<>();
				fixedUri.put(head, map);	
			}
			map.put(tail+":"+apiMethod.name(), new ApiPair(apiMethod,api,bean,method));
		}
		Map<String,ApiPair> map = fixedUri.get(head);
		if(map == null) {
			map = new HashMap<>();
			fixedUri.put(head, map);	
		}
		map.put(tail+":"+apiMethod.name(), new ApiPair(apiMethod,api,bean,method));
	}
	
	public ApiPair search(ApiMethod apiMethod,String uri) {
		for (int sp = uri.length(); sp >= 0; sp = uri.lastIndexOf('/', sp - 1)) {
			String head = uri.substring(0, sp);
			Map<String,ApiPair> map = fixedUri.get(head);
			if(map != null && !map.isEmpty()) {
				//TODO
				break;
			}
		}
		
		return null;
	}
	
}
