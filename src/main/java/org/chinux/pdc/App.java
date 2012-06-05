package org.chinux.pdc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

	@SuppressWarnings("static-access")
	private static Options getInputOptions() {
		final Options opts = new Options();

		final Option proxyport = OptionBuilder.withLongOpt("proxyport")
				.withDescription("The port to listen in, default is 9090")
				.hasArg().create("proxyport");

		final Option logfile = OptionBuilder
				.withLongOpt("logfile")
				.hasArg()
				.withDescription(
						"The file to log to, the default output is stdout")
				.create("logfile");

		final Option workerthreads = OptionBuilder
				.withLongOpt("workerthreads")
				.hasArg()
				.withDescription(
						"The amount of worker threads to use, default is number of cores * 2")
				.create("workerthreads");

		final Option configport = OptionBuilder
				.withLongOpt("configport")
				.hasArg()
				.withDescription(
						"The port to listen with the configuration server, default is 9091")
				.create("configport");

		final Option monitorport = OptionBuilder
				.withLongOpt("monitorport")
				.hasArg()
				.withDescription(
						"The port to listen with the monitor server, default is 9092")
				.create("monitorport");

		final Option help = OptionBuilder.withLongOpt("help")
				.withDescription("Shows this help").create("help");

		opts.addOption(help);
		opts.addOption(proxyport);
		opts.addOption(configport);
		opts.addOption(monitorport);
		opts.addOption(logfile);
		opts.addOption(workerthreads);

		return opts;
	}

	public static void main(final String[] args) {

		final CommandLineParser parser = new GnuParser();
		try {
			final Options opts = getInputOptions();
			final CommandLine line = parser.parse(opts, args);

			final boolean valid = parseOptions(opts, line);

			if (valid) {
				new ClassPathXmlApplicationContext("META-INF/beans.xml");
			}
		} catch (final ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static boolean parseOptions(final Options opts,
			final CommandLine line) throws FileNotFoundException,
			ParseException {
		if (line.hasOption("help")) {
			new HelpFormatter().printHelp("java -jar chinuproxy.jar", opts);
			return false;
		} else {
			if (line.hasOption("logfile")) {
				if (new File(line.getOptionValue("logfile")).exists()) {
					System.setOut(new PrintStream(new FileOutputStream(line
							.getOptionValue("logfile"))));
				} else {
					throw new ParseException("Invalid file "
							+ line.getOptionValue("logifle")
							+ " added, does it exist?");
				}
			}
			setPropertyFromOption(line, "workerthreads", Runtime.getRuntime()
					.availableProcessors() * 2);
			setPropertyFromOption(line, "proxyport", 9090);
			setPropertyFromOption(line, "configport", 9091);
			setPropertyFromOption(line, "monitorport", 9092);
			return true;
		}
	}

	private static void setPropertyFromOption(final CommandLine line,
			final String property, final int defaultPort) throws ParseException {
		if (line.hasOption(property)) {
			try {
				if (Integer.valueOf(line.getOptionValue(property)) != null) {
					System.setProperty(property, line.getOptionValue(property));
				}
			} catch (final NumberFormatException e) {
				throw new ParseException("Invalid property " + property);
			}

		} else {
			System.setProperty(property, String.valueOf(defaultPort));
		}
	}
}
