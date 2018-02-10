package top.onceio.aop.proxies;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import top.onceio.exception.Failed;


public class CacheKeyResovler {
	
	public static String extractKey(String key,Object[] args) {
		StringBuilder sb = new StringBuilder();
		List<String> marks = new ArrayList<>();
		int b = 0;
		int e = -1;
		for(int i = 0; i < key.length(); i++) {
			if(key.charAt(i) == '$') {
				b = i;
				sb.append(key.substring(e+1, b));
			}else if(key.charAt(i) == '}') {
				e = i;
				sb.append("%s");
				marks.add(key.substring(b, e+1));
			}
		}
		List<Object> objs = new ArrayList<>(marks.size());
		for(String mark:marks) {
			objs.add(extractObj(mark.substring(2, mark.length() -1),args));
		}
		return String.format(sb.toString(), objs.toArray());
	}

	/**
	 * TODO O3，根据固定的路径和固定的类型编译编译参数对象获取链
	 * @param key
	 * @param args
	 * @return
	 */
	public static Object extractObj(String path, Object[] args) {
		int first = path.indexOf('.');
		String[] p = null;
		if(first >= 0) {
			p = path.split("\\.");
		} else {
			p = new String[] {path};
		}
		int ai = Integer.parseInt(p[0]);
		Object obj = args[ai];
		Class<?> clazz = obj.getClass();
		for(int i = 1 ; i < p.length; i++) {
			try {
				Field field = clazz.getDeclaredField(p[i]);
				field.setAccessible(true);
				obj = field.get(obj);
				clazz = obj.getClass();
			}catch (Exception e) {
				e.printStackTrace();
				Failed.throwError(e.getMessage());
			}
		}
		return obj;
	}

	
}