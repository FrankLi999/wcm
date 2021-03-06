package com.bpwizard.wcm.repo.rest.jcr.model;

public class Footer {
//	public enum Position {
//		above("above"),
//		above_static("above-static"),
//		above_fixed("above-fixed"),
//		below("below"),
//		below_static("below-static"),
//		below_fixed("below-fixed");
//		
//		private final String position;
//		private Position(String position) {
//			this.position = position;
//		}
//		
//		public String getPosition() {
//	        return this.position;
//	    }
//	}
	private boolean customBackgroundColor;
	private String background;
	private boolean display;
	private String position;
	private String style;
	public boolean isCustomBackgroundColor() {
		return customBackgroundColor;
	}
	public void setCustomBackgroundColor(boolean customBackgroundColor) {
		this.customBackgroundColor = customBackgroundColor;
	}
	public String getBackground() {
		return background;
	}
	public void setBackground(String background) {
		this.background = background;
	}
	public boolean isDisplay() {
		return display;
	}
	public void setDisplay(boolean display) {
		this.display = display;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	@Override
	public String toString() {
		return "Footer [customBackgroundColor=" + customBackgroundColor + ", background=" + background + ", display="
				+ display + ", position=" + position + ", style=" + style + "]";
	}
}
