package es.fz1code.amihd.comu.utils;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	public static final int NIVELL_DEBUG=4;
	public static final int NIVELL_INFO=3;
	public static final int NIVELL_WARN=2;
	public static final int NIVELL_ERROR=1;
	
	private String nom = null;
	private int nivell = NIVELL_DEBUG;
	
	private PrintStream out = System.out;
	private PrintStream err = System.err;
	
	public Logger(String nom) {
		this.nom =nom; 
	}
	
	public Logger(Class<?> classe) {
		this.nom = classe.getName();
	}

	public void info(String msg) {
		if(nivell>=NIVELL_INFO) {
			this.printMsg(msg, "INFO");
		}
	}
	
	public void debug(String msg) {
		if(nivell>=NIVELL_DEBUG) {
			this.printMsg(msg, "DEBUG");
		}
	}
	
	
	public void error(String msg) {
		this.error(msg,null);
	}

	public void error(Throwable ex) {
		this.error(ex.getMessage(), ex);
	}

	public void error(String msg, Throwable ex) {
		if(nivell>=NIVELL_ERROR) {
			this.printError(msg, ex, "ERROR");
		}
	}
	
	public void warn(String msg) {
		this.warn(msg, null);
	}

	public void warn(Throwable ex) {
		this.warn(ex.getMessage(), ex);
	}

	public void warn(String msg, Throwable ex) {
		if(nivell>=NIVELL_WARN) {
			this.printError(msg, ex, "WARN");
		}
	}
	
	private StringBuffer printCapsalera(String tipus) {
		StringBuffer bf = new StringBuffer();
		bf.append("[");
		bf.append(tipus);
		bf.append("] ");
		bf.append(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date()));
		bf.append(" - ");
		bf.append(this.nom);
		bf.append(": ");
		
		return bf;
	}

	private void printError(String msg, Throwable ex, String tipus) {
		StringBuffer bf = printCapsalera(tipus);
		bf.append(msg);
		bf.append(" --> ");
		
		err.println(bf.toString());
	
		if(ex!=null) {
			ex.printStackTrace(err);
		}
	}
	
	private void printMsg(String msg, String tipus) {
		StringBuffer bf = printCapsalera(tipus);
		bf.append(msg);
		
		out.println(bf.toString());
	}
}
