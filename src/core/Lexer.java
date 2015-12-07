package core;

import java.util.*;
import java.io.*;
import static common.Utilities.*;
import common.*;

/**
 * basic�ʷ�������
 */

public class Lexer {
    byte[] z, z_bak;
    int p, line, p_bak, line_bak, c_bak;
    
    Map<String, Integer> keyword;
    
    int tok;
    int ival;
    double rval;
    String sval;
    
    public Lexer(InputStream in) throws IOException {
            try {
                z = new byte[in.available()];
                in.read(z);
            } finally {
                in.close();
            }
        keyword = C.keywords;
        line = 1;
    }
    
    /**
     * ��ȡ��ǰ��ַ
     * @return ��ַ
     */
    public Addr getAddr() {
        //ע�⣬c�п��ܴ�����ǰ�����ַ�����ʱҪ��ƫ�Ƽ�1
        return new Addr(p - (c == ' ' ? 0 : 1), line);
    }
    
    /**
     * ��ת��ָ����ַ
     * @param a ��ַ
     */
    public void resumeAddr(Addr a) {
        p = a.addr;
        line = a.line;
        c = ' ';
    }
    
    /**
     * ���õ�ַ
     */
    public void reset() {
        p = 0;
        line = 1;
        c = ' ';
    }
    
    /**
     * ���ݵ�ǰ�ֽ����͵�ַ
     */
    public void backup() {
        z_bak = z;
        p_bak = p;
        line_bak = line;
        c_bak = c;
    }
    
    /**
     * �ָ����ݵ��ֽ����͵�ַ
     */
    public void restore() {
        if (z_bak != null) {
            z = z_bak;
            p = p_bak;
            line = line_bak;
            c = c_bak;
            z_bak = null;
        }
    }
    
    /**
     * �����µ��ֽ�������ַ��ʼ��Ϊ0�������Զ�����
     * @param newb ���ֽ���
     */
    public void setByteStream(byte[] newb) {
        z = newb;
        p = 0;
    }
    
    /**
     * ��ȡһ���ֽ�
     */
    public int getc() {
        try {
            return z[p++] & 0xff;
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }
    
    /**
     * ��ȡһ���ֽڣ��ᱣ�浽��ǰ��
     */
    public int peek() {
        return c = getc();
    }
    
    boolean getc(int ch) {
        peek();
        if (c != ch)
            return false;
        peek();
        return true;
    }
    
    int c = ' ';
    /**
     * ��ȡ��һ���ʷ���Ԫ
     * @return token��������INTEGER����ival��rval����ֵ
     */
    public int getToken() throws BasicException {
        while (isControl(c)) {
            if (c == 0xa) {
                line++;
                break;
            }
            peek();
        }
        switch (c) {
        case 0xa:
            peek();
            return tok = 10;
        case '<':
            if (getc('>'))
                return tok = C.NEQ;
            else if (c == '=') {
                peek();
                return tok = C.LTE;
            }
            return tok = '<';
        case '>':
            if (getc('='))
                return tok = C.GTE;
            return tok = '>';
        case '"':
            ByteStringBuffer bsb = new ByteStringBuffer(255);
            while (peek() != '"' && c != 0xd && c != 0xa && c != -1) {
                bsb.append(c);
            }
            if (c == '"')
                peek();
            sval = bsb.toString();
            return tok = C.STRING;
        default:
            try {
                if (c >= '0' && c <= '9' || c == '.') {
                    ByteStringBuffer bsb2 = new ByteStringBuffer();
                    while (c >= '0' && c <= '9') {
                        bsb2.append(c);
                        peek();
                    }
                    if (c != '.' && c != 'E' && c != 'e') {
                        rval = ival = Integer.parseInt(bsb2.toString());
                        return tok = C.INTEGER;
                    }
                    if (c == '.')
                        do {
                            bsb2.append(c);
                        } while (peek() >= '0' && c <= '9');
                    if (c == 'E' || c == 'e') {
                        bsb2.append(0x45);
                        peek();
                        if (c == '+' || c == '-') {
                            bsb2.append(c);
                            peek();
                        }
                        while (c >= '0' && c <= '9') {
                            bsb2.append(c);
                            peek();
                        }
                    }
                    rval = Double.parseDouble(bsb2.toString());
                    return tok = C.REAL;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new BasicException(E.SYNTAX);
            }
            if (isAlpha(c)) {
                ByteStringBuffer bsb3 = new ByteStringBuffer();
                do {
                    bsb3.append(toLowerCase(c));
                } while (isWord(peek()));
                if (c == '%' || c == '$') {
                    bsb3.append((byte) c);
                    peek();
                }
                sval = bsb3.toString();
                Integer i = keyword.get(sval.toString());
                if (i != null)
                    return tok = i;
                return tok = C.ID;
            }
            tok = c;
            peek();
            return tok;
        }
    }
    
    /**
     * ������һ�У��ᱣ��0xa��-1
     */
    public void skipToNewLine() {
        while (c != 10 && c != -1)
            peek();
    }
    
    public String toString() {
        switch (tok) {
        case C.ID:
            return line + ": <ID, " + sval + ">";
        case C.STRING:
            return line + ": <String, \"" + sval + "\">";
        case C.REAL:
            return line + ": <Real, " + rval + ">";
        case C.INTEGER:
            return line + ": <Integer, " + ival + ">";
        case C.GTE:
            return line + ": < >= >";
        case C.LTE:
            return line + ": < <= >";
        case C.NEQ:
            return line + ": < <> >";
        default:
            if (tok > 31 && tok < 127)
                return line + ": <" + ((char) tok) + ">";
            return line + ": <" + tok + ">";
        }
    }
    
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        InputStream i = new BufferedInputStream(new FileInputStream("bas/1.txt"));
        Lexer l = new Lexer(i);
        System.setOut(new PrintStream(new File("s.txt")));
        int k = 0;
        do {
            k = l.getToken();
            System.out.println(l.toString());
        } while (k != -1);
        
    }

}
