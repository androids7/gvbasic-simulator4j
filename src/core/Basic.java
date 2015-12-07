package core;

import gui.*;
import io.*;
import common.*;
import static common.Utilities.*;

import java.io.*;
import java.util.*;

/**
 * gvb���������Ĵ���
 */

public class Basic {
    GraphicalScreen scr;
    Form frm;
    Lexer l;
    
    Stack2<For> fors = new Stack2<>();
    Stack2<While> whiles = new Stack2<>();
    Map<String, Fn> funs = new HashMap<>();
    
    /**
     * ��Ϊpublic����frm���ӱ�������Ҫsynchronized
     */
    public Map<Id, Object> vars = new HashMap<>();
    
    /**
     * �кš���ַ��һ�п�ͷ�������кţ�������Ҫ��¼�к�
     */
    Map<Integer, Addr> stmts = new HashMap<>();
    
    /**
     * gosub���ӳ���ջ
     */
    Stack2<Pack> subs = new Stack2<>();
    
    /**
     * �����ӳ�
     */
    int delay;
    
    /**
     * ���ú������Ƽ���
     */
    Set<String> infuns;
    
    DataReader dr = new DataReader();
    
    Memory ram;

    FileState[] files = new FileState[3];
    FileManager fm = new FileManager(3);
    
    public Basic(GraphicalScreen screen, Memory m, Form f) {
        scr = screen;
        frm = f;
        delay = getDelay();
        infuns = C.funs;
        ram = m;
        for (int i = 0; i < files.length; i++)
            files[i] = new FileState();
        fm.chDir("dat");
    }
    
    /**
     * ���ý�������������������գ�����Ӱ��lexer
     */
    public void reset() {
        dr.clear();
        fors.clear();
        whiles.clear();
        funs.clear();
        vars.clear();
        stmts.clear();
        subs.clear();
        fm.closeAll();
        fns.clear();
        fnvar.clear();
        for (FileState fs : files)
            fs.close();
        pb = ram.getRAM();
        
        pb[191] = pb[192] = pb[193] = pb[194] = pb[195] =
                pb[196] = pb[197] = pb[198] = -1;
        pb[199] = 13;
    }
    
