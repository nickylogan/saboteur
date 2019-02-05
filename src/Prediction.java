import java.util.ArrayList;

public class Prediction implements Comparable<Prediction>{	
	private Player otherPlayer;
	private double score;
	private double prediction;
	
	/**
	 * 
	 * @param otherPlayer
	 */
	public Prediction(Player otherPlayer)
	{
		this.otherPlayer = otherPlayer;
		score = GameModel.c1;
		prediction = 1;
	}
	
	/**
	 * 
	 * @param increment
	 */
	public void incrementScore(double increment, double maxScore)
	{
		score += increment;
		prediction = score/maxScore;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getScore()
	{
		return score;
	}
	/**
	 * 
	 * @return
	 */
	public double prediction()
	{
		return prediction;
	}

	@Override
	/**
	 * check equality based on the person (enemy) contained by this and the given Prediction object
	 */
	public boolean equals(Object obj) 
	{
		if(obj == null)
			return false;
		else if(!(obj instanceof Prediction))
			return false;
		else
		{
			Prediction otherPrediction = (Prediction) obj;
			return otherPlayer.equals(otherPrediction.otherPlayer);
		}
					
	};
	
	@Override
	/**
	 * Implementation of Comparable interface by using the score as the comparison basis 
	 */
	public int compareTo(Prediction arg0) {
		// TODO Auto-generated method stub
		if(this.score < arg0.score)
			return -1;
		else if(this.score > arg0.score)
			return 1;
		else
			return 0;		
	}
	
	
}
