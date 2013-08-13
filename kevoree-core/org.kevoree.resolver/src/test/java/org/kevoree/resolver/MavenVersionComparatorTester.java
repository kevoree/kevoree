package org.kevoree.resolver;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.resolver.util.MavenVersionComparator;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/08/13
 * Time: 11:50
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class MavenVersionComparatorTester {

    @Test
    public void compareEqualsVersionRelease() {
        String version1 = "2.0.0";
        String version2 = "2.0.0";
        String result = MavenVersionComparator.max(version1, version2);
        String result2 = MavenVersionComparator.max(version2, version1);
        System.out.println(result + "?=" + result2);
        Assert.assertEquals(result, result2);
    }

    @Test
    public void compareInequalsVersionRelease() {
        String version1 = "2.0.0";
        String version2 = "2.0.1";
        String result = MavenVersionComparator.max(version1, version2);
        String result2 = MavenVersionComparator.max(version2, version1);
        System.out.println(result + "?=" + result2);
        Assert.assertEquals(result, result2);
    }

    @Test
    public void compareEqualsVersionSnapshot() {
        String version1 = "2.0.0";
        String version2 = "2.0.0";
        String result = MavenVersionComparator.max(version1, version2);
        String result2 = MavenVersionComparator.max(version2, version1);
        System.out.println(result + "?=" + result2);
        Assert.assertEquals(result, result2);
    }

    @Test
    public void compareInequalsVersionSnapshot() {
        String version1 = "2.0.0-SNAPSHOT";
        String version2 = "2.0.1-SNAPSHOT";
        String result = MavenVersionComparator.max(version1, version2);
        String result2 = MavenVersionComparator.max(version2, version1);
        System.out.println(result + "?=" + result2);
        Assert.assertEquals(result, result2);
    }
}
