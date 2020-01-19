/*********************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Lukas Gregori
 * contact@lukasgregori.com
 * www.lukasgregori.com
 *
 * (c) 2020 by Lukas Gregori
 *********************************************************************/

package com.lukasgregori.idoc2impex.routes;

import com.lukasgregori.idoc2impex.types.raw.E1MARAM;
import com.lukasgregori.idoc2impex.types.target.ProductModel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * @author Lukas Gregori
 */
public class MATMAS05Route extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:processMATMAS05")
                .split(body().tokenizeXML("E1MARAM", null)).streaming()
                .unmarshal(createXMLDataFormat())
                .convertBodyTo(ProductModel.class)
                .marshal(createCSVDataFormat())
                .to("direct:exportCSVToFile");
    }

    private JaxbDataFormat createXMLDataFormat() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(E1MARAM.class);
        JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);
        jaxbDataFormat.setPartClass(E1MARAM.class.getName());
        jaxbDataFormat.setFragment(true);
        return jaxbDataFormat;
    }

    private BindyCsvDataFormat createCSVDataFormat() {
        return new BindyCsvDataFormat(com.lukasgregori.
                idoc2impex.types.target.ProductModel.class);
    }
}
