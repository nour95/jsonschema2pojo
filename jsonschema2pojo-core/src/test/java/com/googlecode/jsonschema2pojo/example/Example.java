/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo.example;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.SchemaMapperImpl;
import com.sun.codemodel.JCodeModel;

public class Example {

    public static void main(String[] args) throws IOException {
        
        // BEGIN EXAMPLE
        
        JCodeModel codeModel = new JCodeModel();
        
        URL source = new File("schema.json").toURI().toURL();
        
        SchemaMapper schemaMapper = new SchemaMapperImpl();
        schemaMapper.generate(codeModel, "ClassName", "com.example", source);
        
        codeModel.build(new File("output"));
        
        // END EXAMPLE

    }
    
}