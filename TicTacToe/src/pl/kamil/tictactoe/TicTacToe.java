


package pl.kamil.tictactoe;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
 * @author Piotr Jaœkiewicz Kamil ¯ak
 *
 */
public class TicTacToe implements Runnable {
	
	
	private final int WIDTH = 1024;
	private final int HEIGHT = 724;
	
	private String ip = "localhost";
	private int port = 22222 ;
	private Thread thread;
	
	private JFrame frame;
	private JFrame mainFrame;
	private JLabel headerLabel;
	private JLabel statusLabel;
	private JPanel controlPanel;  
	

	private Painter painter;
	private Socket socket;
	private DataOutputStream dos;
	private DataInputStream dis;
	private ServerSocket serverSocket;

	private BufferedImage img;
	private BufferedImage board;
	private BufferedImage redX;
	private BufferedImage blueX;
	private BufferedImage redCircle;
	private BufferedImage blueCircle;
	
	public String data;
	private String[] spaces = new String[9];

	private boolean won = false;
	private boolean enemyWon = false;
	private boolean tie = false;
	private boolean yourTurn = false;
	private boolean circle = true;
	private boolean accepted = false;
	private boolean uTCWO = false;


	private int lengthOfSpace = 160;
	private int errors = 0;
	private int firstCheck = -1;
	private int secondCheck = -1;

	private Font font = new Font("Comic Sans MS", Font.BOLD, 32);
	private Font smallerFont = new Font("Comic Sans MS", Font.BOLD, 20);
	private Font largerFont = new Font("Comic Sans MS", Font.BOLD, 40);

	private String playAs = "Grasz jako:";
	private String waitingString = "Oczekiwanie na 2 gracza";
	private String uTCWOString = "Blad komunikacji";
	private String wonString = "Wygrales!";
	private String enemyWonString = "Wygrywa przeciwnik";
	private String tieString = "Remis";

