import java.util.Observable;


public abstract class SelectableObject extends Observable{
	/** the variable used to indicate whether this object is available to be selected*/
	private boolean isSelectable;
	
	public SelectableObject()
	{
		isSelectable = false;
	}
	
	/**
	 * set the isSelectable attribute to indicate whether this object can be selected or not
	 * @param isSelectable true if this object is available to be selected, false otherwise
	 */
	public void setSelectable(boolean isSelectable)
	{
		this.isSelectable = isSelectable;
		setChanged();
		notifyObservers(isSelectable);
	}	
	/**
	 * get whether this object can be selected or not
	 * @return true if this object is available to be selected, false otherwise
	 */
	public boolean isSelectable()
	{
		return isSelectable;
	}
}
