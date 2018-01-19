package top.onceio.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;

import cn.xian.app.entity.Goods;
import cn.xian.app.entity.GoodsDesc;
import cn.xian.app.entity.GoodsOrder;
import cn.xian.app.entity.GoodsOrderView;
import cn.xian.app.entity.GoodsShipping;
import cn.xian.app.entity.UserChief;
import cn.xian.app.entity.UserFriend;
import cn.xian.app.entity.UserProfile;
import cn.xian.app.entity.Wallet;
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
		prop.load(new FileInputStream("src/main/resources/onceio.properties"));
		String driver = prop.getProperty("onceio.datasource.driver");
		String url = prop.getProperty("onceio.datasource.url");
		String username =prop.getProperty("onceio.datasource.username");
		String password = prop.getProperty("onceio.datasource.password");
		String maxActive = prop.getProperty("onceio.datasource.maxActive");
		DruidDataSource ds = new DruidDataSource();
		ds.setDriverClassName(driver);
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setMaxActive(Integer.parseInt(maxActive));
		jdbcHelper.setDataSource(ds);
		IdGenerator idGenerator = new IdGenerator() {
			@Override
			public Long next(Class<?> entityClass) {
				return IDGenerator.randomID();
			}
			
		};
		DDHoster.upgrade();
		List<Class<? extends OEntity>> entities = new ArrayList<>();
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
