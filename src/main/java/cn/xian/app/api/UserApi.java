package cn.xian.app.api;

import cn.xian.app.executor.UserService;
import top.onceio.mvc.annocations.Api;
import top.onceio.mvc.annocations.Param;
import top.onceio.mvc.annocations.Using;

@Api("/user")
public class UserApi {
	
	@Using
	private UserService userService;
	
	@Api("/{username}")
	public String signin(@Param("username")String username,@Param("age")Integer age) {
		return "Hello, "+ username;
	}
}
