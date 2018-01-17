package top.onceio.mvc;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import top.onceio.beans.ApiMethod;
import top.onceio.beans.ApiPair;
import top.onceio.beans.BeansEden;
import top.onceio.util.OUtils;

@WebServlet(value="/",asyncSupported = false)
public class OIOServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static Gson GSON = new Gson();
	
	
	
	@Override
	public void init() throws ServletException {
	    super.init();
	}
	
	static <T> T readParam(HttpServletRequest req, Class<T> clazz) throws InstantiationException, IllegalAccessException, IOException{
		T result = clazz.newInstance();
		req.getParameterMap();
		if(req.getContentLength() > 0) {
			ServletInputStream sis = req.getInputStream();
			Reader reader = new InputStreamReader(sis);
			GSON.fromJson(reader, clazz);
		}
		return result;
	}
	
	void writeRepsone(HttpServletResponse resp, Object obj) throws IOException {
		if(obj != null) {
			Appendable writer = resp.getWriter();
			GSON.toJson(obj, writer);
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
		PrintWriter pw = resp.getWriter();
		Date now = new Date();
        pw.printf("OnceIO %s\n", now.toString());
        pw.printf("method %s\n", req.getMethod());
        pw.printf("URI: %s\n", req.getRequestURI());
        pw.printf("Param: %s\n", GSON.toJson(req.getParameterMap()));
        ApiPair apiPair = BeansEden.get().search(ApiMethod.valueOf(req.getMethod()), req.getRequestURI());
        if(apiPair != null) {
        	pw.printf("api:%s, ApiClass:%s\n",apiPair.getApi(),apiPair.getBean().getClass());
        	Map<String,Object> result = new HashMap<>();
        	apiPair.resoveUriParams(result, req.getRequestURI());
        	apiPair.resoveReqParams(result, req);
        	pw.printf("param : %s", OUtils.toJSON(result));
        }
        pw.close();

	}
}