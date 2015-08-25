/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.beegman.webbee.model.AppModel;

public class Avatar<A extends AppModel> extends Stream<A> {
	JLabel cachedLabel = new JLabel();

	Color color = new Color(240, 255, 245, 0);

	protected String getHtmlText() {
		return new Date().toString();
	}

	protected Image getImage() {
		return null;
	}

	protected Dimension getSize() {
		return new Dimension(300, 20);
	}

	protected String getImageFormat() {
		return "png";
	}

	protected Color getBackgroundColor() {
		return color;
	}

	protected BufferedImage getAvatar() {
		JLabel label = cachedLabel;//new MyLabel();
		label.setText(getHtmlText());
		Image im = getImage();
		if (im != null)
			label.setIcon(new ImageIcon(im));
		Dimension d = getSize();
		label.setSize(d);

		BufferedImage biDest = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);

		// Draw the texture image into the memory buffer.
		Graphics2D twodg = biDest.createGraphics();
		try {
			twodg.setBackground(getBackgroundColor());

			twodg.fillRect(0, 0, d.width, d.height);
			label.paint(twodg);
		} finally {
			twodg.dispose();
			//log("LABEL parent:" + label.getParent(), null);
		}

		return biDest;
	}

	@Override
	protected void fillStream(OutputStream os) throws IOException {
		Iterator<ImageWriter> ii = ImageIO.getImageWritersByFormatName(getImageFormat());
		if (ii != null && ii.hasNext()) {
			ImageWriter iw = ii.next();
			iw.setOutput(new MemoryCacheImageOutputStream(os));

			try {
				iw.write(getAvatar());
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				log("", e);
			} finally {
				iw.dispose();
			}
		}
	}

	@Override
	protected void setHeaders() {
		resp.setContentType("image/" + getImageFormat());
	}

	static final class MyLabel extends JLabel {

		@Override
		protected void finalize() throws Throwable {

			super.finalize();
			System.err.println("=>>>>>>>>>  LABEL WSE");
		}

	}
}
