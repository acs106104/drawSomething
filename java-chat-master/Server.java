import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.Timer;

import java.util.regex.Matcher;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Server {

  private int port;
  private List<User> clients;
  private ServerSocket server;
  private String Painter = "nickname";
  private int whichDraw =0;
  private int turns = 0;
  public boolean startCheck=false;
  public String answer="#answer？";
  public String guess;
  private MyDataBase db;
  public int point = 10;
  public Timer timer;
  public int highestpoint=0;
  public User numberOne;

  public static void main(String[] args) throws IOException {
    new Server(12345).run();//啟動伺服器
  }

  public Server(int port) {
    this.port = port;
    this.clients = new ArrayList<User>();//允許多個client 連接到同一個port
    db = new MyDataBase();
    
    timer = new Timer(100, new ActionListener() {
        private int counter = 100;
        @Override
        public void actionPerformed(ActionEvent ae) {
        	//System.out.println("hihi!");
        	timeCount(counter);
        	//System.out.println(counter);
        	--counter;
      
            if (counter < 0) {
             	 changeDraw();
             	setTurns(getTurns() + 1);
                //timer.stop();//時間停止
                counter=100;
            }
            if(highestpoint>=20) {
      	  	  StopGame();
      	  	  timer.stop();
      	  	  /*if(gamecheck()) {
      	  		changeDraw();
      	  		turns++;
      	  		timer.start();
      	  	  }
      	  	  else timer.stop();*/
            }
        }
    });
  }

  public void run() throws IOException {
    server = new ServerSocket(port) {
      protected void finalize() throws IOException {
        this.close();
      }
    };
    System.out.println("Port 12345 is now open.");//成功啟動的message

    while (true) {
      // accepts a new client 允許新的client加入
      Socket client = server.accept(); 

      // get nickname of newUser 獲得client的暱稱
      String nickname = (new Scanner ( client.getInputStream() )).nextLine();
      nickname = nickname.replace(",", ""); //  ',' use for serialisation
      nickname = nickname.replace(" ", "_");
      /*有新的client加入則server印出通知*/
      System.out.println("New Client: \"" + nickname + "\"\n\t     Host:" + client.getInetAddress().getHostAddress());

      // create new User 建立一個新的使用者
      User newUser = new User(client, nickname);

      // add newUser message to list 把新的使用者的部分加入到client
      this.clients.add(newUser); 

      // Welcome msg
      newUser.getOutStream().println(
          "<img src='https://www.kizoa.fr/img/e8nZC.gif' height='42' width='42'>"
          + "<b>Welcome</b> " + newUser.toStringTalk() +
          "<img src='https://www.kizoa.fr/img/e8nZC.gif' height='42' width='42'>"
          );

      // create a new thread for newUser incoming messages handling
      new Thread(new UserHandler(this, newUser)).start();
      
      if(clients.size() >= 2) {
    	  System.out.println("There are >2 clients! You can start your game!");
    	  startCheck=true;//確定有兩人以上 準備開始遊戲
      }
      else if(clients.size() < 2) {
    	  reset();
    	  startCheck=false;
    	  System.out.println("There are <2 clients! You can't start your game!");
    	  timeCount(0);
      }
      if(turns == 0 && startCheck==true) {
    	  reset();
    	  changeDraw();
    	  turns++;
    	  timer.start();
      }
      else if(turns >= 0 && startCheck==false) {
    	  turns=0;//重新從0計算
    	  timer.stop();//停止計時
      }
     
    }
  }
  
  public void HighestScorePerson() {
	for (User client : this.clients) {
		if(client.getScore()>highestpoint) {
			highestpoint=client.getScore();//最高分
			numberOne=client;//最高分的人
		}
	}
	System.out.println("H!");
	System.out.println(highestpoint);
  }
  public boolean gamecheck() {
	  if(clients.size() >= 2) {
    	  return true;
	  }
	  else return false;
  }
  
  public void reset() {
	  turns=0;
	  highestpoint=0;
	  numberOne=null;
  }
  public void StopGame() {
		for (User client : this.clients) {
			client.getOutStream().println("$gameOver"+String.valueOf(highestpoint)+","+numberOne.getNickname());
		}
		System.out.println("done!");
		reset();
  }
  
  //倒數計時
  public void timeCount(int t) {
		for (User client : this.clients) {
			client.getOutStream().println("&"+String.valueOf(t));
			//System.out.println("&"+String.valueOf(t));
		}
  }
  
  public void ansUnloctoUser() {
	for (User client : this.clients) {
		client.anslock=false;
	}	
  }

// delete a user from the list
  public void removeUser(User user){
    this.clients.remove(user);
    if(clients.size()<2) {
      turns=0;//重新從0計算
   	  System.out.println("Remove and then stop!");
    }
  }

  // send incoming msg to all Users  訊息傳送格式(每個使用者）
  public void broadcastMessages(String msg, User userSender) {
    for (User client : this.clients) {
      client.getOutStream().println(
          userSender.toStringTalk() + "<span>: " + msg+"</span>");
    }
  }

  // send list of clients to all Users
  public void broadcastAllUsers(){
    for (User client : this.clients) {
      client.getOutStream().println(this.clients);
    }
  }

  // send message to a User (String) 傳訊息給指定的使用者
  public void sendMessageToUser(String msg, User userSender, String user){
    boolean find = false;
    for (User client : this.clients) {
      if (client.getNickname().equals(user) && client != userSender) {
        find = true;
        userSender.getOutStream().println(userSender.toStringTalk() + " -> " + client.toStringTalk() +": " + msg);
        client.getOutStream().println(
            "(<b>Private</b>)" + userSender.toStringTalk() + "<span>: " + msg+"</span>");
      }
    }
    if (!find) {
      userSender.getOutStream().println(userSender.toStringTalk() + " -> (<b>no one!</b>): " + msg);
    }
  }
  
  public void changeDraw() {
	HighestScorePerson();//分數小計
	point=10;
	ansUnloctoUser();
	int n = clients.size();//當前連線人數
	int which=0;
	
	if(getTurns() == 0) {
		Painter = clients.get(0).getNickname();
		System.out.println(Painter+": I am the first person to draw!");
		clients.get(0).getOutStream().println("$draw");//傳送指令給他接收！
		whichDraw=0;
	}else {//換下一個人畫
	    boolean find = false;
	    for (User client : this.clients) {
	      which++;
	      if (client.getNickname().equals(Painter)) {
	        find = true;//找到當前的painter是誰 換下一個人畫！
	        System.out.println("換下一個人畫畫++which: "+which);
	        break;
	      }
	    }//end of for
	    
	    if(which>=n) {
	    	which=0;//從頭開始第一個人畫畫
	    }
	    System.out.println("which2: "+which);
	    if (!find) {
	    	Painter = clients.get(whichDraw).getNickname();
	    	clients.get(whichDraw).getOutStream().println("$draw");//那個人離開了，就讓遞補的人畫
	    }
	    Painter =clients.get(which).getNickname();
	    System.out.println(clients.get(which).getNickname()+": I am the next person to draw!");
	    whichDraw = which;
	    clients.get(which).getOutStream().println("$draw");//傳送指令給他接收！
	}//end of else
	ChangeGuess();//其餘人傳“=guess”
	clients.get(which).anslock=true;//畫畫的人猜對沒有用
	QnA();
	//timeStartoUser();
  }
  
  //其他人為guess
  public void ChangeGuess(){
	  //System.out.println("whichDraw: "+whichDraw);
	 for(int i=0;i<clients.size();i++) {
		if(i==whichDraw) {
			 //clients.get(i).getOutStream().println("$draw");//傳送指令給他接收！
			System.out.println(clients.get(i).getNickname()+": Draw!");
		}
		else {
			clients.get(i).getOutStream().println("$guess");
			System.out.println(clients.get(i).getNickname()+": Guess!");
		}
	 }
  }
  //產生題目
  public void QnA() {
	  String QandA=db.getInfo();
	  StringTokenizer st = new StringTokenizer(QandA,"#");
	  answer=st.nextToken();
	  guess=st.nextToken();
	  for (User client : this.clients) {
		  client.getOutStream().println("$drawQnA," + QandA);  
	  } 
  }
  //把畫的內容同步至其他人畫板
  public void broadcastDraw(String dmsg){
	  //System.out.println("\nBroadcastDraw\n\nwhichDraw: "+whichDraw);
	 for(int i=0;i<clients.size();i++) {
		if(i==whichDraw) {
		}
		else {
			clients.get(i).getOutStream().println(dmsg);
		}
	 }
  }

public int getTurns() {
	return turns;
}

public void setTurns(int turns) {
	this.turns = turns;
}
	
}

