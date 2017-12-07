package top.onceio.db.tbl;

import top.onceio.annotation.I18nCfg;
import top.onceio.db.annotation.Col;
import top.onceio.db.annotation.Tbl;
import top.onceio.util.OUtils;

@Tbl
public class OI18n extends OEntity<String>{
    @Col(size=64,nullable = false)
	private String id;
	@Col(size=255,nullable=false)
	private String name;
	@Col(size=32,nullable=true)
	private String val;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVal() {
		return val;
	}
	public void setVal(String val) {
		this.val = val;
	}
	public String toString() {
		return OUtils.toJSON(this);
	}
	
	public static String msgId(String lang,String format) {
		return "msg/"+lang+"_"+OUtils.encodeMD5(format);		
	}
	
	public static String constId(String lang,Class<?> clazz,String fieldName) {
		I18nCfg group = clazz.getAnnotation(I18nCfg.class);
		return "const/" + group.value()+ "_"+ clazz.getSimpleName() + "_" + fieldName;
	}
}
