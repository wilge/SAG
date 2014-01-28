package sag;

import java.sql.Timestamp;
import java.util.Random;
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
	String ostatniKlient; // ID karty
	double ostatniaKwota; // poprzednia kwota
	long Czas = (long) 0; // czas transakcji
	
	Integer iloscBankomatow = 99;
	Integer iloscOdpowiedzi = 0;
	
	Integer odpowiedzTak = 0;
	Integer autoryzacja = 0;
	
	String IDkarty;
	double Kwota;
	double Poprzednia_kwota;
	String PIN;
	Boolean zgodnosc = false;
	
	String wiadomosc;

	 protected void parsuj(String content)
     {
			// "ID_karty";"PIN";"Ostatnia_transakcja_kwota";"Nieudane_autoryzacje";"Licznik_bezgotówkowych";"Saldo";"Wlasciciel";
			// id pin kwota ost_kwota czas zgodnosc
             try
             {
                     if (content.isEmpty()) // wiadomosc pusta
                     {
                             System.out.println("Brak treœci wiadomoœci!");
                     } else
                     {
                             {
                                     StringTokenizer st = new StringTokenizer(content, ";");
//                                     System.out.println("Liczba tokenow: " + st.countTokens());
                                     int tokens = st.countTokens(); 
                                     switch (tokens)
                                     {
                                     case 2:
                                             setIDkarty(st.nextToken());
                                             setPoprzednia_kwota(Double.parseDouble(st.nextToken()));
                                             break;
                                     case 3:
                                            setIDkarty(st.nextToken());
             								setCzas(Long.parseLong(st.nextToken()));
            								setZgodnosc(Boolean.parseBoolean(st.nextToken()));
                                             break; //TODO 
                                     case 4:
                                     case 5:
                                     case 6:
                                    	 setIDkarty(st.nextToken());
             							 setPIN(st.nextToken());
            						     setKwota(Double.parseDouble(st.nextToken()));
            							 setPoprzednia_kwota(Double.parseDouble(st.nextToken()));
            						 	 if (tokens>4) setCzas(Long.parseLong(st.nextToken()));
            							 if (tokens>5) setZgodnosc(Boolean.parseBoolean(st.nextToken()));
                                             break;
                                     default:
                                             System.out.println("B³êdna treœæ wiadomoœci!");
                                     }
                             }
                     }
             } catch (Exception e)
             {
                     e.printStackTrace();
             }


     }

	protected void setup()
	{
		// Nazwa us³ugi
		String serviceName = "cash withdrawn";

		// lub podawana z argumentu
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
			serviceName = (String) args[0];
		}

		// Rejestracja us³ugi
		// System.out.println("Agent " + getLocalName()
		// + " registering service \"" + serviceName
		// + "\" of type \"cash withdrawn\"");
		try
		{
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setName(serviceName);
			sd.setType("cash withdrawn");
			sd.addOntologies("banking-ontology");
			sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
			sd.addProperties(new Property("country", "Poland"));
			dfd.addServices(sd);

			DFService.register(this, dfd);
		} catch (FIPAException fe)
		{
			fe.printStackTrace();
		}
		// Bankomat siê przedstawia
		// System.out.println("Witaj, nazywam sie " + getAID().getLocalName());
		// System.out.println("Moj AID to: " + getAID());

		addBehaviour(new Behaviour() // zachowanie 1 - reakcja na chêæ wyp³aty
										// gotówki // flaga REQUEST
		{ // przepytaj bankomaty - DF services
			// if chec wyplaty gotowki
			// zapytaj DF o inne bankomaty
			// wyslij zapytanie do bankomatow: Czy ID#### wyplacal u was
			// ostatnio pieniadze? - zmiana tresci wiadomosci

			public void action()
			{
				MessageTemplate mt;
				mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg_wyplata = receive(mt);
				// wiadomosci typu: "Id_karty;kwota;Poprzednia_kwota;PIN"
				if (msg_wyplata != null
						&& msg_wyplata.getPerformative() == ACLMessage.REQUEST)
				{
					DFAgentDescription dfd = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("cash withdrawn");
					dfd.addServices(sd);
					SearchConstraints ALL = new SearchConstraints();
					ALL.setMaxResults(new Long(-1));

					parsuj(msg_wyplata.getContent());
					setWiadomosc(msg_wyplata.getContent());


					try
					{
						DFAgentDescription[] result = DFService.search(myAgent,
								dfd, ALL);
						AID[] bankomat = new AID[result.length];
						for (int i = 0; i < result.length; i++)
						{
							// zapytaj wszystkie bankomaty
							bankomat[i] = result[i].getName();
							// System.out.println(bankomat[i]);

							ACLMessage askNeighbors = new ACLMessage(
									ACLMessage.REQUEST_WHEN);

							// wiadomosci typu: "Id_karty;Poprzednia_kwota" //args 2
							setWiadomosc(getIDkarty()+";"+getPoprzednia_kwota());
							askNeighbors.setContent(getWiadomosc()); 
//							System.out.println(getWiadomosc());
						
							askNeighbors.addReceiver(bankomat[i]);
							send(askNeighbors);
							// System.out.println("Wykonano.");

						}
//						System.out.println(getKwota());
						iloscBankomatow = bankomat.length;
//						System.out.println("Iloœæ bankomatów: "
//								+ iloscBankomatow);
//						System.out.println("Zaktualizowano listê bankomatów.");

					} catch (FIPAException fe)
					{
						fe.printStackTrace();
					}
				} else
					block();
			}

			@Override
			public boolean done()
			{
				return false;
			}

		});

		addBehaviour(new Behaviour() // zachowanie 2 - reakcja na zapytanie
										// bankomatu //flaga REQUEST_WHEN
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
				if (msg_pytanieBankomatu != null
						&& msg_pytanieBankomatu.getPerformative() == ACLMessage.REQUEST_WHEN)
				{
					// sprawdz czy tu z tej karty wyp³acano ostatnio pieni¹dze

					// *************************************************************//
					// TODO potrzebujemy informacji o kwocie ostatniej
					// transakcji!!!//
					// *************************************************************//

					parsuj(msg_pytanieBankomatu.getContent());
					
					if (getPoprzednia_kwota()==getOstatniaKwota() && getIDkarty().equals(getOstatniKlient()))
						setZgodnosc(true);
					else
						setZgodnosc(false);
					

//					System.out.println("Ostatni klient: "+getZgodnosc());
					// wiadomosci typu: "Id_karty;czas;TAK/NIE" //args 3
					setWiadomosc(getIDkarty()+";"+getCzas()+";"+getZgodnosc());				
					
					ACLMessage replyNeighbor = new ACLMessage(ACLMessage.INFORM);
					replyNeighbor.setContent(getWiadomosc());																
					replyNeighbor.addReceiver(msg_pytanieBankomatu.getSender());
					send(replyNeighbor);
					// System.out.println("Odpowiedziano na zapytanie bankomatu.");
					// System.out.println("Biezaca liczba bankomatów: "
					// + iloscBankomatow);

				} else
					block();

			}

			@Override
			public boolean done()
			{
				return false;
			}

		});

		addBehaviour(new Behaviour() // zachowanie 3 - sprawdzanie odpowiedzi od
										// bankomatów flaga INFORM
		// tutaj zliczamy odpowiedzi
		// reakcja na odpowiedz od wszystkich bankomatów
		{
			// if wszystkie bankomaty odpowiedzialy (albo okreslona ilosc)
			// if uplynal czas
			// if wszyscy odpowiedzieli NIE && if historia karty niepusta =>
			// oszustwo!
			// if ktos odpowiedzial TAK && adresat != historia karty => TODO
			// oszustwo!
			// else ustaw flage zgodnosci
			@Override
			public void action()
			{
				MessageTemplate mt;
				mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage msg_rec = receive(mt);
				if (msg_rec != null
						&& msg_rec.getPerformative() == ACLMessage.INFORM)
				{
					parsuj(msg_rec.getContent());
					if (getOdpowiedzTak() == 0)
					{
						//if ktorys bankomat odpowiedzial tak ->
						if (getZgodnosc().booleanValue())
						{
							setOdpowiedzTak(getOdpowiedzTak()+1);
							setZgodnosc(false);
						}
					} else if (getOdpowiedzTak() > 0)
					{
						//if kolejny odpowiedzial Tak -> oszustwo ->
						if (getZgodnosc().booleanValue())
						{
							setOdpowiedzTak(getOdpowiedzTak()+1);
							setZgodnosc(false);
						}
					}
					
					
					int roznica =150;//150ms
//					int roznica =(1000*10); //10sec	
					long t1 = System.currentTimeMillis();   //TODO czas
					Timestamp timeStamp = new Timestamp(getCzas());
					Timestamp ts = new Timestamp(t1-roznica);	
					if (ts.before(timeStamp)) 
					{
						System.out.println("Korzystano z tej karty w przeci¹gu ostatnich "+roznica/1000.0+" sekund!"); //TODO
						System.out.println("Wstrzymano autoryzacjê.");
						System.out.println("ts:" +timeStamp+"  | t0: "+ts);
						autoryzacja=-1;						
					}

//					System.out.println("TAK: "+getOdpowiedzTak());
					iloscOdpowiedzi++;
//					System.out.println("Biezaca liczba odpowiedzi: "
//							+ iloscOdpowiedzi);

					
					
					// wiadomosci typu: "Id_karty;czas;TAK/NIE" //args 3
					// wiadomosci typu: "Id_karty;PIN;Kwota;Poprzednia_kwota" //args 4
					// wiadomosci typu: "Id_karty;Poprzednia_kwota" //args 2
					// id pin kwota ost_kwota czas zgodnosc						
			
					
					if (iloscBankomatow == iloscOdpowiedzi)
					{
						// TODO sprawdzanie warunków
						iloscOdpowiedzi = 0;
//						System.out
//								.println("Wszystkie bankomaty odpowiedzia³y. Iloœæ bankomatów: "
//										+ iloscBankomatow);
						 if (getOdpowiedzTak()<2 && autoryzacja >=0)
						{
							autoryzacja = 1;

						}
						 else 
							 {
							 	autoryzacja=-1;
							 	long tic = System.currentTimeMillis();
								Random generator = new Random();
								for (int i=0;i<1E6;i++)
									Math.pow(generator.nextDouble(), generator.nextDouble());
								long toc = System.currentTimeMillis();
								System.out.println(toc-tic);
							 }
						
						 if(autoryzacja==1) System.out.println("autoryzacja : poprawna"  );
						 else System.out.println("autoryzacja : brak");


						// zapytaj serwer o autoryzacje

						if (autoryzacja == 1)
						{
							ACLMessage confirm = new ACLMessage(
									ACLMessage.REQUEST);
							// wiadomosci typu:
							// "Id_karty;kwota;Poprzednia_kwota;PIN"
							
							setWiadomosc(getIDkarty()+";"+getPIN()+";"+getKwota()+";"+getPoprzednia_kwota());
							confirm.setContent(wiadomosc);
							confirm.setOntology("Bankomat");
							confirm.addReceiver(Serwer);
							send(confirm);
//							System.out.println("Wykonano.");
							autoryzacja = 0;
							setOdpowiedzTak(0);
						} else if (autoryzacja == -1)
						{
							System.out
									.println("Wykryto próbê oszustwa, brak po³¹czenia do serwera.");
							System.out.println();
							autoryzacja = 0;
						}
					}

				} else
					block();

			}

			@Override
			public boolean done()
			{
				return false;
			}

		});

		addBehaviour(new Behaviour() // zachowanie 4 - reakcja na odpowiedz od
										// serwera
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
//					System.out.println("Otrzymano odpowiedz od serwera.");
					
					setOstatniKlient(getIDkarty()); // ID karty
					setOstatniaKwota(getKwota()); // poprzednia kwota
					setCzas(System.currentTimeMillis());// czas transakcji
					
					System.out.println(msg_rec.getContent());
					System.out.println();
					
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

	public double getOstatniaKwota()
	{
		return ostatniaKwota;
	}

	public void setOstatniaKwota(double i)
	{
		this.ostatniaKwota = i;
	}

	public String getIDkarty()
	{
		return IDkarty;
	}

	public void setIDkarty(String iDkarty)
	{
		IDkarty = iDkarty;
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

	public double getKwota()
	{
		return Kwota;
	}

	public void setKwota(double kwota)
	{
		Kwota = kwota;
	}

	public double getPoprzednia_kwota()
	{
		return Poprzednia_kwota;
	}

	public void setPoprzednia_kwota(double poprzednia_kwota)
	{
		Poprzednia_kwota = poprzednia_kwota;
	}

	public Long getCzas()
	{
		return Czas;
	}

	public void setCzas(Long czas)
	{
		Czas = czas;
	}

	public Boolean getZgodnosc()
	{
		return zgodnosc;
	}

	public void setZgodnosc(Boolean zgodnosc)
	{
		this.zgodnosc = zgodnosc;
	}

}