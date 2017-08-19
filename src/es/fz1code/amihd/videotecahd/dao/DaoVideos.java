package es.fz1code.amihd.videotecahd.dao;


import java.util.List;

import es.fz1code.amihd.comu.dao.DaoDinamic;
import es.fz1code.amihd.comu.dao.GestorDB;
import es.fz1code.amihd.videotecahd.bean.Video;

public class DaoVideos extends DaoDinamic<Video> {

	public DaoVideos(GestorDB gestorDB)
			throws Exception {
		
		super(gestorDB, "VIDEOS");
	}

	@Override
	protected void initListCampsOrderBy() {
	}

	@Override
	protected void initPropietatsJava() {
		this.newPropietatJava("nomOriginal", "nom_original");
		this.newPropietatJava("rutaCaratula", "ruta_caratula");
		this.newPropietatJava("merceNo", "merce_no");
		this.newPropietatJava("notaFA", "nota_fa");
		this.newPropietatJava("urlFA", "url_fa");
		this.newPropietatJava("codiEstat", "estat");
		this.newPropietatJava("codiPrioritat", "prioritat");
		this.newPropietatJava("codiDiscDur", "discdur");
		this.newPropietatJava("pendentInfoFA", "pendent_info_fa");
		this.newPropietatJava("nomFitxer", "nom_fitxer");
		this.newPropietatJava("nomCarpeta", "carpeta");
	}
	
	@Override
	protected void initListCampsPK() {
		PropietatJava prop = this.getPropietatJava("id");
		this.addPropietatPK(prop);
	}

	@Override
	public Video selectById(Object id) throws Exception {
		Video crit = new Video();
		crit.setId((Integer)id);
		
		List<Video> result = this.select(crit);
		if(result!=null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

}
