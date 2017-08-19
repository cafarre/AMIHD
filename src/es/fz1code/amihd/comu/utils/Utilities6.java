package es.fz1code.amihd.comu.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class Utilities6 {
	
	private Utilities6 () {
		//Esta clase NO se puede isntanciar
	}
	
	public static String[] split(String str, String separatorChars) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        List<String> list = new ArrayList<String>();
        int i = 0;
        int start = 0;
        boolean match = false;

        while (i < len) {
            if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                list.add(str.substring(start, i));
                match = false;
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }

        if (match) {
            list.add(str.substring(start, i));
        }
        return (String[]) list.toArray(new String[list.size()]);
    }
	
	//Transforma un String en ArrayList
	public static List<String> indexarString(String s1){
		List<String> entornosList = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(s1,",");
		while (st.hasMoreTokens()){
			String aux = st.nextToken();
			entornosList.add(aux);
		}		
		return entornosList;
	 }
	
	//Comprueba si un ArrayList contiene un String
	public static boolean comprobarStringEnArray(String s1, List<String> lista){
		for (String str : lista){
			if(s1.equals(str)){
				return true;
			}
		}
		return false;
	}
	
	
	public static String getIdioma(Locale locale) {
		if("CA".equalsIgnoreCase(locale.getLanguage())) {
			return "CAT";
		}
		return "CAS";
	}
	
	public static StringBuffer readFile(String filename) throws Exception
    {				
		StringBuffer buffer=new StringBuffer();
		BufferedReader bufferStream = null;
		try
        {
			bufferStream = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"UTF8"));
			String line;
			while ((line=bufferStream.readLine())!=null)
			{
				buffer.append(line);
			}

         }catch(IOException ex){
            	 
			StringBuffer err=new StringBuffer("ERROR: S'ha produït un error al llegir el fitxer: ");
			err.append(filename);
			err.append(". Exception= ");
			err.append(err.append(ex.getMessage()));
			throw new Exception(err.toString()); 	
         }
		finally {
			if(bufferStream!=null) {
				bufferStream.close();
			}
		}
         return buffer;
     }
	
	
	/**
	 * @param text 		- Texte amb camps clau a substituir Ex: {SALTO_LINEA}{SALTO_LINEA}Cordialment.{SALTO_LINEA}
	 * @param params	- Un Map amb les claus-valor a substituir Ex: paramMap.put("SALTO_LINEA", "\n");
	 * @return			- Retorna un StringBuffer amb les substitucions fetes al text Ex: \n\nCordialment.\n
	 * @throws SSIException
	 */
	public static StringBuffer calculaText(String text, Map<String,String> params) throws Exception
    {
		StringBuffer sb = new StringBuffer(text);
		try {
			for (String key: params.keySet()) {
				String valor = params.get(key);
				
				if(valor==null) valor = ""; 
				
				String tag = "{"+key+"}";
				while( sb.toString().indexOf(tag)> -1 ){
					int indexCount=sb.toString().indexOf(tag);
					sb.replace(indexCount,indexCount+tag.length(),valor);
				}
			}
			return sb;		
		}
		catch (Exception e)
		{			
			StringBuffer err=new StringBuffer("ERROR: S'ha produït un error al parsejar el text");
			err.append(". Exception= ");
			err.append(err.append(e.getMessage()));
			throw new Exception(err.toString());
		}
	}
	
	/**
	 * Retorna el nombre de la máquina o null si hay algun error
	 * @return String
	 */
	public static String getHostName() {
		String retorno = null;
		try {
			retorno = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
				
		}
		return retorno;
	}
	
	public static StringBuffer parseAndSubstitute(StringBuffer sb, List<String> datos) throws Exception
	{
		int indexCount = -1;
		String dato = "";
					
		if (sb == null) throw new Exception("ERROR: S'ha produït un error, la cadena a parsejar es null.");
		StringBuffer buffer = new StringBuffer(sb.toString());

		try {
			for (int i=0; i<datos.size(); i++) {
				if(datos.get(i)==null) dato = ""; 
				else dato = datos.get(i).toString();
		
				String tag = "{"+i+"}";
				while( buffer.toString().indexOf(tag)> -1 ){
					indexCount=buffer.toString().indexOf(tag);
					buffer.replace(indexCount,indexCount+tag.length(),dato);
				}
			}

			return buffer;
	
		}
		catch (Exception e)
		{
			StringBuffer err=new StringBuffer("ERROR: S'ha produït un error al parsejar la platilla del email");
			err.append(". Exception= ");
			err.append(err.append(e.getMessage()));
			throw new Exception(err.toString());
		}
	}
	
	/*
	 * Substituye una cadena por otra dentro de un texto tantas veces como aparezca
	 */
    public static String replace(String text, String find, String replace) {
        StringBuffer sb = new StringBuffer(text);
        int idx = text.indexOf(find);
        while (idx != -1) {
            sb.replace(idx, idx + find.length(), replace);
            idx = sb.toString().indexOf(find, idx + replace.length());
        }
        return sb.toString();
    }
    
    public static String getTimeStamp() {
		Calendar calen = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		return (sdf.format(calen.getTime()));    	
	}	

    public static File grabaFitxerDisc(InputStream isImg, String ruta) throws Exception  {
		File file = new File(ruta);
		FileOutputStream fos = new FileOutputStream(file); 

		if(isImg!=null) {
			byte[] buffer = new byte[1024];
			int bytesRead;
		    while ((bytesRead = isImg.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
			fos.flush();
			fos.close();
		}
		
		return file;
	}
	
	public static String streamToString(InputStream isImg, String charset) throws Exception  {
		
		StringBuffer result = new StringBuffer();
		if(isImg!=null) {
			
			byte[] buffer = new byte[1024];
			int bytesRead;
		    while ((bytesRead = isImg.read(buffer)) != -1) {
				result.append(new String(buffer,0,bytesRead,charset));
			}
		}
		
		return result.toString();
	}
	
	public static ByteArrayInputStream streamToByteArrayInputStream(InputStream isImg) throws Exception  {
		
		
		ByteArrayInputStream bais = null;
		if(isImg!=null) {
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int bytesRead;
		    while ((bytesRead = isImg.read(buffer)) != -1) {
		    	baos.write(buffer,0,bytesRead);
			}
		    baos.flush();
		    
		    byte[] bytes = baos.toByteArray();
		    bais = new ByteArrayInputStream(bytes);
		}
		
		return bais;
	}
}
