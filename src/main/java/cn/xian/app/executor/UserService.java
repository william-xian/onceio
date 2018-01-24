package cn.xian.app.executor;

import top.onceio.mvc.annocations.Def;

@Def
public class UserService {
	
	public boolean signup(String account,String passwd) {
		return account.equals(passwd);
	}
}
