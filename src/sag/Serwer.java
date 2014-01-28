package sag;

//import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class Serwer extends Agent
{

	ArrayList<ArrayList<String>> dataArray = new ArrayList<>();
	String IDkarty;
	double Kwota;
	double Poprzednia_kwota;
	String PIN;
	Long Czas;
	String path = ("C:/Users/luky/Desktop/");
	String we =	("sag.csv"), wy = "baza.csv";	
	String wiadomosc;

	String Wlasciciel;
	String CVC2;
	Date data_waznosci;
	
	int l_otrz=0,l_poprawnych=0,l_blednych=0;
	int t_otrz=0,t_poprawnych=0,t_blednych=0;

	// "ID_karty";"PIN";"Ostatnia_transakcja_kwota";"Nieudane_autoryzacje";"Licznik_bezgotówkowych";"Saldo";"Wlasciciel"
	String tID, tPIN, tWLASCICIEL, tCVC2;
	int  tNIEAUTORYZOWANE, tBEZGOTOWKOWE;
	double tOSTATNIA, tSALDO;
	Date tdata_waznosci;
	
	void FileWritter (ArrayList<ArrayList<String>> tab) throws FileNotFoundException
	{		
		PrintWriter zapis = new PrintWriter(path+wy);		  
		for (int i=0; i<tab.size();i++)
		{
			for (int j=0; j<tab.get(i).size(); j++)
				zapis.print(tab.get(i).get(j)+";");
			zapis.println();
		}
		zapis.close();
	}

	 protected void parsujt(String content)
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
            //1005;0006;100;242;2015-02-03
             } catch (Exception e)
             {
                     e.printStackTrace();
             }


     }

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
					
					System.out.println(content);
					
