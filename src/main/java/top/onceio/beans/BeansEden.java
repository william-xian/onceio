package top.onceio.beans;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alibaba.druid.pool.DruidDataSource;

import top.onceio.OnceIO;
import top.onceio.db.annotation.Tbl;
import top.onceio.db.annotation.TblView;
import top.onceio.db.dao.IdGenerator;
import top.onceio.db.dao.impl.DaoHelper;
import top.onceio.db.jdbc.JdbcHelper;
import top.onceio.db.tbl.OEntity;
import top.onceio.mvc.annocations.Api;
import top.onceio.mvc.annocations.AutoApi;
import top.onceio.mvc.annocations.Config;
import top.onceio.mvc.annocations.Def;
import top.onceio.mvc.annocations.Definer;
import top.onceio.mvc.annocations.OnCreate;
import top.onceio.mvc.annocations.OnDestroy;
import top.onceio.mvc.annocations.Using;
import top.onceio.util.AnnotationScanner;
import top.onceio.util.IDGenerator;
import top.onceio.util.OAssert;
import top.onceio.util.OReflectUtil;

public class BeansEden {

	private static final Logger LOGGER = Logger.getLogger(BeansEden.class);
	
	private final static Map<String,Object> nameToBean = new HashMap<>();

	private final static ApiResover apiResover = new ApiResover();
	private final static Map<String,Object> nameToDef = new HashMap<>();
	private final static Properties prop = new Properties();
	
