package top.onceio.db;

import java.sql.Connection;
import java.sql.Savepoint;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.xian.app.entity.UserChief;
import top.onceio.util.IDGenerator;

public class TransTest extends DaoBaseTest{
	@BeforeClass
	public static void init() {
		initDao();
	}
	
	@Test
	public void tran() {
		UserChief uc1 = new UserChief();
		uc1.setId(IDGenerator.randomID());
		uc1.setName("zhang"+System.currentTimeMillis());
		uc1.setGenre(1);
		
		jdbcHelper.beginTransaction(Connection.TRANSACTION_READ_COMMITTED, false);
		String insertTpl = "INSERT INTO UserChief(id,rm,name,passwd,avatar,genre) VALUES(?,?,?,?,?,?)";
		Long begin = System.currentTimeMillis();
		for(int i = 0;  i < 10; i++) {
			Savepoint spb1 = jdbcHelper.setSavepoint();
			jdbcHelper.update(insertTpl,
						new Object[]{begin+i,false,"Zhang-"+begin+"-"+i,"2","avatar",1});
			Savepoint spb2 = jdbcHelper.setSavepoint();
			jdbcHelper.update(insertTpl,
						new Object[]{begin+1000L+i,false," Wang-"+begin+"-"+i,"2","avatar",1});

			if (i % 4 == 0) {
				if (spb2 != null) {
					jdbcHelper.rollback(spb2);
				} else {
					System.out.println("-----2");
				}
			}
			if (i % 3 == 0) {
				if (spb1 != null) {
					jdbcHelper.rollback(spb1);
				} else {
					System.out.println("-----1");
				}
			}
		}
		jdbcHelper.commit();
		
	}
}
