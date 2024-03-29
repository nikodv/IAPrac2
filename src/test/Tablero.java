package test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nikolay.dimitrov@estudiants.urv.cat
 */
public class Tablero implements Cloneable {

	private List<Ficha> fichasTablero;
	private int lado1, lado2;
	private Jugador jugador1, jugador2;
	private Jugador tornActual;
	private Ficha lastMove = null;
	private int lastSide;	
	
	
	// Used for minimax and alpha-beta pruning
	private int minimaxValue;
	List<Tablero> successorsMinimax;
	
	
	
	/**
	 * @param jugador1 - Un dels jugadors de la partida.
	 * @param jugador2 - L'altre jugador de la partida.
	 */
	public Tablero(Jugador jugador1, Jugador jugador2) {
		this.jugador1 = jugador1;
		this.jugador2 = jugador2;
		this.fichasTablero = new ArrayList<Ficha>();
		if ((this.jugador1 != null) && (this.jugador2 != null)) {
			this.tornActual = getFirstPlayer();
			this.tornActual.setPossiblesTiradas(this.tornActual.getFichasJugador());
		}		
	}
	
	
	
	/**
	 * Mètode per afegir una fitxa al taulell sense importar el costat, on es pugui.
	 * @param ficha - Fitxa que vols afegir.
	 */
	public void addFicha(Ficha ficha) {
		
		if (this.lado2 != ficha.getNum1() && this.lado2 == ficha.getNum2()) {
			ficha.flip();
			this.fichasTablero.add(ficha);
			this.lastSide = 1;
		} else if (this.lado2 == ficha.getNum1()) {
			this.fichasTablero.add(ficha);
			this.lastSide = 1;
		} else if (this.lado1 != ficha.getNum2() && this.lado1 == ficha.getNum1()) {
			ficha.flip();
			this.fichasTablero.add(0, ficha);
			this.lastSide = 0;
		} else {
			this.fichasTablero.add(0, ficha);
			this.lastSide = 0;
		}		
		
		this.lado1 = this.fichasTablero.get(0).getNum1();
		this.lado2 = this.fichasTablero.get(fichasTablero.size()-1).getNum2();	
		this.tornActual.getFichasJugador().remove(ficha);
		this.tornActual.setPossiblesTiradas(this.lado1, this.lado2);
		this.lastMove = ficha;
	}
	
	
	
	/**
	 * Mètode per afegir una fitxa amb el costat explícit.
	 * @param ficha - Fitxa que vols afegir.
	 * @param costat - Costat del taulell (0 = esquerra, 1 = dreta)
	 */
	public void addFicha(Ficha ficha, int costat) {
		if (costat == 0) {
			this.fichasTablero.add(0, ficha);
			this.lastSide = 0;
			if (this.lado1 != ficha.getNum2()) ficha.flip();
		}
		else {
			this.fichasTablero.add(ficha);
			this.lastSide = 1;
			if (this.lado2 != ficha.getNum1()) ficha.flip();
		}
		
		this.lado1 = this.fichasTablero.get(0).getNum1();
		this.lado2 = this.fichasTablero.get(fichasTablero.size()-1).getNum2();				
		this.tornActual.getFichasJugador().remove(ficha);
		this.tornActual.setPossiblesTiradas(ficha.getNum1(), ficha.getNum2());
		this.lastMove = ficha;
	}
	
	
	
	/**
	 * Mètode que intercanvia la variable tornActual cada cop que es crida.
	 * Es crida per canviar de jugador cada cop que un jugador acabi el seu torn.
	 */
	public void swapPlayer() {
		if (this.tornActual.equals(jugador1)) {
			setTornActual(this.jugador2);
		} else {
			setTornActual(this.jugador1);
		}
	}
	

	
	/**
	 * Mètode que comprova si hi ha un guanyador de la partida en aquest moment.
	 * Mira si a algún jugador ja no li quedin fitxes a la mà o que els dos jugadors a la vegada
	 * no tinguin possibles tirades a fer.
	 * @return true si hi ha jugador, false si no hi ha
	 */
	public boolean hasWinner() {
		boolean retorn = false;
		
		if (this.jugador1.getFichasJugador().isEmpty()) retorn = true;
		else if (this.jugador2.getFichasJugador().isEmpty()) retorn = true;
		else if ((this.jugador1.getPossiblesTiradas().isEmpty()) && (this.jugador2.getPossiblesTiradas().isEmpty())) retorn = true;		
		
		return retorn;
	}
	
	
	
	/**
	 * Es utilitzada per l'algoritme Minimax o Poda Alfa-Beta.
	 * Serveix per generar els fills que pot tenir un node que representa la instància actual
	 * del taulell.
	 */
	public void setSuccessors() {
		List<Tablero> aux = new ArrayList<Tablero>();
		Tablero tableroAux = null;
		
		for (Ficha ficha : this.tornActual.getPossiblesTiradas()) {
			if (ficha.canBeBothSides(this.lado1, this.lado2)) {
				try {
					tableroAux = (Tablero) this.clone();
					tableroAux.addFicha(ficha, 0);
					aux.add(tableroAux);
					tableroAux = (Tablero) this.clone();
					tableroAux.addFicha(ficha, 1);
					aux.add(tableroAux);
				} catch(CloneNotSupportedException c){}
			} else {
				try {
					tableroAux = (Tablero) this.clone();
					tableroAux.addFicha(ficha);
					aux.add(tableroAux);
				} catch(CloneNotSupportedException c){}
			}
		}
		
		this.successorsMinimax = aux;		
	}	
	
	
	
