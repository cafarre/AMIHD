package es.fz1code.amihd.comu.dao;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.fz1code.amihd.comu.bean.MyBean;


/**
 * Classe abstracta que implementa les operacions "tipiques" sobre BeanSSI:
 * <ol>
 * <li>SELECT
 * <li>INSERT
 * <li>UPDATE
 * <li>DELETE
 * </ol>
 * 
 * <p> La idea es que els DAO's específics només hagin d'extendre per indicar les 
 * particularitats com el nom de la taula i la traducció de camps Java<->Oracle.
 * 
 * @author cafarre
 *
 * @param <O> Tipus genèric corresponent a un BeanSSI.
 */
public abstract class DaoDinamic<O extends MyBean> extends Dao<O> {

	private String nomTaula = null;
	private List<PropietatJava> listCampsJava=null;
	private Map<String, PropietatJava> mapJavaToOracle = new HashMap<String, PropietatJava>();
	private List<String> listPropietatsJavaExcluides = new ArrayList<String>();
	private List<CampOrderBy> listCampsOrderBy = new ArrayList<CampOrderBy>();
	private List<PropietatJava> listCampsPK = new ArrayList<PropietatJava>();
	
	/**
	 * 
	 * @param gestorDB
	 * @param locale
	 * @param nomTaula Nom de la taula Oracle
	 */
	public DaoDinamic(GestorDB gestorDB, String nomTaula) throws Exception {
		super(gestorDB);
		
		this.nomTaula = nomTaula;
		
		initPropietatsJava();
		initPropietatsJavaExcluides();
		addPropietatJavaExcluida("class");
		initRestaPropietatsJava();
		initListCampsOrderBy();
		initListCampsPK();
	}
	

	/**
	 * <p>Inicialitza les propietats Java. 
	 * Cal cridar al metode <code>this.newPropietatJava("nomJava", "nomOracle")</code>.
	 * 
	 * <p>Exemple de Relació de propietats Java-Oracle:
	 * <pre>this.newPropietatJava("tipoContrato", "TIPO_CONTRATO");</pre> 
	 *  
	 * <p>Exemple de canvi de JavaOracleConversor:
	 * <pre>
	 * 		JavaOracleConversor string10ToBool = new JavaOracleConversor() {
	 *		@Override
	 *		public Object OracleToJava(Object obj) {
	 *			Boolean b = stringToBoolean((String)obj,"0", "1");
	 *			return b;
	 *		}
	 *
	 * 		@Override
	 *		public Object JavaToOracle(Object obj) {
	 *
	 *			String s = BooleanToString((Boolean)obj, "0", "1");
	 *			return s;
	 *		}
	 *	}; 
	 * 
	 * this.newPropietatJava("mostrarMiembrosVip", "mtt_usuarios_vip").setJavaOracleConversor(string10ToBool);
	 * </pre> 
	 * 
	 * <p>Exemple de definicio de SELECT i WHERE:
	 * <pre>
	 * 		PropietatJava prop = this.newPropietatJava("idCentro", "idcentro");
	 *		prop.setColumnaSelect("centrolpad(IDCENTRO,5) as IDCENTRO");
	 *		prop.setColumnaWhereCriteri("centrolpad(IDCENTRO,5)");
	 * 		prop.setColumnaWhereValor("centrolpad(?,5)");
	 * </pre> 
	 * 
	 * @see PropietatJava
	 * @see #newPropietatJava(String, String)
	 * @see PropietatJava#setJavaOracleConversor(es.lacaixa.ssi6.comu.daos.dinamic.DaoDinamic.JavaOracleConversor)
	 * 
	 */
	protected abstract void initPropietatsJava();

	/**
	 * Inicialitza les columnes (camps) de Oracle que serviran per fer el 
	 * OrderBy en la SELECT.
	 * 
	 * <p>Cal cridar el metode <code>this.addCampOrderBy("nomOracle")</code>.
	 * 
	 * <p>Exemple: 
	 *  <pre>this.addCampOrderBy("idempresa", false);</pre> 
	 *
	 * @see CampOrderBy
	 * @see #addCampOrderBy(String, boolean)
	 */
	protected abstract void initListCampsOrderBy();

	/**
	 * Nom de la taula que ataca el DAO dinamic.<br>
	 * 
	 * @return nom_taula
	 */
	public String getNomTaula() {
		return nomTaula;
	}
	
