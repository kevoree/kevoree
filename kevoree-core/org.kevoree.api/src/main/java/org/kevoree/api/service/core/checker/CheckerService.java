/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.api.service.core.checker;

import java.util.List;
import org.kevoree.ContainerRoot;

/**
 *
 * @author ffouquet
 */
public interface CheckerService {

    public List<CheckerViolation> check(ContainerRoot model);

}
