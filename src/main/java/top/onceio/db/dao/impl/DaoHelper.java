package top.onceio.db.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import top.onceio.OConfig;
import top.onceio.db.annotation.ConstraintType;
import top.onceio.db.dao.Cnd;
import top.onceio.db.dao.IdGenerator;
import top.onceio.db.dao.Page;
import top.onceio.db.dao.tpl.SelectTpl;
import top.onceio.db.dao.tpl.UpdateTpl;
import top.onceio.db.meta.ColumnMeta;
import top.onceio.db.meta.ConstraintMeta;
import top.onceio.db.meta.TableMeta;
import top.onceio.db.tbl.OEntity;
import top.onceio.db.tbl.OTableMeta;
import top.onceio.exception.VolidateFailed;
import top.onceio.util.OAssert;
import top.onceio.util.OLog;
import top.onceio.util.OUtils;

public class DaoHelper {
	private JdbcTemplate jdbcTemplate;
	private Map<String,TableMeta> tableToTableMeta;
	private IdGenerator idGenerator;
	@SuppressWarnings("rawtypes")
	private Map<String,RowMapper> tableToRowMapper = new HashMap<>();
	private List<Class<? extends OEntity<?>>> entities;
	public DaoHelper(){
	}
	
	public DaoHelper(JdbcTemplate jdbcTemplate, IdGenerator idGenerator,List<Class<? extends OEntity<?>>> entitys) {
		super();
		init(jdbcTemplate,idGenerator,entitys);
	}
	
	public boolean exist(Class<?> tbl) {
		Integer cnt = jdbcTemplate.queryForObject(String.format("select count(*) from pg_class where relname = '%s'", tbl.getSimpleName().toLowerCase()), Integer.class);
		if(cnt != null && cnt > 0) {
			return true;
		}else {
			return false;
		}
	}
	/** TODO 依赖关系排序  */
	public void init(JdbcTemplate jdbcTemplate,IdGenerator idGenerator,List<Class<? extends OEntity<?>>> entities) {
		this.jdbcTemplate = jdbcTemplate;
		this.idGenerator = idGenerator;
		this.tableToTableMeta = new HashMap<>();
		if(!exist(OTableMeta.class)) {
			List<String> sqls = this.createOrUpdate(OTableMeta.class);
			if(sqls != null && !sqls.isEmpty()) {
				jdbcTemplate.batchUpdate(sqls.toArray(new String[0]));	
			}
		}
		TableMeta tm = TableMeta.createBy(OTableMeta.class);
		tableToTableMeta.put(tm.getTable(), tm);
		Cnd<OTableMeta> cnd = new Cnd<>(OTableMeta.class);
		cnd.setPage(1);
		cnd.setPageSize(Integer.MAX_VALUE);
		Page<OTableMeta> page = this.find(OTableMeta.class, cnd);
		for(OTableMeta meta:page.getData()) {
			if(meta.getName().equals(OTableMeta.class.getSimpleName())){
				continue;
			}
			TableMeta old = OUtils.createFromJson(meta.getVal(), TableMeta.class);
			old.getFieldConstraint();
			old.freshConstraintMetaTable();
			old.freshNameToField();
			tableToTableMeta.put(old.getTable(), old);
		}
		if(entities != null) {
			this.entities = entities;
			Map<String,List<String>> tblSqls = new HashMap<>();
			for(Class<? extends OEntity<?>> tbl:entities) {
				List<String> sqls = this.createOrUpdate(tbl);
				if(sqls != null && !sqls.isEmpty()) {
					tblSqls.put(tbl.getSimpleName(), sqls);
					Cnd<OTableMeta> cndMeta = new Cnd<>(OTableMeta.class);
					cndMeta.eq().setName(tbl.getSimpleName());
					TableMeta tblMeta = TableMeta.createBy(tbl);
					OTableMeta ootm = this.fetch(OTableMeta.class, null, cndMeta);
					save(ootm, tblMeta.getTable(), OUtils.toJSON(tblMeta));
				}
			}
			List<String> order = new ArrayList<>();
			for(String tbl:tblSqls.keySet()) {
				sorted(tbl,order);
			}
			
			List<String> sqls = new ArrayList<>();
			for(String tbl:order) {
				sqls.addAll(tblSqls.get(tbl));
			}
			if(!sqls.isEmpty()) {
				jdbcTemplate.batchUpdate(sqls.toArray(new String[0]));	
			}
		}
		
	}
	private void sorted(String tbl,List<String> order) {
		if(!order.contains(tbl)) {
			TableMeta tblMeta = tableToTableMeta.get(tbl);
			if(tblMeta != null) {
				for(ConstraintMeta cm:tblMeta.getFieldConstraint()) {
					if(cm.getType().equals(ConstraintType.FOREGIN_KEY)) {
						sorted(cm.getRefTable(),order);
					}
				}
			}
			order.add(tbl);
		}
	}