	/**
	 * Llista de camps de la taula utilitzats per el Order By de la SELECT.
	 * L'ordre de la llista és el ordre que s'aplicará en el Order By.
	 * 
	 * <p>El CampOrderBy representa el nom del camp de la taula i si l'ordre es ASC o DESC.
	 * 
	 * @return llista de camps Order By.
	 */
	public List<CampOrderBy> getListCampsOrderBy() {
		return listCampsOrderBy;
	}

	/**
	 * Retorna tots els elements de la taula
	 * @return
	 * @throws Exception
	 */
	public List<O> selectAll() throws Exception {
		List<O> listCriteris = new ArrayList<O>();
		listCriteris.add(getBeanInstance());
		
		return select(listCriteris);
	}

	/**
	 * Implementació genèrica del SELECT a partir de una llista de criteris.
	 * 
	 * <p>Es pot sobrescriure el metode per modificar la implementacio
	 * o evitar (capar) que es realitzin SELECT sobre la taula.
	 * 
	 * @param listCriteris Llista de Beans criteri. Cada Bean es tracat com un OR en el WHERE.
	 * @return Lista de Beans Resultat de la cerca.
	 * @throws Exception
	 */
	public List<O> select(List<O> listCriteris) throws Exception {
		return this.select(listCriteris, null, null);
	}
	
