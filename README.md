# SmartScanner MRZ Parser

Parse and extract information from MRZ string. Used by [SmartScanner Core](https://github.com/idpass/smartscanner-core/) to extract data from the scanned MRZ.

## Installation

Declare Maven Central repository in the dependency configuration, then add this library in the dependencies. An example using `build.gradle`:

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation "org.idpass:smartscanner-mrz-scanner:0.0.1-SNAPSHOT"
}
```

If you want to build this library from source, instructions to do so can be found in the [Building from source](https://github.com/idpass/smartscanner-mrz-parser/wiki/Building-from-source) wiki page.

## Usage

Import the `MrzParser` class from the library. This provides methods for working with MRZ strings.

```java
import org.idpass.smartscanner.mrz.parser.innovatrics.MrzParser;
```

Call `MrzParser.parse()` to parse an MRZ string. Refer to the [API Reference](https://github.com/idpass/smartscanner-mrz-parser/wiki/API-Reference) for other available methods in the `MrzParser` class and the properties of the parsed record.

```java
String mrz = "I<UTOD231458907<<<<<<<<<<<<<<<\n" +
             "7408122F1204159UTO<<<<<<<<<<<6\n" +
             "ERIKSSON<<ANNA<MARIA<<<<<<<<<<";
MrzRecord parsed = MrzParser.parse(mrz);
```

## License

[GNU Lesser General Public License v3.0](LICENSE)
