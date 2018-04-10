/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CatchExceptionStrategyTest {

  private static final String CATCH_SAMPLE_XML = "catchExceptionStrategy.xml";
  private static final Path CATCH_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path CATCH_SAMPLE_PATH = CATCH_EXAMPLES_PATH.resolve(CATCH_SAMPLE_XML);

  private CatchExceptionStrategy catchExceptionStrategy;
  private Element node;

  @Before
  public void setUp() throws Exception {
    catchExceptionStrategy = new CatchExceptionStrategy();
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    catchExceptionStrategy.execute(null);
  }

  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(CATCH_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, catchExceptionStrategy.getAppliedTo().getExpression()).get(0);
    catchExceptionStrategy.execute(node);

    assertThat("The node didn't change", node.getName(), is("error-handler"));
  }
}