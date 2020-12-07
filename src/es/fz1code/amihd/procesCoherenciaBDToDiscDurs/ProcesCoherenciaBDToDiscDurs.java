package es.fz1code.amihd.procesCoherenciaBDToDiscDurs;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
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
		
		log.info("INICI - Procés de cerca de ubicacions de pelicules BBDD.");
		long init = System.currentTimeMillis();
		
		try {
			List<ResultatCoherencia> resultats = new ArrayList<>();
			
			//1:VIDEOTECA HD 1
			ProcesCoherenciaBDToDiscDurs proc  = new ProcesCoherenciaBDToDiscDurs();
			resultats.add(proc.processarDisc("H:/PeliculesHD", 1, true, true));
			
			//2:HD Pendents
			proc = new ProcesCoherenciaBDToDiscDurs();
			resultats.add(proc.processarDisc("F:/Pelicules HD", 2, false, true));
			
			//3:HD Temporal
			proc = new ProcesCoherenciaBDToDiscDurs();
			resultats.add(proc.processarDisc("I:/Pelicules HD", 3, false, true));
			
			//99:HD INCOMING TORRENT
			proc = new ProcesCoherenciaBDToDiscDurs();
			resultats.add(proc.processarDisc("D:/Server Media/Incoming Torrents", 99, false, false));
			
			//VIDEOS NO UBICATS NI ELIMINATS
			proc = new ProcesCoherenciaBDToDiscDurs();
			int numVideosBBDDSenseDisc = proc.processarBBDDSenseDisc();
			
			
			mostraInformeListResultats(resultats, numVideosBBDDSenseDisc);
			
			long fin = System.currentTimeMillis();
			long temps = fin - init;
			
			log.info("FI - Procés de cerca de ubicacions de pelicules BBDD amb " + temps + "ms.");			
		} catch(Exception ex) {
			log.error("ERROR en el procés.", ex);
		}
	}
	
	private static void mostraInformeListResultats(List<ResultatCoherencia> resultats, int numVideosBBDDSenseDisc) {
		ResultatCoherencia total = new ResultatCoherencia();
		total.titol = "[TOTALS]";
		total.numVideosBBDDSenseDisc=numVideosBBDDSenseDisc;

		StringBuilder head = new StringBuilder();
		head.append("**************************************************************************\n");
		head.append("*           RESULTAT PROCES COHERENCIA BBDD TO DISC DURS                 *\n");
		head.append("**************************************************************************\n");
		head.append("*                                                                        *\n");
		System.out.print(head.toString());
		
		for(ResultatCoherencia res : resultats) {
			mostraInformeResultat(res);
			
			total.numVideosAltreLloc += res.numVideosAltreLloc; 
			total.numVideosBBDD += res.numVideosBBDD;
			total.numVideosDisc += res.numVideosDisc;
			total.numVideosMatch += res.numVideosMatch;
			total.numVideosNoEnDisc += res.numVideosNoEnDisc;
			total.numVideosUbicats += res.numVideosUbicats;
			total.numVideosUpdatets += res.numVideosUpdatets;
		}
		
		mostraInformeResultat(total);
		
		StringBuilder foot = new StringBuilder();
		foot.append("*                                                                        *\n");
		foot.append("**************************************************************************\n");
		System.out.print(foot.toString());
	}

	private static void mostraInformeResultat(ResultatCoherencia res) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("*------------------------------------------------------------------------*\n");
		sb.append("*").append(strF(" RESULTAT DE: " + res.titol)).append("*\n");
		sb.append("*------------------------------------------------------------------------*\n");
		sb.append("*").append(strF(" RUTA                   : " + res.rutaMkvs)).append("*\n");
		sb.append("*").append(strF(" CODI HD                : " + res.codiDiscDur)).append("*\n");
		sb.append("*").append(strF(" FORCE ASSIGN           : " + res.forceAssignacio)).append("*\n");
		sb.append("*").append(strF(" POSAR FOTO             : " + res.posarFoto)).append("*\n");
		sb.append("*").append(strF(" NºVIDEOS BBDD EN HD    : " + res.numVideosBBDD)).append("*\n");
		sb.append("*").append(strF(" NºVIDEOS DISC          : " + res.numVideosDisc)).append("*\n");
		sb.append("*").append(strF(" NºVIDEOS MATCH DISC-BD : " + res.numVideosMatch)).append("*\n");
		sb.append("*").append(strF(" NºVIDEOS (RE)UBICATS   : " + res.numVideosUbicats)).append("*\n");
		sb.append("*").append(strF(" NºVIDEOS DIR UPDATED   : " + res.numVideosUpdatets)).append("*\n");
		sb.append("*").append(strF(" NºVIDEOS ALTRE HD      : " + res.numVideosAltreLloc)).append("*\n");
		sb.append("*").append(strF(" NºVIDEOS NO EN DISC    : " + res.numVideosNoEnDisc)).append("*\n");
		sb.append("*").append(strF(" NºVIDEOS BD SENSE DISC : " + res.numVideosBBDDSenseDisc)).append("*\n");
		sb.append("*                                                                        *\n");
		sb.append("*------------------------------------------------------------------------*\n");
		System.out.print(sb.toString());
	}
	
	private static String strF(String str) {
		final int mida = 71;
		StringBuilder res = new StringBuilder();
		if(str.length() > mida) {
			res.append(str, 0, mida-1);
		}
		else {
			res.append(str);
			for(int i=str.length() ; i <= mida; i++) {
				res.append(" ");
			}
		}
		
		return res.toString();
	}
	
	private int processarBBDDSenseDisc() {

		int count=0;
		try {
			Video crit = new Video();
			List<Video> listVideos = daoV.select(crit);
			
			log.info("Tractant Videos en BBDD sense ubicacio en cap DISC.");
			
			if(listVideos != null) {
				for(Video video : listVideos) {
					if(video.getCodiEstat().intValue() < 7 && video.getCodiEstat().intValue() > 2 && video.getCodiDiscDur()==null) {
						log.warn("El video " + video.getNomFitxer() + " NO está ubicat a cap disc dur i NO consta com ELIMINAT ni PDT DESCARREGAR.");
						count++;
					}
				}
			}
		}
		catch (Exception ex) {
			log.error(ex);
		}
		finally {
			try {
				if(gest!=null) gest.closeConnection();
			}catch(Exception ex) {
				log.error("Error al tancar conexio BBDD.", ex);
			}
		}
		
		return count;
	}
	
	
	private ResultatCoherencia processarDisc(String rutaMKs, Integer codiDiscDurBD, Boolean forceAsinacio, Boolean posarFoto) {

		ResultatCoherencia result = new ResultatCoherencia();
		result.titol="DISC " + codiDiscDurBD + " / RUTA " + rutaMKs;
		result.codiDiscDur=codiDiscDurBD;
		result.rutaMkvs=rutaMKs;
		result.forceAssignacio=forceAsinacio;
		result.posarFoto=posarFoto;
		
		try {
			log.info("Tractant fitxers de la ruta: " + rutaMKs);
			
			Video crit = new Video();
			List<Video> listVideos = daoV.select(crit);
			
			
			Map<String, Video> mapBBDD = new HashMap<String, Video>();
			Map<String, Video> mapBBDDDisc = new HashMap<String, Video>();
			if(listVideos != null) {
				for(Video video : listVideos) {
					if(video.getCodiEstat().intValue() < 7 ) {
						mapBBDD.put(video.getNomFitxer(), video);
						
						if(codiDiscDurBD.equals(video.getCodiDiscDur())) {
							mapBBDDDisc.put(video.getNomFitxer(), video);
							result.numVideosBBDD++;
						}
					}
				}
			}

			List<File> listFileVideos = FileVideos.obtenirVideosCarpeta(rutaMKs);
			if(listFileVideos != null) {
				result.numVideosDisc=listFileVideos.size();
				
				Map<String, File> mapDisc = new HashMap<>();
				for(File file : listFileVideos) {
					Video video = mapBBDD.get(file.getName());
					mapDisc.put(file.getName(), file);
					if(video!=null && video.getNom()!=null) {
						log.info("Tractant Video: " + video.getNom());
						
						//Carpeta actual
						String carpetaActual = file.getParent();
						int pos=carpetaActual.indexOf(":");
						if(pos!=-1) {
							carpetaActual = carpetaActual.substring(pos+1);
						}

						boolean ferUpdate=false;
						
						//CAS video no ubicat mai o bé ubicat en un altre disc dur menys prioritari (com mes petit id disc dur mes prioritari)
						if(video.getCodiDiscDur()==null || 
								codiDiscDurBD < video.getCodiDiscDur() || 
								(forceAsinacio && codiDiscDurBD > video.getCodiDiscDur())) {
							ferUpdate=true;
							
							//Estableix ubicació
							video.setCodiDiscDur(codiDiscDurBD);
							video.setNomCarpeta(carpetaActual);
							
							if(posarFoto) {
								//Posar fotografia
								posarFoto(video, file);
							}

							if(video.getCodiDiscDur()!=null && codiDiscDurBD < video.getCodiDiscDur()) {
								log.warn("El fitxer " + video.getNomFitxer() + " s'ha reubicat a un altre lloc de: " + video.getCodiDiscDur() + " a " + codiDiscDurBD );
							}
							
							log.info("Fitxer ubicat a: " + file.getPath());
							
							result.numVideosUbicats++;
						}
						//Cas mateix disc dur
						else if(video.getCodiDiscDur().equals(codiDiscDurBD)) {
							log.debug("El fitxer " + video.getNomFitxer() + " ja està ubicat a aquest mateix disc dur.");

							//Actualitza la carpeta per actualitzar els casos en que carpeta no informada o canvis de carpeta
							if(!carpetaActual.equalsIgnoreCase(video.getNomCarpeta())) {
								log.info("Carpeta actualitzada de: " + video.getNomCarpeta() + " a: " + carpetaActual);
								ferUpdate=true;
								video.setNomCarpeta(carpetaActual);
								result.numVideosUpdatets++;
							}
							
							if (posarFoto && !existImatge(file)) {
								//Posar fotografia
								posarFoto(video, file);
							}
						}
						else {
							log.warn("El fitxer " + video.getNomFitxer() + " ja està ubicat en BBDD a un altre lloc: " + video.getCodiDiscDur() + " (actual en disc: " + codiDiscDurBD + ").");
							result.numVideosAltreLloc++;
						}

						if(ferUpdate) {
							daoV.update(video);
						}
						
						result.numVideosMatch++;
					}
					else {
						log.warn("Fitxer NO trobat a la BBDD: " + file.getAbsolutePath());
					}
				}
				
				//Recorre videos BBDD en disc per veure si no estan fisicament a disc
				for(Video vid : mapBBDDDisc.values()) {
					if(!mapDisc.containsKey(vid.getNomFitxer())) {
						log.warn("El video " + vid.getNomFitxer() + " NO es troba en aquest disc: " + codiDiscDurBD);
						result.numVideosNoEnDisc++;
					}
				}
			}


			log.info("PROCES ACABAT AMB " + result.numVideosDisc + " de " + listFileVideos.size() + " UBICATS (" + result.numVideosUbicats + " nous).");
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
		
		return result;
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
