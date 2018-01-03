package top.onceio.beans;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;

import top.onceio.util.OUtils;

public class ApiResover {

	private TreeMap<String, ApiPair> fixedUri = new TreeMap<>();

	public ApiResover push(ApiMethod apiMethod, String api, Object bean, Method method) {
		StringBuilder sb = new StringBuilder();
		String[] ts = api.split("/");
		for (String s : ts) {
			if (s.startsWith("{") && s.endsWith("}")) {
				sb.append("/[^/]*");
			} else if (!s.isEmpty()) {
				sb.append("/" + s);
			}
		}
		String pattern = sb.toString();
		fixedUri.put(apiMethod.name() + ":" + pattern, new ApiPair(apiMethod, api, bean, method));
		return this;
	}
	private String[] apis = null;
	public ApiResover build() {
		apis = fixedUri.keySet().toArray(new String[0]);
		Arrays.sort(apis, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(o2, o1);
			}

		});
		return this;
	}
	
	/**
	 * TODO O3
	 * 	  api 
	 * 1. /a/ 
	 * 2. /a 
	 * 3. /a/b 
	 * 4. /a/{v1} 
	 * 5. /a/{v1}/b 
	 * 6. /a/{v1}/{v2}
	 */
	public ApiPair search(ApiMethod apiMethod, String uri) {
		String target = apiMethod.name() + ":" + uri;
		System.out.println(OUtils.toJSON(apis) + " vs " + target);
		for(String api:apis) {
			if (target.matches(api)) {
				return fixedUri.get(api);
			}
		}
		return null;
	}

}
