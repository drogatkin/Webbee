/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.aldan3.util.Stream;
import org.aldan3.util.inet.HttpUtils;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

/** This class take care of asynchronous storing attachments
 *
 * <p> How to manage post load and store<br> WebBee design intention to reduce
 * number of explicit intercepter methods and utilize OO principles, so if
 * any intercepter is required, then methods  <code>storeAttachment</code> or <code>loadAttachment</code>
 * have to be  overridden with adding intercepter code.
 * 
 * 
 * 
 * 
 * @author Dmitriy
 * 
 */
public class Attach<A extends AppModel> extends BaseBlock<A> {
	protected String attachId;
	
	private boolean downloadRequest;

	@Override
	protected Object doControl() {
		downloadRequest = false;
		String fileName = getParameterValue("browsefile+filename", "", 0);
		String attachId = generateId(fileName);
		HashMap pageMap = (HashMap) getModel();
		if (attachId != null) {
			//log("............Storing......." + fileName + " with " + attachId, null);
			try {
				storeAttachment(getObjectParameterValue("browsefile", null, 0, false), attachId);
				pageMap.put("id", attachId);
				pageMap.put("filename", extractFileName(fileName));
			} catch (IOException e) {
				pageMap.put("error", 1);
				log("error", e);
			}
		}
		return pageMap;
	}

	private String extractFileName(String filePath) {
		int lsl = filePath.lastIndexOf('/');
		int lbsl = filePath.lastIndexOf('\\');
		if (lsl < lbsl)
			lsl = lbsl;
		if (lsl < 0)
			return filePath;
		return filePath.substring(lsl+1);
	}

	@Override
	protected Object getModel(){ //try {
		attachId = null;
		attachId = getId();
		if (attachId == null) {
			try {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			} catch (IOException e) {
				log("",e);
			}
			return null;
		}
		if (attachId.length() > 0) {
			downloadRequest = true;
			resp.setContentType(getAttachContentType(attachId));
			if (getParameterValue("download", 0, 0) == 1)
				resp.setHeader("Content-disposition", "attachment; filename=\"" + getName(attachId) + '"');
			try {
				loadAttachment(attachId, resp.getOutputStream());
			} catch (IOException e) {
				log("problem in download attachment", e);
			}
			return null;
		}
		downloadRequest = false;
		HashMap pageMap = new HashMap();
		pageMap.put("divid", getParameterValue("divid", "??", 0));
		pageMap.put("name", getParameterValue("name", "??", 0));
		pageMap.put("target", getParameterValue("target", "??", 0));
		pageMap.put("upload", getParameterValue("upload", 0, 0));
		return pageMap;//}catch(Throwable t) {log("==>", t);} return null;
	}

	@Override
	protected String getSubmitPage() {
		return null;
	}
	
	@Override
	protected String getViewName() {
		if ("json".equals(getParameterValue("response", "", 0))) 
			return "attach_json" + getConfigValue(CONFIG_VIEWEXT, VIEW_EXT); // getResourceName()
		return super.getViewName();
	}

	@Override
	protected void visit() {

	}	

	@Override
	protected boolean useLabels() {
		return false;
	}

	@Override
	protected String getCanvasView() {
		return null;
	}

	@Override
	protected boolean canCache() {
		return downloadRequest;
	}

	@Override
	protected long getLastModified() {
		try {
			return getAttachmentFile(attachId).lastModified();
		} catch (IOException e) {
			log("", e);
		}
		return -1;
	}

	protected String generateId(String fileName) {
		return "" + (long) (10000000 * Math.random() / 1) + ext(fileName);
	}

	protected String getId() {
		return getParameterValue("id", "", 0);
	}

	protected String ext(String filename) {
		int ldp = filename.lastIndexOf('.');
		if (ldp > 0)
			return filename.substring(ldp);
		return ".";
	}

	protected void storeAttachment(Object attachment, String id) throws IOException {
		File attachFile = getAttachmentFile(id);
		File attachFolder = attachFile.getParentFile();
		if (attachFolder != null)
			attachFolder.mkdirs();

		if (attachment instanceof byte[]) {
			FileOutputStream fos = new FileOutputStream(attachFile);
			try {
				fos.write((byte[]) attachment);
				fos.flush();
			} finally {
				fos.close();
			}
		} else if (attachment instanceof File) {
			Stream.copyFile((File) attachment, attachFile);
		} else if (attachment instanceof String) {
			FileWriter fw = new FileWriter(attachFile);
			try {
				fw.write((String) attachment);
			} finally {
				fw.close();
			}
		} else
			log("Not implemented storing of " + attachment, null);
	}

	protected File getAttachmentFile(String id) throws IOException {
		return new File(getAttachmentRoot(), id.replaceAll("/", "_").replaceAll("\\\\", "_"));
	}

	protected File getAttachmentRoot() {
		return getAppModel().getAttachmentHome();
	}

	protected boolean loadAttachment(String id, OutputStream os) throws IOException {
		InputStream is = null;
		try {
			File af = getAttachmentFile(id);
			long flen = af.length();
			resp.setDateHeader("Last-modified", af.lastModified());
			String range = req.getHeader("Range");
			if (range != null) {
				long[] rr = HttpUtils.parseRangeHeader(range, flen);
				if (rr != null && rr[1] > 0) {
					if (rr[0] > rr[1] || rr[1] >= flen) {
						reportRangeError(flen);
						return false;
					}
					is = new FileInputStream(af);
					long sl = is.skip(rr[0]);
					if (rr[0] != sl) {
						reportRangeError(flen);
						return false;
					}
					long clen = rr[1]-rr[0]+1;
					resp.setHeader("content-length", String.valueOf(clen));
					resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
					resp.setHeader("Content-Range", HttpUtils.BYTES_UNIT + " " + rr[0] + '-' + rr[1] + '/' + flen);
					Stream.copyStream(is, os, clen);
					return rr[1] == (flen -1);
				} 					
			} 
			resp.setHeader("content-length", String.valueOf(flen));
			resp.setHeader("Accept-Ranges", HttpUtils.BYTES_UNIT);
			Stream.copyStream(is = new FileInputStream(getAttachmentFile(id)), os, 0);
			return true;
		} finally {
			// TODO consider possible masking of original exception
			if (is != null)
				is.close();
		}
	}

	protected void reportRangeError(long flen) {
		resp.setHeader("Content-Range", HttpUtils.BYTES_UNIT + " */" + flen);
		resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
	}
	
	protected String getAttachContentType(String id) {
		return frontController.getServletContext().getMimeType(id);
	}

	protected String getName(String id) {
		return id;
	}
}
