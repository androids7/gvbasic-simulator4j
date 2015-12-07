package io;

import java.io.*;
import common.*;

/**
 * wqx�ļ���������д������ʱ�����Զ�׷�ӷָ�������ȡʱ���Զ������ָ���
 * <br><b>ʾ��:</b>
 * <br><code>FileManager fm = new FileManager(3); //���ͬʱ�������ļ�
 * <br>System.out.println(fm.open("1.DAT", 0, FileManager.INPUT)); //��һ�������ļ�������ʾ�Ƿ�ɹ�
 * <br>System.out.println(fm.readReal(0)); //��ȡһ��ʵ��������ʾ
 * <br>fm.closeAll(); //�˳�����ǰȷ���ļ�ȫ���رգ���������޷������ļ�
 * </code>
 * @author Amlo
 *
 */

public class FileManager {
    public static final FileAttr INPUT = new FileAttr("i", true, false, false),
            OUTPUT = new FileAttr("w", false, true, false),
            APPEND = new FileAttr("a", false, true, true), //append�ǿɶ�λ�ģ������ڳ�����Ҫ���Ʋ��ܶ�λ
            RANDOM = new FileAttr("r", true, true, true);
    
    GFile[] files;
    String dir;
    
    /**
     * Ĭ�ϳ�ʼ����֧��ͬʱ��10���ļ�
     */
    public FileManager() {
        this(10);
    }
    
    /**
     * ��ʼ��һ���ļ�������
     * @param maxFile ֧��ͬʱ�򿪵�����ļ���
     */
    public FileManager(int maxFile) {
        files = new GFile[maxFile];
        dir = "";
    }
    
    /**
     * �л�����Ŀ¼
     * @return �Ƿ��л��ɹ�
     */
    public boolean chDir(String dir) {
        if (new File(dir).isDirectory()) {
            this.dir = dir;
            if (!this.dir.endsWith("/"))
                this.dir += "/";
            return true;
        }
        return false;
    }
    
