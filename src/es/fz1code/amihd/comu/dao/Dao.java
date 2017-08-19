package es.fz1code.amihd.comu.dao;

import java.util.List;


public abstract class Dao<O> extends DaoBasic {
	
	public Dao(GestorDB gestorDB) {
		super(gestorDB);
	}

	public abstract List<O> select(O criteris) throws Exception;
	public abstract O selectById(Object id) throws Exception;
	public abstract void insert(O objecte) throws Exception;
	public abstract void update(O objecte) throws Exception;
	public abstract void deleteById(Object id) throws Exception;
}
