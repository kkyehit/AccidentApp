package android_sever;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javax.management.remote.TargetedNotification;
import javax.swing.JFrame;
import javax.swing.event.TreeWillExpandListener;

import java.lang.Math;

public class Accident_server implements Runnable {
	
	/*서버 정보*/
	int sever_port = 9999;
	String sever_ip = "192.168.1.2";
	Socket client;
	
	/*접속 한 횟수*/
	int amount = 0;
	int person = 0;
	
	/*소캣을 통해 읽기/ 쓰기*/
	BufferedReader input;
	static PrintWriter output;
	
	/*소캣을 통해 주고받을 문자열*/
	static String input_string;
	static String Output_string;
	
	/*다중 클라이언트*/
	static ArrayList<client_list> client_array;
	static int client_num;
	
	/*사고및 정차 리스트*/
	static ArrayList<String> accident_array;
	
	/*플레그 제외한 문자열*/
	String temp = new String();
	
	/*파싱된 문자와 경도, 위도*/
	String []parsed_string;
	static ArrayList<Double> lattitude,longitude;
	String union_string;
	
	/*위도와 경로를 이용해 거리 계산*/
	double seta, alph;
	
	/*사고 추가*/
	private void array_add_acc(){
		temp = input_string.substring(2, input_string.length());
		accident_array.add(temp);
		state_frame.vector_add(temp);
		parsed_string = temp.split("/");
		lattitude.add(Double.parseDouble(parsed_string[3]));
		longitude.add(Double.parseDouble(parsed_string[4]));
	}
	
	/*정차 추가*/
	private void array_add_stop(){
		temp = input_string.substring(2, input_string.length());
		System.out.println(temp+"\n");
		accident_array.add(temp);
		parsed_string = temp.split("/");
		lattitude.add(Double.parseDouble(parsed_string[3]));
		longitude.add(Double.parseDouble(parsed_string[4]));
	}
	
	/*사고및 정차 해결*/
	private void array_del(){
		temp = input_string.substring(2, input_string.length());
		for(int i = 0; i < accident_array.size(); i++){
		if(accident_array.get(i).equals(temp)){
				accident_array.remove(i);
				lattitude.remove(i);
				longitude.remove(i);
				break;
			}
		}
		state_frame.del_singo(temp);
	}
	
	/*주변 사고 검색*/
	private void array_such(){
		temp = input_string.substring(2, input_string.length());
		parsed_string = temp.split("/");
		union_string = "";
		for( int i = 0 ; i < lattitude.size(); i++){
			if(iscareful(i)){//조심해야 되는 상황 이면
				union_string = union_string + "@" + lattitude.get(i)+"/"+longitude.get(i);
			}
		}
	}
	
	/*서비스 상태 에서의 주변 사고 검색*/
	private void service_such(){
		temp = input_string.substring(2, input_string.length());
		parsed_string = temp.split("/");
		Output_string = "FALSE";
		for( int i = 0 ; i < lattitude.size(); i++){
			if(iscareful(i)){//조심해야 되는 상황 이면
				Output_string = "TRUE";
				break;
			}
		}
	}
	
	/*일정거리내 사고상황 검색*/
	private boolean iscareful(int i){
		/*파싱된 문자 -> Double형으로 변환*/
		Double temp_latti = Double.parseDouble(parsed_string[3]);
		Double temp_longi = Double.parseDouble(parsed_string[4]);
		
		/*거리계산*/
		seta = Math.acos(Math.sin(Math.toRadians(temp_latti.doubleValue()))
				*Math.sin(Math.toRadians(lattitude.get(i).doubleValue()))
				+Math.cos(Math.toRadians(temp_latti.doubleValue()))
				*Math.cos(Math.toRadians(lattitude.get(i).doubleValue()))
				*Math.cos(Math.abs(Math.toRadians(longitude.get(i).doubleValue())
						-Math.toRadians(temp_longi.doubleValue()))));
		alph = Math.toDegrees(seta);
		alph = alph * 60 * 1.1515 * 1.609344;//km
		alph = alph*1000;//m
		
		if(alph <1000)// 1000m 이내에 사고. 정차 정보 있음
			return true;
		else// 1000m이내에 사고, 정차 정보 없음
			return false;
	}
	
