package top.onceio.beans;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alibaba.druid.pool.DruidDataSource;

import top.onceio.db.annotation.Tbl;
import top.onceio.db.annotation.TblView;
import top.onceio.db.dao.IdGenerator;
import top.onceio.db.dao.impl.DaoHelper;
import top.onceio.db.jdbc.JdbcHelper;
import top.onceio.db.tbl.OEntity;
import top.onceio.mvc.annocations.Api;
import top.onceio.mvc.annocations.AutoApi;
import top.onceio.mvc.annocations.Def;
import top.onceio.mvc.annocations.Definer;
import top.onceio.mvc.annocations.Using;
import top.onceio.util.AnnotationScanner;
import top.onceio.util.IDGenerator;

public class BeansEden {

	private static final Logger LOGGER = Logger.getLogger(BeansEden.class);
	
	private final static Map<String,Object> nameToBean = new HashMap<>();
	
	private final static Properties prop = new Properties();
	
	static {
		try {
			FileInputStream in;
			in = new FileInputStream("onceio.properties");
			prop.load(in);
			in.close();
		} catch (IOException e) {
			LOGGER.warn(e.getMessage());
		}
	}
	
	private final static AnnotationScanner scanner = new AnnotationScanner(Api.class,AutoApi.class,
			Definer.class,Def.class,Using.class,
			Tbl.class,TblView.class);
	
	

	private static DataSource createDataSource() {
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
		return ds;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Class<? extends OEntity>> matchTblTblView() {
		List<Class<? extends OEntity>> entities= new LinkedList<>();
		for(Class<?> clazz:scanner.getClasses(Tbl.class)) {
			if(clazz.isAssignableFrom(OEntity.class)) {
				entities.add((Class<? extends OEntity>)clazz);
			}
		}
		for(Class<?> clazz:scanner.getClasses(TblView.class)) {
			if(clazz.isAssignableFrom(OEntity.class)) {
				entities.add((Class<? extends OEntity>)clazz);
			}
		}
		return entities;
	}
	
	private static IdGenerator createIdGenerator() {
		return new IdGenerator(){
			@Override
			public Long next(Class<?> entityClass) {
				return IDGenerator.randomID();
			}
			
		};
	}
	
	private static DaoHelper createDaoHelper(DataSource ds,IdGenerator idGenerator,List<Class<? extends OEntity>> entities) {
		DaoHelper daoHelper = new DaoHelper();
		JdbcHelper jdbcHelper = new JdbcHelper();
		jdbcHelper.setDataSource(ds);
		daoHelper.init(jdbcHelper, idGenerator, entities);
		return null;
	}
	
	private static void loadDefiner() {
		Set<Class<?>> definers = scanner.getClasses(Definer.class);
		for(Class<?> defClazz:definers) {
			try {
				Object def = defClazz.newInstance();
				for(Method method : defClazz.getMethods()) {
					Def defAnn = method.getAnnotation(Def.class);
					if(defAnn != null) {
						if(method.getParameterTypes().length == 0){
							Class<?> beanType = method.getReturnType();
							if(!beanType.equals(void.class)){
								String beanName = defAnn.value();
								try {
									Object bean = method.invoke(def);
									store(beanType,beanName, bean);
								} catch (IllegalArgumentException | InvocationTargetException e) {
									LOGGER.warn("Def 生成Bean失败 "+e.getMessage());
								}
							}else {
								LOGGER.warn("Def 作用在返回值上");
							}
						}else {
							LOGGER.warn("Def 不支持带参数的构造函数");
						}
					}
				}
			} catch (InstantiationException|IllegalAccessException e) {
				LOGGER.warn(e.getMessage());
			}
		}
		
	}
	private static void loadApiAutoApi() {
		Set<Class<?>> definers = scanner.getClasses(Api.class,AutoApi.class);
		for(Class<?> defClazz:definers) {
			try {
				Object bean = defClazz.newInstance();
				store(defClazz,null, bean);
			} catch (InstantiationException|IllegalAccessException e) {
				LOGGER.warn(e.getMessage());
			}
		}
		
	}
	
	//TODO
	private static void linkBeans() {
		
	}
	//TODO OnDestroy
	private static void executeOnCreate() {
		
	}
	public static void resovle(String... packages) {
		nameToBean.clear();
		scanner.scanPackages(packages);
		
		loadDefiner();
		
		DataSource ds = load(DataSource.class,null);
		if(ds == null) {
			ds = createDataSource();
			store(DataSource.class,null,ds);
		}
		
		IdGenerator idGenerator = load(IdGenerator.class,null);
		if(idGenerator == null) {
			idGenerator = createIdGenerator();
			store(IdGenerator.class,null,idGenerator);
		}
		
		DaoHelper daoHelper = load(DaoHelper.class,null);
		if(daoHelper == null) {
			daoHelper = createDaoHelper(ds,idGenerator,matchTblTblView());
			store(DaoHelper.class,null,daoHelper);
		}
		loadApiAutoApi();
		
		linkBeans();
		
		executeOnCreate();
	}
	
	protected static <T> void store(Class<T> clazz,String beanName,Object bean) {
		nameToBean.put(clazz.getName()+":" + beanName,bean);
	}
	
	public static <T> T load(Class<T> clazz) {
		return load(clazz,null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T load(Class<T> clazz, String beanName) {
		Object v = nameToBean.get(clazz.getName()+":" + beanName);
		if(v != null) {
			return (T)v;
		}
		return null;
	}
	//TODO OnDestroy
	public static <T> void erase(Class<T> clazz,String beanName) {
		
	}
	
}
