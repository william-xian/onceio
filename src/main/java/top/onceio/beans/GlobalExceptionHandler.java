package top.onceio.beans;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import top.onceio.db.dao.Dao;
import top.onceio.db.tbl.OI18n;
import top.onceio.exception.Failed;
import top.onceio.util.OLog;
import top.onceio.util.OUtils;



public class GlobalExceptionHandler {
	
	Dao<OI18n,String> dao;
 
	public Map<String,Object> failedHandler(HttpServletRequest req, Failed failed) throws Exception {
    	String defaultFromat  = failed.getFormat();
    	Locale  locale = req.getLocale();
    	String lang = locale == null ? null:locale.getLanguage();
    	if(lang != null && !lang.equals(Locale.getDefault().getLanguage())){
        	String id = "msg/"+lang+"_"+OUtils.encodeMD5(failed.getFormat());	
        	OI18n i18n = dao.get(id);
        	if(i18n != null) {
        		defaultFromat  = i18n.getName();
        	}
    	}
    	String msg  = String.format(defaultFromat, failed.getArgs());
    	OLog.error(String.format("Host %s invokes url %s ERROR: %s", req.getRemoteHost(), req.getRequestURL(), msg));
        Map<String,Object> result = new HashMap<>();
        if(failed.getData() != null) {
        	result.put("data", failed.getData());	
        }
        switch(failed.getLevel()){
        case Failed.ERROR:
        	result.put("error", msg);
        	break;
        case Failed.WARN:
        	result.put("warnning", msg);
        	break; 
        case Failed.MSG:
        	result.put("msg", msg);
        	break;
        }
        return result;
    }
 
    public Map<String,String> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
    	OLog.error(String.format("Host %s invokes url %s ERROR: %s", req.getRemoteHost(), req.getRequestURL(), e.getMessage()));
        Map<String,String> result = new HashMap<>();
    	result.put("error", e.getMessage());
        return result;
    }
}	