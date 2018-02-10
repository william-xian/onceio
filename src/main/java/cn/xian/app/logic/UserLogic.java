package cn.xian.app.logic;

import java.util.HashMap;
import java.util.Map;

import cn.xian.app.model.entity.UserInfo;
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
	
	public UserInfo signup(String account,String passwd) {
		UserInfo entity = new UserInfo();
		entity.setName(account);
		entity.setPasswd(passwd);
		entity = userProvider.insert(entity);
		return entity;
	}
	
	public boolean signin(String account,String passwd) {
		UserInfo uc = userProvider.fetchByName(account);
		if(uc == null) {
			Failed.throwMsg("用户%s不存在", account);
		}
		if(uc.getPasswd() != null  && uc.getPasswd().equals(passwd)) {
			return true;
		}
		return false;
	}
	
	public Page<UserInfo> find(UserInfo uc) {
		System.out.println("find:" + uc);
		Page<UserInfo> ucs = userProvider.find(new Cnd<UserInfo>(UserInfo.class));
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
