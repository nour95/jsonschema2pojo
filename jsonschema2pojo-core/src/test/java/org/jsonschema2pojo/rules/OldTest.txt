/**
 * Copyright © 2010-2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.rules;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;

import com.sun.codemodel.*;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.example.Example;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class DefaultRuleTest {


    //https://stackoverflow.com/questions/25751758/how-to-assign-a-value-to-a-specific-index-of-an-array-with-java-codemodel

    private GenerationConfig config = mock(GenerationConfig.class);
    private RuleFactory ruleFactory = mock(RuleFactory.class);

    private TypeRule rule = new TypeRule(ruleFactory);

    @Before
    public void wireUpConfig() {
        when(ruleFactory.getGenerationConfig()).thenReturn(config);
    }

    @AfterClass
    public static void runOnceAfterClass()
    {
        int[] branchIDs = DefaultRule.branchIDs;
        System.out.println(branchIDs[0]);
        System.out.println(branchIDs[1]);
        System.out.println(branchIDs[2]);


    }

    @Test
    public void getDefaultValueString() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "string"); // in Json = {"type":"string"}

        //fooBar will actually not be assigend or used at all in this test case since the type is a string
        JType type = rule.apply("fooBar", objectNode, null, jpackage, null);

        JExpression ex = DefaultRule.getDefaultValue(type, "NourA");
        //boolean correctType = ex instanceof JStringLiteral; //true
        assertThat(ex instanceof JStringLiteral, is(true));
        assertThat(ex instanceof JVar, is(false));

        if (ex instanceof JStringLiteral)
        {
            JStringLiteral ex1 = (JStringLiteral) ex;
            assertThat(ex1.str, is("NourA"));

        }

        /*
        JStringLiteral ex1 = (JStringLiteral) ex;
        JExpression ex2 = DefaultRule.getDefaultValue(type, "NourB");
        JExpressionImpl ex3 = (JExpressionImpl) DefaultRule.getDefaultValue(type, "NourC");

        String x1 = ex1.str;
        String x2 = ex2.toString();
        JFieldRef x3 = ex3.ref("");
        String x4 = x3.toString();

        assertThat(type.fullName(), is(String.class.getName()));*/
    }

    @Test //TODO
    public void applyGeneratesDate() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "string");

        TextNode formatNode = TextNode.valueOf("date-time");
        objectNode.set("format", formatNode);

        JType mockDateType = mock(JType.class);
        FormatRule mockFormatRule = mock(FormatRule.class);
        when(mockFormatRule.apply(eq("fooBar"), eq(formatNode), any(), Mockito.isA(JType.class), isNull(Schema.class))).thenReturn(mockDateType);
        when(ruleFactory.getFormatRule()).thenReturn(mockFormatRule);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result, equalTo(mockDateType));
    }


    @Test
    public void nourTest() throws Exception
    {
        JCodeModel codeModel = new JCodeModel();

        //JClass mapClass = codeModel.ref(String.format("%s.%sMap", packageName, retrieve.getId()));

        JDefinedClass retrieveClass = codeModel._class(JMod.PUBLIC, "Np", ClassType.CLASS);
        retrieveClass.javadoc().append("Auto generated class. Do not modify!");
        //retrieveClass._extends(codeModel.ref(AbstractRetrieve.class).narrow(mapClass));

        // Constructor
        //JMethod constructor = retrieveClass.constructor(JMod.PUBLIC);
        //constructor.param(String.class, "id");
        //constructor.body().invoke("super").arg(JExpr.ref("id"));

        // Implemented method
        /*JMethod getMapMethod = retrieveClass.method(JMod.PUBLIC, mapClass, "getMap");
        getMapMethod.annotate(Override.class);
        getMapMethod.param(codeModel.ref(Map.class).narrow(String.class, Object.class), "data");
        getMapMethod.body()._return(JExpr._new(mapClass).arg(JExpr.ref("data")));*/


        File target = new File("target/generated-sources/java");
        if (!target.mkdirs()) {
            throw new IOException("could not create directory");
        }
        codeModel.build(target);

        //codeModel.build(f2);




    }



    @Test
    public void applyGeneratesInteger() throws Exception //TODO fix exception
    {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        JType type = rule.apply("fooBar", objectNode, null, jpackage, null);

        JCodeModel codeModel = new JCodeModel();
        JExpression ex = DefaultRule.getDefaultValue(type, "45");

        //Not needed
        //JBlock block = new JBlock();
        //JVar jvar = block.decl(JMod.NONE, codeModel.parseType("int"), "numberToTest", ex);

        JDefinedClass dClass = codeModel._class(JMod.PUBLIC, "Np", ClassType.CLASS);
        JFieldVar fieldVar = dClass.field(JMod.PRIVATE, codeModel.parseType("int"), "numberToTest");
        fieldVar.init(ex);
        int m = fieldVar.mods().getValue();
        File f = new File("teee.txt");
        codeModel.build(f);
        //String ssd = jvarIndex.equals();

        //
        //PrintWriter pw = new PrintWriter(new File("teee1.txt"));
        //JFormatter formatter = new JFormatter(pw);
        //String s = formatter.p("sd");

        boolean correctType = ex instanceof  JExpressionImpl; //true
        boolean correctType2 = ex instanceof JArray;
        boolean correctType3 = ex instanceof JAssignment;
        boolean correctType4 = ex instanceof JEnumConstant;
        boolean correctType5 = ex instanceof JFieldRef;
        boolean correctType6 = ex instanceof JInvocation;
        boolean correctType7 = ex instanceof JStringLiteral;
        boolean correctType8 = ex instanceof JVar;
        //boolean correctType9 = ex instanceof JAtom;

        JInvocation v = ex.invoke("45");
        String v2 = v.toString();
        //int ss = v.complement();

        assertThat(ex instanceof JExpressionImpl, is(true));
        assertThat(ex instanceof JStringLiteral, is(false));
        assertThat(ex instanceof JVar, is(false));

        if (ex instanceof JExpressionImpl)
        {
            JExpressionImpl ex1 = (JExpressionImpl) ex;
            String b = ex1.toString();
            System.out.println(ex1);
            //assertThat(ex1.what, is("45"));

        }

        //assertThat(result.fullName(), is(Integer.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("int"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeIntegerPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "int");

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("int"));
    }

    @Test
    public void applyGeneratesBigInteger() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        when(config.isUseBigIntegers()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigInteger.class.getName()));
    }

    @Test
    public void applyGeneratesBigIntegerOverridingLong() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        // isUseBigIntegers should override isUseLongIntegers
        when(config.isUseBigIntegers()).thenReturn(true);
        when(config.isUseLongIntegers()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigInteger.class.getName()));
    }

    @Test
    public void applyGeneratesBigDecimal() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        when(config.isUseBigDecimals()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigDecimal.class.getName()));
    }

    @Test
    public void applyGeneratesBigDecimalOverridingDouble() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        //this shows that isUseBigDecimals overrides isUseDoubleNumbers
        when(config.isUseDoubleNumbers()).thenReturn(true);
        when(config.isUseBigDecimals()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigDecimal.class.getName()));
    }


    @Test
    public void applyGeneratesIntegerUsingJavaTypeInteger() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "java.lang.Integer");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Integer"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "long");

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLong() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "java.lang.Long");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMaximumGreaterThanIntegerMax() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMaximumGreaterThanIntegerMax() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMaximumLessThanIntegerMin() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMaximumLessThanIntegerMin() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMinimumLessThanIntegerMin() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMinimumLessThanIntegerMin() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMinimumGreaterThanIntegerMax() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMinimumGreaterThanIntegerMax() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeBigInteger() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "java.math.BigInteger");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.math.BigInteger"));
    }

    @Test
    public void applyGeneratesNumber() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        when(config.isUseDoubleNumbers()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Double.class.getName()));
    }

    @Test
    public void applyGeneratesNumberPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        when(config.isUsePrimitives()).thenReturn(true);
        when(config.isUseDoubleNumbers()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeFloatPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "float");

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("float"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeFloat() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "java.lang.Float");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Float"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeDoublePrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "double");

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeDouble() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "java.lang.Double");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeBigDecimal() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "java.math.BigDecimal");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.math.BigDecimal"));
    }

    @Test
    public void applyGeneratesBoolean() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "boolean");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Boolean.class.getName()));
    }

    @Test
    public void applyGeneratesBooleanPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "boolean");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("boolean"));
    }

    @Test
    public void applyGeneratesAnyAsObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "any");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesNullAsObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "null");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesArray() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "array");

        JClass mockArrayType = mock(JClass.class);
        ArrayRule mockArrayRule = mock(ArrayRule.class);
        when(mockArrayRule.apply("fooBar", objectNode, null, jpackage, null)).thenReturn(mockArrayType);
        when(ruleFactory.getArrayRule()).thenReturn(mockArrayRule);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result, is((JType) mockArrayType));
    }

    @Test
    public void applyGeneratesCustomObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "object");

        JDefinedClass mockObjectType = mock(JDefinedClass.class);
        ObjectRule mockObjectRule = mock(ObjectRule.class);
        when(mockObjectRule.apply("fooBar", objectNode, null, jpackage, null)).thenReturn(mockObjectType);
        when(ruleFactory.getObjectRule()).thenReturn(mockObjectRule);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result, is((JType) mockObjectType));
    }

    @Test
    public void applyChoosesObjectOnUnrecognizedType() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "unknown");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));

    }

    @Test
    public void applyDefaultsToTypeAnyObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

}