	/*사고 리스트 출력*/
	private void array_print(){
		for(int i = 0; i < accident_array.size(); i++){
			System.out.println(lattitude.get(i)+"\n");
			System.out.println(longitude.get(i)+"\n");
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			System.out.println("sever : Connecting...");
			ServerSocket serverSocket = new ServerSocket(sever_port);//포트로 연결받을 준비
			Output_string = "";
			union_string = "";
			while(true){
				System.out.println("sever : Watting..");//연결을 기다림
				state_frame.access_amout.setText("ONLINE \n 접속횟수 : "+ amount + "\n 접속자 수 " + person +" \n " + client_array.size());	
				client = serverSocket.accept();//연결 됨
				amount++;
				System.out.println("access popular : "+amount);
				state_frame.access_amout.setText("ONLINE \n접속횟수 : "+ amount);
				client_array.add(new client_list(client));	
				client_array.get(client_array.size()-1).start();
			/*	int i = 0;	
				while(true){		
					System.out.println("sever : Recevieng");
					try{
						//writer 설정
						output = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));
						
						if(i== 0){//처음 연결했을 때 온라인 알려줌
							output.println("ONLINE");
							System.out.println("send : ONLINE");
						}
						output.flush();
						
						//reader 설정
						input = new BufferedReader(
								new InputStreamReader(client.getInputStream()));
						input_string = input.readLine();
						System.out.println(i+".sever : Receive < "+input_string+" >");
						
						//연결 종료 명령어
						if(input_string.compareTo("#33") == 0){
							//메세지 보내기
							output.println("Sever Say <" +"Output_string"+">");
							output.flush();
							
							break;
						}else if(input_string.charAt(0) == '0'){
							//일반적인 움직임
							array_such();
							state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+"\nmove" + temp);
							//메세지 보내기
							Output_string = union_string;
							output.println(union_string);
							output.flush();
							
						}else if (input_string.charAt(0) == '1'){
							//사고
							array_add_acc();
							state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+"\naccident" + temp);
							//메세지 보내기
							output.println("Sever Say <" +"Output_string"+">");
							output.flush();
							
						}else if (input_string.charAt(0) == '2'){
							//정차
							array_add_stop();
							state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+"\nstop" + temp);
							//메세지 보내기
							output.println("Sever Say <" +"O_s"+">");
							output.flush();
						}else if (input_string.charAt(0) == '3'){
							//해결
							array_del();
							state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+"\nsolve" + temp);	
							//메세지 보내기
							output.println("Sever Say <" +"O_s"+">");
							output.flush();
							
						}else if (input_string.charAt(0) == '4'){
							//안드로이드의 service로 검색할 때
							service_such();
							//메세지 보내기
							state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+"\nservice" + temp);	
							
							output.println(Output_string);
							output.flush();		
						}
						
						System.out.println("send : "+Output_string);		
					}catch(Exception e){
						System.out.println(e);
						break;
					}finally {
						i++;// i는 채팅 횟수
					}
				}*/
				//client.close();
				//person--;
				state_frame.access_amout.setText("ONLINE \n 접속횟수 : "+ amount + "\n 접속자 수 " + person);
			}
		}catch(Exception e){
			System.out.println("sever : eroor");
			e.printStackTrace();
		}
	}
	
	static State_frame state_frame;
	
	public class client_list extends Thread{
		Socket socket;
		
