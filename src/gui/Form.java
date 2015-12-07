package gui;

import io.*;
import core.*;
import common.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.*;

import java.io.*;
import java.lang.Thread.State;

public class Form extends JFrame implements ActionListener {
    Graph graph = new Graph();
    Text text = new Text(graph);
    Screen scr = new Screen(text);
    Controller ctrl = new Controller(scr, this);
    Basic b = new Basic(ctrl, ctrl, this);

    static final String title = "GVBASICģ����";
    public Form() {
        super(title);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (exec != null && exec.isAlive()) {
                    exec.interrupt();
                    try {
                        exec.join();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                if (imd != null)
                    imd.dispose();
                dispose();
                System.exit(0);
            }
        });
        setLayout(new GridBagLayout());
        
        GridBagConstraints cs = new GridBagConstraints();
        cs.insets = new Insets(3, 2, 3, 2);
        cs.anchor = GridBagConstraints.BASELINE_LEADING;
        cs.weightx = .3;
        //��
        btnLoad.addActionListener(this);
        btnLoad.setFocusable(false); //����Ϊ�޷���ȡ���㣬��ֹ���س�ʱ������ť
        add(btnLoad, cs);
        //����
        cs.gridy = 0;
        cs.gridx = 1;
        btnRun.addActionListener(this);
        btnRun.setFocusable(false);
        add(btnRun, cs);
        //��ͼ
        cs.gridy = 0;
        cs.gridx = 2;
        btnCap.addActionListener(this);
        btnCap.setFocusable(false);
        add(btnCap, cs);
        //��Ļ
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 3;
        add(scr, cs);
        //��Ϣ��
        cs.gridy = 2;
        infoLabel.setPreferredSize(new Dimension(320, 20));
        infoLabel.setBorder(new EtchedBorder());
        add(infoLabel, cs);
        //��ɽ����
        cs.gridy = 3;
        btnHack.addActionListener(this);
        btnHack.setFocusable(false);
        add(btnHack, cs);
        //ͼ�����
        cs.gridx = 2;
        cs.gridy = 3;
        btnImage.addActionListener(this);
        btnImage.setFocusable(false);
        add(btnImage, cs);
        
        //�����б�
        vartable = new JTable(new VTableModel());
        jvt = new JScrollPane(vartable);
        jvt.setPreferredSize(new Dimension(320, 180));
        vartable.setShowVerticalLines(false);
        vartable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vartable.setToolTipText("˫����Ŀ�޸ı���");
        vartable.addMouseListener(new VarListener());
        
        pack();
        Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(new Point((sz.width - getWidth()) / 2, (int) ((sz.height - getHeight()) * .309)));
        setVisible(true);
        
        jf.setCurrentDirectory(new File("bas"));
        jf.setFileFilter(new FileNameExtensionFilter("�ı��ļ�(*.txt)", "txt"));
        
        mem = ctrl.getRAM();
        
