package geogebra.web.javax.swing;

import geogebra.common.awt.GPoint;
import geogebra.html5.css.GuiResources;
import geogebra.web.euclidian.EuclidianControllerW;
import geogebra.web.html5.AttachedToDOM;
import geogebra.web.main.AppW;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Popup menu for web.
 * @author Judit Elias
 */
public class GPopupMenuW extends geogebra.common.javax.swing.GPopupMenu implements AttachedToDOM{


	protected PopupPanel popupPanel;
	protected PopupMenuBar popupMenu;
	private int popupMenuSize = 0;
	/* popup panel for submenu
	 * this field used to avoid having more submenu at the same time 
	 */
	GPopupMenuW subPopup;
	private AppW application;

	/**
	 * @param app
	 * 
	 * Creates a popup menu. App needed for get environment style
	 */
	public GPopupMenuW(AppW app){
		application = app;
		popupPanel = new PopupPanel();		
		popupMenu = new PopupMenuBar(true);
		popupMenu.setAutoOpen(true);
		popupPanel.add(popupMenu);
		
		popupPanel.addCloseHandler(new CloseHandler<PopupPanel>(){

			public void onClose(CloseEvent<PopupPanel> event) {
				if (subPopup != null){
					subPopup.removeFromDOM();
					subPopup = null;
				}
            }
			
		});
		
		popupPanel.setAutoHideEnabled(true);
	}
	
	/*
	 * Constructor for submenu-popups
	 */
	public GPopupMenuW(MenuBar mb){
		popupPanel = new PopupPanel();
		popupPanel.add(mb);
	}
	
//	public void add(MenuItem mi) {
//	    impl.addItem(mi);
//	    
//    }
	
	public void setVisible(boolean v){
		popupPanel.setVisible(v);
	}
	
	/**
	 * Shows the popup menu, ensures that the popup menu must be on the client area.
	 */
	public void show(GPoint p){
		int top = p.getY();
		int left = p.getX();
		boolean newPoz = false;
		showAtPoint(p);
		if (left + popupPanel.getOffsetWidth() > Window.getClientWidth() + Window.getScrollLeft()){
			left = Window.getClientWidth() - popupPanel.getOffsetWidth()+Window.getScrollLeft();
			newPoz = true;
		}
		if (top + popupPanel.getOffsetHeight() > Window.getClientHeight() + Window.getScrollTop()){
			top = Window.getClientHeight() - popupPanel.getOffsetHeight()+ Window.getScrollTop();
			newPoz = true;
		}
		left *= (int) ((EuclidianControllerW) application.getActiveEuclidianView().getEuclidianController()).getScaleXMultiplier();
		top *= (int) ((EuclidianControllerW) application.getActiveEuclidianView().getEuclidianController()).getScaleYMultiplier();
		if (newPoz) popupPanel.setPopupPosition(left, top);
	}
	
	/**
	 * Shows the popup menu at the p point, independently of there is enough
	 * place for the popup menu. (Maybe some details of the popup menu won't be
	 * visible.)
	 */
	public void showAtPoint(GPoint p){
		popupPanel.setPopupPosition(p.getX(), p.getY());
		popupPanel.show();
	}
	
	public void show(Canvas c, int x, int y) {
		show(new GPoint(c.getAbsoluteLeft()+x, c.getAbsoluteTop()+y));
	}

	public void show(Widget c, int x, int y) {
		show(new GPoint(c.getAbsoluteLeft()+x, c.getAbsoluteTop()+y));
	}

	public void removeFromDOM(){
		removeSubPopup();
		popupPanel.removeFromParent();
	}
	
	public void clearItems(){
		popupMenu.clearItems();
	}
	
	public int getComponentCount(){
		return popupMenuSize;
	}
	
	public void addSeparator(){
		popupMenu.addSeparator();
	}
	
	private void addHideCommandFor(MenuItem item){
		MenuBar submenu = item.getSubMenu();
		if (submenu==null){
			final ScheduledCommand oldCmd = item.getScheduledCommand();
			ScheduledCommand cmd = new ScheduledCommand(){
				public void execute() {
					oldCmd.execute();
					popupPanel.hide();
	            }
			};
			item.setScheduledCommand(cmd);
		} else {
//			CloseHandler<PopupPanel> closehandler = new CloseHandler<PopupPanel>(){
//				public void onClose(CloseEvent<PopupPanel> event) {
//	                App.debug("popuppanel closed");
//                }
//			}; 
//			submenu.addCloseHandler(closehandler);
//			submenu.addHandler(closehandler, CloseEvent.getType());			
			
			submenu.addHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
	                popupPanel.hide();
                }
			}, ClickEvent.getType());
		}		
	}
	
	private ImageResource getSubMenuIcon(boolean isRTL){
		return isRTL ? GuiResources.INSTANCE.menuBarSubMenuIconRTL() : GuiResources.INSTANCE.menuBarSubMenuIconLTR();
	}
	
