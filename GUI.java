import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
public class GUI extends JFrame{
    final static String configPath="resources/kaffeligan.config";
    JLabel labelIn=new JLabel("Input path (csv):"),labelOut=new JLabel("Output path (png,jpg or gif):"),format=new JLabel("Which format? ");
    JLabel versionLabel=new JLabel("\"Kaffeligan \"+");
    JTextField in=new JTextField(40),out=new JTextField(40),version=new JTextField(5);
    JButton browseIn=new JButton("browse..."),browseOut=new JButton("browse..."),apply=new JButton("Create");
    JComboBox<CustomerData.Bank> bankChooser=new JComboBox<CustomerData.Bank>(CustomerData.Bank.values());
    JDropDownButton<OutputType> create=new JDropDownButton<OutputType>(OutputType.values());
    public enum OutputType{// the types of file this program can create
        KAFFELIGAN("Kaffeligan"),CIVET("Civet"),BALANCE_GRAPH("Saldo Graf");
        String name;
        private OutputType(String name){
            this.name=name;
        }
        @Override
        public String toString(){
            return name;
        }
    }
    public static void main(String[] args){
        new GUI();
    }
    public static InputStream load(String path){// for reading in resources in jar file
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
    public void config(){// the config file can be used to change global variables, \n separated values
        try{
            BufferedReader in=new BufferedReader(new InputStreamReader(load(configPath)));
            CustomerData.dateIndex=Integer.parseInt(in.readLine());
            CustomerData.nameIndex=Integer.parseInt(in.readLine());
            CustomerData.paidIndex=Integer.parseInt(in.readLine());
            CustomerData.balanceIndex=Integer.parseInt(in.readLine());
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
        catch(Throwable t){
            in.setText("Config file not found, standard settings will be used.");
        }
    }
    public GUI(){
        super("ZKK");
        try{
            setIconImage(javax.imageio.ImageIO.read(new File(Kaffeligan.logoImagePath)));// sets window icon
        }
        catch(IOException x){}
        browseIn.addActionListener(new ActionListener(){// open filebrowser for input field
            public void actionPerformed(ActionEvent e){
                File temp=selectInput();
                if(temp!=null){
                    in.setText(temp.toString());
                }
            }
        });
        browseOut.addActionListener(new ActionListener(){// open filebrowser for output field
            public void actionPerformed(ActionEvent e){
                File temp=selectOutput();
                if(temp!=null){
                    out.setText(temp.toString());
                }
            }
        });
        create.addActionListener(new ActionListener(){// try to create the file
            public void actionPerformed(ActionEvent e){
                boolean worked=true;
                Kaffeligan.lp=version.getText();// static value for no reason in particular
                String inPath=in.getText(),outPath=out.getText();
                CustomerData.Bank bank=(CustomerData.Bank)bankChooser.getSelectedItem();
                try{
                    CustomerData cd=new CustomerData(inPath,bank);// reads the input file
                    switch(create.getSelectedItem()){
                        case KAFFELIGAN:    Kaffeligan.create(outPath,cd);
                                            break;
                        case CIVET:         Civet.writeGIF(outPath,cd);
                                            break;
                        case BALANCE_GRAPH: BalanceGraph.writeGraph(outPath,cd);
                                            break;
                    }
                }
                catch(Throwable t){
                    out.setText(t.toString());
                    worked=false;
                }
                if(worked){
                    try{// open the folder where the file was made
                        Desktop.getDesktop().browse(new File(out.getText().replaceAll("/[^/]*?$","")).toURI());// won't work on windows
                    }catch(Exception x){}
                }
            }
        });
        setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
        JPanel top=new JPanel();// input field, bank dropdown menu, and output field in three rows
        GroupLayout layout=new GroupLayout(top);
        top.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
           layout.createSequentialGroup()
              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                   .addComponent(labelIn)
                   .addComponent(labelOut)
                   .addComponent(format))
              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                   .addComponent(in)
                   .addComponent(out)
                   .addComponent(bankChooser))
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
                .addComponent(format)
                .addComponent(bankChooser)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelOut)
                .addComponent(out)
                .addComponent(browseOut)
            )
        );
        JPanel middle=new JPanel();// for writing text to "Kaffeligan" image in a row
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
        JPanel bottom=new JPanel();// apply button and dropdown menu for choosing output type
        GroupLayout bottomLayout=new GroupLayout(bottom);
        bottomLayout.setHorizontalGroup(
            bottomLayout.createSequentialGroup()
                .addComponent(create)
        );
        bottomLayout.setVerticalGroup(
            bottomLayout.createSequentialGroup()
                .addComponent(create)
        );
        add(top);
        add(middle);
        add(bottom);
        pack();
        config();
        setVisible(true);
    }
    public File selectInput(){// file chooser for input file, looks for csv
        JFileChooser chooser=new JFileChooser();
        FileNameExtensionFilter filter=new FileNameExtensionFilter("Supported Files","csv");
        chooser.setFileFilter(filter);
        int result=chooser.showOpenDialog(this);
        if(result==JFileChooser.APPROVE_OPTION){
            return chooser.getSelectedFile();
        }
        return null;
    }
    public File selectOutput(){// file chooser for output file, looks for png, jpg, and gif
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
