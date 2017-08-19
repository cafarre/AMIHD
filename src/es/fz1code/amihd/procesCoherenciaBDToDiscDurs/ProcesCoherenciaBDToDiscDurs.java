package es.fz1code.amihd.procesCoherenciaBDToDiscDurs;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.fz1code.amihd.comu.dao.GestorDB;
import es.fz1code.amihd.comu.utils.FileVideos;
import es.fz1code.amihd.comu.utils.Logger;
import es.fz1code.amihd.comu.utils.Utilities6;
import es.fz1code.amihd.videotecahd.bean.Video;
import es.fz1code.amihd.videotecahd.dao.DaoVideos;

public class ProcesCoherenciaBDToDiscDurs {
	
	private static Logger log = new Logger(ProcesCoherenciaBDToDiscDurs.class);
	private GestorDB gest = null;
	private DaoVideos daoV = null;
	
	private ProcesCoherenciaBDToDiscDurs() throws Exception {
		gest = new GestorDB();
		daoV = new DaoVideos(gest);
	}
	
	public static void main(String[] args) {
		
		log.info("INICI - Proc�s de cerca de ubicacions de pelicules BBDD.");
		long init = System.currentTimeMillis();
		
		try {
			ProcesCoherenciaBDToDiscDurs proc  = new ProcesCoherenciaBDToDiscDurs();

			//1:VIDEOTECA HD 1
			//proc.processar("F:///Pelicules HD", 1, false);
			
			//2:VIDEOTECA HD 2
			//proc.processar("G://Pelicules HD", 2,false);
			
			//3:HDTECA NENS
			proc.processar("E://PeliculesHD", 3, false);
			
			//4:HD WD TEMPORAL 1
			//proc.processar("E://Pelicules HD", 4, false);
			
			//99:HD INCOMING TORRENT
			//proc.processar("C://Server Media//Incoming Torrents", 99,false);
			//proc.processar("C://Server Media//Pelicules HD//Per passar a HD Teca n�2", 99);
			
			
			long fin = System.currentTimeMillis();
			long temps = fin - init;
			
			log.info("FI - Proc�s de cerca de ubicacions de pelicules BBDD amb " + temps + "ms.");			
		} catch(Exception ex) {
			log.error("ERROR en el proc�s.", ex);
		}
	}
	
	private void processar(String rutaMKs, Integer codiDiscDurBD, Boolean forceAsinacio) {

		try {
			Video crit = new Video();
			List<Video> listVideos = daoV.select(crit);
			
			List<File> listFileVideos = FileVideos.obtenirVideosCarpeta(rutaMKs);
			//List<File> listImatgesVideos = FileVideos.obtenirImatgesCarpeta(rutaMKs);
			log.info("Tractant fitxers de la ruta: " + rutaMKs);
			
			Map<String, Video> map = new HashMap<String, Video>();
			if(listVideos != null) {
				for(Video video : listVideos) {
					if(video.getCodiEstat().intValue() < 7 ) {
						map.put(video.getNomFitxer(), video);
					}
				}
			}
			
			int numVideos = 0;
			int numVideosNous = 0;
			if(listFileVideos != null) {
				for(File file : listFileVideos) {
					Video video = map.get(file.getName());
					if(video!=null && video.getNom()!=null) {
						log.info("Tractant Video: " + video.getNom());
						
						//Carpeta actual
						String carpetaActual = file.getParent();
						int pos=carpetaActual.indexOf(":");
						if(pos!=-1) {
							carpetaActual = carpetaActual.substring(pos+1);
						}

						boolean ferUpdate=false;
						
						//CAS video no ubicat mai o b� ubicat en un altre disc dur menys prioritari (com mes petit id disc dur mes prioritari)
						if(video.getCodiDiscDur()==null || codiDiscDurBD < video.getCodiDiscDur()
								|| (forceAsinacio && codiDiscDurBD > video.getCodiDiscDur())) {
							ferUpdate=true;
							
							//Estableix ubicaci�
							video.setCodiDiscDur(codiDiscDurBD);
							video.setNomCarpeta(carpetaActual);
							
							//Posar fotografia
							posarFoto(video, file);

							if(video.getCodiDiscDur()!=null && codiDiscDurBD < video.getCodiDiscDur()) {
								log.warn("El fitxer " + video.getNomFitxer() + " s'ha reubicat a un altre lloc de: " + video.getCodiDiscDur() + " a " + codiDiscDurBD );
							}
							
							log.info("Fitxer ubicat a: " + file.getPath());
							
							numVideosNous++;
						}
						//Cas mateix disc dur
						else if(video.getCodiDiscDur().equals(codiDiscDurBD)) {
							log.debug("El fitxer " + video.getNomFitxer() + " ja est� ubicat a aquest mateix disc dur.");

							//Actualitza la carpeta per actualitzar els casos en que carpeta no informada o canvis de carpeta
							if(!carpetaActual.equalsIgnoreCase(video.getNomCarpeta())) {
								log.info("Carpeta actualitzada de: " + video.getNomCarpeta() + " a: " + carpetaActual);
								ferUpdate=true;
								video.setNomCarpeta(carpetaActual);
							}
							
							if (!existImatge(file)) {
								//Posar fotografia
								posarFoto(video, file);
							}
						}
						else {
							log.warn("El fitxer " + video.getNomFitxer() + " ja est� ubicat a un altre lloc: " + video.getCodiDiscDur());
						}

						if(ferUpdate) {
							daoV.update(video);
						}
						
						numVideos++;
					}
					else {
						log.warn("Fitxer NO trobat a la BBDD: " + file.getName());
					}
				}
			}


			log.info("PROCES ACABAT AMB " + numVideos + " de " + listFileVideos.size() + " UBICATS (" + numVideosNous + " nous).");
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
	
	private boolean existImatge(File file) {
		
		String nomImg = file.getAbsolutePath().replaceAll(".mkv", ".jpg");
		nomImg = nomImg.replaceAll(".mp4", ".jpg");
		nomImg = nomImg.replaceAll(".avi", ".jpg");
		
		File fileImg = new File(nomImg);
		
		return fileImg.exists();
	}
	
	private void posarFoto(Video video, File file) {
		String nomVideo = file.getName();
		int lenCaratula = video.getRutaCaratula().length();
		String extensio = video.getRutaCaratula().substring(lenCaratula-3,lenCaratula);
		String nomFileFoto = nomVideo.substring(0, nomVideo.length()-3) + extensio;
		//String path = file.getParent().replace("\\", "/") + "/";
		//path = path.replaceFirst("/", "//");
		String path = file.getParent() + "\\";
		
		try {
			File fileFotoDesti = new File(path + nomFileFoto);
			if(!fileFotoDesti.exists()) {
				File fileFotoOrigen = new File(FileVideos.rutaImatges + video.getRutaCaratula());
				
				FileInputStream fis = new FileInputStream(fileFotoOrigen); 
				Utilities6.grabaFitxerDisc(fis, fileFotoDesti.getPath());
				log.info("Imatge CREADA: " + fileFotoDesti.getPath());
			}
			log.info("La imatge ja existia: " + fileFotoDesti.getPath());
		}
		catch(Exception ex) {
			log.error(ex);
		}
	}
}