	private int[][] wins = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 }, { 2, 4, 6 } };
	

	public TicTacToe() {
		
		prepareConField();
		showLogin();
	
	}
	
	 private void prepareConField(){
	      mainFrame = new JFrame("Polaczenie");
	      mainFrame.setSize(400,200);
	      mainFrame.setLayout(new GridLayout(3, 1));
	      
	      mainFrame.addWindowListener(new WindowAdapter() {
	         public void windowClosing(WindowEvent windowEvent){
	            System.exit(0);
	         }        
	      });    
	      headerLabel = new JLabel("", JLabel.CENTER);        
	      statusLabel = new JLabel("",JLabel.CENTER);    
	      statusLabel.setSize(350,100);

	      controlPanel = new JPanel();
	      controlPanel.setLayout(new FlowLayout());

	      mainFrame.add(headerLabel);
	      mainFrame.add(controlPanel);
	      mainFrame.add(statusLabel);
	      mainFrame.setVisible(true);  
	   }
	 
	 private void showLogin() {
	      headerLabel.setText("Wpisz adres IP, aby rozpoczaæ gre"); 

	      JLabel  ipLabel= new JLabel("Adres IP: ", JLabel.RIGHT);
	      JLabel  portLabel = new JLabel("Port: ", JLabel.CENTER);
	      final JTextField ipText = new JTextField(6);
	      final JTextField portText = new JTextField(6); 
	       
	      JButton conButton = new JButton("Polacz");
	      
	      conButton.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	        	  if(!ipText.equals(ip))ip = ipText.getText();
	      
	              if(!portText.equals(port)){String port2 = portText.getText();
	              port = Integer.valueOf(port2);
	              }
	              
	              statusLabel.setText(data);  
	              
	       startTic();
	       mainFrame.setVisible(false);
	     
	          }
	          }); 
	      controlPanel.add(ipLabel);
	      controlPanel.add(ipText);
	      controlPanel.add(portLabel);       
	      controlPanel.add(portText);
	      controlPanel.add(conButton);
	
	      mainFrame.setVisible(true);  
	   }
	 
	public void startTic()
	{
		loadImages();

		painter = new Painter();
		painter.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		if (!connect()) initializeServer();
	
		frame = new JFrame();
		frame.setTitle("Kolko i krzyzyk");
		frame.setContentPane(painter);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);

		thread = new Thread(this, "TicTacToe");
		thread.start();
		
 	 
  }
	 
	 
	 
	public void run() {
		while (true) {
			tick();
			painter.repaint();

			if (!circle && !accepted) {
				listenForServerRequest();
			}

		}
	}

	private void render(Graphics g) {
		
		g.drawImage(img, 0, 0, null);
		g.drawImage(board, 0, 0, null);
		g.setColor(Color.RED);
		g.setFont(largerFont);
		g.drawString(playAs, 600, 50);
		if(circle)
		{
			g.drawImage(blueCircle, 700, 70, null);
		}
		else
			g.drawImage(blueX, 700, 70, null);
		
		
		
		if (uTCWO) {
			g.setColor(Color.RED);
			g.setFont(smallerFont);
			Graphics2D g2 = (Graphics2D) g;
		
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(uTCWOString);
			g.drawString(uTCWOString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			return;
		}

		if (accepted) {
			for (int i = 0; i < spaces.length; i++) {
				if (spaces[i] != null) {
					if (spaces[i].equals("X")) {
						if (circle) {
							g.drawImage(redX, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						} else {
							g.drawImage(blueX, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						}
					} else if (spaces[i].equals("O")) {
						if (circle) {
							g.drawImage(blueCircle, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						} else {
							g.drawImage(redCircle, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						}
					}
				}
			}
			if (won || enemyWon) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(10));
				g.setColor(Color.BLACK);
				g.drawLine(firstCheck % 3 * lengthOfSpace + 10 * firstCheck % 3 + lengthOfSpace / 2, (int) (firstCheck / 3) * lengthOfSpace + 10 * (int) (firstCheck / 3) + lengthOfSpace / 2, secondCheck % 3 * lengthOfSpace + 10 * secondCheck % 3 + lengthOfSpace / 2, (int) (secondCheck / 3) * lengthOfSpace + 10 * (int) (secondCheck / 3) + lengthOfSpace / 2);

				g.setColor(Color.RED);
				g.setFont(largerFont);
				if (won) {
					int stringWidth = g2.getFontMetrics().stringWidth(wonString);
					g.drawString(wonString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			
				} 
				else if (enemyWon) {
					int stringWidth = g2.getFontMetrics().stringWidth(enemyWonString);
					g.drawString(enemyWonString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
				}
			}
			if (tie) {
				Graphics2D g2 = (Graphics2D) g;
				g.setColor(Color.BLACK);
				g.setFont(largerFont);
				int stringWidth = g2.getFontMetrics().stringWidth(tieString);
				g.drawString(tieString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			}
		} else {
			g.setColor(Color.RED);
			g.setFont(font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(waitingString);
			g.drawString(waitingString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
		}

	}

	private void tick() {
		if (errors >= 10) uTCWO = true;

		if (!yourTurn && !uTCWO) {
			try {
				int space = dis.readInt();
				if (circle) spaces[space] = "X";
				else spaces[space] = "O";
				checkForEnemyWin();
				checkForTie();
				yourTurn = true;
			} catch (IOException e) {
				e.printStackTrace();
				errors++;
			}
		}
	}

	private void checkForWin() {
		for (int i = 0; i < wins.length; i++) {
			if (circle) {
				if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O") {
					firstCheck = wins[i][0];
					secondCheck = wins[i][2];
					won = true;
					
				
				}
			} else {
				if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X") {
					firstCheck = wins[i][0];
					secondCheck = wins[i][2];
					won = true;
				}
			}
		}
	}

	private void checkForEnemyWin() {
		for (int i = 0; i < wins.length; i++) {
			if (circle) {
				if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X") {
					firstCheck = wins[i][0];
					secondCheck = wins[i][2];
					enemyWon = true;
				}
			} else {
				if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O") {
					firstCheck = wins[i][0];
					secondCheck = wins[i][2];
					enemyWon = true;
				}
			}
		}
	}

	private void checkForTie() {
		for (int i = 0; i < spaces.length; i++) {
			if (spaces[i] == null) {
				return;
			}
		}
		tie = true;
	}

	private void listenForServerRequest() {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			accepted = true;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean connect() {
		try {
			socket = new Socket(ip, port);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			accepted = true;
		} catch (IOException e) {
			System.out.println("Nie mozna polaczyc z: " + ip + ":" + port + " | Tworze serwer");
			return false;
		}
		System.out.println("Udalo sie polaczyc z serwerem");
		return true;
	}

	public void initializeServer()  {
		try {
			serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
		} catch (Exception e) {
			e.printStackTrace();
		}
		yourTurn = true;
		circle = false;
	}

	private void loadImages() {
		try {
			img = ImageIO.read(getClass().getResourceAsStream("/back.jpg"));
			board = ImageIO.read(getClass().getResourceAsStream("/board1.png"));
			redX = ImageIO.read(getClass().getResourceAsStream("/redX.png"));
			redCircle = ImageIO.read(getClass().getResourceAsStream("/redCircle.png"));
			blueX = ImageIO.read(getClass().getResourceAsStream("/blueX.png"));
			blueCircle = ImageIO.read(getClass().getResourceAsStream("/blueCircle.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		 try {
	            UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) {
	   
	    }
		TicTacToe ticTacToe = new TicTacToe();
	   
	 
	}

	private class Painter extends JPanel implements MouseListener {
		private static final long serialVersionUID = 1L;

		public Painter() {
			
			setFocusable(true);
			requestFocus();
			setBackground(Color.gray);
			addMouseListener(this);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			render(g);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (accepted) {
				if (yourTurn && !uTCWO && !won && !enemyWon) {
					int x = e.getX() / lengthOfSpace;
					int y = e.getY() / lengthOfSpace;
					y *= 3;
					int position = x + y;

					if (spaces[position] == null) {
						if (!circle) spaces[position] = "X";
						else spaces[position] = "O";
						yourTurn = false;
						repaint();
						Toolkit.getDefaultToolkit().sync();

						try {
							dos.writeInt(position);
							dos.flush();
						} catch (IOException e1) {
							errors++;
							e1.printStackTrace();
						}

						checkForWin();
						checkForTie();
					

					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

	}

}

