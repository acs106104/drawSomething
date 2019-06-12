import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;


public class ClientGui extends Thread{

  final JTextPane jtextFilDiscu = new JTextPane();
  final JTextPane jtextListUsers = new JTextPane();
  final JTextField jtextInputChat = new JTextField();
  private String oldMsg = "";
  private Thread read;//讀取訊息
  private Thread draw;//畫畫
  private String serverName;
  private int PORT;
  private String name;
  BufferedReader input;
  PrintWriter output;
  Socket server;
  boolean check=false;
  boolean timeCheck=false;
  boolean timeStopCheck=false;
  
  private JProgressBar progressbar;
  private Timer timer;
  
  //Draw
  JPanel DrawingPanel;
  JPanel ColorPanel;
  JPanel ToolPanel;
  JPanel CoverPanel;//覆蓋的
  JLabel Question;
  JLabel Answer;
  JLabel QLabel;//題目
  JLabel ALabel;//答案
  JLabel CoverLabel;//覆蓋答案的
  JSlider Pencilcon;
  
  int toolFlag = 0;
  int startX = -1;
  int startY = -1;
  int endX = -1;
  int endY = -1;
  Color c = Color.BLACK;
  
  boolean drawCheck=false;
  
  JToggleButton jToggleButton[];
  ButtonGroup buttonGroup;
  Icon tool[]=new ImageIcon[5];
  
