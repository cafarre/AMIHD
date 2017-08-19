package es.fz1code.amihd.comu.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import es.fz1code.amihd.comu.utils.Logger;

public class DaoBasic {
	protected GestorDB gestorDB = null;
	protected Logger log = new Logger(this.getClass());
	private static final String TRUE_STRING = "S";
	private static final String FALSE_STRING = "N";
	    
	public DaoBasic(GestorDB gestorDB) {
		this.gestorDB = gestorDB;
	}

	protected final void close(Statement statement, ResultSet rs) {
    	close(rs);
        close(statement);
    }
	
    /**
     * Cierra el resultset que se le pase
     * @param ResultSet
     */
	protected final void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
        	log.error("Error [" + e + "] cerrando el resultset.", e);
        }
    }

    /**
     * Cierra el statement que se le pase
     * @param statement
     */
	protected final void close(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (Exception e) {
        	log.error("Error [" + e + "] cerrando el Statement.", e);
        }
    }	
	
	
	/**
	 * Obtiene un Long de un ResultSet
	 * @throws SQLException 
	 */
	protected final Long getLongFromResultSet(
			String nombreCampo, 
			ResultSet rs) throws SQLException {
		
		String result = rs.getString(nombreCampo);
		if (result != null) {
			return Long.parseLong(result);
		}
		return null;
	}
	
	
	/**
	 * Obtiene un Integer de un ResultSet
	 * @throws SQLException 
	 */
	protected final Integer getIntegerFromResultSet(
			String nombreCampo, 
			ResultSet rs) throws SQLException {
		
		String result = rs.getString(nombreCampo);
		if (result != null) {
			return Integer.parseInt(result);
		}
		return null;
	}
	
	protected final Boolean getBoolSNFromResultSet(
			String nombreCampo, 
			ResultSet rs) throws SQLException {
		
		String result = rs.getString(nombreCampo);
		if (result != null) {
			return Boolean.valueOf("S".equalsIgnoreCase(result));
		}
		return null;
	}

	protected final Boolean getBool01FromResultSet(
			String nombreCampo, 
			ResultSet rs) throws SQLException {
		String result = rs.getString(nombreCampo);
		if (result != null) {
			return Boolean.valueOf("1".equalsIgnoreCase(result));
		}
		return null;
	}

	protected final Boolean getBool10FromResultSet(
			String nombreCampo, 
			ResultSet rs) throws SQLException {
		
		return !getBool01FromResultSet(nombreCampo, rs);
	}

	protected final String getStringFromInteger(Integer valor) {
		
		if(valor!=null) {
			return valor.toString();
		}
		else {
			return null;
		}
	}
	
	protected final String getStringFromLong(Long valor) {
		
		if(valor!=null) {
			return valor.toString();
		}
		else {
			return null;
		}
	}

	protected final java.sql.Date getDateSql(java.util.Date valor) {
		
		if(valor!=null) {
			return new java.sql.Date (valor.getTime());
		}
		else {
			return null;
		}
	}

    protected Boolean stringToBoolean(String text) {
        return stringToBoolean(text, TRUE_STRING, FALSE_STRING);
    }
    
    protected Boolean stringToBoolean(String text, String trueString, String falseString) {
        if (trueString.equals(text)) {
            return Boolean.TRUE;
        } else if (falseString.equals(text)) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }
    
    protected String BooleanToString(Boolean bool) {
        return BooleanToString(bool, TRUE_STRING, FALSE_STRING);
    }
    
    protected String BooleanToString(Boolean bool, String trueString, String falseString) {
    	if(bool==null) {
    		return null;
    	}
    	else if(bool.booleanValue()) {
    		return trueString;
    	}
    	else {
    		return falseString;
    	}
    }
}
