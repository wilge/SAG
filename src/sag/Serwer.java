package sag;

//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.BufferedWriter;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Serwer extends Agent
{

	ArrayList<ArrayList<String>> dataArray = new ArrayList<>();
	String IDkarty;
	int Kwota;
	int Poprzednia_kwota;
	String PIN;

	// "ID_karty";"PIN";"Ostatnia_transakcja_kwota";"Nieudane_autoryzacje";"Licznik_bezgotówkowych";"Saldo";"Wlasciciel"
	String tID, tPIN, tWLASCICIEL;
	int tOSTATNIA, tNIEAUTORYZOWANE, tBEZGOTOWKOWE, tSALDO;

	String tworz_wiadomosc(String id, String pin, int ost, int nie, int bez,
			int sal, String wla)
	{
		return (id + ";" + pin + ";" + ost + ";" + nie + ";" + bez + ";" + sal
				+ ";" + wla);
	}

	// void czytaj_wiadomosc(String content)
	// {
	// try{
	// if (content.isEmpty())
	// {
	// System.out.println("Brak treœci wiadomoœci!");
	// }
	// else {
	// {
	// StringTokenizer st = new StringTokenizer(content, ";");
	// }
	// }

	protected void parsuj(String content)
	{

		try
		{
			if (content.isEmpty())
			{
				System.out.println("Brak treœci wiadomoœci!");
			} else
			{
				{
					StringTokenizer st = new StringTokenizer(content, ";");
					// System.out.println("Liczba tokenow: "+st.countTokens());
					switch (st.countTokens())
					{
					case 2:
						setIDkarty(st.nextToken());
						setPoprzednia_kwota(Integer.parseInt(st.nextToken()));
						break;
					case 3:
						setIDkarty(st.nextToken());
						setPoprzednia_kwota(Integer.parseInt(st.nextToken()));
						String odpowiedz = st.nextToken();
						// if (odpowiedz.equals("TAK"))
						// setOdpowiedzTak(getOdpowiedzTak()+1);
						// else if (odpowiedz.equals("NIE"));
						// else System.out.println("B³êdna odpowiedz");
						break;
					case 4:
						setIDkarty(st.nextToken());
						setKwota(Integer.parseInt(st.nextToken()));
						setPoprzednia_kwota(Integer.parseInt(st.nextToken()));
						setPIN(st.nextToken());
						break;
					default:
						System.out.println("B³êna treœæ wiadomoœci!");
					}
				}
			}
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setup()
	{
		// otworz plik

		String path = ("C:/Users/luky/Desktop/sag.csv");

		// wczytaj kolejne linie do tablicy
		dataArray = FileLoader.loadFile(path);

		System.out.println("Rozmiar arraylisty: " + dataArray.size() + " x "
				+ dataArray.get(0).size());

		for (int nr = 1; nr < dataArray.size(); nr++)
		{
			System.out.println("Zdarzenie nr " + nr + ":");
			for (int i = 0; i < dataArray.get(nr).size(); i++)
				System.out.print(dataArray.get(nr).get(i) + "|");
			System.out.println();
		}
		System.out.println();

		System.out.println("**********************");
		System.out.println(dataArray.get(18).toString());
		// ID_karty|PIN|Ostatnia_transakcja_kwota|Nieudane_autoryzacje|Saldo|Wlasciciel|

		addBehaviour(new Behaviour()
		{
			@Override
			public void action()
			{

				// otrzymujemy aclm
				// wiadomosci typu: "Id_karty;Kwota;Poprzednia_kwota;PIN" //args
				// 4
				// 1008;100;0;"0009"
				// sprawdzamy czy content wiadomosci zgadza sie z ponizszymi
				// wartosciami

				// agree() , zeruj licznik, odejmij saldo
				// reject(), inkrementuj licznik nieautoryzowanych

				// TODO Auto-generated method stub
				MessageTemplate mt;
				mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg_rec = receive(mt);
				if (msg_rec != null
						&& msg_rec.getPerformative() == ACLMessage.REQUEST)
				{
					// wiadomosci typu: "Id_karty;kwota;Poprzednia_kwota;PIN"
					parsuj(msg_rec.getContent());

					System.out.println("Odebrano wiadomoœæ od: "
							+ msg_rec.getSender());

					int rekord = 0;
					for (int i = 1; i < dataArray.size(); i++)
						if (dataArray.get(i).contains(IDkarty))
						{
							rekord = i;
							System.out.println("Zdarzenie o polu Id: "
									+ IDkarty + " ma numer: " + rekord);
						}

					if (rekord == 0)
						System.out.println("Nie znaleziono podanego IDkarty!");
					else
					{
						// tID, tPIN, tOSTATNIA, tNIEAUTORYZOWANE,
						// tBEZGOTOWKOWE, tSALDO, tWLASCICIEL;
						settID(dataArray.get(rekord).get(0));
						settPIN(dataArray.get(rekord).get(1));
						settOSTATNIA(Integer.parseInt(dataArray.get(rekord)
								.get(2)));
						settNIEAUTORYZOWANE(Integer.parseInt(dataArray.get(
								rekord).get(3)));
						settBEZGOTOWKOWE(Integer.parseInt(dataArray.get(rekord)
								.get(4)));
						settSALDO(Integer
								.parseInt(dataArray.get(rekord).get(5)));
						settWLASCICIEL(dataArray.get(rekord).get(6));

						// if licznik<3
						if (tNIEAUTORYZOWANE > 2)
							System.out.println("Konto zablokowane!");
						else
						{
							if (Kwota < 50)
								settBEZGOTOWKOWE(gettBEZGOTOWKOWE() + 1);
							// if pin=pin_wzor
							else if (!PIN.equals(dataArray.get(rekord).get(1)))
							{
								System.out.println("B³êdny kod PIN!");
								settNIEAUTORYZOWANE(gettNIEAUTORYZOWANE() + 1);
								System.out.println("Pozosta³o "
										+ (3 - gettNIEAUTORYZOWANE())
										+ " prob.");
							} else
							{
								settBEZGOTOWKOWE(0);
								// if kwota<saldo
								if (Kwota > tSALDO)
								{
									System.out.println("Brak srodków!");
								} else
								{
									// if ost_transakcja sie zgadza
									if (getPoprzednia_kwota() != gettOSTATNIA())
										System.out
												.println("Poprzednia transakcja ró¿na, wykryto próbê oszustwa!");
									else
									{
										// agree() , zeruj licznik, odejmij
										// saldo
										settNIEAUTORYZOWANE(0);
										// odejmij saldo
										settSALDO(gettSALDO() - getKwota());

										System.out
												.println("Wyplacono pieniadze.");

										// wyslij alcm do bankomatu
										System.out.println("Wiadomosc:");
										String odpowiedz=(tworz_wiadomosc(
												gettID(), gettPIN(),
												gettOSTATNIA(),
												gettNIEAUTORYZOWANE(),
												gettBEZGOTOWKOWE(),
												gettSALDO(), gettWLASCICIEL()));
										System.out.println(odpowiedz);
										
										ACLMessage reply = new ACLMessage(1);
										reply.setContent(odpowiedz); // TODO zmienic content - odpowiedz czy wyplacano
										reply.addReceiver(msg_rec.getSender());  
										send(reply);

									}

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

	public int getKwota()
	{
		return Kwota;
	}

	public void setKwota(int kwota)
	{
		Kwota = kwota;
	}

	public int getPoprzednia_kwota()
	{
		return Poprzednia_kwota;
	}

	public void setPoprzednia_kwota(int poprzednia_kwota)
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

	public String gettID()
	{
		return tID;
	}

	public void settID(String tID)
	{
		this.tID = tID;
	}

	public String gettPIN()
	{
		return tPIN;
	}

	public void settPIN(String tPIN)
	{
		this.tPIN = tPIN;
	}

	public int gettOSTATNIA()
	{
		return tOSTATNIA;
	}

	public void settOSTATNIA(int tOSTATNIA)
	{
		this.tOSTATNIA = tOSTATNIA;
	}

	public int gettNIEAUTORYZOWANE()
	{
		return tNIEAUTORYZOWANE;
	}

	public void settNIEAUTORYZOWANE(int tNIEAUTORYZOWANE)
	{
		this.tNIEAUTORYZOWANE = tNIEAUTORYZOWANE;
	}

	public int gettBEZGOTOWKOWE()
	{
		return tBEZGOTOWKOWE;
	}

	public void settBEZGOTOWKOWE(int tBEZGOTOWKOWE)
	{
		this.tBEZGOTOWKOWE = tBEZGOTOWKOWE;
	}

	public int gettSALDO()
	{
		return tSALDO;
	}

	public void settSALDO(int tSALDO)
	{
		this.tSALDO = tSALDO;
	}

	public String gettWLASCICIEL()
	{
		return tWLASCICIEL;
	}

	public void settWLASCICIEL(String tWLASCICIEL)
	{
		this.tWLASCICIEL = tWLASCICIEL;
	}

}
