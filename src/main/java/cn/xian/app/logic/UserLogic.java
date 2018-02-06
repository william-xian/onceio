package cn.xian.app.logic;

import java.util.HashMap;
import java.util.Map;

import cn.xian.app.model.entity.UserChief;
import cn.xian.app.provider.UserProvider;
import cn.xian.app.provider.WalletProvider;
import top.onceio.aop.annotation.Transactional;
import top.onceio.db.dao.Cnd;
import top.onceio.db.dao.Page;
import top.onceio.exception.Failed;
import top.onceio.mvc.annocations.Def;
import top.onceio.mvc.annocations.Using;

@Def
public class UserLogic {

	@Using
	private UserProvider userProvider;
	@Using
	private WalletProvider walletProvider;
	
	public UserChief signup(String account,String passwd) {
		UserChief entity = new UserChief();
		entity.setName(account);
		entity.setPasswd(passwd);
		entity = userProvider.insert(entity);
		return entity;
	}
	
	public boolean signin(String account,String passwd) {
		UserChief uc = userProvider.fetchByName(account);
		if(uc == null) {
			Failed.throwMsg("用户%s不存在", account);
		}
		if(uc.getPasswd() != null  && uc.getPasswd().equals(passwd)) {
			return true;
		}
		return false;
	}
	
	public Page<UserChief> find(UserChief uc) {
		System.out.println("find:" + uc);
		Page<UserChief> ucs = userProvider.find(new Cnd<UserChief>(UserChief.class));
		return ucs;
	}
	
	@Transactional
	public Map<String,Object> transfer(Long from,Long to,Integer v) {
		Map<String,Object> result = new HashMap<>();
		result.put("before-a", walletProvider.get(from));
		result.put("before-b", walletProvider.get(to));
		walletProvider.transfer(from, to, v);
		result.put("after-a", walletProvider.get(from));
		result.put("after-b", walletProvider.get(to));
		return result;
	}
}