        //����ȫ�ּ����¼�
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                if (!isFocused())
                    return;
                KeyEvent e = (KeyEvent) event;
                int k = e.getKeyCode();
                if (k == 192) //`��
                    k = 96;
//                else if (k >= 112 && k <= 123 || k >= 33 && k <= 40) //F��ݼ�
//                    k = 0;
                switch (e.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (k > 0 && k < 256) {
                        key = k;
                        tmpk = Utilities.convertKeyCode(k);
                        mem[199] = (byte) (tmpk | 0x80);
                        tmpk = Utilities.mapWQXKey(tmpk);
                        if (tmpk != 0) {
                            mem[191 + (tmpk >>> 8)] &= ~tmpk & 0xff;
                        }
                        keyList[k] = true;
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (k > 0 && k < 256) {
                        key = 0;
                        tmpk = Utilities.mapWQXKey(Utilities.convertKeyCode(k));
                        if (tmpk != 0) {
                            mem[191 + (tmpk >>> 8)] |= tmpk & 0xff;
                        }
                        keyList[k] = false;
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);
    }
    
    public String getDefaultTitle() {
        return title;
    }
    
    TButton btnLoad = new TButton("��"), btnRun = new TButton("����"), btnHack = new TButton("����"),
            btnCap = new TButton("��ͼ"), btnImage = new TButton("ͼ�����");
    
    Thread exec;
    
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case "��":
            //���ļ�,���b.load����ֵ
            error("");
            loadFile();
            btnRun.setText("����");
            break;
        case "ֹͣ":
            exec.interrupt();
            try {
                exec.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            break;
        case "����": //��������ͣ��������basic���б�־������������룬������
            if (exec == null || exec.getState() == State.TERMINATED)
                return;
            if (!exec.isAlive()) {
                exec.start();
                btnLoad.setText("ֹͣ");
            } else
                b.cont();
            btnRun.setText("��ͣ");
            error("");
            break;
        case "��ͣ":
            b.pause();
            btnRun.setText("����");
            break;
        case "����":
            if (vtShow) {
                remove(jvt);
                scr.requestFocus(); //����ת�Ƶ���Ļ�������޷���ⰴ��
            } else {
                GridBagConstraints cs = new GridBagConstraints();
                cs.gridy = 4;
                cs.gridwidth = 3;
                add(jvt, cs);
            }
            vtShow = !vtShow;
            pack();
            break;
        case "��ͼ":
            screenshot();
            break;
        case "ͼ�����":
            if (imd == null)
                imd = new ImageDialog(this);
            break;
        }
    }
    
    JFileChooser jf = new JFileChooser();
    
    void loadFile() {
        int r = jf.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File f = jf.getSelectedFile();
            if (!f.exists()) {
                error("File not exist!");
                return;
            }
            InputStream in;
            try {
                in = new BufferedInputStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                error("File read failed!");
                return;
            }
            if (!b.load(in)) {
                error("File load failed!");
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //threadֻ��startһ�Σ����ÿ���½�һ��
            exec = new Executor();
        }
    }
    
    /**
     * ��Ϣ��ʾ��
     */
    JLabel infoLabel = new JLabel();
    
    /**
     * ��Ϣ����ʾ������Ϣ
     * @param s ��Ϣ
     */
    void error(String s) {
        infoLabel.setForeground(Color.red);
        infoLabel.setText(s);
        infoLabel.repaint();
    }
    
    /**
     * ͼ�������
     */
    public ImageDialog imd;
    
    /**
     * �����б��޸ı���Map����Ҫrevalidate�������б�
     */
    public JTable vartable;
    JScrollPane jvt;
    boolean vtShow;
    
    class VTableModel extends AbstractTableModel {
        String[] colName =  {"������", "��������", "ֵ"};
        
        public String getColumnName(int column) {
            return colName[column];
        }

        @Override
        public int getRowCount() {
            synchronized (b.vars) {
                return b.vars.size();
            }
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            synchronized (b.vars) {
                Id id = b.vars.keySet().toArray(new Id[0])[rowIndex];
                
                switch (columnIndex) {
                case 0:
                    return id.id.toUpperCase();
                case 1:
                    switch (id.type) {
                    case Id.REAL:
                        return "ʵ��";
                    case Id.INTEGER:
                        return "����";
                    case Id.STRING:
                        return "�ַ���";
                    case Id.ARRAY:
                        Object v = ((Array<?>) b.vars.get(id)).value;
                        if (v instanceof Double[])
                            return "ʵ������";
                        else if (v instanceof Integer[])
                            return "��������";
                        else if (v instanceof S[])
                            return "�ַ�������";
                        else
                            return "δ֪��������";
                    default:
                        return "δ֪����";
                    }
                case 2:
                    switch (id.type) {
                    case Id.ARRAY:
                        return "...";
                    case Id.REAL:
                        return Utilities.realToString((Double) b.vars.get(id));
                    case Id.INTEGER: case Id.STRING:
                        return b.vars.get(id);
                    default:
                        return "???";
                    }
                default:
                    return null;
                }
            }
        }
    }
    
