package org.chinux.pdc.http.impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPImageResponseReader implements HTTPReader {

	private HTTPResponseHeader responseheader;
	private boolean finished;
	private Integer currlenght;
	private byte[] image;
	private int index;
	private final int MAXSIZE = 1000000;

	public HTTPImageResponseReader(final HTTPResponseHeader responseheader) {
		this.responseheader = responseheader;
		this.finished = false;
		this.currlenght = 0;
		this.index = 0;
		this.image = new byte[this.MAXSIZE];
	}

	@Override
	public byte[] processData(final byte[] data) {
		final Integer contentlenght = Integer.valueOf(this.responseheader
				.getHeader("Content-Length"));
		this.currlenght += data.length;
		int i = 0;
		while (i < data.length) {
			this.image[this.index++] = data[i++];
		}
		if (this.currlenght >= contentlenght) {
			this.finished = true;
			return this.flip();
		}
		return data;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

	public byte[] flip() {
		final InputStream in = new ByteArrayInputStream(this.image);
		BufferedImage img = null;
		try {
			img = ImageIO.read(in);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final BufferedImage flipped = this.verticalflip(img);
		return flipped.toString().getBytes();

	}

	public BufferedImage verticalflip(final BufferedImage img) {
		final int w = img.getWidth();
		final int h = img.getHeight();
		final BufferedImage dimg = new BufferedImage(w, h, img.getColorModel()
				.getTransparency());
		final Graphics2D g = dimg.createGraphics();
		g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);
		g.dispose();
		return dimg;
	}

}
