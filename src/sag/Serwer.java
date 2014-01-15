package sag;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import java.util.Vector;

public class Serwer extends Agent{

	public void setup() 
	{
		
		//TODO wczytywanie z pliku, parsowanie, zapis do tablicy
		
		
		addBehaviour(new Behaviour()
		{
//if otrzymano zapytanie o autoryzacje			
//odpowiedz na zapytanie od agenta (bankomat)
//			if licznik<3
//			if pin=pin_wzor
//			if kwota<saldo
			//agree() , zeruj licznik, odejmij saldo
			//reject(), inkrementuj licznik nieautoryzowanych
			@Override
			public boolean done()
			{
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void action()
			{
				// TODO Auto-generated method stub
				
			}
		}); 
	}

}