    /**
     * ����basicԴ����
     * @param in ������
     * @return �Ƿ�����ɹ�
     */
    public boolean load(InputStream in) {
        try {
            //reset();
            l = new Lexer(in);

            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * ִ�г���
     * @return ���򷵻ص���Ϣ����Ϊnull�����������������쳣����
     */
    public String run() {
        if (l != null) {
            try {
                reset();
                scanStmt();
                program();
            } catch (BasicException e) {
                e.printStackTrace(); //debug
                return e + " in " + stmt;
            } finally {
                System.out.println("current label:" + stmt);
                System.out.println(vars); //debug
                //System.out.println(fors);
                fm.closeAll();
            }
        } else
            return "Program hasn't been loaded!";
        return null;
    }
    
    /**
     * ��ͣ
     */
    public void pause() {
        paused = true;
    }
    
    /**
     * ����ִ��
     */
    public void cont() {
        paused = false;
    }
    
    Boolean paused;
    
    /**
     * Ԥ����ɨ���кţ���¼��ַ
     */
    void scanStmt() throws BasicException {
        l.reset();
        Addr a = l.getAddr();
        peek();
        stmt = 0;
        
        while (tok != -1) {
            if (tok != C.INTEGER)
                error(E.SYNTAX);
            if (l.ival < stmt)
                error(E.STMT_ORDER);
            stmt = l.ival;
            if (stmts.containsKey(l.ival))
                error(E.STMT_DUPLICATE);
            if (stmt < 0)
                error(E.ILLEGAL_QUANTITY);
            stmts.put(l.ival, a);
            while (tok != 0xa && tok != -1) {
                if (tok == C.REM) {
                    l.skipToNewLine();
                    break;
                } else if (tok == C.DATA) { //��ȡ����
                    int c = l.peek();
                    boolean quote = false;
                    dr.mark(stmt);
                    while (c != 0xd && c != ':' && c != 0xa && c != -1) {
                        if (c == '"')
                            quote = !quote;
                        dr.append(c);
                        c = l.peek();
                        if (c == ':' && quote) {
                            dr.append(':');
                            c = l.peek();
                        }
                    }
                    if (quote)
                        dr.append('"');
                    dr.addComma();
                    if (c != ':')
                        break;
                    peek();
                } else
                    peek();
            }
            if (tok != 0xa)
                peek(); //�������з�
            a = l.getAddr();
            peek();
        }
    }
    
    int tok;
    /**
     * ��ǰ��
     * @throws BasicException
     */
    void peek() throws BasicException {
        tok = l.getToken();
    }
    
    /**
     * ƥ��ʷ���Ԫ����ƥ������ǰ���������׳�syntax�쳣
     */
    void match(int t) throws BasicException {
        if (t != tok)
            error(E.SYNTAX);
        peek();
    }
    
    /**
     * ��ǰ�к�
     */
    int stmt;
    
    /**
     * ��Ϊ�����ʾλ��if�ڲ�
     */
    int ifs;
    
    /**
     * �Ƿ����µ�һ��
     */
    boolean newline;
    
    /**
     * ִ�еĲ�����������ʱ
     */
    int count;
    
    /**
     * ִ��,�쳣��run����
     */
    void program() throws BasicException {
        paused = false;
        l.reset();
        ifs = 0;
        newline = true;
        count = 0;
        stmt = 0;
        scr.text();
        peek();
        
        while (tok != -1) {
            try {
                if (newline) {
                    if (tok != C.INTEGER)
                        error(E.SYNTAX);
                    stmt = l.ival;
                    peek();
                    ifs = 0;
                    newline = false;
                }
                switch (tok) {
                case C.GRAPH: //ͼ��ģʽ
                    scr.graph();
                    peek();
                    break;
                case C.TEXT: //�ı�ģʽ
                    scr.text();
                    peek();
                    break;
                case C.CLS: //����
                    scr.cls();
                    peek();
                    break;
                case C.REM: //ע��
                    l.skipToNewLine();
                    peek();
                    break;
                case C.DATA:
                    int c = l.peek();
                    boolean quote = false;
                    while (c != 0xd && c != ':' && c != 0xa && c != -1) {
                        if (c == '"')
                            quote = !quote;
                        c = l.peek();
                        if (c == ':' && quote)
                            c = l.peek();
                    }
                    peek();
                    break;
                case C.INKEY: //����
                    frm.inkey();
                    peek();
                    break;
                case C.DIM: //��������
                    exe_dim();
                    break;
                case C.LET: //let��ֵ
                    peek();
                case C.ID: //��ֵ
                    exe_assign();
                    break;
                case C.PRINT: //��ʾ
                    exe_print();
                    break;
                case C.END: //����
                    peek();
                    throw new InterruptedException();
                case C.INPUT: //����
                    exe_input();
                    break;
                case C.LOCATE: //��λ
                    exe_locate();
                    break;
                case C.SWAP: //��������
                    exe_swap();
                    break;
                case C.GOTO: //��ת
                    exe_goto();
                    break;
                case C.IF:
                    exe_if();
                    break;
                case C.INTEGER: //if����е��к�
                    if (ifs == 0)
                        error(E.SYNTAX);
                    jump();
                    break;
                case C.GOSUB:
                    exe_gosub();
                    break;
                case C.RETURN:
                    exe_return();
                    break;
                case C.POP: //��ջ
                    peek();
                    if (subs.empty())
                        error(E.RETURN_WITHOUT_GOSUB);
                    subs.pop();
                    break;
                case C.CLEAR:
                    peek();
                    dr.restore();
                    fors.clear();
                    whiles.clear();
                    funs.clear();
                    subs.clear();
                    fm.closeAll();
                    for (FileState fs : files)
                        fs.close();
                    synchronized (vars) {
                        vars.clear();
                        frm.vartable.revalidate();
                    }
                    break;
                case C.INVERSE: case C.CONT: case C.BEEP: //������Ч��
                    peek();
                    break;
                case C.PLAY: //��Ч
                    peek();
                    expr(E_STRING);
                    break;
                case C.DRAW:
                    exe_draw();
                    break;
                case C.BOX:
                    exe_box();
                    break;
                case C.LINE:
                    exe_line();
                    break;
                case C.CIRCLE:
                    exe_circle();
                    break;
                case C.ELLIPSE:
                    exe_ellipse();
                    break;
                case C.ON:
                    exe_on();
                    break;
                case C.READ:
                    exe_read();
                    break;
                case C.RESTORE:
                    peek();
                    if (tok == C.INTEGER) {
                        if (!stmts.containsKey(l.ival))
                            error(E.UNDEFD_STMT);
                        dr.restore(l.ival);
                        peek();
                    } else
                        dr.restore();
                    break;
                case C.POKE:
                    exe_poke();
                    break;
                case C.CALL:
                    exe_call();
                    break;
                case C.WHILE:
                    exe_while();
                    break;
                case C.WEND:
                    exe_wend();
                    break;
                case C.FOR:
                    exe_for();
                    break;
                case C.NEXT:
                    exe_next();
                    break;
                case C.TEXTOUT:
                    exe_textout();
                    break;
                case C.SLEEP:
                    exe_sleep();
                    break;
                case C.PAINT:
                    exe_paint();
                    break;
                case C.LSET:
                    exe_lset();
                    break;
                case C.RSET:
                    exe_rset();
                    break;
                case C.OPEN:
                    exe_open();
                    break;
                case C.CLOSE:
                    exe_close();
                    break;
                case C.WRITE:
                    exe_write();
                    break;
                case C.FIELD:
                    exe_field();
                    break;
                case C.PUT:
                    exe_put();
                    break;
                case C.GET:
                    exe_get();
                    break;
                case C.FSEEK:
                    exe_fseek();
                    break;
                case C.FGET:
                    exe_fget();
                    break;
                case C.FPUT:
                    exe_fput();
                    break;
                case C.FREAD:
                    exe_fread();
                    break;
                case C.FWRITE:
                    exe_fwrite();
                    break;
                case C.LOAD:
                    exe_load();
                    break;
                case C.DEF:
                    exe_def();
                    break;
                }
                //����Ƿ���ͣ
                if (++count == 200 || paused) {
                    count = 0;
                    do {
                        Thread.sleep(delay);
                    } while (paused);
                }

                switch (tok) {
                case C.ELSE:
                    if (ifs > 0) {
                        do {
                            peek();
                        } while (tok != 0xa);
                    } else
                        error(E.SYNTAX);
                case 0xa:
                    newline = true;
                    ifs = 0;
                case ':':
                    peek();
                case -1:
                    break;
                default:
                    System.out.println(l);//debug
                    error(E.SYNTAX);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
    
    List<Integer> blist = new ArrayList<>();
    Integer[] uslsi = new Integer[0];
    /**
     * �������飨�±�ֻ�������ͳ����������ܶ������
     */
    void exe_dim() throws BasicException, InterruptedException {
        peek();
        String s;
        Array<?> ar;
        Id id;
        int i;
        
        do {
            blist.clear();
            s = l.sval;
            id = new Id(s, Id.ARRAY);
            match(C.ID);
            match('(');
            if (vars.containsKey(id))
                error(E.REDIM_ARRAY);
            switch (s.charAt(s.length() - 1)) {
            case '$':
                ar = new Array<S>();
                break;
            case '%':
                ar = new Array<Integer>();
                break;
            default:
                ar = new Array<Double>();
            }
            do {
                i = (int) (double) expr(E_NUMBER);
                blist.add(i);
                if (tok == ')')
                    break;
                match(',');
            } while (true);
            peek();
            ar.bound = blist.toArray(uslsi);
            ar.base = new int[ar.bound.length];
            ar.base[0] = 1;
            for (i = 1; i < ar.base.length; i++)
                ar.base[i] = ar.base[i - 1] * (ar.bound[i - 1] + 1);
            i--;
            switch (s.charAt(s.length() - 1)) {
            case '$':
                @SuppressWarnings("unchecked")
                Array<S> ar2 = (Array<S>) ar;
                ar2.value = new S[ar.base[i] * (ar.bound[i] + 1)];
                for (i = 0; i < ar.value.length; i++)
                    ar2.value[i] = new S();
                break;
            case '%':
                @SuppressWarnings("unchecked")
                Array<Integer> ar3 = (Array<Integer>) ar;
                ar3.value = new Integer[ar.base[i] * (ar.bound[i] + 1)];
                for (i = 0; i < ar.value.length; i++)
                    ar3.value[i] = 0;
                break;
            default:
                @SuppressWarnings("unchecked")
                Array<Double> ar4 = (Array<Double>) ar;
                ar4.value = new Double[ar.base[i] * (ar.bound[i] + 1)];
                for (i = 0; i < ar.value.length; i++)
                    ar4.value[i] = 0d;
            }
            synchronized (vars) {
                vars.put(id, ar);
                frm.vartable.revalidate();
            }
            if (tok != ',')
                break;
            peek();
        } while (true);
    }
    
    /**
     * ��������Ľӿ�
     */
    class ArrayAccess extends Access {
        public Array<?> arr;
        public int index;
        /**
         * ����һ��������ʶ���
         * @param id �����id
         * @param type ����Ԫ�ص�����
         * @param array ����
         * @param index Ԫ�������±꣨չ���ģ�
         */
        public ArrayAccess(Id id, int type, Array<?> array, int index) {
            super(id, type);
            arr = array;
            this.index = index;
        }
        public Object get() {
            return arr.value[index];
        }
        @SuppressWarnings("unchecked")
        public void put(Object val) { //�޸����鲻Ӱ������б�����ͬ��
            synchronized (arr) {
                switch (type) {
                case Id.STRING:
                    ((Array<S>) arr).value[index] = (S) val;
                    break;
                case Id.INTEGER:
                    ((Array<Integer>) arr).value[index] = (Integer) val;
                    break;
                default:
                    ((Array<Double>) arr).value[index] = (Double) val;
                }
            }
        }
        public String toString() {
            return id + "[" + index + "]";
        }
    }
    /**
     * ���ʱ����Ľӿ�
     */
    class IdAccess extends Access {
        public IdAccess(Id id) {
            super(id, id.type);
        }
        public Object get() {
            return vars.get(id);
        }
        public void put(Object val) {
            synchronized (vars) {
                vars.put(id, val);
                frm.vartable.revalidate();
            }
        }
        public String toString() {
            return id.toString();
        }
    }
    
    /**
     * ��ֵ
     */
    void exe_assign() throws BasicException, InterruptedException {
        Access a = getAccess();
        match('=');
        switch (a.type) {
        case Id.INTEGER:
            a.put((int) (double) expr(E_NUMBER));
            break;
        case Id.STRING:
            a.put((S) expr(E_STRING));
            break;
        default:
            a.put((Double) expr(E_NUMBER));
        }
    }
    
    boolean cr;
    Object ps;
    int pt;
    byte[] pb;
    /**
     * ��ʾ
     */
    void exe_print() throws BasicException, InterruptedException {
        peek();
        while (tok != ':' && tok != 0xa && tok != -1 && tok != C.ELSE) {
            if (tok != ';' && tok != ',') {
                if (tok == C.ID && l.sval.equals(C.TAB) || l.sval.equals(C.SPC)) {
                    if (l.sval.equals(C.TAB)) {
                        peek();
                        match('(');
                        pt = (int) (double) expr(E_NUMBER);
                        match(')');
                        if (pt < 1 || pt > 20) //������
                            error(E.ILLEGAL_QUANTITY);
                        if (scr.getX() >= pt)
                            scr.print((S) null, true);
                        scr.locate(scr.getY(), pt - 1);
                        ps = null;
                    } else {
                        peek();
                        match('(');
                        pt = (int) (double) expr(E_NUMBER);
                        match(')');
                        if (pt < 0)
                            error(E.ILLEGAL_QUANTITY);
                        if (pt > 0) {
                            pb = new byte[pt];
                            Arrays.fill(pb, (byte) 32);
                            ps = new S(pb);
                        } else
                            ps = null;
                    }
                } else
                    ps = expr();
            } else
                ps = null;
            cr = tok == ',' || tok == ':' || tok == 0xa || tok == -1 || tok == C.ELSE;
            if (ps instanceof Double)
                scr.print(realToString((Double) ps), cr);
            else
                scr.print((S) ps, cr);
            if (tok == ';' || tok == ',')
                peek();
        }
        scr.print((S) null, false);
    }
    
    String iprm;
    Access iacc;
    Integer ishr;
    Double idbl;
    S ss, iinp;
    /**
     * �ļ�����Ļ����
     * <br>input [#n / str$], id / id$ [, ...]
     */
    void exe_input() throws BasicException, InterruptedException {
        peek();
        if (tok == '#' || tok == C.INTEGER) { //�ļ���ȡ
            pt = getFileNumber();
            if (files[pt].state != FileState.INPUT) //ֻ��inputģʽ���ܶ�ȡ
                error(E.FILE_MODE);
            match(',');
            while (true) {
                iacc = getAccess();
                switch (iacc.type) {
                case Id.INTEGER:
                    ishr = fm.readInteger(pt);
                    if (ishr == null)
                        error(E.FILE_READ);
                    iacc.put(ishr);
                    break;
                case Id.STRING:
                    ss = fm.readS(pt);
                    if (ss == null)
                        error(E.FILE_READ);
                    iacc.put(ss);
                    break;
                default:
                    idbl = fm.readReal(pt);
                    if (idbl == null)
                        error(E.FILE_READ);
                    iacc.put(idbl);
                } //�Զ������ָ��������ö�ȡ
                if (tok == ',')
                    peek();
                else
                    break;
            }
        } else { //��Ļ��ȡ
            if (tok == C.STRING) {
                iprm = l.sval;
                peek();
                match(';');
            } else
                iprm = null;
            iacc = getAccess();
            while (true) {
                if (iprm != null) {
                    scr.print(iprm, false);
                    iprm = null;
                } else
                    scr.print("?", false);
                iinp = scr.input();
                switch (iacc.type) {
                case Id.INTEGER:
                    try {
                        iacc.put(Integer.parseInt(iinp.toString()));
                    } catch (NumberFormatException e) {
                        break;
                    }
                    break;
                case Id.STRING:
                    iacc.put(iinp);
                    break;
                default:
                    try {
                        iacc.put(Double.parseDouble(iinp.toString()));
                    } catch (NumberFormatException e) {
                        break;
                    }
                    break;
                }
                if (tok == ',') {
                    peek();
                    iacc = getAccess();
                } else
                    break;
            }
        }
    }
    
    /**
     * ��궨λ��ʡ��y���Զ���һ��
     * <br>locate y, x
     */
    void exe_locate() throws BasicException, InterruptedException {
        peek();
        int y;
        if (tok != ',')
            y = (int) (double) expr(E_NUMBER);
        else
            y = scr.getY() + 1;
        if (y < 1 || y > 5) //������
            error(E.ILLEGAL_QUANTITY);
        match(',');
        int x = (int) (double) expr(E_NUMBER);
        if (x < 1 || x > 20) //������
            error(E.ILLEGAL_QUANTITY);
        scr.locate(y - 1, x - 1);
    }
    
    /**
     * ��������
     */
    void exe_swap() throws BasicException, InterruptedException {
        peek();
        Access a2, a1 = getAccess();
        match(',');
        a2 = getAccess();
        if (a1.type != a2.type)
            error(E.TYPE_MISMATCH);
        Object obj = a1.get();
        a1.put(a2.get());
        a2.put(obj);
    }
    
    /**
     * ��ת
     */
    void exe_goto() throws BasicException {
        peek();
        jump();
    }
    
    /**
     * ����һ���кţ���ת
     */
    void jump() throws BasicException {
        int i = l.ival;
        match(C.INTEGER);
        Addr a = stmts.get(i);
        if (a == null)
            error(E.UNDEFD_STMT);
        l.resumeAddr(a);
        tok = 0xa; //��ת��һ�п�ͷ����װ�����˻��з�
    }

    /**
     * �������
     */
    void exe_if() throws BasicException, InterruptedException {
        peek();
        if (doubleIsZero((Double) expr(E_NUMBER))) {
            if (tok != C.THEN && tok != C.GOTO)
                error(E.SYNTAX);
            if (tok == C.THEN) {
                peek();
                int nest = 0;
                while (true) {
                    if (tok == C.IF)
                        nest++;
                    else if (tok == C.ELSE) {
                        if (nest == 0)
                            break;
                        nest--;
                    } else if (tok == 0xa || tok == -1)
                        break;
                    peek();
                }
            } else {
                peek();
                match(C.INTEGER);
                if (tok == ':')
                    peek();
            }
            if (tok == C.ELSE) {
                ifs++;
                tok = ':';
            }
        } else {
            ifs++;
            if (tok == C.THEN) {
                tok = ':';
            } else if (tok == C.GOTO) {
                peek();
                jump();
            } else
                error(E.SYNTAX);
        }
    }
    
    /**
     * ��ת�ӳ���
     */
    void exe_gosub() throws BasicException {
        peek();
        int i = l.ival;
        match(C.INTEGER);
        Addr a = stmts.get(i);
        if (a == null)
            error(E.UNDEFD_STMT);
        subs.push(getAddr());
        l.resumeAddr(a);
        tok = 0xa;
    }
    
    /**
     * �ӳ��򷵻�
     */
    void exe_return() throws BasicException {
        peek();
        if (tok != 0xa && tok != ':' && tok != -1 && tok != C.ELSE)
            error(E.SYNTAX);
        if (subs.empty())
            error(E.RETURN_WITHOUT_GOSUB);
        resumeAddr(subs.pop(), true);
    }
    
    int x1, y1, x2, y2, fill, ptype;
    /**
     * ����
     */
    void exe_draw() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        if (tok == ',') {
            peek();
            ptype = (int) (double) expr(E_NUMBER);
        } else
            ptype = 1;
        ptype &= 3;
        if (ptype == 3)
            ptype = 2;
        scr.draw(x1, y1, ptype);
    }
    
    void exe_line() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        match(',');
        x2 = (int) (double) expr(E_NUMBER);
        match(',');
        y2 = (int) (double) expr(E_NUMBER);
        if (tok == ',') {
            peek();
            ptype = (int) (double) expr(E_NUMBER);
        } else
            ptype = 1;
        ptype &= 3;
        if (ptype == 3)
            ptype = 2;
        scr.line(x1, y1, x2, y2, ptype);
    }
    
    void exe_box() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        match(',');
        x2 = (int) (double) expr(E_NUMBER);
        match(',');
        y2 = (int) (double) expr(E_NUMBER);
        if (tok == ',') {
            peek();
            fill = (int) (double) expr(E_NUMBER);
            if (tok == ',') {
                peek();
                ptype = (int) (double) expr(E_NUMBER);
            } else
                ptype = 1;
        } else {
            fill = 0;
            ptype = 1;
        }
        fill &= 1;
        ptype &= 3;
        if (ptype == 3)
            ptype = 2;
        scr.box(x1, y1, x2, y2, fill, ptype);
    }
    
    void exe_circle() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        match(',');
        x2 = (int) (double) expr(E_NUMBER); //r
        if (tok == ',') {
            peek();
            fill = (int) (double) expr(E_NUMBER);
            if (tok == ',') {
                peek();
                ptype = (int) (double) expr(E_NUMBER);
            } else
                ptype = 1;
        } else {
            fill = 0;
            ptype = 1;
        }
        fill &= 1;
        ptype &= 3;
        if (ptype == 3)
            ptype = 2;
        scr.circle(x1, y1, x2, fill, ptype);
    }

    void exe_ellipse() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        match(',');
        x2 = (int) (double) expr(E_NUMBER); //rx
        match(',');
        y2 = (int) (double) expr(E_NUMBER); //ry
        if (tok == ',') {
            peek();
            fill = (int) (double) expr(E_NUMBER);
            if (tok == ',') {
                peek();
                ptype = (int) (double) expr(E_NUMBER);
            } else
                ptype = 1;
        } else {
            fill = 0;
            ptype = 1;
        }
        fill &= 1;
        ptype &= 3;
        if (ptype == 3)
            ptype = 2;
        scr.ellipse(x1, y1, x2, y2, fill, ptype);
    }
    
    List<Integer> onlist = new ArrayList<>();
    /**
     * on ... goto / gosub
     */
    void exe_on() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER) - 1;
        if (tok != C.GOTO && tok != C.GOSUB)
            error(E.SYNTAX);
        y1 = tok;
        onlist.clear();
        peek();
        while (true) {
            onlist.add(l.ival);
            match(C.INTEGER);
            if (tok == ',')
                peek();
            else
                break;
        }
        if (tok != ':' && tok != 0xa && tok != -1 && tok != C.ELSE)
            error(E.SYNTAX);
        if (x1 >= 0 && x1 < onlist.size()) {
            Addr a = stmts.get(onlist.get(x1));
            if (a == null)
                error(E.UNDEFD_STMT);
            if (y1 == C.GOSUB) {
                Pack p = getAddr();
                subs.push(p);
            }
            l.resumeAddr(a);
            tok = 0xa;
        }
    }
    
    /**
     * ��ȡ����
     * <br>read id [, ...]
     */
    void exe_read() throws BasicException, InterruptedException {
        peek();
        while (true) {
            Access a = getAccess();
            switch (a.type) {
            case Id.INTEGER:
                a.put((int) (double) dr.readDouble());
                break;
            case Id.REAL:
                a.put(dr.readDouble());
                break;
            default:
                a.put(dr.readS());
            }
            if (tok != ',')
                break;
            peek();
        }
    }
    
    /**
     * �޸��ڴ�
     */
    void exe_poke() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        ram.poke(x1, (byte) y1);
    }
    
    /**
     * ���û�����
     */
    void exe_call() throws BasicException, InterruptedException {
        peek();
        ram.call((int) (double) expr(E_NUMBER));
    }
    
    While w;
    
    void exe_while() throws BasicException, InterruptedException {
        w = new While(getAddr()); //�ָ���ַ����Ҫpeek
        peek();
        //�����Ƿ����ظ���ѭ��
        whiles.xreset();
        while (whiles.xpeek() != null && !whiles.xpeek().equals(w)) {
            whiles.xpop();
        }
        if (w.equals(whiles.xpeek())) {
            while (!whiles.pop().equals(w));
        }
        
        if (!while_public()) {
            whiles.push(w);
        }
    }
    
    void exe_wend() throws BasicException, InterruptedException {
        if (whiles.empty())
            error(E.WEND_WITHOUT_WHILE);
        resumeAddr(whiles.peek().addr, false);
        peek();
        if (while_public())
            whiles.pop();
    }
    
    boolean while_public() throws BasicException, InterruptedException {
        if (doubleIsZero((Double) expr(E_NUMBER))) {
            if (tok != ':' && tok != 0xa && tok != -1)
                error(E.SYNTAX);
            peek();
            int nest = 0;
            while (tok != -1) {
                if (tok == C.WHILE)
                    nest++;
                else if (tok == C.WEND) {
                    if (nest == 0)
                        break;
                    nest--;
                }
                peek();
            }
            peek();
            return true;
        } else
            return false;
    }
    
    For ff;
    Object fo;
    Id fid;
    String fs;
    /**
     * for id = exp1 to exp2 [ step exp3 ]
     */
    void exe_for() throws BasicException, InterruptedException {
        peek();
        ff = new For();
        fs = l.sval;
        ff.var = getAccess();
        if (ff.var.id.type == Id.ARRAY) //for�ı�������������
            error(E.SYNTAX);
        match('=');
        fo = expr(E_NUMBER);
        if (ff.var.type == Id.REAL)
            ff.var.put(fo);
        else
            ff.var.put((int) (double) fo);
        match(C.TO);
        ff.dest = (Double) expr(E_NUMBER);
        if (tok == C.STEP) {
            peek();
            ff.step = (Double) expr(E_NUMBER);
        } else
            ff.step = 1d;
        ff.addr = getAddr(); //for֮��ֻ������: 0xa��-1��:,else(���ô��������)�������������
        
      //�����Ƿ����ظ���ѭ��
        fors.xreset();
        while (fors.xpeek() != null && !fors.xpeek().equals(ff))
            fors.xpop();
        if (ff.equals(fors.xpeek())) {
            while (!fors.pop().equals(ff));
        }
        
        if (ff.step > 0d && Double.compare((Double) ff.var.get(), ff.dest) > 0 ||
                ff.step < 0d && Double.compare((Double) ff.var.get(), ff.dest) < 0) {
            int nest = 0;
            while (tok != -1) {
                if (tok == C.FOR) {
                    nest++;
                    peek();
                } else if (tok == C.NEXT) {
                    peek();
                    while (tok == C.ID) {
                        if (l.sval.equals(ff.var.id.id))
                            nest = 0;
                        peek();
                        if (tok == ',')
                            peek();
                    }
                    if (nest == 0)
                        break;
                    nest--;
                } else
                    peek();
            }
        } else
            fors.push(ff);
    }
    
    /**
     * next [ id [, ...] ]
     */
    void exe_next() throws BasicException {
        peek();
        while (true) {
            if (tok == C.ID) {
                while (!fors.peek().var.id.id.equals(l.sval)) {
                    fors.pop();
                    if (fors.empty())
                        error(E.NEXT_WITHOUT_FOR);
                }
                ff = fors.peek();
                peek();
            } else if (fors.empty())
                error(E.NEXT_WITHOUT_FOR);
            else
                ff = fors.peek();
            if (ff.var.type == Id.INTEGER)
                ff.var.put((int) ((int) ff.var.get() + ff.step));
            else
                ff.var.put((Double) ff.var.get() + ff.step);
            if (ff.step > 0d && Double.compare((Double) ff.var.get(), ff.dest) > 0 ||
                    ff.step < 0d && Double.compare((Double) ff.var.get(), ff.dest) < 0) { //����ѭ��
                fors.pop();
                if (tok == ',') {
                    peek();
                    if (tok != C.ID)
                        error(E.SYNTAX);
                } else
                    break;
            } else { //����
                resumeAddr(ff.addr, true);
                break;
            }
        }
    }
    
    /**
     * Draw a string at any coordinate on the screen.
     * <br><b>Usage:</b>
     * <br>textout str$, x, y [, isSmall [, mode] ]
     * <br><b>mode:</b>
     * <br>&nbsp&nbsp&nbsp0 clear
     * <br>&nbsp&nbsp&nbsp1 or
     * <br>&nbsp&nbsp&nbsp2 not
     * <br>&nbsp&nbsp&nbspbit2 = 1 transparent; = 0 opaque
     */
    void exe_textout() throws BasicException, InterruptedException {
        peek();
        ss = (S) expr(E_STRING);
        match(',');
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        if (tok == ',') {
            peek();
            fill = (int) (double) expr(E_NUMBER);
            if (tok == ',') {
                peek();
                ptype = (int) (double) expr(E_NUMBER);
            } else
                ptype = 1;
        } else
            fill = 0;
        scr.textOut(ss, x1, y1, fill, ptype);
    }
    
    /**
     * Delay for certain milliseconds.
     * <br><b>Usage:</b>
     * <br>sleep millisecond
     */
    void exe_sleep() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        frm.getScreen().stopFlash();
        Thread.sleep(x1);
        frm.getScreen().flash();
    }
    
    /**
     * Draw a picture stored in ram at any coordinate on the screen.
     * <br><b>Usage:</b>
     * <br>paint addr, x, y, w, h [, mode]
     * <br><b>mode:</b>
     * <br>&nbsp&nbsp&nbspthe same as textout
     */
    void exe_paint() throws BasicException, InterruptedException {
        peek();
        fill = (int) (double) expr(E_NUMBER);
        match(',');
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        match(',');
        x2 = (int) (double) expr(E_NUMBER); //w
        match(',');
        y2 = (int) (double) expr(E_NUMBER); //h
        if (tok == ',') {
            peek();
            ptype = (int) (double) expr(E_NUMBER);
        } else
            ptype = 1;
        scr.paint(fill, x1, y1, x2, y2, ptype);
    }
    
    /**
     * ������ַ���
     * lset id$ = exp$
     */
    void exe_lset() throws BasicException, InterruptedException {
        peek();
        iacc = getAccess(E_STRING);
        match('=');
        pb = ((S) expr(E_STRING)).getBytes();
        byte[] pb2 = ((S) iacc.get()).getBytes();
        for (int i = 0, j = pb.length > pb2.length ? pb2.length : pb.length; i < j; i++)
            pb2[i] = pb[i];
        iacc.put(new S(pb2));
    }
    
    /**
     * �Ҷ����ַ���
     * rset id$ = exp$
     */
    void exe_rset() throws BasicException, InterruptedException {
        peek();
        iacc = getAccess(E_STRING);
        match('=');
        pb = ((S) expr(E_STRING)).getBytes();
        byte[] pb2 = ((S) iacc.get()).getBytes();
        for (int i = pb2.length - 1, j = pb.length - 1; i >= 0 && j >= 0; i--, j--)
            pb2[i] = pb[j];
        iacc.put(new S(pb2));
    }
    
    /**
     * ��ȡ�ļ���
     * @return �ļ��ţ�0��2��
     */
    int getFileNumber() throws BasicException {
        if (tok == '#')
            peek();
        int i = l.ival - 1;
        match(C.INTEGER);
        if (i > 2 || i < 0)
            error(E.FILE_NUMBER);
        return i;
    }
    
    /**
     * open str$ for FILE_MODE as #n
     * <br><b>FILE_MODE:</b>
     * <br>&nbsp&nbsp&nbspinput
     * <br>&nbsp&nbsp&nbspoutput
     * <br>&nbsp&nbsp&nbspappend
     * <br>&nbsp&nbsp&nbsprandom
     * <br>&nbsp&nbsp&nbspbinary
     */
    void exe_open() throws BasicException, InterruptedException {
        peek();
        ss = (S) expr(E_STRING);
        if (!ss.contains((byte) '.'))
            ss = ss.concat(new S(".dat"));
        match(C.FOR);
        pt = tok;
        if (tok != C.INPUT && tok != C.OUTPUT && tok != C.APPEND && tok != C.RANDOM && tok != C.BINARY)
            error(E.FILE_MODE);
        peek();
        match(C.AS);
        x1 = getFileNumber();
        if (files[x1].state != FileState.CLOSE)
            error(E.FILE_REOPEN);
        if (pt == C.RANDOM && tok == C.ID && l.sval.equals("len")) { //LEN = n
            peek();
            match('=');
            y1 = files[x1].len = l.ival;
            match(C.INTEGER);
            if (y1 < 1)
                error(E.ILLEGAL_QUANTITY);
        }
        switch (pt) {
        case C.INPUT:
            files[x1].state = FileState.INPUT;
            cr = fm.open(ss.toString(), x1, FileManager.INPUT);
            break;
        case C.OUTPUT:
            files[x1].state = FileState.OUTPUT;
            cr = fm.open(ss.toString(), x1, FileManager.OUTPUT);
            break;
        case C.APPEND:
            files[x1].state = FileState.APPEND;
            cr = fm.open(ss.toString(), x1, FileManager.APPEND);
            break;
        case C.BINARY:
            files[x1].state = FileState.BINARY;
            cr = fm.open(ss.toString(), x1, FileManager.RANDOM);
            break;
        default:
            files[x1].state = FileState.RANDOM;
            cr = fm.open(ss.toString(), x1, FileManager.RANDOM);
            files[x1].rec = null;
        }
        if (!cr)
            error(E.FILE_OPEN);
        files[x1].size = fm.getLength(x1);
    }
    /**
     * close #n
     */
    void exe_close() throws BasicException {
        peek();
        pt = getFileNumber();
        if (files[pt].state == FileState.CLOSE)
            error(E.FILE_CLOSE);
        files[pt].close();
        if (!fm.close(pt))
            error(E.FILE_CLOSE);
    }
    
    /**
     * write #n, expr [, ...]
     */
    void exe_write() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.APPEND && files[pt].state != FileState.OUTPUT)
            error(E.FILE_MODE);
        match(',');
        while (true) {
            fo = expr();
            if (fo instanceof S)
                fm.writeQuotedS((S) fo, pt);
            else
                fm.writeReal((Double) fo, pt);
            if (tok == ',') {
                peek();
                if (!fm.writeComma(pt))
                    error(E.FILE_WRITE);
            } else {
                if (!fm.writeEOF(pt))
                    error(E.FILE_WRITE);
                break;
            }
        }
    }
    
    Record rec;
    List<Integer> fil = new ArrayList<>();
    List<Access> fal = new ArrayList<>();
    Access[] uslsac = new Access[0];
    /**
     * �����ļ���������id$����'\0'���
     * field #n, m as id$ [, ...]
     */
    void exe_field() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.RANDOM)
            error(E.FILE_MODE);
        match(',');
        rec = new Record();
        fil.clear();
        fal.clear();
        while (true) {
            x1 = l.ival;
            match(C.INTEGER);
            if (x1 < 1)
                error(E.ILLEGAL_QUANTITY);
            match(C.AS);
            iacc = getAccess();
            if (iacc.type != Id.STRING || iacc.id.type == Id.ARRAY) //����������
                error(E.SYNTAX);
            fil.add(x1);
            fal.add(iacc);
            iacc.put(new S(new byte[x1])); //�ַ�����0���
            rec.total += x1;
            if (tok == ',')
                peek();
            else
                break;
        }
        rec.size = fil.toArray(uslsi);
        rec.acc = fal.toArray(uslsac);
        files[pt].rec = rec;
        if (files[pt].len > 0 && rec.total != files[pt].len)
            error(E.ASK_CACHE);
    }
    
    /**
     * put #n, expr
     */
    void exe_put() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.RANDOM)
            error(E.FILE_MODE);
        match(',');
        x1 = (int) (double) expr(E_NUMBER) - 1;
        if (x1 < 0)
            error(E.ILLEGAL_QUANTITY);
        rec = files[pt].rec;
        if (rec == null)
            error(E.NOT_ASK_CACHE);
        if (!fm.seek(rec.total * x1, pt))
            error(E.RECORD_NUMBER);
        for (x2 = 0; x2 < rec.size.length; x2++) {
            if (!fm.writeBytes(Arrays.copyOf(((S) rec.acc[x2].get()).getBytes(), rec.size[x2]), pt))
                error(E.FILE_WRITE);
        }
    }
    
    /**
     * get #n, expr
     */
    void exe_get() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.RANDOM)
            error(E.FILE_MODE);
        match(',');
        x1 = (int) (double) expr(E_NUMBER) - 1;
        if (x1 < 0)
            error(E.ILLEGAL_QUANTITY);
        rec = files[pt].rec;
        if (rec == null)
            error(E.NOT_ASK_CACHE);
        if (!fm.seek(rec.total * x1, pt) || fm.eof(pt))
            error(E.RECORD_NUMBER);
        for (x2 = 0; x2 < rec.size.length; x2++) {
            pb = fm.readBytes(rec.size[x2], pt);
            if (pb == null)
                error(E.FILE_READ);
            rec.acc[x2].put(new S(pb));
        }
    }
    
    /**
     * Locate binary file pointer.
     * <br><b>Usage:</b>
     * <br>fseek #n, pos
     * <br>&nbsp&nbsppos: 0-indexed
     */
    void exe_fseek() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        x1 = (int) (double) expr(E_NUMBER);
        if (x1 < 0)
            error(E.ILLEGAL_QUANTITY);
        if (!fm.seek(x1, pt))
            error(E.FILE_SEEK);
    }
    
    /**
     * Read data to ram from binary file.
     * <br><b>Usage:</b>
     * <br>fread #n, addr, size
     */
    void exe_fread() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        if (y1 < 0)
            error(E.ILLEGAL_QUANTITY);
        if (y1 > 0) {
            pb = fm.readBytes(y1, pt);
            if (pb == null)
                error(E.FILE_READ);
            ram.poke(x1, pb);
        }
    }
    
    /**
     * Write data to binary file from ram.
     * <br><b>Usage:</b>
     * <br>fwrite #n, addr, size
     */
    void exe_fwrite() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        if (y1 < 0)
            error(E.ILLEGAL_QUANTITY);
        if (y1 > 0 && !fm.writeBytes(ram.peek(x1, y1), pt))
                error(E.FILE_WRITE);
    }
    
    long fln;
    /**
     * Read primitive from binary file.
     * <br><b>Usage:</b>
     * <br>fget #n, access / access$ / access% [, ...]
     */
    void exe_fget() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        while (true) {
            iacc = getAccess();
            switch (iacc.type) {
            case Id.INTEGER:
                pb = fm.readBytes(2, pt);
                if (pb == null)
                    error(E.FILE_READ);
                iacc.put((pb[0] & 0xff) | pb[1]); //little endian
                break;
            case Id.REAL:
                pb = fm.readBytes(8, pt);
                if (pb == null)
                    error(E.FILE_READ);
                for (x1 = 7; x1 >= 0; x1--) {
                    fln <<= 8;
                    fln |= pb[x1] & 0xff;
                }
                iacc.put(Double.longBitsToDouble(fln));
                break;
            default:
                ss = (S) iacc.get();
                if (ss.length() > 0) {
                    pb = fm.readBytes(ss.length(), pt);
                    if (pb == null)
                        error(E.FILE_READ);
                    iacc.put(new S(pb));
                }
            }
            if (tok == ',')
                peek();
            else
                break;
        }
    }
    
    int fshr;
    /**
     * Write primitive to binary file.
     * <br><b>Usage:</b>
     * <br>fwrite #n, access / access$ / access% [, ...]
     */
    void exe_fput() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        while (true) {
            iacc = getAccess();
            switch (iacc.type) {
            case Id.INTEGER:
                fshr = (int) iacc.get();
                if (!(fm.writeByte(fshr, pt) && fm.writeByte(fshr >>> 8, pt)))
                        error(E.FILE_WRITE);
                break;
            case Id.REAL:
                fln = Double.doubleToLongBits((Double) iacc.get());
                for (x1 = 0; x1 < 8; x1++) {
                    if (!fm.writeByte((byte) fln, pt))
                        error(E.FILE_WRITE);
                    fln >>>= 8;
                }
                break;
            default:
                pb = ((S) iacc.get()).getBytes();
                if (pb.length > 0 && !fm.writeBytes(pb, pt))
                    error(E.FILE_WRITE);
            }
            if (tok == ',')
                peek();
            else
                break;
        }
    }
    
    /**
     * Write byte(s) to binary file.
     * <br><b>Usage:</b>
     * <br>fputc #n, expr / expr$ [, ...]
     * <br>If expr is a string, then the first byte of the string will
     * <br>be written to file; otherwise the lowest 8 bit of integer
     * <br>presentation of expr will be written.
     */
    void exe_fputc() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        while (true) {
            fo = expr();
            if (fo instanceof Double) {
                if (!fm.writeByte((int) (double) fo, pt))
                    error(E.FILE_WRITE);
            } else {
                if (!fm.writeByte(((S) fo).byteAt(0), pt))
                    error(E.FILE_WRITE);
            }
            if (tok == ',')
                peek();
            else
                break;
        }
    }
    
    /**
     * Load direct data to ram
     * <br><b>Usage:</b>
     * <br>load addr, exp1 [, ...]
     */
    void exe_load() throws BasicException, InterruptedException {
        peek();
        pt = (int) (double) expr(E_NUMBER);
        match(',');
        while (true) {
            ram.poke(pt++, (byte) (int) (double) expr(E_NUMBER));
            if (tok == ',')
                peek();
            else
                break;
        }
    }
    
    /**
     * def fn f(x) = ...
     */
    void exe_def() throws BasicException {
        peek();
        match(C.FN);
        Fn f = new Fn();
        fs = l.sval;
        match(C.ID);
        match('(');
        f.var = l.sval;
        match(C.ID);
        match(')');
        f.addr = getAddr();
        match('=');
        while (tok != ':' && tok != 0xa && tok != -1)
            peek();
        if (fs.charAt(fs.length() - 1) == '$')
            f.ftype = E_STRING;
        else
            f.ftype = E_NUMBER;
        if (f.var.charAt(f.var.length() - 1) == '$')
            f.vtype = E_STRING;
        else
            f.vtype = E_NUMBER;
        funs.put(fs, f);
    }
    
    /**
     * ��ȡָ�����͵ķ��ʽӿ�
     * @param type ���ͳ�������exprһ��
     */
    Access getAccess(int type) throws BasicException, InterruptedException {
        Access gaacc = getAccess();
        if (type == E_NUMBER && gaacc.type == Id.STRING ||
                type == E_STRING && gaacc.type != Id.STRING)
            error(E.TYPE_MISMATCH);
        return gaacc;
    }
    
    /**
     * ��ȡid���ʽӿڣ������Ƿ���id��ͷ
     */
    Access getAccess() throws BasicException, InterruptedException {
        String s = l.sval;
        Id id;
        int index = 0, t, base, d;
        
        match(C.ID);
        if (tok == '(') { //����
            peek();
            id = new Id(s, Id.ARRAY);
            if (!vars.containsKey(id)) { //����δ���壬�����±������������
                base = d = 1;
                switch (s.charAt(s.length() - 1)) {
                case '$':
                    Array<S> sar = new Array<>();
                    while (true) {
                        t = (int) (double) expr(E_NUMBER);
                        if (t > 10 || t < 0)
                            error(E.BAD_SUBSCRIPT);
                        index += t * base;
                        base *= 11;
                        if (tok == ',') {
                            peek();
                            d++;
                        } else
                            break;
                    }
                    match(')');
                    
                    sar.base = new int[d];
                    sar.bound = new Integer[d];
                    base = 1;
                    for (int i = 0; i < d; i++) {
                        sar.base[i] = base;
                        base *= 11;
                        sar.bound[i] = 10;
                    }
                    sar.value = new S[base];
                    for (int i = 0; i < base; i++)
                        sar.value[i] = new S();
                    synchronized (vars) {
                        vars.put(id, sar);
                        frm.vartable.revalidate();
                    }
                    
                    return new ArrayAccess(id, Id.STRING, sar, index);
                case '%':
                    Array<Integer> iar = new Array<>();
                    while (true) {
                        t = (int) (double) expr(E_NUMBER);
                        if (t > 10 || t < 0)
                            error(E.BAD_SUBSCRIPT);
                        index += t * base;
                        base *= 11;
                        if (tok == ',') {
                            peek();
                            d++;
                        } else
                            break;
                    }
                    match(')');
                    
                    iar.base = new int[d];
                    iar.bound = new Integer[d];
                    base = 1;
                    for (int i = 0; i < d; i++) {
                        iar.base[i] = base;
                        base *= 11;
                        iar.bound[i] = 10;
                    }
                    iar.value = new Integer[base];
                    for (int i = 0; i < base; i++)
                        iar.value[i] = 0;
                    synchronized (vars) {
                        vars.put(id, iar);
                        frm.vartable.revalidate();
                    }
                    
                    return new ArrayAccess(id, Id.INTEGER, iar, index);
                default:
                    Array<Double> rar = new Array<>();
                    while (true) {
                        t = (int) (double) expr(E_NUMBER);
                        if (t > 10 || t < 0)
                            error(E.BAD_SUBSCRIPT);
                        index += t * base;
                        base *= 11;
                        if (tok == ',') {
                            peek();
                            d++;
                        } else
                            break;
                    }
                    match(')');
                    
                    rar.base = new int[d];
                    rar.bound = new Integer[d];
                    base = 1;
                    for (int i = 0; i < d; i++) {
                        rar.base[i] = base;
                        base *= 11;
                        rar.bound[i] = 10;
                    }
                    rar.value = new Double[base];
                    for (int i = 0; i < base; i++)
                        rar.value[i] = 0d;
                    synchronized (vars) {
                        vars.put(id, rar);
                        frm.vartable.revalidate();
                    }
                    
                    return new ArrayAccess(id, Id.REAL, rar, index);
                }
            }
            switch (s.charAt(s.length() - 1)) { //��ȡ�����±�
            case '$':
                @SuppressWarnings("unchecked")
                Array<S> sar = (Array<S>) vars.get(id);
                for (int i = 0; i < sar.bound.length; i++) {
                    t = (int) (double) expr(E_NUMBER);
                    if (t > sar.bound[i] || t < 0)
                        error(E.BAD_SUBSCRIPT);
                    index += t * sar.base[i];
                    if (i < sar.bound.length - 1)
                        match(',');
                }
                match(')');
                return new ArrayAccess(id, Id.STRING, sar, index);
            case '%':
                @SuppressWarnings("unchecked")
                Array<Integer> iar = (Array<Integer>) vars.get(id);
                for (int i = 0; i < iar.bound.length; i++) {
                    t = (int) (double) expr(E_NUMBER);
                    if (t > iar.bound[i] || t < 0)
                        error(E.BAD_SUBSCRIPT);
                    index += t * iar.base[i];
                    if (i < iar.bound.length - 1)
                        match(',');
                }
                match(')');
                return new ArrayAccess(id, Id.INTEGER, iar, index);
            default:
                @SuppressWarnings("unchecked")
                Array<Double> rar = (Array<Double>) vars.get(id);
                for (int i = 0; i < rar.bound.length; i++) {
                    t = (int) (double) expr(E_NUMBER);
                    if (t > rar.bound[i] || t < 0)
                        error(E.BAD_SUBSCRIPT);
                    index += t * rar.base[i];
                    if (i < rar.bound.length - 1)
                        match(',');
                }
                match(')');
                return new ArrayAccess(id, Id.REAL, rar, index);
            }
        } else { //��ͨ����
            switch (s.charAt(s.length() - 1)) {
            case '$':
                id = new Id(s, Id.STRING);
                break;
            case '%':
                id = new Id(s, Id.INTEGER);
                break;
            default:
                id = new Id(s, Id.REAL);
            }
            if (!vars.containsKey(id)) { //����δ���壬����һ���±���
                synchronized (vars) {
                    switch (id.type) {
                    case Id.INTEGER:
                        vars.put(id, 0);
                        break;
                    case Id.STRING:
                        vars.put(id, new S());
                        break;
                    default:
                        vars.put(id, 0d);
                    }
                    frm.vartable.revalidate();
                }
            }
            return new IdAccess(id);
        }
    }
    
    static int E_NUMBER = 1, E_STRING = 2;
    
    /**
     * ��ȡָ�����ͱ��ʽ
     * @param type ����<br><i>E_NUMBER</i> �� <i>E_STRING</i>
     */
    Object expr(int type) throws BasicException, InterruptedException {
        Object r = E(0);
        if (type == E_NUMBER && r instanceof S ||
                type == E_STRING && !(r instanceof S))
            error(E.TYPE_MISMATCH);
        return r;
    }
    /**
     * ��ȡ���ʽ
     */
    Object expr() throws BasicException, InterruptedException {
        return E(0);
    }
    
    Object E(int p) throws BasicException, InterruptedException {
        Object result = F();
        int i;
        while (getp(tok) > p) {
            i = tok;
            peek();
            result = arith(i, result, E(getp(i)));
        }
        return result;
    }

    Stack2<String> fns = new Stack2<>(); //�����Ա�����
    Stack2<Object> fnvar = new Stack2<>(); //�����Ա���ֵ
    //�洢��һ�������
    double rnd;
    /**
     * ֻ�᷵��Double��S
     */
    Object F() throws BasicException, InterruptedException { //num, string, var, +-, not, inkey, (
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        
        Object o = null;
        String s;
        S s2;
        int t, t2;
        long lt;
        byte[] b;
        double d;
        
        switch (tok) {
        case C.INTEGER: case C.REAL:
            Double r = l.rval;
            peek();
            return r;
        case C.STRING:
            s = l.sval;
            peek();
            return new S(s);
        case C.ID:
            if (infuns.contains(l.sval)) { //���ú���
                s = l.sval;
                peek();
                match('(');
                switch (s) {
                case C.ABS: //����ֵ
                    o = Math.abs((Double) expr(E_NUMBER));
                    break;
                case C.ASC: //ascii�룬�ַ�������Ϊ0�򷵻�0
                    s2 = (S) expr(E_STRING);
                    if (s2.length() == 0)
                        o = 0d;
                    else
                        o = (double) s2.charAt(0);
                    break;
                case C.ATN: //������ֵ
                    o = Math.atan((Double) expr(E_NUMBER));
                    break;
                case C.CHR: //ascii��תΪ�ַ�����ȡ�Ͱ�λ�������⣺���bit7=1������Զ�ת��Ϊ�ʺ�
                    o = new S((byte) (double) expr(E_NUMBER));
                    break;
                case C.COS: //����ֵ
                    o = Math.cos((Double) expr(E_NUMBER));
                    break;
                case C.CVI: //�ַ���ǰ2byteתΪ����.little endian
                    b = Arrays.copyOf(((S) expr(E_STRING)).getBytes(), 2);
                    o = (double) ((b[0] & 0xff) + ((b[1] & 0xff) << 8));
                    break;
                case C.MKI: //����ת��Ϊ2byte�ַ���
                    t = (int) (double) expr(E_NUMBER);
                    o = new S(new byte[] {(byte) t, (byte) (t >>> 8)});
                    break;
                case C.CVS: //�ַ���ǰ8�ֽ�ת��Ϊdouble
                    b = Arrays.copyOf(((S) expr(E_STRING)).getBytes(), 8);
                    for (lt = t = 0; t < 8; t++) {
                        lt |= b[7 - t];
                        lt <<= 8;
                    }
                    o = Double.longBitsToDouble(lt);
                    break;
                case C.MKS: //doubleת��Ϊ8�ֽ��ַ���
                    lt = Double.doubleToLongBits((Double) expr(E_NUMBER));
                    b = new byte[8];
                    for (t = 0; t < 8; t++) {
                        b[t] = (byte) lt;
                        lt >>>= 8;
                    }
                    o = new S(b);
                    break;
                case C.EXP: //e��n�η�
                    o = Math.exp((Double) expr(E_NUMBER));
                    break;
                case C.INT: //ȡ��
                    o = Math.floor((Double) expr(E_NUMBER));
                    break;
                case C.LEFT: //ȡ�ַ�����ǰn�ֽ�
                    s2 = (S) expr(E_STRING);
                    b = s2.getBytes();
                    match(',');
                    t = (int) (double) expr(E_NUMBER);
                    if (t < 1)
                        error(E.ILLEGAL_QUANTITY);
                    if (t > b.length)
                        t = b.length;
                    o = new S(b, 0, t);
                    break;
                case C.LEN: //�ַ��������ֽڣ�
                    o = (double) ((S) expr(E_STRING)).length();
                    break;
                case C.LOG: //ln
                    o = Math.log((Double) expr(E_NUMBER));
                    break;
                case C.MID: //ȡ�ַ�����m���ֽڿ�ʼ��n�ֽڣ���ʡ��n��n=1
                    s2 = (S) expr(E_STRING);
                    b = s2.getBytes();
                    match(',');
                    t = (int) (double) expr(E_NUMBER) - 1;
                    if (tok == ',') {
                        peek();
                        t2 = (int) (double) expr(E_NUMBER);
                    } else
                        t2 = 1;
                    if (t >= b.length || t < 0 || t2 < 1)
                        error(E.ILLEGAL_QUANTITY);
                    o = new S(b, t, t + t2);
                    break;
                case C.POS: //��ȡ�������ꡣ����û��
                    expr(E_NUMBER);
                    o = (double) (scr.getX() + 1);
                    break;
                case C.RIGHT: //ȡ�ַ�����n���ֽ�
                    s2 = (S) expr(E_STRING);
                    b = s2.getBytes();
                    match(',');
                    t = (int) (double) expr(E_NUMBER);
                    if (t < 1)
                        error(E.ILLEGAL_QUANTITY);
                    if (t > b.length)
                        t = b.length;
                    o = new S(b, b.length - t, b.length);
                    break;
                case C.RND: //�������������Ϊ0�򷵻���һ�������
                    if (doubleIsZero((Double) expr(E_NUMBER)))
                        o = rnd;
                    else
                        o = rnd = Math.random();
                    break;
                case C.SGN:
                    d = (Double) expr(E_NUMBER);
                    o = doubleIsZero(d) ? 0d : Double.compare(d, 0d) > 0 ? 1d : -1d;
                    break;
                case C.SIN:
                    o = Math.sin((Double) expr(E_NUMBER));
                    break;
                case C.SQR:
                    o = Math.sqrt((Double) expr(E_NUMBER));
                    break;
                case C.STR: //ʵ��ת�ַ���
                    o = new S(realToString((Double) expr(E_NUMBER)));
                    break;
                case C.TAN:
                    o = Math.tan((Double) expr(E_NUMBER));
                    break;
                case C.VAL: //�ַ���תʵ�������ַ���Ϊ���򷵻�0.����ǰ���հ׷�
                    o = str2d(((S) expr(E_STRING)).toString());
                    break;
                case C.PEEK: //�����ڴ淶Χ����0
                    o = (double) (ram.peek((int) (double) expr(E_NUMBER)) & 0xff);
                    break;
                case C.POINT: //��ȡ�㣬��������Ļ�򷵻�1
                    t = (int) (double) expr(E_NUMBER);
                    match(',');
                    t2 = (int) (double) expr(E_NUMBER);
                    o = (double) scr.point(t, t2);
                    break;
                case C.CHECKKEY: //���ĳ���Ƿ񱻰���,�Ƿ���1
                    t = (int) (double) expr(E_NUMBER);
                    if (t > 127 || t < 0)
                        error(E.ILLEGAL_QUANTITY);
                    o = frm.checkKey(recoverKeyCode(t)) ? 1d : 0d;
                    break;
                case C.EOF: //�Ƿ񵽴��ļ�β���Ƿ���0�����򷵻�1.ֻ��inputģʽ����Ч.���������ǳ���
                    t = getFileNumber();
                    if (files[t].state != FileState.INPUT)
                        error(E.FILE_MODE);
                    o = fm.eof(t) ? 0d : 1d;
                    break;
                case C.LOF: //�����ļ����ȡ�ֻҪ�ļ��򿪾Ϳ��Ե���.���������ǳ���
                    o = (double) files[getFileNumber()].size;
                    break;
                case C.FTELL: //���ص�ǰ�ļ�ָ�롣ֻ��binary����Ч
                    t = getFileNumber();
                    if (files[t].state != FileState.BINARY)
                        error(E.FILE_MODE);
                    o = (double) fm.tell(t);
                    break;
                case C.FGETC: //��ȡһ���ֽڡ�ֻ��BINARY����Ч
                    t = getFileNumber();
                    if (files[t].state != FileState.BINARY)
                        error(E.FILE_MODE);
                    try {
                        o = (double) (fm.readByte(t) & 0xff); //unsigned
                    } catch (NullPointerException e) {
                        error(E.FILE_READ);
                    }
                    break;
                }
                match(')');
                return o;
            } else if (!fns.empty() && l.sval.equals(fns.peek())) { //�����Ա���
                peek();
                return fnvar.peek();
            } else { //�Ǳ���
                Object oo = getAccess().get();
                if (oo instanceof Integer)
                    return (double) (int) oo;
                else
                    return oo;
            }
        case '+': case '-':
            t = tok;
            peek();
            o = E(5);
            if (!(o instanceof Double))
                error(E.TYPE_MISMATCH);
            return t == '-' ? -(Double) o : o;
        case C.NOT:
            peek();
            o = E(12);
            if (!(o instanceof Double))
                error(E.TYPE_MISMATCH);
            return doubleIsZero((Double) o) ? 1d : 0d;
        case C.INKEY:
            peek();
            return new S((byte) convertKeyCode(frm.inkey(false)));
        case '(':
            peek();
            Object ooo = E(0);
            match(')');
            return ooo;
        case C.FN:
            peek();
            s = l.sval;
            match(C.ID);
            match('(');
            Fn fn = funs.get(s);
            if (fn == null)
                error(E.UNDEFD_FUNC);
            o = expr(fn.vtype); //�Ա���
            Pack p = getAddr();
            fns.push(fn.var);
            fnvar.push(o);
            resumeAddr(fn.addr, false);
            peek();
            o = expr(fn.ftype); //����ֵ
            resumeAddr(p, true);
            match(')');
            fns.pop();
            fnvar.pop();
            return o;
        default:
            error(E.SYNTAX);
            return 0d;
        }
    }
    
    int getp(int op) {
        switch (op) {
        case C.GTE: case C.LTE: case '=': case C.NEQ: case '>': case '<':
            return 2;
        case '+': case '-':
            return 4;
        case '*': case '/':
            return 6;
        case '^':
            return 8;
        case C.AND: case C.OR:
            return 1;
        default:
            return 0;
        }
    }
    
    Object arith(int op, Object a, Object b) throws BasicException {
        switch (op) {
        case '+':
            if (a instanceof Double && b instanceof Double)
                return (Double) a + (Double) b;
            else if (a instanceof S && b instanceof S)
                return S.concat((S) a, (S) b);
            break;
        case '-':
            if (a instanceof Double && b instanceof Double)
                return (Double) a - (Double) b;
            break;
        case '*':
            if (a instanceof Double && b instanceof Double)
                return (Double) a * (Double) b;
            break;
        case '/':
            if (a instanceof Double && b instanceof Double) {
                if (doubleIsZero((Double) b))
                        error(E.DIVISION_BY_ZERO);
                return (Double) a / (Double) b;
            }
            break;
        case '^':
            if (a instanceof Double && b instanceof Double)
                return Math.pow((Double) a, (Double) b);
            break;
        case C.GTE:
            if (a instanceof Double && b instanceof Double)
                return Double.compare((Double) a, (Double) b) > 0 ||
                        doubleEqual((Double) a, (Double) b) ? 1d : 0d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).compareTo((S) b) >= 0 ? 1d : 0d;
            break;
        case C.LTE:
            if (a instanceof Double && b instanceof Double)
                return Double.compare((Double) a, (Double) b) < 0 ||
                        doubleEqual((Double) a, (Double) b) ? 1d : 0d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).compareTo((S) b) <= 0 ? 1d : 0d;
            break;
        case '>':
            if (a instanceof Double && b instanceof Double)
                return Double.compare((Double) a, (Double) b) > 0 ? 1d : 0d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).compareTo((S) b) > 0 ? 1d : 0d;
            break;
        case '<':
            if (a instanceof Double && b instanceof Double)
                return Double.compare((Double) a, (Double) b) < 0 ? 1d : 0d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).compareTo((S) b) < 0 ? 1d : 0d;
            break;
        case '=':
            if (a instanceof Double && b instanceof Double)
                return doubleEqual((Double) a, (Double) b) ? 1d : 0d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).equals(b) ? 1d : 0d;
            break;
        case C.NEQ:
            if (a instanceof Double && b instanceof Double)
                return doubleEqual((Double) a, (Double) b) ? 0d : 1d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).equals(b) ? 0d : 1d;
            break;
        case C.OR:
            if (a instanceof Double && b instanceof Double)
                return doubleIsZero((Double) a) && doubleIsZero((Double) b) ? 0d : 1d;
            break;
        case C.AND:
            if (a instanceof Double && b instanceof Double)
                return doubleIsZero((Double) a) || doubleIsZero((Double) b) ? 0d : 1d;
            break;
        default:
            error(E.SYNTAX);
        }
        error(E.TYPE_MISMATCH);
        return null;
    }
    
    /**
     * �׳��쳣
     * @param type �쳣����
     */
    void error(int type) throws BasicException {
        throw new BasicException(type);
    }
    
    /**
     * ��ȡ��ǰ��ַ���ʷ���������ַ+�к�+ifǶ��+��ǰ�ʷ���Ԫ��
     */
    Pack getAddr() {
        return new Pack(l.getAddr(), stmt, ifs, tok);
    }
    
    /**
     * �ָ���ַ
     * @param p ����ĵ�ַ
     * @param b �Ƿ�ָ�Դ��ַ�Ĵʷ���Ԫ
     */
    void resumeAddr(Pack p, boolean b) {
        l.resumeAddr(p.addr);
        stmt = p.stmt;
        ifs = p.ifs;
        if (b)
            tok = p.tok;
    }
    
    /**
     * ��ȡwqx��ֵ
     */
    int inkey() throws InterruptedException {
        return convertKeyCode(frm.inkey());
    }
}

