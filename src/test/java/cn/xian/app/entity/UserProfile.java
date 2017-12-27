package cn.xian.app.entity;

import top.onceio.db.annotation.Col;
import top.onceio.db.annotation.Tbl;
import top.onceio.db.tbl.OEntity;

@Tbl(extend=UserChief.class)
public class UserProfile extends OEntity<Long>{
    @Col(nullable = false, size=20)
	private String nickname;
    @Col(nullable = false)
	private Boolean gender;
    @Col(nullable = false, size=16)
	private String phone;
    
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Boolean getGender() {
		return gender;
	}
	public void setGender(Boolean gender) {
		this.gender = gender;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
}