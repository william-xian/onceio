package cn.xian.app.model.entity;

import top.onceio.db.annotation.Col;
import top.onceio.db.annotation.Tbl;
import top.onceio.db.tbl.OEntity;

@Tbl
public class UserFriend extends OEntity{
	@Col(ref=UserChief.class,nullable = false)
	private Long userId;
	@Col(ref=UserChief.class,nullable = false)
	private Long friendId;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getFriendId() {
		return friendId;
	}
	public void setFriendId(Long friendId) {
		this.friendId = friendId;
	}
}