/**
 * forѭ����Ϣ
 */
class For {
    /**
     * �Ա���
     */
    public Access var;
    /**
     * Ŀ��ֵ
     */
    public double dest;
    /**
     * ����
     */
    public double step;
    /**
     * ��װ�õĵ�ַ
     */
    public Pack addr;
    
    public boolean equals(Object o) {
        return o instanceof For ? var.equals(((For) o).var) : false;
    }
    
    public String toString() {
        return "for:" + var + " to " + dest + " step " + step + ":" + addr;
    }
}

/**
 * whileѭ����Ϣ
 */
class While {
    public Pack addr;
    
    public boolean equals(Object o) {
        return o instanceof While ? addr.equals(((While) o).addr) : false;
    }
    
    public While() {}
    
    public While(Pack a) {
        addr = a;
    }
    
    public String toString() {
        return "while:" + addr;
    }
}

/**
 * �Զ��庯��
 */
class Fn {
    
    /**
     * �Ա�����������double��
     */
    public String var;
    /**
     * �������ʽ����,E_NUMBER��E_STRING
     */
    public int ftype;
    /**
     * �Ա������ͣ�ͬ��
     */
    public int vtype;
    
    /**
     * �������ʽ���ڵĵ�ַ
     */
    public Pack addr;
    
