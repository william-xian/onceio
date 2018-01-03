package top.onceio.beans;

import org.junit.Assert;
import org.junit.Test;


public class ApiResoverTest {


	public void a() {
	}

	public void a1() {
	}

	public void ab() {
	}

	public void av1(String v1) {
	}

	public void av1b(String v1) {
	}

	public void av1v2(String v1, String v2) {
	}

	@Test
	public void test() throws NoSuchMethodException, SecurityException {
		ApiResover r = new ApiResover();
		ApiResoverTest bean = new ApiResoverTest();
		r.push(ApiMethod.GET, "/a/", bean, ApiResoverTest.class.getMethod("a"));
		r.push(ApiMethod.GET, "/a", bean, ApiResoverTest.class.getMethod("a"));
		r.push(ApiMethod.GET, "/a/b", bean, ApiResoverTest.class.getMethod("ab"));
		r.push(ApiMethod.GET, "/a/{v1}", bean, ApiResoverTest.class.getMethod("av1", String.class));
		r.push(ApiMethod.GET, "/a/{v1}/b", bean, ApiResoverTest.class.getMethod("av1", String.class));
		r.push(ApiMethod.GET, "/a/{v1}/{v2}", bean, ApiResoverTest.class.getMethod("av1v2", String.class, String.class));
		r.build();
		ApiPair ap = r.search(ApiMethod.GET, "/a/z");
		Assert.assertEquals("/a/{v1}", ap.getApi());
		ap = r.search(ApiMethod.GET, "/a/b");
		Assert.assertEquals("/a/b", ap.getApi());
		ap = r.search(ApiMethod.GET, "/a/z/b");
		Assert.assertEquals("/a/{v1}/b", ap.getApi());
		ap = r.search(ApiMethod.GET, "/a/z/1");
		Assert.assertEquals("/a/{v1}/{v2}", ap.getApi());
	}
}
