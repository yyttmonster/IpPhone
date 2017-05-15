package client;

import java.awt.*;

import javax.swing.*;

import server.Server;

import java.awt.event.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.sound.sampled.*;

public class Client extends JFrame implements ActionListener {

	public static void main(String[] args) throws LineUnavailableException, IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("输入服务端口号：");
		int port = scanner.nextInt();
		scanner.close();
		Client client = new Client(port);
		client.openServer();
		client.listen();
	}

	private static final long serialVersionUID = 1L;
	private JPanel container;
	private JPanel jp1, jp2, jp3;
	private JLabel jl1 = null;
	private JButton callBtn, anserBtn, hangUpBtn;
	private TextField socketInput;

	private Socket socket = null;
	private DatagramSocket localSocket;
	private int tcpport;
	private int udpPort;
	private int counterpartPort;
	private Server server = null;
	private boolean isbusy;
	private ClientSendVoice clientSendVoice = null;
	private ClientPlay clientPlay = null;

	/*
	 * 构造函数 port表示当前用户的用于接收来电的server端口号; 构造函数中同时创建了用户界面
	 */
	public Client(int port) throws LineUnavailableException, IOException {
		this.tcpport = port;
		this.udpPort = port + 1;
		this.isbusy = false;
		localSocket = new DatagramSocket(udpPort);

		// 组件初始化
		
		jp1 = new JPanel();
		jp2 = new JPanel();
		jp3 = new JPanel();
		container = new JPanel();
		socketInput = new TextField("端口号",5);
		container.setSize(500, 400);
		container.add(jp1, BorderLayout.NORTH);
		container.add(jp2, BorderLayout.CENTER);
		container.add(jp3, BorderLayout.SOUTH);

		// 定义字体
		Font myFont = new Font("华文新魏", Font.BOLD, 30);
		jl1 = new JLabel("请拨号");
		jl1.setFont(myFont);
		jp1.setLayout(null);
		jl1.setLocation(20,20);
		socketInput.setBounds(150,120,300,300);
		jp1.add(jl1);
		jp1.add(socketInput);

		callBtn = new JButton("拨号");
		callBtn.addActionListener(this);
		callBtn.setActionCommand("callBtn");

		anserBtn = new JButton("接听");
		anserBtn.addActionListener(this);
		anserBtn.setActionCommand("anserBtn");

		hangUpBtn = new JButton("挂断");
		hangUpBtn.addActionListener(this);
		hangUpBtn.setActionCommand("hangUpBtn");

		
		jp3.setLayout(null);
		jp3.setLayout(new GridLayout(1, 3, 10, 10));
		jp3.add(callBtn);
		jp3.add(anserBtn);
		jp3.add(hangUpBtn);
		// 设置按钮的属性
		callBtn.setEnabled(true);
		anserBtn.setEnabled(false);
		hangUpBtn.setEnabled(false);
		// 设置窗口的属性

		this.setTitle("通话");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/*
	 * 响应界面点击事件
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("callBtn")) {
			dial();
			callBtn.setEnabled(false);
			anserBtn.setEnabled(false);
			hangUpBtn.setEnabled(true);
			jl1.setText("正在呼叫......");

		} else if (e.getActionCommand().equals("anserBtn")) {
			if (AnserCall()) {
				callBtn.setEnabled(false);
				anserBtn.setEnabled(false);
				hangUpBtn.setEnabled(true);
				jl1.setText("通话中...");
			}
		} else if (e.getActionCommand().equals("hangUpBtn")) {
			isbusy = false;
			sendMessage("hang");
			HangUp();
		}
	}

	/*
	 * 
	 */
	public boolean openServer() {
		try {
			server = new Server(tcpport);
			new Thread(server).start();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/*
	 * 监听是否有来电 返回来电者Socket或NULL
	 */

	public void listen() {
		while (true) {
			if (HasPhoneCall())
				break;
		}
	}

	public Boolean HasPhoneCall() {
		if (!server.isConnected()) {
			System.out.print("");
			server.isConnected();
			return false;
		}
		isbusy = true;
		callBtn.setEnabled(false);
		anserBtn.setEnabled(true);
		hangUpBtn.setEnabled(true);
		jl1.setText("来自" + server.getCounterpartAddess() + "的电话");
		this.socket = server.getSocket();
		return true;
	}

	/*
	 * 接听电话 新开线程进行语音接收以及语音发送 关闭待机线程
	 * 
	 */
	public boolean AnserCall() {
		if (!HasPhoneCall())
			return false;
		sendMessage("talk");
		communicate();
		WaitRespond();
		callBtn.setEnabled(false);
		anserBtn.setEnabled(false);
		hangUpBtn.setEnabled(true);
		return true;
	}

	/*
	 * 发送语音，以及播放语音
	 */
	public boolean communicate() {
		try {
			counterpartPort = 3334;
			clientSendVoice = new ClientSendVoice(localSocket, server.getCounterpartAddess(), counterpartPort);
			new Thread(clientSendVoice).start();
			clientPlay = new ClientPlay(localSocket);
			new Thread(clientPlay).start();
			System.out.println("call2");
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/*
	 * 电话空闲，拨号
	 */
	public boolean dial() {
		if (isbusy)
			return false;
		isbusy = true;
		while (socket == null) {
			try {
				if (socket != null) {
					socket = new Socket("localhost", 3333);
					sendMessage("call");
				}
			} catch (Exception e) {
				socket = null;
			}
		}
		// sendMessage(String.valueOf(localSocket.getLocalPort()));
		WaitRespond();
		return true;
	}

	public void WaitRespond() {
		new Thread(() -> {
			while (isbusy) {
				try {
					byte[] responsebyte = new byte[4];
					DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
					dataInputStream.read(responsebyte);
					String response = new String(responsebyte);
					System.out.println(response);
					if (response.equals("talk")) {
						callBtn.setEnabled(false);
						anserBtn.setEnabled(false);
						hangUpBtn.setEnabled(true);
						jl1.setText("通话中...");
						communicate();
					}
					if (response.equals("hang")) {
						sendMessage("hang");
						HangUp();
						System.out.println("receive hang");
						isbusy = false;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void sendMessage(String message) {
		new Thread(() -> {
			DataOutputStream dataOutputStream;
			try {
				dataOutputStream = new DataOutputStream(socket.getOutputStream());
				dataOutputStream.write(message.getBytes());
				System.out.println(tcpport + ":" + message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}

	public void HangUp() {
		new Thread(() -> {
			jl1.setText("已挂断");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			callBtn.setEnabled(true);
			anserBtn.setEnabled(false);
			hangUpBtn.setEnabled(false);
			jl1.setText("请拨号");
		}).start();
		if (clientSendVoice != null)
			clientPlay.setHangUp(true);
		if (clientSendVoice != null)
			clientSendVoice.setHangUp(true);
		System.out.println("hang up");
//		try {
//			socket.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
