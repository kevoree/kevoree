/*****************************************************************************
  jnt.Bench.Plotter
 *****************************************************************************/
package jnt.Bench;
import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 Plotter for the Bench Package
 Draws a horizontal bar graph, with labels for each bar at the left.
 A `Special' row, in a different color, is used to highlight the measurements of
 the current system.

@author Bruce R. Miller (bruce.miller@nist.gov)
@author Contribution of the National Institute of Standards and Technology,
@author not subject to copyright.
*/

public class Plotter extends Canvas {
  String labels[];		// Labels (strings) for the bars
  double values[];		// Values (Doubles) for bar lengths
  String axisLabel;		// Label for X axis
  int specindex = -1;		// index of `special' entry (bar drawn RED)

  public Plotter() {}

  /** Set the data to be displayed by the Plotter.
    * @param labels array of label strings for each bar.
    * @param values array of values for the length of each bar.
    * @param axisLabel label for the X axis (along the bars).
    * @param specindex the index of the `Special' entry. 
    */
  public void setData(String labels[], double values[], 
		      String axisLabel, int specindex){
    this.labels = labels;
    this.values = values;
    this.axisLabel = axisLabel; 
    this.specindex = specindex;
    repaint(); }

  /* Interesting parameters */
  final int MARGIN = 5;		// Extra space at margins (pixels)
  final int TICK = 3;		// Length of tick marks (pixels)
  final int GAP  = 4;		// gap between Bars (if possible)
  final boolean MINZERO = true;	// Should minimum value be at most 0 ?
  final int FONTSIZE = 10;	// Font size (points) for labels
  Color barColor = Color.yellow; // Color of bars
  Color specColor= Color.red; // Color of the `special' bar
  Color bgColor = Color.white; // Background of graph subwindow
  Color fgColor = Color.black; // Foreground color of text, margins,etc.

  /** Set the color of the horizontal bars. */
  void setBarColor(Color c)     { barColor=c; }
  /** Set the background color of the plotter. */
  void setPlotterColor(Color c) { bgColor=c; }
  /** Set the color of the outlines of the plotter and bars. */
  void setLineColor(Color c)    { fgColor=c; }
  /** Set the color of the Special bar. */
  void setSpecialColor(Color c) { specColor=c; }

  void drawBox(Graphics g, Color c, int x0, int y0, int w, int h) {
    g.setColor(c);
    g.fillRect(x0,y0,w,h);
    g.setColor(fgColor);
    g.drawRect(x0,y0,w,h); }

  public void paint(Graphics g) {
    if ((labels == null) || (labels.length==0)) return;
    Color textcolor= getForeground();
    int n = labels.length;
    int tmp;
    /* Get Font info */
    Font font = new Font("Helvetica",Font.PLAIN,FONTSIZE);
    FontMetrics metrics = getFontMetrics(font);
    int fontH = metrics.getHeight();

    /* Prescan the data for min, max & label lengths */
    int labLen = 0;
    double min = (MINZERO ? 0.0 : Double.MAX_VALUE), 
           max = -Double.MAX_VALUE;
    for(int i=0; i<n; i++) {
      if ((tmp=metrics.stringWidth((String) labels[i]))>labLen) labLen=tmp;
      if(values[i] > max) max=values[i];
      if(values[i] < min) min=values[i]; }
    if (min==max) return;	// nothing to draw (?!?)

    /* Compute `attractively rounded' min & max. */
    // System.out.println("Range is "+min+","+max);
    double l=Math.floor(Math.log(max-min)/Math.log(10.0));
    double u=Math.pow(10.0,l);
    max = Math.ceil(max/u)*u;
    min = Math.floor(min/u)*u;
    int ll=(int)l;
    int nsteps = (int) ((max-min)/u);
    if (nsteps == 1) { u=u/10.0; ll--; }
    else if (nsteps==2) { u=u/2.0; ll--; }
    // System.out.println("Rounded to "+min+","+max);
    // System.out.println("Unit is "+u+" with scale="+l);

    /* Get size of Plotter, leaving room for labels, ticks, etc. */
    Dimension sz = size();
    int W = sz.width, H = sz.height; // Total size of Plotter
    int top  =0,   left=Math.min(labLen,W/2) + 2*MARGIN, // Edges of box for drawing bars
        right=W-2, bottom=H-2*fontH-TICK,
        h = (bottom-top)/n,	// vertical space for each entry
        barH = Math.max(4,h-GAP); // height of each bar
    double scale = (right-left-MARGIN)/(max-min);

    drawBox(g,bgColor,left,top,right-left-1,bottom-top-1);// Clear Background

    g.setFont(font);
    for(int i=0, yb=top+(h-barH)/2, yt=top+(h+fontH)/2; i<n; i++, yb+=h,yt+=h) {
      g.setColor(textcolor);
      g.drawString(labels[i], left-MARGIN-metrics.stringWidth(labels[i]), yt);
      drawBox(g,i==specindex ? specColor : barColor,
	      left,yb,(int)((values[i]-min)*scale),barH); }

    /* Draw tick marks, scale values and axis label */
    g.setColor(textcolor);	// Draw these in the `outer' text color.
    g.drawLine(left,bottom,right,bottom);
    int nt=(int)Math.round((max-min)/u);
    // System.out.println("NTics="+nt);    
    for(int i=0; i<=nt; i++) {
      int xx = left+ (int)(i*u*scale);
      g.drawLine(xx,bottom,xx,bottom+TICK); }
    tmp=-ll; if(tmp<0) tmp=0;
    String smin = Formatter.format(min,tmp),
           smax = Formatter.format(max,tmp);
    g.drawString(smin,left,bottom+TICK+fontH);
    g.drawString(smax,right-metrics.stringWidth(smax),bottom+TICK+fontH);
    g.drawString(axisLabel,(right+left)/2-metrics.stringWidth(axisLabel),
		           bottom+TICK+(3*fontH)/2);
  }
}