//	public void addItem(final MenuItem item) {
//		addHideCommandFor(item);
//	    popupMenu.addItem(item);
//	    popupMenuSize++;
//    }
	
	public void addItem(final MenuItem item) {
		final MenuBar subMenu = item.getSubMenu();
		addHideCommandFor(item);
		if (subMenu == null){
			popupMenu.addItem(item);
		}
		else {
			// The submenu is not added for the menu as submenu,
			// but this will be placed on a different popup panel. 
			// In this way we can set this popup panel's position easily.
			String itemHTML = item.getHTML();
			ScheduledCommand itemCommand = null;
			final MenuItem newItem = new MenuItem(itemHTML, true, itemCommand);
			newItem.setStyleName(item.getStyleName());
			newItem.getElement().setAttribute("hasPopup", "true");
			popupMenu.addItem(newItem);			
			itemCommand = new ScheduledCommand(){
				public void execute() {
					int xCord, yCord;
					if (subPopup != null) subPopup.removeFromDOM();
	                subPopup = new GPopupMenuW(subMenu);
	                subPopup.setVisible(true);
	                
	                //Calculate the position of the "submenu", and show it
	                if (LocaleInfo.getCurrentLocale().isRTL()){
	                	xCord = getLeftSubPopupXCord();
	                	if (xCord < 0) xCord = getRightSubPopupXCord();
	                } else {
	                	xCord = getRightSubPopupXCord();
	                	if (xCord + getSubPopupWidth()> Window.getClientWidth()) xCord = getLeftSubPopupXCord();
	                }
	                yCord = Math.min(newItem.getAbsoluteTop(), Window.getClientHeight()-getSubPopupHeight());
	                subPopup.showAtPoint(new GPoint(xCord,yCord));
                }	
			};
			newItem.setScheduledCommand(itemCommand);
			
			//adding arrow for the menuitem
			Element td = DOM.createTD();
			DOM.setElementProperty(td, "vAlign", "middle");
			td.addClassName("subMenuIcon");
			ImageResource imgRes = getSubMenuIcon(LocaleInfo.getCurrentLocale().isRTL());
			td.setInnerSafeHtml(AbstractImagePrototype.create(imgRes).getSafeHtml());
			newItem.getElement().setAttribute("colspan", "1");
			DOM.appendChild((Element) newItem.getElement().getParentNode(), td);								
		}
	    popupMenuSize++;
	    
	    item.addStyleName("gPopupMenu_item");
    }
	
	/**
	 * @return the width of the submenu.
	 */
	public int getSubPopupWidth(){
		int width;
		boolean shown = subPopup.popupPanel.isShowing();
    	if (!shown) subPopup.popupPanel.show();
        width = subPopup.popupPanel.getOffsetWidth();
        if (!shown) subPopup.popupPanel.hide();
        return width;
	}

	/**
	 * @return the height of the submenu.
	 */
	public int getSubPopupHeight(){
		int ret;
		boolean shown = subPopup.popupPanel.isShowing();
    	if (!shown) subPopup.popupPanel.show();
        ret = subPopup.popupPanel.getOffsetHeight();
        if (!shown) subPopup.popupPanel.hide();
        return ret;
	}
	
	/**
	 * Gets the submenu's suggested absolute left position in pixels, as
	 * measured from the browser window's client area, in case of the submenu is
	 * on the left side of its parent menu.
	 * 
	 * @return submenu's left position in pixels
	 */
	public int getLeftSubPopupXCord(){
		int xCord;
        xCord = popupPanel.getAbsoluteLeft() - getSubPopupWidth();
        return xCord;	
	}
	
	/**
	 * Gets the submenu's suggested absolute left position in pixels, as
	 * measured from the browser window's client area, in case of the submenu is
	 * on the right side of its parent menu.
	 * 
	 * @return submenu's left position in pixels
	 */
	public int getRightSubPopupXCord(){
		return popupPanel.getAbsoluteLeft() + popupPanel.getOffsetWidth();
	}

	public void addItem(GCheckBoxMenuItem item) {
	    addItem(item.getMenuItem());
	    
    }
	
	public void hide(){
		popupPanel.hide();
	}

	public MenuBar getPopupMenu(){
		return popupMenu;
	}

	public PopupPanel getPopupPanel() {
		return popupPanel;
	}

	public void removeSubPopup(){
		if (subPopup != null){
			subPopup.removeFromDOM();
			subPopup = null;
		}
	}
	
	private class PopupMenuBar extends MenuBar{
		
		public PopupMenuBar(boolean vertical){
			super(vertical);
		}

		private MenuItem findItem(Element hItem) {
			for (MenuItem item : getItems()) {
				if (DOM.isOrHasChild(item.getElement(), hItem)) {
					return item;
				}
			}
			return null;
		}
		
		@Override
        public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
				case Event.ONMOUSEOVER: {
					MenuItem item = findItem(DOM.eventGetTarget(event));
					if (item != null){
						if (item.getElement().getAttribute("hasPopup") == "true"){
							item.getScheduledCommand().execute();
						} else removeSubPopup();
					}
					break;
				}
			}			
			super.onBrowserEvent(event);
		}		
	}
	

}
