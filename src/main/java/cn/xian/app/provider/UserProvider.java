package cn.xian.app.provider;

import cn.xian.app.entity.UserChief;
import top.onceio.cache.annotation.Cacheable;
import top.onceio.db.dao.Cnd;
import top.onceio.db.dao.DaoProvider;
import top.onceio.mvc.annocations.Api;
import top.onceio.mvc.annocations.AutoApi;
import top.onceio.mvc.annocations.Param;

@AutoApi(UserChief.class)
@Cacheable
public class UserProvider extends DaoProvider<UserChief> {
	@Cacheable
	@Api
	public UserChief fetchByName(@Param("name") String name) {
		Cnd<UserChief> cnd = new Cnd<>(UserChief.class);
		cnd.eq().setName(name);
		return super.fetch(null,cnd);
	}
}
