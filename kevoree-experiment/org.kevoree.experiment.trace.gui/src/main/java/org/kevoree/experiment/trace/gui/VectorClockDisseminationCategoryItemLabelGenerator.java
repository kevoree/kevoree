package org.kevoree.experiment.trace.gui;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

import java.util.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/04/11
 * Time: 16:38
 */
class VectorClockDisseminationCategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator {

	private List<String> nodeIds;
	private List<String> timeRepresentations;
	private Map<String, Map<String, String>> vectorClocks;

	VectorClockDisseminationCategoryItemLabelGenerator(List<String> nodeIds, Set<String> timeRepresentations, Map<String, Map<String, String>> vectorClocks) {
		this.nodeIds = nodeIds;
		this.vectorClocks = vectorClocks;

		this.timeRepresentations = new ArrayList<String>(timeRepresentations.size());
		Iterator<String> iter = timeRepresentations.iterator();
		int index = 0;
		while (iter.hasNext()) {
			this.timeRepresentations.add(index, iter.next());
			index++;
		}
	}



	public String generateLabel(CategoryDataset dataset, int row, int column) {
		/*System.out.println("row : " + row);
		System.out.println("column : " + column);
		System.out.println(vectorClocks.get(nodeIds.get(row)).get(timeRepresentations.get(column)));*/
        return "<" + vectorClocks.get(nodeIds.get(row)).get(timeRepresentations.get(column)) + ">";
    }
}