    public String toString() {
        return "(" + var + "):" + addr;
    }
}

/**
 * ���кź͵�ַ��װ����
 */
class Pack {
    public final Addr addr;
    public final int stmt, ifs, tok;
    
    public boolean equals(Object o) {
        return o instanceof Pack ? addr.equals(((Pack) o).addr) : false;
    }
    
    /**
     * ��װ
     * @param a �ʷ��������ĵ�ַ
     * @param s ��ǰ�к�
     */
    public Pack(Addr a, int s, int ifs, int tok) {
        addr = a;
        stmt = s;
        this.ifs = ifs;
        this.tok = tok;
    }
    
    public String toString() {
        return "[P " + addr + " S:" + stmt + "]";
    }
}

/**
 * ���ڱ������ʵĽӿ�
 */
abstract class Access {
    public final Id id;
    public final int type;
    public Access(Id id, int type) {
        this.id = id;
        this.type = type;
    }
    public abstract Object get();
    public abstract void put(Object val);
    public boolean equals(Object o) {
        return o instanceof Access && id.equals(((Access) o).id);
    }
}

/**
 * ���ݶ�ȡ��
 */
class DataReader {
    private byte[] z;
    private int cap; //����
    private int pos; //��ȡλ��
    
    private List<Integer> stmt = new ArrayList<>(), mark = new ArrayList<>();
    