//"ID_karty";"PIN";"Ostatnia_transakcja_kwota";"Nieudane_autoryzacje";"Licznik_bezgotówkowych";"Saldo";"Wlasciciel";
//id pin kwota ost_kwota czas		
			
					int tokens = st.countTokens();
					if (tokens>0)	{	setIDkarty(st.nextToken());
						if  (tokens>1) {	setPIN(st.nextToken());
						if  (tokens>2) {	setKwota(Double.parseDouble(st.nextToken()));
						if  (tokens>3) {	setPoprzednia_kwota(Double.parseDouble(st.nextToken()));
						if  (tokens>4) {	setCzas(Long.parseLong(st.nextToken()));						
						}
						}
						}
						}
					else
						System.out.println("B³êna treœæ wiadomoœci!");
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("serial")
	public void setup()
	{
		wy=we;
		// otworz plik
//		String path = ("C:/Users/luky/Desktop/");


		// wczytaj kolejne linie do tablicy
		dataArray = FileLoader.loadFile(path+we);

		System.out.println("Baza wiedzy: Wczytano: " + (dataArray.size()-1) + " rekordów.");
		System.out.println();


//		wypisywanie zawartosci dataArray
//		for (int nr = 1; nr < dataArray.size(); nr++)
//		{
//			System.out.println("Zdarzenie nr " + nr + ":");
//			for (int i = 0; i < dataArray.get(nr).size(); i++)
//				System.out.print(dataArray.get(nr).get(i) + "|");
//			System.out.println();
//		}
//		System.out.println();

		// ID_karty|PIN|Ostatnia_transakcja_kwota|Nieudane_autoryzacje|Saldo|Wlasciciel|

		addBehaviour(new Behaviour()
		{
			@Override
			public void action()
			{

				// otrzymujemy aclm
				// wiadomosci typu: "Id_karty;Kwota;Poprzednia_kwota;PIN" //args
				// 4
				// 1008;100;0;0009
				// sprawdzamy czy content wiadomosci zgadza sie z ponizszymi
				// wartosciami
				// agree() , zeruj licznik, odejmij saldo
				// reject(), inkrementuj licznik nieautoryzowanych
			
				MessageTemplate mt;
				mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg_rec = receive(mt);
				if (msg_rec != null
						&& msg_rec.getOntology().equals("Bankomat"))
				{
					l_otrz++;
					// wiadomosci typu: "Id_karty;kwota;Poprzednia_kwota;PIN"
					parsuj(msg_rec.getContent());

//					System.out.println("Odebrano wiadomoœæ od: "
//							+ msg_rec.getSender());

					int rekord = 0;
					for (int i = 1; i < dataArray.size(); i++)
						if (dataArray.get(i).contains(IDkarty))
						{
							rekord = i;
//							System.out.println("Zdarzenie o polu Id: "
//									+ IDkarty + " ma numer: " + rekord);
						}

					ACLMessage reply = new ACLMessage(ACLMessage.DISCONFIRM);
					if (rekord == 0)
						setWiadomosc("Nie znaleziono podanego ID karty!");
					else
					{
						// tID, tPIN, tOSTATNIA, tNIEAUTORYZOWANE,
						// tBEZGOTOWKOWE, tSALDO, tWLASCICIEL;
						settID(dataArray.get(rekord).get(0));
						settPIN(dataArray.get(rekord).get(1));
						settOSTATNIA(Double.parseDouble(dataArray.get(rekord)
								.get(2)));
						settNIEAUTORYZOWANE(Integer.parseInt(dataArray.get(
								rekord).get(3)));
						settBEZGOTOWKOWE(Integer.parseInt(dataArray.get(rekord)
								.get(4)));
						settSALDO(Double.parseDouble(dataArray.get(rekord).get(5)));
						settWLASCICIEL(dataArray.get(rekord).get(6));


						// if licznik<3
						if (gettNIEAUTORYZOWANE() > 2)
						{
							System.out.println("Konto zablokowane!");
							setWiadomosc("Karta o ID = "+gettID()+" zosta³a zablokowana. Trzykrotnie wpisano b³êdny kod PIN.");
							l_blednych++;
						}
						else
						{
							if (Kwota < 50 && gettBEZGOTOWKOWE()<10)
								{
								settBEZGOTOWKOWE(gettBEZGOTOWKOWE() + 1);
								setWiadomosc("Transakcja poni¿ej 50 z³.");
								l_poprawnych++;
								}
//							else if (gettBEZGOTOWKOWE() > 9 && gettPIN().equals(null))
//								setWiadomosc("Nale¿y autoryzowaæ siê za pomoc¹ kodu PIN, spróboj ponownie.");
							// if pin!=pin_wzor
							else if (!gettPIN().equals((dataArray.get(rekord).get(1))))
							{							
								settNIEAUTORYZOWANE(gettNIEAUTORYZOWANE() + 1);
								setWiadomosc("B³êdny kod PIN! Pozosta³o "+(3 - gettNIEAUTORYZOWANE())+" prob.");
								l_blednych++;
							} else if (getPoprzednia_kwota() != gettOSTATNIA())
								{
									setWiadomosc("Poprzednia transakcja b³êdna, wykryto próbê oszustwa!");
									l_blednych++;
								}
								else if (Kwota > tSALDO)
								{
									// if kwota<saldo
									settBEZGOTOWKOWE(0);
									settNIEAUTORYZOWANE(0);
									setWiadomosc("Brak œrodków na koncie.");
									l_blednych++;
								} 
	
									else  // if ost_transakcja sie zgadza		
									{
										// agree() 
										reply.setPerformative(ACLMessage.AGREE);
										// odejmij saldo
										settSALDO(gettSALDO() - getKwota());
										//ostatnia transakcja
										settOSTATNIA(getKwota());
										settBEZGOTOWKOWE(0);
										settNIEAUTORYZOWANE(0);

										System.out.println("Wyplacono pieniadze.");
										l_poprawnych++;
										setWiadomosc("Wyplacono "+getKwota()+"z³. Saldo rachunku: "+gettSALDO());
										
										
										
			
									}

								}
							}
						
					reply.setContent(getWiadomosc());
					reply.addReceiver(msg_rec.getSender());  
					send(reply);


							dataArray.get(rekord).set(2, String.valueOf(gettOSTATNIA()));
							dataArray.get(rekord).set(3, Integer.toString(gettNIEAUTORYZOWANE()));
							dataArray.get(rekord).set(5, String.valueOf(gettSALDO()));
							
							try
							{
								FileWritter(dataArray);
							} catch (FileNotFoundException e)
							{
								e.printStackTrace();
							}
						}		


				else if (msg_rec != null && msg_rec.getOntology().equals("Terminal"))
				{
					parsujt(msg_rec.getContent());
					
					t_otrz++;
					setWiadomosc("");
					
					int rekord = 0;
					for (int i = 1; i < dataArray.size(); i++)
						if (dataArray.get(i).contains(IDkarty))
						{
							rekord = i;
						}

					ACLMessage reply = new ACLMessage(ACLMessage.DISCONFIRM);
					if (rekord == 0)
						setWiadomosc("Nie znaleziono podanego ID karty!");
					else
					{
						settID(dataArray.get(rekord).get(0));
						settPIN(dataArray.get(rekord).get(1));
						settNIEAUTORYZOWANE(Integer.parseInt(dataArray.get(
								rekord).get(3)));
						settBEZGOTOWKOWE(Integer.parseInt(dataArray.get(rekord)
								.get(4)));
						settSALDO(Double.parseDouble(dataArray.get(rekord).get(5)));
						settWLASCICIEL(dataArray.get(rekord).get(6));
						settCVC2(dataArray.get(rekord).get(7));
						setTdata_waznosci(Date.valueOf(dataArray.get(rekord).get(8)));

						if(!getWlasciciel().equals(gettWLASCICIEL()))
							setWiadomosc("Bledny wlasciciel! ");
						if(!getCVC2().equals(gettCVC2()))		
						{
							setWiadomosc(getWiadomosc()+"Bledny kod CVC2! ");
							System.out.println(getCVC2()+"   |   "+gettCVC2() + "   "+getCVC2().equals(gettCVC2()));
						}
						if(!getData_waznosci().equals(getTdata_waznosci()))
							setWiadomosc(getWiadomosc()+"Nie zgadza siê data waznosci! ");
								
						if (!getWiadomosc().equals(""))
						{
							settNIEAUTORYZOWANE(gettNIEAUTORYZOWANE() + 1);
							t_blednych++;
						}
						else
							{
						if (tNIEAUTORYZOWANE > 2)
						{
							System.out.println("Konto zablokowane!");
							setWiadomosc("Karta o ID = "+gettID()+" zosta³a zablokowana. Trzykrotnie wpisano b³êdny kod PIN.");
							t_blednych++;
						}
						else
						{
							if (Kwota < 50 && gettBEZGOTOWKOWE()<10)
								{
								settBEZGOTOWKOWE(gettBEZGOTOWKOWE() + 1);
								setWiadomosc("Transakcja poni¿ej 50 z³.");
								reply.setPerformative(ACLMessage.AGREE);
								settSALDO(gettSALDO() - getKwota());
								setWiadomosc(getWiadomosc()+" Wyp³acono "+gettSALDO()+" zl.");								
								t_poprawnych++;
								}
							else if (gettBEZGOTOWKOWE() > 9 && gettPIN().equals(" "))
							{
								setWiadomosc("Nale¿y autoryzowaæ siê za pomoc¹ kodu PIN, spróboj ponownie.");
								t_blednych++;
							}
							// if pin!=pin_wzor
							else if (!gettPIN().equals((dataArray.get(rekord).get(1))))
							{							
								settNIEAUTORYZOWANE(gettNIEAUTORYZOWANE() + 1);
								setWiadomosc("B³êdny kod PIN! Pozosta³o "+(3 - gettNIEAUTORYZOWANE())+" prob.");
								t_blednych++;
							}
								else if (Kwota > tSALDO)
								{
									// if kwota<saldo
									settBEZGOTOWKOWE(0);
									settNIEAUTORYZOWANE(0);
									setWiadomosc("Brak œrodków na koncie.");
									t_blednych++;
								} 
	
									else  // if ost_transakcja sie zgadza		
									{
										// agree() 
										reply.setPerformative(ACLMessage.AGREE);
										// odejmij saldo
										settSALDO(gettSALDO() - getKwota());
										//ostatnia transakcja
										settOSTATNIA(getKwota());
										settBEZGOTOWKOWE(0);
										settNIEAUTORYZOWANE(0);

										System.out.println("Wyplacono pieniadze.");
										t_poprawnych++;
										setWiadomosc("Wyplacono "+getKwota()+"z³. Saldo rachunku: "+gettSALDO());
										
										
										
			
									}
							}

								}
							}
						
					reply.setContent(getWiadomosc());
					reply.addReceiver(msg_rec.getSender());  
					send(reply);

							dataArray.get(rekord).set(3, Integer.toString(gettNIEAUTORYZOWANE()));
							dataArray.get(rekord).set(4, Integer.toString(gettBEZGOTOWKOWE()));
							dataArray.get(rekord).set(5, String.valueOf(gettSALDO()));
					
							try
							{
								FileWritter(dataArray);
							} catch (FileNotFoundException e)
							{
								e.printStackTrace();
							}
					
					
					
					
					
				}
				
				}
			
	

			@Override
			public boolean done()
			{
				return false;
			}
		});
		
		addBehaviour(new TickerBehaviour(null, 60000)
		{
			
			@Override
			protected void onTick()
			{
			System.out.println();
			System.out.println("---------------SERWER-------------------");
			System.out.println("Wszystkie\tPoprawne\tBledy");
			System.out.println("   "+l_otrz+"\t\t  "+l_poprawnych+"\t          "+l_blednych+"\t<-Bankomaty");
			System.out.println("   "+t_otrz+"\t\t  "+t_poprawnych+"\t          "+t_blednych+"\t<-Terminale");
			System.out.println();				
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

	public Long getCzas()
	{
		return Czas;
	}

	public void setCzas(Long czas)
	{
		Czas = czas;
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

	public String gettWLASCICIEL()
	{
		return tWLASCICIEL;
	}

	public void settWLASCICIEL(String tWLASCICIEL)
	{
		this.tWLASCICIEL = tWLASCICIEL;
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

	public double gettOSTATNIA()
	{
		return tOSTATNIA;
	}

	public void settOSTATNIA(double tOSTATNIA)
	{
		this.tOSTATNIA = tOSTATNIA;
	}

	public double gettSALDO()
	{
		return tSALDO;
	}

	public void settSALDO(double tSALDO)
	{
		this.tSALDO = tSALDO;
	}

	public String getWlasciciel()
	{
		return Wlasciciel;
	}

	public void setWlasciciel(String wlasciciel)
	{
		Wlasciciel = wlasciciel;
	}

	public String gettCVC2()
	{
		return tCVC2;
	}

	public void settCVC2(String tCVC2)
	{
		this.tCVC2 = tCVC2;
	}

	public Date getTdata_waznosci()
	{
		return tdata_waznosci;
	}

	public void setTdata_waznosci(Date tdata_waznosci)
	{
		this.tdata_waznosci = tdata_waznosci;
	}


	

}
