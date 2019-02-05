import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public abstract class SelectablePanel extends JPanel implements MouseListener{
	/** the variable used to indicate whether this object is available to be selected*/
	private boolean isSelectable;
	
	
	public final LineBorder BORDER_SELECTABLE = new LineBorder(new Color(30, 196, 34), 2);
	public final LineBorder BORDER_HOVERED = new LineBorder(new Color(152, 239, 154), 2);
	public final LineBorder BORDER_CLICKED = new LineBorder(new Color(19, 128, 22), 2);	
	public final Border BORDER_NORMAL = new LineBorder(Color.BLACK, 1);
	
	public SelectablePanel()
	{
		setBorder(BORDER_NORMAL);
		setSelectable(false);	
		addMouseListener(this);
	}
		
	/**
	 * set the isSelectable attribute to indicate whether this object can be selected or not
	 * @param isSelectable true if this object is available to be selected, false otherwise
	 */
	public void setSelectable(boolean isSelectable)
	{
		this.isSelectable = isSelectable;
		if(isSelectable)
			setBorder(BORDER_SELECTABLE);
		else
			setBorder(BORDER_NORMAL);		
	}	
	/**
	 * get whether this object can be selected or not
	 * @return true if this object is available to be selected, false otherwise
	 */
	public boolean isSelectable()
	{
		return isSelectable;
	}
	
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(isSelectable())
		{
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			this.setBorder(BORDER_HOVERED);
		}
		else
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(isSelectable())
		{
			this.setCursor(Cursor.getDefaultCursor());
			this.setBorder(BORDER_SELECTABLE);
		}
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(isSelectable())
			this.setBorder(BORDER_CLICKED);					
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(isSelectable())
		{
			this.setBorder(BORDER_HOVERED);		
		}
	}
}
