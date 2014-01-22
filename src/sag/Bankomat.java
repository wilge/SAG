package sag;

import java.util.StringTokenizer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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
	String ostatniKlient; //ID karty
	String ostatniaKwota;
	Integer iloscBankomatow=9; 
	Integer iloscOdpowiedzi=0;
	Integer odpowiedzTak=0;
	Integer autoryzacja=0;
	String IDkarty;
	String Kwota;
	String Poprzednia_kwota;
	String PIN;
	String wiadomosc;
	


	protected void parsuj(String content)
	{
		
		try{
		if (content.isEmpty())// equals(null))
		{ 
			System.out.println("Brak treœci wiadomoœci!");
		}
		else {		
		{
			StringTokenizer st = new StringTokenizer(content, ";");
			System.out.println("Liczba tokenow: "+st.countTokens());
			switch (st.countTokens())
			{
			case 2:
				setIDkarty(st.nextToken());
				setPoprzednia_kwota(st.nextToken());
				break;
			case 3:
				setIDkarty(st.nextToken());
				setPoprzednia_kwota(st.nextToken());
				String odpowiedz = st.nextToken();
				if (odpowiedz.equals("TAK")) setOdpowiedzTak(getOdpowiedzTak()+1);	
				else if (odpowiedz.equals("NIE"));
				else System.out.println("B³êdna odpowiedz");			
				break;
			case 4:
				setIDkarty(st.nextToken());
				setKwota(st.nextToken());
				setPoprzednia_kwota(st.nextToken());
				setPIN(st.nextToken());
				break;
			default:
				System.out.println("B³êna treœæ wiadomoœci!");					
			}
		}
		}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//parsuj contontent i zapisz do 4 zmiennych: IDkarty; Kwota; Poprzednia_kwota; PIN				
		//ID karty - potrzebne do bankomtów i do serwera
		//Kwota - wysy³ane do serwera
		//Poprzednia_kwota - potrzebna bankomatom do sprawdzenia
		//PIN - sprawdzany na serwerze
		//"1010";150;0;"0011"
		
	//TODO parser wiadomosci, zapisywanie zmiennych
		//wiadomosci typu: "Id_karty;Kwota;Poprzednia_kwota;PIN"  	//args 4
		//wiadomosci typu: "Id_karty;Poprzednia_kwota"				//args 2
		//wiadomosci typu: "Id_karty;Poprzednia_kwota;TAK/NIE"		//args 3
		
		//ID_karty;Poprzednia_kwota;Kwota;PIN;Tak/Nie
	}
	

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

		
	addBehaviour(new Behaviour() //zachowanie 1 - reakcja na chêæ wyp³aty gotówki
		{ // przepytaj bankomaty - DF services //done
		// if chec wyplaty gotowki //done
		// zapytaj DF o inne bankomaty //done
		// TODO wyslij zapytanie do bankomatow: Czy ID#### wyplacal u was ostatnio pieniadze? - zmiana tresci wiadomosci

			public void action()
			{	
				MessageTemplate mt;
				mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg_wyplata = receive(mt);				
				//wiadomosci typu: "Id_karty;kwota;Poprzednia_kwota;PIN"
				if (msg_wyplata != null	&& msg_wyplata.getPerformative() == ACLMessage.REQUEST)
			{
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("cash withdrawn");
				dfd.addServices(sd);
				SearchConstraints ALL = new SearchConstraints();
				ALL.setMaxResults(new Long(-1));

				parsuj(msg_wyplata.getContent());
				setWiadomosc(msg_wyplata.getContent());

				System.out.println("Nr karty to: " +getIDkarty());
				System.out.println("Kwota to: " +getKwota());
				System.out.println("Poprzednia kwota to:" +getPoprzednia_kwota());
				System.out.println("Pin: " + getPIN());
				System.out.println("Liczba TAK: "+getOdpowiedzTak());
				System.out.println();

				
				try
				{
					DFAgentDescription[] result = DFService.search(myAgent, dfd, ALL);
					AID[] bankomat = new AID[result.length];
					for (int i=0; i<result.length; i++)
				        {
						bankomat[i] = result[i].getName() ;
				                System.out.println(bankomat[i]);							
								
									ACLMessage askNeighbors = new ACLMessage(ACLMessage.REQUEST_WHEN);
									//wiadomosci typu: "Id_karty;Poprzednia_kwota"
									askNeighbors.setContent(msg_wyplata.getContent()); // TODO zmienic content
									askNeighbors.addReceiver(bankomat[i]);  
									send(askNeighbors);
									System.out.println("Wykonano.");
									                
				        }
					iloscBankomatow = bankomat.length;
					System.out.println("Iloœæ bankomatów: " +iloscBankomatow );
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

	addBehaviour(new Behaviour() //zachowanie 2 - reakcja na zapytanie bankomatu
		{ // odpowiedz na zapytanie bankomatu
		// if zapytanie o historie transakcji
		// Czy ID#### wyplacal u was ostatnio pieniadze?
		// Odpowiedz do nadawcy TAK/NIE
			@Override
			public void action()
			{
				MessageTemplate mt;
				mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST_WHEN);	
				ACLMessage msg_pytanieBankomatu = receive(mt);
				if (msg_pytanieBankomatu != null	&& msg_pytanieBankomatu.getPerformative() == ACLMessage.REQUEST_WHEN)
				{
					//TODO sprawdz czy tu z tej karty wyp³acano ostatnio pieni¹dze
					
					//*************************************************************//
					//TODO potrzebujemy informacji o kwocie ostatniej transakcji!!!//
					//*************************************************************//
					
					//wiadomosci typu: "Id_karty;Poprzednia_kwota;TAK/NIE"
					ACLMessage replyNeighbor = new ACLMessage(ACLMessage.INFORM);
					replyNeighbor.setContent(msg_pytanieBankomatu.getContent()); // TODO zmienic content - odpowiedz czy wyplacano
					replyNeighbor.addReceiver(msg_pytanieBankomatu.getSender());  
					send(replyNeighbor);
					System.out.println("Odpowiedziano na zapytanie bankomatu.");
					System.out.println("Biezaca liczba bankomatów: "+iloscBankomatow);

				}
				else
					block();

			}

			@Override
			public boolean done()
			{
				return false;
			}

		});

	addBehaviour(new Behaviour() //zachowanie 3 - reakcja na odpowiedz od wszystkich bankomatów
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
				if (iloscBankomatow == iloscOdpowiedzi)
				{
					//TODO sprawdzanie warunków
					iloscOdpowiedzi = 0;
					System.out.println("Wszystkie bankomaty odpowiedzia³y. Iloœæ bankomatów: "+iloscBankomatow);
//					if (getOdpowiedzTak()==1) // || odpowiedzTak==0 && kartaPusta==1)
					{
						autoryzacja=1;
						System.out.println("autoryzacja : "+autoryzacja);
					}
//					else autoryzacja=-1;						
				}				
			}

			@Override
			public boolean done()
			{
				return false;
			}

		});

	addBehaviour(new Behaviour() //zachowanie 4 - sprawdzanie odpowiedzi od bankomatów
		{  //TODO to zachowanie nie jest kierowane do serwera, tutaj zliczamy odpowiedzi
		// if flaga zgodnosci //TODO ustawiamy flagê na podstawie odpowiedzi od bankomatów

			@Override
			public void action()
			{
				MessageTemplate mt;
				mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);	
				ACLMessage msg_rec = receive(mt);
				if (msg_rec != null	&& msg_rec.getPerformative() == ACLMessage.INFORM)
				{
					if (getOdpowiedzTak()==0)
					{
						//TODO  if ktorys bankomat odpowiedzial tak -> odpowiedzTak=1; 						
					}
					else if (getOdpowiedzTak()==1)
					{
						//TODO if kolejny odpowiedzial Tak -> oszustwo -> odpowiedzTak=2;
					}
					iloscOdpowiedzi++;
					System.out.println("Biezaca liczba odpowiedzi: "+iloscOdpowiedzi);
				} else
					block();
			}

			@Override
			public boolean done()
			{
				return false;
			}
		});
	
	addBehaviour(new Behaviour() //zachowanie 5 - wyslanie zapytania do serwera
	{ // zapytaj serwer o autoryzacje 
	// wyswietl komunikat
		@Override
		public void action()
		{
			// TODO Auto-generated method stub
			if (autoryzacja==1)// && iloscOdpowiedzi==iloscBankomatow)
			{
			ACLMessage confirm = new ACLMessage(ACLMessage.REQUEST);
			//wiadomosci typu: "Id_karty;kwota;Poprzednia_kwota;PIN"
			confirm.setContent(wiadomosc);
			confirm.addReceiver(Serwer);
			send(confirm);
			System.out.println("Wykonano.");
			autoryzacja=0; 
			}
			else if (autoryzacja==-1)
			{
				System.out.println("Wykryto próbê oszustwa, brak po³¹czenia do serwera.");
			}
		}

		@Override
		public boolean done()
		{
			return false;
		}

	});

	addBehaviour(new Behaviour() //zachowanie 6 - reakcja na odpowiedz od serwera
		{ // odpowiedz od serwera
		// wyswietl komunikat
		// if autoryzacja od serwera => wyplac pieniadze
			@Override
			public void action()
			{
				MessageTemplate mt;
				mt = MessageTemplate.MatchSender(Serwer);	
				ACLMessage msg_rec = receive(mt);
				if (msg_rec != null)
				{
					System.out.println("Otrzymano odpowiedz od serwera.");
				} else
					block();
			}

			@Override
			public boolean done()
			{
				return false;
			}

		});
	
	
	
	
	
	}

	public Integer getOdpowiedzTak()
	{
		return odpowiedzTak;
	}

	public void setOdpowiedzTak(Integer odpowiedzTak)
	{
		this.odpowiedzTak = odpowiedzTak;
	}
	public String getOstatniKlient()
	{
		return ostatniKlient;
	}

	public void setOstatniKlient(String ostatniKlient)
	{
		this.ostatniKlient = ostatniKlient;
	}

	public String getOstatniaKwota()
	{
		return ostatniaKwota;
	}

	public void setOstatniaKwota(String ostatniaKwota)
	{
		this.ostatniaKwota = ostatniaKwota;
	}

	public String getIDkarty()
	{
		return IDkarty;
	}

	public void setIDkarty(String iDkarty)
	{
		IDkarty = iDkarty;
	}

	public String getKwota()
	{
		return Kwota;
	}

	public void setKwota(String kwota)
	{
		Kwota = kwota;
	}

	public String getPoprzednia_kwota()
	{
		return Poprzednia_kwota;
	}

	public void setPoprzednia_kwota(String poprzednia_kwota)
	{
		Poprzednia_kwota = poprzednia_kwota;
	}

	public String getPIN()
	{
		return PIN;
	}

	public void setPIN(String pIN)
	{
		PIN = pIN;
	}


	public String getWiadomosc()
	{
		return wiadomosc;
	}


	public void setWiadomosc(String wiadomosc)
	{
		this.wiadomosc = wiadomosc;
	}

	
}