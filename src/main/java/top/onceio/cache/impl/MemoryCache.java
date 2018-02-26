package top.onceio.cache.impl;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import top.onceio.cache.Cache;
import top.onceio.util.Tuple2;

public class MemoryCache implements Cache {

	private Map<Object, Tuple2<Long, SoftReference<Object>>> objs = new HashMap<>();
	private String name;
	private int size;
	private int length = 0;
	
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void init() {
		if(objs == null) {
			objs = new HashMap<>(size);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object key, Class<T> type) {
		Tuple2<Long, SoftReference<Object>> sf = objs.get(key);
		if (sf == null) {
			return null;
		}
		Object obj = sf.b.get();
		return (T) obj;
	}

	@Override
	public void put(Object key, Object value) {
		if(!objs.containsKey(key)) {
			this.length++;
		}
		objs.put(key,
				new Tuple2<Long, SoftReference<Object>>(System.currentTimeMillis(), new SoftReference<Object>(value)));
	}

	@Override
	public void evict(Object key) {
		this.length--;
		objs.remove(key);
	}

	@Override
	public void clear() {
		this.length = 0;
		objs.clear();
	}

}
