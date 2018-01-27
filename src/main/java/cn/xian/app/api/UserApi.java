package cn.xian.app.api;

import java.util.Map;

import cn.xian.app.entity.UserChief;
import cn.xian.app.executor.UserService;
import top.onceio.db.dao.Page;
import top.onceio.mvc.annocations.Api;
import top.onceio.mvc.annocations.Param;
import top.onceio.mvc.annocations.Using;

@Api("/user")
public class UserApi {
	
	@Using
	private UserService userService;
	
	@Api("/signup/{username}")
	public boolean signup(@Param("username") String username, @Param("passwd") String passwd) {
		return userService.signup(username, passwd);
	}

	@Api("/signin/{username}")
	public Page<UserChief> signin(@Param("username") String username, @Param("passwd") String passwd) {
		return userService.signin(username, passwd);
	}	
	@Api("/transfer")
	public Map<String,Object> transfer(@Param("from") Long from, @Param("to") Long to,@Param("v")Integer v) {
		return userService.transfer(from, to, v);
	}
}
