package io;

import common.*;

public class Text implements Constants {
    int x, y; //���λ��
    byte[] t; //���ֻ���
    Graph g;
    boolean text; //�Ƿ�����ģʽ
    boolean cursor; //����Ƿ���˸
    GFont font;
    
    public Text(Graph g) {
        this.g = g;
        font = GFont.FONT16;
        t = new byte[GFont.FONT12.COLUMN * GFont.FONT12.ROW];
        text = true;
        cursor = true;
    }
    
    /**
     * �Ƿ�����ģʽ������ģʽ��ˢ����Ļ��ͼ�����ʧ��
     * @return ���
     */
    public boolean isText() {
        return text;
    }

    /**
     * ��������ģʽ
     * @param text �Ƿ�����ģʽ
     */
    public void setTextMode(boolean text) {
        this.text = text;
    }

    /**
     * ����Ƿ���˸
     * @return ��˸
     */
    public boolean isCursor() {
        return cursor;
    }

    /**
     * ���ù����˸
     * @param cursor �Ƿ���˸
     */
    public void setCursor(boolean cursor) {
        this.cursor = cursor;
    }

    /**
     * ��ȡ�����꣨0��ʼ��
     * @return
     */
    public int getX() {
        return x;
    }
    
    /**
     * ��ȡ�����꣨0��ʼ��
     * @return
     */
    public int getY() {
        return y;
    }
    
    /**
     * ���ù������꣨0��ʼ��
     * @param x 
     */
    public void setX(int x) {
        if (x < 0 || x >= font.COLUMN)
            return;
        this.x = x;
    }
    
    /**
     * ���������꣨0��ʼ��
     * @param y
     */
    public void setY(int y) {
        if (y < 0 || y >= font.ROW)
            return;
        this.y = y;
    }
    
    @Deprecated
    public void locate(int y, int x) {
        if (x < 0 || y < 0 || x >= font.COLUMN || y >= font.ROW)
            return;
        this.y = y;
        this.x = x;
    }
    
    /**
     * ��ǰλ����ʾ���֣�����wqx����ʾ��
     * @param s ����
     */
    public void append(String s) {
        byte[] txt = s.getBytes();
        for (int i = x; i < font.COLUMN; i++) //�ѵ�ǰ��֮�������ȫ���
            t[i + y * font.COLUMN] = 0;
        for (int i = 0; i < txt.length; i++) {
            if ((txt[i] & 0xff) > 0xa0 && x == font.COLUMN - 1) {
                t[y * font.COLUMN + x] = 32;
                x = 0;
                y++;
            }
            if (y >= font.ROW)
                moveLine();
            t[y * font.COLUMN + x++] = txt[i];
            if (i < txt.length - 1 && (txt[i] & 0xff) > 0xa0)
                t[y * font.COLUMN + x++] = txt[++i];
            if (x >= font.COLUMN) {
                x = 0;
                y++;
            }
        }
    }
    public void append(S s) {
        byte[] txt = s.getBytes();
        for (int i = x; i < font.COLUMN; i++) //�ѵ�ǰ��֮�������ȫ���
            t[i + y * font.COLUMN] = 0;
        for (int i = 0; i < txt.length; i++) {
            if ((txt[i] & 0xff) > 0xa0 && x == font.COLUMN - 1) {
                t[y * font.COLUMN + x] = 32;
                x = 0;
                y++;
            }
            if (y >= font.ROW)
                moveLine();
            t[y * font.COLUMN + x++] = txt[i];
            if (i < txt.length - 1 && (txt[i] & 0xff) > 0xa0)
                t[y * font.COLUMN + x++] = txt[++i];
            if (x >= font.COLUMN) {
                x = 0;
                y++;
            }
        }
    }
    
