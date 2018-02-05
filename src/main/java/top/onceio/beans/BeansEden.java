package top.onceio.beans;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alibaba.druid.pool.DruidDataSource;

import net.sf.cglib.proxy.Enhancer;
import top.onceio.OnceIO;
import top.onceio.annotation.I18nCfg;
import top.onceio.annotation.I18nCfgBrief;
import top.onceio.annotation.I18nMsg;
import top.onceio.aop.TransactionProxy;
import top.onceio.db.annotation.Tbl;
import top.onceio.db.annotation.TblView;
import top.onceio.db.dao.Cnd;
import top.onceio.db.dao.DaoProvider;
import top.onceio.db.dao.IdGenerator;
import top.onceio.db.dao.impl.DaoHelper;
import top.onceio.db.jdbc.JdbcHelper;
import top.onceio.db.tbl.OEntity;
import top.onceio.db.tbl.OI18n;
import top.onceio.exception.Failed;
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
import top.onceio.util.OUtils;

public class BeansEden {
	private final static Logger LOGGER = Logger.getLogger(BeansEden.class);

	private Map<String, Object> nameToBean = new HashMap<>();
	private ApiResover apiResover = new ApiResover();
	private Properties prop = new Properties();

	private BeansEden() {
	}

	private static volatile BeansEden instance = null;

	public static BeansEden get() {
		synchronized (BeansEden.class) {
			if (instance == null) {
				instance = new BeansEden();
			}
		}
		return instance;
	}

	private void loadDefaultProperties() {
		try {
			InputStream in = null;
			in = OnceIO.getClassLoader().getResourceAsStream("onceio.properties");
			if (in != null) {
				prop.load(in);
				in.close();
			}
		} catch (IOException e) {
			LOGGER.warn(e.getMessage());
		}
	}

	private AnnotationScanner scanner = new AnnotationScanner(Api.class, AutoApi.class, Definer.class, Def.class,
			Using.class, Tbl.class, TblView.class, I18nMsg.class, I18nCfg.class);

	private DataSource createDataSource() {
		String driver = prop.getProperty("onceio.datasource.driver");
		String url = prop.getProperty("onceio.datasource.url");
		String username = prop.getProperty("onceio.datasource.username");
		String password = prop.getProperty("onceio.datasource.password");
		String maxActive = prop.getProperty("onceio.datasource.maxActive", "3");
		DruidDataSource ds = new DruidDataSource();
		ds.setDriverClassName(driver);
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setMaxActive(Integer.parseInt(maxActive));
		return ds;
	}

	@SuppressWarnings("unchecked")
	public List<Class<? extends OEntity>> matchTblTblView() {
		List<Class<? extends OEntity>> entities = new LinkedList<>();
		for (Class<?> clazz : scanner.getClasses(Tbl.class)) {
			if (OEntity.class.isAssignableFrom(clazz)) {
				entities.add((Class<? extends OEntity>) clazz);
			}
		}
		for (Class<?> clazz : scanner.getClasses(TblView.class)) {
			if (OEntity.class.isAssignableFrom(clazz)) {
				entities.add((Class<? extends OEntity>) clazz);
			}
		}
		return entities;
	}

	private IdGenerator createIdGenerator() {
		return new IdGenerator() {
			@Override
			public Long next(Class<?> entityClass) {
				return IDGenerator.randomID();
			}
		};
	}

	private JdbcHelper createJdbcHelper(DataSource ds, IdGenerator idGenerator,
			List<Class<? extends OEntity>> entities) {
		JdbcHelper jdbcHelper = new JdbcHelper();
		jdbcHelper.setDataSource(ds);
		return jdbcHelper;
	}

	private DaoHelper createDaoHelper(JdbcHelper jdbcHelper, IdGenerator idGenerator,
			List<Class<? extends OEntity>> entities) {
		DaoHelper daoHelper = new DaoHelper();
		daoHelper.init(jdbcHelper, idGenerator, entities);
		return daoHelper;
	}

