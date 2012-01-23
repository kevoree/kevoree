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
package org.kevoree.tools.arduino.framework.fuzzylogic.gen.utils;

import java.io.*;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 10/01/12
 * Time: 17:23
 */
public class ArduinoHelpers {


    public static String readFile(String path)
    {
        StringBuilder buffer = new StringBuilder();
        try
        {
            BufferedReader buff = new BufferedReader(new FileReader(path));
            String line;
            while ((line = buff.readLine()) != null)
            {
                buffer.append(line+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public static void createFile(String filename,String contenu) throws IOException {
        FileWriter fstream = new FileWriter(filename);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(contenu);
        out.close();
    }


    public static String createMainArduinoECA(){

        return "void setup()\n" +
                "{\n" +
                "  \n" +
                "  Serial.begin(115200);\n" +
                "  setTime(8,29,0,24,1,12); // set time to 8:29:00am Jan 24 2012\n" +
                " \n" +
                "  setup_eca();  \n" +
                "\n" +
                "}\n" +
                "\n" +
                "void  loop(){  \n" +
                "\n" +
                "  Alarm.delay(1000); // wait one second between clock\n" +
                "}";
    }

    public static String createMainGCC(){
       return " int main(void){\n" +
               "while(1){\n" +
               "    float temperature[3];\n" +
               "	temperature[0]= 45;\n" +
               "    printf(\"Temperature %f \\n\",temperature[0]);\n" +
                "   control(temperature, crisp_outputs);	\n" +
               "    printf(\"Pourcentage motor fan : %f \\n\",crisp_outputs[0]);\n" +
               "	sleep(1);\n" +
               "}" +
               "}";

    }


    public static String createBaliseHommeMortMain(){
        return "void setup() {                \n" +
                "  // initialize the digital pin as an output.\n" +
                "  // Pin 13 has an LED connected on most Arduino boards:\n" +
                "  pinMode(13, OUTPUT);   \n" +
                "setTime(8,29,0,24,1,12); // set time to 8:29:00am Jan 24 2012\n" +
                "setup_eca();pinMode(4, OUTPUT); // set a pin for buzzer output\n" +
                "Serial.begin(115200);\n" +
                "}\n" +
                "\n" +
                "void loop() \n" +
                "{\n" +
                "  Alarm.delay(1000);\n" +
                "\n" +
                "int sensorValue = analogRead(A0);\n" +
                "\n" +
                "\n" +
                "eca_inputs_values[ECA_temperature] = map((int)sensorValue,0,1024,-10,60 );\n" +
                "\n" +
                "\n" +
                "  Serial.print(\"Fake Temperature\");\n" +
                "  Serial.println(eca_inputs_values[ECA_temperature]);\n" +
                "\n" +
                "}";
    }

    public static String createFreeRam(){
        return "int freeRam ()\n" +
                "{\n" +
                "  extern int __heap_start, *__brkval;\n" +
                "  int v;\n" +
                "  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval);\n" +
                "}\n" +
                "";
    }
        public static String createMainArduino(){
       return createFreeRam()+"void setup()\n" +
               "{\n" +
                "Serial.begin(115200);\n " +
               "Serial.print(\"Number of Rules : \");\n " +
               "Serial.println(NUM_RULES); " +
                "Serial.print(\"Free Ram \");\n" +
               "  Serial.println(freeRam ());\n" +
               "displayRules();\n" +
               "displayDomain();\n" +
               "delay(3000);" +
               "}\n" +
               " void loop() \n " +
               "{ \n " +
               "float temperature[3];\n" +
               "  	temperature[0]= 45;\n" +
               "        Serial.print(\"t= \");\n" +
                    "       Serial.println(temperature[0]);\n" +
                    "       control(temperature, crisp_outputs);\n" +
                    "        Serial.print(\"f= :\");\n" +
                    "      Serial.println(crisp_outputs[0]); \n" +
               "delay(2000);\n" +

               "} ";

}



}
