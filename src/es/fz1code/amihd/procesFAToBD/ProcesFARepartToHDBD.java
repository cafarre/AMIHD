package es.fz1code.amihd.procesFAToBD;

import java.util.List;

import es.fz1code.amihd.comu.dao.GestorDB;
import es.fz1code.amihd.comu.utils.Logger;
import es.fz1code.amihd.comu.utils.PrintBeanToLog;
import es.fz1code.amihd.videotecahd.bean.Video;
import es.fz1code.amihd.videotecahd.dao.DaoVideos;


public class ProcesFARepartToHDBD {

	private static Logger log = new Logger(ProcesFARepartToHDBD.class);
	private GestorDB gest = null;
	private FAtoJava  lector = null;
	private DaoVideos daoV = null;
	
	private ProcesFARepartToHDBD() throws Exception {
		gest = new GestorDB();
		daoV = new DaoVideos(gest);
		lector = new FAToJavaJsoup();
	}
	
	public static void main(String[] args) {
		
		log.info("INICI - Procés de descarrega d'informació de FA.");
		long init = System.currentTimeMillis();
		
		try {
			ProcesFARepartToHDBD proc  = new ProcesFARepartToHDBD();
			proc.processar();
			
			long fin = System.currentTimeMillis();
			long temps = fin - init;
			
			log.info("FI - Procés de descarrega d'informació de FA amb " + temps + "ms.");			
		} catch(Exception ex) {
			log.error("ERROR en el procés.", ex);
		}
	}
	
	private void processar() {

		try {
			Video criteris = new Video();
			List<Video> videos = daoV.select(criteris);
			
			int numVideos=0;
			int numVideosUpdated=0;
			int numVideosErr=0;
			if(videos != null) {
				for(Video vid: videos) {
					//Nomes tenim en compte els videos que tenen la URL informada i no tenen repart
					if(vid.getUrlFA()!=null && vid.getUrlFA().length() > 20 && (vid.getRepart()==null || vid.getRepart().length()==0)) {
						try {
							this.tractaVideo(vid);
							numVideosUpdated++;
						}
						catch (Exception ex) {
							numVideosErr++;
						}
						
					}
					numVideos++;
				}
			}
			log.info("PROCES ACABAT AMB " + numVideosUpdated + " ACTUALITZATS de (" + numVideos + ") i " + numVideosErr + " ERRORS.");
		}
		catch (Exception ex) {
			log.error(ex);
		}
		finally {
			try {
				if(gest!=null) gest.closeConnection();
			}catch(Exception ex) {
				log.error("Error al tancar conexio BBDD.",ex);
			}
		}
	}
	
	private void tractaVideo(Video vid) throws Exception{
		try {
			log.info("INICI - Descarga info pelicula " + vid.getNomFitxer() + ": " + vid.getUrlFA());
			long init = System.currentTimeMillis();
			
			InfoPeliFA info = lector.obtenirVideo(vid.getUrlFA());
			log.debug(PrintBeanToLog.printToLog(info));
			
			vid.setListGeneres(info.getGeneres());
			vid.setListTopics(info.getTopics());
			vid.setSinopsi(info.getSinopsi());
			vid.setDuracio(info.getDuracio());
			vid.setNotaFA(info.getNotaFA());
			vid.setAnyo(info.getAnyo());
			vid.setDirector(info.getDirector());
			vid.setNomOriginal(info.getNomOriginal());
			vid.setListRepart(info.getRepart());
			
			log.debug(PrintBeanToLog.printToLog(vid));
			
			daoV.update(vid);
			gest.endTransaction();
			
			long fin = System.currentTimeMillis();
			long temps = fin - init;
			log.info("FI - Pelicula " + vid.getNom() + " updated OK amb " + temps + "ms.");
		}
		catch (Exception ex) {
			log.error("ERROR al procesar video: " + vid.getUrlFA() + " nom: " + vid.getNom(), ex);
			throw ex;
		}		
	}
}
