import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class JDropDownButton<T> extends JLayeredPane{// a button with a dropdown menu to change it's action
    private JButton button;
    private JComboBox comboBox;
    public JDropDownButton(T[] choices){
        this(new JButton(choices[0].toString()),new JComboBox<T>(choices));
    }
    public JDropDownButton(JButton button,JComboBox comboBox){
        this.button=button;
        this.comboBox=comboBox;
        button.setText(comboBox.getSelectedItem().toString());
        Dimension size=comboBox.getPreferredSize();
        int arrowWidth=comboBox.getComponents()[0].getMinimumSize().width-1;// the component at index 0 for a combobox is the arrow
        setPreferredSize(size);
        setLayout(null);
        comboBox.setBounds(0,0,size.width,size.height);// size the component after the combobox
        button.setBounds(0,0,size.width-arrowWidth,size.height);// put the button over the text part of the combobox
        button.setMargin(new Insets(0,0,0,0));// removes the text shortening from the button
        comboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                button.setText(comboBox.getSelectedItem().toString());// update button when new item is chosen
            }
        });
        add(button,1,0);// put the button on top
        add(comboBox,0,0);
    }
    public T getSelectedItem(){// gets the object currently on the button
        return (T)comboBox.getSelectedItem();
    }
    public void addActionListener(ActionListener al){// adds an external ActionListener to the button
        button.addActionListener(al);
    }
}