![GitHub](https://img.shields.io/github/license/eclipse-serializer/serializer?style=for-the-badge)
![GitHub issues](https://img.shields.io/github/issues/eclipse-serializer/serializer?style=for-the-badge)
![Maven Central](https://img.shields.io/maven-central/v/org.eclipse.serializer/serializer?style=for-the-badge)


# Eclipse Serializer
## High secure serialization for the JVM and Android

- Strict separation of data and code
- Neither code nor any class information is transferred
- No code is executed through deserialization
- Objects are generated by the framework
- Injecting malicious code won't have any consequences because executing is impossible
- Implementing the interface java.io.Serializable is not required at all
- Serialize any object
- Any object from 3rd party APIs can be serialized
- Built for any complex object graphs, circular references are troublefree
- The depth of an object graph is not limited, no stack-based recursion
- No requirements to your classes, no interfaces, superclasses or annotations required

### Serialize any object
Eclipse Serializer lets you serialize any object. There are no specific superclasses, interfaces or annotations at all. Even serializing objects from third-party APIs is trouble-free.
- Implementing Serializable is not required
- No superclasses, interfaces, annotations
- Inheritance is trouble-free
- Custom-tailored type handling

### Built for complex object graphs
The strength of it is that you can serialize any object graph of any size and complexity. The more complex your object graph is, the faster it will get in comparison.
- Any circular reference
- The depth of an object graph is not limited
- No stack-based recursion

### Optimized byte format
Eclipse Serializer uses a highly optimized byte format. This enables circular references, unlimited size, complexity, and depth of your object graphs, and minimizes overhead.

### Converter
The byte format can be converted into CSV and other encoding formats.

### Versioning
Classes evolve over time. Therefore, Eclipse Serializer provides a legacy type mapping that lets you manage different versions of your classes.
- New, changed and removed fields
- Type conversion
- Renamed and moved classes

## Usage

```xml
<dependency>
  <groupId>org.eclipse.serializer</groupId>
  <artifactId>serializer</artifactId>
  <version>1.3.1</version>
</dependency>
```

```java
// create a company object
Company company = new Company();
company.setName("Acme Inc.");
// create a serializer which handles byte arrays
Serializer<byte[]> serializer = Serializer.Bytes();

// serialize a company
byte[] data = serializer.serialize(company);

// deserialize the data back to a company
company = serializer.deserialize(data);

if(company.getName().equals("Acme Inc.")) {
    System.out.println("It works!");
}
```

## Applications

Eclipse Serializer is used in [EclipseStore](https://github.com/eclipse-store/store).

## Contribute

If you want to contribute to this project, please read our [guidelines](CONTRIBUTING.md).

## Links

- [Eclipse project page](https://projects.eclipse.org/projects/technology.serializer)
- [Dev mailing list](https://accounts.eclipse.org/mailing-list/serializer-dev)
