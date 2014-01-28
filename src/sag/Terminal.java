package sag;

import java.sql.Date;
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

public class Terminal extends Agent
{
	AID Serwer = new AID("Serwer", AID.ISLOCALNAME);
	long Czas = (long) 0; // czas transakcji
	
	Integer autoryzacja = 0;
	
	String IDkarty;
	double Kwota;
	String PIN;
	String Wlasciciel;
	String CVC2;
	Date data_waznosci;
	
	int Poprzednia_kwota;
	Boolean zgodnosc = false;
	
	String wiadomosc;

	 protected void parsuj(String content)
     {
			// "ID_karty";"PIN";"Ostatnia_transakcja_kwota";"Nieudane_autoryzacje";"Licznik_bezgotówkowych";"Saldo";"Wlasciciel";		 
		 //id pin kwota wlasciciel cvc2 data_waznosci
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
                                     if (tokens>5)
                                     {
                                    	 setIDkarty(st.nextToken());
                                   		 setPIN(st.nextToken());
                                   	     setKwota(Double.parseDouble(st.nextToken()));
                                   	     setWlasciciel(st.nextToken());
                                    	 setCVC2(st.nextToken());			
            							 setData_waznosci(Date.valueOf(st.nextToken()));
                                     }
                                     else System.out.println("B³êdna treœæ wiadomoœci!");
                                     }
                             }
            //1005;0006;100;W;242;2015-02-03
             } catch (Exception e)
             {
                     e.printStackTrace();
             }


     }

	protected void setup()
	{
		// Nazwa us³ugi
		String serviceName = "cashless payment";

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
			sd.setType("cashless payment");
			sd.addOntologies("banking-ontology");
			sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
			sd.addProperties(new Property("country", "Poland"));
			dfd.addServices(sd);

			DFService.register(this, dfd);
		} catch (FIPAException fe)
		{
			fe.printStackTrace();
		}
		// System.out.println("Witaj, nazywam sie " + getAID().getLocalName());
		// System.out.println("Moj AID to: " + getAID());

		addBehaviour(new Behaviour() // zachowanie 1 - reakcja na chêæ zap³aty
										// gotówki // flaga REQUEST
		{
			public void action()
			{
				MessageTemplate mt;
				mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = receive(mt);
				if (msg != null	&& msg.getPerformative() == ACLMessage.REQUEST)
				{{
					DFAgentDescription dfd = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("cashless payment");
					dfd.addServices(sd);
					SearchConstraints ALL = new SearchConstraints();
					ALL.setMaxResults(new Long(-1));
				}
           	 setIDkarty(" ");
          	 setPIN(" ");
          	 setKwota(0);
          	 setWlasciciel(" ");
           	 setCVC2(" ");			
			 setData_waznosci(new Date(0));
				
					parsuj(msg.getContent());
					System.out.println(getIDkarty()+" | "+getPIN()+" | "+getKwota()+" | "+getWlasciciel()+" | "+getCVC2()+" | "+getData_waznosci());				
					Date now = new Date(System.currentTimeMillis());
					if (getData_waznosci().equals(new Date(0)))
						System.out.println("Nale¿y podaæ wszystkie 6 parametrów.");
					else if(now.after(getData_waznosci())) 
						{
						System.out.println("Karta przeterminowana.");
						autoryzacja=-1;		
						}
					else
					{
					setWiadomosc(msg.getContent());
					autoryzacja=1;
					}	
					
					
						 if(autoryzacja==1) {}// System.out.println("Wyslano zapytanie do serwera"  );
						 else System.out.println("Brak polaczenia z serwerem - niekompletne dane");


						// zapytaj serwer o autoryzacje
						if (autoryzacja == 1)
						{
							ACLMessage confirm = new ACLMessage(
									ACLMessage.REQUEST);							
							confirm.setContent(wiadomosc);
							confirm.setOntology("Terminal");
							confirm.addReceiver(Serwer);
							send(confirm);
//							System.out.println("Wykonano.");
							autoryzacja = 0;
						} 
					}
				else block();
		}
			@Override
			public boolean done()
			{
				return false;
			}

		});

		addBehaviour(new Behaviour() // zachowanie 2 - reakcja na odpowiedz od
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
	
	public int getPoprzednia_kwota()
	{
		return Poprzednia_kwota;
	}

	public void setPoprzednia_kwota(int poprzednia_kwota)
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

	public double getKwota()
	{
		return Kwota;
	}

	public void setKwota(double kwota)
	{
		Kwota = kwota;
	}

	public String getCVC2()
	{
		return CVC2;
	}

	public void setCVC2(String cVC2)
	{
		CVC2 = cVC2;
	}

	public Date getData_waznosci()
	{
		return data_waznosci;
	}

	public void setData_waznosci(Date data_waznosci)
	{
		this.data_waznosci = data_waznosci;
	}

	public String getWlasciciel()
	{
		return Wlasciciel;
	}

	public void setWlasciciel(String wlasciciel)
	{
		Wlasciciel = wlasciciel;
	}
	

}