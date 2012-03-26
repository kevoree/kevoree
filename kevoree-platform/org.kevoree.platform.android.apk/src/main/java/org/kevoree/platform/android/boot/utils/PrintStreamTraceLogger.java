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
package org.kevoree.platform.android.boot.utils;

import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import java.io.OutputStream;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 09/03/12
 * Time: 11:39
 */
public class PrintStreamTraceLogger extends OutputStream {
        private TextView _textArea = null;
        private int _color = 0;
        StringBuilder currentLine = new StringBuilder();
         FragmentActivity ctx;

        public PrintStreamTraceLogger(FragmentActivity ctx,TextView textArea, int color) {
            _textArea = textArea;
            _color = color;
            this.ctx = ctx;
        }

        @Override
        public void write(final int b) {
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (b == (int) '\n') {
                            currentLine.append("\n");
                            if (!currentLine.toString().startsWith("Error reading from ")) {

                                _textArea.append(currentLine);
                                final int scrollAmount = _textArea.getLayout().getLineTop(_textArea.getLineCount()) - _textArea.getHeight();

                                if (scrollAmount > 0)
                                    _textArea.scrollTo(0, scrollAmount);
                                else
                                    _textArea.scrollTo(0, 0);

                                _textArea.setTextColor(_color);
                                currentLine = new StringBuilder();
                            }



                        } else {
                            currentLine.append((char) b);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });

        }

    }