package com.zyh.algo.simple;

import java.util.Stack;

public class ILinkedList {

	private ILinkedList next;
	private Object data;

	public ILinkedList getNext() {
		return next;
	}

	public void setNext(ILinkedList next) {
		this.next = next;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(String.valueOf(getData()));
		ILinkedList n = getNext();
		while (n != null) {
			sb.append("->").append(n.getData());
			n = n.getNext();
		}
		return sb.toString();
	}

	public static ILinkedList reverse(ILinkedList head) {
		if (null == head || head.getNext() == null) {
			return head;
		}
		ILinkedList node1 = head;
		ILinkedList node2 = node1.getNext();
		ILinkedList node3 = node2.getNext();
		head.setNext(null);
		node2.setNext(node1);
		ILinkedList newHead = node2;
		while (node3 != null) {
			node1 = node3.getNext();
			node3.setNext(node2);
			newHead = node3;
			node2 = node3;
			node3 = node1;
		}
		return newHead;
	}

	public static ILinkedList reverseStack(ILinkedList head) {
		if (null == head || head.getNext() == null) {
			return head;
		}
		Stack<ILinkedList> stack = new Stack<>();
		ILinkedList node = head;
		while (node != null) {
			stack.push(node);
			node = node.getNext();
		}
		ILinkedList newHead = stack.pop();
		node = newHead;
		while (!stack.isEmpty()) {
			ILinkedList nodet = stack.pop();
			node.setNext(nodet);
			node = nodet;
		}
		head.next = null;
		return newHead;
	}

	public static void main(String... args) {
		System.out.println(newList(1));
		System.out.println(reverse(newList(1)));
		System.out.println(reverseStack(newList(1)));
		System.out.println(newList(2));
		System.out.println(reverse(newList(2)));
		System.out.println(reverseStack(newList(2)));
		System.out.println(newList(3));
		System.out.println(reverse(newList(3)));
		System.out.println(reverseStack(newList(3)));
		System.out.println(newList(4));
		System.out.println(reverse(newList(4)));
		System.out.println(reverseStack(newList(4)));
		System.out.println(newList(5));
		System.out.println(reverse(newList(5)));
		System.out.println(reverseStack(newList(5)));
		System.out.println(newList(6));
		System.out.println(reverse(newList(6)));
		System.out.println(reverseStack(newList(6)));
		System.out.println(newList(7));
		System.out.println(reverse(newList(7)));
		System.out.println(reverseStack(newList(7)));
		System.out.println(newList(8));
		System.out.println(reverse(newList(8)));
		System.out.println(reverseStack(newList(8)));
		System.out.println(newList(9));
		System.out.println(reverse(newList(9)));
		System.out.println(reverseStack(newList(9)));
		System.out.println(newList(10));
		System.out.println(reverse(newList(10)));
		System.out.println(reverseStack(newList(10)));
	}

	private static ILinkedList newList(int count) {
		ILinkedList head = new ILinkedList();
		head.setData(0);
		ILinkedList node = head;
		for (int i = 1; i < count; i++) {
			ILinkedList nodeNew = new ILinkedList();
			nodeNew.setData(i);
			node.setNext(nodeNew);
			node = nodeNew;
		}
		return head;
	}
}
