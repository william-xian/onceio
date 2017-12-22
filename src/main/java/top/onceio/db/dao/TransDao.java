package top.onceio.db.dao;

import java.sql.Savepoint;

public interface TransDao{
	void beginTransaction(int level);
	Savepoint setSavepoint();
	void rollback();
	void rollback(Savepoint sp);
	void commit();
}
