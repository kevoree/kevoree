package org.kevoree.kmfTest;

import org.kevoree.ContainerRoot;
import org.kevoree.Dictionary;
import org.kevoree.Group;
import org.kevoree.framework.KevoreeXmiHelper;
import scala.Some;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 11/06/12
 * Time: 19:53
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class KMFUpdateDictionary {

	public static void main(String[] args) {
		ContainerRoot model = KevoreeXmiHelper.load("/home/edaubert/model_user_kloud.kev");
		for (Group g : model.getGroupsForJ()) {
			if (g.getName().equals("sync")) {
				System.out.println(g.getDictionary());
				Dictionary d = g.getDictionary().get();
				g.setDictionary(new Some(d));
				System.out.println(g.getDictionary());
			}
		}
	}
}