    /**
     * ��һ���ļ�
     * @param filename �ļ���
     * @param fnum �ļ���(0 - ����ļ���)
     * @param fa �ļ�����
     * @return �ļ����Ƿ�ɹ�
     */
    public boolean open(String filename, int fnum, FileAttr fa) {
        try {
            GFile f = files[fnum];
            if (f != null && (f.canRead() || f.canPosition() || f.canWrite())) //�ļ��Ѵ�
                return false;
            files[fnum] = new GFile(dir + filename, fa.canRead, fa.canWrite, fa.canPosition);
            switch (fa.description) {
            case "a":
                files[fnum].position(files[fnum].length()); //���ļ�ָ�붨λ���ļ�ĩβ
                break;
            case "w":
                files[fnum].clear(); //����ļ�����
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * �ر��ļ�
     * @param fnum �ļ���
     * @return �Ƿ�رճɹ�
     */
    public boolean close(int fnum) {
        try {
            files[fnum].close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * �ر������Ѵ򿪵��ļ�
     * @return �Ƿ�رճɹ�
     */
    public boolean closeAll() {
        boolean r = true;
        for (int i = 0; i < files.length; i++) {
            if (files[i] != null) {
                try {
                    files[i].close();
                } catch (IOException e) {
                    r = false;
                }
            }
        }
        return r;
    }
    
    /**
     * �����ļ�ָ��λ�á������������򷵻�-1
     * @param fnum �ļ���
     * @return �ļ�ָ��
     */
    public int tell(int fnum) {
        try {
            return files[fnum].position();
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * ��λ�ļ�ָ��
     * @param pos �ļ�ָ��
     * @param fnum �ļ���
     * @return �Ƿ�λ�ɹ�
     */
    public boolean seek(int pos, int fnum) {
        try {
            files[fnum].position(pos);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * ��ȡһ���ַ�
     * @param fnum �ļ���
     * @return ��ȡ���ַ�������ȡʧ���򷵻�null
     */
    public Byte readByte(int fnum) {
        try {
            return files[fnum].read();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * ��ȡһ��byte
     * @param size ����С
     * @param fnum �ļ���
     * @return ��ȡ���ַ�������byte������ʽ����
     */
    public byte[] readBytes(int size, int fnum) {
        try {
            return files[fnum].read(size);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * ��ȡһ������
     * @param fnum �ļ���
     * @return ��ȡ��������������ȡʧ���򷵻�null
     */
    public Integer readInteger(int fnum) {
        try {
            return Integer.parseInt(getContent(fnum));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * ��ȡһ��ʵ��
     * @param fnum �ļ���
     * @return ��ȡ����ʵ��������ȡʧ���򷵻�null
     */
    public Double readReal(int fnum) {
        try {
            return Double.parseDouble(getContent(fnum));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * ��ȡһ��˫����������ַ���
     * @param fnum �ļ���
     * @return ��ȡ�����ļ�������ȡʧ���򷵻�null
     */
    public String readString(int fnum) {
        try {
            GFile f = files[fnum];
            if (f.read() != '"')
                return null;
            ByteStringBuffer b = new ByteStringBuffer();
            int c = 0;
            try {
                while ((c = f.read()) != '"')
                    b.append(c);
            } catch (IOException e) {
            }
            if (c != '"')
                return null;
            else
                f.read(); //�����ָ���
            return b.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    public S readS(int fnum) {
        try {
            GFile f = files[fnum];
            if (f.read() != '"')
                return null;
            ByteStringBuffer b = new ByteStringBuffer();
            int c = 0;
            try {
                while ((c = f.read()) != '"')
                    b.append(c);
            } catch (IOException e) {
            }
            if (c != '"')
                return null;
            else
                f.read(); //�����ָ���
            return b.toS();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * д��һ��ʵ��
     * @param a ʵ��
     * @param fnum �ļ���
     * @return �Ƿ�д��ɹ�
     */
    public boolean writeReal(double a, int fnum) {
        try {
            files[fnum].write(common.Utilities.realToString(a).getBytes());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * д��һ������
     * @param a ����
     * @param fnum �ļ���
     * @return д���Ƿ�ɹ�
     */
    public boolean writeInteger(short a, int fnum) {
        try {
            files[fnum].write(Short.toString(a).getBytes());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean writeInteger(int a, int fnum) {
        return writeInteger((short) a, fnum);
    }
    
    /**
     * д��һ���ַ�
     * @param a ascii�ַ�
     * @param fnum �ļ���
     * @return �Ƿ�д��ɹ�
     */
    public boolean writeByte(byte a, int fnum) {
        try {
            files[fnum].write(a);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean writeByte(int a, int fnum) {
        return writeByte((byte) a, fnum);
    }
    
    /**
     * д��һ��byte����
     * @param b ����
     * @param fnum �ļ���
     * @return д���Ƿ�ɹ�
     */
    public boolean writeBytes(byte[] b, int fnum) {
        try {
            files[fnum].write(b);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * д��һ������
     * @param fnum �ļ���
     * @return д���Ƿ�ɹ�
     */
    public boolean writeComma(int fnum) {
        return writeByte((byte) ',', fnum);
    }
    
    /**
     * д��һ��EOF (0xff)
     * @param fnum �ļ���
     * @return �Ƿ�д��ɹ�
     */
    public boolean writeEOF(int fnum) {
        return writeByte((byte) 0xff, fnum);
    }
    
    /**
     * д��һ���ַ������Զ���˫��������
     * @param s �ַ���
     * @param fnum �ļ���
     * @return �Ƿ�д��ɹ�
     */
    public boolean writeQuotedString(String s, int fnum) {
        try {
            files[fnum].write(("\"" + s + "\"").getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean writeQuotedS(S s, int fnum) {
        try {
            files[fnum].write((byte) '"');
            files[fnum].write(s.getBytes());
            files[fnum].write((byte) '"');
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * д��һ���ַ�����û��˫����
     * @param s �ַ���
     * @param fnum �ļ���
     * @return �Ƿ�д��ɹ�
     */
    public boolean writeString(String s, int fnum) {
        try {
            files[fnum].write(s.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean writeS(S s, int fnum) {
        try {
            files[fnum].write(s.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * ��ȡһ���Զ��Ż�0xff�ָ������ļ����ݣ��������ָ���
     * @param fnum �ļ���
     * @return ��ȡ�������ݣ����ַ�����ʾ
     * @throws Exception ��ȡ����
     */
    String getContent(int fnum) throws Exception {
        GFile f = files[fnum];
        ByteStringBuffer bsb = new ByteStringBuffer();
        int c;
        try {
            while ((c = f.read()) != ',' && (c & 0xff) != 0xff)
                bsb.append(c);
        } catch (IOException e) { //���ļ�ĩβ
        }
        return bsb.toString();
    }
    
    /**
     * ��ȡ�ļ�����
     * @param fnum �ļ���
     * @return �ļ����ȡ�����ȡʧ���򷵻�null
     */
    public Integer getLength(int fnum) {
        try {
            return files[fnum].length();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * �ļ�ָ���Ƿ񵽴��ļ�ĩβ
     */
    public boolean eof(int fnum) {
        try {
            return files[fnum].position() >= files[fnum].length();
        } catch (Exception e) {
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        FileManager fm = new FileManager(3);
        //System.out.println(fm.open("LZ.DAT",0,FileManager.INPUT));
        System.out.println(fm.open("1.txt",1,FileManager.RANDOM));
        //System.out.println(fm.getString(0));
        //System.out.println(fm.getString(0));
        System.out.println(fm.seek(12,1));
        System.out.println(fm.eof(1));
        //System.out.println(fm.getLength(1));
        //System.out.println(fm.readByte(1));
        //System.out.println(fm.readReal(1));
//        String s;
//        while ((s = fm.getString(0)) != null) {
//            fm.writeString(s, 1);
//            fm.writeByte(',', 1);
//        }
        fm.closeAll();
    }

}

class FileAttr {
    /**
     * ��ʼ���ļ�����
     * @param des ��������
     * @param a �ɶ�
     * @param b ��д
     * @param c �ɶ�λ
     */
    FileAttr(String des, boolean a, boolean b, boolean c) {
        description = des;
        canRead = a;
        canWrite = b;
        canPosition = c;
    }
    final boolean canRead, canWrite, canPosition;
    final String description;
}