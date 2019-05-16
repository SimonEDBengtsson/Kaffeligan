import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
public class GUI extends JFrame{
    final static String configPath="resources/kaffeligan.config";
    static int imageWidth=1920,imageHeight=1080;
    JLabel labelIn=new JLabel("Input path (csv):"),labelOut=new JLabel("Output path (png,jpg or gif):"),format=new JLabel("Which bank? ");
    JTextField in=new JTextField(40),out=new JTextField(40);
    JButton browseIn=new JButton("browse..."),browseOut=new JButton("browse..."),apply=new JButton("Create");
    JComboBox<FinancialData.Bank> bankChooser=new JComboBox<FinancialData.Bank>(FinancialData.Bank.values());
    JDropDownButton<OutputType> create=new JDropDownButton<OutputType>(OutputType.values());
    public enum OutputType{// the types of file this program can create
        KAFFELIGAN("Kaffeligan"),CIVET("Civet"),BALANCE_GRAPH("Saldograf"),TRANSACTION_GRAPH("Transaktionsgraf"),
        TRANSACTION_SPECTRUM("Transaktionsspektrum");
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
            FinancialData.dateIndex=Integer.parseInt(in.readLine());
            FinancialData.nameIndex=Integer.parseInt(in.readLine());
            FinancialData.paidIndex=Integer.parseInt(in.readLine());
            FinancialData.balanceIndex=Integer.parseInt(in.readLine());
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
                String inPath=in.getText(),outPath=out.getText();
                FinancialData.Bank bank=(FinancialData.Bank)bankChooser.getSelectedItem();
                try{
                    FinancialData fd;// reads the input file
                    switch(bank){
                        case ICA:   fd=new ICAData(inPath);
                                    break;
                        default:    fd=null;
                                    worked=false;
                                    out.setText("Bank not supported");
                    }
                    if(fd!=null){
                        createFile(outPath,fd);
                    }
                }
                catch(Throwable t){
                    out.setText(t.toString());
                    t.printStackTrace();
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
    public void createFile(String outPath,FinancialData fd)throws Exception{
        switch(create.getSelectedItem()){
            case KAFFELIGAN:            Kaffeligan.create(outPath,fd,this);
                                        break;
            case CIVET:                 Civet.writeGIF(outPath,fd);
                                        break;
            case BALANCE_GRAPH:         BalanceGraph.writeGraph(outPath,fd);
                                        break;
            case TRANSACTION_GRAPH:     TransactionGraph.writeGraph(outPath,fd,this);
                                        break;
            case TRANSACTION_SPECTRUM:  TransactionSpectrum.writeGraph(outPath,fd,this);
                                        break;
        }
    }
    public String requestDataFromUser(Object request,String title,String[] choices,String firstChoice){
        Icon icon=null;
        return (String)JOptionPane.showInputDialog(this,request,title,JOptionPane.QUESTION_MESSAGE,icon,choices,firstChoice);
    }
}