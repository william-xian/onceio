package top.onceio;

import test.entity.UserChief;
import top.onceio.db.dao.tpl.SelectTpl;
import top.onceio.db.dao.tpl.UpdateTpl;

public class Main {
	
	public static void main(String[] args) {
		System.out.println("hello");
		
		SelectTpl<UserChief> distinct = new SelectTpl<UserChief>(UserChief.class);
		distinct.using().setGenre(SelectTpl.USING_INT);
		System.out.println(distinct.sql());

		UpdateTpl<UserChief> tpl = new UpdateTpl<>(UserChief.class);
		tpl.set().setId(1L);
		tpl.add().setGenre(1);

		System.out.println(tpl.getSetTpl());
	}
	
}
