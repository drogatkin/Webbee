/* **********************************************************************
 * WebBee Copyright 2009-2016 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.aldan3.util.BarCode2D;

import com.beegman.webbee.model.AppModel;

public class Barcode2d<A extends AppModel> extends Stream<A> {
	static final String IMAGE_FORMAT = "gif";

	static final int masks[] = { 1, 2, 4, 8, 16, 32, 64, 128 };

	@Override
	protected void fillStream(OutputStream os) throws IOException {
		BarCode2D bc = new BarCode2D();
		BarCodeAttributes bca = getBarcodeAttributes();
		try {
			bc.setText(bca.code);
			bc.setOptions(bca.format);
			//bc.setYHeight(1);
			bc.paintCode();
			int bitsw = bc.getBitColumns();
			int bitsh = bc.getCodeRows();
			byte bits[] = bc.getOutBits();
			int dotW = bca.dotWidth;
			int dotH = bca.dotHeight;
			//log("bar %dx%d = %d for %s", null, bitsw, bitsh, bits.length, bca.code);
			int[] barcode = new int[bitsw * bitsh * dotW * dotH];
			BufferedImage bi = new BufferedImage(bitsw * dotW, bitsh * dotH, BufferedImage.TYPE_INT_RGB);
			int byteW = (bitsw + 7) / 8;
			for (int r = 0; r < bitsh; r++) {
				for (int c = 0; c < bitsw; c++) {
					int bm = c % 8;
					int bp = c / 8;
					putDot(barcode, bitsw * dotW, c, r, dotW, dotH,
							(bits[r * byteW + bp] & masks[bm]) != 0 ? 0xffffff : 0);
				}
			}
			bi.setRGB(0, 0, bitsw * dotW, bitsh * dotH, barcode, 0, bitsw * dotW);
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(IMAGE_FORMAT);
			if (writers != null && writers.hasNext()) {
				ImageWriter wr = (ImageWriter) writers.next();
				// TODO analyze on freeing resources
				MemoryCacheImageOutputStream mms;
				wr.setOutput(mms = new MemoryCacheImageOutputStream(os));
				wr.write(bi);
				mms.flush();
				wr.dispose();
			} else
				log("No image writer found for %s", null, IMAGE_FORMAT);
		} catch (UnsupportedEncodingException e) {
			log("Error in barcode gen", e);
		} catch (IOException e) {
			log("Error in barcode out", e);
		}
	}

	protected void putDot(int[] barcode, int ss, int c, int r, int dw, int dh, int rgb) {
		int cornX = c * dw;
		int cornY = r * ss * dh;
		for (int y = 0; y < dh; y++) {
			for (int x = 0; x < dw; x++) {
				barcode[cornY + (y * ss) + cornX + x] = rgb;
			}
		}
	}

	@Override
	protected void setHeaders() {
		super.setHeaders();
		resp.setContentType("image/" + IMAGE_FORMAT);
	}

	protected BarCodeAttributes getBarcodeAttributes() {
		BarCodeAttributes res = new BarCodeAttributes();
		res.code = getStringParameterValue("code", "", 0);
		return res;
	}

	public static class BarCodeAttributes {
		public String code;
		public int dotWidth = 2;
		public int dotHeight =  dotWidth*3;
		public int format = BarCode2D.PDF417_INVERT_BITMAP;
	}

}
