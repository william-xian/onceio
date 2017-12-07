package top.onceio.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;


public class AnnotationScanner {
	
    private final Set<Class<?>> filter = new HashSet<>();
    
    public AnnotationScanner(Class<?> ...annotation) {
    	filter.addAll(Arrays.asList(annotation));
	}
    
	public Set<Class<?>> getFilter() {
		return filter;
	}

	private final Map<Class<?>,Set<Class<?>>> annotations = new HashMap<>();
    
    public void scanPackages(String ...packages){
    	OLog.info("dls scan packages:" + packages);
		ClassScanner.findBy(new Consumer<Class<?>> (){

			@Override
			public void accept(Class<?> clazz) {
				for(Annotation a:clazz.getAnnotations()){
					if(filter.contains(a.annotationType())) {
						putClass(a.annotationType(),clazz);							
					}
				}
			}
			
		}, packages);
    }
    
    private void putClass(Class<?> annotation,Class<?> clazz) {
		Set<Class<?>> clazzList = annotations.get(annotation);
		if(clazzList == null) {
			clazzList = new HashSet<>();
			annotations.put(annotation, clazzList);
		}
		clazzList.add(clazz);
    }
	
    public Set<Class<?>> getClasses(Class<?> annotation) {
		return annotations.get(annotation);
	}
}
