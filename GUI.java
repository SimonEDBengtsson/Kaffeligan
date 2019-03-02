import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
public class GUI extends JFrame{
    final static String configPath="dependencies/kaffeligan.config";
    JLabel labelIn=new JLabel("Input path (csv):"),labelOut=new JLabel("Output path (png,jpg or gif):");
    JLabel versionLabel=new JLabel("\"Kaffeligan \"+");
    JTextField in=new JTextField(40),out=new JTextField(40),version=new JTextField(5);
    JButton browseIn=new JButton("browse..."),browseOut=new JButton("browse..."),apply=new JButton("Apply");
    public static void main(String[] args){
        new GUI();
    }
    public void config(){// the config file can be used to change global variables, \n separated values
        try{
            BufferedReader in=new BufferedReader(new FileReader(configPath));
            Civet.dateIndex=Integer.parseInt(in.readLine());
            Kaffeligan.nameIndex=Integer.parseInt(in.readLine());
            Kaffeligan.paidIndex=Integer.parseInt(in.readLine());
            Civet.balanceIndex=Integer.parseInt(in.readLine());
            Kaffeligan.backgroundImagePath=in.readLine();
            Kaffeligan.logoImagePath=in.readLine();
            Kaffeligan.goldImagePath=in.readLine();
            Kaffeligan.silverImagePath=in.readLine();
            Kaffeligan.bronzeImagePath=in.readLine();
            Civet.civetPath=in.readLine();
            Civet.tearPath=in.readLine();
            Civet.sparklePath=in.readLine();
            Civet.smilePath=in.readLine();
            Civet.fps=Integer.parseInt(in.readLine());
            Civet.duration=Integer.parseInt(in.readLine());
        }
        catch(java.io.IOException t){
            in.setText("Config file not found, standard settings will be used.");
        }
    }
    public GUI(){
        super("ZKK");
        browseIn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                File temp=selectCSV();
                if(temp!=null){
                    in.setText(temp.toString());
                }
            }
        });
        browseOut.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                File temp=selectOutput();
                if(temp!=null){
                    out.setText(temp.toString());
                }
            }
        });
        apply.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                boolean worked=true;
                Kaffeligan.lp=version.getText();
                try{
                    if(out.getText().matches(".*\\.png$")){
                        Kaffeligan.writePNG(out.getText(),Kaffeligan.read(in.getText()));
                    }
                    else if(out.getText().matches(".*\\.jpg$")){
                        Kaffeligan.writeJPG(out.getText(),Kaffeligan.read(in.getText()));
                    }
                    else if(out.getText().matches(".*\\.gif$")){
                        Civet.writeGIF(out.getText(),in.getText());
                    }
                    else{
                        out.setText("Invalid file type");
                        worked=false;
                    }
                }
                catch(IOException x){
                    out.setText("IOException");
                    worked=false;
                }
                if(worked){
                    setVisible(false);
                    dispose();
                }
            }
        });
        setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
        JPanel top=new JPanel();
        GroupLayout layout=new GroupLayout(top);
        top.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
           layout.createSequentialGroup()
              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                   .addComponent(labelIn)
                   .addComponent(labelOut))
              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                   .addComponent(in)
                   .addComponent(out))
              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                   .addComponent(browseIn)
                   .addComponent(browseOut))
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelIn)
                .addComponent(in)
                .addComponent(browseIn)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelOut)
                .addComponent(out)
                .addComponent(browseOut)
            )
        );
        JPanel middle=new JPanel();
        GroupLayout lo=new GroupLayout(middle);
        lo.setHorizontalGroup(
            lo.createSequentialGroup()
                .addComponent(versionLabel)
                .addComponent(version)
        );
        lo.setVerticalGroup(
            lo.createSequentialGroup()
            .addGroup(lo.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(versionLabel)
                .addComponent(version))
        );
        add(top);
        add(middle);
        add(apply);
        pack();
        config();
        setVisible(true);
    }
    public File selectCSV(){
        JFileChooser chooser=new JFileChooser();
        FileNameExtensionFilter filter=new FileNameExtensionFilter("Supported Files","csv");
        chooser.setFileFilter(filter);
        int result=chooser.showOpenDialog(this);
        if(result==JFileChooser.APPROVE_OPTION){
            return chooser.getSelectedFile();
        }
        return null;
    }
    public File selectOutput(){
        JFileChooser chooser=new JFileChooser();
        FileNameExtensionFilter filter=new FileNameExtensionFilter("Supported Files","png","jpg","gif");
        chooser.setFileFilter(filter);
        int result=chooser.showSaveDialog(this);
        if(result==JFileChooser.APPROVE_OPTION){
            return chooser.getSelectedFile();
        }
        return null;
    }
}
