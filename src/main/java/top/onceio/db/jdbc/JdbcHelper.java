package top.onceio.db.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

public class JdbcHelper {
	
	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	private ResultSet exec(String sql,Object[] args) {
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		if(dataSource != null) {
			try {
				conn = dataSource.getConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				stat = conn.prepareStatement(sql, ResultSet.FETCH_UNKNOWN, ResultSet.CONCUR_UPDATABLE);
				if(args != null) {
					for(int i = 0; i < args.length; i++ ){
						stat.setObject(i+1, args[i]);
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				rs = stat.executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.commit();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return rs;
	}
	
	private ResultSet query(String sql,Object[] args) {
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		if(dataSource != null) {
			try {
				conn = dataSource.getConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				stat = conn.prepareStatement(sql, ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
				if(args != null) {
					for(int i = 0; i < args.length; i++ ){
						stat.setObject(i+1, args[i]);
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				rs = stat.executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.commit();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return rs;
	}
	
 	public <T> T queryForObject(String sql, Class<T> clazz) {
		return queryForObject(sql,null,clazz);
	}	
	public <T> T queryForObject(String sql, Object[] args, Class<T> clazz) {
		try {
			ResultSet rs = query(sql,args);
			if(rs.next()) {
				return  rs.getObject(1, clazz);
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public <E> List<E> query(String sql, Object[] args, RowCallbackHandler rowCallbackHandler) {
		List<E> result = new LinkedList<>();
		try {
			ResultSet rs = query(sql,args);
			if(rs != null) {
				rowCallbackHandler.processRow(rs);	
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public <E> List<E> query(String sql, Object[] array, RowMapper<E> rowMapper) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int[] batchUpdate(String sql, BatchPreparedStatementSetter batchPreparedStatementSetter) {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] batchUpdate(String... sql) {
		// TODO Auto-generated method stub
		return null;	
	}
	
	public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int update(String sql, Object... args) {
		// TODO Auto-generated method stub
		return 0;
	}

}
