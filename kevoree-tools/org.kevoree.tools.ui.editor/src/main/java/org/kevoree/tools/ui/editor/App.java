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
/* $Id: App.java 13086 2010-10-21 11:40:48Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        JFrame jframe = new JFrame("Kevoree Editor");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setPreferredSize(new Dimension(800, 600));
        KevoreeEditor artpanel = new KevoreeEditor();
        jframe.add(artpanel.getPanel(),BorderLayout.CENTER);
        jframe.pack();
        jframe.setVisible(true);

    }
}
