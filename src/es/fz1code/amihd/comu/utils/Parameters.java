package es.fz1code.amihd.comu.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

public class Parameters {
	private static Properties store=null;
	private static String m_properties="";

    public Parameters() {
    }

    public static boolean getBoolean(String s,boolean flag) {
        String s1 = Parameters.getParameter(s);
        if(s1 == null)
            return flag;
        else
            return (new Boolean(s1)).booleanValue();
    }

    public static char getCharacter(String s, char c) {
        String s1 = Parameters.getParameter(s);
        if(s1 == null)
            return c;
        else
            return s1.charAt(0);
    }

    public static int getInteger(String s, int i) {
        String s1 = Parameters.getParameter(s);
        if(s1 == null)
            return i;
        else
            return Integer.parseInt(s1);
    }

    public static String[] getListaIPProa() {
        String as[] = null;
        String s = Parameters.getString("ip.proa", "");
        if(s == null || s.equals(""))
            return null;
        StringTokenizer stringtokenizer = new StringTokenizer(s, ",");
        int i = stringtokenizer.countTokens();
        as = new String[i];
        for(int j = 0; j < i; j++)
            as[j] = stringtokenizer.nextToken();

        return as;
    }

    public static String getParameter(String s) {
        if(store == null)
            return null;
        else
            return store.getProperty(s);
    }

    public static String getString(String s, String s1) {
		
        String s2 = Parameters.getParameter(s);
	
        if(s2 == null)
            return s1;
        else
            return s2;
    }

    public static void loadProperties(String s) throws Exception {
        m_properties = s;
        store = new Properties();
        try{
            FileInputStream fileinputstream = new FileInputStream(m_properties);
            store.load(fileinputstream);
            fileinputstream.close();
        }catch(FileNotFoundException e1){
            store = null;
			throw new Exception(Parameters.class+":loadProperties():Fichero de propiedades no encontrado:"+e1.getMessage());
        }catch(IOException e2) {
            store = null;
			throw new Exception(Parameters.class+":loadProperties():Error de entrada/salida:"+e2.getMessage());
        }catch(NullPointerException e3) {
			throw new Exception(Parameters.class+":loadProperties():Error de puntero nulo:"+e3.getMessage());
        }
    }
}