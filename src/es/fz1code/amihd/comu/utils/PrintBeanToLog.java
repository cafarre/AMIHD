package es.fz1code.amihd.comu.utils;

import java.lang.reflect.Field;

import es.fz1code.amihd.comu.bean.MyBean;

public class PrintBeanToLog{
	
	public static <O extends MyBean> String printToLog(O beanssi){
		String result = ""; 
		try {
            Field fieldlist[] = beanssi.getClass().getDeclaredFields();
            result += "[";
            for (int i = 0; i < fieldlist.length; i++) {
                Field fld = fieldlist[i];
                fld.setAccessible(true);
                if(i!=0) result += ",";
                result+= fld.getName() + "=" + fld.get(beanssi);
            }
            result += "]";
        } catch (Exception e) {
        	e.printStackTrace();
        }
		 return result;
	}
	
}
