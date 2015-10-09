/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.aldan3.model.Log;
import org.aldan3.model.ProcessException;
import org.aldan3.model.ServiceProvider;
import org.aldan3.model.TemplateProcessor;
import org.aldan3.servlet.Constant.CharSet;
import org.aldan3.util.Stream;
import org.aldan3.util.ResourceManager;
import org.aldan3.util.inet.Base64Codecs;
import org.aldan3.util.inet.HttpUtils;
import org.aldan3.util.inet.SendMail;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

// TODO inherit from Cron
public class Mailer<T, A extends AppModel> implements ServiceProvider, Runnable {

	public static final String UNDIS_RECIP = "undisclosed-recipients:;";

	protected static final String REL_PART = "_rel";

	protected static final String ALT_PART = "_alt";

	protected static final String MARKER = "--";

	protected static final String RN = "\r\n";

	protected static final int MAX_IMAGE_SIZE = 64;

	protected A appModel;

	private boolean active;

	private Object monitor;

	private Properties headers;

	protected SendMail mailer;
	
	private Thread mthread;

	public Mailer(A model) {
		appModel = model;
		initMailEngine();
		service();
	}

	@Override
	public String getPreferredServiceName() {
		return getClass().getName();
	}

	@Override
	public Object getServiceProvider() {
		return this;
	}

	/**
	 * Initiate service to work
	 * 
	 */
	public void service() {
		// ScheduledExecutorService scheduler =
		// Executors.newScheduledThreadPool
		mthread = new Thread(this);
		setupThread(mthread);
		active = true;
		monitor = new Object();
		mthread.start();
	}

	/**
	 * Signals to the service to stop working
	 * 
	 */
	public void deactivate() {		
		mthread.interrupt();
		active = false;
		mthread = null;
		//System.err.printf("Service %s deactivated%n", this);
	}

	/**
	 * sets common mail engine props
	 * 
	 */
	protected void initMailEngine() {
		headers = new Properties();
		headers.put("To", UNDIS_RECIP);
		headers.put("Mime-Version", "1.0");
		headers.put("Content-Type", String.format("multipart/mixed; boundary=\"%s\"", getBoundary()));
		headers.put("X-Mailer", "WebBee app blocks framework");
		Properties mailProp = 
		appModel.fillConfigProperties("smtp_config");
		mailer = new SendMail(mailProp);
	}

	/**
	 * customization boundary for multipart
	 * 
	 * @return
	 */
	protected String getBoundary() {
		// TODO maybe make it non statically defined
		return "part1_d51.5292cfcc.37ae3d65_boundary";
	}

	/**
	 * sets up thread attributes, can be overridden for final tune of the thread
	 * 
	 * @param thread
	 */
	protected void setupThread(Thread thread) {
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setName(getPreferredServiceName());
	}

