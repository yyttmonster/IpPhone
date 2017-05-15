package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

	private ServerSocket serverSocket;
	private boolean stopFlag;

	private Socket socket = null;
	private InetAddress counterpartAddress;
//	private int counterpartPort = -1;
//	private byte[] receivePort;

	public Server(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.stopFlag = false;
//		this.receivePort = new byte[4];
	}

	public boolean closeSeverSocket() {
		try {
			this.serverSocket.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public InetAddress getInetAddress() {
		return this.serverSocket.getInetAddress();
	}

	public boolean isConnected() {
		if (socket == null)
			return false;
		return true;
	}

	public InetAddress getCounterpartAddess() {
		return this.counterpartAddress;
	}

	public int getCounterPartPort() {
		return 6666;
	}

	@Override
	public void run() {
		while (!stopFlag) {
			if (socket == null) {
				try {
					socket = serverSocket.accept();
					if (isConnected()) {
						this.counterpartAddress = socket.getInetAddress();
//						setCounterpartPort();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

//	private void setCounterpartPort() {
//			while (counterpartPort < 0) {
//				DataInputStream dataInputStream;
//				try {
//					dataInputStream = new DataInputStream(socket.getInputStream());
//					counterpartPort = dataInputStream.readUnsignedShort();
//					System.out.println(counterpartPort);
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				System.out.println("counterpartSocket" + counterpartPort);
//			}
//	}

	public boolean isStopFlag() {
		return stopFlag;
	}

	public void setStopFlag(boolean stopFlag) {
		this.stopFlag = stopFlag;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	

}
