package top.onceio.mvc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import top.onceio.db.annotation.Tbl;
import top.onceio.db.annotation.TblView;
import top.onceio.db.tbl.OEntity;
import top.onceio.mvc.annocations.Api;
import top.onceio.mvc.annocations.AutoApi;
import top.onceio.mvc.annocations.Def;
import top.onceio.mvc.annocations.Definer;
import top.onceio.mvc.annocations.Using;
import top.onceio.util.AnnotationScanner;

@WebServlet(value = "/", asyncSupported = false)
public class OIOServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static Gson GSON = new Gson();

	private final static AnnotationScanner scanner = new AnnotationScanner(Api.class,AutoApi.class,
			Definer.class,Def.class,Using.class,
			Tbl.class,TblView.class);
	
	@Override
	public void init() throws ServletException {
	    super.init();
	    System.out.println("load ... req&resp");
	}
	
	@SuppressWarnings("unchecked")
	public List<Class<? extends OEntity<?>>> scanTblTblView() {
		List<Class<? extends OEntity<?>>> entities= new LinkedList<>();
		scanner.scanPackages("cn.mx.app");
		for(Class<?> clazz:scanner.getClasses(Tbl.class)) {
			if(clazz.isAssignableFrom(OEntity.class)) {
				entities.add((Class<? extends OEntity<?>>)clazz);
			}
		}
		for(Class<?> clazz:scanner.getClasses(TblView.class)) {
			if(clazz.isAssignableFrom(OEntity.class)) {
				entities.add((Class<? extends OEntity<?>>)clazz);
			}
		}
		return entities;
	}
	
	
	@SuppressWarnings("deprecation")
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();
		Date now = new Date();
        pw.printf("OnceIO %s\n", now.toLocaleString());
        pw.printf("URI: %s\n", req.getRequestURI());
        pw.printf("Param: %s\n", GSON.toJson(req.getParameterMap()));
        pw.close();
    }
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    super.doPost(req, resp);
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
}