package cn.xian.app.provider;

import cn.xian.app.model.entity.UserInfo;
import top.onceio.aop.annotation.Cacheable;
import top.onceio.db.dao.Cnd;
import top.onceio.db.dao.DaoProvider;
import top.onceio.mvc.annocations.Api;
import top.onceio.mvc.annocations.AutoApi;
import top.onceio.mvc.annocations.Param;

@AutoApi(UserInfo.class)
@Cacheable
public class UserProvider extends DaoProvider<UserInfo> {
	@Cacheable
	@Api
	public UserInfo fetchByName(@Param("name") String name) {
		Cnd<UserInfo> cnd = new Cnd<>(UserInfo.class);
		cnd.eq().setName(name);
		return super.fetch(null,cnd);
	}
}