	public List<Class<? extends OEntity<?>>> getEntities() {
		return entities;
	}

	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public Map<String, TableMeta> getTableToTableMata() {
		return tableToTableMeta;
	}

	public void setTableToTableMata(Map<String, TableMeta> tableToTableMeta) {
		this.tableToTableMeta = tableToTableMeta;
	}

	@SuppressWarnings("rawtypes")
	public Map<String, RowMapper> getTableToRowMapper() {
		return tableToRowMapper;
	}

	@SuppressWarnings("rawtypes")
	public void setTableToRowMapper(Map<String, RowMapper> tableToRowMapper) {
		this.tableToRowMapper = tableToRowMapper;
	}

	private void save(OTableMeta ootm,String name,String val) {
		if(ootm == null) {
			ootm = new OTableMeta();
			ootm.setId(idGenerator.next(OTableMeta.class));
			ootm.setName(name);
			ootm.setVal(val);
			ootm.setCreatetime(System.currentTimeMillis());
			insert(ootm);
		}else {
			ootm.setVal(val);
			update(ootm);
		}
	}
	
	public <E extends OEntity<?>> List<String> createOrUpdate(Class<E> tbl) {
		TableMeta old = tableToTableMeta.get(tbl.getSimpleName());
		if(old == null) {
			old = TableMeta.createBy(tbl);
			List<String> sqls = old.createTableSql();
			tableToTableMeta.put(old.getTable(), old);
			return sqls;
		}else {
			TableMeta tm = TableMeta.createBy(tbl);
			if(old.equals(tm)){
			} else {
				List<String> sqls = old.upgradeTo(tm);
				tableToTableMeta.put(tm.getTable(), tm);
				return sqls;
			}
		}
		return null;
	}

	public <E extends OEntity<?>> boolean drop(Class<E> tbl) {
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		if(tm == null) {
			return false;
		}
		String sql = String.format("DROP TABLE IF EXISTS %s;", tbl.getSimpleName());
		jdbcTemplate.batchUpdate(sql);
		return true;
	}

	public int[] batchUpdate(final String... sql) throws DataAccessException {
		return jdbcTemplate.batchUpdate(sql);
	}
	
	public int[] batchUpdate(final String sql,List<Object[]> batchArgs) throws DataAccessException {
		return jdbcTemplate.batchUpdate(sql, batchArgs);
	}
	
	private static <E extends OEntity<?>> RowMapper<E> genRowMapper(Class<E> tbl,TableMeta tm) {
		RowMapper<E> rowMapper = new RowMapper<E>(){
			@Override
			public E mapRow(ResultSet rs, int rowNum) throws SQLException {
				E row = createBy(tbl,tm,rs);
				return row;
			}
		};
		return rowMapper;
	}
	public static <E extends OEntity<?>> E createBy(Class<E> tbl,TableMeta tm,ResultSet rs) throws SQLException {
		E row = null;
		try {
			row = tbl.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			OAssert.warnning("%s InstantiationException", tbl);
		}
		if(row != null) {
			ResultSetMetaData rsmd = rs.getMetaData();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String colName = rsmd.getColumnName(i);
				ColumnMeta cm = tm.getColumnMetaByName(colName);
				if (cm != null) {
					try {
						Object val = rs.getObject(colName, cm.getJavaBaseType());
						cm.getField().set(row, val);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				} else {
					row.put(colName, rs.getObject(i));
				}
			}
			return row;
		}
		return row;
	}
	
	public <E extends OEntity<ID>,ID> E get(Class<E> tbl,ID id) {
		Cnd<E> cnd = new Cnd<E>(tbl);
		cnd.setPage(1);
		cnd.setPageSize(1);
		cnd.eq().setId(id);
		Page<E> page = find(tbl,null,cnd);
		if(page.getData().size() == 1) {
			return page.getData().get(0);
		}
		return null;
	}
	
