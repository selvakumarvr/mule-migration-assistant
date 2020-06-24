/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import static java.util.Arrays.stream;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RemoveBuildHelperMavenPluginTest {

  private static final String POM_WITH_HELPER_MAVEN_PLUGIN = "/pommodel/buildHelperMavenPlugin/pom.xml";
  private static final String POM_WITH_CHANGED_HELPER_MAVEN_PLUGIN = "/pommodel/buildHelperMavenPluginChanged/pom.xml";
  private static final String POM_WITH_ADDED_HELPER_MAVEN_PLUGIN = "/pommodel/buildHelperMavenPluginAdded/pom.xml";
  private static final String POM_WITHOUT_HELPER_MAVEN_PLUGIN = "/pommodel/simple-pom/pom.xml";

  @Rule
  public ReportVerification report = new ReportVerification();

  private PomModel model;
  private RemoveBuildHelperMavenPlugin removeBuildHelperMavenPlugin;

  @Before
  public void setUp() {
    removeBuildHelperMavenPlugin = new RemoveBuildHelperMavenPlugin();
  }

  @Test
  public void executeWhenBuildHelperMavenPluginIsPresent() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITH_HELPER_MAVEN_PLUGIN).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("build-helper-maven-plugin should be present in pom", isPluginInModel(), is(true));
    removeBuildHelperMavenPlugin.execute(model, report.getReport());
    assertThat("build-helper-maven-plugin should not be present in pom", isPluginInModel(), is(false));
  }

  /**
   * If the user updated the default config generated by Studio 6, we must keep the changes the user made.
   */
  @Test
  public void executeWhenBuildHelperMavenPluginIsPresentAndWasChanged()
      throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITH_CHANGED_HELPER_MAVEN_PLUGIN).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("build-helper-maven-plugin should be present in pom", isPluginInModel(), is(true));
    removeBuildHelperMavenPlugin.execute(model, report.getReport());

    Xpp3Dom addResourcesConfiguration = getAddResourcesConfig();
    stream(addResourcesConfiguration.getChild("resources").getChildren("resource"))
        .anyMatch(resource -> resource.getChild("directory").getValue().equals("src/main/mule/"));
    stream(addResourcesConfiguration.getChild("resources").getChildren("resource"))
        .noneMatch(resource -> resource.getChild("directory").getValue().equals("src/main/app/"));

    assertThat("build-helper-maven-plugin should still be present in pom", isPluginInModel(), is(true));
  }

  @Test
  public void executeWhenBuildHelperMavenPluginIsPresentAndConfigWasAdded()
      throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITH_ADDED_HELPER_MAVEN_PLUGIN).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("build-helper-maven-plugin should be present in pom", isPluginInModel(), is(true));
    removeBuildHelperMavenPlugin.execute(model, report.getReport());

    Xpp3Dom addResourcesConfiguration = getAddResourcesConfig();
    stream(addResourcesConfiguration.getChild("resources").getChildren("resource"))
        .anyMatch(resource -> resource.getChild("directory").getValue().equals("src/main/mule/"));
    stream(addResourcesConfiguration.getChild("resources").getChildren("resource"))
        .noneMatch(resource -> resource.getChild("directory").getValue().equals("src/main/app/"));

    assertThat("build-helper-maven-plugin should still be present in pom", isPluginInModel(), is(true));
  }

  private Xpp3Dom getAddResourcesConfig() {
    return model.getPlugins().get(0).getExecutions().get(0).getConfiguration();
  }

  @Test
  public void executeWhenBuildHelperMavenPluginIsNotPresent() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITHOUT_HELPER_MAVEN_PLUGIN).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("build-helper-maven-plugin should not be present in pom", isPluginInModel(), is(false));
    removeBuildHelperMavenPlugin.execute(model, report.getReport());
    assertThat("build-helper-maven-plugin should not be present in pom", isPluginInModel(), is(false));
  }

  public boolean isPluginInModel() {
    return model.getPlugins().stream()
        .anyMatch(plugin -> StringUtils.equals(plugin.getArtifactId(), "build-helper-maven-plugin"));
  }
}
