package com.beegman.webbee.block;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.aldan3.util.Pdf417;
import org.aldan3.util.Pdf417.Symbol;

import com.beegman.webbee.model.AppModel;

public class Pdf417img<A extends AppModel> extends Stream<A> {
	static final String IMAGE_FORMAT = "gif";
	static final int masks[] = { 1, 2, 4, 8, 16, 32, 64, 128 };

	@Override
	protected void fillStream(OutputStream os) throws IOException {
		Symbol sym = getBarcodeAttributes();
		char[] ca = sym.input.toCharArray();
		int res = new Pdf417().pdf417enc(sym, ca, ca.length);
		if (res == 0) {
			int h = 0;
			int dh = 0;

			while (sym.row_height[h] != 0) {
				h++;
				dh += sym.row_height[h];
			}
			int w = sym.width;
			int ch = dh / h;
			BufferedImage bi = new BufferedImage(w, dh, BufferedImage.TYPE_INT_RGB);
			int barcode[] = new int[w * dh];
			for (int r = 0; r < h; r++) {
				for (int c = 0; c < w; c++) {
					int bm = c % 7;
					int bp = c / 7;
					putDot(barcode, w * 1, c, r, 1, ch, (sym.encoded_data[r][bp] & masks[bm]) != 0 ? 0xffffff : 0);
				}
			}
			bi.setRGB(0, 0, w * 1, h * ch, barcode, 0, w * 1);
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
		} else
			log("Problem in barcode creation %d, %s", null, res, sym.errtxt);
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

	protected Symbol getBarcodeAttributes() {
		Symbol res = new Symbol();
		res.symbology = Pdf417.BARCODE_HIBC_PDF;
		res.option_3 = 928;
		res.input = "+" + getStringParameterValue("code", "", 0);
		return res;
	}

}
