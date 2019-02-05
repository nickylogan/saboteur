import java.util.ArrayList;

public class HandCard {
	
	private int index;
	private CardPlayable card;
	private double value;
	
	public HandCard(int index, CardPlayable card) {
		super();
		this.index = index;
		this.card = card;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public CardPlayable getCard() {
		return card;
	}

	public void setCard(CardPlayable card) {
		this.card = card;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
}
