package core;

/**
 * ���ڴʷ������ĵ�ַ�࣬�����ֽ���ƫ�ƣ���ַ���͵�ǰ��
 * @author Amlo
 *
 */

public class Addr {
    public int addr, line;

    public Addr(int addr, int line) {
        this.addr = addr;
        this.line = line;
    }

    @Override
    public String toString() {
        return addr + " (" + line + ")";
    }
    
    public boolean equals(Object o) {
        return o instanceof Addr && ((Addr) o).addr == addr;
    }
}