class UserHandler implements Runnable {

  private Server server;
  private User user;

  public UserHandler(Server server, User user) {
    this.server = server;
    this.user = user;
    this.server.broadcastAllUsers();
  }

  public void run() {
    String message;

    // when there is a new message, broadcast to all 有新的訊息 傳給每個使用者(同步)
    Scanner sc = new Scanner(this.user.getInputStream());
    while (sc.hasNextLine()) {
      message = sc.nextLine();

      // smiley 表情符號替換
      message = message.replace(":)", "<img src='http://4.bp.blogspot.com/-ZgtYQpXq0Yo/UZEDl_PJLhI/AAAAAAAADnk/2pgkDG-nlGs/s1600/facebook-smiley-face-for-comments.png'>");
      message = message.replace(":D", "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
      message = message.replace(":d", "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
      message = message.replace(":(", "<img src='http://2.bp.blogspot.com/-rnfZUujszZI/UZEFYJ269-I/AAAAAAAADnw/BbB-v_QWo1w/s1600/facebook-frown-emoticon.png'>");
      message = message.replace("-_-", "<img src='http://3.bp.blogspot.com/-wn2wPLAukW8/U1vy7Ol5aEI/AAAAAAAAGq0/f7C6-otIDY0/s1600/squinting-emoticon.png'>");
      message = message.replace(";)", "<img src='http://1.bp.blogspot.com/-lX5leyrnSb4/Tv5TjIVEKfI/AAAAAAAAAi0/GR6QxObL5kM/s400/wink%2Bemoticon.png'>");
      message = message.replace(":P", "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
      message = message.replace(":p", "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
      message = message.replace(":o", "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");
      message = message.replace(":O", "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");

      // Gestion des messages private
      if (message.charAt(0) == '@'){
        if(message.contains(" ")){
          System.out.println("private msg : " + message);
          int firstSpace = message.indexOf(" ");
          String userPrivate= message.substring(1, firstSpace);
          server.sendMessageToUser(
              message.substring(
                firstSpace+1, message.length()
                ), user, userPrivate
              );
        }

      // Gestion du changement
      }
      else if (message.charAt(0) == '#'){
        user.changeColor(message);
        // update color for all other users
        this.server.broadcastAllUsers();
      }
      /*
      else if(message.charAt(0) == '|') {
    	server.setTurns(server.getTurns() + 1);
    	server.changeDraw();
        System.out.println("\nchange $\n");
      }*/
      else if(message.charAt(0) == '$') {
    	  if(message.length()>=9) {
		      if(message.substring(0,8).equals("$drawmsg")) {
		    	  //System.out.println(user.getNickname()+": 在畫畫！");
		    	  server.broadcastDraw(message);
		      }
	      }
      }
      else{
        // update user list
    	  String msgA=message.replace(server.answer,"***");
    	//猜對了
    	if(!message.equals(msgA)) {
    		//System.out.println(user.getNickname()+":答對了！");
    		int point=server.point;
    		if(message.equals(server.answer)){
	    		if(!user.anslock) {
		    		if(server.point<=2) {
		    			user.setScore(point);
		    		}
		    		else {
		        		user.setScore(point);
		        		server.point=server.point-2;
		    		}
		    		server.broadcastAllUsers();
		    		server.broadcastMessages(user.getNickname()+"答對!  <span style='color:red'>+"+point+"</span>", user);
		    		user.anslock=true;//表示回答過了 不可以再回答第二次
		    		server.broadcastMessages(msgA, user);//可以回答 不過沒有用～
	    		}
	    		else
	    			server.broadcastMessages(msgA, user);
    		}
    		else
    			server.broadcastMessages(message, user);//可以回答 不過沒有用～
    	}else
    		server.broadcastMessages(message, user);
      }
    }
    // end of Thread
    this.server.removeUser(user);
    this.server.broadcastAllUsers();
    sc.close();
  }
}

class User {
  private static int nbUser = 0;
  private int userId;
  private PrintStream streamOut;
  private InputStream streamIn;
  private String nickname;
  private Socket client;
  private String color;
  private int score;//計算分數
  public boolean anslock=false;
  

  // constructor
  public User(Socket client, String name) throws IOException {
    this.streamOut = new PrintStream(client.getOutputStream());
    this.streamIn = client.getInputStream();
    this.client = client;
    this.nickname = name;
    this.userId = nbUser;
    this.color = ColorInt.getColor(this.userId);
    nbUser += 1;
  }

  // change color user 改變使用者顯示名稱 顏色
  public void changeColor(String hexColor){
    // check if it's a valid hexColor
    Pattern colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})");
    Matcher m = colorPattern.matcher(hexColor);
    if (m.matches()){
      Color c = Color.decode(hexColor);
      // if the Color is too Bright don't change
      double luma = 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue(); // per ITU-R BT.709
      if (luma > 160) {
        this.getOutStream().println("<b>Color Too Bright</b>");
        return;
      }
      this.color = hexColor;
      this.getOutStream().println("<b>Color changed successfully</b> " + this.toStringTalk());
      return;
    }
    this.getOutStream().println("<b>Failed to change color</b>");
  }

  // getteur
  public PrintStream getOutStream(){
    return this.streamOut;
  }

  public InputStream getInputStream(){
    return this.streamIn;
  }
  
  public void setScore(int score){
	   this.score+=score;
  }
  public int getScore(){
	    return this.score;
  }

  public String getNickname(){
    return this.nickname;
  }

  // print user with his color
  public String toString(){

    return "<u><span style='color:"+ this.color
      +"'>" + this.getNickname() + "</span> : "+this.getScore()+"分</u>";

  }
  
  public String toStringTalk(){

	    return "<u><span style='color:"+ this.color
	      +"'>" + this.getNickname() + "</span></u>";

	  }
}

class ColorInt {
    public static String[] mColors = {
            "#3079ab", // dark blue
            "#e15258", // red
            "#f9845b", // orange
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#e0ab18", // mustard
            "#f092b0", // pink
            "#e8d174", // yellow
            "#e39e54", // orange
            "#d64d4d", // red
            "#4d7358", // green
    };

    public static String getColor(int i) {
        return mColors[i % mColors.length];
    }
}