	static {
		try {
			InputStream in = OnceIO.getClassLoader().getResourceAsStream("onceio.properties");
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
		String maxActive = prop.getProperty("onceio.datasource.maxActive","3");
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
		return daoHelper;
	}
	
	private static void loadConfig(Class<?> clazz,Object bean,Field field) {
		Config cnfAnn = field.getAnnotation(Config.class);
		if(cnfAnn != null) {
			Class<?> fieldType = field.getType();
			String  val = prop.getProperty(cnfAnn.value());
			if(val != null) {
				try {

					if(OReflectUtil.isBaseType(fieldType)) {
						field.set(bean, OReflectUtil.strToBaseType(fieldType, val));
					}else {
						LOGGER.error(String.format("属性不支持该类型：%s",fieldType.getName()));
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					LOGGER.error(e.getMessage(),e);
				}
			} else {
				LOGGER.error(String.format("找不到属性：%s",cnfAnn.value()));
			}
			
		}
	}
	
	private static void loadConfig(Class<?> clazz,Object bean) {
		if(clazz!= null && bean != null) {
			for(Field field:clazz.getFields()) {
				loadConfig(clazz,bean,field);
			}	
		}
	}
	
	private static void loadDefiner() {
		Set<Class<?>> definers = scanner.getClasses(Definer.class);
		for(Class<?> defClazz:definers) {
			try {
				Object def = defClazz.newInstance();
				loadConfig(defClazz,def);
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
				LOGGER.error(e.getMessage(),e);
			}
		}
		
	}

	private static void loadApiAutoApi() {
		Set<Class<?>> definers = scanner.getClasses(Api.class,AutoApi.class);
		for(Class<?> defClazz:definers) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("load Apis: "+defClazz.getName());
			}
			try {
				Object bean = defClazz.newInstance();
				store(defClazz,null, bean);
			} catch (InstantiationException|IllegalAccessException e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
		
	}

	private static void linkBeans() {
		Iterator<Object> beans = nameToBean.values().iterator();

		while(beans.hasNext()) {
			Object bean = beans.next();
			Class<?> clazz = bean.getClass();
			for(Field field : clazz.getFields()) {
				
				loadConfig(clazz,bean,field);
				
				Using usingAnn = field.getAnnotation(Using.class);
				if(usingAnn != null) {
					Class<?> fieldType = field.getType();
					Object fieldBean = load(fieldType,usingAnn.value());
					if(fieldBean != null) {
						try {
							field.set(bean, fieldBean);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							LOGGER.error(e.getMessage(),e);
						}
					} else {
						LOGGER.error(String.format("找不到 %s:%s", fieldType.getName(),usingAnn.value()));
					}
					
				}
			}
			
		}
	}
	
	private static void executeOnCreate(Object bean,Method method) {
		OnCreate onCreateAnn = method.getAnnotation(OnCreate.class);
		if(onCreateAnn != null) {
			if(method.getParameterCount() == 0) {
				try {
					method.invoke(bean);
				} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
					LOGGER.error(e.getMessage(),e);
				}
			} else {
				LOGGER.error(String.format("初始化函数%s,不应该有参数", method.getName()));
			}
			
		}
	}

	private static void checkOnDestroy(Object bean,Method method) {
		OnDestroy onDestroyAnn = method.getAnnotation(OnDestroy.class);
		if(onDestroyAnn != null) {
			if(method.getParameterCount() == 0) {
			} else {
				LOGGER.error(String.format("初始化函数%s,不应该有参数", method.getName()));
			}
			
		}
	}
	private static void resoveApi(Class<?> clazz,Api fatherApi,Api methodApi,Object bean,Method method){
		String api = fatherApi.value() + methodApi.value();
		ApiMethod[] apiMethods = methodApi.apiMethod();
		if(apiMethods.length == 0) {
			apiMethods = fatherApi.apiMethod();
		}
		if(apiMethods.length == 0) {
			LOGGER.error("Api的不能为空");
		}
		for(ApiMethod apiMethod:apiMethods) {
			apiResover.push(apiMethod,api, bean, method);	
		}
	}

	private static void resoveAutoApi(Class<?> clazz, AutoApi autoApi, Object bean, Method method) {
		String api = autoApi.value().getSimpleName().toLowerCase();
		if (method.getName().equals("get")) {
			apiResover.push(ApiMethod.GET, api + "/{id}", bean, method);
		} else if (method.getName().equals("insert")) {
			apiResover.push(ApiMethod.POST, api, bean, method);
		} else if (method.getName().equals("update")) {
			apiResover.push(ApiMethod.PUT, api, bean, method);
		} else if (method.getName().equals("updateIgnoreNull")) {
			apiResover.push(ApiMethod.PATCH, api, bean, method);
		} else if (method.getName().equals("delete")) {
			apiResover.push(ApiMethod.DELETE, api, bean, method);
		} else if (method.getName().equals("recovery")) {
			apiResover.push(ApiMethod.RECOVERY, api, bean, method);
		} else if(method.getDeclaringClass().equals(clazz)){
			apiResover.push(ApiMethod.GET, api + "/" + method.getName(), bean, method);
		}
	}
	
	private static void resoveDef(Class<?> clazz,Def def,Object bean){
		nameToDef.put(clazz.getName()+":"+def.value(), bean);
	}
	private static void resoveBeanMethod() {
		Iterator<Object> beans = nameToBean.values().iterator();
		while(beans.hasNext()) {
			Object bean = beans.next();
			Class<?> clazz = bean.getClass();
			Api fatherApi = clazz.getAnnotation(Api.class);
			AutoApi autoApi = clazz.getAnnotation(AutoApi.class);
			for(Method method : clazz.getDeclaredMethods()) {
				executeOnCreate(bean,method);
				checkOnDestroy(bean,method);
				Api methodApi = method.getAnnotation(Api.class);
				if(fatherApi != null && methodApi != null) {
					resoveApi(clazz,fatherApi,methodApi,bean,method);
				}
			}

			if (autoApi != null) {
				for (Method method : clazz.getMethods()) {
					executeOnCreate(bean, method);
					checkOnDestroy(bean, method);
					resoveAutoApi(clazz, autoApi, bean, method);
				}
			}
			Def def = clazz.getAnnotation(Def.class);
			if(def != null) {
				resoveDef(clazz,def,bean);
			}
		}

		apiResover.build();
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
		
		resoveBeanMethod();
		
	}
	
	protected static <T> void store(Class<T> clazz,String beanName,Object bean) {
		if(beanName  == null) {
			beanName = "";
		}
		OAssert.err(bean != null,"%s:%s can not be null!", clazz.getName(),beanName);
		nameToBean.put(clazz.getName()+":" + beanName,bean);
	}
	
	public static <T> T load(Class<T> clazz) {
		return load(clazz,null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T load(Class<T> clazz, String beanName) {
		if(beanName  == null) {
			beanName = "";
		}
		Object v = nameToBean.get(clazz.getName()+":" + beanName);
		if(v != null) {
			return (T)v;
		}
		return null;
	}
	
	public static <T> void erase(Class<T> clazz, String beanName) {
		if (beanName == null) {
			beanName = "";
		}
		Object bean = load(clazz, beanName);
		if (bean != null) {
			for (Method method : clazz.getMethods()) {
				OnDestroy onDestroyAnn = method.getAnnotation(OnDestroy.class);
				if (onDestroyAnn != null) {
					if (method.getParameterCount() == 0) {
						try {
							method.invoke(bean);
						} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
							LOGGER.error(e.getMessage(), e);
						}
					} else {
						LOGGER.error(String.format("构造%s,不应该有参数", method.getName()));
					}
				}
			}
		} else {
			LOGGER.error(String.format("找不到Bean对象：  %s:%s", clazz.getName(), beanName));
		}
	}
	
	public static ApiPair search(ApiMethod apiMethod,String uri) {
		return apiResover.search(apiMethod,uri);
	}
}
