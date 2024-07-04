# Dataform Repo Get
When we use Dataform on Google Cloud, an internal git repository is created.
We can see the files in this repository from the console.  However, what if we
want to download a copy of this repository?  It doesn't expose itself as a remote
Git server.  Fortunately, there are Dataform APIs to be able to work with the
individual files.  What this utility provides is a mechanism to supply the identity
of the repository and download the complete set of files.

The parameter to the utility are:

* `--project` - The identity of the Google Cloud project in which Dataform is configured.
* `--location` - The identity of the location (region) where Dataform is configured.
* `--repository` - The identity of the Dataform repository.
* `--workspace` - The identity of the workspace in the Dataform repository.
* `--output` - (**optional**) The output directory.  Defaults to `out`.x

For example:

```
<Utility> --project <MyProject> \
  --location <MyLocation> \
  --repository <MyRepository> \
  --workspace <MyWorkspace>
```

## Building
The utility is provided in source and leverages Maven for build.  It was tested again Java 17.

```bash
mvn package
```

### Run the utility

```bash
mvn exec:java -Dexec.mainClass=com.google.srtt.dataform.repo_get.Main -Dexec.args="--project=<project> --location=<location> --repository=<repo> --workspace=<workspace>"
```

Replace the placeholders (e.g., bv-cdip-431876591-poc, us-east1) with your actual Google Cloud project, location, repository, and workspace values.

The repository is written to a local directory called `out`.