	/**
	 * @return String amb el guanyador de la partida amb el seu nom. Si és empat afegeix al String retornat
	 * amb quants punts ha guanyat.
	 */
	public String whoIsWinner() {
		String retorn = "\n";
		
		if (this.jugador1.getFichasJugador().isEmpty()) retorn = retorn + "WINNER: " + this.jugador1;
		else if (this.jugador2.getFichasJugador().isEmpty()) retorn = retorn + "WINNER: " + this.jugador2;
		else if ((this.jugador1.getPossiblesTiradas().isEmpty()) && (this.jugador2.getPossiblesTiradas().isEmpty())) {
			if (this.jugador1.sumPoints() <= this.jugador2.sumPoints()) {
				retorn = retorn + "WINNER: " + this.jugador1 + " amb " + this.jugador1.sumPoints() + " punts.";
			} else {
				retorn = retorn + "WINNER: " + this.jugador2 + " amb " + this.jugador2.sumPoints() + " punts.";
			}
		}	
		
		return retorn;	
	}
	
	
	
	/**
	 * Setter del tornActual del taulell i que també actualitza la llista de possibles tirades del jugador.
	 * @param tornActual - Jugador passat per paràmetre
	 */
	public void setTornActual(Jugador tornActual) {
		this.tornActual = tornActual;
		this.tornActual.setPossiblesTiradas(lado1, lado2);
	}
	
	
	
	/**
	 * GETTERS I SETTERS
	 */
	
	public List<Tablero> getSuccessorsMinimax() {
		return successorsMinimax;
	}
	
	public Jugador getFirstPlayer() {
		return this.jugador1.hasFicha(new Ficha(6,6)) ? this.jugador1 : this.jugador2;
	}	
	
	public Jugador getTornActual() {
		return tornActual;
	}

	public List<Ficha> getFichasTablero() {
		return fichasTablero;
	}

	public Ficha getLastMove() {
		return lastMove;
	}	

	public int getLastSide() {
		return lastSide;
	}

	public void setLastMove(Ficha lastMove) {
		this.lastMove = lastMove;
	}	

	public void setFichasTablero(List<Ficha> fichasTablero) {
		this.fichasTablero = fichasTablero;
	}

	public void setLado1(int lado1) {
		this.lado1 = lado1;
	}

	public void setLado2(int lado2) {
		this.lado2 = lado2;
	}

	public void setJugador1(Jugador jugador1) {
		this.jugador1 = jugador1;
	}

	public void setJugador2(Jugador jugador2) {
		this.jugador2 = jugador2;
	}

	public void setLastSide(int lastSide) {
		this.lastSide = lastSide;
	}

	public void setSuccessorsMinimax(List<Tablero> successorsMinimax) {
		this.successorsMinimax = successorsMinimax;
	}

	public int getLado1() {
		return lado1;
	}

	public int getLado2() {
		return lado2;
	}	

	public int getMinimaxValue() {
		return minimaxValue;
	}
	
	public void setMinimaxValue(int minimaxValue) {
		this.minimaxValue = minimaxValue;
	}


	
	
	
	@Override
	public String toString() {
		String cadena = "";
		int possiblesCounter = 0;
		
		cadena = cadena + new String(new char[(this.jugador1 + "     ").length()+3]).replace("\0", " ");
		
		if(this.tornActual.equals(jugador1)) {
			for (int i=0; i<this.jugador1.getFichasJugador().size(); i++) { 
				if (jugador1.getPossiblesTiradas().contains(this.jugador1.getFichasJugador().get(i))) cadena = possiblesCounter>9 ? cadena + possiblesCounter++ + "     " : cadena + possiblesCounter++ + "      ";
				else cadena = possiblesCounter>9 ? cadena + "       " : cadena + "       ";
			}
		}
		
		possiblesCounter = 0;
		
		cadena = cadena + "\n" + this.jugador1 + "     " + this.jugador1.getFichasJugador() + "\n\n";
		cadena = cadena + "=================================================================================================================================================================================\n";
		cadena = cadena + this.fichasTablero + "\n";
		cadena = cadena + "=================================================================================================================================================================================\n\n";
		cadena = cadena + this.jugador2 + "     " + this.jugador2.getFichasJugador() + "\n";
		
		cadena = cadena + new String(new char[(this.jugador2 + "     ").length()+3]).replace("\0", " ");
		
		if(this.tornActual.equals(jugador2)) {
			for (int i=0; i<this.jugador2.getFichasJugador().size(); i++) { 
				if (jugador2.getPossiblesTiradas().contains(this.jugador2.getFichasJugador().get(i))) cadena = possiblesCounter>9 ? cadena + possiblesCounter++ + "     " : cadena + possiblesCounter++ + "      ";
				else cadena = possiblesCounter>9 ? cadena + "       " : cadena + "       ";
			}
		}
		
		return cadena;
	}
	
	
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		
		Tablero cloned = (Tablero) super.clone();
		
		List<Ficha> newLista = new ArrayList<Ficha>(this.fichasTablero);
		cloned.setFichasTablero(newLista);
		
		
		if (this.lastMove != null) {
			Ficha lastMove = (Ficha)this.lastMove.clone();
			cloned.setLastMove(lastMove);
		} else cloned.setLastMove(null);		
		
		if (this.tornActual.equals(this.jugador2)) {
			Jugador newJugador2 = (Jugador)this.jugador2.clone();;
			cloned.setTornActual(newJugador2);
		} else {
			Jugador newJugador1 = (Jugador)this.jugador1.clone();
			cloned.setTornActual(newJugador1);
		}
		
		return cloned;
	}		
}