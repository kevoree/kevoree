/*
 *  ChartPanel.java, a decoration of a Chart2D that adds popup menues for traces and the chart.
 *  Copyright (C) 2005 - 2011 Achim Westermann.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart.views;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.annotations.IAnnotationCreator;
import info.monitorenter.gui.chart.annotations.bubble.AnnotationCreatorBubble;
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.layouts.FlowLayoutCorrectMinimumSize;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

/**
 * A decoration for {@link Chart2D} that adds various controls for a
 * {@link Chart2D} and it's {@link ITrace2D} instances in form of popup menues.
 * <p>
 * <h2>Performance note</h2>
 * The context menu items register themselves with the chart to adapt their
 * basic UI properties (font, foreground color, background color) via weak
 * referenced instances of
 * {@link info.monitorenter.gui.chart.controls.LayoutFactory.BasicPropertyAdaptSupport}
 * . This ensures that dropping a complete menu tree from the UI makes them
 * garbage collectable without introduction of highly unstable and
 * unmaintainable active memory management code. A side effect is that these
 * listeners remain in the property change listener list of the chart unless
 * they are finalized.
 * <p>
 * Adding and removing many traces to / from charts that are wrapped in
 * {@link ChartPanel} without {@link java.lang.System#gc()} followed by
 * {@link java.lang.System#runFinalization()} in your code will leave a huge
 * amount of listeners for non-visible uncleaned menu items in the chart which
 * causes a high cpu throttle for increasing the listener list.
 * <p>
 * The reason seems to be the implementation of (
 * {@link javax.swing.event.EventListenerList} that is used by
 * {@link javax.swing.event.SwingPropertyChangeSupport}). It is based upon an
 * array an grows only for the space of an additional listener by using
 * {@link java.lang.System#arraycopy(java.lang.Object, int, java.lang.Object, int, int)}
 * (ouch, this should be changed).
 * <p>
 * 
 * Profiling a day with showed that up to 2000 dead listeners remained in the
 * list. The cpu load increased after about 200 add / remove trace operations.
 * Good news is that no memory leak could be detected.
 * <p>
 * If those add and remove trace operations on {@link ChartPanel} - connected
 * charts are performed with intermediate UI action property change events on
 * dead listeners will let them remove themselves from the listener list thus
 * avoiding the cpu overhead. So UI / user - controlled applications will
 * unlikely suffer from this problem.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */
public class ChartPanel extends JLayeredPane implements PropertyChangeListener {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3905801963714197560L;

  /**
   * Main enbtry for demo app.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {
    // some data:
    final double[] data = new double[100];
    for (int i = 0; i < 100; i++) {
      data[i] = Math.random() * i + 1;
    }
    final JFrame frame = new JFrame("ChartPanel demo");
    final Chart2D chart = new Chart2D();
    // trace 1
    final ITrace2D trace1 = new Trace2DLtd(100);
    trace1.setName("Trace 1");

    // AbstractDataCollector collector1 = new
    // RandomDataCollectorOffset(trace1,500);
    // trace2
    final ITrace2D trace2 = new Trace2DLtd(100);
    trace2.setName("Trace 2");
    // add to chart
    chart.addTrace(trace1);
    chart.addTrace(trace2);
    // AbstractDataCollector collector2 = new
    // RandomDataCollectorOffset(trace2,500);
    for (int i = 0; i < 100; i++) {
      trace1.addPoint(i + 2, data[i]);
      trace2.addPoint(i + 2, 100 - data[i]);
    }

    final ChartPanel cPanel = new ChartPanel(chart);
    frame.getContentPane().add(cPanel);
    frame.setSize(new Dimension(400, 600));
    frame.addWindowListener(new WindowAdapter() {
      /**
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(final WindowEvent w) {
        System.exit(0);
      }

    });
    frame.setJMenuBar(LayoutFactory.getInstance().createChartMenuBar(cPanel, false));
    frame.setVisible(true);
  }

  /** The annotation creator factory for this panel. */
  private IAnnotationCreator m_annotationCreator = AnnotationCreatorBubble.getInstance();

  /** The decorated chart. */
  private final Chart2D m_chart;

  /**
   * <p>
   * An internal panel for the labels of the traces that uses a
   * {@link FlowLayout}.
   * </p>
   * 
   */
  protected JPanel m_labelPanel;

