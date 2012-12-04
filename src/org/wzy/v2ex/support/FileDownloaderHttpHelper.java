package org.wzy.v2ex.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.StatusLine;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class FileDownloaderHttpHelper {
	public static interface DownloadListener {
		public void pushProgress(int progress, int max);
	}
	
	public static boolean saveFile(HttpGet httpGet, HttpResponse response,
			String path, FileDownloaderHttpHelper.DownloadListener downloadListener) {
		StatusLine status = response.getStatusLine();
		int statusCode = status.getStatusCode();
		
		return statusCode == HttpStatus.SC_OK &&
				saveFileAndGetFileAbsolutePath(httpGet, response, path, downloadListener);
	}
	
	private static boolean saveFileAndGetFileAbsolutePath(HttpGet httpGet,
			HttpResponse response, String path,
			FileDownloaderHttpHelper.DownloadListener downloadListener) {
		HttpEntity httpEntity = response.getEntity();
		File file = FileManager.createNewFileInSDCard(path);
		if (file == null) {
			return false;
		}
		
		FileOutputStream out = null;
		InputStream in = null;
		
		try {
			int bytetotal = (int) httpEntity.getContentLength();
			int bytesum = 0;
			int byteread = 0;
			out = new FileOutputStream(file);
			in = httpEntity.getContent();
			byte[] buffer = new byte[1444];
			while ((byteread = in.read(buffer)) != -1) {
				bytesum += byteread;
				out.write(buffer, 0, byteread);
				if (downloadListener != null && bytetotal > 0) {
					downloadListener.pushProgress(bytesum, bytetotal);
				}
			}
			
			EntityUtils.consume(response.getEntity());
		} catch (Exception e) {
			httpGet.abort();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					
				}
			}
		}
		
		return file.exists() && (file.length() > 0);
	}
}
