package cn.xian.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import top.onceio.mvc.Webapp;

public class Launcher {
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {

    	Webapp.run(Launcher.class, args);
	}
	
}