  /**
   * Creates an instance that decorates the given chart with controls in form of
   * popup menus.
   * <p>
   * 
   * @param chart
   *          A configured Chart2D instance that will be displayed and
   *          controlled by this panel.
   */
  public ChartPanel(final Chart2D chart) {
    super();
    this.m_chart = chart;
    this.setBackground(chart.getBackground());
    // we paint our own labels
    chart.setPaintLabels(false);
    // get the layout factory for popup menues:
    final LayoutFactory factory = LayoutFactory.getInstance();

    factory.createChartPopupMenu(this, true);

    // layout
    this.setLayout(new BorderLayout());
    this.add(chart, BorderLayout.CENTER);
    // initial Labels
    // put to a flow layout panel
    this.m_labelPanel = new JPanel();
    this.m_labelPanel.setFont(chart.getFont());
    this.m_labelPanel.setLayout(new FlowLayoutCorrectMinimumSize(FlowLayout.LEFT));
    this.m_labelPanel.setBackground(chart.getBackground());
    JLabel label;
    for (final ITrace2D trace : chart) {
      label = factory.createTraceContextMenuLabel(chart, trace, true);
      if (label != null) {
        this.m_labelPanel.add(label);
      }
      // In case trace.getLabel() becomes empty hide the corresponding menu
      // label via listeners!
      trace.addPropertyChangeListener(ITrace2D.PROPERTY_PHYSICALUNITS, this);
      trace.addPropertyChangeListener(ITrace2D.PROPERTY_NAME, this);
    }
    this.add(this.m_labelPanel, BorderLayout.SOUTH);
    chart.addPropertyChangeListener(Chart2D.PROPERTY_BACKGROUND_COLOR, this);
    // listen to new traces and deleted ones:
    final List<IAxis> allAxes = chart.getAxes();
    for (final IAxis currentAxis : allAxes) {
      currentAxis.addPropertyChangeListener(IAxis.PROPERTY_ADD_REMOVE_TRACE, this);
    }
    // a bit tricky: stay in touch with removed / added traces in case axes are
    // changed in the chart: use the axis property change event to update me as
    // a listener:
    chart.addPropertyChangeListener(Chart2D.PROPERTY_AXIS_X, this);
    chart.addPropertyChangeListener(Chart2D.PROPERTY_AXIS_Y, this);
  }

