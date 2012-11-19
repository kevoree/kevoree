/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.library.javase.adaptationsuperviser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
* Frame that is in charge of showing the play/pause buttons,
* Also in charge of waiting until the step or run button is clicked.
*/
class PlayFrame extends JPanel {

    private JTextPane statusText;
    private JButton playBtn;
    private JButton stepPauseBtn;

    private boolean paused = false;
    private boolean stepPressed = false;
    private boolean waitingStepPressed = false;


    public PlayFrame() {
        setPreferredSize(new Dimension(AdaptationSuperviser.FRAME_WIDTH, 40));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        playBtn = new JButton("Play");
        playBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setPaused(false);
                updateStatus();
            }
        });
        stepPauseBtn = new JButton("Pause");
        stepPauseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(isPaused()){
                    setStepPressed(true);
                }
                else{
                    setPaused(true);
                }
                updateStatus();
            }
        });

        statusText = new JTextPane();
        statusText.setFocusable(false);
        statusText.setEditable(false);

        add(playBtn);
        add(stepPauseBtn);
        add(statusText);

        updateStatus();
        setVisible(true);
    }

    synchronized public boolean isPaused(){
          return paused;
    }

    synchronized  void setPaused(boolean newValue){
        paused = newValue;
    }

    synchronized boolean isStepPressed(){
          return stepPressed;
    }

    synchronized  void setStepPressed(boolean newValue){
        stepPressed = newValue;
    }

    synchronized boolean isWaitingStepPressed(){
              return waitingStepPressed;
    }

    synchronized  void setWaitingStepPressed(boolean newValue){
        waitingStepPressed = newValue;
    }

    void step(){
        // reset stepPressed
        setStepPressed(false);
        setWaitingStepPressed(true);
        updateStatus();
        while (isPaused() && !isStepPressed()) {
            try {
                Thread.sleep(250);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        setWaitingStepPressed(false);
        updateStatus();
    }

    public void updateStatus(){
        if(isPaused()){
            if(isWaitingStepPressed()){
                 statusText.setText("Adaptation paused (waiting)");
            }
            else{
                 statusText.setText("Next adaptation request will be paused");
            }
            stepPauseBtn.setText("Step");
        }
        else{
            statusText.setText("Normal run");
            stepPauseBtn.setText("Pause");
        }
    }

}
