package top.onceio.beans;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import top.onceio.annotation.I18nCfg;
import top.onceio.annotation.I18nCfgBrief;
import top.onceio.annotation.I18nMsg;
import top.onceio.db.dao.Cnd;
import top.onceio.db.dao.Dao;
import top.onceio.db.tbl.OI18n;
import top.onceio.exception.Failed;
import top.onceio.util.AnnotationScanner;
import top.onceio.util.OReflectUtil;
import top.onceio.util.OUtils;


public class StartupRunner {
	private static final Logger LOGGER = Logger.getLogger(StartupRunner.class);
    //@Autowired
    private Dao<OI18n> dao;
    //@Value("${cn.dls.packages}")
    private String packages;
    
    private final static AnnotationScanner annotations = new AnnotationScanner(OI18n.class,I18nMsg.class,I18nCfg.class);
 

    private void annlysisI18nMsg(){
    	Set<Class<?>> classes = annotations.getClasses(I18nMsg.class);
    	if(classes == null) return;
    	List<OI18n> i18ns = new ArrayList<>();
    	for(Class<?>clazz:classes){
    		I18nMsg group = clazz.getAnnotation(I18nMsg.class);
    		for(Field field:clazz.getFields()){
    			field.setAccessible(true);
    			try {
					String name = field.get(null).toString();
					String key ="msg/"+group.value()+"_"+OUtils.encodeMD5(name);
		        	Cnd<OI18n> cnd = new Cnd<>(OI18n.class);
		        	cnd.eq().setKey(key);
		        	OI18n i18n = dao.fetch(null, cnd);
					if(i18n == null) {
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
		dao.insert(i18ns);
    }

    private void annlysisConst(){
    	Set<Class<?>> classes = annotations.getClasses(I18nCfg.class);
    	if(classes == null) return;
    	List<OI18n> i18ns = new ArrayList<>();
    	for(Class<?>clazz:classes){
    		I18nCfg group = clazz.getAnnotation(I18nCfg.class);
    		for(Field field:clazz.getFields()){
    			field.setAccessible(true);
    			I18nCfgBrief cons = field.getAnnotation(I18nCfgBrief.class);
    			try {
					String fieldname = field.getName();
					String val = field.get(null).toString();
					String key = "const/" + group.value()+ "_"+ clazz.getSimpleName() + "_" + fieldname;
					String name = cons.value();
		        	Cnd<OI18n> cnd = new Cnd<>(OI18n.class);
		        	cnd.eq().setKey(key);
		        	OI18n i18n = dao.fetch(null, cnd);
					
					if(i18n == null) {
						i18n = new OI18n();
						i18n.setKey(key);
						i18n.setName(name);
						i18n.setVal(val);
						LOGGER.debug("add: " + i18n);
			        	i18ns.add(i18n);
					}else {
						/** The val depend on database */
						if(!val.equals(i18n.getVal())){
							i18n.setVal(val);
							field.set(null, OReflectUtil.strToBaseType(field.getType(), val));
							LOGGER.debug("reload: " + i18n);
						}
						if(!i18n.getName().equals(name) ){
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
		dao.insert(i18ns);
    }
    
    public void run(String... args) throws Exception {
    	LOGGER.info("dls framework runner " + OUtils.toJSON(args));
        if(packages != null && !packages.trim().equals("")){
        	annotations.scanPackages(packages.split(","));
        }
        annlysisI18nMsg();
        annlysisConst();
    }
}
