package top.onceio.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.Test;

import test.entity.Goods;
import test.entity.GoodsDesc;
import test.entity.GoodsOrder;
import test.entity.GoodsOrderView;
import test.entity.GoodsShipping;
import test.entity.UserChief;
import test.entity.UserFriend;
import test.entity.UserProfile;
import test.entity.Wallet;
import top.onceio.db.dao.IdGenerator;
import top.onceio.db.dao.impl.DaoHelper;
import top.onceio.db.jdbc.JdbcHelper;
import top.onceio.db.tbl.OEntity;
import top.onceio.db.tbl.OI18n;
import top.onceio.util.IDGenerator;

public class DaoBaseTest {
	protected static final JdbcHelper jdbcHelper = new JdbcHelper();

	protected static final DaoHelper daoHelper = new DaoHelper();
	
	public static void initDao() {
		try {
		Properties prop = new Properties();
		prop.load(new FileInputStream("src/main/resources/application.properties"));
		String driver = prop.getProperty("spring.datasource.driver");
		String url = prop.getProperty("spring.datasource.url");
		String username =prop.getProperty("spring.datasource.username");
		String password = prop.getProperty("spring.datasource.password");
		String maxActive = prop.getProperty("spring.datasource.maxActive");
		DataSource ds = new DataSource();
		ds.setDriverClassName(driver);
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setMaxActive(Integer.parseInt(maxActive));
		jdbcHelper.setDataSource(ds);
		System.out.println("loaded jdbcTemplate");
		IdGenerator idGenerator = new IdGenerator() {
			@Override
			public Long next(Class<?> entityClass) {
				return IDGenerator.randomID();
			}
			
		};
		DDHoster.upgrade();
		List<Class<? extends OEntity<?>>> entities = new ArrayList<>();
		entities.add(UserChief.class);
		entities.add(UserProfile.class);
		entities.add(Wallet.class);
		entities.add(UserFriend.class);
		entities.add(Goods.class);
		entities.add(GoodsDesc.class);
		entities.add(GoodsOrder.class);
		entities.add(GoodsShipping.class);
		entities.add(GoodsOrderView.class);
		entities.add(OI18n.class);
		
		daoHelper.init(jdbcHelper, idGenerator,entities);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//@Test
	public void createTbl() {
		initDao();
		System.out.println(OI18n.class);
	}
	
}
