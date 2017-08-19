package es.fz1code.amihd.procesFAToBD;

import java.util.List;

import es.fz1code.amihd.comu.bean.MyBean;

public class InfoPeliFA implements MyBean {
	
	private String nom;
	private String nomOriginal;
	private String urlCaratula;
	private String rutaDiscCaratula;
	private List<String> generes;
	private List<String> topics;
	private String sinopsi;
	private String anyo;
	private String director;
	private List<String> repart;
	private String duracio;
	private Double notaFA; 
	private String urlFA;

	public String getRutaDiscCaratula() {
		return rutaDiscCaratula;
	}
	public void setRutaDiscCaratula(String rutaDiscCaratula) {
		this.rutaDiscCaratula = rutaDiscCaratula;
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
	public String getUrlCaratula() {
		return urlCaratula;
	}
	public void setUrlCaratula(String urlCaratula) {
		this.urlCaratula = urlCaratula;
	}
	public List<String> getGeneres() {
		return generes;
	}
	public void setGeneres(List<String> generes) {
		this.generes = generes;
	}
	public List<String> getTopics() {
		return topics;
	}
	public void setTopics(List<String> topics) {
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
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public List<String> getRepart() {
		return repart;
	}
	public void setRepart(List<String> repart) {
		this.repart = repart;
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
	

}
