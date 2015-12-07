package common;

import java.io.*;
import java.util.*;

/**
 * ʵ�ù���
 */

public abstract class Utilities implements Constants {
    static Map<Integer, Integer> pc2wqx = new HashMap<>(),
            wqx2pc = new HashMap<>(),
            mapping = new HashMap<>();
    static int gbuf = 0, tbuf = 0, delay = 0;
    static {
        System.loadLibrary("utilities");
        
        IniEditor ini = new IniEditor();
        try {
            ini.load("res/config.ini");
            gbuf = Integer.parseInt(ini.get("Ram", "graphbuffer"), 16);
            tbuf = Integer.parseInt(ini.get("Ram", "textbuffer"), 16);
            delay = Integer.parseInt(ini.get("Interpreter", "delay"));
            List<String> l = ini.optionNames("KeyValue");
            for (String s : l) {
                Integer i1 = Integer.parseInt(s),
                        i2 = Integer.parseInt(ini.get("KeyValue", s));
                wqx2pc.put(i1, i2);
                pc2wqx.put(i2, i1);
            }
            l = ini.optionNames("Mapping");
            for (String s : l) {
                Integer i1 = Integer.parseInt(s),
                        i2 = Integer.parseInt(ini.get("Mapping", s), 16);
                mapping.put(i1, i2);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("load ini file failed.");
        }
    }
    
    /**
     * ��ȡ  <i>Shift + ĳ����</i> ����õ��ַ�
     * @param key PC��ֵ
     * @return ��õ��ַ�
     */
    public static int getUpperChar(int key) {
        if (key >= 'A' && key <= 'Z')
            return key & 0xdf;
        switch (key) {
        case '`':
            return '~';
        case '1':
            return '!';
        case '2':
            return '@';
        case '3':
            return '#';
        case '4':
            return '$';
        case '5':
            return '%';
        case '6':
            return '^';
        case '7':
            return '&';
        case '8':
            return '*';
        case '9':
            return '(';
        case '0':
            return ')';
        case '-':
            return '_';
        case '=':
            return '+';
        case '\\':
            return '|';
        case '[':
            return '{';
        case ']':
            return '}';
        case ';':
            return ':';
        case '\'':
            return '"';
        case ',':
            return '<';
        case '.':
            return '>';
        case '/':
            return '?';
        default:
            return key;
        }
    }
    
    /**
     * �ж������������Ƿ���ȣ�����1e-9
     * @param a
     * @param b
     * @return �жϽ��
     */
    public static boolean doubleEqual(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }
    
    /**
     * �жϸ������Ƿ�Ϊ0
     * @param a ������
     * @return �жϽ��
     */
    public static boolean doubleIsZero(double a) {
        return Math.abs(a) < 1e-9;
    }
    
    /**
     * ��ʵ��ת��Ϊ�ַ���������Ϊ1E-9
     */
    public static native String realToString(double a);
    
    /**
     * ��1�ֽ�ת��Ϊ�ַ�����ȱ�ݣ���b < 0 (-128 ~ -1), ����Ϊ�ʺ�
     */
    public static native String byteToString(byte b);
    
    /**
     * �ַ���ת��Ϊdouble��c����str2dʵ��
     */
    public static native double str2d(String s);
    
    /**
     * �ж��ַ��Ƿ��ǿɴ�ӡ�Ŀ����ַ���<i>F1 - F12, Up, Down, Left, Right, PageUp, PageDown, End, Home</i>��
     * @param c �ַ�
     * @return �жϽ��
     */
    public static boolean isPrintableControl(int c) {
        return c >= 112 && c <= 123 || c >= 33 && c <= 40;
    }
    
    /**
     * �ж��ַ��Ƿ��ǿհ׷������������з�0xa��
     * @param c �ַ�
     * @return �жϽ��
     */
    public static boolean isWhiteSpace(int c) {
        return c == ' ' || c == '\t' || c == '\f' || c == 0xd;
    }
    
    /**
     * �ж��ַ��Ƿ��ǿ����ַ�
     * @param c �ַ�
     * @return �жϽ��
     */
    public static boolean isControl(int c) {
        return c >= 0 && c < 0x21 || c == 0x7f;
    }
    
    /**
     * �ж��ַ��Ƿ�����ĸ
     * @param c �ַ�
     * @return �жϽ��
     */
    public static boolean isAlpha(int c) {
        return (c | 0x20) >= 'a' && (c | 0x20) <= 'z';
    }
    
    /**
     * �Ѵ�д��ĸת����Сд��ĸ
     * @param c �ַ�
     * @return ת��������Ǵ�д��ĸ�򷵻�ԭ�ַ�
     */
    public static int toLowerCase(int c) {
        return c >= 'A' && c <= 'Z' ? c | 0x20 : c;
    }
    
    /**
     * �ж��ַ��Ƿ���gvb��ʶ��
     * @param c �ַ�
     * @return �жϽ��
     */
    public static boolean isWord(int c) {
        return (c | 0x20) >= 'a' && (c | 0x20) <= 'z' || c >= '0' && c <= '9';
    }
    
    /**
     * pc��λתwqx��λ,pc��λ�Ĵ�д��ĸ�Զ�תСд
     * @param rawKey pc��λ
     * @return wqx��λ
     */
    public static int convertKeyCode(int rawKey) {
        Integer i = pc2wqx.get(rawKey);
        if (rawKey >= 'A' && rawKey <= 'Z')
            return rawKey | 0x20;
        return i == null ? rawKey : i;
    }
    
    /**
     * wqx��λתpc��λ
     * @param wqxKey 
     * @return pc��λ
     */
    public static int recoverKeyCode(int wqxKey) {
        Integer i = wqx2pc.get(wqxKey);
        if (i == null && wqxKey >= 'a' && wqxKey <= 'z')
            return wqxKey & 0xdf;
        return i == null ? wqxKey : i;
    }
    
    /**
     * ��ȡ����ӳ��ֵ����ӳ��ֵ�������򷵻�0
     * @param wqxKey wqx��ֵ
     * @return ӳ��ֵ
     * <br><i>bit0 - bit7</i> : ��ֵ��ʾ��λ
     * <br><i>bit8 - bit10</i> : ��ֵӳ����ڴ�ƫ��
     */
    public static int mapWQXKey(int wqxKey) {
        Integer i = mapping.get(wqxKey);
        return i == null ? 0 : i;
    }
    
    /**
     * ��ȡ��ʾ����
     * @return ��ʾ����
     */
    public static int getGBUF() {
        return gbuf;
    }
    
    /**
     * ��ȡ���ֻ���
     * @return ���ֻ���
     */
    public static int getTBUF() {
        return tbuf;
    }
    
    public static int getDelay() {
        return delay;
    }
    
    /**
     * byte����ģʽƥ��
     * @param s Ҫƥ�������
     * @param a ģʽ����
     * @return �ҵ���λ�ã��Ҳ�������-1
     */
    public static int byteArrayMatch(byte[] s, byte[] a) {
        L1:
        for (int i = 0, l = s.length - a.length; i < l; i++) {
            if (s[i] == a[0]) {
                for (int j = 1; j < a.length; j++) {
                    if (s[i + j] != a[j])
                        continue L1;
                }
                return i;
            }
        }
        return -1;
    }
    
    /**
     * �����ص�������ת��Ϊbyte��������
     * @param graph ���ص���
     * @param w ���ؿ��
     * @param h ���ظ߶�
     * @return byte����
     */
    public static byte[] toByteData(int[] graph, int w, int h) {
        int bw = (w + 7) >>> 3, bw_ = w >>> 3;
        byte[] b = new byte[bw * h];
        
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < bw_; i++) {
                byte bb = 0;
                for (int k = 0; k < 8; k++) {
                    if (graph[j * w + (i << 3) + k] != 0)
                        bb |= pmask[k];
                }
                b[j * bw + i] = bb;
            }
            if (bw > bw_) {
                byte bb = 0;
                for (int i = 0, k = w - (bw_ << 3); i < k; i++) {
                    if (graph[j * w + (bw_ << 3) + i] != 0)
                        bb |= pmask[i];
                }
                b[j * bw + bw_] = bb;
            }
        }
        
