package simplerMultiTouch.library;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

class TouchSourceThread extends Thread {
	String label;
	String execArg;
	String prematureShutdownError;

	protected static LinkedList<Process> tuioServerList = new LinkedList<Process>();
	protected static LinkedList<BufferedReader> tuioServerErrList = new LinkedList<BufferedReader>();
	protected static LinkedList<BufferedReader> tuioServerOutList = new LinkedList<BufferedReader>();
	protected static LinkedList<BufferedWriter> tuioServerInList = new LinkedList<BufferedWriter>();

	TouchSourceThread(String label, String execArg, String prematureShutdownError) {
		this.label = label;
		this.execArg = execArg;
		this.prematureShutdownError = prematureShutdownError;
	}

	@Override
	public void run() {
		System.out.println("in run().  starting the touchsourcethread");

		try {

			Process tuioServer = Runtime.getRuntime().exec(execArg);

			BufferedReader tuioServerErr = new BufferedReader(new InputStreamReader(tuioServer.getErrorStream()));

			BufferedReader tuioServerOut = new BufferedReader(new InputStreamReader(tuioServer.getInputStream()));

			BufferedWriter tuioServerIn = new BufferedWriter(new OutputStreamWriter(tuioServer.getOutputStream()));

			tuioServerList.add(tuioServer);

			tuioServerErrList.add(tuioServerErr);

			tuioServerOutList.add(tuioServerOut);

			tuioServerInList.add(tuioServerIn);

			System.out.println("all thread stuff is setup and good to go");
			
			while (true) {
				try {
					int result = tuioServer.exitValue();
					break;
				} catch (IllegalThreadStateException exception) {
					// still running... sleep time
					Thread.sleep(100);
				}
			}

		} catch (IOException exception) {
			System.err.println("io exception in TouchSourceThread");
			System.err.println(exception.getMessage());
		} catch (Exception exception) {
			System.err.println(label + " TUIO Server stopped!");
		}

	}
} // end class TouchSourceThread
