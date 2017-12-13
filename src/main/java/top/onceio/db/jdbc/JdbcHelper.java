package top.onceio.db.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.sql.DataSource;

import top.onceio.exception.Failed;
import top.onceio.util.OLog;

public class JdbcHelper {
	
	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public ResultSet call(String sql,Object[] args) {
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		if(dataSource != null) {
			try {
				conn = dataSource.getConnection();
			} catch (SQLException e) {
				Failed.throwError(e.getMessage());
			}
			try {
				stat = conn.prepareCall(sql, ResultSet.FETCH_UNKNOWN, ResultSet.CONCUR_UPDATABLE);
				if(args != null) {
					for(int i = 0; i < args.length; i++ ){
						stat.setObject(i+1, args[i]);
					}
				}
				rs = stat.executeQuery();	
			} catch (SQLException e) {
				Failed.throwMsg(e.getMessage());
			}finally {
				if(stat != null) {
					try {
						stat.close();
					} catch (SQLException e) {
						Failed.throwMsg(e.getMessage());
					}
				}
				if(conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						Failed.throwMsg(e.getMessage());
					}
				}
			}
		}
		return rs;
	}
	
	private int[] batchExec(String sql,List<Object[]> args) {
		Connection conn = null;
		PreparedStatement stat = null;
		int[] result = null;
		if(dataSource != null) {
			try {
				conn = dataSource.getConnection();
				//conn.setAutoCommit(false);
			} catch (SQLException e) {
				Failed.throwError(e.getMessage());
			}
			try {
				stat = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				for (Object[] arr:args) {
					for (int i = 0; i < arr.length; i++) {
						stat.setObject(i+1, arr[i]);
					}
					stat.addBatch();
				}
				stat.setMaxRows(args.size());
				result = stat.executeBatch();
				//conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
				Failed.throwMsg(e.getMessage());
			}finally {
				if(stat != null) {
					try {
						stat.close();
					} catch (SQLException e) {
						Failed.throwMsg(e.getMessage());
					}
				}		
				if(conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						Failed.throwMsg(e.getMessage());
					}
				}
			}
		}
		return result;
	}
	
	public void query(String sql,Object[] args,Consumer<ResultSet> consumer) {
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		if(dataSource != null) {
			try {
				conn = dataSource.getConnection();
			} catch (SQLException e) {
				Failed.throwError(e.getMessage());
			}
			try {
				stat = conn.prepareStatement(sql, ResultSet.FETCH_UNKNOWN, ResultSet.CONCUR_READ_ONLY);
				if(args != null) {
					for(int i = 0; i < args.length; i++ ){
						stat.setObject(i+1, args[i]);
					}
				}
				rs = stat.executeQuery();
				while(rs.next()) {
					consumer.accept(rs);
				}
			} catch (SQLException e) {
				Failed.throwMsg(e.getMessage());
			}finally {
				
				if(stat != null) {
					try {
						stat.close();
					} catch (SQLException e) {
						Failed.throwMsg(e.getMessage());
					}
				}
				
				if(conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						Failed.throwMsg(e.getMessage());
					}
				}
			}
		}
	}
	
 	public <T> T queryForObject(String sql, Class<T> clazz) {
		return queryForObject(sql,null,clazz);
	}	
	public <T> T queryForObject(String sql, Object[] args, Class<T> clazz) {
		final List<T> result  = new LinkedList<>();
		
		query(sql,args,new Consumer<ResultSet>(){
			@Override
			public void accept(ResultSet rs) {
				try {
					result.add(rs.getObject(1, clazz));
				} catch (SQLException e) {
					Failed.throwMsg(e.getMessage());
				}
			}
		});
		if(result.size() > 1) {
			Failed.throwError("The count of result(%s) is more then 1.", result.size());
		}else if(result.size() == 1) {
			return result.get(0);
		}
		return null;
	}

	public int[] batchUpdate(String sql) {
		return batchUpdate(sql, null);
	}

	public int[] batchUpdate(String... sql) {
		StringBuilder sb = new StringBuilder();
		for(String s:sql) {
			if(s != null && s.isEmpty()) {
				sb.append(s);
				if(!s.endsWith(";")) {
					sb.append(';');
				}
			}
		}
		return batchUpdate(sb.toString());	
	}
	
	public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
		return batchExec(sql,batchArgs);
	}
	
	public int update(String sql, Object[] args) {
		int cnt = 0;
		Connection conn = null;
		PreparedStatement stat = null;
		if(dataSource != null) {
			try {
				conn = dataSource.getConnection();
				//conn.setAutoCommit(false);
			} catch (SQLException e) {
				e.printStackTrace();
				Failed.throwError(e.getMessage());
			}
			try {
				stat = conn.prepareStatement(sql, ResultSet.FETCH_UNKNOWN, ResultSet.CLOSE_CURSORS_AT_COMMIT);
				OLog.debug("%s ", sql);
				if(args != null) {
					for(int i = 0; i < args.length; i++ ){
						stat.setObject(i+1, args[i]);
					}
				}
				cnt = stat.executeUpdate();
				//conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
				Failed.throwMsg(e.getMessage());
			}finally {
				if(stat != null) {
					try {
						stat.close();
					} catch (SQLException e) {
						Failed.throwMsg(e.getMessage());
					}
				}
				if(conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						Failed.throwMsg(e.getMessage());
					}
				}
			}
		}
		return cnt;
	}

}
