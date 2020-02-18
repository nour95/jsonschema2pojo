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

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.text.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import java.util.Date;
import java.net.URI;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.sun.codemodel.*;
import org.joda.time.DateTime;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.example.Example;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;
import org.jsonschema2pojo.util.NameHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DefaultRuleTest {


    //https://stackoverflow.com/questions/25751758/how-to-assign-a-value-to-a-specific-index-of-an-array-with-java-codemodel

    private GenerationConfig config = mock(GenerationConfig.class);
    private RuleFactory ruleFactory = mock(RuleFactory.class);
    private BufferedReader br;
    private String path = "target/generated-sources/java";

    private TypeRule rule = new TypeRule(ruleFactory);
    private String className = "JavaFile";


    @Before
    public void wireUpConfig() {
        when(ruleFactory.getGenerationConfig()).thenReturn(config);
    }

    @AfterClass
    public static void runOnceAfterClass()
    {
        System.out.println("-----------------------------------------");
        int[] branchIDs = DefaultRule.branchIDs;
        for (int i = 0; i < branchIDs.length; i++)
            System.out.println("Branch ID: DG" + i + " has been tested " + branchIDs[i] + " times");

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


    @Test
    public void getDefaultValueInteger()
    {
        String varName = "numberToTest";
        String typeName = "int";
        int valueToCheck = 43;
        boolean expIsSavedCorrectly = false;

        JCodeModel codeModel = new JCodeModel();

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        JType type = rule.apply("NotUsed", objectNode, null, jpackage, null);

        JExpression ex = DefaultRule.getDefaultValue(type, "" + valueToCheck);


        try {
            JDefinedClass dClass = codeModel._class(JMod.PUBLIC, className, ClassType.CLASS);
            JFieldVar fieldVar = dClass.field(JMod.PRIVATE, codeModel.parseType(typeName), varName);
            fieldVar.init(ex);


            File target = new File(path);

            codeModel.build(target);

            String dataNeeded = "private " + typeName + " " + varName + " = " + valueToCheck + ";";
            expIsSavedCorrectly = fileContains(className, dataNeeded);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        assertThat(expIsSavedCorrectly, is( true ));
    }

    @Test
    public void getDefaultValueBigInteger()
    {
        String varName = "numberToTest";
        String typeName = "long";

        long valueToCheck = 150150150;
        boolean expIsSavedCorrectly = false;

        JCodeModel codeModel = new JCodeModel();

        when(config.isUseBigIntegers()).thenReturn(true);

        JType type = generateJType("integer");
        JExpression ex = DefaultRule.getDefaultValue(type, "" + valueToCheck);


        try {
            generateJField(codeModel, typeName, varName, ex);

            File target = new File(path);

            codeModel.build(target);

            String dataNeeded = "private " + typeName + " " + varName + " = new BigInteger(\"" + valueToCheck + "\");";
            expIsSavedCorrectly = fileContains(className, dataNeeded);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        assertThat(expIsSavedCorrectly, is( true ));


    }

    @Test
    public void getDefaultValueDouble() //TODO test double with the other method
    {
        String varName = "numberToTest";
        String typeName = "double";

        double valueToCheck = 43.37;
        boolean expIsSavedCorrectly = false;

        JCodeModel codeModel = new JCodeModel();

        when(config.isUsePrimitives()).thenReturn(true);
        when(config.isUseDoubleNumbers()).thenReturn(true);

        JType type = generateJType("number");
        JExpression ex = DefaultRule.getDefaultValue(type, "" + valueToCheck);


        try {
            generateJField(codeModel, typeName, varName, ex);

            File target = new File(path);

            codeModel.build(target);

            String dataNeeded = "private " + typeName + " " + varName + " = " + valueToCheck + "D;";
            expIsSavedCorrectly = fileContains(className, dataNeeded);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        assertThat(expIsSavedCorrectly, is( true ));
    }


    @Test
    public void getDefaultValueBigDecimal()
    {
        String varName = "numberToTest";
        String typeName = "double";

        double valueToCheck = 150150150.150;
        boolean expIsSavedCorrectly = false;

        JCodeModel codeModel = new JCodeModel();

        when(config.isUseBigDecimals()).thenReturn(true);

        JType type = generateJType("number");
        JExpression ex = DefaultRule.getDefaultValue(type, "" + valueToCheck);


        try {
            generateJField(codeModel, typeName, varName, ex);

            File target = new File(path);

            codeModel.build(target);

            String dataNeeded = "private " + typeName + " " + varName + " = new BigDecimal(\"" + valueToCheck + "\");";
            expIsSavedCorrectly = fileContains(className, dataNeeded);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        assertThat(expIsSavedCorrectly, is( true ));

    }


    @Test
    public void getDefaultValueBoolean()
    {
        String varName1 = "numberToTest1";
        String varName2 = "numberToTest2";
        String typeName = "boolean";

        boolean valueToCheck1 = false;
        boolean valueToCheck2 = true;
        boolean expIsSavedCorrectly = false;
        boolean expIsSavedCorrectly2 = false;


        JCodeModel codeModel = new JCodeModel();


        JType type = generateJType("boolean");
        JExpression ex1 = DefaultRule.getDefaultValue(type, "" + valueToCheck1);
        JExpression ex2 = DefaultRule.getDefaultValue(type, "" + valueToCheck2);


        try {
            JDefinedClass dClass = codeModel._class(JMod.PUBLIC, className, ClassType.CLASS);

            generateJField(codeModel, dClass, typeName, varName1, ex1);
            generateJField(codeModel, dClass, typeName, varName2, ex2);

            File target = new File(path);

            codeModel.build(target);

            String dataNeeded1 = "private " + typeName + " " + varName1 + " = " + valueToCheck1 + ";";
            expIsSavedCorrectly = fileContains(className, dataNeeded1);
            String dataNeeded2 = "private " + typeName + " " + varName2 + " = " + valueToCheck2 + ";";
            expIsSavedCorrectly2 = fileContains(className, dataNeeded2);


        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        assertThat(expIsSavedCorrectly, is( true ));
        assertThat(expIsSavedCorrectly2, is( true ));

    }


    @Test
    public void getDefaultValueDateTime() {
        String varName = "numberToTest";
        String varName2 = "numberToTest2";

        String typeName = "DateTime";

        DateTime dateTime = new DateTime();

        Date date = new Date();
        long valueToChecked2 = date.getTime();

        boolean expIsSavedCorrectly = false;
        boolean expIsSavedCorrectly2 = false;

        JCodeModel codeModel = new JCodeModel();
        when(config.isUseJodaDates()).thenReturn(true);

        JType type = generateJType("string", "date-time");

        JExpression ex = DefaultRule.getDefaultValue(type, "" + dateTime.toString());
        JExpression ex2 = DefaultRule.getDefaultValue(type, "" + valueToChecked2);

        try {
            JDefinedClass dClass = codeModel._class(JMod.PUBLIC, className, ClassType.CLASS);

            generateJField(codeModel, dClass, typeName, varName, ex);
            generateJField(codeModel, dClass, typeName, varName2, ex2);

            File target = new File(path);
            codeModel.build(target);

            long valueToChecked =  new StdDateFormat().parse("" + dateTime).getTime();
            String dataNeeded = "private " + typeName + " " + varName + " = new org.joda.time.DateTime(" + valueToChecked + "L);";
            expIsSavedCorrectly = fileContains(className, dataNeeded);

            //long valueToChecked2 = dateTime.getMillis();
            String dataNeeded2 = "private " + typeName + " " + varName2 + " = new org.joda.time.DateTime(" + valueToChecked2 + "L);";
            expIsSavedCorrectly2 = fileContains(className, dataNeeded2);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException | ParseException e ) {
            System.out.println(e);
        }

        assertThat(expIsSavedCorrectly, is( true ));
        assertThat(expIsSavedCorrectly2, is( true ));

    }

    @Test
    public void getDefaultValueLocalDate()
    {
        String varName = "numberToTest";
        String varName2 = "numberToTest2";

        String typeName = "DateTime";

        LocalDate localDate = new LocalDate();
        LocalTime localTime = new LocalTime();

        boolean expIsSavedCorrectly = false;
        boolean expIsSavedCorrectly2 = false;

        JCodeModel codeModel = new JCodeModel();
        when(config.isUseJodaLocalDates()).thenReturn(true);
        when(config.isUseJodaLocalTimes()).thenReturn(true);

        JType type = generateJType("string", "date");
        JType type2 = generateJType("string", "time");

        JExpression ex = DefaultRule.getDefaultValue(type, "" + localDate);
        JExpression ex2 = DefaultRule.getDefaultValue(type2, "" + localTime);

        try {
            JDefinedClass dClass = codeModel._class(JMod.PUBLIC, className, ClassType.CLASS);

            generateJField(codeModel, dClass, typeName, varName, ex);
            generateJField(codeModel, dClass, typeName, varName2, ex2);

            File target = new File(path);
            codeModel.build(target);

            String dataNeeded = "private " + typeName + " " + varName + " = new LocalDate(\"" + localDate + "\");";
            expIsSavedCorrectly = fileContains(className, dataNeeded);

            String dataNeeded2 = "private " + typeName + " " + varName2 + " = new LocalTime(\"" + localTime + "\");";
            expIsSavedCorrectly2 = fileContains(className, dataNeeded2);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException e ) {
            System.out.println(e);
        }

        assertThat(expIsSavedCorrectly, is( true ));
        assertThat(expIsSavedCorrectly2, is( true ));


    }


    @Test
    public void getDefaultValueLong()
    {
        String varName = "numberToTest";
        String typeName = "long";

        long valueToCheck = 150150150;
        boolean expIsSavedCorrectly = false;

        JCodeModel codeModel = new JCodeModel();

        when(config.isUsePrimitives()).thenReturn(false);

        JType type = generateJType("integer", "existingJavaType", "long");
        JExpression ex = DefaultRule.getDefaultValue(type, "" + valueToCheck);

        try {
            generateJField(codeModel, typeName, varName, ex);

            File target = new File(path);

            codeModel.build(target);

            String dataNeeded = "private " + typeName + " " + varName + " = " + valueToCheck + "L;";
            expIsSavedCorrectly = fileContains(className, dataNeeded);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        assertThat(expIsSavedCorrectly, is( true ));


    }

    @Test
    public void getDefaultValueFloat()
    {
        String varName = "numberToTest";
        String typeName = "float";

        float valueToCheck = 10.01f;
        boolean expIsSavedCorrectly = false;

        JCodeModel codeModel = new JCodeModel();

        when(config.isUsePrimitives()).thenReturn(false);

        JType type = generateJType("number", "existingJavaType", "float");
        JExpression ex = DefaultRule.getDefaultValue(type, "" + valueToCheck);

        try {
            generateJField(codeModel, typeName, varName, ex);

            File target = new File(path);

            codeModel.build(target);

            String dataNeeded = "private " + typeName + " " + varName + " = " + valueToCheck + "F;";
            expIsSavedCorrectly = fileContains(className, dataNeeded);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        assertThat(expIsSavedCorrectly, is( true ));


    }

    @Test
    public void getDefaultValueURI() throws URISyntaxException {
        String varName = "numberToTest";
        String typeName = "URI";

        String uriBase = "https://www.geeksforgeeks.org/";
        URI uri = new URI(uriBase);

        boolean expIsSavedCorrectly = false;

        JCodeModel codeModel = new JCodeModel();
        when(config.isUseJodaLocalDates()).thenReturn(true);

        JType type = generateJType("string", "uri");

        JExpression ex = DefaultRule.getDefaultValue(type, "" + uri);

        try {

            generateJField(codeModel, typeName, varName, ex);

            File target = new File(path);
            codeModel.build(target);

            String dataNeeded = "private " + typeName + " " + varName + " = java.net.URI.create(\"" + uri + "\");";
            expIsSavedCorrectly = fileContains(className, dataNeeded);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException e ) {
            System.out.println(e);
        }

        assertThat(expIsSavedCorrectly, is( true ));
    }

    private static class FirstArgAnswer<T> implements Answer<T> {
        @SuppressWarnings("unchecked")
        @Override
        public T answer(InvocationOnMock invocation) {
            Object[] args = invocation.getArguments();
            //noinspection unchecked
            return (T) args[0];
        }
    }

    @Test
    public void nourTest()
    {
        EnumRule rule2 = new EnumRule(ruleFactory);
        TypeRule typeRule = mock(TypeRule.class);
        Schema schema = mock(Schema.class);


        Answer<String> firstArgAnswer = new EnumRuleTest.FirstArgAnswer<>();

        NameHelper nameHelper = mock(NameHelper.class);
        Annotator annotator = mock(Annotator.class);
        //RuleLogger logger = mock(RuleLogger.class);

        when(nameHelper.getClassName(anyString(), Matchers.any(JsonNode.class))).thenAnswer(firstArgAnswer);
        when(nameHelper.replaceIllegalCharacters(anyString())).thenAnswer(firstArgAnswer);
        when(nameHelper.normalizeName(anyString())).thenAnswer(firstArgAnswer);

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectMapper objectMapper = new ObjectMapper();
        //ArrayNode arrayNode = objectMapper.createArrayNode();
        //arrayNode.add("open");
        //arrayNode.add("closed");
        ObjectNode enumNode = objectMapper.createObjectNode();
        enumNode.put("type", "string");
        //enumNode.set("enum", arrayNode);


        // We're always a string for the purposes of this test
        when(typeRule.apply("status", enumNode, null, jpackage, schema))
                .thenReturn(jpackage.owner()._ref(String.class));
        when(ruleFactory.getNameHelper()).thenReturn(nameHelper);
        //when(ruleFactory.getLogger()).thenReturn(logger);
        when(ruleFactory.getAnnotator()).thenReturn(annotator);
        when(ruleFactory.getTypeRule()).thenReturn(typeRule);


        JType type = rule2.apply("status", enumNode, null, jpackage, schema);

        boolean c = type instanceof JDefinedClass;
        ClassType d = ((JDefinedClass) type).getClassType(); //.equals(ClassType.ENUM);
        ClassType e = ClassType.ENUM;
        assertThat(d, is(e));
        System.out.println(d);

        JExpression ex = DefaultRule.getDefaultValue(type, "");

        JCodeModel codeModel = new JCodeModel();
        //codeModel.

        String s = ((JDefinedClass) ex).name();
        System.out.println("Happy "+ s);
        //File target = new File(path);
        //codeModel.build(target);



    }


    @Test
    public void nourTest2() {
        EnumRule rule2 = new EnumRule(ruleFactory);


        String varName = "numberToTest";
        String typeName = "EJa";

        boolean expIsSavedCorrectly = false;

        try {

            JCodeModel codeModel = new JCodeModel();
            //JDefinedClass dClass = codeModel._class(JMod.PUBLIC, "JavaEnum", ClassType.ENUM);
            //JType type = dClass;

            JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode enumNode = objectMapper.createObjectNode();
            enumNode.put("type", "string");

            JDefinedClass sd = rule2.createEnum(enumNode, "EJa", jpackage);

            //JDefinedClass defineClass = owner.defineClass(t, ClassType.ENUM)

            //boolean c = type instanceof JDefinedClass;
            //ClassType d = ((JDefinedClass) type).getClassType(); //.equals(ClassType.ENUM);
            ClassType dd = sd.getClassType();
            ClassType e = ClassType.ENUM;

            JExpression ex = DefaultRule.getDefaultValue(sd, "");
            generateJField(codeModel, typeName, varName, ex);

            String s = ((JDefinedClass) ex).name();
            System.out.println("Happy "+ s);

            File target = new File(path);
            codeModel.build(target);

            //String dataNeeded = "private " + typeName + " " + varName + " = java.net.URI.create(\"" + uri + "\");";
            //expIsSavedCorrectly = fileContains("JavaEnum", dataNeeded);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException | ClassAlreadyExistsException e ) {
            System.out.println(e);
        }

        //assertThat(expIsSavedCorrectly, is( true ));
    }

    @Test
    public void getDefaultValueEnum() {
        String varName = "numberToTest";
        String typeName = "URI";

        boolean expIsSavedCorrectly = false;

        try {

            JCodeModel codeModel = new JCodeModel();
            JDefinedClass dClass = codeModel._class(JMod.PUBLIC, "JavaEnum", ClassType.ENUM);
            JType type = dClass;

            //JDefinedClass defineClass = owner.defineClass(t, ClassType.ENUM)

            boolean c = type instanceof JDefinedClass;
            ClassType d = ((JDefinedClass) type).getClassType(); //.equals(ClassType.ENUM);
            ClassType e = ClassType.ENUM;

            JExpression ex = DefaultRule.getDefaultValue(type, "");
            generateJField(codeModel, typeName, varName, ex);

            File target = new File(path);
            codeModel.build(target);

            //String dataNeeded = "private " + typeName + " " + varName + " = java.net.URI.create(\"" + uri + "\");";
            //expIsSavedCorrectly = fileContains("JavaEnum", dataNeeded);

        }
        catch (JClassAlreadyExistsException | IOException | ClassNotFoundException e ) {
            System.out.println(e);
        }

        //assertThat(expIsSavedCorrectly, is( true ));
    }


    @Test
    public void getDefaultValueNull()
    {
        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "Person");
        JType type = rule.apply("fooBar", objectNode, null, jpackage, null);

        JExpression ex = DefaultRule.getDefaultValue(type, "");
        assertThat(ex, is(JExpr._null()));
    }


    //================================
    // Help functions:
    //================================


    private JFieldVar generateJField(JCodeModel codeModel, String typeName,
                                     String varName, JExpression ex) throws JClassAlreadyExistsException, ClassNotFoundException
    {
        JDefinedClass dClass = codeModel._class(JMod.PUBLIC, className, ClassType.CLASS);
        JFieldVar fieldVar = dClass.field(JMod.PRIVATE, codeModel.parseType(typeName), varName);
        fieldVar.init(ex);
        return fieldVar;
    }

    private JFieldVar generateJField(JCodeModel codeModel, JDefinedClass dClass, String typeName,
                                     String varName, JExpression ex) throws ClassNotFoundException
    {
        JFieldVar fieldVar = dClass.field(JMod.PRIVATE, codeModel.parseType(typeName), varName);
        fieldVar.init(ex);
        return fieldVar;
    }

    private JType generateJType(String value, String formatValue)
    {
        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", value);

        TextNode formatNode = TextNode.valueOf(formatValue);
        objectNode.set("format", formatNode);

        FormatRule mockFormatRule = mock(FormatRule.class);
        FormatRule fRule = new FormatRule(ruleFactory);


        when(ruleFactory.getFormatRule()).thenReturn(fRule);
        JType type = rule.apply("fooBar", objectNode, null, jpackage, null);
        return type;
    }

    private JType generateJType(String value)
    {
        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", value);
        JType type = rule.apply("NotUsed", objectNode, null, jpackage, null);
        return type;
    }

    private JType generateJType(String value, String type2, String value2)
    {
        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", value);
        objectNode.put(type2, value2);
        JType type = rule.apply("NotUsed", objectNode, null, jpackage, null);
        return type;
    }



    private boolean fileContains(String className, String dataNeeded)  //TODO delete Exceptio
    {
        File file = new File(path + "/" + className + ".java");

        try
        {
            br = new BufferedReader(new FileReader(file));

            String st;
            while ((st = br.readLine()) != null)
            {
                //System.out.println(st);
                if (st.contains(dataNeeded))
                    return true;
            }

        }
        catch (IOException e)
        {
            System.out.println(e);
        }

        return false;

    }

    private long parseDateToMillisecs(String valueAsText) {

        try {
            return Long.parseLong(valueAsText);
        } catch (NumberFormatException nfe) {
            try {
                return new StdDateFormat().parse(valueAsText).getTime();
            } catch (ParseException pe) {
                throw new IllegalArgumentException("Unable to parse this string as a date: " + valueAsText);
            }
        }

    }




}
