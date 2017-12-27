package top.onceio.db.dao;

import java.util.List;
import java.util.function.Consumer;

import top.onceio.db.dao.impl.DaoHelper;
import top.onceio.db.dao.tpl.SelectTpl;
import top.onceio.db.dao.tpl.UpdateTpl;
import top.onceio.db.tbl.OEntity;
import top.onceio.mvc.annocations.Using;

public abstract class DaoProvider<T extends OEntity<ID>,ID> implements Dao<T,ID> {
	
	@Using
	protected DaoHelper<ID> daoHelper;
	Class<T> tbl;
	
	public DaoProvider() {
	}
	
	@Override
	public T get(ID id) {
		daoHelper.get(tbl, id);
		return daoHelper.get(tbl, id);
	}

	@Override
	public T insert(T entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int insert(List<T> entities) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(T entity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateIgnoreNull(T entity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateByTpl(UpdateTpl<T> tmpl) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateByTplCnd(UpdateTpl<T> tmpl, Cnd<T> cnd) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int remove(ID id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int remove(List<ID> ids) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int remove(Cnd<T> cnd) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int recovery(Cnd<T> cnd) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(ID id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(List<ID> ids) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Cnd<T> cnd) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public T fetch(SelectTpl<T> tpl, Cnd<T> cnd) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<T> findByIds(List<ID> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<T> find(Cnd<T> cnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<T> findTpl(SelectTpl<T> tpl, Cnd<T> cnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void download(SelectTpl<T> tpl, Cnd<T> cnd, Consumer<T> consumer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long count() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long count(Cnd<T> cnd) {
		// TODO Auto-generated method stub
		return 0;
	}

}
