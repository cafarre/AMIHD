package es.fz1code.amihd.comu.dao;

import java.sql.Connection;
import java.sql.DriverManager;

import java.util.Date;
import java.util.Properties;

import es.fz1code.amihd.comu.utils.Logger;
import es.fz1code.amihd.comu.utils.Parameters;

public class GestorDB {

public static enum MODO {WEB, BATCH};
		private Logger log = new Logger(this.getClass());

	private String databaseURL;
	//private String driver;
	
	/*
	private String user, password;
	private String source;
	private int reintentos;
	private int tespera;
	private Hashtable<String, String> hashtable;*/

	//private DataSource dataSource;
	private Connection connection = null;	private String threadId;
	//private boolean conexionPorDataSource = true;
	private boolean transaccionIniciada = false;
	
	
	public GestorDB() throws Exception{
		inicializar();		
		openConnection();
	}
	
	public GestorDB(MODO modo) throws Exception {
		if (modo != null && modo.equals(MODO.BATCH)) {
			log.info("GestorDB en modo BATCH.");
			//this.conexionPorDataSource = false;
		}
		
		inicializar();
		openConnection();
	}

	public GestorDB(Connection connection) {
		this.connection=connection;
		
		try {
			inicializar();
		} catch(Exception ex) {
			log.error(ex);
		}
	}	
	
	
	private void inicializar() throws Exception {
		try {
			//TODO: fer que funcionin els parametres
			//this.driver = Parameters.getString("bbdd.driver", "sun.jdbc.odbc.JdbcOdbcDriver");
			this.databaseURL = Parameters.getString("bbdd.databaseURL", "jdbc:postgresql://localhost:5432/BDHDTECA");
			
			
			/*
			String claseFactoria = Parameters.getString(Constantes.CLAVE_DS_CONF_CLASEFACTORIA, "");		
			        
	        this.user = Parameters.getString(Constantes.CLAVE_DS_USER, "");
			this.password =	Parameters.getString(Constantes.CLAVE_DS_PASSWORD, "");
			this.claseFactoriaOci = Parameters.getString(Constantes.CLAVE_DS_OCI_CONNECTMAIN, "");
			Parameters.getString("lacaixa.ssi.datasource.connectmain", "");
			this.source = Parameters.getString(Constantes.CLAVE_DS_NOMBRE_DATASOURCE, "");
			this.reintentos=Parameters.getInteger(Constantes.CLAVE_DS_REINTENTS,  3);	
			this.tespera=Parameters.getInteger(Constantes.CLAVE_DS_TEMPS_ESPERA,  11000);	        
	        
			this.hashtable = new Hashtable<String, String>();
			this.hashtable.put("Context.INITIAL_CONTEXT_FACTORY", claseFactoria);
			*/
			
			Thread threadActual = Thread.currentThread(); 
			threadId = threadActual.getId() + "." + threadActual.getName();			
			
		} catch (Exception e) {
			log.debug("Excepcion al cargar los parametros para el GestorDB: " + e.getMessage());
			throw new Exception("Excepcion al cargar los parametros para el GestorDB: "	+ e.getMessage());
		}
	}
	
	public void openConnection() throws Exception {
		
		if (!isClosedConnection()) {
			return; //Si ya esta la conexion abierta no hacemos nada
		}		
		/*
		if(this.conexionPorDataSource) {
			for (int i=0; i<reintentos; i++) {
				Context ctx = null;
				try {
					ctx = new InitialContext(hashtable);
					try{
						this.dataSource = (DataSource) ctx.lookup(source);
					}catch(NameNotFoundException n){
						  
						ctx = new InitialContext();
						Context envCtx = (Context) ctx.lookup("java:comp/env");
		                
						// Look up our data source
						this.dataSource = (DataSource) envCtx.lookup(source);
		                        
					}
					
					Connection c = null;
					try{
						c = dataSource.getConnection(user, password);
					}catch(java.lang.UnsupportedOperationException e){
						c = dataSource.getConnection();//Para que pueda conectar con Tomcat
					}
					
					this.connection=c;
					
					log.info("Conexion a la Base de Datos establecida con DataSource. Intento: " + i + ". Thread: " + threadId);
					return;
				} catch (Throwable e) {
					log.error("Parte WEB: [Error al recuperar el DataSource]", e);
					log.error("Intento " + i);
					Thread.sleep(tespera);					
				}
			}
		}
*/
		//Conexion JDBC
		//this.conexionPorDataSource = false;
		try {
			//DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			//Connection cn = DriverManager.getConnection(claseFactoriaOci, user, password);
			
			Class.forName("org.postgresql.Driver");
			Properties props = new Properties();
			props.setProperty("user","postgres");
			props.setProperty("password","fazerfz1");
			Connection cn = DriverManager.getConnection(databaseURL, props);
			this.connection=cn;
			
			log.info("Conexion con la Base de Datos establecida mediante JDBC. Thread: " + threadId);
		} catch (Exception e) {
			log.error("No se ha podido establecer conexión con la base de datos mediante DataSource ni JDBC.");
			throw new Exception("[Error al realizar la conexion JDBC]", e);
		}
	}
	
	public Connection getConnection() {
		return connection;
	}

	public void closeConnection() throws Exception {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				log.info("Conexion con la Base de Datos Cerrada. Thread: " + threadId);
			}
		} catch (Exception e) {
			log.error("Error al cerrar la conexion BBDD. Thread: " + threadId, e);
			throw new Exception(e.getMessage());
		}
	}
	
	public boolean isClosedConnection() throws Exception {
		return (connection == null || connection.isClosed());
	}		
		
	public void startTransaction() throws Exception {
		try {
			Connection connection = getConnection();
			if(connection==null || connection.isClosed()) {
				openConnection();
			}
			
			connection = getConnection();
			if(connection==null || connection.isClosed()) {
				throw new Exception("No se ha podido establecer conexion con la BBDD.");
			}
			
			connection.setAutoCommit(false);
			transaccionIniciada = true;
			
			log.info("Transaccion de BBDD Iniciada: " + new Date());
		} catch (Exception e) {
			log.error("Transaccion no iniciada.", e);
			throw e;
		}
	}
	
	public void endTransaction() throws Exception {
		Connection connection = getConnection();
		if (connection != null 
				&& !connection.isClosed()
				&& transaccionIniciada == true) {
			
			connection.commit();
			connection.setAutoCommit(true);
			this.transaccionIniciada = false;
			
			log.info("Transaccion de BBDD Finalizada con COMMIT: " + new Date());
		}
	}
	
	public void rollbackTransaction() throws Exception {
		Connection connection = getConnection();
		if (connection != null 
				&& !connection.isClosed()
				&& transaccionIniciada == true) {
			
			connection.rollback();
			connection.setAutoCommit(true);
			this.transaccionIniciada = false;
			
			log.info("Transaccion de BBDD Finalizada con ROLLBACK: " + new Date());
		}
	}
	
	public boolean isTransactionStarted() {
		return this.transaccionIniciada;
	}	
	
}
