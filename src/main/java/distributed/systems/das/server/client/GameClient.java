package distributed.systems.das.server.client;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

// import distributed.systems.das.units.Player;

public class GameClient {
	
    // public Player player;	
	
    public JPanel panel;
    public JTextField username;
    public JButton loginButton;
    private JButton upButton;
    private JButton leftButton;
    private JButton rightButton;
    private JButton downButton;
    public JTextArea console;
    private JPanel arenaPanel;
	
	public GameClient() {
		System.out.println("constructor called");
		// System.getproperty("java.classpath");
		//server = findServer();
        // player = null;
		//arena = new BattleField();
/*
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(player == null) {
                    //if(server == null) server = findServer();
                    login(username.getText(),"");
                    loginButton.setText("Logout");
                } else { // LOGOUT
                    logout();
                    loginButton.setText("Login");
                }

            }
        });

        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    Unit source = player.getUnit();
                    Unit target = source.clone();

                    target.setCoord(target.getX(), target.getY() + 1);
                    ArrayList<Unit> units = new ArrayList<Unit>();
                    units.add(0, source);
                    units.add(1, target);

                    // server.sendEvent(UnitEvent.UNIT_MOVE, units);

                } catch (RemoteException e1) {
//                    e1.printStackTrace();
                    // server = findServer();
                }
            }
        });

        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Unit source = player.getUnit();
                    Unit target = source.clone();

                    target.setCoord(target.getX(), target.getY() - 1);
                    ArrayList<Unit> units = new ArrayList<Unit>();
                    units.add(0, source);
                    units.add(1, target);

                    // server.sendEvent(UnitEvent.UNIT_MOVE, units);

                } catch (RemoteException e1) {
//                    e1.printStackTrace();
                    // server = findServer();
                }
            }
        });

        rightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Unit source = player.getUnit();
                    Unit target = source.clone();

                    target.setCoord(target.getX() + 1, target.getY());
                    ArrayList<Unit> units = new ArrayList<Unit>();
                    units.add(0, source);
                    units.add(1, target);

                    // server.sendEvent(UnitEvent.UNIT_MOVE, units);
                } catch (RemoteException e1) {
//                    e1.printStackTrace();
                    // server = findServer();
                }
            }
        });

        leftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Unit source = player.getUnit();
                    Unit target = source.clone();

                    target.setCoord(target.getX() - 1, target.getY());
                    ArrayList<Unit> units = new ArrayList<Unit>();
                    units.add(0, source);
                    units.add(1, target);

                    // server.sendEvent(UnitEvent.UNIT_MOVE, units);
                } catch (RemoteException e1) {
//                    e1.printStackTrace();
                    // server = findServer();
                }
            }
        });


        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                consoleLog("[System] Logout and disconnect from server...");
                //logout();
                consoleLog("Bye!");
            }
        });*/
    }

	public void consoleLog(String message) {
        console.append(message);
        console.append("\n");
        console.setCaretPosition(console.getDocument().getLength());
    }
	
	private void createUIComponents() {
        arenaPanel = new JPanel(new GridLayout(0, 25));
        arenaPanel.setBorder(new LineBorder(Color.BLACK));

        for(int j=0;j<25;j++) {
            for(int i=0;i<25;i++) {
                JLabel cellLabel =  new JLabel(" ");
                cellLabel.setHorizontalTextPosition(JLabel.CENTER);
                cellLabel.setSize(10,10);
                cellLabel.setOpaque(true);
                cellLabel.setBorder(new LineBorder(Color.GRAY));
                arenaPanel.add(cellLabel);
            }
        }
    }
	
	public static void main(String[] args){
		System.out.println("starts!");
		
		BattleFieldViewer viewer = new BattleFieldViewer();
		
        /*GameClient ui = new GameClient();
        JFrame frame = new JFrame("Dragon vs Players");
        // frame.setContentPane(ui.panel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(1250,700);
        frame.setVisible(true);*/
    }
}
