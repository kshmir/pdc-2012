package org.chinux.http.readers;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.impl.HTTPResponseHeaderImpl;
import org.chinux.pdc.http.impl.readers.HTTPGzipReader;
import org.chinux.util.TestUtils;
import org.junit.Test;

public class GzipReaderTest {

	@Test
	public void test() throws IOException {
		// cat request6.txt | nc http://www.ietf.org/ 80 >
		// ../responses/resoponse7.txt

		final HTTPResponseHeader responseheader = new HTTPResponseHeaderImpl(
				TestUtils.stringFromFile("http/responses/response6.txt"));

		final InputStream inputStream = new FileInputStream(
				"src/test/resources/http/responses/example.gz");

		final byte fileContent[] = new byte[1024];
		inputStream.read(fileContent);
		final ByteBuffer aux = ByteBuffer.wrap(fileContent);

		final HTTPGzipReader reader = new HTTPGzipReader(responseheader);

		final byte[] out = reader.processData(ByteBuffer.wrap(fileContent))
				.array();

		System.out.println(new String(out, "UTF-8"));

	}

	// @Test
	public void test1() throws FileNotFoundException, IOException {

		GZIPInputStream gzipInputStream = null;
		gzipInputStream = new GZIPInputStream(new FileInputStream(
				"src/test/resources/http/responses/example.gz"));
		final String outFilename = "gzipdecoded.txt";
		OutputStream out;
		out = new FileOutputStream(outFilename);
		int len;
		final byte[] buf = new byte[1024];
		while ((len = gzipInputStream.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		gzipInputStream.close();
		out.close();
	}

	// @Test
	public void test2() throws IOException {
		final InputStream inputStream = new FileInputStream(
				"src/test/resources/http/responses/example.gz");
		final byte fileContent[] = new byte[1024];
		inputStream.read(fileContent);
		final ByteBuffer aux = ByteBuffer.wrap(fileContent);

		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				aux.array());
		GZIPInputStream gzipInputStream = null;
		try {
			gzipInputStream = new GZIPInputStream(byteArrayInputStream);
		} catch (final IOException e) {
			System.out.println("HIER?");
			e.printStackTrace();
		}
		final byte[] out = new byte[2048];
		try {
			gzipInputStream.read(out);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		System.out.println(new String(out, "UTF-8"));

	}
}
