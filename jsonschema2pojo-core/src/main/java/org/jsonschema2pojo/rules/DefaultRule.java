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

import static org.apache.commons.lang3.StringUtils.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import scala.annotation.meta.field;

/**
 * Applies the "default" schema rule.
 *
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20</a>
 */
public class DefaultRule implements Rule<JFieldVar, JFieldVar> {

    private final RuleFactory ruleFactory;
    public static int[] branchIDs = new int[13];

    public DefaultRule(RuleFactory ruleFactory, int[] branchIDs)
    {
        this.ruleFactory = ruleFactory;
        if (branchIDs == null || branchIDs.length == 0)
            this.branchIDs = new int[12];
        else
            this.branchIDs = branchIDs;
    }

    public DefaultRule(RuleFactory ruleFactory) {

        this.ruleFactory = ruleFactory;
        //branchIDs = new int[]{0,0,0};

        if (branchIDs == null || branchIDs.length == 0)
            this.branchIDs = new int[12];
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * Default values are implemented by assigning an expression to the given
     * field (so when instances of the generated POJO are created, its fields
     * will then contain their default values).
     * <p>
     * Collections (Lists and Sets) are initialized to an empty collection, even
     * when no default value is present in the schema (node is null).
     *
     * @param nodeName
     *            the name of the property which has (or may have) a default
     * @param node
     *            the default node (may be null if no default node was present
     *            for this property)
     * @param field
     *            the Java field that has added to a generated type to represent
     *            this property
     * @return field, which will have an init expression is appropriate
     */
    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema currentSchema) {

        //TODO Complexity = 9
        boolean defaultPresent = node != null && isNotEmpty(node.asText());

        String fieldType = field.type().fullName();

        if (defaultPresent && !field.type().isPrimitive() && node.isNull()) {
            System.out.println("Branch ID: D1");
            field.init(JExpr._null());

        } else if (fieldType.startsWith(List.class.getName())) {
            System.out.println("Branch ID: D2");
            field.init(getDefaultList(field.type(), node));

        } else if (fieldType.startsWith(Set.class.getName())) {
            System.out.println("Branch ID: D3");
            field.init(getDefaultSet(field.type(), node));
        } else if (fieldType.startsWith(String.class.getName()) && node != null ) {
            System.out.println("Branch ID: D4");
            field.init(getDefaultValue(field.type(), node));
        } else if (defaultPresent) {
            System.out.println("Branch ID: D5");
            field.init(getDefaultValue(field.type(), node));

        }
        return field;
    }

    static JExpression getDefaultValue(JType fieldType, JsonNode node) {
        return getDefaultValue(fieldType, node.asText());
    }

    public int[] getIDlist()
    {
        return branchIDs;
    }

    public static void increaseAndPrint(int i)
    {
        branchIDs[i]++;
        System.out.println("Branch ID: DG" + i);
    }

    //TODO> complexity = 16, and it is actully a public method (static)
    //    static JExpression getDefaultValue(JType fieldType, String value)
    public static JExpression getDefaultValue(JType fieldType, String value)
    {
        //If this class is a wrapper type for a primitive, return the primitive type. Otherwise return this.
        // For example, for "java.lang.Integer", this method returns "int".
        fieldType = fieldType.unboxify();

        if (fieldType.fullName().equals(String.class.getName())) { //TODO done string
            increaseAndPrint(0);
            return JExpr.lit(value);

        } else if (fieldType.fullName().equals(int.class.getName())) { //TODO done int
            increaseAndPrint(1);
            return JExpr.lit(Integer.parseInt(value));

        } else if (fieldType.fullName().equals(BigInteger.class.getName())) { //TODO done bigInt
            increaseAndPrint(2);
            return JExpr._new(fieldType).arg(JExpr.lit(value));

        } else if (fieldType.fullName().equals(double.class.getName())) { //TODO done double
            increaseAndPrint(3);
            return JExpr.lit(Double.parseDouble(value));

        } else if (fieldType.fullName().equals(BigDecimal.class.getName())) {  //TODO done bigDec
            increaseAndPrint(4);
            return JExpr._new(fieldType).arg(JExpr.lit(value));

        } else if (fieldType.fullName().equals(boolean.class.getName())) { //TODO done boolean
            increaseAndPrint(5);
            return JExpr.lit(Boolean.parseBoolean(value));

        } else if (fieldType.fullName().equals(DateTime.class.getName()) || fieldType.fullName().equals(Date.class.getName())) {
            increaseAndPrint(6);                            //TODO done date/ dateTime
            long millisecs = parseDateToMillisecs(value);

            JInvocation newDateTime = JExpr._new(fieldType);
            newDateTime.arg(JExpr.lit(millisecs));

            return newDateTime;

        } else if (fieldType.fullName().equals(LocalDate.class.getName()) || fieldType.fullName().equals(LocalTime.class.getName())) {
            increaseAndPrint(7);                                   //TODO done localTime and localDate
            JInvocation stringParseableTypeInstance = JExpr._new(fieldType);
            stringParseableTypeInstance.arg(JExpr.lit(value));
            return stringParseableTypeInstance;

        } else if (fieldType.fullName().equals(long.class.getName())) { //TODO done long
            increaseAndPrint(8);
            return JExpr.lit(Long.parseLong(value));

        } else if (fieldType.fullName().equals(float.class.getName())) { //TODO done float
            increaseAndPrint(9);
            return JExpr.lit(Float.parseFloat(value));

        } else if (fieldType.fullName().equals(URI.class.getName())) {  //TODO done URI
            increaseAndPrint(10);
            JInvocation invokeCreate = fieldType.owner().ref(URI.class).staticInvoke("create");
            return invokeCreate.arg(JExpr.lit(value));

        } else if (fieldType instanceof JDefinedClass && ((JDefinedClass) fieldType).getClassType().equals(ClassType.ENUM)) {
            increaseAndPrint(11);
            return getDefaultEnum(fieldType, value);

        } else {                                //TODO done empty
            increaseAndPrint(12);
            return JExpr._null();

        }

    }

