package cn.xian.app.api;

import java.util.Map;

import cn.xian.app.logic.UserLogic;
import cn.xian.app.model.entity.UserChief;
import top.onceio.db.dao.Page;
import top.onceio.mvc.annocations.Api;
import top.onceio.mvc.annocations.Param;
import top.onceio.mvc.annocations.Using;

@Api("/user")
public class UserApi {
	
	@Using
	private UserLogic userService;
	
	@Api("/signup/{username}")
	public UserChief signup(@Param("username") String username, @Param("passwd") String passwd) {
		return userService.signup(username, passwd);
	}
	
	@Api("/signin/{username}")
	public boolean signin(@Param("username") String username, @Param("passwd") String passwd) {
		return userService.signin(username, passwd);
	}
	
	@Api("/find")
	public Page<UserChief> find(@Param() UserChief uc) {
		return userService.find(uc);
	}
	
	@Api("/transfer")
	public Map<String,Object> transfer(@Param("from") Long from, @Param("to") Long to,@Param("v")Integer v) {
		return userService.transfer(from, to, v);
	}
}
