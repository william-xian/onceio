package top.onceio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import top.onceio.mvc.OnceIO;

public class Main {
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {

    	OnceIO.run(Main.class, args);
	}
	
}
