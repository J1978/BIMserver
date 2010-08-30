package org.bimserver.ifcengine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.bimserver.shared.ResourceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailSafeIfcEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(FailSafeIfcEngine.class);
	private static final int PORT_START = 20000;
	private static final int PORT_END = 50000;
	private static int portCounter = PORT_START;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private IfcEngineServer ifcEngineServer;
	private EngineProces engineProces;
	private int port;

	public FailSafeIfcEngine(final File schemaFile, final File nativeBaseDir, ResourceFetcher resourceFetcher) {
		try {
			port = getNewPort();
			engineProces = new EngineProces(this, port, schemaFile, nativeBaseDir, resourceFetcher);
			engineProces.start();
			connectClient();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	private synchronized static int getNewPort() {
		if (portCounter < PORT_END) {
			portCounter++;
		} else {
			portCounter = PORT_START;
		}
		return portCounter;
	}
	
	private void connectClient() throws UnknownHostException, IOException {
		for (int i = 0; i < 3; i++) {
			try {
				socket = new Socket("127.0.0.1", port);
				in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				break;
			} catch (ConnectException e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public synchronized IfcEngineModel openModel(File ifcFile) throws IfcEngineException {
		writeCommand(Command.OPEN_MODEL);
		writeUTF(ifcFile.getAbsolutePath());
		flush();
		int modelId = readInt();
		return new IfcEngineModel(this, modelId);
	}

	public int readInt() throws IfcEngineException {
		try {
			return in.readInt();
		} catch (IOException e) {
			throw new IfcEngineException(e);
		}
	}

	public void flush() throws IfcEngineException {
		try {
			out.flush();
		} catch (IOException e) {
			throw new IfcEngineException(e);
		}
	}

	void writeUTF(String value) throws IfcEngineException {
		try {
			out.writeUTF(value);
		} catch (IOException e) {
			throw new IfcEngineException(e);
		}
	}

	public void writeCommand(Command command) throws IfcEngineException {
		try {
			out.writeByte(command.getId());
		} catch (IOException e) {
			throw new IfcEngineException(e);
		}
	}

	public synchronized void close() {
		if (engineProces != null) {
			engineProces.shutdown();
		}
		if (ifcEngineServer != null) {
			ifcEngineServer.close();
		}
	}

	public void writeInt(int value) throws IfcEngineException {
		try {
			out.writeInt(value);
		} catch (IOException e) {
			throw new IfcEngineException(e);
		}
	}

	public void writeBoolean(boolean value) throws IfcEngineException {
		try {
			out.writeBoolean(value);
		} catch (IOException e) {
			throw new IfcEngineException(e);
		}
	}

	public float readFloat() throws IfcEngineException {
		try {
			return in.readFloat();
		} catch (IOException e) {
			throw new IfcEngineException(e);
		}
	}

	public synchronized void engineStopped() {
	}

	public String readString() throws IfcEngineException {
		try {
			return in.readUTF();
		} catch (IOException e) {
			throw new IfcEngineException(e);
		}
	}

	public void writeDouble(double d) throws IfcEngineException {
		try {
			out.writeDouble(d);
		} catch (IOException e) {
			throw new IfcEngineException(e);
		}
	}

	public long readLong() throws IfcEngineException {
		try {
			return in.readLong();
		} catch (IOException e) {
			throw new IfcEngineException(e);
		}
	}
}