package core;

import java.util.*;
/**
 * ����
 * @param <T> ��������
 */
public class Array<T> {
    /**
     * ��ά��Ȩֵ
     */
    public int[] base;
    /**
     * ��ά������±�
     */
    public Integer[] bound;
    /**
     * չ���������Ԫ��
     */
    public T[] value;
    
    /**
     * ��ʾ����������±꣬Ȩֵ��Ԫ��
     */
    public String toString() {
        return "array" + Arrays.toString(bound) + ": (" + Arrays.toString(base)
                + "val=" + (value.length > 20 ? "..." : Arrays.toString(value)) + ")";
    }
}
