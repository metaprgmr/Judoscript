import java.applet.Applet;  //needed for: extends Applet
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.awt.Frame;
import java.awt.BorderLayout;

//***********************************************************************
public class MyApplet extends Applet implements MouseListener,
                                                MouseMotionListener,
                                                ActionListener
//***********************************************************************
{
  // Data members:

  float R = 200;

//-----------------------------------------------------------------------
  public void init() {
//-----------------------------------------------------------------------  
  
    setBackground( Color.white );

    this.addMouseListener(this);        //Activates MouseListener
    this.addMouseMotionListener(this);  //Activates MouseMotionListener
  }

//-----------------------------------------------------------------------
  public void paint(Graphics g) {
//-----------------------------------------------------------------------  

    setBackground( Color.white );

    int r = (int)R;
    int d = r + r;
    int x = r;
    int y = r;
    int alfa = 30;

    drawPie(g, Color.red,   x, y, d, alfa);
    drawPie(g, Color.green, x, y, d, alfa + 120);
    drawPie(g, Color.blue,  x, y, d, alfa + 240);

    drawSlice(g, Color.blue,  x, y, d, alfa);
    drawSlice(g, Color.red,   x, y, d, alfa + 120);
    drawSlice(g, Color.green, x, y, d, alfa + 240);

  } //paint()

  private void drawPie(Graphics g, Color color, int x, int y, int d, int alfa) {
    g.setColor(color);
    g.fillArc(x, y, d, d, alfa, 120);
  }

  private void drawSlice(Graphics g, Color color, int x, int y, int d, int alfa) {
    int _x = x + (int)(R * Math.cos(alfa-60));
    int _y = y + (int)(R * Math.sin(alfa-60));

    g.setColor(color);
    g.fillArc(_x, _y, d, d, 240-alfa, 60);
  }

//-----------------------------------------------------------------------
  public void update(Graphics g)  {
//-----------------------------------------------------------------------
    
    g.clearRect(0, 0, 200, this.getSize().height );  //Clear word area.

    paint( g );

  } //update()

//-----------------------------------------------------------------------
  public void actionPerformed(ActionEvent e)  { //MouseListener
//-----------------------------------------------------------------------
    
    repaint();
  }

//-----------------------------------------------------------------------
  public void mousePressed(MouseEvent event) { //MouseListener
//-----------------------------------------------------------------------
    //This method is called the mouse button has been pressed.

    repaint();
  }

//-----------------------------------------------------------------------
  public void mouseClicked(MouseEvent event) {} //MouseListener
//-----------------------------------------------------------------------
  
//-----------------------------------------------------------------------
  public void mouseReleased(MouseEvent event) {} //MouseListener
//-----------------------------------------------------------------------

//-----------------------------------------------------------------------
  public void mouseEntered(MouseEvent event) {} //MouseListener
//-----------------------------------------------------------------------
  
//-----------------------------------------------------------------------
  public void mouseExited(MouseEvent event) {} //MouseListener
//-----------------------------------------------------------------------

//-----------------------------------------------------------------------
  public void mouseDragged(MouseEvent event)  { //MouseMotionListener
//-----------------------------------------------------------------------
    
    repaint();
  }

//-----------------------------------------------------------------------
  public void mouseMoved(MouseEvent event)  {} //MouseMotionListener
//-----------------------------------------------------------------------

//-----------------------------------------------------------------------
  public static void main( String args [] ) {
//-----------------------------------------------------------------------
    
    //1. This applet can be run as an application!!!
    //2. main() is not run if the Browser is running this Applet.

    Frame app = new Frame( "Application - MyApplet" );  
    app.setSize(200, 200);

    app.addWindowListener(      //Register an anonymous class as a listener.
         new WindowAdapter() {
            public void windowClosing( WindowEvent e ) 
            {  
               System.exit( 0 );
            }
         }
    );
    
    MyApplet applet = new MyApplet();         //create the applet.
//    applet.blnLoadHtmlParms = false;          //Don't get HTML parms because run as an Application.
    applet.init();                            //initialize applet.
    applet.start();                           //start applet.

    app.add( applet, BorderLayout.CENTER );   //add applet to center of frame.
    app.setVisible( true );                   //Make frame visible.

  } //main()

} //class MyApplet
