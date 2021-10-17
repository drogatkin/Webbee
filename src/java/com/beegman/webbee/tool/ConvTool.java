/* **********************************************************************
 * WebBee Copyright 2012 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.tool;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.aldan3.util.Stream;
import org.aldan3.util.inet.Base64Codecs;

public class ConvTool {

	public static void imgToBase64(InputStream image, Appendable base64) throws IOException {
		base64.append(Base64Codecs.base64Encode(Stream.streamToBytes(image, -1)));
	}
	
	
	public static void main(String ...args) {
		if (args.length < 2) {
			System.out.printf("No parameters specidfied%n");
			System.exit(-1);
		}
		if ("to64".equals(args[0])) {
			try {
				System.out.printf("Converting image %s to %s,%n can be used as embedded css image background-image: url(data:image/png;base64,xxx);%n", args[1], args[2]);
				FileWriter b64w;
				imgToBase64(new FileInputStream(args[1]), b64w = new FileWriter(args[2]));
				b64w.flush();
			} catch (IOException e) {
				e.printStackTrace(); 
			}
		}
	}
}
