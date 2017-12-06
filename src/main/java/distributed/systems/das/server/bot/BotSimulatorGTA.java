package distributed.systems.das.server.bot;

import distributed.systems.das.server.ClientRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BotSimulatorGTA {

	private static List<Bot> bots;

	public static void main(String[] args) {
		String path = "src/main/java/distributed/systems/das/server/bot/SC2_Edge_Detailed";
		System.out.println("Opening GTAv06 file at "+path);
		bots = readGTAv06File(path);
		
		//sort based on spawn time
		Collections.sort(bots, new Comparator<Bot>() {
			public int compare(Bot p1, Bot p2) {
				return p1.compareTimestamp(p2);
			}
		});
		
		for (int i=0;i<bots.size();i++){
			System.out.println("Bot "+bots.get(i).getId()+" at "+bots.get(i).getTimestamp()+" for "+bots.get(i).getLifespan());
		}
		
		executeBots(bots);
	}

	private static void executeBots(List<Bot> bots) {
		System.out.println("check");
		for (int i = 0; i < bots.size() && i < 20; i++) {
			double lifespan = bots.get(i).getLifespan();
			String[] argument = {"bot_" + bots.get(i).getId(), lifespan + ""};
			
			Runnable myRunnable = new Runnable() {
				public void run() {
					try {
						ClientRunner.main(argument);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};			
			Thread thread = new Thread(myRunnable);
			thread.start();
			
			if (i < bots.size()) {
				double time = (bots.get(i + 1).getTimestamp() - bots.get(i).getTimestamp()) / 1000;
				System.out.println("Wait for: " + time);
				try {
					Thread.sleep((long) time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static List<Bot> readGTAv06File(String path) {
		List<Bot> bots = new ArrayList<Bot>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line = br.readLine();
			int i = 0;

			while (line != null && i < 108) {	//108 includes the header of SC2_Edge_Detailed, relevant lines are only 100
				line = br.readLine();
				Bot bot;
				
				String[] temp = line.split(", ");

				if (temp.length >= 3) {
					int id = 0;
					try {
						id = Integer.parseInt(temp[0]);						//RowID
						double timestamp = Double.parseDouble(temp[1]);		//Timestamp
						double lifespan = Double.parseDouble(temp[2]);		//EdgeLifetime
						bot = new Bot(id, timestamp, lifespan);
						bots.add(bot);
						
					} catch (Exception e) {
					}

				}
				i += 1;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return bots;
	}	
}
