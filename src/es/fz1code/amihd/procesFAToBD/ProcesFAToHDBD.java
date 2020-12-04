package es.fz1code.amihd.procesFAToBD;

import java.util.List;

import es.fz1code.amihd.comu.dao.GestorDB;
import es.fz1code.amihd.comu.utils.Logger;
import es.fz1code.amihd.comu.utils.PrintBeanToLog;
import es.fz1code.amihd.videotecahd.bean.Video;
import es.fz1code.amihd.videotecahd.dao.DaoVideos;


public class ProcesFAToHDBD {

	private static Logger log = new Logger(ProcesFAToHDBD.class);
	private GestorDB gest = null;
	private FAtoJava  lector = null;
	private DaoVideos daoV = null;
	
	private ProcesFAToHDBD() throws Exception {
		gest = new GestorDB();
		daoV = new DaoVideos(gest);
		lector = new FAToJavaJsoup();
	}
	
	public static void main(String[] args) {
		
		log.info("INICI - Procés de descarrega d'informació de FA.");
		long init = System.currentTimeMillis();
		
		try {
			ProcesFAToHDBD proc  = new ProcesFAToHDBD();
			proc.processar();
			
			long fin = System.currentTimeMillis();
			long temps = fin - init;
			
			log.info("FI - Procés de descarrega d'informació de FA amb " + temps + "ms.");			
		} catch(Exception ex) {
			log.error("ERROR en el procés.", ex);
		}
	}
	
	private void processar() {
		/*
		 * v- Conectar a la BBDD HD
		 * v- Per cada pelicula amb URL FA i flag pendent info
		 * v-- Obtenir la info de FA
		 * 
		 * --- Conectar a FA amb HttpClient i descarregar el html de la peli
		 * v--- Aplicar el scraper i obtenir el xml de dades de la peli
		 * v--- Omplir el DTO o Bean que modela la peli en Java
		 * v--- Obtenir la caratula amb HttpClient
		 * v--- Guardar caratula en disc
		 * v--- Actualitzar el bean amb la nova ruta de la IMG
		 * 
		 * v-- Actualitzar la info de FA a BBDD 
		 * -- En cas de algun error grabar la tra�a amb tota la info posible en una nova taula de logs
		 *  
		 */
		try {
			//Obtenir les pelicules pendents de descargar info de FA
			Video criteris = new Video();
			criteris.setPendentInfoFA(true);
			List<Video> videos = daoV.select(criteris);
			int numVideos=0;
			int numVideosErr=0;
			if(videos != null) {
				for(Video vid: videos) {
					//Nomes tenim en compte els videos que tenen la URL informada
					if(vid.getUrlFA()!=null && vid.getUrlFA().length() > 20) {
						try {
							this.tractaVideo(vid);
							numVideos++;
						}
						catch (Exception ex) {
							numVideosErr++;
						}
						
					}
				}
			}
			log.info("PROCES ACABAT AMB " + numVideos + " OBTINGUTS i " + numVideosErr + " ERRORS.");
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
			
			//Comprova que no existeixi una altre pelicula amb aquest nom, en tal cas afegeix l'any en el nom de la peli
			Video crit = new Video();
			crit.setNom(info.getNom());
			List<Video> listVideos = daoV.select(crit);
			if(listVideos!=null && listVideos.size() > 0) {
				boolean repe = false;
				for(Video video : listVideos) {
					if(!video.getId().equals(vid.getId())) {
						repe=true;
						break;
					}
				}
				
				if(repe) {
					String newNom = info.getNom() + " (" + info.getAnyo() + ")";
					info.setNom(newNom);
				}
			}

			info = lector.obtenirCaratula(info);
			
			vid.setNom(info.getNom());
			vid.setRutaCaratula(info.getRutaDiscCaratula());
			vid.setListGeneres(info.getGeneres());
			vid.setListTopics(info.getTopics());
			vid.setSinopsi(info.getSinopsi());
			vid.setDuracio(info.getDuracio());
			vid.setNotaFA(info.getNotaFA());
			vid.setUrlFA(info.getUrlFA());
			vid.setPendentInfoFA(false);
			vid.setAnyo(info.getAnyo());
			vid.setDirector(info.getDirector());
			vid.setNomOriginal(info.getNomOriginal());
			vid.setListRepart(info.getRepart());
			
			log.debug(PrintBeanToLog.printToLog(vid));
			
			daoV.update(vid);
			gest.endTransaction();
			
			long fin = System.currentTimeMillis();
			long temps = fin - init;
			log.info("FI - Pelicula " + vid.getNom() + " obtinguda OK amb " + temps + "ms.");
		}
		catch (Exception ex) {
			log.error("ERROR al procesar video: " + vid.getUrlFA() + " nom: " + vid.getNom(), ex);
			throw ex;
		}		
	}
}
