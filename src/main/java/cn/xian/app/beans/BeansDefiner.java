package cn.xian.app.beans;

import top.onceio.cache.Cache;
import top.onceio.cache.impl.FIFOMemoryCache;
import top.onceio.mvc.annocations.Def;
import top.onceio.mvc.annocations.Definer;

@Definer
public class BeansDefiner {

	@Def
	public Cache createFIFOMemoryCache(){
		return new FIFOMemoryCache(1000);
	}
}
