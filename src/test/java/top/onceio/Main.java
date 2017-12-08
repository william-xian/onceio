package top.onceio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;

public class Main {
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {
		Properties prop = new Properties();
		prop.load(new FileInputStream("src/main/resources/application.properties"));
		String driver = prop.getProperty("spring.datasource.driver");
		String url = prop.getProperty("spring.datasource.url");
		String username =prop.getProperty("spring.datasource.username");
		String password = prop.getProperty("spring.datasource.password");
		String maxActive = prop.getProperty("spring.datasource.maxActive");
		DruidDataSource ds = new DruidDataSource();
		ds.setDriverClassName(driver);
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setMaxActive(Integer.parseInt(maxActive));
		
		Connection conn = ds.getConnection();
		String sql = "select * from otablemeta where name like ?";
		PreparedStatement stat = conn.prepareStatement(sql,ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
		stat.setObject(1, "Goods%");
		ResultSet rs = stat.executeQuery();
		while(rs.next()) {
			ResultSetMetaData rsmd = rs.getMetaData();
			for(int  i = 1; i < rsmd.getColumnCount();i++){
				System.out.println(String.format("%s : %s", rsmd.getColumnName(i),rs.getObject(i)));
			}
		}
		rs.close();
		stat.close();
		conn.close();
	}
	
}
