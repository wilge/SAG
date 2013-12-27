package sag;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;

public class Bankomat extends Agent{

AID Serwer = new AID("Serwer", AID.ISLOCALNAME);	
	public void setup() 
	{
		
		//czekaj na wiadomosc
		//przy otrzymaniu zapytania Request o wyplate stworz zapytanie do serwera o pin i $$
		
		//zapytaj inne bankomaty - cfp?
		
		//wyswietl wiadomosc o statusie transakcji
//		
//		ACLMessage msg =new ACLMessage(ACLMessage.INFORM);
//		msg.addReceiver(new AID("Piotrus", AID.ISLOCALNAME));
//		send(msg);
		System.out.println("Witaj, nazywam sie "+getAID().getLocalName());
		System.out.println("Moj AID to: "+getAID());

	
	
	addBehaviour(new Behaviour() {
		
		@Override
		public void action() {
			ACLMessage msg_rec = receive();
			//if flag == REQUEST then
			if (msg_rec != null && msg_rec.getPerformative()==ACLMessage.REQUEST) 
			{				
				ACLMessage confirm = new ACLMessage(ACLMessage.REQUEST);
				confirm.setContent(msg_rec.getContent());
				confirm.addReceiver(Serwer);
//				("Serwer"+"@192.168.63.16:1099/JADE"); // TODO przepisac
				send(confirm);
				System.out.println("Wykonano.");
			}
			else block();
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}
	});

	}

}
