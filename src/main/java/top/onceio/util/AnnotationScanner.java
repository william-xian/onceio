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

	private final Map<Class<?>,Set<Class<?>>> classifiedAnns = new HashMap<>();
    
    public void scanPackages(String ...packages){
    	
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
		Set<Class<?>> clazzList = classifiedAnns.get(annotation);
		if(clazzList == null) {
			clazzList = new HashSet<>();
			classifiedAnns.put(annotation, clazzList);
		}
		clazzList.add(clazz);
    }
	
    public Set<Class<?>> getClasses(Class<?>... annotation) {
    	Set<Class<?>> result = new HashSet<>();
    	for(Class<?> ann:annotation) {
    		result.addAll(classifiedAnns.get(ann));
    	}
		return result;
	}
}
