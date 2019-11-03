package android_sever;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/*********
 * 상태 창   *
 *********/
public class State_frame{
	JFrame frame;
	JTextArea all_msg;
	JScrollPane Scroll_pane,list_scroll,select_list_scroll;
	Container content_pane;
	JLabel access_amout;
	JPanel penel;
	JList<String> jList,select_List;
	Vector<String> list_vector,select_vector;
	JButton select;
	
	public State_frame() {
		// TODO Auto-generated constructor stub
		frame = new JFrame();
		
		frame.setTitle("상태 창");
		frame.setSize(1000,1000);
		
		content_pane = frame.getContentPane();
		all_msg = new JTextArea(" ");
		access_amout = new JLabel("amount");
		penel = new JPanel();
		jList = new JList<String>();
		select_List = new JList<String>();
		list_vector = new Vector<String>();
		select_vector = new Vector<String>();
		Scroll_pane = new JScrollPane(all_msg);
		list_scroll = new JScrollPane(jList);
		select_list_scroll = new JScrollPane(select_List);
		select = new JButton("Select");
		
		select.setBounds(640, 420, 80, 30);
		
		all_msg.setSize(400, 500);
		all_msg.setFont(new Font("", 2, 10));
		all_msg.setText("");
	
		penel.add(access_amout);
		penel.setLayout(null);
		penel.setLocation(0,0);
		penel.setSize(400,100);
		penel.setBackground(new Color(244, 244, 1));
		
		content_pane.setLayout(null);
		
		access_amout.setSize(400,100);
		access_amout.setLocation(0,0);
		access_amout.setFont(new Font("", 1, 15));
			
		Scroll_pane.setSize(400,700);
		Scroll_pane.setLocation(0,100);
		
		list_scroll.setSize(400, 300);
		list_scroll.setLocation(500,100);
		
		jList.setListData(list_vector);
		jList.setSize(400,300);
		
		select_list_scroll.setSize(400, 300);
		select_list_scroll.setLocation(500, 500);
		
		select_List.setListData(select_vector);
		select_List.setSize(400,300);
		
		content_pane.add(penel);
		content_pane.add(Scroll_pane);
		content_pane.add(list_scroll);
		content_pane.add(select);
		content_pane.add(select_list_scroll);

		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		list_scroll.setVisible(true);
		select_list_scroll.setVisible(true);
		
		/*밑으로 옮기기*/
		select.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int i = jList.getSelectedIndex();
				select_vector.add(list_vector.get(i));
				list_vector.remove(i);
				select_List.setListData(select_vector);
				jList.setListData(list_vector);
			}
		});
	}
	
	public JTextArea retern_all_msg(){
		return all_msg;
	}public JLabel retern_access_mount(){
		return access_amout;
	}public void referesh(){
		penel.repaint();
		frame.repaint();
	}
	
	/*신고 리스트 추가*/
	public void vector_add(String s){
		list_vector.add(s);
		jList.setListData(list_vector);
		referesh();
	}
	
	/*신고 지우기*/
	public void del_singo(String ds){
		for(int i = 0; i < list_vector.size(); i++){
			if(list_vector.get(i).equals(ds)){
				list_vector.remove(i);
				break;
			}
		}
		for(int i = 0; i < select_vector.size(); i++){
			if(select_vector.get(i).equals(ds)){
				select_vector.remove(i);
				break;
			}
		}
		select_List.setListData(select_vector);
		jList.setListData(list_vector);
		referesh();
	}
}
