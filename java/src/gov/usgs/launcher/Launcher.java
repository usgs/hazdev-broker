package gov.usgs.launcher;

import gov.usgs.hazdevbroker.ClientBase;
import gov.usgs.consumerclient.ConsumerClient;
import gov.usgs.producerclient.ProducerClient;
import gov.usgs.archiveclient.ArchiveClient;

/**
 * a launcher class used to support launching either the ConsumerClient or the
 * ProducerClient from the HazDevBroker JAR
 *
 * @author U.S. Geological Survey &lt;jpatton at usgs.gov&gt;
 */
public class Launcher {
	/**
	 * main function for launcher
	 * 
	 * @param args
	 *            - A String[] containing the command line arguments.
	 */
	public static void main(String[] args) {

		// for version numbers
		new ClientBase();

		// check number of arguments
		if (args == null || args.length == 0) {
			System.out
					.println("Usage: hazdev-broker v" + 
						ClientBase.VERSION_MAJOR + "." + 
						ClientBase.VERSION_MINOR + "." + 
						ClientBase.VERSION_PATCH + 
						" <clientType> <configfile>");
			System.exit(1);
		}

		String option = args[0];
		String[] args2 = new String[0];

		if (args.length > 1) {
			args2 = new String[args.length - 1];
			System.arraycopy(args, 1, args2, 0, args2.length);
		}

		if (option.equals("ConsumerClient")) {
			new ConsumerClient();
			ConsumerClient.main(args2);
		} else if (option.equals("ProducerClient")) {
			new ProducerClient();
			ProducerClient.main(args2);
		} else if (option.equals("ArchiveClient")) {
			new ArchiveClient();
			ArchiveClient.main(args2);
		} else {
			System.out.println(
					"Launcher: Invalid hazdev-broker <clientType> provided, only ConsumerClient, ProducerClient, or ArchiveClient supported.");
		}

	}
}
