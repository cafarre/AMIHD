package es.fz1code.amihd.procesFAToBD;

import java.io.File;
import java.io.InputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import es.fz1code.amihd.comu.utils.FileVideos;
import es.fz1code.amihd.comu.utils.Logger;
import es.fz1code.amihd.comu.utils.Utilities6;


public abstract class FAtoJava {
	
	protected Logger log =new Logger(this.getClass());
	

	/*
	 * --- Conectar a FA amb HttpClient i descarregar el html de la peli
	 * --- Aplicar el scraper i obtenir el xml de dades de la peli
	 * --- Omplir el DTO o Bean que modela la peli en Java
	 * --- Obtenir la caratula amb HttpClient
	 * --- Guardar caratula en disc
	 * --- Actualitzar el bean amb la nova ruta de la IMG
	 * 
	 */
	public abstract InfoPeliFA obtenirVideoEspec(String urlHttp) throws Exception;
	
	public InfoPeliFA obtenirVideo(String urlHttp) throws Exception {
		log.info("Descarregant info de FA: " + urlHttp);
		
		InfoPeliFA vid = this.obtenirVideoEspec(urlHttp);
		vid.setUrlFA(urlHttp);
		
		return vid;
	}
	
	public InfoPeliFA obtenirCaratula(InfoPeliFA info) throws Exception  {
		//Obte la caratula
		if(info.getUrlCaratula()!=null) {
			//InputStream caratula = descargaHttp("C://workspaceEI/amihd files/AvatarFilmAffinity_files/Avatar-495280-full.jpg");
			log.info("Descarregant caratula de FA: " + info.getUrlCaratula());
			InputStream caratula = descargaHttp(info.getUrlCaratula());
		
			String ruta = guardaCaratulaDisc(caratula, info);
			info.setRutaDiscCaratula(ruta);
		}
		return info;
	}
	
	protected InputStream descargaHttp(String urlHttp) throws Exception {

		HttpClient httpclient = new HttpClient();

//		HostConfiguration hConf = new HostConfiguration();
//		hConf.setProxy("10.115.4.2", 8080);
//		httpclient.setHostConfiguration(hConf);
		
		GetMethod get = new GetMethod(urlHttp);
		httpclient.executeMethod(get);
		
		InputStream preis = get.getResponseBodyAsStream();
		
		return preis;
		
		/*
		File file = new File(urlHttp);
		FileInputStream fis = new FileInputStream(file);
		return fis;
		*/
	}
	


	protected String guardaCaratulaDisc(InputStream isImg, InfoPeliFA info) throws Exception {
		//TODO: parametrizar
		String nom = info.getNom();
		nom = nom.replaceAll("[^a-zA-Z0-9]", "");

		String ruta = FileVideos.rutaImatges + nom + ".jpg";
		File file = Utilities6.grabaFitxerDisc(isImg, ruta);
		isImg.close();
		
		return file.getName();
	}
}
