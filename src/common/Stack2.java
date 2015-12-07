package common;

/**
 * ���ܼ򵥵�ջ
 * @author Amlo
 *
 * @param <E> ջԪ������
 */

public class Stack2<E> {
    class Node {
        E e;
        Node next;
        Node(E element, Node next) {
            e = element;
            this.next = next;
        }
    }
    
    Node top;
    
    public void push(E e) {
        top = new Node(e, top);
    }
    
    /**
     * ����Ԫ�أ���ջ�գ��򷵻�null
     * @return ջ��Ԫ��
     */
    public E pop() {
        try {
            E e = top.e;
            top = top.next;
            return e;
        } catch (NullPointerException e) {
            return null;
        }
    }
    
    public boolean empty() {
        return top == null;
    }
    
    public void clear() {
        top = null;
    }
    
    /**
     * �鿴ջ��Ԫ�أ���ջ�գ��򷵻�null
     * @return ջ��Ԫ��
     */
    public E peek() {
        try {
            return top.e;
        } catch (NullPointerException e) {
            return null;
        }
    }
    
    public String toString() {
        Node n = top;
        StringBuffer sb = new StringBuffer("[");
        while (n != null) {
            sb.append(n.e);
            if (n.next != null)
                sb.append(" -> ");
            n = n.next;
        }
        return sb.append("]").toString();
    }
    
    Node xtop;
    public void xreset() {
        xtop = top;
    }
    public E xpop() {
        try {
            E e = xtop.e;
            xtop = xtop.next;
            return e;
        } catch (NullPointerException e) {
            return null;
        }
    }
    public E xpeek() {
        try {
            return xtop.e;
        } catch (NullPointerException e) {
            return null;
        }
    }
}