    /**
     * ��������һ��, x������0
     */
    void moveLine() {
        for (int j = 0; j < font.ROW - 1; j++) {
            for (int k = 0; k < font.COLUMN; k++)
                t[k + j * font.COLUMN] = t[k + (j + 1) * font.COLUMN];
        }
        for (int j = 0; j < font.COLUMN; j++)
            t[(font.ROW - 1) * font.COLUMN + j] = 0;
        y--;
        
        //����Graph
        g.scroll(Graph.UP_MASK, 0, font.HEIGHT);
    }
    
    /**
     * ����Ƶ���һ��
     */
    public void nextLine() {
        y++;
        x = 0;
        if (y >= font.ROW)
            moveLine();
    }
    
    /**
     * ��������
     * @param isBig �Ƿ������
     */
    public void setFont(boolean isBig) {
        if (isBig)
            font = GFont.FONT16;
        else
            font = GFont.FONT12;
    }
    
    /**
     * ��ȡ����
     * @return ����
     */
    public GFont getFont() {
        return font;
    }
    
    /**
     * ��ȡ�Դ�
     * @return �Դ�
     */
    public Graph getGraph() {
        return g;
    }
    
    /**
     * ������ֻ��壬���ѹ����Ϊ(0, 0)�����������Ļ
     */
    public void clear() {
        java.util.Arrays.fill(t, (byte) 0);
        x = y = 0;
    }
    
    /**
     * �ѻ���������ˢ��Graph<br>
     */
    public void update() {
        if (text)
            g.clear();
        int p = 0;
        char c;
        for (int j = 0; j < font.ROW; j++) {
            for (int i = 0; i < font.COLUMN; i++) {
                c = (char) (t[p++] & 0xff);
                if (c > 0xa0)
                    c = (char) ((c << 8) | (t[p++] & 0xff));
                if (c != 0)
                    g.drawChar(c, i * font.ASCII_WIDTH, j * font.HEIGHT, font);
                if (c > 0xa0)
                    i++;
            }
        }
    }
    
    /**
     * ֱ���޸����ֻ���
     * @param pos ƫ��
     * @param value byteֵ
     */
    public void poke(int pos, byte value) {
        t[pos] = value;
    }
    
    /**
     * ��ȡ���ֻ����ֵ
     * @param pos λ��
     * @return byteֵ
     */
    public byte peek(int pos) {
        return t[pos];
    }
    
    /**
     * ֱ���������ֻ���ƫ��
     * @param pos ƫ��
     */
    public void setPosition(int pos) {
        y = pos / font.COLUMN;
        x = pos % font.COLUMN;
    }
    
    /**
     * ��ȡ���ֻ���ƫ��
     * @return ƫ��
     */
    public int getPosition() {
        return y * font.COLUMN + x;
    }
    
    /**
     * ��ȡ���ֻ���
     * @return ���ֻ��棨���ã��Ǹ��ƣ�
     */
    public byte[] getBuffer() {
        return t;
    }
    
    /**
     * ��ȡ�����С
     * @return �����С
     */
    public int getBufferSize() {
        return t.length;
    }
    
    public static final int IS_ASCII = 0, IS_FORMER_GBK = 1, IS_LATTER_GBK = 2;
    /**
     * ��ȡ��ǰ���״̬
     * @return Text.<i>IS_ASCII</i> ���λ��ascii�ַ�<br>Text.<i>IS_FORMER_GBK</i> ���λ��GBK�ַ���ǰ�벿��<br>Text.<i>IS_LATTER_GBK</i> ���λ��GBK�ַ��ĺ�벿��
     */
    public int getPositionState() {
        int i = y * font.COLUMN, j, s = 0;
        for (j = 0; j <= x; j++) {
            if ((t[i + j] & 0xff) >= 161) { //0xa0
                if (s == IS_FORMER_GBK)
                    s = IS_LATTER_GBK;
                else
                    s = IS_FORMER_GBK;
            } else {
                s = IS_ASCII;
            }
        }
        return s;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0, j = font.COLUMN * font.ROW; i < j; i++) {
            sb.append(Integer.toHexString(t[i] & 0xff));
            if (i < j - 1)
                sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