    /**
     * Creates a default value for a list property by:
     * <ol>
     * <li>Creating a new {@link ArrayList} with the correct generic type
     * <li>Using {@link Arrays#asList(Object...)} to initialize the list with
     * the correct default values
     * </ol>
     *
     * @param fieldType
     *            the java type that applies for this field ({@link List} with
     *            some generic type argument)
     * @param node
     *            the node containing default values for this list
     * @return an expression that creates a default value that can be assigned
     *         to this field
     */
    private JExpression getDefaultList(JType fieldType, JsonNode node) {

        JClass listGenericType = ((JClass) fieldType).getTypeParameters().get(0);

        JClass listImplClass = fieldType.owner().ref(ArrayList.class);
        listImplClass = listImplClass.narrow(listGenericType);

        JInvocation newListImpl = JExpr._new(listImplClass);

        if (node instanceof ArrayNode && node.size() > 0) {
            JInvocation invokeAsList = fieldType.owner().ref(Arrays.class).staticInvoke("asList");
            for (JsonNode defaultValue : node) {
                invokeAsList.arg(getDefaultValue(listGenericType, defaultValue));
            }
            newListImpl.arg(invokeAsList);
        } else if (!ruleFactory.getGenerationConfig().isInitializeCollections()) {
            return JExpr._null();
        }

        return newListImpl;

    }

    /**
     * Creates a default value for a set property by:
     * <ol>
     * <li>Creating a new {@link LinkedHashSet} with the correct generic type
     * <li>Using {@link Arrays#asList(Object...)} to initialize the set with the
     * correct default values
     * </ol>
     *
     * @param fieldType
     *            the java type that applies for this field ({@link Set} with
     *            some generic type argument)
     * @param node
     *            the node containing default values for this set
     * @return an expression that creates a default value that can be assigned
     *         to this field
     */
    private JExpression getDefaultSet(JType fieldType, JsonNode node) {

        JClass setGenericType = ((JClass) fieldType).getTypeParameters().get(0);

        JClass setImplClass = fieldType.owner().ref(LinkedHashSet.class);
        setImplClass = setImplClass.narrow(setGenericType);

        JInvocation newSetImpl = JExpr._new(setImplClass);

        if (node instanceof ArrayNode && node.size() > 0) {
            JInvocation invokeAsList = fieldType.owner().ref(Arrays.class).staticInvoke("asList");
            for (JsonNode defaultValue : node) {
                invokeAsList.arg(getDefaultValue(setGenericType, defaultValue));
            }
            newSetImpl.arg(invokeAsList);
        } else if (!ruleFactory.getGenerationConfig().isInitializeCollections()) {
            return JExpr._null();
        }

        return newSetImpl;

    }

    /**
     * @see EnumRule
     */
    private static JExpression getDefaultEnum(JType fieldType, String value) {

        JDefinedClass enumClass = (JDefinedClass) fieldType;
        JType backingType = enumClass.fields().get("value").type();
        JInvocation invokeFromValue = enumClass.staticInvoke("fromValue");
        invokeFromValue.arg(getDefaultValue(backingType, value));

        return invokeFromValue;
    }

    private static long parseDateToMillisecs(String valueAsText) {

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