    public DataReader() {
        this(1024);
    }
    
    public DataReader(int capacity) {
        z = new byte[capacity];
        stmt.clear();
        mark.clear();
    }
    
    public void clear() {
        cap = 0;
        pos = 0;
        z = new byte[1024];
    }
    
    public void append(byte b) {
        ensureCapacity(1);
        z[cap++] = b;
    }
    
    public void append(int b) {
        append((byte) b);
    }
    
    void ensureCapacity(int i) {
        while (cap + i > z.length)
            z = Arrays.copyOf(z, z.length + 1024);
    }
    
    public void addComma() {
        append(',');
    }
    
    int getc() {
        return pos < cap ? z[pos++] & 0xff : -1;
    }
    
    void peek() {
        c = getc();
    }
    
    int c;
    ByteStringBuffer bsb = new ByteStringBuffer();
    
    public String readString() throws BasicException {
        peek();
        bsb.clear();
        if (c == -1)
            throw new BasicException(E.OUT_OF_DATA);
        if (c == '"') {
            peek();
            while (c != '"' && c != -1) {
                bsb.append(c);
                peek();
            }
            if (c == '"') //��������
                peek();
        } else {
            while (c != ',') {
                bsb.append(c);
                peek();
            }
        }
        return bsb.toString();
    }
    
