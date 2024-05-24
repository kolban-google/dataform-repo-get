/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.srtt.dataform.repo_get;

import com.google.cloud.dataform.v1beta1.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class Main {
  // Design:
  // Query Dataform and start iterating over each of the files and directories in the repo.
  // We use recursion for directory processing.

  private String project;
  private String location;
  private String repository;
  private String workspace;
  private String outputDirectory;
  private DataformClient dataformClient;

  /**
   * The repo entries to exclude from download/copy.
   */
  private static final List<String> excludes = Arrays.asList();

  /**
   * Get the string representation of the workspace.
   * @return The string representation of the workspace.
   */
  private String getWorkspace() {
    return WorkspaceName.of(project, location, repository, workspace).toString();
  } // getWorkspace

  /**
   * Process a file name that we have found in the repository.  We retrieve the content of the file and
   * write it to a local file of the same name.  We take account of any relative directory path.
   * @param fileName The name of a file found in the respository.
   * @throws Exception
   */
  private void processFile(String fileName) throws Exception {
    System.out.println("Processing file name: " + fileName);
    ReadFileRequest fileRequest = ReadFileRequest.newBuilder()
      .setWorkspace(getWorkspace())
      .setPath(fileName)
      .build();
    ReadFileResponse response = dataformClient.readFile(fileRequest);
    String content = response.getFileContents().toStringUtf8();
    File file = new File(outputDirectory, fileName);
    FileUtils.writeByteArrayToFile(file, response.getFileContents().toByteArray());
    //System.out.println(content);
  } // processFile

  /**
   * Process a directory name that we have found in the repository.  We process a directory by getting all the
   * entries in it.  For a file, we process the file.  For a directory, we recursively call ourselves to process
   * that new directory.  There are some entries that we wish to exclude and these are also honored.
   * @param directoryName The directory to retrieve from the repository.  A value of "" means the root.
   * @throws Exception
   */
  private void processDirectory(String directoryName) throws Exception {
    System.out.println("Processing directory: " + directoryName);

    // Check if the directory is to be excluded.
    if (excludes.contains(directoryName)) {
      System.out.println("Skipping: " + directoryName);
      return;
    }

    // Retrieve the entries for the current directory.
    QueryDirectoryContentsRequest request = QueryDirectoryContentsRequest.newBuilder()
      .setWorkspace(getWorkspace())
      .setPath(directoryName)
      .build();
    DataformClient.QueryDirectoryContentsPagedResponse response = dataformClient.queryDirectoryContents(request);

    // Iterate through each of the directory entries.
    for (DirectoryEntry entry: response.iterateAll()) {
      //System.out.println(entry);
      if (entry.getEntryCase() == DirectoryEntry.EntryCase.FILE) {
        processFile(entry.getFile());
      } else if (entry.getEntryCase() == DirectoryEntry.EntryCase.DIRECTORY) {
        processDirectory(entry.getDirectory());
      } else {
        throw new Exception("Unexpected entry case: " + entry.getEntryCase());
      }
    } // End of iterate through all directory entries.
  } // processDirectory

  /**
   * Run the application.
   */
  public void run(String[] args) {
    try {
      dataformClient = DataformClient.create();

      Option projectOption = Option.builder()
        .longOpt("project")
        .argName("project")
        .hasArg()
        .desc("The GCP project")
        .required()
        .build();

      Option locationOption = Option.builder()
        .longOpt("location")
        .argName("location")
        .hasArg()
        .desc("The location")
        .required()
        .build();

      Option repositoryOption = Option.builder()
        .longOpt("repository")
        .argName("repository")
        .hasArg()
        .desc("The Dataform repository")
        .required()
        .build();

      Option workspaceOption = Option.builder()
        .longOpt("workspace")
        .argName("workspace")
        .hasArg()
        .desc("The Dataform workspace")
        .required()
        .build();

      Option outputOption = Option.builder()
        .longOpt("output")
        .argName("output")
        .hasArg()
        .desc("The output directory")
        .required(false)
        .build();

      Options options = new Options();
      options.addOption(projectOption);
      options.addOption(locationOption);
      options.addOption(repositoryOption);
      options.addOption(workspaceOption);
      options.addOption(outputOption);
      CommandLineParser parser = new DefaultParser();
      CommandLine line = parser.parse(options, args);

      project = line.getOptionValue(projectOption);
      location = line.getOptionValue(locationOption);
      repository = line.getOptionValue(repositoryOption);
      workspace = line.getOptionValue(workspaceOption);
      outputDirectory = line.getOptionValue(outputOption, "out");

      //System.out.println("Created the Dataform client");
      processDirectory("");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  } // run

  /**
   * Main entry into the application.
   * @param args The arguments passed into the application.
   */
  public static void main(String[] args) {
    Main main = new Main();
    main.run(args);
  } // main
} // Main