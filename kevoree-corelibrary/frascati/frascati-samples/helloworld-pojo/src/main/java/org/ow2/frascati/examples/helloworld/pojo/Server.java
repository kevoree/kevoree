/**
 * OW2 FraSCAti Examples : HelloWorld POJO
 * Copyright (C) 2008-2010 INRIA, University of Lille 1
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
 * Author: Damien Fournier
 *         Christophe Demarey
 *
 * Contributor(s): Philippe Merle
 *
 */
package org.ow2.frascati.examples.helloworld.pojo;

/**
 * The print service implementation.
 */
public class Server
  implements PrintService
{    
    private String header = "->";
    private int count = 1;

    /**
     * Default constructor.
     */
    public Server()
    {
        System.out.println("SERVER created.");
    }

    /**
     * PrintService implementation.
     */
    public final void print(final String msg)
    {
        System.out.println("SERVER: begin printing...");
        for (int i = 0; i < count; ++i) {
            System.out.println(header + msg);
        }
        System.out.println("SERVER: print done.");
    }

    /**
     * Set the Header property.
     */
    public final void setHeader(final String header)
    {
        System.out.println("SERVER: setting header to '" + header + "'.");
        this.header = header;
    }

    /**
     * Set the count property.
     */
    public final void setCount(final int count)
    {
        System.out.println("SERVER: setting count to '" + count + "'.");
        this.count = count;
    }
}