    public S readS() throws BasicException {
        peek();
        bsb.clear();
        if (c == -1)
            throw new BasicException(E.OUT_OF_DATA);
        if (c == '"') {
            peek();
            while (c != '"' && c != -1) {
                bsb.append(c);
                peek();
            }
            if (c == '"') //��������
                peek();
        } else {
            while (c != ',') {
                bsb.append(c);
                peek();
            }
        }
        return bsb.toS();
    }
    
    String s;
    
    public double readDouble() throws BasicException {
        peek();
        bsb.clear();
        if (c == -1)
            throw new BasicException(E.OUT_OF_DATA);
        while (c != ',') {
            bsb.append(c);
            peek();
        }
        s = bsb.toString();
        if (s.length() > 0) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw new BasicException(E.SYNTAX);
            }
        } else
            return 0d;
    }
    
    public void mark(int stm) {
        if (!stmt.contains(stm)) {
            stmt.add(stm);
            mark.add(cap);
        }
    }
    
    public void restore() {
        pos = 0;
    }
    
    public void restore(int stm) {
        int i;
        for (i = 0; i < stmt.size(); i++) {
            if (stmt.get(i) >= stm)
                break;
        }
        if (i < stmt.size())
            pos = mark.get(i);
        else
            pos = cap;
    }
    
    public String toString() {
        return "data:" + new String(z, 0, cap);
    }
}

/**
 * ����ļ���¼
 */
class Record {
    public int total;
    public Integer[] size;
    public Access[] acc;
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("record[").append(total).append("]:{");
        for (int i = 0; i < size.length; i++) {
            sb.append(acc).append("[").append(size).append("]");
            if (i < size.length - 1)
                sb.append(",");
        }
        return sb.append("}").toString();
    }
}
/**
 * �ļ�״̬
 */
class FileState {
    public static final int INPUT = 1, OUTPUT = 2, APPEND = 3, RANDOM = 4, BINARY = 5, CLOSE = 0;
    
    public int state; //�ļ�״̬
    public int size; //�ļ���С
    
    Record rec; //����ļ��ļ�¼
    public int len; //����ļ���������С
    
    public void close() {
        state = CLOSE;
        len = 0;
    }
}