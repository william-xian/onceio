package top.onceio.beans;

import java.lang.reflect.Method;
import java.util.TreeMap;

public class ApiResover {

	private TreeMap<String,ApiPair> fixedUri = new TreeMap<>();
	
	private TreeMap<String,ApiPair> floatUri = new TreeMap<>();
	
	//TODO
	public void push(ApiMethod apiMethod,String api,Object bean,Method method) {
		
	}
	//TODO
	public ApiPair search(String uri) {
		return null;
	}
	
}
