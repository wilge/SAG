package sag;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

public class Serwer extends Agent{

	ArrayList<ArrayList<String>> dataArray = new ArrayList<>();
	String IDkarty;
	String Kwota;
	String Poprzednia_kwota;
	String PIN;
	
	
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
				//if (odpowiedz.equals("TAK")) setOdpowiedzTak(getOdpowiedzTak()+1);	
				//else if (odpowiedz.equals("NIE"));
				//else System.out.println("B³êdna odpowiedz");			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setup() 
	{
		//otworz plik
	
		String path = ("C:/Users/luky/Desktop/sag.csv");
		
		//wczytaj kolejne linie do tablicy	
		dataArray = FileLoader.loadFile(path);
		
		System.out.println("Rozmiar arraylisty: " + dataArray.size() + " x " + dataArray.get(0).size());

		
		for (int nr=0 ; nr < dataArray.size() ; nr++ )			
		{
			System.out.println ("Zdarzenie nr " + nr + ":");
			for (int i=0 ; i<dataArray.get(nr).size() ; i++)
				System.out.print(dataArray.get(nr).get(i)+ "|");
			System.out.println();
		}
        System.out.println();
				       		
        		
		addBehaviour(new Behaviour()
		{			
			@Override
			public void action()
			{
				
				//otrzymujemy aclm
				//wiadomosci typu: "Id_karty;Kwota;Poprzednia_kwota;PIN"  	//args 4
				//1008;100;0;"0009"
				//sprawdzamy czy content wiadomosci zgadza sie z ponizszymi wartosciami			
				
				



				//agree() , zeruj licznik, odejmij saldo
				//reject(), inkrementuj licznik nieautoryzowanych

				// TODO Auto-generated method stub
				MessageTemplate mt;
				mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);	
				ACLMessage msg_rec = receive(mt);
				if (msg_rec != null	&& msg_rec.getPerformative() == ACLMessage.INFORM)
				{
				//wiadomosci typu: "Id_karty;kwota;Poprzednia_kwota;PIN"
					parsuj(msg_rec.getContent());
				
				System.out.println("Odebrano wiadomoœæ od: " +msg_rec.getSender());
				
				int rekord = 0;
				for (int i=0; i< dataArray.size(); i++)
				if (dataArray.get(i).contains(IDkarty)) 
				{
					rekord = i;
					System.out.println("Zdarzenie o polu Id: "+IDkarty+" ma numer: " + rekord);
				}
				
//				if licznik<3
				int Liczba_nieautoryzowanych = Integer.parseInt(dataArray.get(rekord).get(3));
				if (Liczba_nieautoryzowanych > 2) 
				System.out.println("Konto zablokowane!");
				else
				{
//					if pin=pin_wzor
					if (!PIN.equals(dataArray.get(rekord).get(1)))
					{
					System.out.println("B³êdny kod PIN!");					
					Liczba_nieautoryzowanych++;
					System.out.println("Pozosta³o " + (3-Liczba_nieautoryzowanych) + " prob.");
					}
					else
					{
//						if kwota<saldo
						if (Integer.parseInt(Kwota)>Integer.parseInt(dataArray.get(rekord).get(4)))
						{
							System.out.println("Brak srodków!");
						}
						else 
						{
//							if ost_transakcja sie zgadza
							if (!Poprzednia_kwota.equals(dataArray.get(rekord).get(2)))
								System.out.println("Poprzednia transakcja ró¿na, wykryto próbê oszustwa!");
							else
							{
								//agree() , zeruj licznik, odejmij saldo
								Liczba_nieautoryzowanych = 0;
								//odejmij saldo
								//wyslij alcm do bankomatu
								System.out.println("Wyplacono pieniadze.");
								
				
//				String ID_karty = dataArray.get(1).get(0);
//				String Wlasciciel = dataArray.get(1).get(5);
					}

				}
				}
				}
				}
			}
			
			@Override
			public boolean done()
			{
				// TODO Auto-generated method stub
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

}
