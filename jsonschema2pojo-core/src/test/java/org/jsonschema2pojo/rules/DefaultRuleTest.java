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
    private BufferedReader br;
    private String path = "target/generated-sources/java";

    private TypeRule rule = new TypeRule(ruleFactory);

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
        String className = "JavaFile";
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
        assertThat(false, is(true));
    }

    @Test
    public void getDefaultValueDouble() //TODO test double with the other method
    {
        String className = "JavaFile";
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
            generateJField(codeModel, className, typeName, varName, ex);

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


    private JFieldVar generateJField(JCodeModel codeModel, String className, String typeName,
                                     String varName, JExpression ex) throws JClassAlreadyExistsException, ClassNotFoundException
    {
        JDefinedClass dClass = codeModel._class(JMod.PUBLIC, className, ClassType.CLASS);
        JFieldVar fieldVar = dClass.field(JMod.PRIVATE, codeModel.parseType(typeName), varName);
        fieldVar.init(ex);
        return fieldVar;
    }


    private JType generateJType(String value)
    {
        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", value);
        JType type = rule.apply("NotUsed", objectNode, null, jpackage, null);
        return type;
    }



}