	public void run() {
		while (active) {
			mail(getMailingList());
			synchronized (monitor) {
				try {
					monitor.wait(getCheckPeriod());
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}
	}

	/**
	 * tells that some mail task can be in queue for immediate processing
	 * 
	 */
	public void ping() {
		synchronized (monitor) {
			monitor.notify();
		}
	}

	@Override
	public String toString() {
		return "Mailer service " + getPreferredServiceName() + " (" + this.getClass().getName();
	}

	protected void mail(Collection<T> mailingList) {
		if (mailingList == null)
			return;
		for (T mo : mailingList) {
			try {
				sendMail(mo);
				updateStatus(mo);
			} catch (Exception e) {
				Log.l.error("", e);
				updateStatus(mo, e);
			}
		}
	}

	protected void sendMail(T mo) throws IOException, ProcessException {
		// TODO consider e-mail content generation using servlet calling by
		// getDispatcher
		TemplateProcessor tp = appModel.getTemplateProcessor();
		if (tp == null)
			throw new ProcessException("Template processor isn't available yet");
		HashMap<String, Object> model = new HashMap<String, Object>(8);
		model.put(BaseBlock.MODEL, mo);
		model.put(BaseBlock.APP_NAME, appModel.getAppName());
		model.put("CHARACTER_SET", getCharSet(mo));
		prepareComposeModel(model, mo);
		CharArrayWriter w = new CharArrayWriter(1024);
		// start related parts
		w.write(MARKER);
		w.write(getBoundary());
		w.write(RN);
		// w.write(HttpUtils.quoted_printableEncode("NOTE: You are reading this note because your email program does not support messages in multi-part \"MIME\" format (you can see the plain text version of the message, but the HTML version is garbled).  The best solution to this problem is to upgrade to an email program that supports MIME.  If you can't do that, then just ignore the part of this message that is improperly displayed by your current email program.",
		// 78));
		// w.write(RN);
		w.write("Content-Type: multipart/related; boundary=\"");
		w.write(getBoundary());
		w.write(REL_PART);
		w.write('"');
		w.write(RN);
		w.write(RN);
		w.write(MARKER); // related part -->
		w.write(getBoundary());
		w.write(REL_PART);
		w.write(RN);
		w.write("Content-Type: multipart/alternative; boundary=\"");
		w.write(getBoundary());
		w.write(ALT_PART);
		w.write('"');
		w.write(RN);
		w.write(RN);
		// write plain
		String viewName = getPlainView();
		if (viewName != null && viewName.length() > 0) {
			w.write(MARKER); // plain alternative part -->
			w.write(getBoundary());
			w.write(ALT_PART);
			w.write(RN);
			w.write("Content-Type: text/plain; charset=\"");
			w.write(getCharSet(mo));
			w.write("\"");
			w.write(RN);
			w.write("Content-Transfer-Encoding: 7bit");
			w.write(RN);
			w.write(RN);
			CharArrayWriter part = new CharArrayWriter(1024);
			tp.process(part, viewName, model, appModel.getBaseConfig(), (Locale) getLocale(mo), getTimeZone(mo));
			w.write(HttpUtils.quoted_printableEncode(part.toString(), getCharSet(mo)));
			w.write(RN);
		}
		viewName = getRichView();
		if (viewName != null && viewName.length() > 0) {
			// write rich
			w.write(MARKER); // rich alternative part
			w.write(getBoundary());
			w.write(ALT_PART);
			w.write(RN);
			w.write("Content-Type: text/html; charset=\"");
			w.write(getCharSet(mo));
			w.write("\"");
			w.write(RN);
			w.write("Content-Transfer-Encoding: quoted-printable");
			w.write(RN);
			w.write(RN);
			CharArrayWriter part = new CharArrayWriter(1024);
			tp.process(part, viewName, model, appModel.getBaseConfig(), (Locale) getLocale(mo), getTimeZone(mo));
			w.write(HttpUtils.quoted_printableEncode(part.toString(), getCharSet(mo)));
			w.write(RN);
		}
		w.write(MARKER); // end of alternative parts <--
		w.write(getBoundary());
		w.write(ALT_PART);
		w.write(MARKER);
		w.write(RN);
		w.write(RN);
		Map<String, Object> images = getImages(mo);
		if (images != null)
			for (String name : images.keySet()) {
				w.write(MARKER); // relative part -->
				w.write(getBoundary());
				w.write(REL_PART);
				w.write(RN);
				w.write("Content-ID: <");
				w.write(name);
				w.write(">");
				w.write(RN);
				w.write("Content-Type: ");
				w.write(getContentType(name));
				w.write("; name=\"");
				Object image = images.get(name);
				if (image instanceof File)
					w.write(((File) image).getName());
				else
					w.write(name);
				w.write('"');
				w.write(RN);
				w.write("Content-Disposition: inline");
				w.write(RN);
				w.write("Content-Transfer-Encoding: base64");
				w.write(RN);
				w.write(RN);
				if (image instanceof File) {
					w.write(streamToBase64(new FileInputStream((File) image)));
				} else if (image instanceof byte[]) {
					w.write(Base64Codecs.base64Encode((byte[]) image));
				} else if (image instanceof URL) {
					w.write(streamToBase64(((URL) image).openStream()));
				} else if (image instanceof InputStream) {
					w.write(streamToBase64((InputStream) image));
				}
				w.write(RN);
			}
		w.write(MARKER); // end of relative parts <--
		w.write(getBoundary());
		w.write(REL_PART);
		w.write(MARKER);
		w.write(RN);
		w.write(RN);
		w.write(MARKER);
		w.write(getBoundary());
		w.write(MARKER);
		w.write(RN);
		mailer.send(getFrom(mo), getTo(mo), getSubject(mo), w.toString(), headers);
	}

	/**
	 * returns char set of message
	 * 
	 * @param mo
	 * @return
	 */
	protected String getCharSet(T mo) {
		return CharSet.UTF8;
	}

	/**
	 * returns locale of message
	 * 
	 * @param mo
	 * @return
	 */
	protected Locale getLocale(T mo) {
		return null;
	}

	/**
	 * returns time zone of message
	 * 
	 * @param mo
	 * @return
	 */
	protected TimeZone getTimeZone(T mo) {
		return null;
	}
	
	protected ResourceBundle getResourceBundle(final T mo) {
		return appModel.getTextResource(getResourcePath(mo),
				new ResourceManager.LocalizedRequester() {
			@Override
			public Locale getLocale() {
				 return Mailer.this.getLocale(mo);
			 }
		      
			@Override
		    public String getEncoding() {
				return getCharSet(mo);
			}

			@Override
		    public TimeZone getTimeZone() {
				return Mailer.this.getTimeZone(mo);
			}
		});
	}

	/**
	 * returns message subject
	 * 
	 * @param mo
	 * @return
	 */
	protected String getSubject(T mo) {
		return null;
	}

	/**
	 * returns to address
	 * 
	 * @param mo
	 * @return
	 */
	protected String getTo(T mo) {
		return null;
	}

	/**
	 * returns cc address
	 * 
	 * @param mo
	 * @return
	 */
	protected String getCc(T mo) {
		return null;
	}

	/**
	 * returns from address
	 * 
	 * @param mo
	 * @return
	 */
	protected String getFrom(T mo) {
		return null;
	}

	/**
	 * returns map of attached objects
	 * 
	 * @param mo
	 * @return
	 */
	protected Map<String, Object> getImages(T mo) {
		return null;
	}

	/**
	 * used for determination attachments content type
	 * 
	 * @param name
	 * @return
	 */
	protected String getContentType(String name) {
		return "image/jpeg";
	}
	
	protected String getResourcePath(T mo) {
		return "serv/"+this.getClass().getSimpleName().toLowerCase()+".properties";
	}

	/** returns limitation in attachment size. It can't be more than  2 ^ 32
	 * 
	 * @return
	 */
	protected int getMaxAttachmentSize() {
		return MAX_IMAGE_SIZE * 1024;
	}
	
	/**
	 * allows to modify or add more data in e-mail compose model <br>
	 * 
	 * @param composeData
	 * @param mo
	 */
	protected void prepareComposeModel(HashMap<String, Object> composeData, T mo) {

	}

	/**
	 * view for preparing rich text (html) message part
	 * 
	 * @return
	 */
	protected String getRichView() {
		return null;
	}

	/**
	 * view of text part of message
	 * 
	 * @return
	 */
	protected String getPlainView() {
		return null;
	}

	/**
	 * implement status update of mailing queue
	 * 
	 * @param mo
	 */
	protected void updateStatus(T mo) {

	}
	
	/** implement the method if updating status when erro happened required to be different
	 * 
	 * @param mo
	 * @param e
	 */
	protected void updateStatus(T mo, Throwable e) {
		updateStatus(mo);
	}

	/**
	 * implement for obtaining mailing list
	 * 
	 * @return
	 */
	protected Collection<T> getMailingList() {
		return null;
	}

	/**
	 * defines message queue poll interval in ms
	 * 
	 * @return
	 */
	protected long getCheckPeriod() {
		return 1000 * 60 * 5;
	}

	private String streamToBase64(InputStream is) {
		byte[] buf;
		try {
			buf = Stream.streamToBytes(is, getMaxAttachmentSize());
			return SendMail.splitLine(Base64Codecs.base64Encode(buf), 78);
		} catch (IOException e) {
			Log.l.error("", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {

			}
		}
		return null;
	}
}
