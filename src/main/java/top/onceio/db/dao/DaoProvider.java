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
	public int batchInsert(List<T> entities) {
		return daoHelper.batchInsert(entities);
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
	public int removeById(Long id) {
		return daoHelper.removeById(tbl, id);
	}

	@Override
	public int removeByIds(List<Long> ids) {
		return daoHelper.removeByIds(tbl, ids);
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
	public int deleteById(Long id) {
		return daoHelper.deleteById(tbl, id);
	}

	@Override
	public int deleteByIds(List<Long> ids) {
		return daoHelper.deleteByIds(tbl, ids);
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
