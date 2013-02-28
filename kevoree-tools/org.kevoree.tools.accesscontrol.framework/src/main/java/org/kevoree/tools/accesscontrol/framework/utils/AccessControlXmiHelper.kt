package org.kevoree.tools.accesscontrol.framework.utils


import java.util.zip.Deflater
import java.util.zip.Inflater
import java.io.*
import org.kevoree.AccessControl.AccessControlRoot
import org.kevoree.AccessControl.serializer.ModelSerializer
import org.kevoree.framework.ZipUtil
import org.kevoree.AccessControl.loader.ModelLoader

object AccessControlXmiHelper {


    fun save(uri: String, root: AccessControlRoot) {
        //CHECK DIRECTORY CREATION
        val folderUri = if(uri.contains(File.separator)){
            uri.substring(0, uri.lastIndexOf(File.separator))
        } else {
            uri
        }
        //  val folderUri = uri.substring(0,uri.lastIndexOf(File.separator))
        val folder = File(folderUri)
        if (!folder.exists()){
            folder.mkdirs()
        }
        val serializer = ModelSerializer()
        val outputFile = File(uri);
        if(!outputFile.exists()) {
            outputFile.createNewFile()
        }
        val fop = FileOutputStream(outputFile)
        serializer.serialize(root, fop)
        fop.flush()
        fop.close()
    }

    fun saveToString(root: AccessControlRoot, prettyPrint: Boolean): String {
        val serializer = ModelSerializer()
        val ba = ByteArrayOutputStream()
        val res = serializer.serialize(root, ba)
        ba.flush()
        val result = String(ba.toByteArray())
        ba.close()
        return result
    }

    fun loadString(model: String): AccessControlRoot? {
        val loader = ModelLoader()
        val loadedElements = loader.loadModelFromString(model)
        if(loadedElements != null && loadedElements.size() > 0) {
            return loadedElements.get(0);
        } else {
            return null;
        }
    }

    fun load(uri: String): AccessControlRoot? {
        val loader = ModelLoader()
        val loadedElements = loader.loadModelFromPath(File(uri))
        if(loadedElements != null && loadedElements.size() > 0) {
            return loadedElements.get(0);
        } else {
            return null;
        }
    }

    fun loadStream(input: InputStream): AccessControlRoot? {
        val loader = ModelLoader()
        val loadedElements = loader.loadModelFromStream(input)
        if(loadedElements != null && loadedElements.size() > 0) {
            return loadedElements.get(0);
        } else {
            return null;
        }

    }

    fun saveStream(output: OutputStream, root: AccessControlRoot): Unit {
        val serializer = ModelSerializer()
        val result = serializer.serialize(root, output)
    }

    fun saveCompressedStream(output: OutputStream, root: AccessControlRoot): Unit {
        val modelStream = ByteArrayOutputStream()
        saveStream(modelStream, root)
        output.write(ZipUtil.compressByteArray(modelStream.toByteArray()))
        output.flush()

    }

    fun loadCompressedStream(input: InputStream): AccessControlRoot? {
        val inputS = ByteArrayInputStream(ZipUtil.uncompressByteArray(input.readBytes(input.available())))
        return loadStream(inputS)
    }



}