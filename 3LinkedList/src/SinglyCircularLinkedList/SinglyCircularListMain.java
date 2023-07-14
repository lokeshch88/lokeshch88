package SinglyCircularLinkedList;

import java.util.Scanner;
class SinglyCircularList{
	static class Node{
		private int data;
		private Node next;
		public Node() {
			data=0;
			next=null;
		}
		public Node(int data) {
			this.data = data;
			this.next = null;
		}
		
	}
	private Node head;
	public SinglyCircularList() {
		head=null;
	}
	
	public void display() {
		if(head==null)
			return;
		Node temp=head;
		do {
			System.out.println(temp.data);
			temp=temp.next;
		}while(temp!=head);
	}

	public void deleteLast() {
		// TODO Auto-generated method stub
		
	}
	void addLast(int val) {
		Node newNode=new Node(val);
		//if list is empty...
		//newnode is first node and make it circular
		if(head==null) {
			head=newNode;
			newNode.next=head;
		}
		//add node to the end
		 //traverse till last node (trav) i.e node whose next contains head
		Node trav=head;
		while(trav.next!=head)
			trav=trav.next;
		//newnodes next to head
		newNode.next=head;
		//last nodes(trav) next to newnode
		trav.next=newNode;
	}

	public void addFirst(int val) {
		Node newNode=new Node(val);
		//if list is empty...
		//newnode is first node and make it circular
		if(head==null) {
			head=newNode;
			newNode.next=head;
		}
		//add node to the end
		 //traverse till last node (trav) i.e node whose next contains head
		Node trav=head;
		while(trav.next!=head)
			trav=trav.next;
		//newnodes next to head
		newNode.next=head;
		//last nodes(trav) next to newnode
		trav.next=newNode;
		head=newNode;
		
	}
	
}

public class SinglyCircularListMain {
	public static void main(String[] args) {
		Scanner sc=new Scanner(System.in);
		SinglyCircularList sl=new SinglyCircularList();
		boolean exit= true;
		do {
		System.out.println("1.display  2.add at last 3.add first 4.delete first  5.delete all  6.delete at pos  7.delete last 0.exit  ");
		System.out.println("enter choice");
		switch(sc.nextInt()) {
		case 1:
			sl.display();
			break;
		case 2:
			System.out.println("enter data");
			sl.addLast(sc.nextInt());
			break;
		case 3:
			System.out.println("enter data");
			sl.addFirst(sc.nextInt());
//			System.out.println("enter pos");
//			int p=sc.nextInt();
//			System.out.println("enter data");
//			int data=sc.nextInt();
//			sl.addAtPos(data, p);
			break;
		case 4:
			//sl.deleteFirst();
			break;
		case 5:
			//sl.deleteAll();
			break;
		case 6:
			System.out.println("enter pos at to delete");
			int pos=sc.nextInt();
			//sl.deleteAtPos(pos);
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
