package io;

public interface Memory {
    /**
     * ��ȡָ����ַ��һ���ֽ�
     */
    public byte peek(int addr);
    /**
     * ��ȡָ����ַ��size���ֽ�
     */
    public byte[] peek(int addr, int size);
    /**
     * �޸��ڴ�
     */
    public void poke(int addr, byte val);
    /**
     * �޸��ڴ�
     */
    public void poke(int addr, byte[] val);
    /**
     * ��ȡ����ram
     */
    public byte[] getRAM();
    /**
     * ����ָ����ַ�Ļ�����
     * @return �����Ƿ�ɹ�
     */
    public boolean call(int addr);
}
