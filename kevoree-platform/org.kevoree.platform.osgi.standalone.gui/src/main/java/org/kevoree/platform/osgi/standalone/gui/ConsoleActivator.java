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
package org.kevoree.platform.osgi.standalone.gui;

import org.apache.felix.shell.ShellService;
import org.osgi.framework.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ConsoleActivator implements BundleActivator {
    // private static final String CHECK_INPUT_PROP = "shell.tui.checkinput";

    private BundleContext m_context = null;
    private volatile ShellGuiRunnable m_runnable = null;
    private volatile Thread m_thread = null;
    private ServiceReference m_shellRef = null;
    private ShellService m_shell = null;
    private volatile boolean m_checkInput = false;

    public void start(BundleContext context) {
        m_context = context;
        // Listen for registering/unregistering impl service.
        ServiceListener sl = new ServiceListener() {
            public void serviceChanged(ServiceEvent event) {
                synchronized (ConsoleActivator.this) {
                    // Initialize the service if we don't have one.
                    if ((event.getType() == ServiceEvent.REGISTERED)
                            && (m_shellRef == null)) {
                        initializeService();
                    }
                    // Unget the service if it is unregistering.
                    else if ((event.getType() == ServiceEvent.UNREGISTERING)
                            && event.getServiceReference().equals(m_shellRef)) {
                        m_context.ungetService(m_shellRef);
                        m_shellRef = null;
                        m_shell = null;
                        // Try to get another service.
                        initializeService();
                    }
                }
            }
        };
        try {
            m_context.addServiceListener(sl,
                    "(objectClass="
                            + org.apache.felix.shell.ShellService.class.getName()
                            + ")");
        } catch (InvalidSyntaxException ex) {
            System.err.println("ShellTui: Cannot add service listener.");
            System.err.println("ShellTui: " + ex);
        }

        // Now try to manually initialize the impl service
        // since one might already be available.
        initializeService();

        // Start impl thread.
        m_thread = new Thread(
                m_runnable = new ShellGuiRunnable(),
                "Felix Shell GUI");
        m_thread.start();
    }

    private synchronized void initializeService() {
        if (m_shell == null) {
            m_shellRef = m_context.getServiceReference(
                    org.apache.felix.shell.ShellService.class.getName());
            if (m_shellRef != null) {
                m_shell = (ShellService) m_context.getService(m_shellRef);
            }
        }
    }

    public void stop(BundleContext context) {
        if (m_runnable != null) {
            m_runnable.stop();
        }
    }

    private class ShellGuiRunnable implements Runnable {
        private volatile boolean m_stop = false;

        private Console console = null;

        public ShellGuiRunnable() {
            try {
                console = new Console();
            } catch (IOException io) {
                io.printStackTrace();
            }

        }

        public void stop() {
            m_stop = true;
        }

        public void run() {
            String line = "";
            //BufferedReader in = new BufferedReader(new InputStreamReader(console.getInputStream()));
            try {
                boolean needPrompt = true;
                int available;
                while (!m_stop) {

                    //System.out.println("LOOP");
                    if (needPrompt) {
                        System.out.print("-> ");
                        needPrompt = false;
                    }
                    available = console.getInputStream().available();
                    if (available == 0) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            // No one should be interrupting this thread, so
                            // ignore it.
                        }
                        continue;
                    }
                    //System.out.println("Before Read");

                    char c = (char) console.getInputStream().read();
                    //System.out.println("read"+c);
                    if (c == '\n') {
                        needPrompt = true;
                        line = line.trim();
                        if (line.length() == 0) {
                            continue;
                        }
                        synchronized (ConsoleActivator.this) {
                            if (m_shell == null) {
                                System.out.println("No impl service available.");
                                continue;
                            }

                            try {
                                //System.out.println("Execute command =>"+line);
                                m_shell.executeCommand(line, System.out, System.err);
                            } catch (Exception ex) {
                                System.err.println("ShellGUI: " + ex);
                                ex.printStackTrace();
                            }
                            line = "";
                        }

                    } else {
                        line = line + c;
                    }


                }
            } catch (IOException ex) {
                // Any IO error causes the thread to exit.
                System.err.println("ShellGUI: Unable to read from stdin...exiting.");
            }
        }
    }

}
