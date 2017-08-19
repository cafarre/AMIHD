package es.fz1code.amihd.videotecahd.bean;

import java.util.List;

import es.fz1code.amihd.comu.bean.MyBean;

public class Video implements MyBean {

		private Integer id;
		private String nom;
		private String nomOriginal;
		private Boolean vist;
		private String rutaCaratula; //
		private String generes;
		private String topics;
		private String sinopsi;
		private String anyo;
		private Boolean MerceNo;//
		private String director;
		private String repart;
		private String duracio;
		private Double notaFA; //
		private String urlFA; //
		private Integer codiEstat; //
		private Integer codiPrioritat; //
		private String definicio;
		private String comentaris;
		private Integer valoracio;
		private Integer codiDiscDur; //
		private Boolean pendentInfoFA; //
		private String nomFitxer;
		private String nomCarpeta;
		
		
		public String getNomFitxer() {
			return nomFitxer;
		}
		public void setNomFitxer(String nomFitxer) {
			this.nomFitxer = nomFitxer;
		}
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getNom() {
			return nom;
		}
		public void setNom(String nom) {
			this.nom = nom;
		}

		public String getNomOriginal() {
			return nomOriginal;
		}
		public void setNomOriginal(String nomOriginal) {
			this.nomOriginal = nomOriginal;
		}

		
		public Boolean getVist() {
			return vist;
		}
		public void setVist(Boolean vist) {
			this.vist = vist;
		}
		public String getRutaCaratula() {
			return rutaCaratula;
		}
		public void setRutaCaratula(String rutaCaratula) {
			this.rutaCaratula = rutaCaratula;
		}
		public String getGeneres() {
			return generes;
		}
		public void setListGeneres(List<String> list) {
			String str = listToString(list);
			this.setGeneres(str);
		}
		public void setGeneres(String generes) {
			this.generes = generes;
		}
		public String getTopics() {
			return this.topics;
		}

		public void setListTopics(List<String> list) {
			String str = listToString(list);
			this.setTopics(str);
		}
		public void setTopics(String topics) {
			this.topics = topics;
		}

		public String getSinopsi() {
			return sinopsi;
		}
		public void setSinopsi(String sinopsi) {
			this.sinopsi = sinopsi;
		}
		public String getAnyo() {
			return anyo;
		}
		public void setAnyo(String anyo) {
			this.anyo = anyo;
		}
		public Boolean getMerceNo() {
			return MerceNo;
		}
		public void setMerceNo(Boolean merceNo) {
			MerceNo = merceNo;
		}
		public String getDirector() {
			return director;
		}
		public void setDirector(String director) {
			this.director = director;
		}
		public String getRepart() {
			return repart;
		}
		public void setRepart(String repart) {
			this.repart = repart;
		}
		public void setListRepart(List<String> list) {
			String str = listToString(list);
			this.setRepart(str);
		}
		public String getDuracio() {
			return duracio;
		}
		public void setDuracio(String duracio) {
			this.duracio = duracio;
		}
		public Double getNotaFA() {
			return notaFA;
		}
		public void setNotaFA(Double notaFA) {
			this.notaFA = notaFA;
		}
		public String getUrlFA() {
			return urlFA;
		}
		public void setUrlFA(String urlFA) {
			this.urlFA = urlFA;
		}
		public Integer getCodiEstat() {
			return codiEstat;
		}
		public void setCodiEstat(Integer codiEstat) {
			this.codiEstat = codiEstat;
		}
		public Integer getCodiPrioritat() {
			return codiPrioritat;
		}
		public void setCodiPrioritat(Integer codiPrioritat) {
			this.codiPrioritat = codiPrioritat;
		}
		public String getDefinicio() {
			return definicio;
		}
		public void setDefinicio(String definicio) {
			this.definicio = definicio;
		}
		public String getComentaris() {
			return comentaris;
		}
		public void setComentaris(String comentaris) {
			this.comentaris = comentaris;
		}
		public Integer getValoracio() {
			return valoracio;
		}
		public void setValoracio(Integer valoracio) {
			this.valoracio = valoracio;
		}
		public Integer getCodiDiscDur() {
			return codiDiscDur;
		}
		public void setCodiDiscDur(Integer codiDiscDur) {
			this.codiDiscDur = codiDiscDur;
		}
		public Boolean getPendentInfoFA() {
			return pendentInfoFA;
		}
		public void setPendentInfoFA(Boolean pendentInfoFA) {
			this.pendentInfoFA = pendentInfoFA;
		}
		public String getNomCarpeta() {
			return nomCarpeta;
		}
		public void setNomCarpeta(String nomCarpeta) {
			this.nomCarpeta = nomCarpeta;
		}
		
		private String listToString(List<String> list) {
			StringBuffer result = new StringBuffer();
			if(list!=null) {
				int i =0;
				for(String str : list) {
					if(i > 0) {
						result.append(", ");
					}
					result.append(str);
					i++;
				}
			}
			return result.toString();
		}
}
