package org.chinux.pdc.http.impl.readers;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPImageResponseReader implements HTTPReader {

	private static Logger log = Logger.getLogger(HTTPImageResponseReader.class);
	private HTTPResponseHeader responseHeader;
	private boolean finished;
	private ByteArrayOutputStream stream = new ByteArrayOutputStream();
	private Integer bytesToRead = null;

	public HTTPImageResponseReader(final HTTPResponseHeader responseheader) {
		this.responseHeader = responseheader;
		this.finished = false;
	}

	private int getBytesToRead() {
		if (this.responseHeader.getHeader("content-length") != null
				&& this.bytesToRead == null) {
			this.bytesToRead = Integer.valueOf(this.responseHeader
					.getHeader("content-length"));
		}
		return this.bytesToRead;
	}

	private boolean is300Response() {
		return this.responseHeader.returnStatusCode() >= 300
				&& this.responseHeader.returnStatusCode() <= 399;
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {

		if (this.is300Response() || this.getBytesToRead() == 0) {
			if (data.array().length == 0) {
				this.finished = true;
			}
			return data;
		}

		try {
			this.stream.write(data.array());
		} catch (final IOException e) {
			e.printStackTrace();
		}

		if (this.stream.size() >= this.getBytesToRead()) {
			this.finished = true;
			ByteBuffer buffer;
			try {
				buffer = this.flip(this.stream);
			} catch (final Exception e) {
				log.error("No se pudo voltear la imagen :(", e);
				buffer = ByteBuffer.wrap(this.stream.toByteArray());
			}

			this.responseHeader.addHeader("X-Image-Rotated", "true");
			this.responseHeader.addHeader("content-length",
					String.valueOf(buffer.array().length));
			return buffer;
		} else {
			return null;
		}
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

	public ByteBuffer flip(final ByteArrayOutputStream buff) {
		final ByteArrayInputStream in = new ByteArrayInputStream(
				buff.toByteArray());
		BufferedImage img = null;
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			img = ImageIO.read(in);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final BufferedImage flipped = this.verticalflip(img);
		try {
			ImageIO.write(flipped, "png", out);

		} catch (final IOException e) {
			e.printStackTrace();
		}
		this.finished = true;
		return ByteBuffer.wrap(out.toByteArray());
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

	@Override
	public boolean modifiesHeaders() {
		return true; // Supongo...
	}

}
