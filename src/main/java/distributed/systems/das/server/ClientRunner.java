package distributed.systems.das.server;

import distributed.systems.das.exception.ActionFailedException;
import distributed.systems.das.server.Interfaces.IMessageReceivedHandler;
import distributed.systems.das.server.Services.Callback;
import distributed.systems.das.server.State.BattleField;
import distributed.systems.das.server.Units.Dragon;
import distributed.systems.das.server.Units.Player;
import distributed.systems.das.server.Units.Unit;
import distributed.systems.das.server.events.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Here we can call the remote objects through the implemented interfaces. Please look in the method body for more
 * explanation
 */
public class ClientRunner extends UnicastRemoteObject implements IMessageReceivedHandler {

    static final Logger Log = LoggerFactory.getLogger(ClientRunner.class);
	private static BattleField battleField;
	private volatile boolean alive = true;

    protected ClientRunner() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException, InterruptedException {
        long currentTime = System.currentTimeMillis();
        long end = currentTime + 10000000; // Initial ending time for testing with single player
        double botTime;

        String playerID = UUID.randomUUID().toString();

        // Create list with servers
        java.util.List<Map.Entry<String, Integer>> servers = new java.util.ArrayList<>();
        servers.add(new java.util.AbstractMap.SimpleEntry<>("localhost", 5001));

        Map.Entry<String, Integer> randomServer = servers.get(new Random().nextInt(servers.size()));

        /**
         * Set bot ID and total time before disconnecting bot from server
         */
        if ((args != null) && (args.length > 0)) {
            //for Bot (GTASimulator), uses 2 arguments: 1. playerID 2. runtime/lifespan
            playerID = args[0];
            botTime = Double.parseDouble(args[1]) * 1000; // From seconds to MS
            end = (long) (currentTime + botTime);
        }

//        Unit localPlayer = new Player(10, 10, playerID);


        Callback updateClient = new Callback();
        try {
            
            ClientRunner runner = new ClientRunner();
			Registry remoteRegistry = LocateRegistry.getRegistry ("localhost", 5001);
			System.out.println (Arrays.toString (remoteRegistry.list ()));
			String serverID = Arrays.stream (remoteRegistry.list ())
					.filter (s -> s.contains ("server"))
					.findFirst ()
					.orElse ("");
			IMessageReceivedHandler server = (IMessageReceivedHandler) remoteRegistry.lookup(serverID);
            remoteRegistry.bind(playerID, runner);

            Message m = new Message(0, System.currentTimeMillis(), playerID, Message.LOGIN);
            Message loginResp = server.onMessageReceived(m);
            Unit u = (Unit)loginResp.body.get("unit");
			Player p = (Player) u;
            System.out.println("playerID: "+playerID+" spawned at "+p.getX()+","+p.getY());
			
			battleField = (BattleField) loginResp.body.get ("battlefield");			

            /*for (int i=0; i<BattleField.MAP_WIDTH; i++) {
        		for (int j=0; j<BattleField.MAP_HEIGHT; j++) {

					Unit u2 = battleField.getUnit (i, j);
					if (!(u2 instanceof Dragon)) {
    					continue;
    				}					

    				System.out.println("Dragon "+u2.getUnitID()+" at ("+i+","+j+")!");

        		}
        	}*/
                        
			runner.startSimulation (server, p);

			//runner.startHeartbeat(server, playerID);

            /**
             * Start heartbeat thread
             */
            String finalPlayerID = playerID;
            new Thread(() -> {
                try {
                    runner.startHeartbeat(server, finalPlayerID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();


            /**
             * Getting updates from server
             *//*
            Callback callback = new Callback();
            while (true){
                Thread.sleep(200);

                if(callback.getUpdate() != null){
                    System.out.println(callback.getUpdate());
                    Message callbackMessage = callback.getUpdate();
                    switch (callbackMessage.type){
                        case Message.ATTACK:
                            System.out.println("Attacked, you have: " + callbackMessage.body.get("adjustedHitpoints"));
                    }

                }

            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    private boolean findAndHeal(IMessageReceivedHandler server, Player p, BattleField battlefield) throws Exception {
		//System.out.println ("try find and heal");

		int nx = p.getX();
    	int ny = p.getY();
    	
    	int tx = -1;
		int ty = -1;
		
		boolean found = false;

    	for (int i=-5; i<=5; i++) {
    		for (int j=-5; j<=5; j++) {
    			if (Math.abs(i) + Math.abs(j) > 5) {
    				continue;
    			}
    			
    			tx = nx + i;
    			ty = ny + j;

    			if (!battlefield.isLegalCoordinates(tx, ty)) {
    				continue;
    			}
    			
    			Unit u = battlefield.getUnit(tx, ty);
    			
    			if (!(u instanceof Player)) {
    				continue;
    			}
    			
    			Player tp = (Player)u;
    			if (tp.getHitPoints() <= tp.getMaxHitPoints() / 2) {
    				Message m = new Message(1, System.currentTimeMillis(), p.getUnitID(), Message.HEAL);
    				m.body.put("x", tx);
    				m.body.put("y", ty);
    				m.body.put("healed", p.getAttackPoints());
    					
    				Message resp_m = server.onMessageReceived(m);
    				battlefield.heal(resp_m);
    				
    				found = true;
        			break;
    			}
    		}
    		
    		if (found) {
    			break;
    		}
    	}

		System.out.println ("Player "+p.getUnitID()+" tries to find and heal:" + found);
		if (found) p.logStatus ();
		return found;
    }
    
    private boolean directlyAttack(IMessageReceivedHandler server, Player p, BattleField battlefield) throws Exception {
		//System.out.println ("try directly attack");
		int nx = p.getX();
    	int ny = p.getY();
    	
    	int tx = -1;
		int ty = -1;
		
		boolean found = false;

    	for (int i=-2; i<=2; i++) {
    		for (int j=-2; j<=2; j++) {
    			if (Math.abs(i) + Math.abs(j) > 2) {
    				continue;
    			}
    			
    			tx = nx + i;
    			ty = ny + j;

    			if (!battlefield.isLegalCoordinates(tx, ty)) {
    				continue;
    			}

    			Unit u = battlefield.getUnit(tx, ty);
    			
    			if (!(u instanceof Dragon)) {
    				continue;
    			}
    			
    			Dragon td = (Dragon)u;

    			Message m = new Message(1, System.currentTimeMillis(), p.getUnitID(), Message.ATTACK);
    			m.body.put("x", tx);
    			m.body.put("y", ty);
    			m.body.put("damage", p.getAttackPoints());
    					
    			Message resp_m = server.onMessageReceived(m);

    			found = true;
    			break;
    		}
    		
    		if (found) {
    			break;
    		}
    	}

		System.out.println ("Player "+p.getUnitID()+" tries to attack dragon in range:" + found);
		if (found) p.logStatus ();
		return found;
    }

    private boolean findNearestDragonAndMove(IMessageReceivedHandler server, Player p, BattleField battlefield) throws Exception {
    	int nx = p.getX();
    	int ny = p.getY();
		System.out.println ("Try to find a dragon and move. Current coordinates: " + nx + "," + ny);

		int nd = 0x7fffffff;
    	int tx=-1, ty=-1;
		
    	for (int i=0; i<BattleField.MAP_WIDTH; i++) {
    		for (int j=0; j<BattleField.MAP_HEIGHT; j++) {
	
				Unit u = battlefield.getUnit(i, j);
				if (!(u instanceof Dragon)) {
					continue;
				}

				int distance = (Math.abs(nx-i) + Math.abs(ny-j));
				if (distance < nd) {
					nd = distance;
					tx = i;
					ty = j;
				}

    		}
    	}


		if (tx == -1 && ty == -1) {
			return false;
		}

		int mx=nx, my=ny;
    	if (tx != nx) {
    		mx += (tx-nx)/Math.abs(tx-nx);
    	} else {
    		my += (ty-ny)/Math.abs(ty-ny);
    	}
    	Message m = new Message(1, System.currentTimeMillis(), p.getUnitID(), Message.MOVE);
		m.body.put("x", mx);
		m.body.put("y", my);
		Message resp_m = server.onMessageReceived(m);
		if((Boolean) resp_m.body.get("move")){
			System.out.println ("Player "+p.getUnitID()+" tries to move from "+nx+","+ny+" to " + mx + "," + my +" to attack dragon at "+tx +","+ ty+" (distance "+nd+"):"+true);
			p.logStatus ();
			return true;
		}

		return false;


	}


	private void startSimulation (IMessageReceivedHandler server, Player p) throws ActionFailedException {
		Random random = new Random();
    	
    	try {
			while (alive) {
				boolean action = findAndHeal (server, p, battleField)
						|| directlyAttack (server, p, battleField)
						|| findNearestDragonAndMove (server, p, battleField);

				if (!action) {
	    			throw new ActionFailedException();
	    		}
	    		
	    		Thread.sleep(1000);
	    	}
			System.out.println("Player "+p.getUnitID()+" is dead!");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    			
    @Override
    public Message onMessageReceived(Message message) {
		Log.debug (message.body.toString ());
		if (message.body.containsKey ("isDead")) {
			alive = false;
			return null;
		}
		switch (message.type) {
			case Message.ATTACK:
				battleField.attack (message);
				break;
			case Message.HEAL:
				battleField.heal (message);
				break;
			case Message.MOVE:
				battleField.move (message);
				break;
		}
		return null;
    }

    private void startHeartbeat(IMessageReceivedHandler server, String playerID) throws Exception {
        Message m = new Message(2, System.currentTimeMillis(), playerID, Message.HEARTBEAT);
        while (true) {
            server.onMessageReceived(m);
            Thread.sleep(1000);
        }
    }
}