    class VarListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                Id id = b.vars.keySet().toArray(new Id[0])[vartable.getSelectedRow()];
                if (id.type == Id.ARRAY) {
                    new ArrayHacker(id.id.toUpperCase(), (Array<?>) b.vars.get(id));
                    return;
                }
                synchronized (b.vars) {
                    String s = JOptionPane.showInputDialog(Form.this, "��������ֵ",
                            vartable.getModel().getValueAt(vartable.getSelectedRow(), 2));
                    if (s == null)
                        return;
                    switch (id.type) {
                    case Id.INTEGER:
                        try {
                            b.vars.put(id, Integer.parseInt(s));
                        } catch (NumberFormatException e1) {
                            error("Modify variable failed!");
                        }
                        break;
                    case Id.REAL:
                        try {
                            b.vars.put(id, Double.parseDouble(s));
                        } catch (NumberFormatException e1) {
                            error("Modify variable failed!");
                        }
                        break;
                    case Id.STRING:
                        b.vars.put(id, new S(s));
                    }
                }
            }
        }
        class ArrayHacker extends JDialog implements ActionListener, ChangeListener {
            JSpinner[] sp;
            Array<?> arry;
            JTextField tf = new JTextField(6);
            TButton b = new TButton("ȷ��");
            
            public ArrayHacker(String name, Array<?> arr) {
                super(Form.this, "", true);
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                setLayout(new FlowLayout());
                
                arry = arr;
                int t = arr.bound.length;
                sp = new JSpinner[t];
                StringBuffer sb = new StringBuffer(name);
                sb.append("(");
                for (int i = 0; i < t; i++) {
                    add(sp[i] = new JSpinner(new SpinnerNumberModel(0, 0, (int) arr.bound[i], 1)));
                    sp[i].addChangeListener(this);
                    sb.append(arr.bound[i]);
                    if (i < t - 1)
                        sb.append(", ");
                }
                sb.append(")");
                
                setTitle(sb.toString());
                tf.setText(arr.value[0].toString());
                add(tf);
                b.addActionListener(this);
                add(b);
                pack();
                setLocationRelativeTo(Form.this);
                setVisible(true);
            }
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                int t = 0, z = arry.bound.length;
                for (int i = 0; i < z; i++)
                    t += arry.base[i] * (Integer) sp[i].getValue();
                synchronized (arry) {
                    Object v = arry.value;
                    if (v instanceof Double[]) {
                        try {
                            ((Array<Double>) arry).value[t] = Double.parseDouble(tf.getText());
                        } catch (NumberFormatException e1) {
                            e1.printStackTrace();
                        }
                    } else if (v instanceof Integer[]) {
                        try {
                            ((Array<Integer>) arry).value[t] = Integer.parseInt(tf.getText());
                        } catch (NumberFormatException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        ((Array<S>) arry).value[t] = new S(tf.getText());
                    }
                }
                dispose();
            }
            public void stateChanged(ChangeEvent e) {
                int t = 0, z = arry.bound.length;
                for (int i = 0; i < z; i++)
                    t += arry.base[i] * (Integer) sp[i].getValue();
                tf.setText(arry.value[t].toString());
            }
        }
    }
    
    static int shot_id = 1;
    static byte[] BMPHeader = {
        0x42, 0x4D, 0x7E, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3E, 0x00, 0x00, 0x00, 0x28, 0x00, 
        0x00, 0x00, (byte) 0xA0, 0x00, 0x00, 0x00, 0x50, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 
        0x00, 0x00, 0x40, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    void screenshot() {
        try {
            OutputStream o = new BufferedOutputStream(new FileOutputStream("Screenshot_" + shot_id + ".bmp"));
            shot_id++;
            o.write(BMPHeader);
            byte[] b = Utilities.toByteData(graph.copy(), 160, 80);
            for (int j = 79; j >= 0; j--) {
                for (int i = 0; i < 20; i++) {
                    o.write(b[i + j * 20]);
                }
            }
            o.close();
        } catch (Exception e) {
            error("Capture screen failed!");
            e.printStackTrace();
        }
    }
    
    public Screen getScreen() {
        return scr;
    }
    
    byte[] mem;
    int key, tmpk;
    boolean[] keyList = new boolean[256];
    
    /**
     * ��ȡpc��λ��ע��<b>��ĸ�Ǵ�д</b>���ı�ģʽ�µȴ�ʱ�����˸
     * @return PC��ֵ
     */
    public int inkey() throws InterruptedException {
        return inkey(true);
    }
    
    /**
     * �Ƿ��м�����
     */
    public boolean keyPressed() {
        return key != 0;
    }
    
    /**
     * ��ȡpc��λ��ע��<b>��ĸ�Ǵ�д</b>
     * @param ctrlFlash �Ƿ���Ƶȴ�ʱ������˸(�ı�ģʽ�µȴ�����ʱ������˸)
     * @return pc��λ
     */
    public int inkey(boolean ctrlFlash) throws InterruptedException {
        if (ctrlFlash)
            scr.flash();
        while (key == 0) {
            Thread.sleep(50);
        }
        mem[199] &= 0x7f;
        int k = key;
        key = 0;
        if (ctrlFlash)
            scr.stopFlash();
        return k;
    }
    
    /**
     * �ж�ĳ���Ƿ񱻰���
     * @param rawKey pc��ֵ
     */
    public boolean checkKey(int rawKey) {
        return keyList[rawKey];
    }
    
    class Executor extends Thread {
        public void run() {
            error(b.run()); //��Ҫ�����ص���Ϣ
            scr.stopFlash();
            scr.repaint();
            btnRun.setText("����");
            btnLoad.setText("��");
        }
    }
    
    public static void main(String[] args) throws Exception {
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new Form();
    }
}

