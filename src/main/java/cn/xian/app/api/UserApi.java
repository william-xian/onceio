package cn.xian.app.api;

import java.util.HashMap;
import java.util.Map;

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
	@Api("/age/{username}")
	public Map<String,Object> age(@Param("username")String username,@Param("passwd")String passwd) {
		Map<String,Object> map = new HashMap<>();
		map.put("username", username);
		map.put("passwd", passwd);
		map.put("isOk",userService.signup(username, passwd));
		return map;
	}
}
