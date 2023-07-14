import java.util.Scanner;

class SinglyList{
	static class Node{
		//node class fields
		private int data;
		private Node next;
		public Node() {
			data=0;
			next=null;
		}
		public Node(int val) {
			data=val;
			next=null;
		}
	}
	//list class fields
	private Node head;
	public SinglyList() {
		this.head=null;
	}
	void display() {
		Node temp;
		temp=head;
		while(temp!=null) {
			System.out.print(temp.data+" ");
			temp=temp.next;
		}
		System.out.println();
	}
	void addAtFirst(int val) {
		Node newNode=new Node(val);
		if(head==null)
			head=newNode;
		else {
			newNode.next=head;
			head= newNode;
		}	
	}
    void addAtLast(int val) {
		
	}
    void addAtPos(int val,int pos) {
    	Node newNode=new Node(val);
    	Node temp=head;
    	for(int i=1;i<pos-1;i++) {
    		temp=temp.next;
    	}
    	newNode.next=temp.next ;
    	temp.next=newNode;
    	
    }
    void deleteFirst() {
    	if(head==null)
    		System.out.println("list is empty");
    	else {
    		//make head pointing to next node
    		head=head.next;
    		//old first node will be garbage collected
    	}
    }
    void deleteAll() {
    	head=null; //all nodes will be garbage collected
    }  
	public void deleteAtPos(int pos) { //using two traversal pointers
		//if pos=1
		if(pos==1)
			deleteFirst();
		//if list is empty or pos<1
		if(head==null || pos<1)
			System.out.println("list is empty or invalid position");
		//take trav pointer running behind temp
		Node temp=head,trav=null;
		//traverse till pos (temp)
		for(int i=1;i<pos;i++) {
			//if pos is beyond list length
			if(temp==null)
				System.out.println("invalid position");
			trav=temp;
			temp=temp.next;	 
		}    
		//temp is the node to be deleted and trav is node before that
		trav.next=temp.next;   //temp node will be garbage collected	
	}
	public void deleteLast() {
		//sp case1: if list is empty throw exception
		if(head==null)
			throw new RuntimeException("list is empty");
		//sp case2: if list has single node, make head null
		if(head.next==null)
			head=null;
		else {
		//either delete last node
		 Node trav=null, temp=head;
		 // traverse till last node (temp) and run trav behind it
		 while(temp.next!=null) {
			 trav=temp;
			 temp=temp.next;
		 }
		 //when last node temp deleted, second last node travs next should be null
		 trav.next=null;
	}	
	}
}

public class SinglyListMain {

	public static void main(String[] args) {
		Scanner sc=new Scanner(System.in);
		SinglyList sl=new SinglyList();
		boolean exit= true;
		do {
		System.out.println("1.display  2.add at first 3.add at pos 4.delete first  5.delete all  6.delete at pos  7.delete last 0.exit  ");
		System.out.println("enter choice");
		switch(sc.nextInt()) {
		case 1:
			sl.display();
			break;
		case 2:
			System.out.println("enter data");
			sl.addAtFirst(sc.nextInt());
			break;
		case 3:
			System.out.println("enter pos");
			int p=sc.nextInt();
			System.out.println("enter data");
			int data=sc.nextInt();
			sl.addAtPos(data, p);
			break;
		case 4:
			sl.deleteFirst();
			break;
		case 5:
			sl.deleteAll();
			break;
		case 6:
			System.out.println("enter pos at to delete");
			int pos=sc.nextInt();
			sl.deleteAtPos(pos);
			break;
		case 7:
			try {
				sl.deleteLast();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
			break;
		case 0:
			exit=false;
			break;
		}
		}while(exit);
		
	}

}
