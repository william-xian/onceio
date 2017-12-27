package top.onceio.beans;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	
	private final static Map<String,Object> nameToBean = new HashMap<>();
	
	public static void resovle(String... packages) {
		nameToBean.clear();
		scanner.scanPackages(packages);
		
		//Connection

		//JdbcHelper
		
		//Tbl TblView
		
		//DaoHelper
		
		//AutoApi
		
		//Definer
		
		//Api
	}
	
	public static <T> T load(Class<T> clazz) {
		return load(clazz,null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T load(Class<T> clazz, String beanName) {
		return (T)nameToBean.get(clazz.getName()+":" + beanName);
	}
	
}
