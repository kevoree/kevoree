package org.kevoree.kevscript.util;

import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.pmodeling.api.KMFContainer;
import org.kevoree.pmodeling.api.json.JSONModelLoader;

public class TestLoadJSON {

	public static void main(String[] args) {
		KevoreeFactory factory = new DefaultKevoreeFactory();
		JSONModelLoader jsonLoader = factory.createJSONLoader();
		String json = "{\"class\":\"org.kevoree.NodeType@name=JavaNode,version=1\",\"name\":\"JavaNode\",\"abstract\":\"false\",\"version\":\"1\",\"deployUnits\":[],\"superTypes\":[],\"dictionaryType\":[{\"class\":\"org.kevoree.DictionaryType@0\",\"generated_KMF_ID\":\"0\",\"attributes\":[{\"class\":\"org.kevoree.DictionaryAttribute@jvmArgs\",\"datatype\":\"STRING\",\"defaultValue\":\"\",\"name\":\"jvmArgs\",\"fragmentDependant\":\"false\",\"optional\":\"true\",\"state\":\"false\",\"genericTypes\":[]},{\"class\":\"org.kevoree.DictionaryAttribute@log\",\"datatype\":\"STRING\",\"defaultValue\":\"INFO\",\"name\":\"log\",\"fragmentDependant\":\"false\",\"optional\":\"true\",\"state\":\"false\",\"genericTypes\":[]},{\"class\":\"org.kevoree.DictionaryAttribute@lol\",\"datatype\":\"STRING\",\"defaultValue\":\"INFO\",\"name\":\"lol\",\"fragmentDependant\":\"false\",\"optional\":\"true\",\"state\":\"false\",\"genericTypes\":[]}]}],\"metaData\":[{\"class\":\"org.kevoree.Value@java.class\",\"name\":\"java.class\",\"value\":\"org.kevoree.library.JavaNode\"}]}";
		KMFContainer loaded = jsonLoader
				.loadModelFromString(
						json)
				.get(0);
		System.out.println(json);
		System.out.println(loaded);

	}

}
