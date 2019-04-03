import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class JDropDownButton<T> extends JLayeredPane{
    private static final int arrowWidth=18;
    JButton button;
    JComboBox comboBox;
    public static void test(){
        JFrame frame=new JFrame();
        frame.add(new JDropDownButton(new JButton("Ica Banken"),new JComboBox(CustomerData.Bank.values())));
        frame.pack();
        frame.setVisible(true);
    }
    public JDropDownButton(){
        this(new JButton(),new JComboBox<T>());
    }
    public JDropDownButton(T[] choices){
        this(new JButton(choices[0].toString()),new JComboBox<T>(choices));
    }
    public JDropDownButton(JButton button,JComboBox comboBox){
        this.button=button;
        this.comboBox=comboBox;
        Dimension size=comboBox.getPreferredSize();
        setPreferredSize(size);
        setLayout(null);
        comboBox.setBounds(0,0,size.width,size.height);
        button.setBounds(0,0,size.width-arrowWidth,size.height);
        button.setMargin(new Insets(0,0,0,0));
        comboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                button.setText(comboBox.getSelectedItem().toString());
            }
        });
        add(button,1,0);
        add(comboBox,0,0);
    }
    public T getSelectedItem(){
        return (T)comboBox.getSelectedItem();
    }
    public void addActionListener(ActionListener al){
        button.addActionListener(al);
    }
}