package es.fz1code.amihd.procesFAToBD;

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FAToJavaJsoup extends FAtoJava {

	@Override
	public InfoPeliFA obtenirVideoEspec(String urlHttp) throws Exception {

		InputStream html = descargaHttp(urlHttp);
		InfoPeliFA vid = getBean(html);
				
		return vid;
	}
	
//	private InfoPeliFA getBeanOld(InputStream htmlPeli) throws Exception {
//
//		Document doc = Jsoup.parse(htmlPeli, "ISO-8859-1", "");
//		htmlPeli.close();
//		
//		//log.debug(doc.toString());
//		
//		InfoPeliFA info = new InfoPeliFA();
//		
//		//TODO:falta parametritzar en el xml de Rules
//		
//		Element taula = doc.select("#mcardtable").first();
//		Element elem = null;
//		Elements elems = null;
//
//		elem = doc.select("DIV > SPAN:has(IMG[src=http://www.filmaffinity.com/images/movie.gif])").first();
//		info.setNom(elem.ownText());
//		
//		elem = taula.select("TH:contains(T�TULO ORIGINAL) + TD > STRONG").first();
//		info.setNomOriginal(elem.ownText());
//		
//		elem = taula.select("TH:contains(A�O) + TD").first();
//		info.setAnyo(elem.ownText());
//
//		elem = taula.select("TH:contains(DURACI�N) + TD").first();
//		info.setDuracio(elem.ownText());
//		
//		elem = taula.select("TH:contains(DIRECTOR) + TD > A").first();
//		info.setDirector(elem.ownText());
//		
//		elems = taula.select("TH:contains(REPARTO) + TD > A");
//		List<String> repart = elementsToList(elems);
//		info.setRepart(repart);
//
//		elems = taula.select("A[href*=moviegenre.php?]");
//		List<String> generes = elementsToList(elems);
//		info.setGeneres(generes);
//
//		elems = taula.select("A[href*=movietopic.php?]");
//		List<String> topics = elementsToList(elems);
//		info.setTopics(topics);
//		
//		elem = taula.select("TH:contains(SINOPSIS) + TD").first();
//		info.setSinopsi(elem.ownText());
//		
//		elem = doc.select("A.lightbox").first();
//		info.setUrlCaratula(elem.attr("href"));
//		
//		elem = doc.select("TD > DIV > DIV:has(IMG.foto) + DIV").first();
//		String nota = elem.ownText();
//		NumberFormat format = NumberFormat.getInstance(new Locale("ES"));
//		Number number = format.parse(nota);
//		info.setNotaFA(number.doubleValue());
//
//		return info;
//	}

	private InfoPeliFA getBean(InputStream htmlPeli) throws Exception {

		//Document doc = Jsoup.parse(htmlPeli, "ISO-8859-1", "");
		Document doc = Jsoup.parse(htmlPeli, "UTF-8", "");
		htmlPeli.close();
		
		//log.debug(doc.toString());
		
		InfoPeliFA info = new InfoPeliFA();
		
		//TODO:falta parametritzar en el xml de Rules
		
		//Element taula = doc.select("DIV[itemtype=http://schema.org/Movie]").first();
		Element taula = doc.select("dl.movie-info").first();
		Element elem = null;
		Elements elems = null;

		//elem = doc.select("H1[id=main-title] > A > SPAN[itemprop=name]").first();
		elem = doc.select("h1[id=main-title] > span[itemprop=name]").first();
		info.setNom(elem.ownText());
		
		elem = taula.select("DT:contains(TÍTULO ORIGINAL) + DD").first();
		info.setNomOriginal(elem.ownText());
		
		elem = taula.select("DT:contains(AÑO) + DD").first();
		info.setAnyo(elem.ownText());

		elem = taula.select("DT:contains(DURACIÓN) + DD").first();
		if(elem!=null) {
			info.setDuracio(elem.ownText());
		}
		
		//elem = taula.select("DT:contains(DIRECTOR) + DD > A").first();
		elem = taula.select("DD[class=directors] > div > span > a > span[itemprop=name]").first();
		info.setDirector(elem.ownText());
		
		//elems = taula.select("DT:contains(REPARTO) + DD > A");
		elems = taula.select("DD[class=card-cast] > div > span > a > span[itemprop=name]");
		List<String> repart = elementsToList(elems);
		if(repart==null) { 
			info.setRepart(Arrays.asList("-"));
		}
		else {
			info.setRepart(repart);
		}

		elems = taula.select("A[href*=moviegenre.php?]");
		List<String> generes = elementsToList(elems);
		info.setGeneres(generes);

		elems = taula.select("A[href*=movietopic.php?]");
		List<String> topics = elementsToList(elems);
		info.setTopics(topics);
		
		elem = taula.select("DT:contains(SINOPSIS) + DD").first();
		info.setSinopsi(elem.ownText());
		
		elem = doc.select("A.lightbox").first();
		info.setUrlCaratula(elem.attr("href"));
		
		elem = doc.select("#movie-rat-avg").first();
		String nota = elem.ownText();
		NumberFormat format = NumberFormat.getInstance(new Locale("ES"));
		Number number = format.parse(nota);
		info.setNotaFA(number.doubleValue());

		return info;
	}

	
	private List<String> elementsToList(Elements elems) {
		if(elems==null || elems.size()==0) return null; 
			
		List<String> result = new ArrayList<String>();
		for(Iterator<Element> it = elems.listIterator();it.hasNext();) {
			Element e = it.next();
			result.add(e.ownText());
		}
		return result;
	}
	
}
