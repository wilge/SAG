package sag;

import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Bankomat extends Agent
{

	AID Serwer = new AID("Serwer", AID.ISLOCALNAME);
	String ostatniKlient;
	String ostatniaKwota;
	
	protected void setup()
	{
		String serviceName = "cash withdrawn";

		// Read the name of the service to register as an argument
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
			serviceName = (String) args[0];
		}

		// Register the service
		System.out.println("Agent " + getLocalName()
				+ " registering service \"" + serviceName
				+ "\" of type \"cash withdrawn\"");
		try
		{
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setName(serviceName);
			sd.setType("cash withdrawn");
			// Agents that want to use this service need to "know" the
			// banking-ontology
			sd.addOntologies("banking-ontology");
			// Agents that want to use this service need to "speak" the FIPA-SL
			// language
			sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
			sd.addProperties(new Property("country", "Poland"));
			dfd.addServices(sd);

			DFService.register(this, dfd);
		} catch (FIPAException fe)
		{
			fe.printStackTrace();
		}

		// czekaj na wiadomosc
		// przy otrzymaniu zapytania Request o wyplate stworz zapytanie do
		// serwera o pin i $$

		// zapytaj inne bankomaty - cfp?

		// wyswietl wiadomosc o statusie transakcji
		
		System.out.println("Witaj, nazywam sie " + getAID().getLocalName());
		System.out.println("Moj AID to: " + getAID());
		
		
	addBehaviour(new Behaviour()
		{ // przepytaj bankomaty - DF services //done
		// if chec wyplaty gotowki //done
		// zapytaj DF o inne bankomaty //done
		// TODO wyslij zapytanie do bankomatow: Czy ID#### wyplacal u was ostatnio pieniadze? - zmiana tresci wiadomosci

			public void action()
			{	
				ACLMessage msg_wyplata = receive();

				if (msg_wyplata != null	&& msg_wyplata.getPerformative() == ACLMessage.REQUEST)
			{
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("cash withdrawn");
				dfd.addServices(sd);
				SearchConstraints ALL = new SearchConstraints();
				ALL.setMaxResults(new Long(-1));
		
				

				try
				{
					DFAgentDescription[] result = DFService.search(myAgent, dfd, ALL);
					AID[] bankomat = new AID[result.length];
					for (int i=0; i<result.length; i++)
				        {
						bankomat[i] = result[i].getName() ;
				                System.out.println(bankomat[i]);							
								
									ACLMessage askNeighbors = new ACLMessage(ACLMessage.REQUEST_WHEN);
									askNeighbors.setContent(msg_wyplata.getContent()); // TODO zmienic content
									askNeighbors.addReceiver(bankomat[i]);  
									send(askNeighbors);
									System.out.println("Wykonano.");
									                
				        }
					System.out.println("Iloœæ bankomatów: " +bankomat.length );
					System.out.println("Zaktualizowano listê bankomatów.");
				}
				catch (FIPAException fe)
				{ fe.printStackTrace(); }
			}
				else
					block();
			}
			
			@Override
			public boolean done()
			{
				// TODO Auto-generated method stub
				return false;
			}

		});

	addBehaviour(new Behaviour()
		{ // odpowiedz na zapytanie bankomatu
		// if zapytanie o historie transakcji
		// Czy ID#### wyplacal u was ostatnio pieniadze?
		// Odpowiedz do nadawcy TAK/NIE
			@Override
			public void action()
			{
				ACLMessage msg_pytanieBankomatu = receive();
				if (msg_pytanieBankomatu != null	&& msg_pytanieBankomatu.getPerformative() == ACLMessage.REQUEST_WHEN)
				{
					//TODO sprawdz czy tu z tej karty wyp³acano ostatnio pieni¹dze
					ACLMessage replyNeighbor = new ACLMessage(ACLMessage.INFORM);
					replyNeighbor.setContent(msg_pytanieBankomatu.getContent()); // TODO zmienic content
					replyNeighbor.addReceiver(msg_pytanieBankomatu.getSender());  
					send(replyNeighbor);
					System.out.println("Odpowiedziano na zapytanie bankomatu.");
				}
				else
					block();

			}

			@Override
			public boolean done()
			{
				// TODO Auto-generated method stub
				return false;
			}

		});

	addBehaviour(new Behaviour()
		{ // odpowiedz od bankomatu
		// if wszystkie bankomaty odpowiedzialy (albo okreslona ilosc)
		// if uplynal czas
		// if wszyscy odpowiedzieli NIE && if historia karty niepusta =>
		// oszustwo!
		// if ktos odpowiedzial TAK && adresat != historia karty => oszustwo!
		// else ustaw flage zgodnosci
			@Override
			public void action()
			{
				// TODO Auto-generated method stub
				block();
			}

			@Override
			public boolean done()
			{
				// TODO Auto-generated method stub
				return false;
			}

		});

	addBehaviour(new Behaviour()
		{ // zapytaj serwer o autoryzacje
		// if flaga zgodnosci

			@Override
			public void action()
			{
				ACLMessage msg_rec = receive();
				// TODO zmiena flagi z request
				// if flag == REQUEST then
				if (msg_rec != null
						&& msg_rec.getPerformative() == ACLMessage.INFORM)
				{
					ACLMessage confirm = new ACLMessage(ACLMessage.REQUEST);
					confirm.setContent(msg_rec.getContent());
					confirm.addReceiver(Serwer);
					send(confirm);
					System.out.println("Wykonano.");
				} else
					block();
			}

			@Override
			public boolean done()
			{
				// TODO Auto-generated method stub
				return false;
			}
		});

	addBehaviour(new Behaviour()
		{ // odpowiedz od serwera
		// wyswietl komunikat
		// if autoryzacja => wyplac pieniadze
			@Override
			public void action()
			{
				// TODO Auto-generated method stub
				block();
			}

			@Override
			public boolean done()
			{
				// TODO Auto-generated method stub
				return false;
			}

		});

	}

}
