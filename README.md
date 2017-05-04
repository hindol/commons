# Commons
Ad-hoc collection of re-usable Java classes.

[![](https://jitpack.io/v/hindol/commons.svg)](https://jitpack.io/#hindol/commons)

## Argument

Reads configuration parameters from environment variables and Java system properties.

### Usage

```java
String javaHome = Argument.resolver()
        .firstTry("javaHome", Argument.Type.PROPERTY) // -DjavaHome="..."
        .thenTry("JAVA_HOME", Argument.Type.ENVIRONMENT_VARIABLE) // export JAVA_HOME=...
        .orElse("/usr/lib/jvm/openjdk-8-jdk/")
        .resolve();
```

## BackOffPolicy

Back-off policy for anything web.

### Usage

```java
BackOffPolicy backOffPolicy = new ExponentialBackOffPolicy.Builder()
        .setInitialInterval(1, TimeUnit.SECONDS)
        .setMaxInterval(1, TimeUnit.MINUTES)
        .setMultiplier(1.5)
        .setMaxElapsedTime(15, TimeUnit.MINUTES)
        .setRandomizationFactor(0.5)
        .build();

// Pseudo code, won't compile
while (true) {
	try {
    	String response = url.fetch();
        backOffPolicy.reset();
    } catch (IOException ioe) {
    	long interval = backOffPolicy.nextIntervalMillis();
        if (interval != BackOffPolicy.STOP) {
        	Thread.sleep(interval);
        } else {
        	throw e;
        }
    }
}
```

## DirectoryWatcher

Watches one or more directories for changes using Java 7's WatchService API.

### Usage

```java
DirectoryWatcher watcher = new DirectoryWatcher.Builder()
        .addDirectories("/home/hindol/")
        .setPreExistingAsCreated(true)
        .build(new DirectoryWatcher.Listener() {
            public void onEvent(DirectoryWatcher.Event event, Path path) {
                switch (event) {
                    case ENTRY_CREATE:
                        System.out.println(path + " created.");
                        break;

                    case ENTRY_MODIFY:
                        ...

                    case ENTRY_DELETE:
                        ...
                }
            }
        });

try {
    watcher.start(); // Actual watching starts here
    TimeUnit.SECONDS.sleep(30);
    watcher.stop(); // Stop watching
} catch (Exception e) {
    // Do something
}
```

## URL

URL encoding/decoding and parser/builder. Definitely not as standards compliant as other libraries but it works!

### Usage

```java
assertEquals(
    URL.timesEncoded("namshi%253A%252F%252Fn%252Ftarget%252F%253Futm_source%253Dtestng"),
    2
);

assertEquals(
    URL.decode("namshi%253A%252F%252Fn%252Ftarget%252F%253Futm_source%253Dtestng", 2),
    "namshi://n/target/?utm_source=testng"
);

assertEquals(
    URL.encode("namshi%3A%2F%2Fn%2Ftarget%2F%3Futm_source%3Dtestng", 0),
    "namshi%3A%2F%2Fn%2Ftarget%2F%3Futm_source%3Dtestng"
);

assertEquals(
    URL.ensureProtocol("www.vizury.com", "https"),
    "https://www.vizury.com"
);
assertEquals(
    URL.ensureProtocol("http://www.vizury.com", "https"),
    "http://www.vizury.com"
);

assertTrue(URL.isDeepLink("mmyt://htl/listing/?checkin=05192016&checkout=05212016&city=GOI"));

assertEquals(
    URL.safeDecode("http://www.google.com?q=Guy+Ritchie"),
    "http://www.google.com?q=Guy+Ritchie"
);

URL.Parser parser = URL.parse("mmyt://htl/listing/?checkin=05192016&checkout=05212016&city=GOI");

assertEquals(parser.protocol(), "mmyt");
assertEquals(parser.host(), "htl");
assertEquals(parser.path(), "/listing/");
assertEquals(parser.query(), "checkin=05192016&checkout=05212016&city=GOI");

URL.QueryParser query = parser.queryParser();

assertEquals(query.parameter("checkin"), "05192016");
assertEquals(query.parameter("checkout"), "05212016");
assertEquals(query.parameter("city"), "GOI");

assertEquals(
    URL.builder().setProtocol("https").setHost("www.vizury.com").toUrl(),
    "https://www.vizury.com/"
);
```

## XML

DSL like XML creation.

### Usage

```java
String xml = Xml.createWriter()
                    .beginElement("VAST")
                        .attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                        .attribute("xsi:noNamespaceSchemaLocation", "vast.xsd")
                        .attribute("version", "3.0")
                        .beginElement("Ad")
                            .attribute("id", "xyz")
                            .beginElement("Wrapper")
                                .element("AdSystem", "Vizury")
                                .element("VASTAdTagURI", Xml.wrapInCdata("https://www.vizury.com/"))
                                .element("Error", Xml.wrapInCdata("https://www.vizury.com/"))
                                .element("Impression", Xml.wrapInCdata("https://www.vizury.com/"))
                                .beginElement("Creatives")
                                    .beginElement("Creative")
                                        .attribute("AdID", "xyz")
                                    .endElement()
                                .endElement()
                            .endElement()
                        .endElement()
                    .endElement().toXml();
```

The above code creates the following XML (minus the indentation of course),

```xml
<VAST xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:noNamespaceSchemaLocation="vast.xsd"
      version="3.0">
    <Ad id="xyz">
        <Wrapper>
            <AdSystem>Vizury</AdSystem>
            <VASTAdTagURI><![CDATA[https://www.vizury.com/]]></VASTAdTagURI>
            <Error><![CDATA[https://www.vizury.com/]]></Error>
            <Impression><![CDATA[https://www.vizury.com/]]></Impression>
            <Creatives><Creative AdID="xyz" /></Creatives>
        </Wrapper>
    </Ad>
</VAST>
```

## Template

Simple string templates.

### Usage

```java
// Use '{{' and '}}' as marker (default).
Template template = Template.compile("To {{verb}} or not to {{verb}}.");
String sentence = template.format("verb", "be");
    
// Use '__' instead.
Template template = Template.createEngine("__", "__").compile("I tried to __verb__ your __noun__.");
String sentence = template.format("verb", "roast", "noun", "toast");
```