        return b;
    }
    
    /**
     * ��8byte����ת��Ϊ���ص���
     * @param b 8byte����
     * @param w ��ȣ����أ�
     * @param h �߶ȣ����أ�
     * @return ���ص���
     */
    public static int[] toPointData(byte[] b, int w, int h) {
        int[] p = new int[w * h];
        int t1 = 0, t2 = 0;
        
        for (int o = 0; o < h; o++) {
            for (int i = 0, j = w >>> 3; i < j; i++) {
                for (int k = 0, l = b[t2]; k < 8; k++)
                    p[t1] = (l & pmask[k]) == 0 ? 0 : 1;
                t1++;
                t2++;
            }
            for (int i = 0, j = w & 7, k = b[t2]; i < j; i++) {
                p[t1] = (k & pmask[i]) == 0 ? 0 : 1;
                t1++;
            }
            if ((w & 7) != 0)
                t2++;
        }
        return p;
    }
    
    public static void main(String[] args) throws IOException {
//        int[] a={1,1,1,1,1,1,1,1,1,1,1,
//                0,0,0,0,0,0,0,0,1,0,0,
//                0,0,0,0,1,1,1,1,1,1,0,
//                1,1,1,1,0,0,0,0,0,0,1};
        //byte[] b=toByteData(a,11,4);
        //System.out.println(Arrays.toString(toPointData(new byte[]{1,2,4,8,16,32,64,-1},4,8)));
        System.out.println(new String(new byte[] {(byte)141,103}));
        
//        for (int i = 0;i < b.length;i++)
//            System.out.print(Integer.toHexString(b[i]&0xff)+",");
        
//        OutputStream o =new BufferedOutputStream(new FileOutputStream("1.bmp"));
//        save1ColorBMP(b,11,4,o);
//        o.close();
    }
}
