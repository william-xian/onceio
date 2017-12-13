package top.onceio.db.dao;

public interface IdGenerator<ID> {
	ID next(Class<?> entityClass);
}