  public ClientGui() {
    this.serverName = "localhost";
    this.PORT = 12345;
    this.name = "nickname";

    String fontfamily = "Arial, sans-serif";
    Font font = new Font(fontfamily, Font.PLAIN, 15);

    final JFrame jfr = new JFrame("Draw Something");
    jfr.getContentPane().setLayout(null);
    jfr.setSize(700, 500);
    jfr.setResizable(false);
    jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // 線程模塊
    jtextFilDiscu.setBounds(25, 25, 490, 320);
    jtextFilDiscu.setFont(font);
    jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
    jtextFilDiscu.setEditable(false);
    JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
    jtextFilDiscuSP.setBounds(184, 36, 490, 320);

    jtextFilDiscu.setContentType("text/html");
    jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // 用戶列表的模塊
    jtextListUsers.setBounds(520, 25, 156, 320);
    jtextListUsers.setEditable(true);
    jtextListUsers.setFont(font);
    jtextListUsers.setMargin(new Insets(6, 6, 6, 6));
    jtextListUsers.setEditable(false);
    JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
    jsplistuser.setBounds(16, 36, 156, 320);

    jtextListUsers.setContentType("text/html");
    jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Field message user input
    jtextInputChat.setBounds(0, 350, 400, 50);
    jtextInputChat.setFont(font);
    jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
    final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
    jtextInputChatSP.setBounds(184, 675, 630, 50);

    // button send
    final JButton jsbtn = new JButton("Send");
    jsbtn.setFont(font);
    jsbtn.setBounds(814,675, 60, 50);

    // button Disconnect
    final JButton jsbtndeco = new JButton("Disconnect");
    jsbtndeco.setFont(font);
    jsbtndeco.setBounds(730, 40, 150, 35);

    jtextInputChat.addKeyListener(new KeyAdapter() {
      // send message on Enter
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }

        // Get last message typed
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }
      }
    });

    // Click on send button
    jsbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendMessage();
      }
    });

    // Connection view
    final JTextField jtfName = new JTextField(this.name);
    final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
    final JTextField jtfAddr = new JTextField(this.serverName);
    final JButton jcbtn = new JButton("Connect");
    

    // check if those field are not empty
    jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    
    //Draw Item
    //DrawPanel=new JPanel();
    //DrawPanel.setBounds(184,200,300,200);
    
    ALabel = new JLabel("答案:");
    ALabel.setFont(new Font("Arial", Font.BOLD, 20));
    ALabel.setBounds(184,30,50,50);
    
    CoverLabel = new JLabel();
    CoverLabel.setBounds(234,40,60,30);
	CoverLabel.setOpaque(true);
	CoverLabel.setBackground(new Color(230, 230, 250));
	CoverLabel.setHorizontalAlignment(SwingConstants.CENTER);
	CoverLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
    
    Answer = new JLabel("?");
    Answer.setBounds(234,40,60,30);
	Answer.setOpaque(true);
	Answer.setBackground(new Color(230, 230, 250));
	Answer.setHorizontalAlignment(SwingConstants.CENTER);
	Answer.setBorder(new LineBorder(new Color(0, 0, 0)));
	Answer.setFont(new Font("Arial", Font.BOLD, 13));
	
    QLabel = new JLabel("提示:");
    QLabel.setFont(new Font("Arial", Font.BOLD, 20));
    QLabel.setBounds(300,30,50,50);
    
    Question = new JLabel();
    Question.setBounds(350,40,200,30);
	Question.setOpaque(true);
	Question.setHorizontalAlignment(SwingConstants.CENTER);
	Question.setBorder(new LineBorder(new Color(0, 0, 0)));
	Question.setFont(new Font("Arial", Font.BOLD, 13));
    
    DrawingPanel=new JPanel();
    DrawingPanel.setBounds(184,85,550,330);
    DrawingPanel.setBackground(Color.WHITE);
    
    //Draw
    DrawingPanel.addMouseListener(new MouseAdapter(){
   //滑鼠按下事件,重置startX,startY
		public void mousePressed(MouseEvent e){
			   startX = e.getX();
			   startY = e.getY();
		}
		public void mouseReleased(MouseEvent e)
		 { 
			Graphics graphics = DrawingPanel.getGraphics();
			//color
			((Graphics2D) graphics).setColor(c);
			//stroke
		    ((Graphics2D) graphics).setStroke(new BasicStroke(Pencilcon.getValue()*2));
		    
		    switch(toolFlag) {
		    case 2://line
				endX = e.getX();
				endY = e.getY();
				((Graphics2D)graphics).drawLine(startX, startY, endX, endY);
				sendDraw(startX,endX,startY,endY,c,toolFlag,Pencilcon.getValue());
				break;
		    case 3://rectangle
				endX = e.getX();
				endY = e.getY();
				((Graphics2D)graphics).drawRect(Math.min(startX,endX), Math.min(startY,endY), Math.abs(startX - endX), Math.abs(startY - endY));
				sendDraw(startX,endX,startY,endY,c,toolFlag,Pencilcon.getValue());
				break;
		    case 4://circle
				endX = e.getX();
				endY = e.getY();
				((Graphics2D)graphics).drawOval(Math.min(startX,endX), Math.min(startY,endY), Math.abs(startX - endX), Math.abs(startY - endY));
				sendDraw(startX,endX,startY,endY,c,toolFlag,Pencilcon.getValue());
				break;
		    }
		 }
	}); 

   //滑鼠拖動事件,自由畫圖
   DrawingPanel.addMouseMotionListener(new MouseAdapter(){
		public void mouseDragged(MouseEvent e){
			Graphics graphics = DrawingPanel.getGraphics();
			//color
			((Graphics2D) graphics).setColor(c);
			//stroke
		    ((Graphics2D) graphics).setStroke(new BasicStroke(Pencilcon.getValue()*2));
		    
		    /*MODE*/
		    /*0:pencil 1:eraser 2:line 3:square 4:circle*/
		   // System.out.println(toolFlag);
		    switch(toolFlag) {
		    case 0://simple draw 
				endX = e.getX();
				endY = e.getY();
				((Graphics2D)graphics).drawLine(startX,startY,endX,endY);
				sendDraw(startX,endX,startY,endY,c,toolFlag,Pencilcon.getValue());
				startX=endX;
				startY=endY;
				break;
		    case 1://eraser
		    	endX = e.getX();
				endY = e.getY();
			    ((Graphics2D) graphics).setStroke(new BasicStroke(Pencilcon.getValue()*5));
		    	((Graphics2D) graphics).setColor(Color.WHITE);
		    	((Graphics2D)graphics).drawLine(startX,startY,endX,endY);
		    	sendDraw(startX,endX,startY,endY,c,toolFlag,Pencilcon.getValue());
				startX=endX;
				startY=endY;
		    	break;
		    }
		}
	});
    
   //顏色盤
    ColorPanel = new JPanel();
    ColorPanel.setBounds(784,85,90,350);
    ColorPanel.setVisible(false);
    //工具盤
    ToolPanel = new JPanel();
    ToolPanel.setBounds(734,85,50,350);
    ToolPanel.setVisible(false);
    //遮答案的
    CoverPanel = new JPanel();
    CoverPanel.setBounds(734,85,140,330);
    CoverPanel.setVisible(true);
    CoverPanel.setBackground(Color.PINK);
    
    
	Color [] colors={Color.black,Color.GRAY,Color.red,Color.orange,Color.YELLOW,Color.green,
			Color.blue,Color.cyan,Color.magenta,Color.pink};
	//颜色按钮添加
	ActionListener colorbtnlistener = new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
			
			JButton bt =(JButton)e.getSource();
			c =bt.getBackground();
		}
	};
	for (int i = 0; i < colors.length; i++) {
		JButton btn = new JButton();
		btn.setBackground(colors[i]);
		btn.setOpaque(true);
		btn.addActionListener(colorbtnlistener);
		btn.setBounds(40, 15+i*30, 30, 30);
		ColorPanel.add(btn);
	}
	//工具按鈕添加
	String toolname[]={"img/tool7.gif","img/tool6.gif","img/tool1.gif","img/tool2.gif","img/tool3.gif"};
	//7:pencil 6:eraser  1:line 2:square 3:circle
	buttonGroup = new ButtonGroup();
	jToggleButton = new JToggleButton[toolname.length];
	for(int i=0;i<toolname.length;i++){
		tool[i] = new ImageIcon(toolname[i]);
		jToggleButton[i] = new JToggleButton(tool[i]);
		//jToggleButton[i].addActionListener( this );
		jToggleButton[i].setFocusable( false );
		jToggleButton[i].setBounds(40, 15+i*30, 30, 30);
		buttonGroup.add(jToggleButton[i]);
	}
	
	//各種工具
	ToolPanel.add(jToggleButton[0]);//pencil
	ToolPanel.add(jToggleButton[1]);//eraser
	ToolPanel.add(jToggleButton[2]);//line
	ToolPanel.add(jToggleButton[3]);//rectangle
	ToolPanel.add(jToggleButton[4]);//circle
	jToggleButton[0].setSelected(true);
	
	//pencil
    jToggleButton[0].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	toolFlag = 0;
        }
      });
    //eraser
    jToggleButton[1].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	toolFlag = 1;
        }
      });
    //line
    jToggleButton[2].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	toolFlag = 2;//3
        }
      });
    //rectangle
    jToggleButton[3].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	toolFlag = 3;//5
        }
      });
    //circle
    jToggleButton[4].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	toolFlag = 4;//4
        }
      });
    
    //pencil con
    Pencilcon = new JSlider(0, 10,5);
    Pencilcon.setPaintTicks(true);// 顯示標尺
    Pencilcon.setPaintLabels(true);//添加數字標籤
    Pencilcon.setMajorTickSpacing(5);// 20一大格
    Pencilcon.setMinorTickSpacing(1);// 5一小格
    Pencilcon.setInverted(false);
    Pencilcon.setOrientation(JSlider.VERTICAL);
    Pencilcon.setPreferredSize(new Dimension(50, 130));
    /*Pencilcon.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            scaleValue = Pencilcon.getValue();
        }
    });*/
    ToolPanel.add(Pencilcon);
    
    progressbar=new JProgressBar(); 
    progressbar.setOrientation(JProgressBar.HORIZONTAL);
    progressbar.setMinimum(0);
    progressbar.setMaximum(200);
    progressbar.setValue(0);
    progressbar.setStringPainted(true);
    progressbar.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
        }
    });
    //设置进度条的几何形状
    //progressbar.setPreferredSize(new Dimension(550,20));
    progressbar.setBorderPainted(true);
    progressbar.setBackground(Color.pink);
    progressbar.setString("");
    progressbar.setBounds(184,415,550,20);
    
    timer = new Timer(100, new ActionListener() {
        private int counter = 100;
        @Override
        public void actionPerformed(ActionEvent ae) {
        	timeCheck=false;
        	//progressbar.setValue(counter);
            progressbar.setValue(--counter);
            if (counter < 0) {
             	 System.out.println("換人畫畫");
             	 System.out.println(name+" "+drawCheck);
                timer.stop();//時間停止
            	 if(drawCheck) {
             		 output.println('|');//換人畫畫 here似乎是這裡有問題
             	 }
                counter=100;
            }
        }
    });
    
    
    // position des Modules
    jcbtn.setFont(font);
    jtfAddr.setBounds(25, 380, 135, 40);
    jtfName.setBounds(375, 380, 135, 40);
    jtfport.setBounds(200, 380, 135, 40);
    jcbtn.setBounds(575, 380, 100, 40);

    // couleur par defaut des Modules fil de discussion et liste des utilisateurs
    jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
    jtextListUsers.setBackground(Color.LIGHT_GRAY);

    // ajout des éléments
    jfr.add(jcbtn);
    jfr.add(jtextFilDiscuSP);
    jfr.add(jsplistuser);
    jfr.add(jtfName);
    jfr.add(jtfport);
    jfr.add(jtfAddr);
    jfr.setVisible(true);


    // info sur le Chat
    appendToPane(jtextFilDiscu, "<h4>The commands you can use：</h4>"
        +"<ul>"
        +"<li><b>@nickname </b> to send a Private Message to the user 'nickname'.</li>"
        +"<li><b>＃d3961b </b> to change the color of his nickname to hexadecimal indicate.</li>"
        +"<li><b>;)</b> some smileys are implemented.</li>"
        +"<li><b>Top arrow</b> to pick up last message typed.</li>"
        +"</ul><br/>");
    
    // On connect
    jcbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
          try {
        	  	  name = jtfName.getText();
     	          String port = jtfport.getText();
     	          serverName = jtfAddr.getText();
     	          PORT = Integer.parseInt(port);
     	
     	          appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
     	          server = new Socket(serverName, PORT);
     	
     	          appendToPane(jtextFilDiscu, "<span>Connected to " +
     	              server.getRemoteSocketAddress()+"</span>");
     	
     	          input = new BufferedReader(new InputStreamReader(server.getInputStream()));
     	          output = new PrintWriter(server.getOutputStream(), true);
     	          
    	          // send nickname to server
    	          output.println(name);
    	          
    	          check=true;
    	          
             } catch (Exception ex) {
                appendToPane(jtextFilDiscu, "<span>Could not connect to Server</span>");
               JOptionPane.showMessageDialog(jfr, ex.getMessage());
               check=false;//錯誤則不調整版面
             }
         if(check){
	          //版面調整
	          jfr.setSize(900,800);//change frame size
	          jsplistuser.setBounds(16, 36, 152, 692);
	          jtextFilDiscuSP.setBounds(184, 440, 690, 235);
	          name = jtfName.getText();
	          jfr.setTitle("【Draw Something】player:"+name);

	          // create new Read Thread
	          read = new Read();
	          read.start();
	          
  	          jfr.add(ALabel);
  	          jfr.add(Answer);
  	          jfr.add(QLabel);
  	          jfr.add(Question);
  	          jfr.add(CoverLabel);
  	          
	          jfr.add(CoverPanel);
	          jfr.add(ToolPanel);
	          jfr.add(ColorPanel);
	          jfr.add(DrawingPanel);
	          jfr.add(progressbar);
	          //paintboard draw = new paintboard(DrawingPanel);
	          jfr.remove(jtfName);
	          jfr.remove(jtfport);
	          jfr.remove(jtfAddr);
	          jfr.remove(jcbtn);
	          jfr.add(jsbtn);
	          jfr.add(jtextInputChatSP);
	          jfr.add(jsbtndeco);
	          jfr.revalidate();
	          jfr.repaint();
	          jtextFilDiscu.setBackground(Color.WHITE);
	          jtextListUsers.setBackground(Color.WHITE);
         }
      }

    });

    // on deco
    jsbtndeco.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent ae) {
    	//調整版面
    	jfr.setSize(700, 500);
    	jtextFilDiscuSP.setBounds(184, 36, 490, 320);
    	jsplistuser.setBounds(16, 36, 156, 320);
    	jfr.setSize(700, 500);//change frame size
    	
        jfr.remove(ALabel);
        jfr.remove(Answer);
        jfr.remove(QLabel);
        jfr.remove(Question);
        jfr.remove(CoverLabel);
        
    	jfr.remove(ToolPanel);
    	jfr.remove(DrawingPanel);
    	jfr.remove(ColorPanel);
    	jfr.remove(CoverPanel);
    	jfr.remove(progressbar);
        jfr.add(jtfName);
        jfr.add(jtfport);
        jfr.add(jtfAddr);
        jfr.add(jcbtn);
        jfr.remove(jsbtn);
        jfr.remove(jtextInputChatSP);
        jfr.remove(jsbtndeco);
        jfr.revalidate();
        jfr.repaint();
        read.interrupt();
        jtextListUsers.setText(null);
        jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
        jtextListUsers.setBackground(Color.LIGHT_GRAY);
        appendToPane(jtextFilDiscu, "<span>Connection closed.</span>");
        output.close();
      }
    });

  }

  // check if if all field are not empty
  public class TextListener implements DocumentListener{
    JTextField jtf1;
    JTextField jtf2;
    JTextField jtf3;
    JButton jcbtn;

    public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn){
      this.jtf1 = jtf1;
      this.jtf2 = jtf2;
      this.jtf3 = jtf3;
      this.jcbtn = jcbtn;
    }

    public void changedUpdate(DocumentEvent e) {}

    public void removeUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }
    public void insertUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }
  }

  // envoi des messages
  public void sendMessage() {
    try {
      String message = jtextInputChat.getText().trim();
      if (message.equals("")) {
        return;
      }
      this.oldMsg = message;
      output.println(message);//send message 
      jtextInputChat.requestFocus();
      jtextInputChat.setText(null);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, ex.getMessage());
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    ClientGui client = new ClientGui();
  }

  // read new incoming messages
  class Read extends Thread {
    public void run() {
      String message;
      while(!Thread.currentThread().isInterrupted()){
        try {
          message = input.readLine();
          if(message != null){
            if (message.charAt(0) == '[') {
            	//System.out.println("1:"+message);
              message = message.substring(1, message.length()-1);
              	//System.out.println("2:"+message);
              ArrayList<String> ListUser = new ArrayList<String>(
                  Arrays.asList(message.split(", "))
                  );
              jtextListUsers.setText(null);
              for (String user : ListUser) {
                appendToPane(jtextListUsers, "@" + user);
                //System.out.println(user);
              }
            }
            else if(message.charAt(0) =='$') {
            	if(message.equals("$draw")) {
	            	System.out.println(name+" draws now!");
	            	drawCheck=true;
	            	DrawingPanel.repaint();
	            	toolFlag=0;//pencil
	            	ToolPanel.setVisible(true);
	            	ColorPanel.setVisible(true);
	            	CoverPanel.setVisible(false);
	            	Answer.setVisible(true);
	            	CoverPanel.setVisible(false);
	            }
	            else if (message.equals("$guess")) {
	            	System.out.println(name+" guess now!");
	            	drawCheck=false;
	            	DrawingPanel.repaint();
	            	toolFlag=-1;//無法使用工具
	            	ToolPanel.setVisible(false);
	            	ColorPanel.setVisible(false);
	            	CoverPanel.setVisible(true);
	            	Answer.setVisible(false);
	            	CoverPanel.setVisible(true);
	            }
	            else if (message.substring(0,8).equals("$drawmsg")) {
	            	//System.out.println(name+": 同步別人的draw");
	            	getDraw(message);
	            }
	            else if (message.substring(0,8).equals("$drawQnA")) {
	          	  	StringTokenizer st = new StringTokenizer(message.substring(9),"#");
	          	  	//System.out.println(message);
	          	  	String answer = st.nextToken();
	          	  	String guess = st.nextToken();
	            	Answer.setText(answer);
	            	Question.setText(guess);
	            }
	            else if(message.substring(0,9).equals("$gameOver")){
	            	System.out.println(message.substring(10));
	            	StringTokenizer st = new StringTokenizer(message.substring(9),",");
	        		int score = Integer.parseInt(st.nextToken());
	        		System.out.println("here"+score);
	        		String numberOne = st.nextToken();
	        		
	        		JOptionPane.showMessageDialog(null,"遊戲結束!\n" + "最高分為: "+score+"分\n"+"No.1: "+numberOne);
	        		
	            }
             }
            //設定目前的值
            else if(message.charAt(0) == '&') {
  	         // System.out.println("start");
  	        //System.out.println(message);
  	          int count = Integer.parseInt(message.substring(1));
  	          
  	      		progressbar.setValue(count);
            }
            /*
            else if(message.charAt(0) =='/') {
              progressbar.setValue(0);
              System.out.println("stop");
            }*/
            else{
              appendToPane(jtextFilDiscu, message);
            }
          }
        }
        catch (IOException ex) {
          System.err.println("Failed to parse incoming message");
        }
      }
    }
  }

  // send html to pane
  private void appendToPane(JTextPane tp, String msg){
    HTMLDocument doc = (HTMLDocument)tp.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
    try {
      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      tp.setCaretPosition(doc.getLength());
    } catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public void sendDraw(int x1,int x2,int y1,int y2,Color c,int tool,int con) {
	  	int cNum=0;
		Color [] colors={Color.black,Color.GRAY,Color.red,Color.orange,Color.YELLOW,Color.green,
				Color.blue,Color.cyan,Color.magenta,Color.pink};
		for(int i=0;i<colors.length;i++) {
			if(c.equals(colors[i])) {
				cNum=i;
				break;
			}
		}
		String drawmsg="";
		drawmsg="$drawmsg,"+String.valueOf(x1)+","+String.valueOf(x2)+","+String.valueOf(y1)+","+String.valueOf(y2)+","+String.valueOf(cNum)
				+","+String.valueOf(tool)+","+String.valueOf(con);
		output.println(drawmsg);
  }
  
  public void getDraw(String dm) {
		Color [] colors={Color.black,Color.GRAY,Color.red,Color.orange,Color.YELLOW,Color.green,
				Color.blue,Color.cyan,Color.magenta,Color.pink};
		StringTokenizer st = new StringTokenizer(dm.substring(9),",");
		int x1 = Integer.parseInt(st.nextToken());
		int x2 = Integer.parseInt(st.nextToken());
		int y1 = Integer.parseInt(st.nextToken());
		int y2 = Integer.parseInt(st.nextToken());
		Color c = colors[Integer.parseInt(st.nextToken())];
		int tool = Integer.parseInt(st.nextToken());
		int con = Integer.parseInt(st.nextToken());
	    /*MODE*/
	    /*0:pencil 1:eraser 2:line 3:square 4:circle*/
	   // System.out.println(toolFlag);
		
		Graphics graphics = DrawingPanel.getGraphics();
		//color
		((Graphics2D) graphics).setColor(c);
		//stroke
	    ((Graphics2D) graphics).setStroke(new BasicStroke(con*2));
		switch(tool) {
			case 0://pencil
				((Graphics2D)graphics).drawLine(x1,y1,x2,y2);
				break;
			case 1://eraser
				((Graphics2D) graphics).setStroke(new BasicStroke(con*5));
				((Graphics2D) graphics).setColor(Color.WHITE);
				((Graphics2D)graphics).drawLine(x1,y1,x2,y2);
				break;
			case 2://line
				((Graphics2D)graphics).drawLine(x1,y1,x2,y2);
				break;
			case 3://rectangle
				((Graphics2D)graphics).drawRect(Math.min(x1,x2), Math.min(y1,y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
				break;
			case 4://circle
				((Graphics2D)graphics).drawOval(Math.min(x1,x2), Math.min(y1,y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
				break;
			default:
				break;
		}
  }
  
  //draw
  // read new incoming messages
  
  /*class Draw extends Thread {
    public void run() {
      //String message;
      while(!Thread.currentThread().isInterrupted()){
    	  if(timeCheck) {
    		  timeStart();
    	  }
    	  if(timeStopCheck){
    		  timeStop();
    	  }
      }//end of while
    }//end of run
  
	  public void timeStart() {
		 timer.start();
		 //System.out.println("Start倒數！");
	  }
	  public void timeStop() {
		  timer.stop();
		  //System.out.println("停止倒數！");
	  }
  }*/
}
