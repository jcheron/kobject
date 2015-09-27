package net.ko.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.ko.framework.Ko;
import net.ko.kobject.KObject;

public class KReflection {
	public static Object invoke(String methodName, Object o){
		return KReflection.invoke(methodName, o, o.getClass());
	}
	public static Object invoke(String methodName, Object o,Class<?> clazz){
		Object ret=new String("");
		try{
			Method method=clazz.getDeclaredMethod(methodName,new Class[] {});
			method.setAccessible(true);
			ret =method.invoke(o,new Object[] {});
		}catch(Exception e){
			Class<?> sClazz=clazz.getSuperclass();
			if (sClazz!=null)
				return KReflection.invoke(methodName, o,clazz.getSuperclass());
		}
		return ret;
	}
	private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
	    try {
	      return clazz.getDeclaredField(fieldName);
	    } catch (NoSuchFieldException e) {
	      Class<?> superClass = clazz.getSuperclass();
	      if (superClass == null) {
	        throw e;
	      } else {
	        return KReflection.getField(superClass, fieldName);
	      }
	    }
	}
	public Class<?> getFieldType(Class<?> clazz,String fieldName) throws NoSuchFieldException{
		Class<?> cls=null;
		Field f=KReflection.getField(clazz,fieldName);
		if(f!=null)
			cls=f.getType();
		return cls;
		
	}	
	public static Object getFieldValue(String fieldName,Object o) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		Object result=null;
		if(o instanceof KObject){
			result=((KObject)o).getAttribute(fieldName);
		}else{
			Field f=KReflection.getField(o.getClass(), fieldName);
			f.setAccessible(true);
			result=f.get(o);
		}
		return result;
	}
	public static Object getMemberValue(String memberName,Object o){
		Object result=null;
		try{
			result=KReflection.getFieldValue(memberName, o);
		}catch(Exception e){
			result=KReflection.invoke(memberName, o);
		}
		if(result==null)
			result=Ko.getNullValue();
		return result;
	}
}
