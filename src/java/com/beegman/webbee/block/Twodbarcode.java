/* **********************************************************************
 * WebBee Copyright 2011 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.aldan3.util.BarCode2D;
import com.beegman.webbee.base.BaseBlock;


public class Twodbarcode  extends BaseBlock {
	static final String IMAGE_FORMAT = "gif";

	static final int BITSIZE = 2;
	
	static final int BITSIZEY = 3;

	static final int masks[] = { 1, 2, 4, 8, 16, 32, 64, 128 };

	@Override
	protected Object getModel() {
		String code = getStringParameterValue("code", "", 0);
		BarCode2D bc = new BarCode2D();
		resp.setContentType("image/" + IMAGE_FORMAT);
		try {
			bc.setText(code);
			bc.setOptions(BarCode2D.PDF417_INVERT_BITMAP);
			bc.setYHeight(1);
			bc.paintCode();
			int bitsw = bc.getBitColumns();
			int bitsh = bc.getCodeRows();
			byte bits[] = bc.getOutBits();
			//System.err.printf("bar %dx%d = %d%n", bitsw, bitsh, bits.length);
			int[] barcode = new int[bitsw * bitsh * BITSIZE * BITSIZE];
			int b = 0;
			BufferedImage bi = new BufferedImage(bitsw * BITSIZE, bitsh * BITSIZE, BufferedImage.TYPE_INT_RGB);//try{
			for (int k = 0; k < bits.length; ++k) {
				for (int m = 0; m < 8; m++) {
					barcode[b] = (bits[k] & masks[m]) == masks[m] ? 0xffffff : 0;
					for (int rf = 0; rf < BITSIZE; rf++) {
						for (int rs = rf==0?1:0; rs < BITSIZE; rs++) {
							barcode[b + (rs * bitsw * BITSIZE) + rf] = barcode[b];
						}
					}
					b += BITSIZE;
					if ((b%(bitsw * BITSIZE)) == 0) {
						b += bitsw * BITSIZE * (BITSIZE - 1);
						break;
					}
				}
				if (b >= barcode.length)
					break;
			}
			bi.setRGB(0, 0, bitsw * BITSIZE, bitsh * BITSIZE, barcode, 0, bitsw * BITSIZE);
			Iterator writers = ImageIO.getImageWritersByFormatName(IMAGE_FORMAT);
			if (writers != null && writers.hasNext()) {
				ImageWriter wr = (ImageWriter) writers.next();
				MemoryCacheImageOutputStream mms;
				wr.setOutput(mms = new MemoryCacheImageOutputStream(resp.getOutputStream()));
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
		return null;
	}

	@Override
	protected Object doControl() {
		return null;
	}

	@Override
	protected String getSubmitPage() {
		return null;
	}
}
