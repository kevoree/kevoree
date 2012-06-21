/**
 * OW2 FraSCAti Examples: HelloWorld POJO
 * Copyright (C) 2009-2010 INRIA, University of Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: frascati@ow2.org
 *
 * Author: Nicolas Dolet
 *
 * Contributor(s): Philippe Merle
 *
 */
package org.ow2.frascati.examples.helloworld;

import org.junit.Test;
import org.ow2.frascati.examples.test.FraSCAtiTestCase;

public class HelloworldTestCase
     extends FraSCAtiTestCase
{
  @Override
  public final String getComposite()
  {
    return "helloworld-pojo";
  }

  @Test
  public final void testService()
  {
    getService(Runnable.class, "r").run();
  }
}
