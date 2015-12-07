package io;

import common.S;

public interface GraphicalScreen {
    /**
     * GraphicScreen.<i>OR</i> ��ɫ<br>GraphicScreen.<i>CLEAR</i> ��ɫ<br>GraphicScreen.<i>NOT</i> ��ɫ
     */
    public int OR = 1, CLEAR = 0, NOT = 2;
    /**
     * <i>FILL</i> ���<br><i>HOLLOW</i> �����
     */
    public int FILL = 1, HOLLOW = 0;
    
    /**
     * ��궨λ
     * @param y ������,0��ʼ
     * @param x ������0��ʼ
     */
    public void locate(int y, int x);
    /**
     * ��ȡ�������꣨0��ʼ��
     */
    public int getX();
    /**
     * ��ȡ��������꣨0��ʼ��
     */
    public int getY();
    /**
     * ��ʾ����
     * @param s ����
     * @param cr �Ƿ���
     */
    public void print(String s, boolean cr);
    public void print(S s, boolean cr);
    /**
     * ��������
     * <br>Ctrl�л���Ӣ�����뷨���������뷨��Google���뷨^_^
     * @return ������ַ���
     */
    public S input() throws InterruptedException;
    /**
     * ����
     */
    public void cls();
    /**
     * �л����ı�ģʽ(�����˸)
     */
    public void text();
    /**
     * �л���ͼ��ģʽ
     */
    public void graph();
    /**
     * ���ù����˸
     * @param f �Ƿ���˸
     */
    public void setFlash(boolean f);
    /**
     * ����
     * @param x ������
     * @param y ������
     * @param mode ��ͼģʽ
     */
    public void draw(int x, int y, int mode);
    /**
     * ����
     * @param x1 ��1��������
     * @param y1
     * @param x2 ��2��������
     * @param y2
     * @param mode ��ͼģʽ
     */
    public void line(int x1, int y1, int x2, int y2, int mode);
    /**
     * ������
     * @param x1 ��1��������
     * @param y1
     * @param x2 ��2��������
     * @param y2
     * @param fill �Ƿ����
     * @param mode ��ͼģʽ
     */
    public void box(int x1, int y1, int x2, int y2, int fill, int mode);
    
    /**
     * ��Բ
     * @param x Բ�ĺ�����
     * @param y Բ��������
     * @param r �뾶
     * @param fill �Ƿ����
     * @param mode ��ͼģʽ
     */
    public void circle(int x, int y, int r, int fill, int mode);
    /**
     * ����Բ
     * @param x Բ�ĺ�����
     * @param y Բ��������
     * @param rx X��뾶
     * @param ry Y��뾶
     * @param fill �Ƿ����
     * @param mode ��ͼģʽ
     */
    public void ellipse(int x, int y, int rx, int ry, int fill, int mode);
    /**
     * ��ȡ���ص�
     * @return ���е��򷵻ط���ֵ���򷵻�0�������곬����Ļ�򷵻�1
     */
    public int point(int x, int y);
    /**
     * ����λ����ʾ����
     * @param s �ַ���
     * @param x
     * @param y
     * @param font Ϊ������壻��Ϊ��С����
     * @param mode ��ͼģʽ��1=COPY,2=OR,3=AND,4=NOT,5=XOR
     */
    //public void textOut(String s, int x, int y, int font, int mode);
    public void textOut(S s, int x, int y, int font, int mode);
    /**
     * ��ͼ
     * @param addr ͼ�����ram��ַ
     * @param x ������
     * @param y ������
     * @param w ��ȣ����أ�
     * @param h �߶ȣ����أ�
     * @param mode ��ͼģʽ��ͬtextOut
     */
    public void paint(int addr, int x, int y, int w, int h, int mode);
}
