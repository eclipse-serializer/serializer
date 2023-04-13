# Prepare code drop for Eclipse Serializer

Within this repo, the code donation of the Eclipse Serializer is prepared.

Scope of Proposal

With the Eclipse Serializer project, you can (de-)serialise any Java object without the need for an annotation, superclass or interface or a data schema that generates code.
Besides the serialisation functionality, you can customise many aspects, has support for circular references and data model evolution (refactoring) as you can define mappings for the binary data to your current class model.


General rules

- No functional or architectural changes. Code will be donated as is. 
- Only code required for the Serialization functionality will be included (all parts related to the StorageManager concept will be part of Eclipse Storage project)
- Moving classes to new artifacts can be necessary to make sure the Serializer parts (like Serializer Foundation and specific Storer) are not leaking into the Eclipse Storage (FIXME Not final name) when they depend on these artifacts.
- Addition of high level integration tests to make sure the serializer works (and is compatible with binary format of MicroStream) is required.
- Addition of user guide in Asciidoc is needed so that developers know what to do and how features can be used.


## Good to know

Run only the integration tests

```
mvn clean verify -f tests/pom.xml
```

Build only the documentation PDF

```
mvn generate-resources -pl :documentation
```

