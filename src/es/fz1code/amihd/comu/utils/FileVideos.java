package es.fz1code.amihd.comu.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FileVideos {
	
	public static final String rutaImatges = "D:/MyWebs/VIDEOTECAHD/WEB/Caratules/"; 
	
	public static List<File> obtenirVideosCarpeta(String pathCarpeta) {
		List<File> result = new ArrayList<File>();
		File carpeta = new File(pathCarpeta);
		
		if(carpeta.isDirectory()) {

			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if(pathname.getName().endsWith("mkv") ||
							pathname.getName().endsWith("mp4") ||
							pathname.getName().endsWith("avi")) {
						return true;
					}
					
					return pathname.isDirectory();
				}
			};
			
			File[] videos = carpeta.listFiles(filter);
			if(videos!=null) {
				for(File video : videos) {
					if(video.isDirectory()) {
						List<File> subresult = FileVideos.obtenirVideosCarpeta(video.getAbsolutePath());
						if(subresult!=null && subresult.size()>0) {
							result.addAll(subresult);
						}
					}
					else {
						result.add(video);
					}
				}
			}
		}
		
		return result;
	}
	
	public static List<File> obtenirImatgesCarpeta(String pathCarpeta) {
		List<File> result = new ArrayList<File>();
		File carpeta = new File(pathCarpeta);
		
		if(carpeta.isDirectory()) {

			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if(pathname.getName().endsWith("jpg")) {
						return true;
					}
					
					return pathname.isDirectory();
				}
			};
			
			File[] videos = carpeta.listFiles(filter);
			if(videos!=null) {
				for(File video : videos) {
					if(video.isDirectory()) {
						List<File> subresult = FileVideos.obtenirImatgesCarpeta(video.getAbsolutePath());
						if(subresult!=null && subresult.size()>0) {
							result.addAll(subresult);
						}
					}
					else {
						result.add(video);
					}
				}
			}
		}
		
		return result;
	}	
}