		int this_num;
		public client_list(Socket client) {
			// TODO Auto-generated constructor stub
			socket = client;
			this_num = client_num;
			client_num++;
			person++;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int i = 0;	
			while(true){		
				System.out.println("sever : Recevieng");
				try{
					//writer 설정
					output = new PrintWriter(
							new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
					
					if(i== 0){//처음 연결했을 때 온라인 알려줌
						output.println("ONLINE");
						System.out.println("send : ONLINE");
					}
					output.flush();
					
					//reader 설정
					input = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					input_string = input.readLine();
					System.out.println(i+".sever : Receive < "+input_string+" >");
					
					//연결 종료 명령어
					if(input_string.compareTo("#33") == 0){
						//메세지 보내기
						output.println("Sever Say <" +"Connecting.."+">");
						output.flush();
						
						break;
					}else if(input_string.charAt(0) == '0'){
						//일반적인 움직임
						array_such();
						state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+
								"\n"+client.getInetAddress().getHostAddress()+" : move" + temp);
						//메세지 보내기
						Output_string = union_string;
						output.println(union_string);
						output.flush();
						
					}else if (input_string.charAt(0) == '1'){
						//사고
						array_add_acc();
						state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+
								"\n"+client.getInetAddress().getHostAddress()+"accident" + temp);
						//메세지 보내기
						output.println("Sever Say <" +"Output_string"+">");
						output.flush();
						
					}else if (input_string.charAt(0) == '2'){
						//정차
						array_add_stop();
						state_frame.all_msg.setText(state_frame.all_msg.getText().toString()
								+"\n"+client.getInetAddress().getHostAddress()+"stop" + temp);
						//메세지 보내기
						output.println("Sever Say <" +"O_s"+">");
						output.flush();
					}else if (input_string.charAt(0) == '3'){
						//해결
						array_del();
						state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+
								"\n"+client.getInetAddress().getHostAddress()+"solve" + temp);	
						//메세지 보내기
						output.println("Sever Say <" +"O_s"+">");
						output.flush();
						
					}else if (input_string.charAt(0) == '4'){
						//안드로이드의 service로 검색할 때
						service_such();
						//메세지 보내기
						state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+
								"\n"+client.getInetAddress().getHostAddress()+"service" + temp);	
						
						output.println(Output_string);
						output.flush();		
					}
					
					System.out.println("send : "+Output_string);		
				}catch(Exception e){
					System.out.println(e);
					break;
				}finally {
					i++;// i는 채팅 횟수
				}
			}

			try {
				socket.close();
				person--;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("socket close error : "+ e);
			}
			System.out.println("sever : done");
			
		}
	}
	
	public static void main(String[] args) {
		Thread severTread = new Thread(new Accident_server());
		state_frame = new State_frame();
		state_frame.access_amout.setText("ONLINE "+"\n"+"접속횟수 : 0");
		accident_array = new ArrayList<String>();
		client_array = new ArrayList<client_list>();
		lattitude = new ArrayList<Double>();
		longitude = new ArrayList<Double>();
		client_num =0 ;
		accident_array.add("16/0:43:11/Latting/37.4437681/126.797789/");
		state_frame.vector_add("16/0:43:11/Latting/37.4437681/126.797789/");
		accident_array.add("17/0:43:11/Latting/37.4437683/126.797789/");
		state_frame.vector_add("17/0:43:11/Latting/37.4437683/126.797789/");
		accident_array.add("17/0:43:11/Latting/37.4437682/126.797789/");
		state_frame.vector_add("17/0:43:11/Latting/37.4437682/126.797789/");
		accident_array.add("16/0:43:11/Latting/37.4437681/126.797789/");
		state_frame.vector_add("16/0:43:11/Latting/37.4437681/126.797789/");
		accident_array.add("17/0:43:11/Latting/37.4437683/126.797789/");
		state_frame.vector_add("17/0:43:11/Latting/37.4437683/126.797789/");
		accident_array.add("17/0:43:11/Latting/37.4437682/126.797789/");
		state_frame.vector_add("17/0:43:11/Latting/37.4437682/126.797789/");
		
		longitude.add(126.797789);
		lattitude.add(37.4437681);
		longitude.add(126.797789);
		lattitude.add(37.4437682);
		longitude.add(126.797789);
		lattitude.add(37.4437683);
		longitude.add(126.797789);
		lattitude.add(37.4437681);
		longitude.add(126.797789);
		lattitude.add(37.4437682);
		longitude.add(126.797789);
		lattitude.add(37.4437683);
		
		severTread.start();// 서버 시작	
	}
}
