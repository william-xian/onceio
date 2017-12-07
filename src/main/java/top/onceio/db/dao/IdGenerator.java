package top.onceio.db.dao;

public interface IdGenerator {
	Long next(Class<?> entityClass);
}
