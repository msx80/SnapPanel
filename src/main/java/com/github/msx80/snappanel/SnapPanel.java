package com.github.msx80.snappanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

public class SnapPanel extends JPanel 
{
	private static final long serialVersionUID = 3013965427462195036L;

	private Point pinLocation;
	
	private SnapPanel linkUp;
	private SnapPanel linkDown;
	
	private boolean isDragging;
	
	private boolean targetTop;
	private boolean targetBottom;
	
	private boolean isLinkableBottom = true;
	private boolean isLinkableTop = true;
	
	private SnapListener onSnap;
	
	public SnapPanel() {
	
		
		this.setBounds(200,200, this.getPreferredSize().width, this.getPreferredSize().height);
		this.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		this.setLayout(new BorderLayout());

		this.addMouseListener(new MouseAdapter() {

        	public void mousePressed(MouseEvent e) {
            	if (e.getClickCount() == 2) {
            		
					System.out.println("Double click!");
                }
            	else
            	{
            		pinLocation = e.getPoint();
            		Container parent = SnapPanel.this.getParent();
            		if(parent instanceof JLayeredPane)
            		{
            			((JLayeredPane) parent).moveToFront(SnapPanel.this);
            		}
            	}
            }	
        	public void mouseReleased(MouseEvent e) 
        	{
        		if(isDragging)
        		{
        			doSnap();
        		}
        		pinLocation = null;
        		isDragging = false;
        		repaint();
            }
        });
        
        this.addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseDragged(MouseEvent e) 
			{
                if (pinLocation != null) {
                	SnapPanel.unlinkUp(SnapPanel.this);
                	isDragging = true;
                	
                	int dx = e.getX() - pinLocation.x;
            		int dy = e.getY() - pinLocation.y;
                	
            		
            		checkSnap();
            		
            		
                	displace(dx, dy);
                    
        
                }
            }
            
        });
	}
	
	public void setContent(Component comp)
	{
		this.removeAll();
		this.add(comp, BorderLayout.CENTER);
		this.setSize(this.getPreferredSize().width, this.getPreferredSize().height);
	}
	
	public void removeNotify()
	{
		super.removeNotify();
		SnapPanel.unlinkUp(this);
		SnapPanel.unlinkDown(this);
	}
	

	@Override
	protected void paintChildren(Graphics g)
	{
	    super.paintChildren(g);
	    
	    paintMagnets(g);
	}

	protected void paintMagnets(Graphics g) {
		if(isDragging)
	    {
	    	g.setColor(Color.RED);
	    	g.fillRect(0, 0, 10, 5);
	    	
	    	if(linkDown!=null)
	    	{
	    		g.setColor(UIManager.getColor("activeCaption"));
		    	g.fillOval(0, this.getSize().height-5, 10, 10);
	    	}
	    }
	    else
	    {
	    	// draw regular "magnets"
		    if(this.isLinkableTop)
		    {
		    	g.setColor(targetTop ? Color.RED :  linkUp==null ? Color.GRAY : UIManager.getColor("activeCaption"));
		    	g.fillOval(0, -5, 10, 10);
		    }
		    
		    if(this.isLinkableBottom)
		    {
		    	g.setColor(targetBottom ? Color.RED :  linkDown==null ? Color.GRAY : UIManager.getColor("activeCaption"));
		    	g.fillOval(0, this.getSize().height-5, 10, 10);
		    }
	    }
	}
		
	
	protected void doSnap() {
		
		Point thisLoc = this.getLocation();
		
		// prioritize top linking
		for (SnapPanel other : unlinkedSibs()) 
		{
			Dimension otherDim = other.getSize();
			Point otherLoc = other.getLocation();

			
			// top
			if(this.isLinkableTop && other.isLinkableBottom &&
					smallDiff(otherLoc.y + otherDim.height, thisLoc.y) &&
					smallDiff(otherLoc.x,  thisLoc.x))
			{
				doLinkUp(other);
				
				// after a single snap return, don't do more than one
				return;
			}
			
		}
		
		// bottom linking
		for (SnapPanel other : unlinkedSibs()) 
		{
			Point otherLoc = other.getLocation();
			
			// top
			if(other.isLinkableTop && this.isLinkableBottom &&
					smallDiff(otherLoc.y , thisLoc.y) &&
					smallDiff(otherLoc.x,  thisLoc.x))
			{
				doLinkDown(other);
				
				// after a single snap return, don't do more than one
				return;
			}
			
		}
	}

	private void doLinkDown(SnapPanel other) 
	{
		other.setTargetTop(false);
		SnapPanel oldUp = other.linkUp;
		unlinkUp(other);
		
		link(this.getBottomOfChain(), other);
		if(oldUp!=null)
		{
			oldUp.setTargetBottom(false);
			tryLink(oldUp, this.getTopOfChain());
		}
		this.getTopOfChain().adjustDownChain();
	}

	private void doLinkUp(SnapPanel other) 
	{
		other.setTargetBottom(false);
		SnapPanel oldDown = other.linkDown;
		unlinkDown(other);
		
		link(other, this);
		if(oldDown!=null)
		{
			oldDown.setTargetTop(false);
			tryLink(this.getBottomOfChain(), oldDown);
		}
		this.getTopOfChain().adjustDownChain();
	}

	private static void unlinkUp(SnapPanel s)
	{
		if(s.linkUp != null)
		{
			assert(s.linkUp.linkDown == s);
			s.linkUp.linkDown = null;
			SnapPanel old = s.linkUp;
			s.linkUp = null;
			
			callUnlink(old, s);
			
		}
	}
	private static void unlinkDown(SnapPanel s)
	{
		if(s.linkDown != null)
		{
			assert(s.linkDown.linkUp == s);
			s.linkDown.linkUp = null;
			SnapPanel old = s.linkDown;
			s.linkDown = null;
			
			callUnlink(s, old);
		}
	}
	private static void callUnlink(SnapPanel high, SnapPanel low) 
	{
		if(high.onSnap != null)
		{
			high.onSnap.unlinked(high, low);
		} else if (low.onSnap != null)
		{
			low.onSnap.unlinked(high, low);
		}
	}

	private static void tryLink(SnapPanel high, SnapPanel low)
	{
		if (high.isLinkableBottom && low.isLinkableTop) {
			link(high, low);
		}
	}
	private static void link(SnapPanel high, SnapPanel low)
	{
		if (high.isLinkableBottom && low.isLinkableTop) 
		{
			if(high.linkDown != null) throw new RuntimeException("high already linked, unlink first");
			if(low.linkUp != null) throw new RuntimeException("low already linked, unlink first");
			high.linkDown = low;
			low.linkUp = high;
			high.repaint();
			low.repaint();
			
			if(high.onSnap != null)
			{
				high.onSnap.snapped(high, low);
			} else if (low.onSnap != null)
			{
				low.onSnap.snapped(high, low);
			}
		}
		else
		{
			throw new RuntimeException("Unlinkable panels!");
		}
	}
	
	private void adjustDownChain() 
	{
		if(linkDown != null)
		{
			linkDown.setLocation (this.getLocation().x, this.getLocation().y+this.getSize().height);
			linkDown.adjustDownChain();
		}
	}
	
	public SnapPanel getBottomOfChain()
	{
		if(this.linkDown == null) return this;
		return this.linkDown.getBottomOfChain();
	}
	
	public SnapPanel getTopOfChain()
	{
		if(this.linkUp == null) return this;
		return this.linkUp.getTopOfChain();
	}
	
	/**
	 * Attach this panel below another panel.
	 * This panel will be unlinked upwards if it's already linked
	 * then moved under the other panel and connected. All panels
	 * linked at the bottom of this panel will be moved as well.
	 *  
	 * @param other
	 */
	public void snapUp(SnapPanel other)
	{
		unlinkUp(this);
		doLinkUp(other);
	}

	/**
	 * Attach a panel below this one.
	 * This panel will be unlinked downwards if it's already linked
	 * then the other panel will be moved under this one and connected. All the
	 * rest of the chain will be moved as well.
	 *  
	 * @param other
	 */
	public void snapDown(SnapPanel other)
	{
		unlinkDown(this);
		doLinkDown(other);
	}


	protected void checkSnap() 
	{
		Point thisLoc = this.getLocation();

		for (SnapPanel other : unlinkedSibs()) 
		{
			Dimension otherDim = other.getSize();
			Point otherLoc = other.getLocation();

			
			// bottom
			
			if( other.isLinkableTop && this.isLinkableBottom &&
					smallDiff(otherLoc.y, thisLoc.y ) &&
					smallDiff(otherLoc.x,  thisLoc.x))
			{
				other.setTargetTop(true);
			}
			else
			{
				other.setTargetTop(false);
			}
			
			
			// top
			
			if(other.isLinkableBottom && this.isLinkableTop &&
					smallDiff(otherLoc.y + otherDim.height, thisLoc.y) &&
					smallDiff(otherLoc.x,  thisLoc.x))
			{
				other.setTargetBottom(true);
			}
			else
			{
				other.setTargetBottom(false);
			}
			
		}
	}
	
	private void setTargetTop(boolean b) {
		this.targetTop = b;
		this.repaint();
		
	}
	private void setTargetBottom(boolean b) {
		this.targetBottom = b;
		this.repaint();
		
	}

	private boolean isLinkedUp(SnapPanel other)
	{
		if(linkUp == null) return false;
		if(linkUp == other) return true;
		return linkUp.isLinkedUp(other);
	}
	
	private boolean isLinkedDown(SnapPanel other)
	{
		if(linkDown == null) return false;
		if(linkDown == other) return true;
		return linkDown.isLinkedDown(other);
	}

	private boolean smallDiff(int y, int i) {
		
		return Math.abs(y-i)<20;
	}

	private void displace(int dx, int dy) 
	{
		Point oldLoc = getLocation();
		int x = oldLoc.x + dx;
		int y = oldLoc.y + dy;
		
		if(x<0) 
		{
			dx-=x;
			x = 0;
		}
		if(y<0) 
		{ 
			dy -= y;
			y = 0; 
		}
		this.setLocation(x, y);
	
        if(linkDown!= null)
        {
        	linkDown.displace(dx, dy);
        }
	}

	private Collection<SnapPanel> unlinkedSibs()
	{
		return Stream.of(this.getParent()
				.getComponents())
				.filter(a -> a instanceof SnapPanel)
				.filter(a -> a != this)
				.map(a -> (SnapPanel)a)
				.filter(a -> !this.isLinkedDown(a))
				.filter(a -> !this.isLinkedUp(a))
				.collect(Collectors.toList());
	}
	
	public boolean isLinkableBottom() {
		return isLinkableBottom;
	}

	public void setLinkableBottom(boolean isLinkableBottom) {
		this.isLinkableBottom = isLinkableBottom;
	}

	public boolean isLinkableTop() {
		return isLinkableTop;
	}

	public void setLinkableTop(boolean isLinkableTop) {
		this.isLinkableTop = isLinkableTop;
	}

	public SnapListener getOnSnap() {
		return onSnap;
	}

	public void setOnSnap(SnapListener onSnap) {
		this.onSnap = onSnap;
	}

	public SnapPanel getLinkedUp() {
		return linkUp;
	}

	public SnapPanel getlinkedDown() {
		
		return linkDown;
	}
	
	
}
