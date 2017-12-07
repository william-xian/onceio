package top.onceio.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import test.entity.Goods;
import test.entity.GoodsDesc;
import test.entity.GoodsOrder;
import test.entity.GoodsOrderView;
import test.entity.GoodsShipping;
import test.entity.UserChief;
import test.entity.UserFriend;
import test.entity.UserProfile;
import test.entity.Wallet;
import top.onceio.db.dao.Cnd;
import top.onceio.db.dao.IdGenerator;
import top.onceio.db.dao.Page;
import top.onceio.db.dao.impl.DaoHelper;
import top.onceio.db.tbl.OEntity;
import top.onceio.util.IDGenerator;

public class DaoBaseTest {
	protected static final JdbcTemplate jdbcTemplate = new JdbcTemplate();

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
		jdbcTemplate.setDataSource(ds);
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
		
		daoHelper.init(jdbcTemplate, idGenerator,entities);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		initDao();
		Cnd<GoodsOrderView> cnd = new Cnd<>(GoodsOrderView.class);
		Page<GoodsOrderView> page = daoHelper.find(GoodsOrderView.class,cnd);
		System.out.println(page);
		
		Cnd<GoodsOrder> rm = new Cnd<>(GoodsOrder.class);
		rm.ge().setId(0L);
		List<Long> ids = Arrays.asList(1L,2L,3L,4L,5L);
		daoHelper.remove(UserChief.class, ids);
		daoHelper.remove(Goods.class, ids);
		daoHelper.remove(GoodsDesc.class, ids);
		daoHelper.remove(GoodsOrder.class, rm);
		Cnd<GoodsOrder> del = new Cnd<>(GoodsOrder.class);
		del.ge().setId(0L);
		daoHelper.delete(GoodsOrder.class, del);
		daoHelper.delete(UserChief.class, ids);
		daoHelper.delete(Goods.class, ids);
		daoHelper.delete(GoodsDesc.class, ids);
	}
}
