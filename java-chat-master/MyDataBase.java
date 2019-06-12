import java.util.ArrayList;
import java.util.Random;

public class MyDataBase {
	
	//随机产生数
	private static Random  r = new Random();
	
	//猜的数据库，装要猜的信息
	private static ArrayList<String> list = new ArrayList<String>();
	
	//数据库
	static{
		list.add("apple#English，5 words");
		list.add("stool#English，5 words");
		list.add("universe#English，8 words");
		list.add("desert#English，6 words");
		list.add("dream#English，5 words");
		list.add("hand#English，4 words");
		list.add("ticket#English，6 words");
		list.add("turkey#English，6 words");
		list.add("champion#English，8 words");
		list.add("rocket#English，6 words");
		list.add("computer#English，8 words");
		list.add("rice#English，4 words");
		list.add("TV#English，2 words");
		list.add("eclipse#English，6 words");
		list.add("earthquake#English，10 words");
		list.add("typhoon#English，7 words");
		list.add("hurricane#English，9 words");
		list.add("SpongeBob#English，9 words");
		list.add("Teletubbies#English，11 words");
		list.add("Teletubbies#English，11 words");
		
		/*list.add("手#一種人體部位，1個字");
		list.add("蘋果#一種水果，2個字");
		list.add("大便#虹婷喜歡的東西，2個字");
		list.add("菜鸟#一种动物，两个字");
		list.add("大树#一种植物，两个字");
		list.add("猪#一种动物，一个字");
		list.add("葡萄#一种水果，两个字");
		list.add("辣条#一种食品，两个字");
		list.add("苹果#一种水果，两个字");
		list.add("蜜蜂#一种动物，两个字");
		list.add("岑祚江#一条大河，三个字");
		list.add("华信教育#教育机构，四个字");
		list.add("程序员#一种职业，三个字");
		list.add("笔记本电脑#一种电子设备，五个字");
		list.add("鼠标#配件，两个字");
		list.add("三只松鼠#零食品牌，四个字");
		list.add("马拉松#一项运动名称，三个字");
		list.add("跳远#一项运动，两个字");
		list.add("兔子#一种动物，两个字");
		list.add("凤凰#一种鸟类，两个字");*/
	}
	
	//隨機從db中獲取一條消息
	public  String getInfo() {
		int i=r.nextInt(list.size());
		//System.out.println("隨機變數： "+i);
		//System.out.println(list.get(i));
		return list.get(i);

	}
}
