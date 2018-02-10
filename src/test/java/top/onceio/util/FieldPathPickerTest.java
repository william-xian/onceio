package top.onceio.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import top.onceio.aop.proxies.FieldPathPicker;

public class FieldPathPickerTest {

	@Test
	public void test_array() {
		int[] arrInt = new int[]{1,2,3,4};
		FieldPathPicker fpp = new FieldPathPicker(int[].class,"[2]");

		Assert.assertEquals(3, (int)(fpp.getField(arrInt)));
	}

	//@Test
	public void test_List() {

	}
	
	//@Test
	public void test_Map() {
		Map<String,Object> map = new HashMap<>();
		Map<String,Object> a = new TreeMap<>();
		a.put("age", 15);
		map.put("a", a);
		FieldPathPicker fpp = new FieldPathPicker(Map.class,"a.age");
		Assert.assertEquals(15, (int)(fpp.getField(map)));
		
	}
}