	/**
	 * Implementació genèrica del SELECT a partir de una llista de criteris i nomes retorna les files compreses entre l'interval de indexos.
	 * 
	 * <p>Es pot sobrescriure el metode per modificar la implementacio
	 * o evitar (capar) que es realitzin SELECT sobre la taula.
	 * 
	 * @param listCriteris Llista de Beans criteri. Cada Bean es tracat com un OR en el WHERE.
	 * @return Lista de Beans Resultat de la cerca.
	 * @throws Exception
	 */
	public List<O> select(List<O> listCriteris, Integer indexIni, Integer numResultats) throws Exception {
		
		if (null == listCriteris) {
			listCriteris = new ArrayList<O>();
		}
		
		if(listCriteris.size()==0 || listCriteris.get(0) == null) {
			return listCriteris;
		}
	
		//Construim el SELECT SQL
		StringBuffer query = new StringBuffer();
		query.append(" SELECT ");
		int i=1;
		for(PropietatJava prop : listCampsJava) {
			query.append(prop.getColumnaSelect());
			
			if(i < listCampsJava.size()) {
				query.append(", ");
			}
			i++;
		}

		query.append(" FROM ");
		query.append(nomTaula);
		query.append(" WHERE (1<>1) ");
		
		//CONSTRUIM EL WHERE
		for(O criteris : listCriteris) {
			query.append(" OR ((1=1) ");

			for(PropietatJava prop : listCampsJava) {
				Object valorAtribut = this.getReadMethod(prop).invoke(criteris);
				if(valorAtribut!=null) {
					query.append(" AND ");
					query.append(prop.getColumnaWhereCriteri());
					query.append(" = ");
					query.append(prop.getColumnaWhereValor());
				}
			}
			
			query.append(") ");
		}
		
		//CONSTRUIM EL ORDER BY
		if(listCampsOrderBy!=null && listCampsOrderBy.size() > 0) {
			query.append(" ORDER BY ");
			int j=1;
			for(CampOrderBy camp : listCampsOrderBy) {
				query.append(camp.getSQL());
				if(j < listCampsOrderBy.size()) {
					query.append(", ");
				}
				j++;
			}
		}
		
		
		//APLICA EL FILTRE PER ROWNUM
		if(indexIni!=null) {
			if(numResultats==null) {
				numResultats = 10;
			}
		
			StringBuffer sqlRowNum = new StringBuffer();
			sqlRowNum.append("select s2.* from (SELECT s1.*, rownum idxRow FROM (");
			sqlRowNum.append(query);
			sqlRowNum.append(") s1) s2 ");
			sqlRowNum.append("WHERE (s2.idxRow between ");
			sqlRowNum.append(indexIni); 
			sqlRowNum.append(" and ");
			sqlRowNum.append(indexIni + numResultats-1);
			sqlRowNum.append(") ");
			
			query = sqlRowNum;
		}
		
		log.debug("ejecutando... " + query.toString());
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			Connection connection = this.gestorDB.getConnection();
			ps = connection.prepareStatement(
					query.toString(),
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);

			//CONSTRUIM ELS VALORS DEL WHERE
			int k=1;
			for(O criteris : listCriteris) {
				for(PropietatJava prop : listCampsJava) {
					Object valorAtribut = this.getReadMethod(prop).invoke(criteris);
					if(valorAtribut!=null) {
						//Aplica la transforamcio si fa falta
						valorAtribut = this.aplicaTransformacioJavaToOracle(prop, valorAtribut);
						
						sendValueToOracle(prop, ps, k, valorAtribut);
					
						k++;
					}
				}
			}
			
			rs = ps.executeQuery();
			List<O> result = new ArrayList<O>();
			while(rs.next()) {
				
				//GENEREM UN BEAN PER CADA FILA DEL RESULTSET
				O obj = this.getBeanInstance();
				for(PropietatJava prop : listCampsJava) {
					//Lectura del valor
					Object valorColumna = receiveValueFromOracle(prop, rs);
					
					//Aplica la transforamcio si fa falta
					valorColumna = this.aplicaTransformacioOracleToJava(prop, valorColumna);
					
					Method setMethod = this.getWriteMethod(prop);
					Class<?>[] parametres = setMethod.getParameterTypes();
					if(parametres.length > 0 ) {
						Class<?> param = parametres[0];
						
						if(valorColumna!=null && 
								!param.isAssignableFrom(valorColumna.getClass())) {
							
							log.warn("CAF: FALTA TROBAR UNA SOLUCIO A AQUESTA CASUISTICA!!");
						}
						
						setMethod.invoke(obj, valorColumna);
					}
				}
				
				result.add(obj);
			}
			return result;
		} 
		catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} 
		finally {
			close(ps, rs);
		}
	}

	/**
	 * Permet fer SELECT a partir d'un criteri.
	 * 
	 * @param criteris Bean de criteri.
	 * @return Lista de Beans Resultat de la cerca.
	 * @throws Exception
	 */
	@Override
	public List<O> select(O criteris) throws Exception {
		return  this.select(criteris, null, null);
	}
	
	/**
	 * Permet fer SELECT a partir d'un criteri i només retorns les files compreses entre l'interval de indexos
	 * 
	 * @param criteris Bean de criteri.
	 * @return Lista de Beans Resultat de la cerca.
	 * @throws Exception
	 */
	public List<O> select(O criteris, Integer indexIni, Integer numResultats) throws Exception {
		List<O> listCriteris = null;
		if(criteris != null){
			listCriteris = new ArrayList<O>();
			listCriteris.add(criteris);
			
			return this.select(listCriteris, indexIni, numResultats);
		}
		else {
			return new ArrayList<O>();
		}
	}

	/**
	 * Permet fer DELETE a partir d'un criteri.
	 * 
	 * @param objID
	 * @throws Exception
	 */
	@Override
	public void deleteById(Object id) throws Exception {

		@SuppressWarnings("unchecked")		
		O objID = (O)id;
		this.deleteById(objID);	
	}
	
	/**
	 * Implementació genèrica del DELETE a partir d'un Bean de criteri.
	 * 
	 * <p>Es pot sobrescriure el metode per modificar la implementacio
	 * o evitar (capar) que es realitzin DELETE sobre la taula.
	 *  
	 * @param id Bean de criteri.
	 * @throws Exception
	 */
	public void deleteById(O objID) throws Exception {
		if(this.listCampsPK==null || this.listCampsPK.size()==0) {
			throw new Exception("ATENCIO! No es pot fer DELETE d'una taula sense PK definit.");
		}
		
		//Construim el INSERT SQL
		StringBuffer query = new StringBuffer();
		query.append(" DELETE FROM ");
		query.append(this.nomTaula);
				
		//CONSTRUIM EL WHERE
		query.append(" WHERE (1=1)");
		for(PropietatJava prop : this.listCampsPK) {

			query.append(" AND ");
			query.append(prop.getNomColumna());
			query.append(" = ");
			query.append(prop.getColumnaWhereValor());
		}
		
		log.debug("ejecutando... " + query.toString());
		PreparedStatement ps = null;
		try {
			Connection connnection = this.gestorDB.getConnection();
			ps = connnection.prepareStatement(query.toString());

			//CONSTRUIM ELS VALORS DEL WHERE
			int k=1;
			for(PropietatJava prop : this.listCampsPK) {				
				
				Object valorAtribut = this.getReadMethod(prop).invoke(objID);
				
//				Aplica la transforamcio si fa falta
				valorAtribut = this.aplicaTransformacioJavaToOracle(prop, valorAtribut);
				sendValueToOracle(prop, ps, k, valorAtribut);
				
				k++;
			}
			
			ps.executeUpdate();
		} 
		catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} 
		finally {
			close(ps);
		}
	}

	
	/**
	 * Implementació genèrica del INSERT a partir d'un Bean de dades.
	 * 
	 * <p>Es pot sobrescriure el metode per modificar la implementacio
	 * o evitar (capar) que es realitzin INSERT sobre la taula.
	 *  
	 * @param id Bean de dades.
	 * @throws Exception
	 */
	@Override
	public void insert(O objecte) throws Exception {
		
		//Construim el INSERT SQL
		StringBuffer query = new StringBuffer();
		query.append(" INSERT INTO ");
		query.append(this.nomTaula);
		query.append(" (");
		
		for(PropietatJava prop : listCampsJava) {
			Object valorAtribut = this.getReadMethod(prop).invoke(objecte);
			if(valorAtribut!=null){
				query.append(prop.getNomColumna());				
				query.append(",");				
			}
		}				
		
		//	CONSTRUIM ELS  VALUES		
		query =  new StringBuffer(query.substring(0, query.length()-1));		
		query.append(") VALUES (");
						
		for(PropietatJava prop : listCampsJava) {
			Object valorAtribut = this.getReadMethod(prop).invoke(objecte);
			if(valorAtribut!=null){
				query.append(prop.getColumnaWhereValor());				
				query.append(",");				
			}
		}									
		
		query =  new StringBuffer(query.substring(0, query.length()-1));
		query.append(")");
		
		log.debug("ejecutando... " + query.toString());
		PreparedStatement ps = null;
		try {
			Connection connnection = this.gestorDB.getConnection();
			ps = connnection.prepareStatement(query.toString());

			//CONSTRUIM ELS VALORS DEL WHERE
			int k=1;
			for(PropietatJava prop : listCampsJava) {
			
				Object valorAtribut = this.getReadMethod(prop).invoke(objecte);
				
				if(valorAtribut!=null){
					//Aplica la transforamcio si fa falta
					valorAtribut = this.aplicaTransformacioJavaToOracle(prop, valorAtribut);
					
					sendValueToOracle(prop, ps, k, valorAtribut);
					k++;
				}
			}
			
			ps.executeUpdate();
		} 
		catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} 
		finally {
			close(ps);
		}
	}
	
	/**
	 * Implementació genèrica del UPDATE a partir d'un Bean de dades.
	 * 
	 * <p>Es pot sobrescriure el metode per modificar la implementacio
	 * o evitar (capar) que es realitzin UPDATE sobre la taula.
	 *  
	 * @param id Bean de dades.
	 * @throws Exception
	 */
	@Override
	public void update(O objecte) throws Exception {
		
		if(this.listCampsPK==null || this.listCampsPK.size()==0) {
			throw new Exception("ATENCIO! No es pot fer UPDATE d'una taula sense PK definit.");
		}
		
		//Exclou les PK dels SETS
		List<PropietatJava> listSetsUpdate = new ArrayList<PropietatJava>();  
		for(PropietatJava prop : listCampsJava) {
			if(!this.listCampsPK.contains(prop)) {
				listSetsUpdate.add(prop);
			}
		}
		
		//Construim el INSERT SQL
		StringBuffer query = new StringBuffer();
		query.append(" UPDATE ");
		query.append(this.nomTaula);
		query.append(" SET ");
		int i=1;
		
		for(PropietatJava prop : listSetsUpdate) {
			if(!this.listCampsPK.contains(prop)) {
				query.append(prop.getNomColumna());
				query.append(" = ");
				query.append(prop.getColumnaWhereValor());
				
				if(i < listSetsUpdate.size()) {
					query.append(",");
				}
				i++;
			}
		}
		
		//CONSTRUIM ELS  VALUES		
		query.append(" WHERE (1=1)");
		for(PropietatJava prop : this.listCampsPK) {

			query.append(" AND ");
			query.append(prop.getNomColumna());
			query.append(" = ");
			query.append(prop.getColumnaWhereValor());
		}
		
		log.debug("ejecutando... " + query.toString());
		PreparedStatement ps = null;
		try {
			Connection connnection = this.gestorDB.getConnection();
			ps = connnection.prepareStatement(query.toString());

			//CONSTRUIM ELS VALORS DEL WHERE
			List <PropietatJava> list = new ArrayList<PropietatJava>();
			list.addAll(listSetsUpdate);
			list.addAll(this.listCampsPK);
			
			int k=1;
			for(PropietatJava prop : list) {
				Object valorAtribut = this.getReadMethod(prop).invoke(objecte);
								
				//Aplica la transforamcio si fa falta
				valorAtribut = this.aplicaTransformacioJavaToOracle(prop, valorAtribut);
					
				sendValueToOracle(prop, ps, k, valorAtribut);
				k++;				
			}
			
			ps.executeUpdate();
		} 
		catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} 
		finally {
			close(ps);
		}
	}
	
	/**
	 * Inicialitza les PropietatsJava que s'excluiran de les operacions Java-Oracle.
	 * 
	 * <p>Cal cridar el metode <code>this.addPropietatJavaExcluida("nomJava")</code>. 
	 * 
	 * <p>ATENCIO! No s'hauria de fer servir mai, però si el bean té alguna propietat
	 * que la taula Oracle no té (no hauria de passar mai, però vaja) <b>cal sobrescriure
	 * aquest metode</b>.
	 * 
	 * <p>Exemple:
	 *  <pre>this.addPropietatJavaExcluida("fichero");</pre>
	 *
	 * @see #addPropietatJavaExcluida(String)
	 */
	protected void initPropietatsJavaExcluides() {
		//Per defecte no fa res
	}
	
	/**
	 * Crea i retorna una nova <code>PropietatJava</code> a partir del nom de la propietat 
	 * Java i el nom de la columna (camp) equivalent en la taula Oracle.
	 * 
	 * <p>Per tal de facilitar la definció de la <code>PropietatJava</code> aquest metode
	 * també retorna la <code>PropietatJava</code> creada.
	 * 
	 * @param nom de la propietat Java en el Bean.
	 * @param nomColumna corresponent en la taula Oracle.  
	 * @return la nova <code>PropietatJava</code> creada.
	 * 
	 * @see PropietatJava
	 */
	protected PropietatJava newPropietatJava(String nom, String nomColumna) {
		PropietatJava prop = new PropietatJava(nom, nomColumna);
		this.mapJavaToOracle.put(prop.getNom(), prop);
		
		return prop;
	}

	/**
	 * Afegeix un nom de propietat Java a la llsita de propietats excluides.
	 * <p>Les propietats excluides no es tracten , és a dir, com si no existissin en el Bean.
	 * 
	 * @param nom de la propietat que es troba en el Bean i que es vol excluir.  
	 */
	protected void addPropietatJavaExcluida(String nom) {
		if(nom!=null && !this.listPropietatsJavaExcluides.contains(nom)) {
			this.listPropietatsJavaExcluides.add(nom);
		}
	}

	
	/**
	 * Crea un nou <code>CampOrderBy</code> i l'afegeix a la llista de camps que
	 * han de formar part del ORDER BY de la SELECT.
	 * 
	 * <p> Es opcional i es té en compte l'ordre en què les columnes s'afegeixen 
	 * a aquesta llista.
	 * 
	 * 
	 * @param nom del camp de la taula
	 * @param descendent Sentit de l'ordenació: ASC=false i DESC=true 
	 * 
	 * @see CampOrderBy
	 * 
	 */
	protected void addCampOrderBy(String nom, boolean descendent) {
		CampOrderBy o = new CampOrderBy(nom, descendent); 
		this.listCampsOrderBy.add(o);
	}
	
	protected void addCampPK(String campPK) {
		PropietatJava prop = trobaPropietatPerCamp(campPK);
		if(prop!=null) {
			addPropietatPK(prop);
		}
	}
	
	protected void addPropietatPK(PropietatJava prop) {
		this.listCampsPK.add(prop);
	}
	
	protected void initListCampsPK() {
		List<String> columnesPK = this.consultaPKsTaula(this.nomTaula);
		if(columnesPK!=null) {
			for(String campPK : columnesPK) {
				this.addCampPK(campPK);
			}
		}
	}
	
	protected PropietatJava getPropietatJava(String nom) {
		return this.mapJavaToOracle.get(nom);
	}
	
	private Method getReadMethod(PropietatJava prop) throws Exception {
		Method method = prop.getPropietat().getReadMethod();
		
		if(method==null) {
			throw new Exception("No existe metodo de lectura para la propiedad " + prop.getNom());
		}
		return method;
	}
	
	private Method getWriteMethod(PropietatJava prop) throws Exception {
		Method method = prop.getPropietat().getWriteMethod();
		
		if(method==null) {
			throw new Exception("No existe metodo de escritura para la propiedad " + prop.getNom());
		}
		return method;
	}

	
	private void initRestaPropietatsJava() throws Exception {
		
		
		O inst = this.getBeanInstance();
		BeanInfo sourceInfo = Introspector.getBeanInfo(inst.getClass());  
		PropertyDescriptor[] sourceDescriptors = sourceInfo.getPropertyDescriptors();
		
		listCampsJava = new ArrayList<PropietatJava>();
		for(PropertyDescriptor propietat : sourceDescriptors) {
			if(propietat.getReadMethod()==null) continue;
			
			String nomCampJava = propietat.getName();
			if (!listPropietatsJavaExcluides.contains(nomCampJava)) {
				PropietatJava propietatJava = mapJavaToOracle.get(nomCampJava);
				//Si no s'ha definit una traducció de nom Java->Oracle vol dir que es el mateix nom en Java i Oracle
				if(propietatJava==null) {
					propietatJava = this.newPropietatJava(nomCampJava, nomCampJava);
				}
				
				propietatJava.setPropietat(propietat);
				listCampsJava.add(propietatJava);
			}
		}
		
		if(listCampsJava.size() == 0) {
			throw new Exception("ATENCIO! Estas utilitzant un Bean que no te propietats, piltrafilla!");
		}
	}
	
	private void sendValueToOracle(PropietatJava prop, 
			PreparedStatement ps, 
			int posValue, 
			Object value) throws Exception {
		
		if(value!=null) {
			if(Date.class.isAssignableFrom(prop.getPropietat().getPropertyType())) {
			
				Date dt = (Date) value;
				Timestamp tm = new Timestamp(dt.getTime());
				
				String str = tm.toString();
				if(str.contains("00:00:00")) {
					ps.setObject(posValue, value);
				}
				else {
					ps.setTimestamp(posValue, tm);
				}
			}
			else {
				ps.setObject(posValue, value);
			}
		}
		else {
			
			ps.setNull(posValue, prop.getJavaSqlType());
		}
	}
	
	private Object receiveValueFromOracle(PropietatJava prop, ResultSet rs) throws Exception {
		Object valorColumna=null;
		if(Date.class.isAssignableFrom(prop.getPropietat().getPropertyType())) {
			valorColumna = rs.getTimestamp(prop.getNomColumna());
		}
		else {
			valorColumna = rs.getObject(prop.getNomColumna());
		}
		
		return valorColumna;
	}
	
	private Object aplicaTransformacioOracleToJava(PropietatJava prop, Object valorPrevi) {
		if(valorPrevi==null) return null;
		
		//Busca si aplica els convertidors estandrd
		JavaOracleConversor conv = aplicaConvertidor(prop, valorPrevi);
		if(conv!=null) {
			return conv.OracleToJava(valorPrevi);
		}
		
		return valorPrevi;
	}
	
	
	private Object aplicaTransformacioJavaToOracle(PropietatJava prop, Object valorPrevi) {
		//Busca si aplica els convertidors estandrd
		JavaOracleConversor conv = aplicaConvertidor(prop, valorPrevi);
		if(conv!=null) {
			return conv.JavaToOracle(valorPrevi);
		}
		
		return valorPrevi;
	}
	
	private JavaOracleConversor aplicaConvertidor(PropietatJava prop, Object valorPrevi) {
		JavaOracleConversor conv = prop.getJavaOracleConversor();
		if(conv==null) {
			Class<?> cl = prop.getPropietat().getPropertyType();
			if(cl.equals(Boolean.class)) {
				if("0".equals(valorPrevi) || "1".equals(valorPrevi)) {
					conv = prop.string01ToBool;
				}
				else if ("S".equals(valorPrevi) || "N".equals(valorPrevi)) {
					conv = prop.stringSNToBool;
				}
				prop.setJavaSqlType(java.sql.Types.BOOLEAN);
			}
			else if (cl.equals(Integer.class)) {
				conv = prop.toInteger;
				prop.setJavaSqlType(java.sql.Types.INTEGER);
			}
			else if (cl.equals(Long.class)) {
				conv = prop.toLong;
				prop.setJavaSqlType(java.sql.Types.NUMERIC);
			}
		}
		prop.setJavaOracleConversor(conv);
		
		return conv;
	}
	
	private O getBeanInstance() throws Exception {
		ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
		
		Type typeGeneric= type.getActualTypeArguments()[0];
		String nomClase = typeGeneric.toString().replaceAll("class ", "");
		Class<?> clase = Class.forName(nomClase);
		
		@SuppressWarnings("unchecked")
		O inst = (O)clase.newInstance();
		
		return inst;
	}	
	
	private List<String> consultaPKsTaula(String taula) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT cc.column_name ");
		sql.append("FROM all_cons_columns cc, all_constraints c ");
		sql.append("WHERE c.constraint_type = 'P' ");
		sql.append("AND c.owner = cc.owner ");
		sql.append("AND c.table_name = cc.table_name ");
		sql.append("AND c.constraint_name = cc.constraint_name ");
		sql.append("AND upper(cc.table_name) = upper(?)");

		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			Connection connection = this.gestorDB.getConnection();
			ps = connection.prepareStatement(sql.toString());
			
			ps.setObject(1, taula);
			rs = ps.executeQuery();
			
			List<String> result = new ArrayList<String>();
			while(rs.next()) {
				result.add(rs.getString("column_name"));
			}
			return result;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			close(ps, rs);
		}
		return null;
	}
	
	private PropietatJava trobaPropietatPerCamp(String campOracle) {
		for(PropietatJava prop : this.mapJavaToOracle.values()) {
			if(prop.getNomColumna().equalsIgnoreCase(campOracle)) {
				return prop;
			}
		}
		return null;
	}

	
	/**
	 * Les PropietatsJava ens permeten definir:
	 * <ol>
	 * 
	 * <li>La relació entre propietats del bean Java i les columnes corresponents 
	 * de la taula Oracle. <b>Només cal indicar-ho si els noms no coincideixen, 
	 * sino ja s'aplica automàticament.</b>
	 * 
	 * <li>El <code>JavaOracleConversor</code>, que permet definir les trasnformacions 
	 * de la info quan llegim o escribim a Oracle.
	 * 
	 * <li>Conté l'objecte <code>PropertyDescriptor</code> que representa la 
	 * propietat Java (que conté el <code>Method</code> de reflection).
	 * 
	 * <li>Permet definir (per casos molt especials com les columnes de idcentre 
	 * amb centrolpad) la part de la SELECT i WHERE corresponent a una columna.
	 * </ol>
	 * 
	 * @author cafarre
	 *
	 * @see JavaOracleConversor
	 * @see PropertyDescriptor
	 */
	protected class PropietatJava {
		private String nom;
		private String nomColumna;
		private String columnaSelect;
		private String columnaWhereCriteri;
		private String columnaWhereValor;
		private int javaSqlType = java.sql.Types.VARCHAR;
		private JavaOracleConversor javaOracleConversor;
		private PropertyDescriptor propietat;
		
		
		JavaOracleConversor stringSNToBool = new JavaOracleConversor() {
			@Override
			public Object JavaToOracle(Object obj) {
				String s = BooleanToString((Boolean)obj);
				return s;
			}

			@Override
			public Object OracleToJava(Object obj) {
				Boolean b = stringToBoolean((String)obj);
				return b;
			}
		};
		
		JavaOracleConversor string01ToBool = new JavaOracleConversor() {
			@Override
			public Object OracleToJava(Object obj) {
				Boolean b = stringToBoolean((String)obj,"1", "0");
				return b;
			}

			@Override
			public Object JavaToOracle(Object obj) {
				String s = BooleanToString((Boolean)obj, "1", "0");
				return s;
			}
		}; 
		
		JavaOracleConversor toInteger = new JavaOracleConversor() {
			@Override
			public Object OracleToJava(Object obj) {
				Integer i = this.toInteger(obj);
				return i;
			}

			@Override
			public Object JavaToOracle(Object obj) {
				return obj;
			}
		}; 

		JavaOracleConversor toLong = new JavaOracleConversor() {
			@Override
			public Object JavaToOracle(Object obj) {
				return obj;
			}

			@Override
			public Object OracleToJava(Object obj) {
				Long i = this.toLong(obj);
				return i;
			}
		}; 
		
		public PropietatJava(String nom, String nomColumna) {
			this.nom=nom;
			this.nomColumna=nomColumna;
		}
		
		public PropertyDescriptor getPropietat() {
			return propietat;
		}

		public void setPropietat(PropertyDescriptor propietat) {
			this.propietat = propietat;
		}


		public JavaOracleConversor getJavaOracleConversor() {
			return javaOracleConversor;
		}

		public void setJavaOracleConversor(JavaOracleConversor javaOracleConversor) {
			this.javaOracleConversor = javaOracleConversor;
		}

		public String getNom() {
			return nom;
		}
		public void setNom(String nom) {
			this.nom = nom;
		}
		public String getNomColumna() {
			return nomColumna;
		}
		public void setNomColumna(String nomColumna) {
			this.nomColumna = nomColumna;
		}

		public String getColumnaSelect() {
			if(columnaSelect==null) {
				return this.nomColumna;
			}
			return columnaSelect;
		}

		public void setColumnaSelect(String columnaSelect) {
			this.columnaSelect = columnaSelect;
		}

		public String getColumnaWhereCriteri() {
			if(columnaWhereCriteri==null) {
				return this.nomColumna;
			}
			return columnaWhereCriteri;
		}

		public void setColumnaWhereCriteri(String columnaWhereCriteri) {
			this.columnaWhereCriteri = columnaWhereCriteri;
		}

		public String getColumnaWhereValor() {
			if(columnaWhereValor==null) {
				return "?";
			}
			return columnaWhereValor;
		}

		public void setColumnaWhereValor(String columnaWhereValor) {
			this.columnaWhereValor = columnaWhereValor;
		}

		public int getJavaSqlType() {
			return javaSqlType;
		}

		public void setJavaSqlType(int javaSqlType) {
			this.javaSqlType = javaSqlType;
		}
		
		
	}

	/**
	 * Permet definir les conversions tant per lectura com escriptura a Oracle. 
	 * <p>És opcional i per els Numeros, Dates y Booleans (0/1 i S/N) s'apliquen automaticament 
	 * sense necesitat d'indicar-ho.
	 * 
	 * @author cafarre
	 *
	 */
	protected abstract class JavaOracleConversor {
		public abstract Object OracleToJava(Object obj);
		public abstract Object JavaToOracle(Object obj);
		
		protected final Integer toInteger(Object obj) {
			if(obj instanceof Integer) {
				return (Integer) obj;
			}
			
			String str = null;
			if(obj instanceof String) {
				str = (String) obj;
			}
			else {
				str = obj.toString();
			}
			
			Integer i = Integer.parseInt(str);
			return i;
		}
		
		protected final Long toLong(Object obj) {
			if(obj instanceof Long) {
				return (Long) obj;
			}
			
			String str = null;
			if(obj instanceof String) {
				str = (String) obj;
			}
			else {
				str = obj.toString();
			}
			
			Long i = Long.parseLong(str);
			return i;
		}
	}
	
	/**
	 * Representa una camp Order By de la SELECT, on es pot indicar el nom del
	 * camp Oracle i l'ordenació ASC o DESC.
	 * 
	 * @author cafarre
	 *
	 */
	protected class CampOrderBy {
		private String nom;
		private boolean descendent;
		
		public CampOrderBy(String nom) {
			this(nom, false);
		}

		public CampOrderBy(String nom, boolean descendent) {
			this.nom=nom;
			this.descendent=descendent;
		}

		public boolean isDescendent() {
			return descendent;
		}

		public void setDescendent(boolean descendent) {
			this.descendent = descendent;
		}

		public String getNom() {
			return nom;
		}

		public void setNom(String nom) {
			this.nom = nom;
		}
		
		public String getSQL() {
			if(this.descendent) {
				return this.nom + " DESC";
			}
			else {
				return this.nom;
			}
		}
	}
}
