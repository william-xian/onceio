package top.onceio.mvc;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet(value = "/", asyncSupported = false)
public class OIOServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_TRACE = "TRACE";
	
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
        pw.close();

	}
}