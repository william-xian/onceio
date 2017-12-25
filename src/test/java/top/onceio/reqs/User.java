package top.onceio.reqs;

import top.onceio.mvc.annocations.Param;
import top.onceio.mvc.annocations.Req;

@Req("/user")
public class User {

	@Req("/{username}")
	public String signin(@Param("username") String username) {
		return "Hello, "+ username;
	}
}
