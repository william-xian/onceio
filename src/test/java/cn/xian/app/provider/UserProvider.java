package cn.xian.app.provider;

import cn.xian.app.entity.UserChief;
import top.onceio.cache.annotation.Cacheable;
import top.onceio.db.dao.Cnd;
import top.onceio.db.dao.DaoProvider;
import top.onceio.mvc.annocations.AutoApi;
import top.onceio.mvc.annocations.Param;

@AutoApi
@Cacheable
public class UserProvider extends DaoProvider<UserChief,Long> {
	@Cacheable
	public UserChief fetchByName(@Param("name") String name) {
		Cnd<UserChief> cnd = new Cnd<>(UserChief.class);
		cnd.eq().setName(name);
		return super.fetch(null,cnd);
	}
	
	public static void main(String[] args) {
		
		UserProvider up = new UserProvider();
		UserChief uc = up.fetchByName("hello");
		
		System.out.println(uc);
	}
	
}
