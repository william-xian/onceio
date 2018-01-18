package top.onceio.mvc;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.log4j.Logger;

import cn.xian.app.Launcher;

public class Webapp {

	private static final Logger LOGGER = Logger.getLogger(Launcher.class);
	
	private static String webappDirLocation = "/src/main/webapp";
    public static void run(Class<?> cnf,String[] args) {
    	Tomcat tomcat = new Tomcat();
        String webPort = System.getenv("PORT");  
        if(webPort == null || webPort.isEmpty()) {  
            webPort = "1230";
        }
        String rootDir = System.getProperty("user.dir");
        tomcat.setPort(Integer.valueOf(webPort));
		try {
			Context ctx = tomcat.addWebapp("/", rootDir+webappDirLocation);
			ctx.addWelcomeFile("/index.html");
	        WebResourceRoot resources = new StandardRoot(ctx);
	        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",rootDir, "/target/classes"));
	        ctx.setResources(resources);
	        ctx.addLifecycleListener(new LifecycleListener() {
				@Override
				public void lifecycleEvent(LifecycleEvent event) {
					if(event.getLifecycle().getState() == LifecycleState.INITIALIZED) {
					}else if(event.getLifecycle().getState() == LifecycleState.STARTING){
						LOGGER.debug("started  " + event.getClass().getClassLoader());
					}
				}
	        	
	        });
	        tomcat.start();
	        
	        tomcat.getServer().await();
	        
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
    }
    

}