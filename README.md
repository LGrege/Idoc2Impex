# Idoc2Impex
POC of a Java & [Apache Camel](https://camel.apache.org/) based tool for converting Idocs to Impex files. 
All sample Idocs used within this example were taken from 
[hybrismart.com](https://hybrismart.com/2019/08/12/commerce-data-hub-a-deep-look-better-late-than-never/).

## Overview

#### Raw class generation
The raw input types used to represent different sections of the Idocs 
are generated using xjc based on the `idoc-definitions.xsd` file.

```xml
<xs:complexType name="E1MARAM">
    <xs:sequence>
        <xs:element name="MATNR" type="xs:string"/>
        <xs:element name="MEINS" type="xs:string"/>
    </xs:sequence>
</xs:complexType>
```
By running the `xsdtojava` maven target (`mvn org.apache.cxf:cxf-xjc-plugin:3.3.0:xsdtojava`), 
the provided xsd file is used to generate java classes.
The resulting jaxb annotated classes can then be used to parse the idocs in the first step of the camel route

```java
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "E1MARAM", propOrder = {"matnr","meins"})
public class E1MARAM {
    
    @XmlElement(name = "MATNR", required = true)
    protected String matnr;
    @XmlElement(name = "MEINS", required = true)
    protected String meins;
}
```
#### Target class annotation

To export the classes as Impex file, the CsvRecord of the 
[Apache Camel Bindy Dataformat](https://camel.apache.org/components/latest/bindy-dataformat.html) can be used. 
The annotation on those classes later on defines the structure of the produced Impex.

```java
@CsvRecord(separator = ";", generateHeaderColumns = true)
public class ProductModel {

    @DataField(pos = 0, columnName = "INSERT_UPDATE Product")
    private String insertStatement;

    @DataField(pos = 1, columnName = "code[unique=true]")
    private String code;

    @DataField(pos = 2, columnName = "unit(code)")
    private String unit;
}
```

#### Raw to target mapping

To map the generated raw classes to their target counterpart, Apache Camels 
[Dozer Component](https://camel.apache.org/components/latest/dozer-component.html) was used.
The attribute based mapping can be found within the `mapping.xml` file.

```xml
<!-- MATMAS05 to ProductModel -->
<mapping>
    <class-a>com.lukasgregori.idoc2impex.types.raw.E1MARAM</class-a>
    <class-b>com.lukasgregori.idoc2impex.types.target.ProductModel</class-b>
    <field>
        <a>matnr</a>
        <b>code</b>
    </field>
    <field>
        <a>meins</a>
        <b>unit</b>
    </field>
</mapping>
```

#### Results

To transform an Idoc to Impex, it needs to be placed within the input directory. Once the application
is started, the file will be either moved to the `archive` or `error` directory, dependent if the Idoc
could be successfully transformed or not.

```
├── input
└── output
    ├── archive
    │   └── baseProduct_MATMAS.xml
    ├── error
    └── processed
        └── baseProduct_MATMAS.impex
```

The resulting Impex can be seen below. All mapped fields are put into the Impex
as specified by the annotations in the target files.

```csv
INSERT_UPDATE Product;code[unique=true];unit(code)
;TKCNF01;PCE
```