	private void loadConfig(Class<?> clazz, Object bean, Field field) {
		Config cnfAnn = field.getAnnotation(Config.class);
		if (cnfAnn != null) {
			Class<?> fieldType = field.getType();
			String val = prop.getProperty(cnfAnn.value());
			if (val != null) {
				try {
					if (OReflectUtil.isBaseType(fieldType)) {
						field.set(bean, OReflectUtil.strToBaseType(fieldType, val));
					} else {
						LOGGER.error(String.format("属性不支持该类型：%s", fieldType.getName()));
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					LOGGER.error(e.getMessage(), e);
				}
			} else {
				LOGGER.error(String.format("找不到属性：%s", cnfAnn.value()));
			}
		}
	}

	private void loadConfig(Class<?> clazz, Object bean) {
		if (clazz != null && bean != null) {
			for (Field field : clazz.getFields()) {
				loadConfig(clazz, bean, field);
			}
		}
	}

	private void loadDefiner() {
		Set<Class<?>> definers = scanner.getClasses(Definer.class);
		for (Class<?> defClazz : definers) {
			try {
				Object def = defClazz.newInstance();
				loadConfig(defClazz, def);
				for (Method method : defClazz.getMethods()) {
					Def defAnn = method.getAnnotation(Def.class);
					if (defAnn != null) {
						if (method.getParameterTypes().length == 0) {
							Class<?> beanType = method.getReturnType();
							if (!beanType.equals(void.class)) {
								String beanName = defAnn.value();
								try {
									Object bean = method.invoke(def);
									store(beanType, beanName, bean);
								} catch (IllegalArgumentException | InvocationTargetException e) {
									LOGGER.warn("Def 生成Bean失败 " + e.getMessage());
								}
							} else {
								LOGGER.warn("Def 作用在返回值上");
							}
						} else {
							LOGGER.warn("Def 不支持带参数的构造函数");
						}
					}
				}
			} catch (InstantiationException | IllegalAccessException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private void loadDefined() {
		Set<Class<?>> definers = scanner.getClasses(Def.class);
		for (Class<?> defClazz : definers) {
			try {
				TransactionProxy cglibProxy = new TransactionProxy();
				Enhancer enhancer = new Enhancer();
				enhancer.setSuperclass(defClazz);
				enhancer.setCallback(cglibProxy);
				Object bean = enhancer.create();

				Def defAnn = defClazz.getAnnotation(Def.class);
				String beanName = defAnn.value();
				store(defClazz, beanName, bean);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private void loadApiAutoApi() {
		Set<Class<?>> definers = scanner.getClasses(Api.class, AutoApi.class);
		for (Class<?> defClazz : definers) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("load Api: " + defClazz.getName());
			}
			try {
				TransactionProxy cglibProxy = new TransactionProxy();
				Enhancer enhancer = new Enhancer();
				enhancer.setSuperclass(defClazz);
				enhancer.setCallback(cglibProxy);
				Object bean = enhancer.create();

				store(defClazz, null, bean);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private void linkBeans() {
		Iterator<Object> beans = new HashSet<>(nameToBean.values()).iterator();
		while (beans.hasNext()) {
			Object bean = beans.next();
			Class<?> beanClass = bean.getClass();
			List<Class<?>> classes = new ArrayList<>();
			for (Class<?> clazz = beanClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
				classes.add(0, clazz);
			}
			for (Class<?> clazz : classes) {
				for (Field field : clazz.getDeclaredFields()) {
					loadConfig(clazz, bean, field);
					Using usingAnn = field.getAnnotation(Using.class);
					if (usingAnn != null) {
						Class<?> fieldType = field.getType();
						field.setAccessible(true);
						Object fieldBean = load(fieldType, usingAnn.value());
						if (fieldBean != null) {
							try {
								field.set(bean, fieldBean);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								LOGGER.error(e.getMessage(), e);
							}
						} else {
							LOGGER.error(String.format("找不到 %s:%s", fieldType.getName(), usingAnn.value()));
						}
					}
				}
			}
		}
	}

	private void executeOnCreate(Object bean, Method method) {
		OnCreate onCreateAnn = method.getAnnotation(OnCreate.class);
		if (onCreateAnn != null) {
			if (method.getParameterCount() == 0) {
				try {
					method.invoke(bean);
				} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
					LOGGER.error(e.getMessage(), e);
				}
			} else {
				LOGGER.error(String.format("初始化函数%s,不应该有参数", method.getName()));
			}
		}
	}

	private void checkOnDestroy(Object bean, Method method) {
		OnDestroy onDestroyAnn = method.getAnnotation(OnDestroy.class);
		if (onDestroyAnn != null) {
			if (method.getParameterCount() == 0) {
			} else {
				LOGGER.error(String.format("初始化函数%s,不应该有参数", method.getName()));
			}
		}
	}

	private void resoveApi(Class<?> clazz, Api fatherApi, Api methodApi, Object bean, Method method) {
		String api = fatherApi.value() + methodApi.value();
		ApiMethod[] apiMethods = methodApi.method();
		if (apiMethods.length == 0) {
			apiMethods = fatherApi.method();
		}
		if (apiMethods.length == 0) {
			LOGGER.error("Api的不能为空");
		}
		for (ApiMethod apiMethod : apiMethods) {
			apiResover.push(apiMethod, api, bean, method);
		}
	}

	private void resoveAutoApi(Class<?> clazz, AutoApi autoApi, Api methodApi, Object bean, Method method,
			String methodName) {
		String api = autoApi.value().getSimpleName().toLowerCase();
		if (methodName != null) {
			api = api + "/" + methodName;
		}
		for (ApiMethod apiMethod : methodApi.method()) {
			apiResover.push(apiMethod, api, bean, method);
		}
	}

	private void resoveBeanMethod() {
		Iterator<Object> beans = new HashSet<>(nameToBean.values()).iterator();
		while (beans.hasNext()) {
			Object bean = beans.next();
			Class<?> clazz = bean.getClass();
			if (clazz.getName().contains("$$EnhancerByCGLIB$$")) {
				clazz = clazz.getSuperclass();
			}
			Api fatherApi = clazz.getAnnotation(Api.class);
			AutoApi autoApi = clazz.getAnnotation(AutoApi.class);
			Set<String> ignoreMethods = new HashSet<>();
			for (Method method : clazz.getDeclaredMethods()) {
				executeOnCreate(bean, method);
				checkOnDestroy(bean, method);
				Api methodApi = method.getAnnotation(Api.class);
				if (fatherApi != null && methodApi != null) {
					resoveApi(clazz, fatherApi, methodApi, bean, method);
				}
				if (autoApi != null && methodApi != null) {
					ignoreMethods.add(method.getName() + method.getParameterTypes().hashCode());
					if (!methodApi.value().equals("")) {
						resoveAutoApi(clazz, autoApi, methodApi, bean, method, methodApi.value());
					} else {
						resoveAutoApi(clazz, autoApi, methodApi, bean, method, method.getName());
					}
				}
			}
			if (autoApi != null) {
				if (clazz.isAssignableFrom(DaoProvider.class)) {
					for (Method method : DaoProvider.class.getDeclaredMethods()) {
						Api methodApi = method.getAnnotation(Api.class);
						if (methodApi != null
								&& !ignoreMethods.contains(method.getName() + method.getParameterTypes().hashCode())) {
							resoveAutoApi(clazz, autoApi, methodApi, bean, method, null);
						}
					}
				}
			}
		}

		apiResover.build();
	}

	public void resovle(String... packages) {
		loadDefaultProperties();
		nameToBean.clear();
		scanner.scanPackages(packages);
		scanner.putClass(Tbl.class, OI18n.class);
		scanner.putClass(AutoApi.class, OI18nProvider.class);

		loadDefiner();
		DataSource ds = load(DataSource.class, null);
		if (ds == null) {
			ds = createDataSource();
			store(DataSource.class, null, ds);
		}
		IdGenerator idGenerator = load(IdGenerator.class, null);
		if (idGenerator == null) {
			idGenerator = createIdGenerator();
			store(IdGenerator.class, null, idGenerator);
		}
		JdbcHelper jdbcHelper = load(JdbcHelper.class, null);
		if (jdbcHelper == null) {
			jdbcHelper = createJdbcHelper(ds, idGenerator, matchTblTblView());
			store(JdbcHelper.class, null, jdbcHelper);
		}
		DaoHelper daoHelper = load(DaoHelper.class, null);
		if (daoHelper == null) {
			daoHelper = createDaoHelper(jdbcHelper, idGenerator, matchTblTblView());
			store(DaoHelper.class, null, daoHelper);
		}
		loadDefined();

		loadApiAutoApi();

		linkBeans();

		resoveBeanMethod();

		init();

	}

	protected <T> void store(Class<T> clazz, String beanName, Object bean) {
		if (beanName == null) {
			beanName = "";
		}
		OAssert.err(bean != null, "%s:%s can not be null!", clazz.getName(), beanName);
		LOGGER.debug("bean name=" + clazz.getName() + ":" + beanName);
		nameToBean.put(clazz.getName() + ":" + beanName, bean);
		for (Class<?> iter : clazz.getInterfaces()) {
			nameToBean.put(iter.getName() + ":" + beanName, bean);
			LOGGER.debug("beanName=" + iter.getName() + ":" + beanName);
		}
	}

	public <T> T load(Class<T> clazz) {
		return load(clazz, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T load(Class<T> clazz, String beanName) {
		if (beanName == null) {
			beanName = "";
		}
		Object v = nameToBean.get(clazz.getName() + ":" + beanName);
		if (v != null) {
			return (T) v;
		}
		return null;
	}

	public <T> void erase(Class<T> clazz, String beanName) {
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

	public void init() {
		annlysisI18nMsg();
		annlysisConst();
	}

	private void annlysisI18nMsg() {
		OI18nProvider dao = this.load(OI18nProvider.class);
		Set<Class<?>> classes = scanner.getClasses(I18nMsg.class);
		if (classes == null)
			return;
		List<OI18n> i18ns = new ArrayList<>();
		for (Class<?> clazz : classes) {
			I18nMsg group = clazz.getAnnotation(I18nMsg.class);
			for (Field field : clazz.getFields()) {
				field.setAccessible(true);
				try {
					String name = field.get(null).toString();
					String key = "msg/" + group.value() + "_" + OUtils.encodeMD5(name);
					Cnd<OI18n> cnd = new Cnd<>(OI18n.class);
					cnd.eq().setKey(key);
					OI18n i18n = dao.fetch(null, cnd);
					if (i18n == null) {
						i18n = new OI18n();
						i18n.setKey(key);
						i18n.setName(name);
						i18ns.add(i18n);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Failed.throwError(e.getMessage());
				}
			}
		}
		dao.batchInsert(i18ns);
	}

	private void annlysisConst() {
		OI18nProvider dao = this.load(OI18nProvider.class);
		Set<Class<?>> classes = scanner.getClasses(I18nCfg.class);
		if (classes == null)
			return;
		List<OI18n> i18ns = new ArrayList<>();
		for (Class<?> clazz : classes) {
			I18nCfg group = clazz.getAnnotation(I18nCfg.class);
			for (Field field : clazz.getFields()) {
				field.setAccessible(true);
				I18nCfgBrief cons = field.getAnnotation(I18nCfgBrief.class);
				try {
					String fieldname = field.getName();
					String val = field.get(null).toString();
					String key = "const/" + group.value() + "_" + clazz.getSimpleName() + "_" + fieldname;
					String name = cons.value();
					Cnd<OI18n> cnd = new Cnd<>(OI18n.class);
					cnd.eq().setKey(key);
					OI18n i18n = dao.fetch(null, cnd);

					if (i18n == null) {
						i18n = new OI18n();
						i18n.setKey(key);
						i18n.setName(name);
						i18n.setVal(val);
						LOGGER.debug("add: " + i18n);
						i18ns.add(i18n);
					} else {
						/** The val depend on database */
						if (!val.equals(i18n.getVal())) {
							i18n.setVal(val);
							field.set(null, OReflectUtil.strToBaseType(field.getType(), val));
							LOGGER.debug("reload: " + i18n);
						}
						if (!i18n.getName().equals(name)) {
							i18n.setName(name);
							dao.insert(i18n);
							LOGGER.debug("update: " + i18n);
						}
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Failed.throwError(e.getMessage());
				}
			}
		}
		dao.batchInsert(i18ns);
	}

	public ApiPair search(ApiMethod apiMethod, String uri) {
		return apiResover.search(apiMethod, uri);
	}
}
