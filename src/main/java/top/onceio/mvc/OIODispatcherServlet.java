package top.onceio.mvc;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import top.onceio.annotation.BeansIn;
import top.onceio.beans.ApiMethod;
import top.onceio.beans.ApiPair;
import top.onceio.beans.BeansEden;
import top.onceio.exception.Failed;
import top.onceio.util.OUtils;

@WebServlet(value="/",asyncSupported = false)
public class OIODispatcherServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static Gson GSON = new Gson();
	
	@Override
	public void init() throws ServletException {
	    super.init();
	    String launcherClass = getInitParameter("launcher");
	    Class<?> cnf;
		try {
			cnf = Class.forName(launcherClass);
			loadBeans(cnf);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
    private static void loadBeans(Class<?> cnf) {
        BeansIn beansPackage = cnf.getDeclaredAnnotation(BeansIn.class);
        if(beansPackage != null && beansPackage.value().length != 0) {
        	BeansEden.get().resovle(beansPackage.value());
        } else {
        	String pkg = cnf.getPackage().getName();
            BeansEden.get().resovle(pkg);
        }
        
    }
	
	@Override
	public void destroy() {
	    System.out.println("servlet销毁！");
	    super.destroy();
	}

	@Override
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        HttpServletRequest  req;
        HttpServletResponse resp;
        if (!(request instanceof HttpServletRequest &&
        		response instanceof HttpServletResponse)) {
            throw new ServletException("non-HTTP request or response");
        }
        req = (HttpServletRequest) request;
        resp = (HttpServletResponse) response;
        ApiPair apiPair = BeansEden.get().search(ApiMethod.valueOf(req.getMethod()), req.getRequestURI());
        if(apiPair != null) {
			try {
				Object obj = apiPair.invoke(req, resp);
				if (obj != null) {
					PrintWriter writer = resp.getWriter();
					GSON.toJson(obj, writer);
					writer.close();
				}
    		}catch(Exception e) {
    			if(e instanceof Failed) {
    				Failed failed = (Failed)e;
    				Map<String,Object> r = new HashMap<>();
    				r.put("msg",String.format(failed.getFormat(), failed.getArgs()));
    				r.put("data", failed.getData());
    				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, OUtils.toJSON(r));
    			}else {
    				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    			}
    		}
        } else {
        	resp.sendError(HttpServletResponse.SC_NOT_FOUND, String.format("找不到请求：%s %s", req.getMethod(), req.getRequestURI()));
        }
	}
}