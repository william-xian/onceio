package top.onceio.beans;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;

import top.onceio.db.annotation.Tbl;
import top.onceio.db.annotation.TblView;
import top.onceio.db.tbl.OEntity;
import top.onceio.mvc.annocations.Api;
import top.onceio.mvc.annocations.AutoApi;
import top.onceio.mvc.annocations.Def;
import top.onceio.mvc.annocations.Definer;
import top.onceio.mvc.annocations.Using;
import top.onceio.util.AnnotationScanner;

public class BeansEden {

	private final static Map<String,Object> nameToBean = new HashMap<>();
	
	private final static Properties prop = new Properties();
	
	static {
		try {
			FileInputStream in;
			in = new FileInputStream("onceio.properties");
			prop.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private final static AnnotationScanner scanner = new AnnotationScanner(Api.class,AutoApi.class,
			Definer.class,Def.class,Using.class,
			Tbl.class,TblView.class);
	
	
	@SuppressWarnings("unchecked")
	public List<Class<? extends OEntity<?>>> scanTblTblView() {
		List<Class<? extends OEntity<?>>> entities= new LinkedList<>();
		scanner.scanPackages("cn.mx.app");
		for(Class<?> clazz:scanner.getClasses(Tbl.class)) {
			if(clazz.isAssignableFrom(OEntity.class)) {
				entities.add((Class<? extends OEntity<?>>)clazz);
			}
		}
		for(Class<?> clazz:scanner.getClasses(TblView.class)) {
			if(clazz.isAssignableFrom(OEntity.class)) {
				entities.add((Class<? extends OEntity<?>>)clazz);
			}
		}
		return entities;
	}
	
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
	
	public static void resovle(String... packages) {
		nameToBean.clear();
		scanner.scanPackages(packages);
		//DataSource
		DataSource ds = load(DataSource.class,null);
		if(ds == null) {
			ds = createDataSource();
			store(DataSource.class,null,ds);
		}
		
		//Tbl TblView
		//DaoHelper
		
		//AutoApi
		
		//Definer
		
		//Api
	}
	
	protected static <T> void store(Class<T> clazz,String beanName,T bean) {
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
	
}
