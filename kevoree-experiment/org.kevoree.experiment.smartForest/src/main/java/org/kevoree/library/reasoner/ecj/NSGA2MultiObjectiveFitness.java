package org.kevoree.library.reasoner.ecj;

/**
 * Created by IntelliJ IDEA.
 * User: jbourcie
 * Date: 07/07/11
 * Time: 15:36
 * To change this template use File | Settings | File Templates.
 */
public class NSGA2MultiObjectiveFitness extends ec.multiobjective.nsga2.NSGA2MultiObjectiveFitness {

    public String fitnessToStringForHumans()
        {
        return super.fitnessToStringForHumans().replace("\n"," ");
        }
}
