package org.chinux.http.readers;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.impl.HTTPImageResponseReader;
import org.chinux.pdc.http.impl.HTTPResponseHeaderImpl;
import org.chinux.pdc.http.util.ImageResponseUtils;
import org.chinux.util.TestUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class HttpImageResponseTest {

	@Ignore
	@Test
	public void processDataTest() throws Exception {
		final InputStream is = new BufferedInputStream(new FileInputStream(
				"src/test/resources/http/images/image1.jpg"));
		final HTTPResponseHeader requestheader = new HTTPResponseHeaderImpl(
				TestUtils.stringFromFile("http/responses/response3.txt"));
		final HTTPImageResponseReader imagereader = new HTTPImageResponseReader(
				requestheader);
		Assert.assertFalse(imagereader.isFinished());
		final byte[] imagearray = ImageResponseUtils.getBytes(is);
		final byte[] inverted = imagereader.processData(
				ByteBuffer.wrap(imagearray)).array();
		Assert.assertTrue(imagereader.isFinished());

		// check if the image is right
		final ByteArrayInputStream in = new ByteArrayInputStream(inverted);
		final BufferedImage out = ImageIO.read(in);
		final File outimage = new File(
				"src/test/resources/http/images/image1flipped.jpg");
		try {
			ImageIO.write(out, "png", outimage);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * should test the transformation of images
	 * 
	 * @throws FileNotFoundException
	 */
	// @Test
	public void flipImageTest() throws FileNotFoundException {
		final InputStream is = new BufferedInputStream(new FileInputStream(
				"src/test/resources/http/images/image1.jpg"));
		BufferedImage img = null;
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			img = ImageIO.read(is);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final int w = img.getWidth();
		final int h = img.getHeight();
		final BufferedImage dimg = new BufferedImage(w, h, img.getColorModel()
				.getTransparency());
		final Graphics2D g = dimg.createGraphics();
		g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);
		g.dispose();
		try {
			ImageIO.write(dimg, "png", out);

		} catch (final IOException e) {
			e.printStackTrace();
		}
		out.toByteArray();
		final File outimage = new File(
				"src/test/resources/http/images/image1flipped.jpg");
		try {
			ImageIO.write(dimg, "png", outimage);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
