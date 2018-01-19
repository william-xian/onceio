package top.onceio.mvc;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;

import cn.xian.app.Launcher;

public class Webapp {

	private static final Logger LOGGER = Logger.getLogger(Launcher.class);
	
    public static void run(Class<?> cnf,String[] args) {
        String webPort = System.getenv("PORT");  
        if(webPort == null || webPort.isEmpty()) {  
            webPort = "1230";
        }
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(Integer.valueOf(webPort));
        server.setConnectors(new Connector[] { connector });
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(OIOServlet.class, "/").setInitParameter("launcher", cnf.getName());
        context.setResourceBase("src/main/webapp");
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { context, new DefaultHandler() });
        server.setHandler(handlers);
        try {
			server.start();
	        server.join();
		} catch (Exception e) {
			LOGGER.error("启动失败！", e);
		}
    }

}