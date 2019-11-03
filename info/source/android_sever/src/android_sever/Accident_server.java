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
	
	/*���� ����*/
	int sever_port = 9999;
	String sever_ip = "192.168.1.2";
	Socket client;
	
	/*���� �� Ƚ��*/
	int amount = 0;
	int person = 0;
	
	/*��Ĺ�� ���� �б�/ ����*/
	BufferedReader input;
	static PrintWriter output;
	
	/*��Ĺ�� ���� �ְ���� ���ڿ�*/
	static String input_string;
	static String Output_string;
	
	/*���� Ŭ���̾�Ʈ*/
	static ArrayList<client_list> client_array;
	static int client_num;
	
	/*���� ���� ����Ʈ*/
	static ArrayList<String> accident_array;
	
	/*�÷��� ������ ���ڿ�*/
	String temp = new String();
	
	/*�Ľ̵� ���ڿ� �浵, ����*/
	String []parsed_string;
	static ArrayList<Double> lattitude,longitude;
	String union_string;
	
	/*������ ��θ� �̿��� �Ÿ� ���*/
	double seta, alph;
	
	/*��� �߰�*/
	private void array_add_acc(){
		temp = input_string.substring(2, input_string.length());
		accident_array.add(temp);
		state_frame.vector_add(temp);
		parsed_string = temp.split("/");
		lattitude.add(Double.parseDouble(parsed_string[3]));
		longitude.add(Double.parseDouble(parsed_string[4]));
	}
	
	/*���� �߰�*/
	private void array_add_stop(){
		temp = input_string.substring(2, input_string.length());
		System.out.println(temp+"\n");
		accident_array.add(temp);
		parsed_string = temp.split("/");
		lattitude.add(Double.parseDouble(parsed_string[3]));
		longitude.add(Double.parseDouble(parsed_string[4]));
	}
	
	/*���� ���� �ذ�*/
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
	
	/*�ֺ� ��� �˻�*/
	private void array_such(){
		temp = input_string.substring(2, input_string.length());
		parsed_string = temp.split("/");
		union_string = "";
		for( int i = 0 ; i < lattitude.size(); i++){
			if(iscareful(i)){//�����ؾ� �Ǵ� ��Ȳ �̸�
				union_string = union_string + "@" + lattitude.get(i)+"/"+longitude.get(i);
			}
		}
	}
	
	/*���� ���� ������ �ֺ� ��� �˻�*/
	private void service_such(){
		temp = input_string.substring(2, input_string.length());
		parsed_string = temp.split("/");
		Output_string = "FALSE";
		for( int i = 0 ; i < lattitude.size(); i++){
			if(iscareful(i)){//�����ؾ� �Ǵ� ��Ȳ �̸�
				Output_string = "TRUE";
				break;
			}
		}
	}
	
	/*�����Ÿ��� ����Ȳ �˻�*/
	private boolean iscareful(int i){
		/*�Ľ̵� ���� -> Double������ ��ȯ*/
		Double temp_latti = Double.parseDouble(parsed_string[3]);
		Double temp_longi = Double.parseDouble(parsed_string[4]);
		
		/*�Ÿ����*/
		seta = Math.acos(Math.sin(Math.toRadians(temp_latti.doubleValue()))
				*Math.sin(Math.toRadians(lattitude.get(i).doubleValue()))
				+Math.cos(Math.toRadians(temp_latti.doubleValue()))
				*Math.cos(Math.toRadians(lattitude.get(i).doubleValue()))
				*Math.cos(Math.abs(Math.toRadians(longitude.get(i).doubleValue())
						-Math.toRadians(temp_longi.doubleValue()))));
		alph = Math.toDegrees(seta);
		alph = alph * 60 * 1.1515 * 1.609344;//km
		alph = alph*1000;//m
		
		if(alph <1000)// 1000m �̳��� ���. ���� ���� ����
			return true;
		else// 1000m�̳��� ���, ���� ���� ����
			return false;
	}
	
	/*��� ����Ʈ ���*/
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
			ServerSocket serverSocket = new ServerSocket(sever_port);//��Ʈ�� ������� �غ�
			Output_string = "";
			union_string = "";
			while(true){
				System.out.println("sever : Watting..");//������ ��ٸ�
				state_frame.access_amout.setText("ONLINE \n ����Ƚ�� : "+ amount + "\n ������ �� " + person +" \n " + client_array.size());	
				client = serverSocket.accept();//���� ��
				amount++;
				System.out.println("access popular : "+amount);
				state_frame.access_amout.setText("ONLINE \n����Ƚ�� : "+ amount);
				client_array.add(new client_list(client));	
				client_array.get(client_array.size()-1).start();
			/*	int i = 0;	
				while(true){		
					System.out.println("sever : Recevieng");
					try{
						//writer ����
						output = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));
						
						if(i== 0){//ó�� �������� �� �¶��� �˷���
							output.println("ONLINE");
							System.out.println("send : ONLINE");
						}
						output.flush();
						
						//reader ����
						input = new BufferedReader(
								new InputStreamReader(client.getInputStream()));
						input_string = input.readLine();
						System.out.println(i+".sever : Receive < "+input_string+" >");
						
						//���� ���� ��ɾ�
						if(input_string.compareTo("#33") == 0){
							//�޼��� ������
							output.println("Sever Say <" +"Output_string"+">");
							output.flush();
							
							break;
						}else if(input_string.charAt(0) == '0'){
							//�Ϲ����� ������
							array_such();
							state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+"\nmove" + temp);
							//�޼��� ������
							Output_string = union_string;
							output.println(union_string);
							output.flush();
							
						}else if (input_string.charAt(0) == '1'){
							//���
							array_add_acc();
							state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+"\naccident" + temp);
							//�޼��� ������
							output.println("Sever Say <" +"Output_string"+">");
							output.flush();
							
						}else if (input_string.charAt(0) == '2'){
							//����
							array_add_stop();
							state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+"\nstop" + temp);
							//�޼��� ������
							output.println("Sever Say <" +"O_s"+">");
							output.flush();
						}else if (input_string.charAt(0) == '3'){
							//�ذ�
							array_del();
							state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+"\nsolve" + temp);	
							//�޼��� ������
							output.println("Sever Say <" +"O_s"+">");
							output.flush();
							
						}else if (input_string.charAt(0) == '4'){
							//�ȵ���̵��� service�� �˻��� ��
							service_such();
							//�޼��� ������
							state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+"\nservice" + temp);	
							
							output.println(Output_string);
							output.flush();		
						}
						
						System.out.println("send : "+Output_string);		
					}catch(Exception e){
						System.out.println(e);
						break;
					}finally {
						i++;// i�� ä�� Ƚ��
					}
				}*/
				//client.close();
				//person--;
				state_frame.access_amout.setText("ONLINE \n ����Ƚ�� : "+ amount + "\n ������ �� " + person);
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
					//writer ����
					output = new PrintWriter(
							new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
					
					if(i== 0){//ó�� �������� �� �¶��� �˷���
						output.println("ONLINE");
						System.out.println("send : ONLINE");
					}
					output.flush();
					
					//reader ����
					input = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					input_string = input.readLine();
					System.out.println(i+".sever : Receive < "+input_string+" >");
					
					//���� ���� ��ɾ�
					if(input_string.compareTo("#33") == 0){
						//�޼��� ������
						output.println("Sever Say <" +"Connecting.."+">");
						output.flush();
						
						break;
					}else if(input_string.charAt(0) == '0'){
						//�Ϲ����� ������
						array_such();
						state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+
								"\n"+client.getInetAddress().getHostAddress()+" : move" + temp);
						//�޼��� ������
						Output_string = union_string;
						output.println(union_string);
						output.flush();
						
					}else if (input_string.charAt(0) == '1'){
						//���
						array_add_acc();
						state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+
								"\n"+client.getInetAddress().getHostAddress()+"accident" + temp);
						//�޼��� ������
						output.println("Sever Say <" +"Output_string"+">");
						output.flush();
						
					}else if (input_string.charAt(0) == '2'){
						//����
						array_add_stop();
						state_frame.all_msg.setText(state_frame.all_msg.getText().toString()
								+"\n"+client.getInetAddress().getHostAddress()+"stop" + temp);
						//�޼��� ������
						output.println("Sever Say <" +"O_s"+">");
						output.flush();
					}else if (input_string.charAt(0) == '3'){
						//�ذ�
						array_del();
						state_frame.all_msg.setText(state_frame.all_msg.getText().toString()+
								"\n"+client.getInetAddress().getHostAddress()+"solve" + temp);	
						//�޼��� ������
						output.println("Sever Say <" +"O_s"+">");
						output.flush();
						
					}else if (input_string.charAt(0) == '4'){
						//�ȵ���̵��� service�� �˻��� ��
						service_such();
						//�޼��� ������
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
					i++;// i�� ä�� Ƚ��
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
		state_frame.access_amout.setText("ONLINE "+"\n"+"����Ƚ�� : 0");
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
		
		severTread.start();// ���� ����	
	}
}