class ImageDialog extends JDialog {
    static BufferedImage bi;
    static Color sel = new Color(0xff, 0, 0, 0xd0);
    static {
        try {
            bi = ImageIO.read(new File("res/image.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    int lx = -1, ly;
    byte[] gb = new byte[2];
    JPanel jp = new JPanel() {
        {
            setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX() >>> 4, y = e.getY() >>> 4, t = y * 20 + x;
                    if (t < 527) {
                        lx = x << 4;
                        ly = y << 4;
                        gb[0] = (byte) (0xf8 + t / 94);
                        gb[1] = (byte) (0xa1 + t % 94);
                        //System.out.println(t+" " +Integer.toHexString(((gb[0] & 0xff) << 8) + (gb[1] & 0xff)));
                        jp2.repaint();
                        repaint();
                    }
                    if (e.getClickCount() == 2) {
                        Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(new String(gb)), null);
                    }
                }
            });
            setToolTipText("˫����ͼ��GB�븴�Ƶ�������");
        }
        public void paintComponent(Graphics g) {
            g.drawImage(bi, 0, 0, null);
            if (lx >= 0) {
                g.setColor(sel);
                g.drawRect(lx, ly, 15, 15);
            }
        }
    }, jp2 = new JPanel() {
        {
            setPreferredSize(new Dimension(48, 60));
        }
        public void paintComponent(Graphics g) {
            g.clearRect(0, 0, getWidth(), getHeight());
            if (lx >= 0) {
                g.drawImage(bi.getSubimage(lx, ly, 16, 16), 0, 0, 48, 48, null);
                g.setFont(new Font("Consolas", Font.BOLD, 16));
                g.drawString(Integer.toHexString(((gb[0] & 0xff) << 8) + (gb[1] & 0xff)).toUpperCase(), 6, 59);
            }
        }
    };
    
    public ImageDialog(JFrame j) {
        super(j, "ͼ�������");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ((Form) getOwner()).imd = null;
                dispose();
            }
        });
        
        setLayout(new FlowLayout());
        
        add(jp);
        add(jp2);
        
        pack();
        setLocationRelativeTo(j);
        setVisible(true);
    }
}