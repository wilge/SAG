package sag;

import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import jade.core.behaviours.*;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;


public class Tester extends Agent
{

	ArrayList<ArrayList<String>> dataArray = new ArrayList<>();
	String path = ("C:/Users/luky/Desktop/");
	String we =	("sag.csv");
	AID[] bankomat = null;
	DFAgentDescription[] result= null;
	int ilosc=10;
	int licznik = 1;
	
	protected String tworz_msg(int i,int kwota,int poprzednia_kwota)
	{
		String msg=(dataArray.get(i).get(1)+";"+
				    dataArray.get(i).get(2)+";"+
				    kwota+";"+
				    poprzednia_kwota);
		// id pin kwota ost_kwota czas zgodnosc
//		id pin kwota ost_kwota
		
		return msg;
	}
	protected String tworz_msg(int i,int kwota)
	{
		String msg=(dataArray.get(i).get(0)+";"+
				    dataArray.get(i).get(1)+";"+
				    kwota+";"+
				    dataArray.get(i).get(2));
		// id pin kwota ost_kwota czas zgodnosc
//		id pin kwota ost_kwota
		
		return msg;
	}
	
	TimerTask a = new TimerTask()
	{
		
		@Override
		public void run()
		{
			
			
		}
	};
	
	@SuppressWarnings("serial")
	protected void setup()
	{
		// Nazwa us³ugi
		String serviceName = "cash withdrawn";

		// lub podawana z argumentu
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
			ilosc = Integer.parseInt((String)args[0]);
			if (args.length>1)serviceName = (String) args[1];
		}

			addBehaviour(new TickerBehaviour(null, 200)
			{
				
				@Override
				protected void onTick()
				{
					while(ilosc>=licznik)
					{
						Random generator = new Random();
						dataArray = FileLoader.loadFile(path+we);

					
						DFAgentDescription dfd = new DFAgentDescription();
						ServiceDescription sd = new ServiceDescription();
						sd.setType("cash withdrawn");
						dfd.addServices(sd);
						SearchConstraints ALL = new SearchConstraints();
						ALL.setMaxResults(new Long(-1));
						
						
						try
						{
							DFAgentDescription[] result = DFService.search(myAgent,
									dfd, ALL);
							AID[] bankomat = new AID[result.length];
							for (int i = 0; i < result.length; i++)
							{

								bankomat[i] = result[i].getName();
								 System.out.println(bankomat[i]);

								ACLMessage askNeighbors = new ACLMessage(
										ACLMessage.REQUEST);

								askNeighbors.setContent(tworz_msg((generator.nextInt(dataArray.size()-1)+1),generator.nextInt(20)*50));					
								int ind = 0;
								askNeighbors.addReceiver(bankomat[ind]);
								send(askNeighbors);
								try
								{
									TimeUnit.MILLISECONDS.sleep(250);
								} catch (InterruptedException e)
								{
									e.printStackTrace();
								}
								System.out.println(licznik+" )---------------------");
								licznik++;
							}	
						}				
						 catch (FIPAException e)
						{
							e.printStackTrace();
						} 


					block(200);
				}

				}
			
			});
	}
	
}
