package cn.xian.app.executor;

import cn.xian.app.entity.UserChief;
import cn.xian.app.provider.UserProvider;
import top.onceio.db.dao.Cnd;
import top.onceio.db.dao.Page;
import top.onceio.exception.Failed;
import top.onceio.mvc.annocations.Def;
import top.onceio.mvc.annocations.Using;

@Def
public class UserService {
	
	@Using
	private UserProvider userProvider;
	
	public boolean signup(String account,String passwd) {
		UserChief uc = userProvider.fetchByName(account);
		if(uc == null) {
			Failed.throwMsg("用户%s不存在", account);
		}
		if(uc.getPasswd() != null  && uc.getPasswd().equals(passwd)) {
			return true;
		}
		return false;
	}
	
	public Page<UserChief> signin(String account,String passwd) {
		UserChief entity = new UserChief();
		entity.setName(account);
		entity.setPasswd(passwd);
		userProvider.insert(entity);
		Page<UserChief> ucs = userProvider.find(new Cnd<UserChief>(UserChief.class));
		return ucs;
	}
}
