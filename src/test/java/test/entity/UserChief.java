package test.entity;

import top.onceio.db.annotation.Col;
import top.onceio.db.annotation.Tbl;
import top.onceio.db.tbl.OEntity;

@Tbl
public class UserChief extends OEntity<Long> {
    @Col(nullable = false, size=32,unique=true)
	private String name;
    @Col(nullable = true, size=64)
    private String passwd;
    @Col(nullable = true, size=255)
    private String avatar;
    @Col(nullable = true, size=255)
    private Integer genre;
    
	public String getName() {
		return name;
	}
	public UserChief setName(String name) {
		this.name = name;
		return this;
	}
	public String getPasswd() {
		return passwd;
	}
	public UserChief setPasswd(String passwd) {
		this.passwd = passwd;
		return this;
	}
	public String getAvatar() {
		return avatar;
	}
	public UserChief setAvatar(String avatar) {
		this.avatar = avatar;
		return this;
	}
	public Integer getGenre() {
		return genre;
	}
	public UserChief setGenre(Integer genre) {
		this.genre = genre;
		return this;
	}
}