	public <E extends OEntity<?>> E insert(E entity) {
		OAssert.warnning(entity != null,"不可以插入null");
		Class<?> tbl = entity.getClass();
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());	
		OAssert.fatal(tm != null,"无法找到表：%s",tbl.getSimpleName());
		validate(tm,entity,false);
		TblIdNameVal<E> idNameVal = new TblIdNameVal<>(tm.getColumnMetas(),Arrays.asList(entity));
		if(idNameVal.getIdAt(0) == null) {
			Object id = idGenerator.next(tbl);
			idNameVal.setIdAt(0, id);
		}
		idNameVal.dropAllNullColumns();
		List<Object> vals = idNameVal.getIdValsList().get(0);
		List<String> names = idNameVal.getIdNames();
		String stub = OUtils.genStub("?",",",names.size());
		String sql = String.format("INSERT INTO %s(%s) VALUES(%s);", tm.getTable(),String.join(",", names),stub);
		jdbcTemplate.update(sql, vals.toArray());
		return entity;
	}
	private void validate(TableMeta tm,Object obj,boolean ignoreNull) {
		for(ColumnMeta cm:tm.getColumnMetas()) {
			if(cm.getName().equals("id") || cm.getName().equals("rm")) {
				continue;
			}
			Object val = null;
			try {
				val = cm.getField().get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				OLog.info(e.getMessage());
			}
			if(!cm.isNullable() && val == null && !ignoreNull) {
				VolidateFailed vf = VolidateFailed.createError("%s cannot be null", cm.getName());
				vf.put(cm.getName(), "cannot be null");
				vf.throwSelf();
			} else if(val != null) {
				if(!cm.getPattern().equals("")) {
					if(val.toString().matches(cm.getPattern())) {
						VolidateFailed vf = VolidateFailed.createError("%s does not matches %s", cm.getName(),cm.getPattern());
						vf.put(cm.getName(), cm.getPattern());
						vf.throwSelf();
					}
				}
			}	
		}
		
	}
	public <E extends OEntity<?>> int insert(List<E> entities) {
		OAssert.warnning(entities != null && !entities.isEmpty(),"不可以插入null");
		Class<?> tbl = entities.get(0).getClass();
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		OAssert.fatal(tm != null,"无法找到表：%s",tbl.getSimpleName());
		
		for(E entity:entities) {
			validate(tm,entity,false);
		}
		
		TblIdNameVal<E> idNameVal = new TblIdNameVal<>(tm.getColumnMetas(),entities);
		for(int i = 0; i < idNameVal.ids.size(); i++) {
			if(idNameVal.getIdAt(i) == null) {
				Object id = idGenerator.next(tbl);
				idNameVal.setIdAt(i, id);
			}
		}

		idNameVal.dropAllNullColumns();
		List<String> names = idNameVal.getIdNames();
		List<List<Object>> valsList = idNameVal.getIdValsList();
		String stub = OUtils.genStub("?",",",names.size());
		String sql = String.format("INSERT INTO %s(%s) VALUES(%s);", tm.getTable(),String.join(",", names),stub);
		
		OLog.debug("%s\n",sql);
		
		int[] cnts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				List<Object> vals = valsList.get(i);
				for(int vi = 0;  vi < vals.size(); vi++) {
					ps.setObject(vi+1, vals.get(vi));
				}
				OLog.debug("%s values:%s \n",sql, OUtils.toJSON(vals));
			}
			@Override
			public int getBatchSize() {
				return entities.size();
			}
			
		});
		int cnt = 0;
		for(int c:cnts) {
			cnt += c;
		}
		return cnt;
	}

	private <E extends OEntity<?>> int update(E entity,boolean ignoreNull) {
		OAssert.warnning(entity != null,"不可以插入null");
		Class<?> tbl = entity.getClass();
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		validate(tm,entity,ignoreNull);
		OAssert.fatal(tm != null,"无法找到表：%s",tbl.getSimpleName());
		TblIdNameVal<E> idNameVal = new TblIdNameVal<>(tm.getColumnMetas(),Arrays.asList(entity));
		Object id = idNameVal.getIdAt(0);
		OAssert.err(id != null,"ID 不能为NULL");
		/** ignore rm */
		idNameVal.dropColumns("rm");
		if(ignoreNull) {
			idNameVal.dropAllNullColumns();
		}
		List<String> names = idNameVal.getNames();
		List<Object> vals = idNameVal.getValsList().get(0);
		String sql = String.format("UPDATE %s SET %s=? WHERE id=? and rm = false;", tm.getTable(),String.join("=?,", names));
		vals.add(id);
		return jdbcTemplate.update(sql, vals.toArray());
	}


	public <E extends OEntity<?>> int update(E entity) {
		return update(entity,false);
	}
	
	public <E extends OEntity<?>> int updateIgnoreNull(E entity) {
		return update(entity,true);	
	}
	
	
	public <E extends OEntity<?>> int updateByTpl(Class<E> tbl, UpdateTpl<E> tpl) {
		OAssert.warnning(tpl.getId() != null && tpl != null,"Are you sure to update a null value?");
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());	
		OAssert.fatal(tm != null,"无法找到表：%s",tbl.getSimpleName());
		//validate(tm,tpl,false);
		String setTpl = tpl.getSetTpl();
		List<Object> vals = new ArrayList<>(tpl.getArgs().size()+1);
		vals.addAll(tpl.getArgs());
		vals.add(tpl.getId());
		String sql = String.format("UPDATE %s SET %s WHERE id=? and rm=false;", tm.getTable(),setTpl);
		return jdbcTemplate.update(sql, vals.toArray());
	}
	
	public <E extends OEntity<?>> int updateByTplCnd(Class<E> tbl,UpdateTpl<E> tpl,Cnd<E> cnd) {
		OAssert.warnning(tpl != null,"Are you sure to update a null value?");
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());	
		OAssert.fatal(tm != null,"无法找到表：%s",tbl.getSimpleName());
		//validate(tm,tpl,false);
		List<Object> vals = new ArrayList<>();
		vals.addAll(tpl.getArgs());
		List<Object> sqlArgs = new ArrayList<>();
		String cndSql = cnd.whereSql(sqlArgs);
		if(cndSql.isEmpty()) {
			OAssert.warnning("查询条件不能为空");
		}
		vals.addAll(sqlArgs);
		String sql = String.format("UPDATE %s SET %s WHERE (%s) and rm=false;", tm.getTable(),tpl.getSetTpl(),cndSql);
		return jdbcTemplate.update(sql, vals.toArray());
	}

	public <E,ID> int remove(Class<E> tbl,ID id) {
		if(id == null) return 0;
		OAssert.warnning(id != null,"ID不能为null");
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		OAssert.fatal(tm != null,"无法找到表：%s",tbl.getSimpleName());
		String sql = String.format("UPDATE %s SET rm=true WHERE id=?;", tm.getTable());
		return jdbcTemplate.update(sql, id);
	}

	public <E,ID> int remove(Class<E> tbl, List<ID> ids) {
		if(ids == null || ids.isEmpty()) return 0;
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		String stub = OUtils.genStub("?",",",ids.size());
		String sql = String.format("UPDATE %s SET rm=true WHERE id in (%s);", tm.getTable(),stub);
		return jdbcTemplate.update(sql, ids.toArray());
	}
	public <E extends OEntity<?>> int remove(Class<E> tbl, Cnd<E> cnd) {
		if(cnd == null) return 0;
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		List<Object> sqlArgs = new ArrayList<>();
		String whereCnd = cnd.whereSql(sqlArgs);
		if(whereCnd.equals("")) {
			return 0;
		}
		String sql = String.format("UPDATE %s SET rm=true WHERE (%s);", tm.getTable(),whereCnd);
		return jdbcTemplate.update(sql, sqlArgs.toArray());
	}
	public <E,ID> int delete(Class<E> tbl, ID id) {
		if(id == null) return 0;
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		String sql = String.format("DELETE FROM %s WHERE id=? and rm = true;", tm.getTable());
		return jdbcTemplate.update(sql, id);
	}
	public <E,ID> int delete(Class<E> tbl, List<ID> ids) {
		if(ids == null || ids.isEmpty()) return 0;
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		String stub = OUtils.genStub("?", ",", ids.size());
		String sql = String.format("DELETE FROM %s WHERE id in (%s) and (rm = true);", tm.getTable(),stub);
		return jdbcTemplate.update(sql, ids.toArray());
	}
	
	public <E extends OEntity<?>> int delete(Class<E> tbl, Cnd<E> cnd) {
		if (cnd == null) return 0;
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		List<Object> sqlArgs = new ArrayList<>();
		String whereCnd = cnd.whereSql(sqlArgs);
		if (whereCnd.equals("")) {
			return 0;
		}
		String sql = String.format("DELETE FROM %s WHERE (rm = true) and (%s);", tm.getTable(), whereCnd);
		return jdbcTemplate.update(sql, sqlArgs.toArray());
	}


	public <E extends OEntity<?>> long count(Class<E> tbl) {
		return count(tbl,null,new Cnd<E>(tbl));
	}
	
	public <E extends OEntity<?>> long count(Class<E> tbl, Cnd<E> cnd) {
		return count(tbl,null,cnd);
	}
	
	public <E extends OEntity<?>> long count(Class<E> tbl, SelectTpl<E> tpl, Cnd<E> cnd) {
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		OAssert.fatal(tm != null,"无法找到表：%s",tbl.getSimpleName());
		List<Object> sqlArgs = new ArrayList<>();
		String sql = cnd.countSql(tm, tpl, sqlArgs);
		OLog.debug(sql);
		OLog.debug(sqlArgs.toString());
		return jdbcTemplate.queryForObject(sql,sqlArgs.toArray(new Object[0]), Long.class);
	}

	public <E extends OEntity<?>> Page<E> find(Class<E> tbl,Cnd<E> cnd) {
		return find(tbl,null,cnd);
	}
	@SuppressWarnings("unchecked")
	public <E extends OEntity<?>> Page<E> find(Class<E> tbl,SelectTpl<E> tpl,Cnd<E> cnd) {
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		OAssert.fatal(tm != null,"无法找到表：%s",tbl.getSimpleName());
		RowMapper<E> rowMapper = null;
		String mapperKey = tbl.getSimpleName();
		if(!tableToRowMapper.containsKey(mapperKey)) {
			rowMapper = genRowMapper(tbl,tm);
			tableToRowMapper.put(mapperKey, rowMapper);
		}else {
			rowMapper = tableToRowMapper.get(mapperKey);	
		}
		Page<E> page = new Page<E>();
		if(cnd.getPage() == null || cnd.getPage() <= 0) {
			page.setPage(cnd.getPage());
			if(cnd.getPage() == null || cnd.getPage() == 0) {
				cnd.setPage(1);
				page.setPage(1);
			}else {
				cnd.setPage(Math.abs(cnd.getPage()));
			}
			page.setTotal(count(tbl,tpl,cnd));
		}
		if(cnd.getPageSize() == null) {
			cnd.setPageSize(OConfig.PAGE_SIZE_DEFAULT);
			page.setPageSize(OConfig.PAGE_SIZE_DEFAULT);
		}else if(cnd.getPageSize() > OConfig.PAGE_SIZE_MAX) {
			cnd.setPageSize(OConfig.PAGE_SIZE_MAX);
			page.setPageSize(OConfig.PAGE_SIZE_MAX);
		}
		if(page.getTotal() == null || page.getTotal() > 0) {
			List<Object> sqlArgs = new ArrayList<>();
			String sql = cnd.pageSql(tm,tpl,sqlArgs);
			OLog.debug(sql);
			List<E> data = jdbcTemplate.query(sql,sqlArgs.toArray(), rowMapper);
			page.setData(data);
		}
		return page;
	}

	public <E extends OEntity<?>> E fetch(Class<E> tbl,SelectTpl<E> tpl,Cnd<E> cnd) {
		if(cnd == null) {
			cnd = new Cnd<E>(tbl);
		}
		cnd.setPage(1);
		cnd.setPageSize(1);
		Page<E> page = find(tbl,tpl,cnd);
		if(page.getData().size() > 0) {
			return page.getData().get(0);
		}
		return null;
	}
	
	public <E extends OEntity<?>> void download(Class<E> tbl,SelectTpl<E> tpl,Cnd<E> cnd, Consumer<E> consumer) {
		TableMeta tm = tableToTableMeta.get(tbl.getSimpleName());
		if(tm == null) {
			return ;
		}
		List<Object> args = new ArrayList<>();
		StringBuffer sql = cnd.wholeSql(tm, tpl, args);
		jdbcTemplate.query(sql.toString(), args.toArray(new Object[0]), new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				E row = createBy(tbl, tm,rs);
				consumer.accept(row);
			}
		});
	}
	
	public <E extends OEntity<ID>,ID> List<E> findByIds(Class<E> tbl, List<ID> ids) {
		if(ids == null || ids.isEmpty()) {
			return new ArrayList<E>();
		}
		Cnd<E> cnd = new Cnd<E>(tbl);
		cnd.setPage(1);
		cnd.setPageSize(ids.size());
		cnd.in(ids.toArray(new Object[0])).setId(null);
		Page<E> page = find(tbl,null,cnd);
		return page.getData();
	}
}

