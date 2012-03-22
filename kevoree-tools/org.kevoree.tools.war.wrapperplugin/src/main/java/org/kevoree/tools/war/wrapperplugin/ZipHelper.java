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
package org.kevoree.tools.war.wrapperplugin;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 15/12/11
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class ZipHelper {

    public static void createParentDirs(File file) throws IOException {
        File parent = file.getCanonicalFile().getParentFile();
        if (parent == null) {
            /*
            * The given directory is a filesystem root. All zero of its ancestors
            * exist. This doesn't mean that the root itself exists -- consider x:\ on
            * a Windows machine without such a drive -- or even that the caller can
            * create it, but this method makes no such guarantees even for non-root
            * files.
            */
            return;
        }
        parent.mkdirs();
        if (!parent.isDirectory()) {
            throw new IOException("Unable to create parent directories of " + file);
        }
    }

    public static void unzipToTempDir(File inputWar, File outputDir, List<String> filtered) {
        try {
            FileInputStream inputWarST = new FileInputStream(inputWar);
            ZipInputStream zis = new ZipInputStream(inputWarST);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(outputDir.getAbsolutePath() + File.separator + entry.getName()).mkdirs();
                } else {

                    boolean filteredMatch = false;
                    for (String name : filtered) {
                        //System.out.println("Filtered = "+entry.getName()+"-"+name.trim()+"-"+entry.getName().contains(name.trim()));
                        if (entry.getName().contains(name.trim())) {
                            filteredMatch = true;
                        }
                    }

                    if (!filteredMatch) {
                        File targetFile = new File(outputDir + File.separator + entry.getName());
                        createParentDirs(targetFile);
                        if (!targetFile.exists()) {
                            targetFile.createNewFile();
                        }

                        BufferedOutputStream outputEntry = new BufferedOutputStream(new FileOutputStream(targetFile));
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while (zis.available() > 0) {
                            len = zis.read(buffer);
                            if (len > 0) {
                                outputEntry.write(buffer, 0, len);
                            }
                        }
                        outputEntry.flush();
                        outputEntry.close();
                    }
                }
            }
            zis.close();
            inputWarST.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
    * zip the folders
    */
    public static void zipFolder(String srcFolder, String destZipFile) throws Exception {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;
        /*
        * create the output stream to zip file result
        */
        fileWriter = new FileOutputStream(destZipFile);
        zip = new ZipOutputStream(fileWriter);
        /*
        * add the folder to the zip
        */
        
        File f = new File(srcFolder);
        File[] subFiles = f.listFiles();
        for(int i=0;i<subFiles.length;i++){
            File subFile = subFiles[i];
            if(subFile.isDirectory()){
                addFolderToZip("", subFile.getAbsolutePath(), zip);
            } else {
                addFileToZip(subFile.getName(), subFile.getAbsolutePath(), zip, false);
            }

        }
        
        

        /*
        * close the zip objects
        */
        zip.flush();
        zip.close();
    }


    /*
    * recursively add files to the zip files
    */
    private static void addFileToZip(String path, String srcFile, ZipOutputStream zip, boolean flag) throws Exception {
        /*
        * create the file object for inputs
        */
        File folder = new File(srcFile);

        /*
         * if the folder is empty add empty folder to the Zip file
         */
        if (flag == true) {
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName() + "/"));
        } else {     /*
             * if the current name is directory, recursively traverse it to get the files
             */
            if (folder.isDirectory()) {
                /*
                 * if folder is not empty
                 */
                addFolderToZip(path, srcFile, zip);
            } else {
                /*
                 * write the file to the output
                */
                byte[] buf = new byte[1024];
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
                while ((len = in.read(buf)) > 0) {
                    /*
                     * Write the Result
                    */
                    zip.write(buf, 0, len);
                }
            }
        }
    }


    /*
    * add folder to the zip file
    */
    private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
            throws Exception {
        File folder = new File(srcFolder);

        /*
         * check the empty folder
         */
        if (folder.list().length == 0) {
            System.out.println(folder.getName());
            addFileToZip(path, srcFolder, zip, true);
        } else {
            /*
             * list the files in the folder
             */
            for (String fileName : folder.list()) {
                if (path.equals("")) {
                    addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip, false);
                } else {
                    addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip, false);
                }
            }
        }
    }


}
