package top.onceio.db.dao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import top.onceio.db.dao.impl.DaoHelper;
import top.onceio.db.dao.tpl.SelectTpl;
import top.onceio.db.dao.tpl.UpdateTpl;
import top.onceio.db.tbl.OEntity;
import top.onceio.mvc.annocations.Using;

public abstract class DaoProvider<T extends OEntity> implements Dao<T> {
	@Using
	protected DaoHelper daoHelper;
	
	private Class<T> tbl;
	
	@SuppressWarnings("unchecked")
	public DaoProvider() {
		Type type = this.getClass().getGenericSuperclass();
		if(type instanceof ParameterizedType) {
			Type[] tps = ((ParameterizedType)type).getActualTypeArguments();
			tbl = (Class<T>) tps[0];
		}
	}
	
	@Override
	public T get(Long id) {
		daoHelper.get(tbl, id);
		return daoHelper.get(tbl, id);
	}

	@Override
	public T insert(T entity) {
		return daoHelper.insert(entity);
	}

	@Override
	public int insert(List<T> entities) {
		return daoHelper.insert(entities);
	}

	@Override
	public int update(T entity) {
		return daoHelper.update(entity);
	}

	@Override
	public int updateIgnoreNull(T entity) {
		return daoHelper.updateIgnoreNull(entity);
	}

	@Override
	public int updateByTpl(UpdateTpl<T> tpl) {
		return daoHelper.updateByTpl(tbl,tpl);
	}

	@Override
	public int updateByTplCnd(UpdateTpl<T> tpl, Cnd<T> cnd) {
		return daoHelper.updateByTplCnd(tbl,tpl,cnd);
	}

	@Override
	public int remove(Long id) {
		return daoHelper.remove(tbl, id);
	}

	@Override
	public int remove(List<Long> ids) {
		return daoHelper.remove(tbl, ids);
	}

	@Override
	public int remove(Cnd<T> cnd) {
		return daoHelper.remove(tbl, cnd);
	}

	@Override
	public int recovery(Cnd<T> cnd) {
		return daoHelper.recovery(tbl,cnd);
	}

	@Override
	public int delete(Long id) {
		return daoHelper.delete(tbl, id);
	}

	@Override
	public int delete(List<Long> ids) {
		return daoHelper.delete(tbl, ids);
	}

	@Override
	public int delete(Cnd<T> cnd) {
		return daoHelper.delete(tbl, cnd);
	}
	@Override
	public T fetch(SelectTpl<T> tpl, Cnd<T> cnd) {
		return daoHelper.fetch(tbl,tpl, cnd);
	}
	
	@Override
	public List<T> findByIds(List<Long> ids) {
		return daoHelper.findByIds(tbl, ids);
	}

	@Override
	public Page<T> find(Cnd<T> cnd) {
		return daoHelper.find(tbl, cnd);
	}

	@Override
	public Page<T> findTpl(SelectTpl<T> tpl, Cnd<T> cnd) {
		return daoHelper.findByTpl(tbl, tpl, cnd);
	}

	@Override
	public void download(SelectTpl<T> tpl, Cnd<T> cnd, Consumer<T> consumer) {
		daoHelper.download(tbl, tpl, cnd, consumer);
	}

	@Override
	public long count() {
		return daoHelper.count(tbl);
	}

	@Override
	public long count(Cnd<T> cnd) {
		return daoHelper.count(tbl,cnd);
	}

}
