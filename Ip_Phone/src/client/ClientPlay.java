package client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class ClientPlay implements Runnable {

	private DatagramSocket localSocket;
	private DatagramPacket datagramPacket;
	private SourceDataLine sourceDataLine;
	private byte[] receiveBuffer;
	private AudioInputStream audioInputStream;
	private AudioFormat audioFormat;
	private boolean isHangUp;

	public ClientPlay(DatagramSocket localSocket) throws LineUnavailableException, SocketException {
		// TODO Auto-generated constructor stub
		this.localSocket = localSocket;
		this.audioFormat = new VoiceAudioFormat().getAudioFormat();
		this.sourceDataLine = (SourceDataLine) AudioSystem
				.getLine(new DataLine.Info(SourceDataLine.class, this.audioFormat));
		this.receiveBuffer = new byte[1024];
		this.datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		this.isHangUp = false;
	}

	@Override
	public void run() {
		if (!sourceDataLine.isOpen()) {
			try {
				sourceDataLine.open(this.audioFormat);
				sourceDataLine.start();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
		while (!isHangUp) {
			try {
				localSocket.receive(datagramPacket);
				audioInputStream = new AudioInputStream(new ByteArrayInputStream(datagramPacket.getData()),
						this.audioFormat, receiveBuffer.length);
				audioInputStream.read(receiveBuffer);
				sourceDataLine.write(receiveBuffer, 0, receiveBuffer.length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
