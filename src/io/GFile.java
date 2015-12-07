package io;

import java.io.*;
import java.util.*;

/**
 * ģ��wqx�ļ��Ļ����࣬���ж�ȡд���ֽڵĹ���
 */

public class GFile {
    private static final int BUFFER_SIZE = 1024;
    
    private byte[] buf;
    private int pos, capacity;
    
    private boolean canRead, canWrite, canPosition;
    private final File file;
    
    public GFile(String filename, boolean readable, boolean writable, boolean positionable)
            throws FileNotFoundException, IOException {
        this(new File(filename), readable, writable, positionable);
    }
    
    public GFile(File file, boolean readable, boolean writable, boolean positionable)
            throws FileNotFoundException, IOException {
        this.file = file;
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            buf = new byte[capacity = in.available()];
            in.read(buf);
            in.close();
        } catch (FileNotFoundException e) {
            if (writable) { //�½��ļ�
                new FileOutputStream(file).close();
                buf = new byte[BUFFER_SIZE];
            }
        } catch (IOException e) {
            if (readable)
                throw e;
            else
                buf = new byte[BUFFER_SIZE];
        }
        
        canRead = readable;
        canWrite = writable;
        if (readable && !file.canWrite())
            throw new IOException("can't write file");
        canPosition = positionable;
    }
    
    /**
     * ��ȡһ�ֽ�
     * @return ��ȡ���ֽ�
     * @throws IOException �ļ����ɶ���û�����ݿɹ���ȡ
     */
    public byte read() throws IOException {
        if (!canRead)
            throw new IOException("can't read");
        try {
            return buf[pos++];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("out of file");
        }
    }
    
    /**
     * ��ȡһ��byte����������ʽ����
     * @param size Ҫ��ȡ���ֽ���
     * @return byte����
     * @throws IOException �ļ����ɶ���û���㹻�����ݹ���ȡ
     */
    public byte[] read(int size) throws IOException {
        if (!canRead)
            throw new IOException("can't read");
        try {
            if (pos + size > buf.length)
                throw new IOException("out of file");
            return Arrays.copyOfRange(buf, pos, pos += size);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("out of file");
        }
    }
    
    /**
     * д��һ�ֽڡ�ʵ����д������ڴ�
     * @param b Ҫд����ֽ�
     * @throws IOException �ļ�����д
     */
    public void write(byte b) throws IOException {
        if (!canWrite)
            throw new IOException("can't write");
        ensureCapacity(1);
        buf[pos++] = b;
    }
    
    /**
     * д��һ���ֽ�
     * @param b һ���ֽ�
     * @throws IOException �ļ����ɶ�
     */
    public void write(byte[] b) throws IOException {
        if (!canWrite)
            throw new IOException("can't write");
        ensureCapacity(b.length);
        for (byte c : b)
            buf[pos++] = c;
    }
    
    /**
     * ��ȡ�ļ�ָ��λ��
     * @return �ļ�ָ��
     */
    public int position() {
        return pos;
    }
    
    /**
     * ��λ�ļ�ָ��
     * @param p �ļ�ָ��
     * @throws IOException ָ�볬���ļ���С
     */
    public void position(int p) throws IOException {
        if (!canPosition || p > capacity)
            throw new IOException("can't position file pointer");
        pos = p;
    }
    
    /**
     * �����ļ���С
     * @return �ļ���С
     */
    public int length() {
        return capacity;
    }
    
    /**
     * �ر��ļ�������ļ���д�룬������ļ�����
     * @throws IOException �޷�д���ļ�
     */
    public void close() throws IOException {
        if (canWrite) {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(buf, 0, capacity);
            out.close();
        }
        canRead = canWrite = canPosition = false;
    }
    
    /**
     * ����ļ�����
     */
    public void clear() {
        capacity = pos = 0;
    }
    
    /**
     * �����ļ�ָ��
     */
    public void rewind() {
        pos = 0;
    }
    
    /**
     * �ļ��Ƿ�ɶ�
     */
    public boolean canRead() {
        return canRead;
    }
    
    /**
     * �ļ��Ƿ��д
     */
    public boolean canWrite() {
        return canWrite;
    }
    
    /**
     * �ļ��Ƿ�ɶ�λ
     */
    public boolean canPosition() {
        return canPosition;
    }

    /**
     * ȷ����д��Ļ������㹻��
     * @param i Ҫд������ֽ�
     */
    private void ensureCapacity(int i) {
        while (buf.length < pos + i) {
            buf = Arrays.copyOf(buf, buf.length + 1024);
        }
        if (pos + i > capacity)
            capacity = pos + i;
    }

    public static void main(String[] args) {
        byte[] a=new byte[] {0,1,2};
        System.out.println(Arrays.toString(Arrays.copyOfRange(a,0,1)));
    }

}