  /**
   * Internal helper that returns whether a label for the given trace is already
   * contained in the internal label panel.
   * <p>
   * 
   * This is needed because an addTrace(ITrace2D) call on the Chart2D is
   * delegated to two axes thus resulting in two events per added trace: We have
   * to avoid adding duplicate labels!
   * <p>
   * 
   * @param tracetoAdd
   *          the trace to check whether a label for it is already contained in
   *          the internal label panel.
   * 
   * @return true if a label for the given trace is already contained in the
   *         internal label panel.
   */
  private boolean containsTraceLabel(final ITrace2D tracetoAdd) {
    boolean result = false;
    final Component[] traceLabels = this.m_labelPanel.getComponents();
    JLabel label;
    final String labelName = tracetoAdd.getLabel();
    for (int i = traceLabels.length - 1; i >= 0; i--) {
      label = (JLabel) traceLabels[i];
      if (labelName.equals(label.getText())) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final ChartPanel other = (ChartPanel) obj;
    if (this.m_annotationCreator == null) {
      if (other.m_annotationCreator != null) {
        return false;
      }
    } else if (!this.m_annotationCreator.equals(other.m_annotationCreator)) {
      return false;
    }
    if (this.m_chart == null) {
      if (other.m_chart != null) {
        return false;
      }
    } else if (!this.m_chart.equals(other.m_chart)) {
      return false;
    }
    if (this.m_labelPanel == null) {
      if (other.m_labelPanel != null) {
        return false;
      }
    } else if (!this.m_labelPanel.equals(other.m_labelPanel)) {
      return false;
    }
    return true;
  }

  /**
   * Returns the annotationCreator.
   * <p>
   * 
   * @return the annotationCreator
   */
  public final IAnnotationCreator getAnnotationCreator() {
    return this.m_annotationCreator;
  }

  /**
   * Returns the chart.
   * <p>
   * 
   * @return the chart
   */
  public final Chart2D getChart() {
    return this.m_chart;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.m_annotationCreator == null) ? 0 : this.m_annotationCreator.hashCode());
    result = prime * result + ((this.m_chart == null) ? 0 : this.m_chart.hashCode());
    result = prime * result + ((this.m_labelPanel == null) ? 0 : this.m_labelPanel.hashCode());
    return result;
  }

  /**
   * Listens for property "background" of the <code>Chart2D</code> instance that
   * is contained in this component and sets the background color.
   * <p>
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    final String prop = evt.getPropertyName();
    if (prop.equals(Chart2D.PROPERTY_BACKGROUND_COLOR)) {
      final Color color = (Color) evt.getNewValue();
      this.setBackground(color);
      this.m_labelPanel.setBackground(color);
    } else if (prop.equals(IAxis.PROPERTY_ADD_REMOVE_TRACE)) {
      /*
       * This event is fired from the axis implementations, an
       * addTrace(ITrace2D) call on the Chart2D is delegated to two axes thus
       * resulting in two events per added trace: We have to avoid adding
       * duplicate labels!
       */
      final ITrace2D oldTrace = (ITrace2D) evt.getOldValue();
      final ITrace2D newTrace = (ITrace2D) evt.getNewValue();
      JLabel label;
      if ((oldTrace == null) && (newTrace != null)) {
        if (!this.containsTraceLabel(newTrace)) {
          label = LayoutFactory.getInstance().createTraceContextMenuLabel(this.m_chart, newTrace,
              true);
          if (label != null) {
            this.m_labelPanel.add(label);
            this.invalidate();
            this.m_labelPanel.invalidate();
            this.validateTree();
            this.m_labelPanel.doLayout();
          }
        }
      } else if ((oldTrace != null) && (newTrace == null)) {
        // search for label:
        final String labelName = oldTrace.getLabel();
        if (!StringUtil.isEmpty(labelName)) {
          final Component[] labels = (this.m_labelPanel.getComponents());
          for (final Component label2 : labels) {
            if (((JLabel) label2).getText().equals(labelName)) {
              this.m_labelPanel.remove(label2);
              this.m_chart.removePropertyChangeListener((PropertyChangeListener) label2);
              oldTrace.removePropertyChangeListener((PropertyChangeListener) label2);
              // clear the popup menu listeners too:
              final MouseListener[] mouseListeners = label2.getMouseListeners();
              for (final MouseListener mouseListener2 : mouseListeners) {
                label2.removeMouseListener(mouseListener2);
              }
              this.m_labelPanel.doLayout();
              this.doLayout();
              break;
            }
          }
        }
      } else if (prop.equals(Chart2D.PROPERTY_AXIS_X) || prop.equals(Chart2D.PROPERTY_AXIS_Y)) {
        final IAxis newAxis = (IAxis) evt.getNewValue();
        final IAxis oldAxis = (IAxis) evt.getOldValue();
        if (oldAxis != null) {
          oldAxis.removePropertyChangeListener(IAxis.PROPERTY_ADD_REMOVE_TRACE, this);
        }
        if (newAxis != null) {
          newAxis.addPropertyChangeListener(IAxis.PROPERTY_ADD_REMOVE_TRACE, this);
        }
      } else if (prop.equals(ITrace2D.PROPERTY_LABEL)) {
        final ITrace2D trace = (ITrace2D) evt.getSource();
        final String oldLabel = (String) evt.getOldValue();
        final String newLabel = (String) evt.getNewValue();

        if ((!StringUtil.isEmpty(oldLabel)) && (StringUtil.isEmpty(newLabel))) {
          final Component[] labels = (this.m_labelPanel.getComponents());
          for (final Component label2 : labels) {
            if (((JLabel) label2).getText().equals(oldLabel)) {
              this.m_labelPanel.remove(label2);
              this.m_chart.removePropertyChangeListener((PropertyChangeListener) label2);
              trace.removePropertyChangeListener((PropertyChangeListener) label2);
              // clear the popup menu listeners too:
              final MouseListener[] mouseListeners = label2.getMouseListeners();
              for (final MouseListener mouseListener2 : mouseListeners) {
                label2.removeMouseListener(mouseListener2);
              }
              this.m_labelPanel.doLayout();
              this.doLayout();
              break;
            }
          }
        } else if ((StringUtil.isEmpty(oldLabel)) && (!StringUtil.isEmpty(newLabel))) {
          if (!this.containsTraceLabel(newTrace)) {
            label = LayoutFactory.getInstance().createTraceContextMenuLabel(this.m_chart, newTrace,
                true);
            if (label != null) {
              this.m_labelPanel.add(label);
              this.invalidate();
              this.m_labelPanel.invalidate();
              this.validateTree();
              this.m_labelPanel.doLayout();
            }
          }
        }
      } else {
        throw new IllegalArgumentException("Bad property change event for add / remove trace.");
      }
    }
  }

  /**
   * Sets the annotationCreator.
   * <p>
   * 
   * @param annotationCreator
   *          the annotationCreator to set
   */
  public final void setAnnotationCreator(final IAnnotationCreator annotationCreator) {
    this.m_annotationCreator = annotationCreator;
  }
}
