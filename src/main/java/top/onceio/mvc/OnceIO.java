package top.onceio.mvc;

import java.io.File;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;


public class OnceIO {
	
	private static String webappDirLocation = "src/main/webapp";
  
    public static void run(Class<?> cnf,String[] args) {
    	Tomcat tomcat = new Tomcat();
        String webPort = System.getenv("PORT");  
        if(webPort == null || webPort.isEmpty()) {  
            webPort = "8888";
        }
        tomcat.setPort(Integer.valueOf(webPort));
		try {
			Context ctx = tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
			ctx.addWelcomeFile("/index.html");
	        File additionWebInfClasses = new File("target/classes");
	        WebResourceRoot resources = new StandardRoot(ctx);
	        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
	                additionWebInfClasses.getAbsolutePath(), "/"));
	        ctx.setResources(resources);
	        tomcat.start();
	        tomcat.getServer().await();
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
    }
}