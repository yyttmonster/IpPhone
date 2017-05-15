package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class ClientSendVoice implements Runnable {

	private DatagramSocket localSocket;
	private InetAddress counterpartAddress;
	private int counterpartport;
	private TargetDataLine targetDataLine;
	private AudioFormat audioFormat;
	private byte[] sendBuffer;
	private DatagramPacket datagramPacket;
	private boolean isHangUp;

	public ClientSendVoice(DatagramSocket localSocket, InetAddress counterpartAddress, int counterpartport)
			throws LineUnavailableException {
		this.localSocket = localSocket;
		this.counterpartAddress = counterpartAddress;
		this.counterpartport = counterpartport;
		this.audioFormat = new VoiceAudioFormat().getAudioFormat();
		this.targetDataLine = (TargetDataLine) AudioSystem
				.getLine((new DataLine.Info(TargetDataLine.class, this.audioFormat)));
		this.sendBuffer = new byte[1024];
		this.datagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length);
		this.isHangUp = false;
	}

	@Override
	public void run() {
		if (!targetDataLine.isOpen()) {
			try {
				this.targetDataLine.open(this.audioFormat);
				this.targetDataLine.start();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
		while (!isHangUp) {
			int isNull = targetDataLine.read(sendBuffer, 0, sendBuffer.length);
			if (isNull > 0) {
				datagramPacket.setData(sendBuffer);
				try {
					datagramPacket.setSocketAddress(
							new InetSocketAddress(InetAddress.getByName("127.0.0.1"), counterpartport));
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					localSocket.send(datagramPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isHangUp() {
		return isHangUp;
	}

	public void setHangUp(boolean isHangUp) {
		this.isHangUp = isHangUp;
	}

}
