package top.onceio.db.tbl;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import top.onceio.db.annotation.Col;

/**
 * @author Administrator
 *
 */
public abstract class OEntity<ID> {
    @Col(nullable = false)
	protected ID id;
    @Col(colDef="boolean default false",nullable = false)
	protected transient Boolean rm;
	/** 用户存储额外数据，如 聚合函数 */
	protected Map<String,Object> extra;
	
	public OEntity() {
	}
	public void init() {
	}
	public void initId(){
	}
	public ID getId() {
		return id;
	}
	public void setId(ID id) {
		this.id = id;
	}
	@SuppressWarnings("unchecked")
	public void setOId(Object id) {
		this.id = (ID)id;
	}
	public Boolean getRm() {
		return rm;
	}
	public void setRm(Boolean rm) {
		this.rm = rm;
	}
	public Map<String,Object> put(String key,Object val) {
		if(extra == null) {
			extra = new HashMap<String,Object>();
		}
		extra.put(key, val);
		return extra;
	}
	public Map<String, Object> getExtra() {
		return extra;
	}
	public void setExtra(Map<String, Object> extra) {
		this.extra = extra;
	}

	private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    @Override
	public String toString(){
    	return GSON.toJson(this);
	}
}
