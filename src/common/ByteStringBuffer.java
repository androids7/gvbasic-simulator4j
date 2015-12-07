package common;

import java.util.Arrays;

/**
 * ʹ��byte�����齨�ַ���
 */

public class ByteStringBuffer {
    byte[] b;
    int pos;
    
    static final int cap = 32;
    
    /**
     * �޲ι��캯����Ĭ��byte�����С32�ֽ�
     */
    public ByteStringBuffer() {
        this(cap);
    }
    
    /**
     * �Զ���Ĭ��byte�����С
     * @param capacity �����С
     */
    public ByteStringBuffer(int capacity) {
        b = new byte[capacity];
    }
    
    /**
     * ʹ��һ��byte�����ʼ��
     * @param str ���ڳ�ʼ����byte����
     */
    public ByteStringBuffer(byte[] str) {
        b = Arrays.copyOf(str, cap);
    }
    
    /**
     * ʹ��һ���ַ�����ʼ��
     * @param str ���ڳ�ʼ�����ַ���
     */
    public ByteStringBuffer(String str) {
        this(str.getBytes());
    }
    
    /**
     * �������
     */
    public ByteStringBuffer clear() {
        pos = 0;
        b = new byte[cap];
        return this;
    }
    
    /**
     * ��һ�ֽ�׷�ӵ�����β��
     * @param by �ֽ�
     * @return this
     */
    public ByteStringBuffer append(byte by) {
        checkCapacity(1);
        b[pos++] = by;
        return this;
    }
    
    /**
     * ��1�ֽ�׷�ӵ�����β��
     * @param c ��ת��Ϊbyte
     * @return this
     */
    public ByteStringBuffer append(int c) {
        return append((byte) c);
    }
    
    /**
     * ���ֽ�����׷�ӵ�����β��
     * @param by �ֽ�����
     * @return this
     */
    public ByteStringBuffer append(byte[] by) {
        checkCapacity(by.length);
        for (int i = 0; i < by.length; i++)
            b[pos++] = by[i];
        return this;
    }
    
    /**
     * ���ַ���׷�ӵ�����β��
     * @param s �ַ���
     * @return this
     */
    public ByteStringBuffer append(String s) {
        return append(s.getBytes());
    }
    
    /**
     * ��һ��object���ַ�����ʽ׷�ӵ�����β��
     * @param o object
     * @return this
     */
    public ByteStringBuffer append(Object o) {
        return append(String.valueOf(o));
    }
    
    void checkCapacity(int l) {
        while (pos + l > b.length)
            b = Arrays.copyOf(b, b.length << 1);
    }
    
    /**
     * ��ȡ��ǰ�����С
     * @return �����С
     */
    public int capacity() {
        return b.length;
    }
    
    /**
     * �õ��ַ���
     */
    public String toString() {
        return new String(b, 0, pos);
    }
    
    public S toS() {
        return new S(b, 0, pos);
    }
    
    /**
     * ��ȡָ���±��byte
     * @param index �±�
     * @return byte
     */
    public byte byteAt(int index) {
        return b[index];
    }
}
