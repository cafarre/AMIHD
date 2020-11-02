package es.fz1code.amihd.procesMKVToBD;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import es.fz1code.amihd.comu.dao.GestorDB;
import es.fz1code.amihd.comu.utils.FileVideos;
import es.fz1code.amihd.comu.utils.Logger;
import es.fz1code.amihd.comu.utils.Utilities6;
import es.fz1code.amihd.videotecahd.bean.Video;
import es.fz1code.amihd.videotecahd.dao.DaoVideos;

public class ProcesMKVToHDBD {
	
	private static Logger log = new Logger(ProcesMKVToHDBD.class);
	private GestorDB gest = null;
	private DaoVideos daoV = null;
	
	private ProcesMKVToHDBD() throws Exception {
		gest = new GestorDB();
		daoV = new DaoVideos(gest);
	}
	
	public static void main(String[] args) {

		if(args.length==0) {
			log.error("Parametres incorrectes. Cal indicar la ruta de la carpeta de MKV's.");
			return;
		}
		
		long init = System.currentTimeMillis();
		log.info("INICI - Procés de carrega de pelicules a la BBDD.");
		try {
			
			ProcesMKVToHDBD proc  = new ProcesMKVToHDBD();
			proc.processar(args[0]);
			//proc.processar("E://Pelicules HD");
			//proc.processar("F://Pelicules HD");
			//proc.processar("G://PeliculesHD");
			
			long fin = System.currentTimeMillis();
			long temps = fin - init;
			
			log.info("FI - Proc�s de carrega de pelicules a la BBDD amb " + temps + "ms.");			
		} catch(Exception ex) {
			log.error("ERROR en el proc�s.", ex);
		}
	}
	
	private void processar(String ruta) {
		log.info("Tractant RUTA: " + ruta);

		
		/*
		 * - Conectar a la carpeta Incoming Torrents
		 * - Per cada fitxer mkv, avi o mp4:
		 * -- Conectar a la BBDD HD 
		 * -- Buscar si ja hi ha un video que tingui aquell nom de fitxer (noms fitxer en taula apart!)
		 * -- Si no hi es Crear el video amb: ID i nom fitxer
		 * -- En cas de algun error grabar la tra�a amb tota la info posible en una nova taula de logs
		 * 
		 * - (Opcional) 
		 * - Recorrer els videos que nomes tenen el nom del fitxer i:
		 * -- Cercar en FA quines pelis podrien ser
		 * -- Guardar cada resultat de la cerca amb les url de les pelis de FA
		 * -- Fer una pantalla per poder informar i validar manualment les url de FA obtingudes oposarla a m�
		 *  
		 */
		try {
			//TODO: parametrotzar
			List<File> listFileVideos = FileVideos.obtenirVideosCarpeta(ruta);
			
			//List<File> listFileVideos = FileVideos.obtenirVideosCarpeta("//192.168.2.101/Videoteca_1/Videoteca HD N�1");
			//List<File> listFileVideos = FileVideos.obtenirVideosCarpeta("//192.168.2.101/hdteca_v2/Pelicules HD/Pendents de Veure");
			//List<File> listFileVideos = FileVideos.obtenirVideosCarpeta("//192.168.2.101/hdteca_v2/Pelicules HD/Videoteca HD n�2");
			//List<File> listFileVideos = FileVideos.obtenirVideosCarpeta("//192.168.2.101/hdteca_v2/Pelicules HD/Per Determinar si Borrar");

			//List<File> listFileVideos = FileVideos.obtenirVideosCarpeta("D://Incoming Torrents");

			int numVideos = 0;
			if(listFileVideos!=null) {
				for(File fileVideo : listFileVideos) {
					log.info("Tractant fitxer: " + fileVideo.getName());
					
					/*String nomPeli = getNomVideo(fileVideo);
					log.info("NOM PELICULA: " + nomPeli);
					*/
					
					//Obtenir les pelicules pendents de descargar info de FA
					Video nouVideo = new Video();
					nouVideo.setNomFitxer(fileVideo.getName());
					
					List<Video> videos = daoV.select(nouVideo);
					if(videos == null || videos.size()==0) {
						if(nouVideo.getNomFitxer().contains("1080p")) {
							nouVideo.setDefinicio("1080p");
						}
						else if(nouVideo.getNomFitxer().contains("1080i")) {
							nouVideo.setDefinicio("1080i");
						}
 						
						daoV.insert(nouVideo);
						numVideos++;
						log.info("NOU VIDEO INSERTAT: " + fileVideo.getName());
					}
				}
			}
			log.info("PROCES ACABAT AMB " + numVideos + " NOUS INSERTATS.");
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
	
	@SuppressWarnings("unused")
	private String getNomVideo(File video) throws Exception {
		Runtime rt = Runtime.getRuntime();
		String[] commands = new String[]{"C://Archivos de programa/MediaInfo/CLI/mediainfo", "--Inform=General;%Title%", video.getAbsolutePath()};
		Process proc = rt.exec(commands);
		
		InputStream input = proc.getInputStream();
		String result = Utilities6.streamToString(input,"UTF-8");
		
		String error = Utilities6.streamToString(proc.getErrorStream(),"UTF-8");
		int exitCode = proc.waitFor();
		log.debug("Exit value: " + exitCode);
		if(exitCode==0) {
			return result.trim();
		}
		else {
			log.error("Alguna cosa no ha anat be al llegir el nom de la peli: " + error);
			return null;
		}
	}
}
